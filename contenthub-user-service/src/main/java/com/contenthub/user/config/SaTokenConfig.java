package com.contenthub.user.config;

import com.contenthub.common.config.BaseSaTokenConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Sa-Token 配置类（用户服务）
 * 
 * 继承BaseSaTokenConfig，只需添加服务特定的排除路径
 */
@Configuration
public class SaTokenConfig extends BaseSaTokenConfig {
    
    /**
     * 获取服务特定的排除路径
     * 
     * @return 用户服务需要排除的路径（登录、注册、验证码接口）
     */
    @Override
    protected List<String> getServiceSpecificExcludePaths() {
        return Arrays.asList(
                // 登录、注册接口
                "/user/login",
                "/user/register",
                // 验证码接口
                "/user/captcha/**",
                // 批量获取用户信息接口（允许服务间调用，不需要Token）
                "/user/batch"
        );
    }
}
