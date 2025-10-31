package com.contenthub.user.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
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
                        .title("内容社区平台 - 用户服务API")
                        .description("用户服务接口文档\n\n" +
                                "## 认证说明\n" +
                                "大部分接口需要Sa-Token认证，请先调用登录接口获取Token\n\n" +
                                "### 使用步骤：\n" +
                                "1. 调用 `/user/login` 接口获取Token\n" +
                                "2. 点击右上角 🔓 **Authorize** 按钮\n" +
                                "3. 在弹窗中**直接粘贴Token**（不需要 'Bearer ' 前缀）\n" +
                                "4. 点击 **Authorize** 按钮\n" +
                                "5. 现在可以调用需要认证的接口了\n\n" +
                                "**注意：Sa-Token不需要Bearer前缀，直接填写Token即可！**")
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

