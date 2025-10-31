# Maven 模块结构说明

## 项目结构

```
Wfh-ContentHub/
├── pom.xml                          # 父工程POM，统一管理依赖版本
├── contenthub-common/               # 公共模块
│   ├── pom.xml
│   └── src/main/java/com/contenthub/common/
│       ├── result/                  # 统一响应结果
│       ├── constant/                # 常量定义
│       ├── exception/               # 全局异常
│       └── utils/                   # 工具类
│
├── contenthub-gateway/              # API网关服务 (端口: 8080)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/contenthub/gateway/
│       └── resources/application.yml
│
├── contenthub-user-service/         # 用户服务 (端口: 8001)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/contenthub/user/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── mapper/
│       │   └── domain/
│       └── resources/application.yml
│
├── contenthub-content-service/      # 内容服务 (端口: 8002)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/contenthub/content/
│       └── resources/application.yml
│
├── contenthub-comment-service/      # 评论服务 (端口: 8003)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/contenthub/comment/
│       └── resources/application.yml
│
├── contenthub-notification-service/ # 通知服务 (端口: 8004)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/contenthub/notification/
│       └── resources/application.yml
│
├── contenthub-search-service/       # 搜索服务 (端口: 8005)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/contenthub/search/
│       └── resources/application.yml
│
├── contenthub-file-service/         # 文件服务 (端口: 8006)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/contenthub/file/
│       └── resources/application.yml
│
└── contenthub-admin-service/        # 管理服务 (端口: 8007)
    ├── pom.xml
    └── src/main/
        ├── java/com/contenthub/admin/
        └── resources/application.yml
```

## 启动顺序

### 1. 基础设施启动（必须）

确保以下服务已启动并运行：

- **Nacos**: 192.168.200.130:8848 (用户名: nacos, 密码: nacos)
- **MySQL**: 192.168.200.130:3306 (用户名: root, 密码: 123456)
- **Redis**: 192.168.200.130:6379 (密码: 654321)

### 2. 可选基础设施

根据需要启动：

- **RabbitMQ**: 192.168.200.130:5672 (用户名: admin, 密码: admin) - 通知服务需要
- **Elasticsearch**: 192.168.200.130:9200 (用户名: elastic, 密码: wfhshuai) - 搜索服务需要
- **MinIO**: 192.168.200.130:9000 (用户名: admin, 密码: admin123) - 文件服务需要
- **Sentinel**: 192.168.200.130:8858 (用户名: sentinel, 密码: sentinel) - 可选

### 3. 初始化数据库

```bash
# 执行数据库初始化脚本
mysql -h 192.168.200.130 -u root -p123456 < database/init.sql
```

### 4. 启动微服务

**推荐启动顺序：**

```bash
# 1. 编译整个项目
mvn clean install -DskipTests

# 2. 启动网关服务
cd contenthub-gateway
mvn spring-boot:run

# 3. 启动业务服务（可并行启动）
cd contenthub-user-service && mvn spring-boot:run
cd contenthub-content-service && mvn spring-boot:run
cd contenthub-comment-service && mvn spring-boot:run
cd contenthub-notification-service && mvn spring-boot:run
cd contenthub-search-service && mvn spring-boot:run
cd contenthub-file-service && mvn spring-boot:run
cd contenthub-admin-service && mvn spring-boot:run
```

**或使用IDE启动：**

在 IntelliJ IDEA 中：
1. 右键点击各服务的 Application 主类
2. 选择 "Run" 或 "Debug"

## 访问地址

| 服务 | 端口 | 访问地址 |
|------|------|----------|
| API网关 | 8080 | http://localhost:8080 |
| 用户服务 | 8001 | http://localhost:8001 |
| 内容服务 | 8002 | http://localhost:8002 |
| 评论服务 | 8003 | http://localhost:8003 |
| 通知服务 | 8004 | http://localhost:8004 |
| 搜索服务 | 8005 | http://localhost:8005 |
| 文件服务 | 8006 | http://localhost:8006 |
| 管理服务 | 8007 | http://localhost:8007 |
| Nacos控制台 | 8848 | http://192.168.200.130:8848/nacos |

## API文档

各服务启动后，可访问 Knife4j API 文档：

- 用户服务: http://localhost:8001/doc.html
- 内容服务: http://localhost:8002/doc.html
- 评论服务: http://localhost:8003/doc.html
- 等等...

通过网关访问：
- http://localhost:8080/api/user/doc.html
- http://localhost:8080/api/content/doc.html

## 常见问题

### 1. 连接Nacos失败

检查Nacos是否启动，确认地址和端口正确。

### 2. 数据库连接失败

检查MySQL是否启动，确认用户名密码正确，数据库是否已创建。

### 3. Redis连接失败

检查Redis是否启动，确认密码配置正确。

### 4. 端口被占用

修改对应服务的 `application.yml` 中的 `server.port` 配置。

## 开发建议

1. **先启动公共模块**：确保 `contenthub-common` 模块已编译
2. **逐个启动服务**：建议先启动核心服务（用户、内容），再启动其他服务
3. **查看日志**：启动时注意观察控制台日志，确认服务注册成功
4. **检查Nacos**：访问Nacos控制台，确认服务已注册

## Maven命令

```bash
# 编译整个项目
mvn clean install

# 跳过测试编译
mvn clean install -DskipTests

# 只编译某个模块
mvn clean install -pl contenthub-user-service -am

# 运行某个服务
cd contenthub-user-service
mvn spring-boot:run

# 打包
mvn clean package
```

## 下一步

1. 完善各服务的业务逻辑
2. 添加全局异常处理
3. 实现JWT认证
4. 配置统一日志
5. 添加单元测试

