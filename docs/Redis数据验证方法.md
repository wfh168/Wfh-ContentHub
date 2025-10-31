# Redis 数据验证方法

## 问题：key的value值都是空的

### 1. 检查数据库选择是否正确

当前项目使用 **database 1**，检查时要切换到正确的数据库：

```bash
# 连接 Redis，切换到 database 1
redis-cli -h 192.168.200.130 -p 6379 -a 654321 -n 1

# 或者连接后切换
redis-cli -h 192.168.200.130 -p 6379 -a 654321
SELECT 1

# 查看所有key
KEYS *

# 查看具体key的值
GET user:info:1
GET user:token:1

# 查看Sa-Token的key（注意key前缀是token-name）
GET Authorization:login:session:1
GET Authorization:login:token:b518f232-14e0-4a67-b4c8-b2e4d76605dd
```

### 2. 验证项目自定义key是否有值

**登录后应该有的key：**
- `user:info:1` - 用户信息JSON（应该很长，包含用户所有信息）
- `user:token:1` - Token字符串（UUID格式，如：b518f232-14e0-4a67-b4c8-b2e4d76605dd）

**验证码key：**
- `captcha:{uuid}` - 验证码字符串（4位字符，如：A3B4）

### 3. Sa-Token的key说明

**Sa-Token默认key格式（token-name: Authorization）：**
- `Authorization:login:session:{loginId}` - 存储会话数据（默认只有用户ID："1"）
- `Authorization:login:token:{tokenValue}` - 存储token值（UUID字符串）

**注意**：Sa-Token的 `session` key 默认只存储用户ID，不是空的！如果显示为空，可能是：
1. 数据库选择错误
2. Redis连接问题
3. 序列化问题

### 4. 代码中的写入逻辑

**项目自定义key的写入：**
```java
// 写入用户信息（JSON字符串）
String userInfoJson = JSONUtil.toJsonStr(userInfoVO);
redisTemplate.opsForValue().set("user:info:1", userInfoJson, 1, TimeUnit.HOURS);

// 写入Token（字符串）
redisTemplate.opsForValue().set("user:token:1", token, 1, TimeUnit.HOURS);
```

**Sa-Token的写入（自动管理）：**
```java
// Sa-Token自动管理，只需要调用login
StpUtil.login(user.getId());  // 自动写入 session 和 token
```

### 5. 如果value确实是空的，检查日志

查看应用日志，应该有写入验证的日志：

```
成功日志：
缓存用户信息到Redis成功: userId=1, key=user:info:1, valueLength=xxx
缓存Token到Redis成功: userId=1, key=user:token:1, token=xxx

失败日志：
缓存用户信息到Redis失败: userId=1, key=user:info:1, value为空
缓存Token到Redis失败: userId=1, key=user:token:1, value为空
```

### 6. 常见问题

**问题1：数据库选择错误**
- 解决方案：确保切换到 database 1

**问题2：序列化问题**
- 解决方案：已添加 RedisConfig，确保使用 String 序列化

**问题3：Redis连接问题**
- 解决方案：检查 Redis 连接配置和密码

**问题4：数据确实为空**
- 检查登录日志，看是否有写入失败的日志
- 检查 Redis 连接是否正常

### 7. 验证步骤

1. **重启服务并登录**
2. **查看应用日志**，确认写入是否成功
3. **连接Redis验证**：
   ```bash
   redis-cli -h 192.168.200.130 -p 6379 -a 654321 -n 1
   GET user:info:1
   GET user:token:1
   ```
4. **如果value为空**，检查日志中的错误信息

