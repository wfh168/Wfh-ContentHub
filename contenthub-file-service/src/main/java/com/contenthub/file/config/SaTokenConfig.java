package com.contenthub.file.config;

import com.contenthub.common.config.BaseSaTokenConfig;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 配置类（文件服务）
 * 
 * 继承BaseSaTokenConfig，文件服务所有接口都需要认证，无服务特定排除路径
 */
@Configuration
public class SaTokenConfig extends BaseSaTokenConfig {
    
    /**
     * 获取服务特定的排除路径
     * 
     * @return 文件服务无公开接口，所有接口都需要认证
     */
    @Override
    protected List<String> getServiceSpecificExcludePaths() {
        // 文件服务所有接口都需要认证，无额外排除路径
        return new ArrayList<>();
    }
}

