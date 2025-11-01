package com.contenthub.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建标签DTO
 */
@Data
@Schema(description = "创建标签请求")
public class CreateTagDTO {
    
    @Schema(description = "标签名称", required = true, example = "Java")
    @NotBlank(message = "标签名称不能为空")
    private String name;
    
    @Schema(description = "标签别名", example = "java")
    private String slug;
    
    @Schema(description = "标签描述", example = "Java编程语言相关")
    private String description;
    
    @Schema(description = "标签颜色", example = "#FF5722")
    private String color;
}

