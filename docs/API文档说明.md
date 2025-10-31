# API文档说明

## 📖 概述

本项目使用 **SpringDoc OpenAPI 3** 生成API文档，替代了之前的 Knife4j/Springfox 方案。SpringDoc 与 Spring Boot 2.6+ 完全兼容，提供了现代化的 API 文档解决方案。

## 🎯 访问地址

### 用户服务
- **Swagger UI**: http://localhost:8001/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8001/v3/api-docs

## 📦 依赖配置

### 父POM配置

```xml
<properties>
    <springdoc.version>1.7.0</springdoc.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 各服务POM配置

```xml
<dependencies>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-ui</artifactId>
    </dependency>
</dependencies>
```

## ⚙️ 配置说明

### application.yml

```yaml
springdoc:
  api-docs:
    enabled: true          # 启用API文档
    path: /v3/api-docs    # API文档JSON路径
  swagger-ui:
    enabled: true          # 启用Swagger UI
    path: /swagger-ui.html # Swagger UI访问路径
    tags-sorter: alpha     # 标签按字母排序
    operations-sorter: alpha # 操作按字母排序
  group-configs:
    - group: 用户服务API
      paths-to-match: /**
      packages-to-scan: com.contenthub.user.controller
```

## 📝 注解使用

### OpenAPI 3 vs Swagger 2 注解对比

| Swagger 2 | OpenAPI 3 | 说明 |
|-----------|-----------|------|
| `@Api` | `@Tag` | 标记Controller |
| `@ApiOperation` | `@Operation` | 标记接口方法 |
| `@ApiModel` | `@Schema` | 标记数据模型 |
| `@ApiModelProperty` | `@Schema` | 标记模型属性 |
| `@ApiParam` | `@Parameter` | 标记请求参数 |
| `@ApiImplicitParam` | `@Parameter` | 隐式参数 |
| `@ApiImplicitParams` | `@Parameters` | 多个隐式参数 |
| `@ApiResponse` | `@ApiResponse` | 响应说明 |
| `@ApiResponses` | `@ApiResponses` | 多个响应说明 |

### Controller层注解示例

```java
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/user")
public class AuthController {

    @Operation(summary = "用户登录", description = "用户登录获取Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }
}
```

### DTO层注解示例

```java
@Data
@Schema(description = "用户登录请求")
public class LoginDTO {
    
    @Schema(description = "用户名", required = true, example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @Schema(description = "密码", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;
}
```

### VO层注解示例

```java
@Data
@Schema(description = "用户登录响应")
public class LoginVO {
    
    @Schema(description = "用户ID", example = "1")
    private Long userId;
    
    @Schema(description = "用户名", example = "admin")
    private String username;
    
    @Schema(description = "访问令牌")
    private String token;
}
```

## 🔧 Java配置类

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("内容社区平台 - 用户服务API")
                        .description("用户服务接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ContentHub团队")
                                .email(""))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
```

## 🚀 功能特性

### 1. 自动生成API文档
SpringDoc 会自动扫描Controller并生成API文档，无需手动配置。

### 2. 支持分组
可以通过配置将不同模块的API分组展示：

```yaml
springdoc:
  group-configs:
    - group: 用户管理
      paths-to-match: /user/**
    - group: 内容管理
      paths-to-match: /content/**
```

### 3. 支持安全认证
配置JWT认证：

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
            .info(apiInfo())
            .addSecurityItem(new SecurityRequirement().addList("JWT"))
            .components(new Components()
                    .addSecuritySchemes("JWT", new SecurityScheme()
                            .name("Authorization")
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")));
}
```

### 4. 自定义响应示例

```java
@Operation(summary = "用户登录")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "登录成功",
        content = @Content(schema = @Schema(implementation = Result.class))),
    @ApiResponse(responseCode = "401", description = "用户名或密码错误")
})
@PostMapping("/login")
public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
    // ...
}
```

## 📌 常见问题

### Q1: 为什么不使用Knife4j？
**A**: Knife4j基于Springfox（Swagger 2），与Spring Boot 2.6+存在兼容性问题，特别是路径匹配策略的变化导致启动失败。SpringDoc是基于OpenAPI 3规范的，原生支持Spring Boot 2.6+。

### Q2: 如何隐藏某些接口不在文档中显示？
**A**: 使用 `@Hidden` 注解：

```java
@Hidden
@GetMapping("/internal")
public Result<?> internalApi() {
    // 此接口不会出现在文档中
}
```

### Q3: 如何在网关中聚合多个服务的文档？
**A**: 配置网关的OpenAPI配置：

```java
@Bean
public List<GroupedOpenApi> apis() {
    List<GroupedOpenApi> groups = new ArrayList<>();
    groups.add(GroupedOpenApi.builder()
            .group("user-service")
            .pathsToMatch("/user/**")
            .build());
    groups.add(GroupedOpenApi.builder()
            .group("content-service")
            .pathsToMatch("/content/**")
            .build());
    return groups;
}
```

## 🔗 参考资料

- [SpringDoc官方文档](https://springdoc.org/)
- [OpenAPI 3规范](https://swagger.io/specification/)
- [从Springfox迁移到SpringDoc](https://springdoc.org/#migrating-from-springfox)

## ✅ 优势总结

1. **完全兼容**: 与Spring Boot 2.6+及更高版本完全兼容
2. **主动维护**: SpringDoc是活跃维护的项目，Springfox已停止更新
3. **标准规范**: 基于OpenAPI 3规范，是行业标准
4. **配置简单**: 零配置即可使用，无需复杂的Bean配置
5. **性能更好**: 启动速度更快，资源占用更少
6. **功能丰富**: 支持更多高级特性，如文件上传、响应式编程等

## 📋 各服务文档地址

| 服务名称 | 端口 | Swagger UI地址 |
|---------|------|---------------|
| 用户服务 | 8001 | http://localhost:8001/swagger-ui.html |
| 内容服务 | 8002 | http://localhost:8002/swagger-ui.html |
| 评论服务 | 8003 | http://localhost:8003/swagger-ui.html |
| 通知服务 | 8004 | http://localhost:8004/swagger-ui.html |
| 搜索服务 | 8005 | http://localhost:8005/swagger-ui.html |
| 文件服务 | 8006 | http://localhost:8006/swagger-ui.html |
| 管理服务 | 8007 | http://localhost:8007/swagger-ui.html |
| 网关服务 | 8000 | http://localhost:8000/swagger-ui.html |

