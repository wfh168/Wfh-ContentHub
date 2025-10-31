package com.contenthub.comment.config;

import com.contenthub.common.config.BaseSaTokenConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Sa-Token 配置类（评论服务）
 * 
 * 继承BaseSaTokenConfig，只需添加服务特定的排除路径
 */
@Configuration
public class SaTokenConfig extends BaseSaTokenConfig {
    
    /**
     * 获取服务特定的排除路径
     * 
     * @return 评论服务需要排除的路径（公开评论列表等，根据业务需求调整）
     */
    @Override
    protected List<String> getServiceSpecificExcludePaths() {
        // 评论列表、详情和数量可以公开访问（不需要登录）
        // 注意：由于拦截器无法区分 HTTP 方法，GET /comment/{commentId} 会被排除
        // DELETE /comment/{commentId} 需要在 Controller 中手动验证（已实现）
        return Arrays.asList(
                "/comment/list",           // 获取评论列表（GET）
                "/comment/count",          // 获取评论数量（GET）
                "/comment/*"               // 获取评论详情（GET /comment/{commentId}）
                                          // DELETE /comment/{commentId} 仍需要登录，在Controller中处理
        );
    }
}

