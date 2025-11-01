package com.contenthub.content.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章VO
 */
@Data
@Schema(description = "文章信息")
public class ArticleVO {
    
    @Schema(description = "文章ID")
    private Long id;
    
    @Schema(description = "作者ID")
    private Long userId;
    
    @Schema(description = "作者用户名")
    private String authorUsername;
    
    @Schema(description = "作者昵称")
    private String authorNickname;
    
    @Schema(description = "作者头像")
    private String authorAvatar;
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "分类名称")
    private String categoryName;
    
    @Schema(description = "标题")
    private String title;
    
    @Schema(description = "文章别名")
    private String slug;
    
    @Schema(description = "摘要")
    private String summary;
    
    @Schema(description = "封面图片")
    private String coverImage;
    
    @Schema(description = "内容（Markdown格式）")
    private String content;
    
    @Schema(description = "HTML内容")
    private String htmlContent;
    
    @Schema(description = "状态（0-草稿，1-已发布，2-审核中，3-审核失败）")
    private Integer status;
    
    @Schema(description = "浏览量")
    private Integer viewCount;
    
    @Schema(description = "点赞数")
    private Integer likeCount;
    
    @Schema(description = "评论数")
    private Integer commentCount;
    
    @Schema(description = "收藏数")
    private Integer collectCount;
    
    @Schema(description = "分享数")
    private Integer shareCount;
    
    @Schema(description = "是否置顶")
    private Boolean isTop;
    
    @Schema(description = "是否推荐")
    private Boolean isRecommend;
    
    @Schema(description = "是否已点赞（当前用户）")
    private Boolean isLiked;
    
    @Schema(description = "是否已收藏（当前用户）")
    private Boolean isCollected;
    
    @Schema(description = "标签列表")
    private List<TagVO> tags;
    
    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;
    
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}

