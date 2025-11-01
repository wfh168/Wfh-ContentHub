package com.contenthub.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 更新文章DTO
 */
@Data
@Schema(description = "更新文章请求")
public class UpdateArticleDTO {
    
    @Schema(description = "标题", example = "Spring Boot 入门教程（更新）")
    private String title;
    
    @Schema(description = "内容（Markdown格式）")
    private String content;
    
    @Schema(description = "摘要", example = "这是一篇关于Spring Boot的文章")
    private String summary;
    
    @Schema(description = "分类ID", example = "1")
    private Long categoryId;
    
    @Schema(description = "封面图片URL", example = "http://example.com/cover.jpg")
    private String coverImage;
    
    @Schema(description = "标签ID列表", example = "[1, 2, 3]")
    private List<Long> tagIds;
    
    @Schema(description = "状态（0-草稿，1-已发布，2-审核中）", example = "1")
    private Integer status;
}

