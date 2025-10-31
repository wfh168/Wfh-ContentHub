package com.contenthub.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建评论DTO
 */
@Data
@Schema(description = "创建评论请求")
public class CreateCommentDTO {
    
    @Schema(description = "文章ID", required = true, example = "1")
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
    
    @Schema(description = "评论内容", required = true, example = "这是一条评论")
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    @Schema(description = "父评论ID（回复评论时使用，为NULL表示一级评论）", example = "10")
    private Long parentId;
    
    @Schema(description = "根评论ID（回复评论时使用，指向一级评论）", example = "10")
    private Long rootId;
}

