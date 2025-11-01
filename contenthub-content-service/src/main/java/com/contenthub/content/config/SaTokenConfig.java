package com.contenthub.content.config;

import com.contenthub.common.config.BaseSaTokenConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Sa-Token 配置类（内容服务）
 * 
 * 继承BaseSaTokenConfig，只需添加服务特定的排除路径
 */
@Configuration
public class SaTokenConfig extends BaseSaTokenConfig {
    
    /**
     * 获取服务特定的排除路径
     * 
     * @return 内容服务需要排除的路径（公开文章列表等，根据业务需求调整）
     */
    @Override
    protected List<String> getServiceSpecificExcludePaths() {
        // 公开接口（不需要登录）
        return Arrays.asList(
                // 文章相关
                "/content/article/list",           // 获取文章列表
                "/content/article/*",              // 获取文章详情（GET请求）
                // 分类相关
                "/content/category/list",         // 获取分类列表
                "/content/category/*",             // 获取分类详情（GET请求）
                // 标签相关
                "/content/tag/list",              // 获取标签列表
                "/content/tag/*",                 // 获取标签详情（GET请求）
                "/content/tag/article/*"          // 根据文章ID获取标签
        );
    }
}

