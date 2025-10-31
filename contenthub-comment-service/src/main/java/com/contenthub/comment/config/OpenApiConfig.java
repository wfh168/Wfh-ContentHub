package com.contenthub.comment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI配置类（SpringDoc）
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("内容社区平台 - 评论服务API")
                        .description("评论服务接口文档\n\n" +
                                "## 功能说明\n" +
                                "提供评论发表、删除、查询、点赞等功能\n" +
                                "支持一级评论和二级评论（回复）\n\n" +
                                "### 认证说明\n" +
                                "所有接口都需要在Header中携带Token进行认证\n" +
                                "格式：`Authorization: {token}`（不需要Bearer前缀）\n\n" +
                                "### 评论结构\n" +
                                "- 一级评论：直接评论文章\n" +
                                "- 二级评论：回复评论")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ContentHub团队")
                                .email("support@contenthub.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                // 添加Sa-Token认证
                .addSecurityItem(new SecurityRequirement().addList("Sa-Token认证"))
                .components(new Components()
                        .addSecuritySchemes("Sa-Token认证",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .description("Sa-Token认证Token（直接填写Token，不需要Bearer前缀）")));
    }
}

