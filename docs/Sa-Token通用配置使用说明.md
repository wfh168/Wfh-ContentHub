# Sa-Token 通用配置使用说明

## 一、架构设计

### 1. 基础配置类（Common模块）

`BaseSaTokenConfig` 位于 `contenthub-common` 模块中，提供了：
- **通用排除路径**：Swagger文档、Actuator监控、错误页面等（所有服务都需要）
- **可扩展方法**：`getServiceSpecificExcludePaths()` 允许各服务添加自己的排除路径

### 2. 各服务配置

各服务只需：
1. 继承 `BaseSaTokenConfig`
2. 重写 `getServiceSpecificExcludePaths()` 方法，添加服务特定的排除路径
3. 添加 `@Configuration` 注解

**示例代码：**

```java
@Configuration
public class SaTokenConfig extends BaseSaTokenConfig {
    
    @Override
    protected List<String> getServiceSpecificExcludePaths() {
        return Arrays.asList(
                "/user/login",
                "/user/register",
                "/user/captcha/**"
        );
    }
}
```

## 二、已配置的服务

### 1. 用户服务 (User Service)
- **排除路径**：`/user/login`, `/user/register`, `/user/captcha/**`
- **文件位置**：`contenthub-user-service/src/main/java/com/contenthub/user/config/SaTokenConfig.java`

### 2. 文件服务 (File Service)
- **排除路径**：无（所有接口都需要认证）
- **文件位置**：`contenthub-file-service/src/main/java/com/contenthub/file/config/SaTokenConfig.java`

### 3. 内容服务 (Content Service)
- **排除路径**：无（当前所有接口都需要认证，可根据业务需求调整）
- **文件位置**：`contenthub-content-service/src/main/java/com/contenthub/content/config/SaTokenConfig.java`

### 4. 评论服务 (Comment Service)
- **排除路径**：无（当前所有接口都需要认证，可根据业务需求调整）
- **文件位置**：`contenthub-comment-service/src/main/java/com/contenthub/comment/config/SaTokenConfig.java`

## 三、如何为新服务添加认证

### 步骤1：添加依赖

在服务的 `pom.xml` 中添加：

```xml
<!-- Sa-Token 权限认证 -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot-starter</artifactId>
</dependency>
<!-- Sa-Token 整合 Redis -->
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-dao-redis-jackson</artifactId>
</dependency>
```

### 步骤2：配置 Redis 和 Sa-Token

在 `application.yml` 中：

```yaml
spring:
  redis:
    host: 192.168.200.130
    port: 6379
    password: 654321
    database: 1  # 与用户服务使用同一个数据库（Sa-Token需要）

# Sa-Token配置
sa-token:
  token-name: Authorization
  timeout: 604800  # 7天
  activity-timeout: -1
  is-concurrent: true
  is-share: false
  token-style: uuid
  is-log: true
  is-read-cookie: false
  is-read-head: true
```

### 步骤3：创建配置类

在服务中创建 `SaTokenConfig.java`：

```java
package com.yourproject.service.config;

import com.contenthub.common.config.BaseSaTokenConfig;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SaTokenConfig extends BaseSaTokenConfig {
    
    @Override
    protected List<String> getServiceSpecificExcludePaths() {
        // 添加服务特定的公开接口
        return Arrays.asList(
                "/your-service/public/**",
                "/your-service/list"
        );
    }
}
```

## 四、优势

### 1. 代码复用
- **通用配置统一管理**：Swagger、Actuator等通用排除路径只需在 `BaseSaTokenConfig` 中维护一次
- **减少重复代码**：各服务只需配置自己的排除路径

### 2. 易于维护
- **集中修改**：如果需要调整通用配置，只需修改 `BaseSaTokenConfig`
- **清晰职责**：每个服务只关注自己的业务逻辑

### 3. 灵活性
- **按需配置**：各服务可以自定义排除路径
- **继承扩展**：如果需要更复杂的逻辑，可以重写 `addInterceptors()` 方法

## 五、注意事项

### 1. 依赖关系
- `contenthub-common` 模块中 Sa-Token 依赖使用 `provided` 作用域
- 使用 Sa-Token 的服务必须在自己的 `pom.xml` 中声明依赖

### 2. Redis 数据库
- **必须使用相同的 Redis database**（建议使用 database 1）
- Sa-Token 需要在 Redis 中存储会话信息

### 3. 路径匹配
- 排除路径使用 **Ant 风格匹配**（支持 `*` 和 `**`）
- 例如：`/user/captcha/**` 匹配 `/user/captcha/generate` 和 `/user/captcha/validate`

### 4. 条件加载
- `BaseSaTokenConfig` 使用 `@ConditionalOnClass` 注解
- 只有当项目中存在 Sa-Token 依赖时才会加载

## 六、常见问题

### Q: 如何禁用某个服务的认证？
A: 删除该服务的 `SaTokenConfig` 配置类即可，或者返回所有路径：

```java
@Override
protected List<String> getServiceSpecificExcludePaths() {
    return Arrays.asList("/**");  // 排除所有路径
}
```

### Q: 如何添加更多的通用排除路径？
A: 修改 `BaseSaTokenConfig.getCommonExcludePaths()` 方法。

### Q: 如何为某个 Controller 方法添加公开访问？
A: 在配置类的 `getServiceSpecificExcludePaths()` 中添加对应的路径，例如：

```java
@Override
protected List<String> getServiceSpecificExcludePaths() {
    return Arrays.asList("/content/public/list");
}
```

然后在 Controller 中：

```java
@GetMapping("/public/list")
public Result<List<Content>> getPublicList() {
    // 公开接口，不需要认证
}
```

## 七、总结

通过将 Sa-Token 配置抽取到 Common 模块，我们实现了：
- ✅ **代码复用**：通用配置统一管理
- ✅ **简化配置**：各服务只需配置自己的排除路径
- ✅ **易于维护**：集中管理，减少重复代码
- ✅ **灵活扩展**：各服务可以自定义配置

每个服务的配置代码从 **44行** 减少到 **约20行**，大大提高了开发效率！

