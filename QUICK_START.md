# 快速启动指南

## ✅ 问题已解决

您遇到的 Knife4j 与 Spring Boot 2.6+ 兼容性问题已经成功解决！

### 解决方案
- ✅ 将项目从 Java 17 降级到 Java 8（适配您的系统环境）
- ✅ 用 **SpringDoc OpenAPI 3** 替代 **Knife4j**（彻底解决兼容性问题）
- ✅ 更新所有相关注解和配置

## 🚀 启动服务

### 方法一：使用启动脚本（推荐）

```bash
# Windows
build-and-run.bat
```

该脚本会自动：
1. 构建并安装 `contenthub-common` 模块
2. 构建 `contenthub-user-service`
3. 启动用户服务

### 方法二：手动启动

```bash
# 1. 构建公共模块
cd contenthub-common
mvn clean install -DskipTests

# 2. 构建并启动用户服务
cd ../contenthub-user-service
mvn clean package -DskipTests
mvn spring-boot:run
```

## 📍 访问地址

服务启动成功后，访问以下地址：

### Swagger UI（推荐）
```
http://localhost:8001/swagger-ui.html
```
提供交互式 API 文档，可以直接测试接口

### API 文档 JSON
```
http://localhost:8001/v3/api-docs
```
标准 OpenAPI 3.0 格式的 API 规范

### 健康检查
```
http://localhost:8001/actuator/health
```

## 🧪 测试接口

### 1. 用户注册

**接口**: `POST /user/register`

**请求体**:
```json
{
  "username": "testuser",
  "nickname": "测试用户",
  "email": "test@example.com",
  "phone": "13800138000",
  "password": "123456"
}
```

**curl命令**:
```bash
curl -X POST http://localhost:8001/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "nickname": "测试用户",
    "email": "test@example.com",
    "phone": "13800138000",
    "password": "123456"
  }'
```

### 2. 用户登录

**接口**: `POST /user/login`

**请求体**:
```json
{
  "username": "testuser",
  "password": "123456"
}
```

**curl命令**:
```bash
curl -X POST http://localhost:8001/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "avatarUrl": null
  }
}
```

### 3. 获取当前用户信息

**接口**: `GET /user/current`

**请求头**: 
```
Authorization: Bearer {token}
```

**curl命令**:
```bash
curl -X GET http://localhost:8001/user/current \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

## 📝 注意事项

### 前置条件
确保以下服务已启动：
- ✅ MySQL (192.168.200.130:3306)
- ✅ Redis (192.168.200.130:6379)
- ✅ Nacos (192.168.200.130:8848)

### 数据库初始化
如果还没有初始化数据库，请执行：
```bash
mysql -h 192.168.200.130 -u root -p123456 < database/init.sql
```

### 常见启动错误

#### 1. 端口占用
**错误**: `Port 8001 is already in use`

**解决**:
```bash
# Windows
netstat -ano | findstr :8001
taskkill /PID {进程ID} /F
```

#### 2. 数据库连接失败
**错误**: `Unable to connect to database`

**检查**:
- MySQL 服务是否启动
- IP地址和端口是否正确
- 用户名密码是否正确
- 数据库 `content_hub` 是否已创建

#### 3. Redis 连接失败
**错误**: `Unable to connect to Redis`

**检查**:
- Redis 服务是否启动
- IP地址和端口是否正确
- 密码是否正确（654321）

#### 4. Nacos 连接失败
**错误**: `Unable to register with Nacos`

**检查**:
- Nacos 服务是否启动
- IP地址和端口是否正确
- 用户名密码是否正确（nacos/nacos）

## 🔍 查看日志

### 控制台日志
如果在前台运行 `mvn spring-boot:run`，日志会直接输出到控制台

### 文件日志
日志文件位置（根据logback配置）：
```
logs/user-service.log        # 所有日志
logs/user-service-error.log  # 错误日志
```

## 📚 相关文档

- [SpringDoc替换Knife4j说明](./docs/SpringDoc替换Knife4j说明.md) - 详细的迁移说明
- [API文档说明](./docs/API文档说明.md) - API文档使用指南
- [认证功能使用说明](./docs/认证功能使用说明.md) - JWT认证详细说明
- [Maven模块结构说明](./docs/Maven模块结构说明.md) - 项目结构说明

## ✨ 主要改进

1. **解决兼容性问题**
   - 从 Knife4j 迁移到 SpringDoc OpenAPI 3
   - 移除了不必要的兼容性配置

2. **适配系统环境**
   - 将项目从 Java 17 降级到 Java 8
   - 确保与您的 JDK 版本兼容

3. **优化配置**
   - 简化了 application.yml 配置
   - 移除了循环依赖和路径匹配策略配置

4. **完善文档**
   - 新增 SpringDoc 替换指南
   - 更新 API 文档说明
   - 提供详细的快速启动指南

## 🎉 下一步

服务启动成功后，您可以：

1. **使用 Swagger UI 测试接口**
   - 访问 http://localhost:8001/swagger-ui.html
   - 查看所有可用接口
   - 直接在页面上测试接口

2. **继续开发其他服务**
   - 参考用户服务的实现
   - 复制相同的配置和注解使用方式

3. **完善用户服务功能**
   - 添加用户信息更新接口
   - 实现头像上传功能
   - 添加用户关注/取消关注功能
   - 集成 Redis 缓存

---

如有问题，请查看相关文档或检查启动日志。

