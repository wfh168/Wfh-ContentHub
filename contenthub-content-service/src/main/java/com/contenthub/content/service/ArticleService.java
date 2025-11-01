package com.contenthub.content.service;

import com.contenthub.content.dto.CreateArticleDTO;
import com.contenthub.content.dto.UpdateArticleDTO;
import com.contenthub.content.vo.ArticleVO;

import java.util.List;

/**
 * 文章服务接口
 */
public interface ArticleService {
    
    /**
     * 发布文章
     * @param userId 用户ID
     * @param createArticleDTO 创建文章DTO
     * @return 文章ID
     */
    Long createArticle(Long userId, CreateArticleDTO createArticleDTO);
    
    /**
     * 更新文章
     * @param articleId 文章ID
     * @param userId 用户ID（用于权限验证）
     * @param updateArticleDTO 更新文章DTO
     */
    void updateArticle(Long articleId, Long userId, UpdateArticleDTO updateArticleDTO);
    
    /**
     * 删除文章（软删除）
     * @param articleId 文章ID
     * @param userId 用户ID（用于权限验证）
     */
    void deleteArticle(Long articleId, Long userId);
    
    /**
     * 获取文章详情
     * @param articleId 文章ID
     * @param currentUserId 当前用户ID（用于判断是否已点赞、收藏）
     * @return 文章详情
     */
    ArticleVO getArticleDetail(Long articleId, Long currentUserId);
    
    /**
     * 获取文章列表（支持多种排序和筛选）
     * @param categoryId 分类ID（可选）
     * @param tagId 标签ID（可选）
     * @param keyword 关键词（搜索标题和摘要）
     * @param status 状态（0-草稿，1-已发布，2-审核中）
     * @param sortBy 排序方式（latest-最新，hot-热门，likes-点赞数，views-浏览量）
     * @param page 页码
     * @param size 每页数量
     * @param currentUserId 当前用户ID（用于判断是否已点赞、收藏）
     * @return 文章列表
     */
    List<ArticleVO> getArticleList(Long categoryId, Long tagId, String keyword, 
                                   Integer status, String sortBy, Integer page, 
                                   Integer size, Long currentUserId);
    
    /**
     * 获取我的文章列表（草稿箱）
     * @param userId 用户ID
     * @param status 状态（0-草稿，1-已发布）
     * @param page 页码
     * @param size 每页数量
     * @return 文章列表
     */
    List<ArticleVO> getMyArticleList(Long userId, Integer status, Integer page, Integer size);
    
    /**
     * 点赞文章
     * @param articleId 文章ID
     * @param userId 用户ID
     */
    void likeArticle(Long articleId, Long userId);
    
    /**
     * 取消点赞
     * @param articleId 文章ID
     * @param userId 用户ID
     */
    void unlikeArticle(Long articleId, Long userId);
    
    /**
     * 收藏文章
     * @param articleId 文章ID
     * @param userId 用户ID
     */
    void collectArticle(Long articleId, Long userId);
    
    /**
     * 取消收藏
     * @param articleId 文章ID
     * @param userId 用户ID
     */
    void uncollectArticle(Long articleId, Long userId);
    
    /**
     * 分享文章（增加分享数）
     * @param articleId 文章ID
     */
    void shareArticle(Long articleId);
    
    /**
     * 增加浏览量
     * @param articleId 文章ID
     */
    void incrementViewCount(Long articleId);
    
    /**
     * 检查是否已点赞
     * @param articleId 文章ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    boolean isLiked(Long articleId, Long userId);
    
    /**
     * 检查是否已收藏
     * @param articleId 文章ID
     * @param userId 用户ID
     * @return 是否已收藏
     */
    boolean isCollected(Long articleId, Long userId);
    
    /**
     * 置顶文章
     * @param articleId 文章ID
     * @param isTop 是否置顶
     */
    void setTopArticle(Long articleId, boolean isTop);
    
    /**
     * 推荐文章
     * @param articleId 文章ID
     * @param isRecommend 是否推荐
     */
    void setRecommendArticle(Long articleId, boolean isRecommend);
    
    /**
     * 更新文章审核状态（管理员功能）
     * @param articleId 文章ID
     * @param status 状态（1-已发布，2-审核中，3-审核失败）
     * @param userId 用户ID（用于验证是否为管理员）
     */
    void updateArticleStatus(Long articleId, Integer status, Long userId);
}

