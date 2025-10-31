package com.contenthub.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 验证码VO
 */
@Data
@Schema(description = "验证码")
public class CaptchaVO {

    @Schema(description = "验证码key（用于登录时验证）", example = "abc123def456")
    private String captchaKey;

    @Schema(description = "验证码图片Base64（data:image/png;base64,...）", example = "data:image/png;base64,iVBORw0KGgo...")
    private String captchaImage;
}

