package com.contenthub.content.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 标签VO
 */
@Data
@Schema(description = "标签信息")
public class TagVO {
    
    @Schema(description = "标签ID")
    private Long id;
    
    @Schema(description = "标签名称")
    private String name;
    
    @Schema(description = "标签别名")
    private String slug;
    
    @Schema(description = "标签描述")
    private String description;
    
    @Schema(description = "标签颜色")
    private String color;
    
    @Schema(description = "文章数量")
    private Integer articleCount;
}

