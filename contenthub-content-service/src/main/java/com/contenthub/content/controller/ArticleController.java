package com.contenthub.content.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.contenthub.common.result.Result;
import com.contenthub.content.dto.CreateArticleDTO;
import com.contenthub.content.dto.UpdateArticleDTO;
import com.contenthub.content.feign.FileServiceClient;
import com.contenthub.content.service.ArticleService;
import com.contenthub.content.vo.ArticleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文章控制器
 */
@Tag(name = "文章管理", description = "文章相关的API接口")
@RestController
@RequestMapping("/content/article")
@RequiredArgsConstructor
public class ArticleController {
    
    private final ArticleService articleService;
    private final FileServiceClient fileServiceClient;
    
    @Operation(
            summary = "发布文章", 
            description = "创建并发布文章，支持上传封面图片", 
            security = {@SecurityRequirement(name = "Authorization")}
    )
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Long> createArticle(
            @Parameter(description = "标题", required = true) @RequestParam String title,
            @Parameter(description = "内容（Markdown格式）", required = true) @RequestParam String content,
            @Parameter(description = "摘要") @RequestParam(required = false) String summary,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(
                    description = "封面图片",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            ) @RequestPart(required = false) MultipartFile coverImage,
            @Parameter(description = "标签ID列表（逗号分隔）") @RequestParam(required = false) String tagIds,
            @Parameter(description = "状态（0-草稿，1-已发布）", example = "0") @RequestParam(required = false, defaultValue = "0") Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 1. 构建CreateArticleDTO
        CreateArticleDTO createArticleDTO = new CreateArticleDTO();
        createArticleDTO.setTitle(title);
        createArticleDTO.setContent(content);
        createArticleDTO.setSummary(summary);
        createArticleDTO.setCategoryId(categoryId);
        createArticleDTO.setStatus(status);
        
        // 2. 处理标签ID列表
        if (tagIds != null && !tagIds.trim().isEmpty()) {
            try {
                List<Long> tagIdList = java.util.Arrays.stream(tagIds.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(java.util.stream.Collectors.toList());
                createArticleDTO.setTagIds(tagIdList);
            } catch (Exception e) {
                return Result.error("标签ID格式错误");
            }
        }
        
        // 3. 处理封面图片上传
        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                Result<String> uploadResult = fileServiceClient.uploadFile(coverImage, "articles");
                if (uploadResult != null && uploadResult.getCode() == 200 && uploadResult.getData() != null) {
                    createArticleDTO.setCoverImage(uploadResult.getData());
                } else {
                    return Result.error("封面图片上传失败");
                }
            } catch (Exception e) {
                return Result.error("封面图片上传失败: " + e.getMessage());
            }
        }
        
        // 4. 创建文章
        Long articleId = articleService.createArticle(userId, createArticleDTO);
        return Result.success("发布成功", articleId);
    }
    
    @Operation(summary = "更新文章", description = "更新文章内容", security = {@SecurityRequirement(name = "Authorization")})
    @PutMapping("/{articleId}")
    public Result<String> updateArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId,
            @Validated @RequestBody UpdateArticleDTO updateArticleDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        articleService.updateArticle(articleId, userId, updateArticleDTO);
        return Result.success("更新成功", null);
    }
    
    @Operation(summary = "下架文章", description = "下架文章（作者操作：将文章状态改为草稿，不删除）", 
               security = {@SecurityRequirement(name = "Authorization")})
    @PutMapping("/{articleId}/unpublish")
    public Result<String> unpublishArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        articleService.unpublishArticle(articleId, userId);
        return Result.success("下架成功", null);
    }
    
    @Operation(summary = "删除文章", description = "删除文章（管理员功能：软删除）", 
               security = {@SecurityRequirement(name = "Authorization")})
    @DeleteMapping("/{articleId}")
    public Result<String> deleteArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        articleService.deleteArticle(articleId, userId);
        return Result.success("删除成功", null);
    }
    
    @Operation(summary = "获取文章详情", description = "根据文章ID获取文章详细信息")
    @GetMapping("/{articleId}")
    public Result<ArticleVO> getArticleDetail(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        Long currentUserId = null;
        try {
            currentUserId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 未登录用户也可以查看已发布的文章
        }
        ArticleVO article = articleService.getArticleDetail(articleId, currentUserId);
        return Result.success(article);
    }
    
    @Operation(summary = "获取文章列表", description = "获取文章列表（支持多种筛选和排序）")
    @GetMapping("/list")
    public Result<List<ArticleVO>> getArticleList(
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "标签ID") @RequestParam(required = false) Long tagId,
            @Parameter(description = "关键词（搜索标题和摘要）") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态（0-草稿，1-已发布，2-审核中）") @RequestParam(required = false) Integer status,
            @Parameter(description = "排序方式（latest-最新，hot-热门，likes-点赞数，views-浏览量，recommend-推荐）") 
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @Parameter(description = "页码", example = "1") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long currentUserId = null;
        try {
            currentUserId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 未登录用户也可以查看已发布的文章
        }
        List<ArticleVO> articles = articleService.getArticleList(categoryId, tagId, keyword, status, sortBy, page, size, currentUserId);
        return Result.success(articles);
    }
    
    @Operation(summary = "获取我的文章列表", description = "获取当前用户发布的文章列表（草稿箱）", 
               security = {@SecurityRequirement(name = "Authorization")})
    @GetMapping("/my")
    public Result<List<ArticleVO>> getMyArticleList(
            @Parameter(description = "状态（0-草稿，1-已发布）") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码", example = "1") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量", example = "20") @RequestParam(required = false, defaultValue = "20") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<ArticleVO> articles = articleService.getMyArticleList(userId, status, page, size);
        return Result.success(articles);
    }
    
    @Operation(summary = "点赞文章", description = "点赞文章", security = {@SecurityRequirement(name = "Authorization")})
    @PostMapping("/{articleId}/like")
    public Result<String> likeArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        articleService.likeArticle(articleId, userId);
        return Result.success("点赞成功", null);
    }
    
    @Operation(summary = "取消点赞", description = "取消点赞文章", security = {@SecurityRequirement(name = "Authorization")})
    @DeleteMapping("/{articleId}/like")
    public Result<String> unlikeArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        articleService.unlikeArticle(articleId, userId);
        return Result.success("取消点赞成功", null);
    }
    
    @Operation(summary = "收藏文章", description = "收藏文章", security = {@SecurityRequirement(name = "Authorization")})
    @PostMapping("/{articleId}/collect")
    public Result<String> collectArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        articleService.collectArticle(articleId, userId);
        return Result.success("收藏成功", null);
    }
    
    @Operation(summary = "取消收藏", description = "取消收藏文章", security = {@SecurityRequirement(name = "Authorization")})
    @DeleteMapping("/{articleId}/collect")
    public Result<String> uncollectArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        Long userId = StpUtil.getLoginIdAsLong();
        articleService.uncollectArticle(articleId, userId);
        return Result.success("取消收藏成功", null);
    }
    
    @Operation(summary = "分享文章", description = "分享文章（增加分享数）")
    @PostMapping("/{articleId}/share")
    public Result<String> shareArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId) {
        articleService.shareArticle(articleId);
        return Result.success("分享成功", null);
    }
    
    @Operation(summary = "置顶文章", description = "设置文章置顶（管理员功能）", security = {@SecurityRequirement(name = "Authorization")})
    @PutMapping("/{articleId}/top")
    public Result<String> setTopArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId,
            @Parameter(description = "是否置顶", required = true) @RequestParam Boolean isTop) {
        articleService.setTopArticle(articleId, isTop);
        return Result.success("设置成功", null);
    }
    
    @Operation(summary = "推荐文章", description = "设置文章推荐（管理员功能）", security = {@SecurityRequirement(name = "Authorization")})
    @PutMapping("/{articleId}/recommend")
    public Result<String> setRecommendArticle(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId,
            @Parameter(description = "是否推荐", required = true) @RequestParam Boolean isRecommend) {
        articleService.setRecommendArticle(articleId, isRecommend);
        return Result.success("设置成功", null);
    }
    
    @Operation(summary = "更新文章审核状态", description = "更新文章审核状态（仅管理员可操作）", 
               security = {@SecurityRequirement(name = "Authorization")})
    @PutMapping("/{articleId}/status")
    public Result<String> updateArticleStatus(
            @Parameter(description = "文章ID", required = true) @PathVariable Long articleId,
            @Parameter(description = "状态（1-已发布，2-审核中，3-审核失败）", required = true) @RequestParam Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        articleService.updateArticleStatus(articleId, status, userId);
        return Result.success("更新成功", null);
    }
}

