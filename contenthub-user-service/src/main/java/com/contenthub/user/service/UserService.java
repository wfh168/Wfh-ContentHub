package com.contenthub.user.service;

import com.contenthub.user.dto.LoginDTO;
import com.contenthub.user.dto.RegisterDTO;
import com.contenthub.user.dto.UpdateUserDTO;
import com.contenthub.user.vo.LoginVO;
import com.contenthub.user.vo.UserInfoVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    void register(RegisterDTO registerDTO);

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取用户信息
     */
    UserInfoVO getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    void updateUserInfo(Long userId, UpdateUserDTO updateUserDTO);

    /**
     * 上传并更新用户头像
     */
    String uploadAvatar(Long userId, MultipartFile file);

    /**
     * 关注用户
     */
    void followUser(Long followerId, Long followedId);

    /**
     * 取消关注
     */
    void unfollowUser(Long followerId, Long followedId);

    /**
     * 检查是否关注
     */
    boolean isFollowing(Long followerId, Long followedId);

    /**
     * 获取关注列表（我关注的人）
     */
    java.util.List<com.contenthub.user.vo.FollowUserVO> getFollowingList(Long userId, Integer page, Integer size);

    /**
     * 获取粉丝列表（关注我的人）
     */
    java.util.List<com.contenthub.user.vo.FollowUserVO> getFollowerList(Long userId, Integer page, Integer size);

    /**
     * 获取关注数和粉丝数
     */
    java.util.Map<String, Long> getFollowStats(Long userId);

    /**
     * 拉黑用户
     */
    void blockUser(Long userId, Long blockedUserId);

    /**
     * 取消拉黑
     */
    void unblockUser(Long userId, Long blockedUserId);

    /**
     * 检查是否在黑名单中
     */
    boolean isBlocked(Long userId, Long blockedUserId);

    /**
     * 获取黑名单列表
     */
    java.util.List<com.contenthub.user.vo.UserInfoVO> getBlacklist(Long userId, Integer page, Integer size);
}

