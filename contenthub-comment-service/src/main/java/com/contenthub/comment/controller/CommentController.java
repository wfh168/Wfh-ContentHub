package com.contenthub.comment.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.contenthub.common.result.Result;
import com.contenthub.comment.dto.CreateCommentDTO;
import com.contenthub.comment.service.CommentService;
import com.contenthub.comment.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 */
@Tag(name = "评论管理", description = "评论相关接口")
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    @Operation(summary = "发表评论", description = "发表一级评论或回复评论（二级评论）")
    @PostMapping("/create")
    public Result<Long> createComment(
            @Validated @RequestBody CreateCommentDTO createCommentDTO) {
        long userId = StpUtil.getLoginIdAsLong();
        Long commentId = commentService.createComment(userId, createCommentDTO);
        return Result.success("评论发表成功", commentId);
    }
    
    @Operation(summary = "删除评论", description = "删除自己的评论（软删除）")
    @DeleteMapping("/{commentId}")
    public Result<String> deleteComment(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId) {
        long userId = StpUtil.getLoginIdAsLong();
        commentService.deleteComment(commentId, userId);
        return Result.success("删除评论成功", null);
    }
    
    @Operation(
            summary = "获取评论列表",
            description = "获取文章下的评论列表（支持分页）\n\n" +
                    "**说明：**\n" +
                    "- 返回一级评论，每个一级评论包含其下的所有二级评论\n" +
                    "- 一级评论按时间倒序排列\n" +
                    "- 二级评论按时间正序排列\n" +
                    "- 会显示当前用户对每条评论的点赞状态"
    )
    @GetMapping("/list")
    public Result<List<CommentVO>> getCommentList(
            @Parameter(description = "文章ID", required = true, example = "1")
            @RequestParam("articleId") Long articleId,
            @Parameter(description = "页码", example = "1")
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        // 从Sa-Token获取当前登录用户ID（如果没有登录则为null）
        Long currentUserId = null;
        try {
            if (StpUtil.isLogin()) {
                currentUserId = StpUtil.getLoginIdAsLong();
            }
        } catch (Exception e) {
            // 未登录，currentUserId 为 null
        }
        
        List<CommentVO> comments = commentService.getCommentList(articleId, page, size, currentUserId);
        return Result.success(comments);
    }
    
    @Operation(
            summary = "获取评论详情",
            description = "根据评论ID获取评论详情",
            security = {}  // 公开接口，不需要认证
    )
    @GetMapping("/{commentId}")
    public Result<CommentVO> getCommentDetail(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId) {
        // 从Sa-Token获取当前登录用户ID（如果没有登录则为null）
        Long currentUserId = null;
        try {
            if (StpUtil.isLogin()) {
                currentUserId = StpUtil.getLoginIdAsLong();
            }
        } catch (Exception e) {
            // 未登录，currentUserId 为 null
        }
        
        CommentVO comment = commentService.getCommentDetail(commentId, currentUserId);
        return Result.success(comment);
    }
    
    @Operation(summary = "点赞评论", description = "为指定评论点赞")
    @PostMapping("/{commentId}/like")
    public Result<String> likeComment(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId) {
        long userId = StpUtil.getLoginIdAsLong();
        commentService.likeComment(commentId, userId);
        return Result.success("点赞成功", null);
    }
    
    @Operation(summary = "取消点赞", description = "取消对指定评论的点赞")
    @DeleteMapping("/{commentId}/like")
    public Result<String> unlikeComment(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId) {
        long userId = StpUtil.getLoginIdAsLong();
        commentService.unlikeComment(commentId, userId);
        return Result.success("取消点赞成功", null);
    }
    
    @Operation(summary = "检查是否已点赞", description = "检查当前用户是否已点赞指定评论")
    @GetMapping("/{commentId}/like/check")
    public Result<Boolean> checkLiked(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId) {
        long userId = StpUtil.getLoginIdAsLong();
        boolean isLiked = commentService.isLiked(commentId, userId);
        return Result.success(isLiked);
    }
    
    @Operation(summary = "获取评论数量", description = "获取指定文章下的评论数量")
    @GetMapping("/count")
    public Result<Long> getCommentCount(
            @Parameter(description = "文章ID", required = true, example = "1")
            @RequestParam("articleId") Long articleId) {
        Long count = commentService.getCommentCount(articleId);
        return Result.success(count);
    }
}

