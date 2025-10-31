package com.contenthub.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contenthub.common.exception.BusinessException;
import com.contenthub.common.result.Result;
import com.contenthub.user.domain.User;
import com.contenthub.user.domain.UserAuth;
import com.contenthub.user.dto.LoginDTO;
import com.contenthub.user.dto.RegisterDTO;
import com.contenthub.user.dto.UpdateUserDTO;
import com.contenthub.user.feign.FileServiceClient;
import com.contenthub.user.domain.UserFollow;
import com.contenthub.user.domain.UserBlacklist;
import com.contenthub.user.mapper.UserAuthMapper;
import com.contenthub.user.mapper.UserMapper;
import com.contenthub.user.mapper.UserFollowMapper;
import com.contenthub.user.mapper.UserBlacklistMapper;
import com.contenthub.user.vo.FollowUserVO;
import com.contenthub.user.service.CaptchaService;
import com.contenthub.user.service.UserService;
import com.contenthub.user.vo.LoginVO;
import com.contenthub.user.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final UserFollowMapper userFollowMapper;
    private final UserBlacklistMapper userBlacklistMapper;
    private final FileServiceClient fileServiceClient;
    private final CaptchaService captchaService;
    private final StringRedisTemplate redisTemplate;

    // Redis key前缀
    private static final String USER_CACHE_KEY_PREFIX = "user:info:";
    private static final String USER_TOKEN_KEY_PREFIX = "user:token:";
    
    // 用户信息缓存过期时间（1小时）
    private static final long USER_CACHE_EXPIRE_HOURS = 1;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO registerDTO) {
        // 1. 检查用户名是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, registerDTO.getUsername());
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 检查邮箱是否存在
        if (StrUtil.isNotBlank(registerDTO.getEmail())) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail, registerDTO.getEmail());
            if (userMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException("邮箱已被注册");
            }
        }

        // 3. 检查手机号是否存在
        if (StrUtil.isNotBlank(registerDTO.getPhone())) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, registerDTO.getPhone());
            if (userMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException("手机号已被注册");
            }
        }

        // 4. 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setNickname(registerDTO.getNickname());
        user.setEmail(registerDTO.getEmail());
        user.setPhone(registerDTO.getPhone());
        
        // 密码加密
        String hashedPassword = BCrypt.hashpw(registerDTO.getPassword());
        user.setPassword(hashedPassword);
        
        user.setStatus(1); // 正常状态
        user.setRole("user"); // 普通用户
        user.setLevel(1);
        user.setExperience(0);
        
        userMapper.insert(user);

        // 5. 创建用户认证记录
        // 邮箱认证
        if (StrUtil.isNotBlank(registerDTO.getEmail())) {
            UserAuth emailAuth = new UserAuth();
            emailAuth.setUserId(user.getId());
            emailAuth.setIdentityType("email");
            emailAuth.setIdentifier(registerDTO.getEmail());
            emailAuth.setCredential(hashedPassword);
            userAuthMapper.insert(emailAuth);
        }

        // 手机号认证
        if (StrUtil.isNotBlank(registerDTO.getPhone())) {
            UserAuth phoneAuth = new UserAuth();
            phoneAuth.setUserId(user.getId());
            phoneAuth.setIdentityType("phone");
            phoneAuth.setIdentifier(registerDTO.getPhone());
            phoneAuth.setCredential(hashedPassword);
            userAuthMapper.insert(phoneAuth);
        }

        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 验证验证码
        if (!captchaService.validateCaptcha(loginDTO.getCaptchaKey(), loginDTO.getCaptchaCode())) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 2. 查询用户信息（支持用户名、邮箱、手机号登录）
        User user = findUserByIdentifier(loginDTO.getUsername());
        
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 4. 验证密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 5. 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 6. 使用 Sa-Token 登录
        StpUtil.login(user.getId());
        
        // 7. 获取Token
        String token = StpUtil.getTokenValue();

        // 8. 缓存用户信息到Redis（1小时过期）
        cacheUserInfo(user);

        // 9. 缓存Token到Redis（用于快速验证，可选）
        String tokenKey = USER_TOKEN_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(tokenKey, token, 
                USER_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        
        // 验证Token写入是否成功
        String cachedToken = redisTemplate.opsForValue().get(tokenKey);
        if (StrUtil.isNotBlank(cachedToken)) {
            log.info("缓存Token到Redis成功: userId={}, key={}, token={}", user.getId(), tokenKey, token);
        } else {
            log.error("缓存Token到Redis失败: userId={}, key={}, value为空", user.getId(), tokenKey);
        }

        // 10. 构造返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatarUrl(user.getAvatarUrl());
        loginVO.setToken(token);

        log.info("用户登录成功: userId={}, username={}, token={}", user.getId(), user.getUsername(), token);
        return loginVO;
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        // 1. 先从Redis缓存获取用户信息
        String cacheKey = USER_CACHE_KEY_PREFIX + userId;
        String cachedUserInfo = redisTemplate.opsForValue().get(cacheKey);
        
        if (StrUtil.isNotBlank(cachedUserInfo)) {
            try {
                UserInfoVO userInfoVO = JSONUtil.toBean(cachedUserInfo, UserInfoVO.class);
                log.info("从缓存获取用户信息: userId={}", userId);
                return userInfoVO;
            } catch (Exception e) {
                log.warn("解析缓存用户信息失败: userId={}, error={}", userId, e.getMessage());
            }
        }

        // 2. 缓存未命中，从数据库查询
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 复制属性到VO（不包含密码等敏感信息）
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtil.copyProperties(user, userInfoVO);

        // 4. 缓存用户信息到Redis
        cacheUserInfo(user);

        log.info("从数据库获取用户信息: userId={}", userId);
        return userInfoVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(Long userId, UpdateUserDTO updateUserDTO) {
        // 1. 查询用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 检查邮箱是否已被其他用户使用
        if (StrUtil.isNotBlank(updateUserDTO.getEmail()) 
                && !updateUserDTO.getEmail().equals(user.getEmail())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail, updateUserDTO.getEmail())
                    .ne(User::getId, userId);
            if (userMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
        }

        // 3. 检查手机号是否已被其他用户使用
        if (StrUtil.isNotBlank(updateUserDTO.getPhone()) 
                && !updateUserDTO.getPhone().equals(user.getPhone())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, updateUserDTO.getPhone())
                    .ne(User::getId, userId);
            if (userMapper.selectCount(queryWrapper) > 0) {
                throw new BusinessException("手机号已被其他用户使用");
            }
        }

        // 4. 更新用户信息（只更新非空字段）
        User updateUser = new User();
        updateUser.setId(userId);
        
        if (StrUtil.isNotBlank(updateUserDTO.getNickname())) {
            updateUser.setNickname(updateUserDTO.getNickname());
        }
        if (StrUtil.isNotBlank(updateUserDTO.getEmail())) {
            updateUser.setEmail(updateUserDTO.getEmail());
        }
        if (StrUtil.isNotBlank(updateUserDTO.getPhone())) {
            updateUser.setPhone(updateUserDTO.getPhone());
        }
        if (StrUtil.isNotBlank(updateUserDTO.getAvatarUrl())) {
            updateUser.setAvatarUrl(updateUserDTO.getAvatarUrl());
        }
        if (StrUtil.isNotBlank(updateUserDTO.getBio())) {
            updateUser.setBio(updateUserDTO.getBio());
        }
        if (updateUserDTO.getGender() != null) {
            updateUser.setGender(updateUserDTO.getGender());
        }
        if (updateUserDTO.getBirthday() != null) {
            updateUser.setBirthday(updateUserDTO.getBirthday());
        }
        if (StrUtil.isNotBlank(updateUserDTO.getLocation())) {
            updateUser.setLocation(updateUserDTO.getLocation());
        }

        userMapper.updateById(updateUser);
        
        log.info("更新用户信息成功: userId={}", userId);
        
        // 5. 清除用户信息缓存，下次查询时重新加载
        String cacheKey = USER_CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(cacheKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadAvatar(Long userId, MultipartFile file) {
        // 1. 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 验证文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException("头像文件不能为空");
        }

        // 验证文件大小（最大2MB）
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException("头像文件大小不能超过2MB");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("头像文件必须是图片格式");
        }

        try {
            // 3. 调用文件服务上传头像，传递userId
            Result<String> uploadResult = fileServiceClient.uploadAvatar(file, userId);
            
            if (uploadResult == null || uploadResult.getCode() != 200) {
                throw new BusinessException("头像上传失败");
            }

            String avatarUrl = uploadResult.getData();
            
            // 4. 更新用户头像URL
            User updateUser = new User();
            updateUser.setId(userId);
            updateUser.setAvatarUrl(avatarUrl);
            userMapper.updateById(updateUser);

            log.info("用户头像上传成功: userId={}, avatarUrl={}", userId, avatarUrl);
            return avatarUrl;
            
        } catch (Exception e) {
            log.error("上传头像失败: userId={}, error={}", userId, e.getMessage(), e);
            throw new BusinessException("头像上传失败: " + e.getMessage());
        } finally {
            // 清除用户信息缓存
            String cacheKey = USER_CACHE_KEY_PREFIX + userId;
            redisTemplate.delete(cacheKey);
        }
    }

    /**
     * 缓存用户信息到Redis
     */
    private void cacheUserInfo(User user) {
        try {
            UserInfoVO userInfoVO = new UserInfoVO();
            BeanUtil.copyProperties(user, userInfoVO);
            
            String cacheKey = USER_CACHE_KEY_PREFIX + user.getId();
            String userInfoJson = JSONUtil.toJsonStr(userInfoVO);
            
            redisTemplate.opsForValue().set(cacheKey, userInfoJson, 
                    USER_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            // 验证写入是否成功
            String cachedValue = redisTemplate.opsForValue().get(cacheKey);
            if (StrUtil.isNotBlank(cachedValue)) {
                log.info("缓存用户信息到Redis成功: userId={}, key={}, valueLength={}", 
                        user.getId(), cacheKey, cachedValue.length());
            } else {
                log.error("缓存用户信息到Redis失败: userId={}, key={}, value为空", user.getId(), cacheKey);
            }
        } catch (Exception e) {
            log.warn("缓存用户信息失败: userId={}, error={}", user.getId(), e.getMessage());
            // 缓存失败不影响主流程，继续执行
        }
    }

    /**
     * 根据标识符查找用户（支持用户名、邮箱、手机号）
     */
    private User findUserByIdentifier(String identifier) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, identifier)
                .or()
                .eq(User::getEmail, identifier)
                .or()
                .eq(User::getPhone, identifier);
        
        return userMapper.selectOne(queryWrapper);
    }

    // ==================== 关注相关方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followUser(Long followerId, Long followedId) {
        // 1. 验证不能关注自己
        if (followerId.equals(followedId)) {
            throw new BusinessException("不能关注自己");
        }

        // 2. 验证用户是否存在
        User follower = userMapper.selectById(followerId);
        if (follower == null) {
            throw new BusinessException("关注者不存在");
        }
        User followed = userMapper.selectById(followedId);
        if (followed == null) {
            throw new BusinessException("被关注者不存在");
        }

        // 3. 检查是否已被拉黑
        if (isBlocked(followedId, followerId)) {
            throw new BusinessException("对方已将你拉黑，无法关注");
        }
        if (isBlocked(followerId, followedId)) {
            throw new BusinessException("你已将对方拉黑，无法关注");
        }

        // 4. 检查是否已经关注
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                   .eq(UserFollow::getFollowedId, followedId);
        UserFollow existing = userFollowMapper.selectOne(queryWrapper);
        if (existing != null) {
            throw new BusinessException("已经关注该用户");
        }

        // 5. 创建关注关系
        UserFollow follow = new UserFollow();
        follow.setFollowerId(followerId);
        follow.setFollowedId(followedId);
        follow.setCreatedAt(LocalDateTime.now());
        userFollowMapper.insert(follow);

        log.info("关注成功: followerId={}, followedId={}", followerId, followedId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollowUser(Long followerId, Long followedId) {
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                   .eq(UserFollow::getFollowedId, followedId);
        
        UserFollow follow = userFollowMapper.selectOne(queryWrapper);
        if (follow == null) {
            throw new BusinessException("未关注该用户");
        }

        userFollowMapper.deleteById(follow.getId());
        log.info("取消关注成功: followerId={}, followedId={}", followerId, followedId);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followedId) {
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, followerId)
                   .eq(UserFollow::getFollowedId, followedId);
        return userFollowMapper.selectOne(queryWrapper) != null;
    }

    @Override
    public List<FollowUserVO> getFollowingList(Long userId, Integer page, Integer size) {
        // 设置分页
        Page<UserFollow> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 20);
        
        // 查询关注列表
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowerId, userId)
                   .orderByDesc(UserFollow::getCreatedAt);
        
        IPage<UserFollow> pageResult = userFollowMapper.selectPage(pageParam, queryWrapper);
        List<UserFollow> follows = pageResult.getRecords();
        
        if (follows.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取被关注者ID列表
        List<Long> followedIds = follows.stream()
                .map(UserFollow::getFollowedId)
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        List<User> users = userMapper.selectBatchIds(followedIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 检查互相关注（查询被关注者是否也关注了当前用户）
        // 即：查询 where follower_id IN (followedIds) AND followed_id = userId
        LambdaQueryWrapper<UserFollow> mutualWrapper = new LambdaQueryWrapper<>();
        mutualWrapper.in(UserFollow::getFollowerId, followedIds)
                    .eq(UserFollow::getFollowedId, userId);
        List<UserFollow> mutualFollows = userFollowMapper.selectList(mutualWrapper);
        Set<Long> mutualSet = mutualFollows.stream()
                .map(UserFollow::getFollowerId)
                .collect(Collectors.toSet());

        // 转换为VO
        return follows.stream()
                .map(follow -> {
                    User user = userMap.get(follow.getFollowedId());
                    if (user == null) {
                        return null;
                    }
                    FollowUserVO vo = new FollowUserVO();
                    BeanUtil.copyProperties(user, vo);
                    vo.setUserId(user.getId());
                    vo.setIsMutualFollow(mutualSet.contains(user.getId()));
                    vo.setFollowTime(follow.getCreatedAt());
                    return vo;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<FollowUserVO> getFollowerList(Long userId, Integer page, Integer size) {
        // 设置分页
        Page<UserFollow> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 20);
        
        // 查询粉丝列表
        LambdaQueryWrapper<UserFollow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserFollow::getFollowedId, userId)
                   .orderByDesc(UserFollow::getCreatedAt);
        
        IPage<UserFollow> pageResult = userFollowMapper.selectPage(pageParam, queryWrapper);
        List<UserFollow> follows = pageResult.getRecords();
        
        if (follows.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取粉丝ID列表
        List<Long> followerIds = follows.stream()
                .map(UserFollow::getFollowerId)
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        List<User> users = userMapper.selectBatchIds(followerIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 检查互相关注
        LambdaQueryWrapper<UserFollow> mutualWrapper = new LambdaQueryWrapper<>();
        mutualWrapper.eq(UserFollow::getFollowerId, userId)
                    .in(UserFollow::getFollowedId, followerIds);
        List<UserFollow> mutualFollows = userFollowMapper.selectList(mutualWrapper);
        Set<Long> mutualSet = mutualFollows.stream()
                .map(UserFollow::getFollowedId)
                .collect(Collectors.toSet());

        // 转换为VO
        return follows.stream()
                .map(follow -> {
                    User user = userMap.get(follow.getFollowerId());
                    if (user == null) {
                        return null;
                    }
                    FollowUserVO vo = new FollowUserVO();
                    BeanUtil.copyProperties(user, vo);
                    vo.setUserId(user.getId());
                    vo.setIsMutualFollow(mutualSet.contains(user.getId()));
                    vo.setFollowTime(follow.getCreatedAt());
                    return vo;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getFollowStats(Long userId) {
        // 查询关注数（我关注的人）
        LambdaQueryWrapper<UserFollow> followingWrapper = new LambdaQueryWrapper<>();
        followingWrapper.eq(UserFollow::getFollowerId, userId);
        Long followingCount = userFollowMapper.selectCount(followingWrapper);

        // 查询粉丝数（关注我的人）
        LambdaQueryWrapper<UserFollow> followerWrapper = new LambdaQueryWrapper<>();
        followerWrapper.eq(UserFollow::getFollowedId, userId);
        Long followerCount = userFollowMapper.selectCount(followerWrapper);

        Map<String, Long> stats = new HashMap<>();
        stats.put("followingCount", followingCount);
        stats.put("followerCount", followerCount);
        return stats;
    }

    // ==================== 黑名单相关方法 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void blockUser(Long userId, Long blockedUserId) {
        // 1. 验证不能拉黑自己
        if (userId.equals(blockedUserId)) {
            throw new BusinessException("不能拉黑自己");
        }

        // 2. 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        User blockedUser = userMapper.selectById(blockedUserId);
        if (blockedUser == null) {
            throw new BusinessException("被拉黑用户不存在");
        }

        // 3. 检查是否已在黑名单中
        LambdaQueryWrapper<UserBlacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserBlacklist::getUserId, userId)
                   .eq(UserBlacklist::getBlockedUserId, blockedUserId);
        UserBlacklist existing = userBlacklistMapper.selectOne(queryWrapper);
        if (existing != null) {
            throw new BusinessException("该用户已在黑名单中");
        }

        // 4. 创建黑名单记录
        UserBlacklist blacklist = new UserBlacklist();
        blacklist.setUserId(userId);
        blacklist.setBlockedUserId(blockedUserId);
        blacklist.setCreatedAt(LocalDateTime.now());
        userBlacklistMapper.insert(blacklist);

        // 5. 自动取消关注关系（如果存在）
        LambdaQueryWrapper<UserFollow> followWrapper = new LambdaQueryWrapper<>();
        followWrapper.and(wrapper -> wrapper
                .eq(UserFollow::getFollowerId, userId).eq(UserFollow::getFollowedId, blockedUserId)
                .or()
                .eq(UserFollow::getFollowerId, blockedUserId).eq(UserFollow::getFollowedId, userId));
        List<UserFollow> follows = userFollowMapper.selectList(followWrapper);
        for (UserFollow follow : follows) {
            userFollowMapper.deleteById(follow.getId());
        }

        log.info("拉黑成功: userId={}, blockedUserId={}", userId, blockedUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unblockUser(Long userId, Long blockedUserId) {
        LambdaQueryWrapper<UserBlacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserBlacklist::getUserId, userId)
                   .eq(UserBlacklist::getBlockedUserId, blockedUserId);
        
        UserBlacklist blacklist = userBlacklistMapper.selectOne(queryWrapper);
        if (blacklist == null) {
            throw new BusinessException("该用户不在黑名单中");
        }

        userBlacklistMapper.deleteById(blacklist.getId());
        log.info("取消拉黑成功: userId={}, blockedUserId={}", userId, blockedUserId);
    }

    @Override
    public boolean isBlocked(Long userId, Long blockedUserId) {
        LambdaQueryWrapper<UserBlacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserBlacklist::getUserId, userId)
                   .eq(UserBlacklist::getBlockedUserId, blockedUserId);
        return userBlacklistMapper.selectOne(queryWrapper) != null;
    }

    @Override
    public List<UserInfoVO> getBlacklist(Long userId, Integer page, Integer size) {
        // 设置分页
        Page<UserBlacklist> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 20);
        
        // 查询黑名单列表
        LambdaQueryWrapper<UserBlacklist> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserBlacklist::getUserId, userId)
                   .orderByDesc(UserBlacklist::getCreatedAt);
        
        IPage<UserBlacklist> pageResult = userBlacklistMapper.selectPage(pageParam, queryWrapper);
        List<UserBlacklist> blacklists = pageResult.getRecords();
        
        if (blacklists.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取被拉黑用户ID列表
        List<Long> blockedIds = blacklists.stream()
                .map(UserBlacklist::getBlockedUserId)
                .collect(Collectors.toList());
        
        // 批量查询用户信息
        List<User> users = userMapper.selectBatchIds(blockedIds);
        
        // 转换为VO
        return users.stream()
                .map(user -> {
                    UserInfoVO vo = new UserInfoVO();
                    BeanUtil.copyProperties(user, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }
}

