package com.contenthub.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 基础配置类（通用部分）
 * 
 * 各服务可以继承此类，只需添加服务特定的排除路径
 */
@Configuration
@ConditionalOnClass(name = "cn.dev33.satoken.stp.StpUtil")
public class BaseSaTokenConfig implements WebMvcConfigurer {
    
    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 获取通用排除路径 + 服务特定排除路径
        List<String> excludePaths = new ArrayList<>();
        
        // 通用排除路径（所有服务都需要的）
        excludePaths.addAll(getCommonExcludePaths());
        
        // 服务特定排除路径（由子类或配置提供）
        excludePaths.addAll(getServiceSpecificExcludePaths());
        
        // 注册 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(excludePaths.toArray(new String[0]));
    }
    
    /**
     * 获取通用排除路径（所有服务都需要的）
     * 
     * @return 通用排除路径列表
     */
    protected List<String> getCommonExcludePaths() {
        List<String> paths = new ArrayList<>();
        
        // Swagger文档
        paths.add("/swagger-ui.html");
        paths.add("/swagger-ui/**");
        paths.add("/v3/api-docs");
        paths.add("/v3/api-docs/**");
        paths.add("/swagger-resources/**");
        paths.add("/webjars/**");
        paths.add("/doc.html");
        
        // Actuator监控端点
        paths.add("/actuator/**");
        
        // 错误页面
        paths.add("/error");
        paths.add("/favicon.ico");
        
        return paths;
    }
    
    /**
     * 获取服务特定的排除路径（由子类重写或通过配置提供）
     * 
     * @return 服务特定的排除路径列表
     */
    protected List<String> getServiceSpecificExcludePaths() {
        // 默认无服务特定排除路径，子类可以重写此方法
        return new ArrayList<>();
    }
}

