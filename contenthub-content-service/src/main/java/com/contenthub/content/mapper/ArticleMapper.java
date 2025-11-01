package com.contenthub.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.contenthub.content.domain.Article;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 文章Mapper
 */
public interface ArticleMapper extends BaseMapper<Article> {
    
    /**
     * 增加浏览量
     */
    @Update("UPDATE articles SET view_count = view_count + 1 WHERE id = #{articleId}")
    void incrementViewCount(@Param("articleId") Long articleId);
    
    /**
     * 增加点赞数
     */
    @Update("UPDATE articles SET like_count = like_count + 1 WHERE id = #{articleId}")
    void incrementLikeCount(@Param("articleId") Long articleId);
    
    /**
     * 减少点赞数
     */
    @Update("UPDATE articles SET like_count = GREATEST(like_count - 1, 0) WHERE id = #{articleId}")
    void decrementLikeCount(@Param("articleId") Long articleId);
    
    /**
     * 增加收藏数
     */
    @Update("UPDATE articles SET collect_count = collect_count + 1 WHERE id = #{articleId}")
    void incrementCollectCount(@Param("articleId") Long articleId);
    
    /**
     * 减少收藏数
     */
    @Update("UPDATE articles SET collect_count = GREATEST(collect_count - 1, 0) WHERE id = #{articleId}")
    void decrementCollectCount(@Param("articleId") Long articleId);
    
    /**
     * 增加分享数
     */
    @Update("UPDATE articles SET share_count = share_count + 1 WHERE id = #{articleId}")
    void incrementShareCount(@Param("articleId") Long articleId);
    
    /**
     * 增加评论数
     */
    @Update("UPDATE articles SET comment_count = comment_count + 1 WHERE id = #{articleId}")
    void incrementCommentCount(@Param("articleId") Long articleId);
    
    /**
     * 减少评论数
     */
    @Update("UPDATE articles SET comment_count = GREATEST(comment_count - 1, 0) WHERE id = #{articleId}")
    void decrementCommentCount(@Param("articleId") Long articleId);
}

