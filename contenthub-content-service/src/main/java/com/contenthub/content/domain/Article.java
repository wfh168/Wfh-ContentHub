package com.contenthub.content.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体
 */
@Data
@TableName("articles")
public class Article {
    
    /**
     * 文章ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 作者ID
     */
    private Long userId;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 文章别名（URL友好）
     */
    private String slug;
    
    /**
     * 摘要
     */
    private String summary;
    
    /**
     * 封面图片
     */
    private String coverImage;
    
    /**
     * 内容（Markdown格式）
     */
    private String content;
    
    /**
     * HTML内容
     */
    private String htmlContent;
    
    /**
     * 状态（0-草稿，1-已发布，2-审核中，3-审核失败，4-已删除）
     */
    private Integer status;
    
    /**
     * 浏览量
     */
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 收藏数
     */
    private Integer collectCount;
    
    /**
     * 分享数
     */
    private Integer shareCount;
    
    /**
     * 是否置顶（0-否，1-是）
     */
    private Integer isTop;
    
    /**
     * 是否推荐（0-否，1-是）
     */
    private Integer isRecommend;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishedAt;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    /**
     * 逻辑删除（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;
}

