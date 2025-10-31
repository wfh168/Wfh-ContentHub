package com.contenthub.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 */
@Data
@Schema(description = "登录请求")
public class LoginDTO {

    @Schema(description = "用户名/邮箱/手机号", required = true, example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", required = true, example = "admin123")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "验证码key（从验证码接口获取）", required = true, example = "abc123def456")
    @NotBlank(message = "验证码key不能为空")
    private String captchaKey;

    @Schema(description = "验证码", required = true, example = "A3B4")
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}

