# Sa-Token配置方案分析

## 一、方案对比

### 方案一：放在 Common 服务（❌ 不推荐）

**优点：**
- 代码统一，只需维护一份配置
- 所有服务自动启用认证

**缺点：**
1. **路径硬编码问题**：配置中的排除路径 `/user/login`, `/user/register` 只适合用户服务
   - 其他服务有自己的路径前缀（如 `/content/**`, `/comment/**`）
   - 会导致其他服务的公开接口也被拦截

2. **过度拦截**：
   - 通知服务、搜索服务可能不需要认证
   - 但放在 common 后，这些服务也会被拦截

3. **灵活性差**：
   - 每个服务需要排除的公开接口不同
   - 无法针对不同服务定制化配置

4. **依赖问题**：
   - Common 服务需要引入 Sa-Token 依赖
   - 但有些服务可能不需要 Sa-Token

### 方案二：各自服务中配置（✅ 推荐）

**优点：**
1. **路径精确匹配**：每个服务配置自己的路径前缀
   ```java
   // 用户服务
   .excludePathPatterns("/user/login", "/user/register", "/user/captcha/**")
   
   // 内容服务
   .excludePathPatterns("/content/public/**")
   
   // 评论服务
   .excludePathPatterns("/comment/public/**")
   ```

2. **按需启用**：
   - 只有需要认证的服务才引入 Sa-Token 依赖和配置
   - 通知服务、搜索服务可以不配置

3. **灵活性强**：
   - 每个服务可以自定义排除路径
   - 可以针对不同服务设置不同的认证规则

4. **职责清晰**：
   - 认证逻辑由各自服务自己管理
   - 符合微服务的独立性原则

**缺点：**
- 需要每个服务都配置一次
- 代码有重复（但路径不同，无法完全统一）

## 二、服务分类

### 需要认证的服务

1. **用户服务 (User Service)** ✅
   - 排除：`/user/login`, `/user/register`, `/user/captcha/**`
   - 其他接口需要认证

2. **内容服务 (Content Service)** ✅
   - 排除：公开文章列表等（根据业务需求）
   - 创建、修改文章需要认证

3. **评论服务 (Comment Service)** ✅
   - 排除：公开评论列表
   - 发表评论需要认证

4. **管理服务 (Admin Service)** ✅
   - 所有接口都需要认证（管理员权限）

### 不需要认证的服务（或部分不需要）

1. **文件服务 (File Service)** ⚠️
   - 公开文件下载可能不需要认证
   - 文件上传需要认证

2. **通知服务 (Notification Service)** ❌
   - 通常是内部服务调用，不需要用户认证

3. **搜索服务 (Search Service)** ⚠️
   - 公开搜索可能不需要认证
   - 个性化搜索需要认证

## 三、推荐实现方式

### 1. 基础配置类（可选，放在 Common）

如果所有服务的基础配置相同，可以创建一个基础配置类：

```java
// contenthub-common/src/main/java/com/contenthub/common/config/BaseSaTokenConfig.java
@Configuration
public abstract class BaseSaTokenConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // Swagger文档（通用）
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        // Actuator（通用）
                        "/actuator/**",
                        // 错误页面（通用）
                        "/error",
                        "/favicon.ico"
                );
    }
    
    /**
     * 子类实现此方法，添加服务特定的排除路径
     */
    protected abstract void addServiceSpecificExclusions(InterceptorRegistration registration);
}
```

### 2. 各服务继承并配置

```java
// contenthub-user-service
@Configuration
public class SaTokenConfig extends BaseSaTokenConfig {
    
    @Override
    protected void addServiceSpecificExclusions(InterceptorRegistration registration) {
        registration.excludePathPatterns(
                "/user/login",
                "/user/register",
                "/user/captcha/**"
        );
    }
}
```

### 3. 或者直接在各服务配置（更简单）

每个服务独立配置，路径更清晰：

```java
// 用户服务
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login", "/user/register", "/user/captcha/**",
                        "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**"
                );
    }
}
```

## 四、最终建议

**推荐方案：各自服务中配置**

原因：
1. ✅ 路径匹配精确，每个服务只拦截自己的路径
2. ✅ 按需启用，不需要认证的服务可以不配置
3. ✅ 灵活性强，每个服务可以自定义排除路径
4. ✅ 符合微服务独立性原则

**实施步骤：**
1. 用户服务：已配置 ✅
2. 内容服务：需要时添加 Sa-Token 依赖和配置
3. 评论服务：需要时添加 Sa-Token 依赖和配置
4. 管理服务：需要时添加 Sa-Token 依赖和配置
5. 其他服务：根据业务需求决定是否配置

## 五、注意事项

1. **Gateway 认证**：
   - 如果使用 Gateway，可以在 Gateway 统一做认证
   - 各微服务内部可以不做认证（信任 Gateway）
   - 或者 Gateway + 各服务都做认证（双重保障）

2. **内部服务调用**：
   - Feign 调用时，可以添加 Token 传递
   - 或者内部服务调用不校验（信任 Nacos 注册的服务）

3. **配置统一性**：
   - 虽然各服务独立配置，但可以统一 Sa-Token 的配置项（timeout、token-name 等）
   - 这些配置可以放在各自的 `application.yml` 中

