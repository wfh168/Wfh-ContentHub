package com.contenthub.comment.service;

import com.contenthub.comment.dto.CreateCommentDTO;
import com.contenthub.comment.vo.CommentVO;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {
    
    /**
     * 发表评论
     * @param userId 用户ID
     * @param createCommentDTO 评论DTO
     * @return 评论ID
     */
    Long createComment(Long userId, CreateCommentDTO createCommentDTO);
    
    /**
     * 删除评论（用户操作：只能删除自己的评论）
     * @param commentId 评论ID
     * @param userId 用户ID（用于权限验证）
     */
    void deleteComment(Long commentId, Long userId);
    
    /**
     * 管理员删除评论（管理员功能：软删除）
     * @param commentId 评论ID
     * @param userId 用户ID（用于验证是否为管理员）
     */
    void adminDeleteComment(Long commentId, Long userId);
    
    /**
     * 获取评论列表（支持分页）
     * @param articleId 文章ID
     * @param page 页码
     * @param size 每页数量
     * @param currentUserId 当前用户ID（用于判断是否已点赞）
     * @return 评论列表
     */
    List<CommentVO> getCommentList(Long articleId, Integer page, Integer size, Long currentUserId);
    
    /**
     * 获取评论详情
     * @param commentId 评论ID
     * @param currentUserId 当前用户ID（用于判断是否已点赞）
     * @return 评论详情
     */
    CommentVO getCommentDetail(Long commentId, Long currentUserId);
    
    /**
     * 点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void likeComment(Long commentId, Long userId);
    
    /**
     * 取消点赞
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void unlikeComment(Long commentId, Long userId);
    
    /**
     * 检查是否已点赞
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    boolean isLiked(Long commentId, Long userId);
    
    /**
     * 获取评论数量
     * @param articleId 文章ID
     * @return 评论数量
     */
    Long getCommentCount(Long articleId);
}

