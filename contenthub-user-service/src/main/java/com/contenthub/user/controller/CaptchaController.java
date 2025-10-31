package com.contenthub.user.controller;

import com.contenthub.common.result.Result;
import com.contenthub.user.service.CaptchaService;
import com.contenthub.user.vo.CaptchaVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码控制器
 */
@Tag(name = "验证码管理", description = "验证码相关接口")
@RestController
@RequestMapping("/user/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @Operation(summary = "生成验证码", description = "生成图片验证码，返回key和base64图片", security = {})
    @GetMapping("/generate")
    public Result<CaptchaVO> generateCaptcha() {
        CaptchaVO captchaVO = captchaService.generateCaptcha();
        return Result.success(captchaVO);
    }
}

