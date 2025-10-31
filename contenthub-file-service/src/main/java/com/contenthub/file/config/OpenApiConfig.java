package com.contenthub.file.config;

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
                        .title("内容社区平台 - 文件服务API")
                        .description("文件服务接口文档\n\n" +
                                "## 功能说明\n" +
                                "提供文件上传、下载、删除等功能\n" +
                                "支持头像上传到MinIO对象存储\n\n" +
                                "### 认证说明\n" +
                                "所有接口都需要在Header中携带Token进行认证\n" +
                                "格式：`Authorization: {token}`（不需要Bearer前缀）\n\n" +
                                "### MinIO配置信息：\n" +
                                "- Endpoint: http://192.168.200.130:9000\n" +
                                "- Bucket: public-bucket\n" +
                                "- 访问权限: 公开读取")
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

