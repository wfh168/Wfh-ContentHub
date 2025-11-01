package com.contenthub.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建分类DTO
 */
@Data
@Schema(description = "创建分类请求")
public class CreateCategoryDTO {
    
    @Schema(description = "分类名称", required = true, example = "技术分享")
    @NotBlank(message = "分类名称不能为空")
    private String name;
    
    @Schema(description = "分类别名（URL友好）", example = "tech")
    private String slug;
    
    @Schema(description = "分类描述", example = "技术相关的文章")
    private String description;
    
    @Schema(description = "分类图标", example = "icon-tech")
    private String icon;
    
    @Schema(description = "排序顺序", example = "1")
    private Integer sortOrder;
}

