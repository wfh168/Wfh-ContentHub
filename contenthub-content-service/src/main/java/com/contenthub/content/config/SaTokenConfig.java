package com.contenthub.content.config;

import com.contenthub.common.config.BaseSaTokenConfig;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
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
        // 根据业务需求，可以添加公开接口，例如：
        // return Arrays.asList("/content/public/**", "/content/list");
        // 目前所有接口都需要认证
        return new ArrayList<>();
    }
}

