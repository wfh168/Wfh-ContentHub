# SpringDoc OpenAPI 替换 Knife4j 说明

## 📋 问题背景

在启动用户服务时遇到以下错误：

```
org.springframework.context.ApplicationContextException: Failed to start bean 'documentationPluginsBootstrapper'
Caused by: java.lang.NullPointerException: Cannot invoke "org.springframework.web.servlet.mvc.condition.PatternsRequestCondition.getPatterns()" because "this.condition" is null
```

### 根本原因

**Knife4j 3.0.3（基于Springfox/Swagger 2）与 Spring Boot 2.6+ 存在兼容性问题**

Spring Boot 2.6+ 将默认路径匹配策略从 `AntPathMatcher` 改为 `PathPatternParser`，导致 Springfox 无法正常工作。

## 🔧 解决方案

### 方案一：添加配置（临时方案）
在 `application.yml` 中添加：
```yaml
spring:
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
```

**缺点**：
- 只是绕过问题，不是根本解决
- Springfox 已停止维护（最后更新：2020年）
- 未来可能会有更多兼容性问题

### 方案二：替换为 SpringDoc OpenAPI（推荐）✅

**优势**：
1. ✅ 基于 OpenAPI 3 规范（业界标准）
2. ✅ 原生支持 Spring Boot 2.6+
3. ✅ 主动维护，持续更新
4. ✅ 性能更好，启动更快
5. ✅ 零配置即可使用
6. ✅ 支持更多高级特性

## 🚀 迁移步骤

### 1. 更新父 POM 依赖管理

**删除**：
```xml
<knife4j.version>3.0.3</knife4j.version>

<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-spring-boot-starter</artifactId>
    <version>${knife4j.version}</version>
</dependency>
```

**添加**：
```xml
<springdoc.version>1.7.0</springdoc.version>

<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>${springdoc.version}</version>
</dependency>
```

### 2. 更新服务模块 POM

**删除**：
```xml
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-spring-boot-starter</artifactId>
</dependency>
```

**添加**：
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
</dependency>
```

### 3. 更新配置文件

**删除** `application.yml` 中的：
```yaml
spring:
  main:
    allow-circular-references: true
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher

knife4j:
  enable: true
  setting:
    language: zh_cn
```

**添加**：
```yaml
springdoc:
  api-docs:
    enabled: true
    path: /v3/api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
  group-configs:
    - group: 用户服务API
      paths-to-match: /**
      packages-to-scan: com.contenthub.user.controller
```

### 4. 替换配置类

**删除** `Knife4jConfig.java`

**创建** `OpenApiConfig.java`：
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

### 5. 更新注解

#### Controller 层

**Swagger 2 (旧)**：
```java
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "认证管理")
@RestController
public class AuthController {
    
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        // ...
    }
}
```

**OpenAPI 3 (新)**：
```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
public class AuthController {
    
    @Operation(summary = "用户登录", description = "用户登录获取Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        // ...
    }
}
```

#### DTO/VO 层

**Swagger 2 (旧)**：
```java
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Data
@ApiModel("登录请求")
public class LoginDTO {
    
    @ApiModelProperty(value = "用户名", required = true)
    private String username;
    
    @ApiModelProperty(value = "密码", required = true)
    private String password;
}
```

**OpenAPI 3 (新)**：
```java
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "登录请求")
public class LoginDTO {
    
    @Schema(description = "用户名", required = true, example = "admin")
    private String username;
    
    @Schema(description = "密码", required = true, example = "123456")
    private String password;
}
```

## 📝 注解对照表

| 功能 | Swagger 2 | OpenAPI 3 |
|------|----------|-----------|
| 标记Controller | `@Api` | `@Tag` |
| 标记接口方法 | `@ApiOperation` | `@Operation` |
| 标记数据模型 | `@ApiModel` | `@Schema` |
| 标记模型属性 | `@ApiModelProperty` | `@Schema` |
| 标记请求参数 | `@ApiParam` | `@Parameter` |
| 隐式参数 | `@ApiImplicitParam` | `@Parameter` |
| 多个隐式参数 | `@ApiImplicitParams` | `@Parameters` |
| 响应说明 | `@ApiResponse` | `@ApiResponse` |
| 多个响应说明 | `@ApiResponses` | `@ApiResponses` |
| 隐藏接口 | `@ApiIgnore` | `@Hidden` |

## 🌐 访问地址变化

| 项目 | Knife4j | SpringDoc |
|------|---------|-----------|
| UI界面 | `/doc.html` | `/swagger-ui.html` |
| API文档 | `/v2/api-docs` | `/v3/api-docs` |

### 用户服务访问地址

- Swagger UI: http://localhost:8001/swagger-ui.html
- API文档JSON: http://localhost:8001/v3/api-docs

## ⚠️ 常见问题

### Q1: 启动后找不到 Swagger UI？

**A**: 检查访问地址是否正确，SpringDoc 使用 `/swagger-ui.html` 而不是 `/doc.html`

### Q2: 接口没有显示在文档中？

**A**: 检查 `application.yml` 中的 `packages-to-scan` 是否配置正确

### Q3: 如何在网关中聚合多个服务的文档？

**A**: 需要在网关中配置 SpringDoc 的路由聚合，参考 [API文档说明.md](./API文档说明.md)

## 📚 参考资料

- [SpringDoc官方文档](https://springdoc.org/)
- [OpenAPI 3 规范](https://swagger.io/specification/)
- [从Springfox迁移指南](https://springdoc.org/#migrating-from-springfox)

## ✅ 迁移检查清单

- [x] 更新父POM依赖版本
- [x] 更新服务POM依赖
- [x] 替换配置类
- [x] 更新 application.yml
- [x] 更新 Controller 注解
- [x] 更新 DTO/VO 注解
- [x] 测试 Swagger UI 访问
- [x] 测试 API 接口调用

## 🎉 迁移成果

- ✅ 解决了 Knife4j 与 Spring Boot 2.6+ 的兼容性问题
- ✅ 升级到业界标准 OpenAPI 3 规范
- ✅ 获得更好的性能和维护支持
- ✅ 简化了配置，移除了不必要的兼容性设置

