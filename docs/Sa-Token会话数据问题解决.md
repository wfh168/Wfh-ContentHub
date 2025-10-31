# Sa-Token 会话数据问题解决方案

## 问题描述

错误信息显示：
```
sys_user_session_map没有获取到avatar
["com.yu.system.modules.system.entity.dto.SysUserDTO",{"id":2,"username":"admin","avatar":null,...}]
```

## 问题分析

1. **`sys_user_session_map`** 是 Sa-Token 自动生成的会话映射 Redis key
2. 错误中的类名 `com.yu.system.modules.system.entity.dto.SysUserDTO` 不属于当前项目
3. `avatar` 为 null，可能用户没有上传头像（这是正常的）

## 可能原因

1. **Redis 中有旧数据**：Redis 中存在其他项目（yu.system）的旧数据
2. **多个项目共用 Redis**：多个项目使用同一个 Redis 实例，导致 key 冲突
3. **Sa-Token 默认行为**：Sa-Token 默认只存储 loginId，不存储整个用户对象
   - 如果有自定义的会话数据存储，可能存储了其他类型的对象

## 解决方案

### 方案一：清除 Redis 中的旧数据（推荐）

连接到 Redis，删除相关的 key：

```bash
# 连接 Redis
redis-cli -h 192.168.200.130 -p 6379 -a 654321

# 查看所有 sys_user_session 相关的 key
KEYS *sys_user_session*

# 删除所有会话数据（谨慎操作）
DEL sys_user_session_map

# 或者删除所有 Sa-Token 相关的 key（如果确定都是旧数据）
KEYS sa-token:*
DEL sa-token:*

# 查看当前项目的数据（确认没有冲突）
KEYS user:*
KEYS captcha:*
```

### 方案二：配置 Sa-Token 使用独立的 Redis 数据库

在 `application.yml` 中配置独立的 Redis database：

```yaml
spring:
  redis:
    host: 192.168.200.130
    port: 6379
    password: 654321
    database: 1  # 使用独立的数据库（0-15），避免与其他项目冲突
```

### 方案三：配置 Sa-Token 使用独立的 key 前缀

Sa-Token 默认使用 `sa-token:` 作为前缀，如果需要避免冲突，可以：

1. **使用不同的 Redis 数据库**（推荐）
2. **或者使用不同的 Redis 实例**

### 方案四：确认 avatar 为 null 是否正常

如果用户没有上传头像，`avatar` 为 `null` 是正常的。如果需要设置默认头像：

```java
// 在 UserServiceImpl.java 的 login 方法中
LoginVO loginVO = new LoginVO();
loginVO.setAvatarUrl(user.getAvatarUrl() != null ? user.getAvatarUrl() : "/default-avatar.png");
```

或者在前端处理：

```javascript
const avatarUrl = userInfo.avatar || '/default-avatar.png';
```

## 验证步骤

1. **检查 Redis 中的数据**：
   ```bash
   redis-cli -h 192.168.200.130 -p 6379 -a 654321
   KEYS *
   GET sys_user_session_map
   ```

2. **检查当前项目的用户数据**：
   ```bash
   KEYS user:info:*
   KEYS user:token:*
   ```

3. **清除旧数据后重新登录**：
   - 清除 Redis 中的 `sys_user_session_map`
   - 重新登录
   - 检查新的数据是否正确

## 建议

1. **每个项目使用独立的 Redis 数据库**：
   - 项目1：database 0
   - 项目2：database 1
   - 避免数据冲突

2. **定期清理旧数据**：
   - Sa-Token 会自动清理过期的 token
   - 但如果有异常数据，需要手动清理

3. **监控 Redis key**：
   - 使用 Redis 监控工具查看 key 的使用情况
   - 避免 key 冲突

## 当前项目的 Redis key 结构

```
# Sa-Token 默认 key（自动管理）
sa-token:token:xxx              # Token存储
sa-token:id:xxx                 # 用户ID映射
sa-token:session:xxx            # 会话数据（可选）

# 项目自定义 key
user:info:{userId}              # 用户信息缓存
user:token:{userId}             # 用户Token缓存
captcha:{uuid}                  # 验证码缓存
```

如果看到 `sys_user_session_map`，说明可能是其他项目的数据。

