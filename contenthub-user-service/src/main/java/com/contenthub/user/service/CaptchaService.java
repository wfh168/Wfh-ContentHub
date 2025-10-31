package com.contenthub.user.service;

import com.contenthub.user.vo.CaptchaVO;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 生成验证码
     * @return 验证码VO（包含key和图片base64）
     */
    CaptchaVO generateCaptcha();

    /**
     * 验证验证码
     * @param captchaKey 验证码key
     * @param captchaCode 验证码
     * @return 验证是否通过
     */
    boolean validateCaptcha(String captchaKey, String captchaCode);
}

