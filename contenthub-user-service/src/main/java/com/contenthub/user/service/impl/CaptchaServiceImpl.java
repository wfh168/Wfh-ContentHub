package com.contenthub.user.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.contenthub.common.exception.BusinessException;
import com.contenthub.user.service.CaptchaService;
import com.contenthub.user.vo.CaptchaVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    private final StringRedisTemplate redisTemplate;

    // Redis key前缀
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";

    // 验证码过期时间（5分钟）
    private static final long CAPTCHA_EXPIRE_MINUTES = 5;

    // 验证码字符集（去除容易混淆的字符：0, O, 1, I, L）
    private static final String CAPTCHA_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    // 验证码长度
    private static final int CAPTCHA_LENGTH = 4;

    // 图片宽度
    private static final int IMAGE_WIDTH = 120;

    // 图片高度
    private static final int IMAGE_HEIGHT = 40;

    @Override
    public CaptchaVO generateCaptcha() {
        // 1. 生成验证码key
        String captchaKey = IdUtil.fastSimpleUUID();

        // 2. 生成随机验证码（4位数字+字母）
        String captchaCode = RandomUtil.randomString(CAPTCHA_CHARS, CAPTCHA_LENGTH);

        // 3. 生成验证码图片
        BufferedImage image = createCaptchaImage(captchaCode);

        // 4. 将图片转换为Base64
        String imageBase64 = imageToBase64(image);

        // 5. 存储验证码到Redis（5分钟过期）
        String redisKey = CAPTCHA_KEY_PREFIX + captchaKey;
        redisTemplate.opsForValue().set(redisKey, captchaCode.toLowerCase(), 
                CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 6. 构造返回结果
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaKey(captchaKey);
        captchaVO.setCaptchaImage("data:image/png;base64," + imageBase64);

        log.info("生成验证码: key={}, code={}", captchaKey, captchaCode);
        return captchaVO;
    }

    @Override
    public boolean validateCaptcha(String captchaKey, String captchaCode) {
        if (StrUtil.isBlank(captchaKey) || StrUtil.isBlank(captchaCode)) {
            return false;
        }

        // 从Redis获取验证码
        String redisKey = CAPTCHA_KEY_PREFIX + captchaKey;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (StrUtil.isBlank(storedCode)) {
            log.warn("验证码已过期或不存在: key={}", captchaKey);
            return false;
        }

        // 验证码不区分大小写
        boolean isValid = storedCode.equalsIgnoreCase(captchaCode.trim());

        // 验证成功后删除验证码（一次性使用）
        if (isValid) {
            redisTemplate.delete(redisKey);
            log.info("验证码验证成功: key={}", captchaKey);
        } else {
            log.warn("验证码验证失败: key={}, input={}, stored={}", captchaKey, captchaCode, storedCode);
        }

        return isValid;
    }

    /**
     * 创建验证码图片
     */
    private BufferedImage createCaptchaImage(String code) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置抗锯齿
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // 绘制干扰线
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 3; i++) {
            int x1 = RandomUtil.randomInt(0, IMAGE_WIDTH);
            int y1 = RandomUtil.randomInt(0, IMAGE_HEIGHT);
            int x2 = RandomUtil.randomInt(0, IMAGE_WIDTH);
            int y2 = RandomUtil.randomInt(0, IMAGE_HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制验证码文字
        Font font = new Font("Arial", Font.BOLD, 28);
        g.setFont(font);

        int charWidth = IMAGE_WIDTH / (CAPTCHA_LENGTH + 1);
        for (int i = 0; i < code.length(); i++) {
            // 随机颜色
            Color color = new Color(
                    RandomUtil.randomInt(50, 200),
                    RandomUtil.randomInt(50, 200),
                    RandomUtil.randomInt(50, 200)
            );
            g.setColor(color);

            // 随机位置和角度
            int x = charWidth * (i + 1);
            int y = IMAGE_HEIGHT / 2 + RandomUtil.randomInt(-5, 5);
            double angle = RandomUtil.randomDouble(-0.3, 0.3);

            g.translate(x, y);
            g.rotate(angle);
            g.drawString(String.valueOf(code.charAt(i)), 0, 0);
            g.rotate(-angle);
            g.translate(-x, -y);
        }

        // 绘制干扰点
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 20; i++) {
            int x = RandomUtil.randomInt(0, IMAGE_WIDTH);
            int y = RandomUtil.randomInt(0, IMAGE_HEIGHT);
            g.fillOval(x, y, 2, 2);
        }

        g.dispose();
        return image;
    }

    /**
     * 将图片转换为Base64
     */
    private String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encode(imageBytes);
        } catch (IOException e) {
            log.error("图片转Base64失败", e);
            throw new BusinessException("验证码生成失败");
        }
    }
}

