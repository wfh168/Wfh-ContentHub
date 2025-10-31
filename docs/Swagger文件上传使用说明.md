# Swagger UI 文件上传使用说明

## 概述

在 Swagger UI 中，文件上传接口已经正确配置，可以自动识别文件格式并生成对应的 cURL 命令（包含 `-F 'file=@/path/to/file.格式'`）。

## 使用步骤

### 1. 访问 Swagger UI

访问文件服务的 Swagger UI：
```
http://localhost:8006/swagger-ui.html
```

### 2. 找到文件上传接口

在接口列表中找到：
- **上传文件** (`POST /file/upload`)
- **上传头像** (`POST /file/avatar/upload`)

### 3. 点击 "Try it out"

点击接口右侧的 **"Try it out"** 按钮，展开接口详情。

### 4. 填写参数

#### 上传文件接口：
- **file**：点击文件输入框旁边的 **"Choose File"** 按钮，选择要上传的文件
  - 支持任意格式：`.md`, `.txt`, `.pdf`, `.jpg`, `.mp4` 等
  - 文件大小限制：最大100MB
- **directory**：可选，默认为 `files`，可以输入 `documents`、`images` 等

#### 上传头像接口：
- **file**：点击文件输入框，选择图片文件
  - 仅支持图片格式：`.jpg`, `.png`, `.gif`, `.bmp`, `.webp`, `.svg` 等
  - 文件大小限制：最大2MB

### 5. 设置认证（如果需要）

在页面顶部的 **"Authorize"** 按钮中：
1. 点击 **"Authorize"**
2. 在弹出框中输入 Sa-Token 的 token 值
3. 点击 **"Authorize"** 确认
4. 点击 **"Close"** 关闭

或者直接在 Header 参数中输入 `Authorization: {token}`

### 6. 执行请求

点击 **"Execute"** 按钮执行请求。

## 自动生成的 cURL 命令

Swagger UI 会自动生成对应的 cURL 命令，格式如下：

### 上传文件示例：

```bash
curl -X 'POST' \
  'http://localhost:8006/file/upload?directory=documents' \
  -H 'accept: */*' \
  -H 'Authorization: b518f232-14e0-4a67-b4c8-b2e4d76605dd' \
  -F 'file=@/path/to/your/file.md'
```

**关键点：**
- ✅ 自动使用 `-F` 参数（文件上传格式）
- ✅ 自动包含文件路径：`file=@/path/to/your/file.md`
- ✅ 自动识别文件扩展名（`.md`, `.txt`, `.pdf` 等）
- ✅ 自动设置 `Content-Type: multipart/form-data` 和 boundary

### 上传头像示例：

```bash
curl -X 'POST' \
  'http://localhost:8006/file/avatar/upload' \
  -H 'accept: */*' \
  -H 'Authorization: b518f232-14e0-4a67-b4c8-b2e4d76605dd' \
  -F 'file=@/path/to/your/avatar.jpg'
```

## Swagger UI 界面说明

### 文件输入框识别

Swagger UI 会根据以下配置自动识别文件上传字段：

1. **请求类型**：`POST` + `multipart/form-data`
2. **参数类型**：`@RequestPart("file")` + `MultipartFile`
3. **Schema 配置**：
   ```java
   @Schema(type = "string", format = "binary")
   ```
4. **媒体类型**：`multipart/form-data`

当满足以上条件时，Swagger UI 会：
- 显示 **文件选择按钮**（而不是文本输入框）
- 生成的 cURL 命令自动使用 `-F` 参数
- 自动包含正确的文件路径和格式

## 常见问题

### Q: Swagger UI 没有显示文件选择框？

**A:** 检查以下几点：
1. 确认接口使用了 `@PostMapping(consumes = "multipart/form-data")`
2. 确认参数使用了 `@RequestPart("file") MultipartFile`
3. 确认 `@Schema(format = "binary")` 已正确配置
4. 刷新浏览器页面（清除缓存）

### Q: 生成的 cURL 命令路径不正确？

**A:** 
- Swagger UI 生成的是**本地路径**，需要根据实际情况替换
- 例如：`file=@C:\Users\YourName\Documents\test.md` (Windows)
- 或：`file=@/home/user/documents/test.md` (Linux/Mac)

### Q: 上传失败（401 Unauthorized）？

**A:** 
1. 确保已在 Swagger UI 中设置 Authorization token
2. 或者直接在 cURL 命令中添加 `-H 'Authorization: {token}'`

### Q: 上传失败（400 Bad Request - Required request part 'file' is not present）？

**A:**
- 确保在 Swagger UI 中选择了文件
- 不要手动修改生成的 cURL 命令的文件路径部分
- 确保文件路径正确且文件存在

## 技术说明

### 当前配置

```java
@PostMapping(value = "/upload", consumes = "multipart/form-data")
public Result<String> uploadFile(
    @Parameter(
        description = "要上传的文件（支持任意格式：md, txt, pdf, jpg, mp4 等）",
        required = true,
        content = @Content(
            mediaType = "multipart/form-data",
            schema = @Schema(type = "string", format = "binary")
        )
    )
    @RequestPart("file") MultipartFile file,
    ...
)
```

### SpringDoc 自动识别机制

SpringDoc OpenAPI 会自动：
1. 检测 `@RequestPart` + `MultipartFile` 组合
2. 识别 `@Schema(format = "binary")`
3. 生成正确的 OpenAPI 3.0 规范
4. Swagger UI 根据规范显示文件上传控件

## 总结

✅ **Swagger UI 完全支持文件上传**
- 自动显示文件选择框
- 自动生成正确的 cURL 命令（包含 `-F 'file=@/path/to/file.格式'`）
- 支持任意文件格式（`.md`, `.txt`, `.pdf`, `.jpg`, `.mp4` 等）
- 无需手动配置，直接使用即可

只需要在 Swagger UI 中选择文件，点击 "Execute"，就可以看到自动生成的、可直接使用的 cURL 命令！

