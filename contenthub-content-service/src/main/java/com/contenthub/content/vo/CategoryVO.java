package com.contenthub.content.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分类VO
 */
@Data
@Schema(description = "分类信息")
public class CategoryVO {
    
    @Schema(description = "分类ID")
    private Long id;
    
    @Schema(description = "分类名称")
    private String name;
    
    @Schema(description = "分类别名")
    private String slug;
    
    @Schema(description = "分类描述")
    private String description;
    
    @Schema(description = "分类图标")
    private String icon;
    
    @Schema(description = "排序顺序")
    private Integer sortOrder;
    
    @Schema(description = "状态（0-禁用，1-启用）")
    private Integer status;
}

