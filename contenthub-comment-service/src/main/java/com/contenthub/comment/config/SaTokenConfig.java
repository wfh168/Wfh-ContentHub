package com.contenthub.comment.config;

import cn.dev33.satoken.stp.StpUtil;
import com.contenthub.common.config.BaseSaTokenConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Sa-Token 配置类（评论服务）
 * 
 * 继承BaseSaTokenConfig，需要自定义拦截器以支持基于HTTP方法的路径排除
 */
@Configuration
public class SaTokenConfig extends BaseSaTokenConfig {
    
    /**
     * 注册 Sa-Token 拦截器（覆盖父类方法，支持基于HTTP方法的排除）
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 获取通用排除路径 + 服务特定排除路径
        List<String> excludePaths = new ArrayList<>();
        excludePaths.addAll(getCommonExcludePaths());
        excludePaths.addAll(getServiceSpecificExcludePaths());
        
        // 注册自定义拦截器（支持基于HTTP方法的排除）
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                String requestMethod = request.getMethod();
                String requestPath = request.getRequestURI();
                
                // GET /comment/{commentId} 允许未登录访问
                if ("GET".equals(requestMethod) && requestPath.matches("/comment/\\d+")) {
                    // GET请求获取评论详情，允许未登录，跳过验证
                    return true;
                }
                
                // 其他请求需要Token验证
                try {
                    StpUtil.checkLogin();
                    return true;
                } catch (Exception e) {
                    throw e;
                }
            }
        })
        .addPathPatterns("/**")
        .excludePathPatterns(excludePaths.toArray(new String[0]));
    }
    
    /**
     * 获取服务特定的排除路径
     * 
     * @return 评论服务需要排除的路径（公开评论列表等）
     */
    @Override
    protected List<String> getServiceSpecificExcludePaths() {
        // 评论列表和数量可以公开访问（不需要登录）
        return Arrays.asList(
                "/comment/list",           // 获取评论列表（GET）
                "/comment/count"           // 获取评论数量（GET）
        );
    }
}

