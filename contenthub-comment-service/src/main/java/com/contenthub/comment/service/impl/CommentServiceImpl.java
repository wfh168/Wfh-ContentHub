package com.contenthub.comment.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contenthub.comment.domain.Comment;
import com.contenthub.comment.domain.CommentLike;
import com.contenthub.comment.dto.CreateCommentDTO;
import com.contenthub.comment.feign.UserServiceClient;
import com.contenthub.comment.mapper.CommentLikeMapper;
import com.contenthub.comment.mapper.CommentMapper;
import com.contenthub.comment.service.CommentService;
import com.contenthub.comment.vo.CommentVO;
import com.contenthub.user.vo.UserInfoVO;
import com.contenthub.common.exception.BusinessException;
import com.contenthub.common.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    
    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final UserServiceClient userServiceClient;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(Long userId, CreateCommentDTO createCommentDTO) {
        // 1. 验证评论内容
        if (StrUtil.isBlank(createCommentDTO.getContent())) {
            throw new BusinessException("评论内容不能为空");
        }
        
        if (createCommentDTO.getContent().length() > 1000) {
            throw new BusinessException("评论内容不能超过1000字");
        }
        
        // 2. 如果是回复评论，验证父评论是否存在
        if (createCommentDTO.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(createCommentDTO.getParentId());
            if (parentComment == null) {
                throw new BusinessException("父评论不存在");
            }
            
            // 如果是二级评论，设置 rootId 为父评论的 rootId（如果父评论是一级评论，则 rootId 为父评论ID）
            if (createCommentDTO.getRootId() == null) {
                Long rootId = parentComment.getRootId();
                if (rootId == null) {
                    // 父评论是一级评论，rootId 就是父评论ID
                    createCommentDTO.setRootId(parentComment.getId());
                } else {
                    // 父评论是二级评论，使用父评论的 rootId
                    createCommentDTO.setRootId(rootId);
                }
            }
        }
        
        // 3. 创建评论
        Comment comment = new Comment();
        comment.setArticleId(createCommentDTO.getArticleId());
        comment.setUserId(userId);
        comment.setContent(createCommentDTO.getContent());
        comment.setParentId(createCommentDTO.getParentId());
        comment.setRootId(createCommentDTO.getRootId());
        comment.setLikeCount(0);
        comment.setStatus(1); // 正常状态
        
        commentMapper.insert(comment);
        
        log.info("发表评论成功: commentId={}, articleId={}, userId={}", 
                comment.getId(), createCommentDTO.getArticleId(), userId);
        
        return comment.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        // 1. 查询评论
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 2. 验证权限（只能删除自己的评论）
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的评论");
        }
        
        // 3. 软删除评论（MyBatis-Plus 自动处理）
        commentMapper.deleteById(commentId);
        
        log.info("删除评论成功: commentId={}, userId={}", commentId, userId);
    }
    
    @Override
    public List<CommentVO> getCommentList(Long articleId, Integer page, Integer size, Long currentUserId) {
        // 设置分页
        Page<Comment> pageParam = new Page<>(page != null ? page : 1, size != null ? size : 20);
        
        // 查询一级评论（parent_id 为 NULL）
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getArticleId, articleId)
                   .isNull(Comment::getParentId) // 只查询一级评论
                   .eq(Comment::getStatus, 1) // 只查询正常状态的评论
                   .orderByDesc(Comment::getCreatedAt);
        
        IPage<Comment> pageResult = commentMapper.selectPage(pageParam, queryWrapper);
        List<Comment> comments = pageResult.getRecords();
        
        if (comments.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取所有评论ID（用于查询点赞状态）
        List<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        
        // 获取评论的用户ID列表
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        // 查询二级评论（所有一级评论的子评论）
        // 二级评论条件：
        // 1. parentId 不为 NULL（确保是二级评论）
        // 2. rootId 在一级评论ID列表中，或者 parentId 在一级评论ID列表中（处理数据不一致的情况）
        LambdaQueryWrapper<Comment> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.isNotNull(Comment::getParentId) // 确保是二级评论（parentId不为空）
                   .and(wrapper -> wrapper
                       .in(Comment::getRootId, commentIds) // rootId 在一级评论ID中
                       .or()
                       .in(Comment::getParentId, commentIds)) // 或者 parentId 在一级评论ID中（处理rootId为null的情况）
                   .eq(Comment::getStatus, 1)
                   .orderByAsc(Comment::getCreatedAt); // 二级评论按时间正序
        
        List<Comment> childComments = commentMapper.selectList(childWrapper);
        log.debug("查询到一级评论数量: {}, 二级评论数量: {}", comments.size(), childComments.size());
        
        // 修正二级评论的 rootId
        childComments.forEach(child -> {
            if (child.getParentId() != null) {
                // 如果 rootId 为 null，但是 parentId 在一级评论ID列表中，则将 rootId 设置为 parentId
                if (child.getRootId() == null && commentIds.contains(child.getParentId())) {
                    child.setRootId(child.getParentId());
                    log.warn("修正二级评论的rootId（null情况）: commentId={}, parentId={}, rootId设置为={}", 
                            child.getId(), child.getParentId(), child.getRootId());
                }
                // 如果 rootId 不在当前一级评论ID列表中，但 parentId 在，则使用 parentId 作为 rootId
                else if (child.getRootId() != null && !commentIds.contains(child.getRootId()) 
                        && commentIds.contains(child.getParentId())) {
                    log.warn("修正二级评论的rootId（不匹配情况）: commentId={}, oldRootId={}, parentId={}, rootId设置为={}", 
                            child.getId(), child.getRootId(), child.getParentId(), child.getParentId());
                    child.setRootId(child.getParentId());
                }
            }
        });
        
        // 获取子评论的用户ID
        List<Long> childUserIds = childComments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        
        userIds.addAll(childUserIds);
        userIds = userIds.stream().distinct().collect(Collectors.toList());
        
        // 批量查询用户信息
        Map<Long, UserInfoVO> userMap = getUserMap(userIds);
        
        // 查询当前用户的点赞状态
        Set<Long> likedCommentIds = getLikedCommentIds(commentIds, currentUserId);
        
        // 查询子评论的点赞状态
        List<Long> childCommentIds = childComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        if (!childCommentIds.isEmpty()) {
            Set<Long> likedChildCommentIds = getLikedCommentIds(childCommentIds, currentUserId);
            likedCommentIds.addAll(likedChildCommentIds);
        }
        
        // 构建子评论Map（按 rootId 分组，如果 rootId 不在当前一级评论ID中，则按 parentId 分组）
        Map<Long, List<Comment>> childCommentMap = childComments.stream()
                .filter(comment -> comment.getRootId() != null || comment.getParentId() != null)
                .collect(Collectors.groupingBy(comment -> {
                    // 优先使用 rootId，如果 rootId 不在当前一级评论ID列表中，则使用 parentId
                    Long rootId = comment.getRootId();
                    if (rootId != null && commentIds.contains(rootId)) {
                        return rootId;
                    } else if (comment.getParentId() != null && commentIds.contains(comment.getParentId())) {
                        return comment.getParentId();
                    }
                    // 如果都不匹配，仍然使用 rootId（用于日志记录）
                    return rootId != null ? rootId : comment.getParentId();
                }));
        
        log.debug("子评论分组结果: rootIds={}, commentIds={}", 
                childCommentMap.keySet(), 
                childComments.stream().map(c -> c.getId() + "(rootId=" + c.getRootId() + ",parentId=" + c.getParentId() + ")").collect(Collectors.joining(",")));
        
        // 转换为VO
        List<CommentVO> result = comments.stream()
                .map(comment -> {
                    CommentVO vo = convertToVO(comment, userMap, likedCommentIds.contains(comment.getId()));
                    
                    // 添加子评论（根据 rootId 查找）
                    List<Comment> children = childCommentMap.getOrDefault(comment.getId(), Collections.emptyList());
                    log.debug("评论ID={}的子评论数量={}", comment.getId(), children.size());
                    
                    List<CommentVO> childVOs = children.stream()
                            .map(child -> convertToVO(child, userMap, likedCommentIds.contains(child.getId())))
                            .collect(Collectors.toList());
                    vo.setChildren(childVOs);
                    
                    return vo;
                })
                .collect(Collectors.toList());
        
        log.info("返回评论列表: 一级评论数量={}, 总评论数量={}", 
                result.size(), 
                result.size() + result.stream().mapToInt(c -> c.getChildren() != null ? c.getChildren().size() : 0).sum());
        
        return result;
    }
    
    @Override
    public CommentVO getCommentDetail(Long commentId, Long currentUserId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 查询用户信息
        Map<Long, UserInfoVO> userMap = getUserMap(Collections.singletonList(comment.getUserId()));
        
        // 查询点赞状态
        boolean isLiked = isLiked(commentId, currentUserId);
        
        return convertToVO(comment, userMap, isLiked);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Long commentId, Long userId) {
        // 1. 验证评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        
        // 2. 检查是否已点赞
        LambdaQueryWrapper<CommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentLike::getCommentId, commentId)
                   .eq(CommentLike::getUserId, userId);
        CommentLike existing = commentLikeMapper.selectOne(queryWrapper);
        
        if (existing != null) {
            throw new BusinessException("已经点赞过该评论");
        }
        
        // 3. 创建点赞记录
        CommentLike commentLike = new CommentLike();
        commentLike.setCommentId(commentId);
        commentLike.setUserId(userId);
        commentLike.setCreatedAt(LocalDateTime.now());
        commentLikeMapper.insert(commentLike);
        
        // 4. 更新评论点赞数
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentMapper.updateById(comment);
        
        log.info("点赞评论成功: commentId={}, userId={}", commentId, userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeComment(Long commentId, Long userId) {
        // 1. 查询点赞记录
        LambdaQueryWrapper<CommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentLike::getCommentId, commentId)
                   .eq(CommentLike::getUserId, userId);
        CommentLike commentLike = commentLikeMapper.selectOne(queryWrapper);
        
        if (commentLike == null) {
            throw new BusinessException("未点赞该评论");
        }
        
        // 2. 删除点赞记录
        commentLikeMapper.deleteById(commentLike.getId());
        
        // 3. 更新评论点赞数
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null && comment.getLikeCount() > 0) {
            comment.setLikeCount(comment.getLikeCount() - 1);
            commentMapper.updateById(comment);
        }
        
        log.info("取消点赞成功: commentId={}, userId={}", commentId, userId);
    }
    
    @Override
    public boolean isLiked(Long commentId, Long userId) {
        if (userId == null) {
            return false;
        }
        LambdaQueryWrapper<CommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentLike::getCommentId, commentId)
                   .eq(CommentLike::getUserId, userId);
        return commentLikeMapper.selectOne(queryWrapper) != null;
    }
    
    @Override
    public Long getCommentCount(Long articleId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getArticleId, articleId)
                   .eq(Comment::getStatus, 1);
        return commentMapper.selectCount(queryWrapper);
    }
    
    /**
     * 批量获取用户信息
     */
    private Map<Long, UserInfoVO> getUserMap(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        try {
            String userIdsStr = userIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            
            log.debug("调用用户服务批量获取用户信息: userIds={}", userIdsStr);
            Result<List<UserInfoVO>> result = userServiceClient.getUsersByIds(userIdsStr);
            
            if (result == null) {
                log.error("用户服务返回结果为空: userIds={}", userIdsStr);
                return Collections.emptyMap();
            }
            
            log.debug("用户服务返回: code={}, message={}, dataSize={}", 
                    result.getCode(), result.getMessage(), 
                    result.getData() != null ? result.getData().size() : 0);
            
            if (result.getCode() == 200 && result.getData() != null && !result.getData().isEmpty()) {
                Map<Long, UserInfoVO> userMap = result.getData().stream()
                        .filter(u -> u != null && u.getId() != null)
                        .collect(Collectors.toMap(UserInfoVO::getId, u -> u));
                log.info("成功获取用户信息: 请求数量={}, 返回数量={}, userMap={}", 
                        userIds.size(), userMap.size(), userMap.keySet());
                return userMap;
            } else {
                log.warn("用户服务返回异常: code={}, message={}, userIds={}", 
                        result.getCode(), result.getMessage(), userIdsStr);
            }
        } catch (Exception e) {
            log.error("批量获取用户信息异常: userIds={}, error={}", userIds, e.getMessage(), e);
        }
        
        return Collections.emptyMap();
    }
    
    /**
     * 获取用户点赞的评论ID集合
     */
    private Set<Long> getLikedCommentIds(List<Long> commentIds, Long userId) {
        if (userId == null || commentIds.isEmpty()) {
            return Collections.emptySet();
        }
        
        LambdaQueryWrapper<CommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CommentLike::getUserId, userId)
                   .in(CommentLike::getCommentId, commentIds);
        List<CommentLike> likes = commentLikeMapper.selectList(queryWrapper);
        
        return likes.stream()
                .map(CommentLike::getCommentId)
                .collect(Collectors.toSet());
    }
    
    /**
     * 转换为VO
     */
    private CommentVO convertToVO(Comment comment, Map<Long, UserInfoVO> userMap, boolean isLiked) {
        CommentVO vo = new CommentVO();
        BeanUtil.copyProperties(comment, vo);
        
        // 设置用户信息
        UserInfoVO user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
        }
        
        vo.setIsLiked(isLiked);
        
        return vo;
    }
}

