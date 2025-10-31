# Wfh-ContentHub
微服务架构的内容社区平台设计与实现

## 📚 文档目录

- [功能设计文档](./docs/功能设计.md) - 详细的功能模块设计
- [技术栈推荐](./docs/技术栈推荐.md) - 技术选型和推荐方案
- [数据库设计文档](./docs/数据库设计文档.md) - 完整的数据库表结构和设计
- [项目架构设计](./docs/项目架构设计.md) - 系统架构和微服务设计
- [Maven模块结构说明](./docs/Maven模块结构说明.md) - Maven多模块项目结构
- [认证功能使用说明](./docs/认证功能使用说明.md) - Sa-Token认证功能使用指南
- [API文档说明](./docs/API文档说明.md) - SpringDoc OpenAPI接口文档
- [SpringDoc替换Knife4j说明](./docs/SpringDoc替换Knife4j说明.md) - API文档框架迁移指南
- [Knife4j完全清理说明](./docs/Knife4j完全清理说明.md) - Knife4j完全清理步骤和验证
- [用户信息更新和头像上传说明](./docs/用户信息更新和头像上传说明.md) - 用户信息更新功能使用指南
- [头像上传功能说明](./docs/头像上传功能说明.md) - 头像覆盖更新详细说明

## 🚀 项目简介

这是一个基于微服务架构的内容社区平台，支持用户发布文章、评论互动、内容推荐等功能。采用Spring Cloud微服务技术栈，提供高可用、高性能的内容社区服务。

## 🎯 核心功能

1. **用户系统**: 注册登录、个人资料、关注关系
2. **内容系统**: 文章发布、分类标签、内容管理
3. **互动系统**: 点赞、收藏、评论、分享
4. **通知系统**: 消息通知、站内信
5. **搜索系统**: 全文搜索、内容检索
6. **管理系统**: 内容审核、用户管理、数据统计

## ✨ 已实现功能

- ✅ Maven多模块项目结构
- ✅ Spring Cloud微服务架构
- ✅ Nacos服务注册与发现
- ✅ Spring Cloud Gateway API网关
- ✅ 全局异常处理
- ✅ 统一响应结果封装
- ✅ Sa-Token认证与授权
- ✅ 统一日志配置（Logback）
- ✅ API接口文档（SpringDoc OpenAPI 3）
- ✅ 用户登录/注册接口
- ✅ 用户服务数据库集成（MyBatis-Plus）
- ✅ 用户信息更新功能
- ✅ 头像上传功能（支持覆盖更新）
- ✅ 文件服务（MinIO对象存储）
- ✅ 服务间调用（OpenFeign）

## 🛠 技术栈

### 后端
- Spring Boot 2.7.x
- Spring Cloud Alibaba
- Nacos (服务注册与发现)
- Spring Cloud Gateway (API网关)
- Sentinel (流量控制)
- MyBatis-Plus (ORM)
- Redis (缓存)
- MySQL 8.0 (数据库)
- Elasticsearch (全文搜索)
- RabbitMQ (消息队列)
- MinIO (对象存储)

### 前端
- Vue 3
- TypeScript
- Vite
- Element Plus / Ant Design Vue
- Pinia (状态管理)

## 📖 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.8+
- MySQL 8.0
- Redis 7.x
- Nacos 2.x

### 启动步骤

1. **启动基础设施**
   - Nacos (192.168.200.130:8848)
   - MySQL (192.168.200.130:3306)
   - Redis (192.168.200.130:6379)

2. **初始化数据库**
   ```bash
   mysql -h 192.168.200.130 -u root -p123456 < database/init.sql
   ```

3. **快速启动（推荐）**
   
   使用一键启动脚本：
   ```bash
   # Windows
   build-and-run.bat
   ```
   
   脚本会自动完成：
   - 清理本地Maven缓存
   - 构建并安装公共模块
   - 构建用户服务
   - 启动用户服务

4. **手动启动**
   
   如需手动启动，请按以下顺序：
   ```bash
   # 1. 构建公共模块
   cd contenthub-common
   mvn clean install -DskipTests
   
   # 2. 构建并启动用户服务
   cd ../contenthub-user-service
   mvn clean package -DskipTests
   mvn spring-boot:run
   ```

5. **访问服务**
   - 用户服务: http://localhost:8001
   - Swagger UI: http://localhost:8001/swagger-ui.html
   - API文档: http://localhost:8001/v3/api-docs

详细文档请查看 [docs](./docs/) 目录。

## 📝 许可证

本项目仅用于学习交流。