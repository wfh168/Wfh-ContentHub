package com.contenthub.user.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Feign请求拦截器
 * 用于在Feign调用时自动传递Token（从当前HTTP请求中获取）
 */
@Slf4j
@Component
public class FeignRequestInterceptor implements RequestInterceptor {
    
    /**
     * Sa-Token的Token名称（Header名称）
     */
    private static final String TOKEN_NAME = "Authorization";
    
    @Override
    public void apply(RequestTemplate template) {
        try {
            // 从当前HTTP请求中获取Token
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                if (request != null) {
                    // 从请求头中获取Token
                    String token = request.getHeader(TOKEN_NAME);
                    if (token != null && !token.isEmpty()) {
                        // 将Token添加到Feign请求头
                        template.header(TOKEN_NAME, token);
                        log.debug("Feign请求自动传递Token: url={}, tokenPrefix={}", 
                                template.url(), token.length() > 10 ? token.substring(0, 10) + "..." : token);
                    } else {
                        log.debug("Feign请求未找到Token，跳过传递: url={}", template.url());
                    }
                }
            } else {
                // 非Web环境（如定时任务），无法获取请求头
                log.debug("非Web环境，无法传递Token: url={}", template.url());
            }
        } catch (Exception e) {
            // 如果获取Token失败，记录警告但不影响请求
            log.warn("Feign请求传递Token失败: url={}, error={}", 
                    template.url(), e.getMessage());
        }
    }
}

