package com.contenthub.file.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.contenthub.common.result.Result;
import com.contenthub.file.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件控制器
 */
@Tag(name = "文件管理", description = "文件上传下载相关接口")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(
            summary = "上传头像",
            description = "上传用户头像到MinIO（需要登录）\n\n" +
                    "**文件要求：**\n" +
                    "- 仅支持图片格式（jpg, png, gif, bmp, webp, svg 等）\n" +
                    "- 文件大小：最大2MB"
    )
    @PostMapping(value = "/avatar/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadAvatar(
            @Parameter(
                    description = "头像文件",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("file") MultipartFile file) {
        // 从Sa-Token获取当前登录用户ID
        long userId = StpUtil.getLoginIdAsLong();
        String avatarUrl = fileService.uploadAvatar(file, userId);
        return Result.success("头像上传成功", avatarUrl);
    }

    @Operation(
            summary = "上传文件",
            description = "上传文件到指定目录（需要登录）\n\n" +
                    "**支持的文件格式：**\n" +
                    "- 文档类：md, txt, doc, docx, pdf, xls, xlsx, ppt, pptx, csv, rtf 等\n" +
                    "- 图片类：jpg, jpeg, png, gif, bmp, webp, svg, ico 等\n" +
                    "- 视频类：mp4, avi, mov, wmv, flv, mkv, webm 等\n" +
                    "- 音频类：mp3, wav, flac, aac, ogg, wma 等\n" +
                    "- 其他格式：支持任意文件格式上传\n\n" +
                    "**限制说明：**\n" +
                    "- 文件大小：最大100MB\n" +
                    "- 文件会自动根据扩展名和目录进行分类"
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadFile(
            @Parameter(
                    description = "要上传的文件（支持任意格式：md, txt, pdf, jpg, mp4 等）",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "目录", example = "documents")
            @RequestParam(value = "directory", defaultValue = "files") String directory) {
        // 从Sa-Token获取当前登录用户ID
        long userId = StpUtil.getLoginIdAsLong();
        String fileUrl = fileService.uploadFile(file, directory, userId);
        return Result.success("文件上传成功", fileUrl);
    }

    @Operation(
            summary = "软删除文件",
            description = "软删除文件，只更新数据库标记，不删除MinIO文件（需要登录）\n\n" +
                    "**说明：**\n" +
                    "- 只更新数据库中的软删除标记（deleted=1）\n" +
                    "- **不会删除MinIO中的文件**，文件仍然可以访问\n" +
                    "- 软删除后的文件可以恢复\n" +
                    "- 参数为文件的完整路径（filePath）"
    )
    @DeleteMapping("/delete")
    public Result<String> deleteFile(
            @Parameter(
                    description = "文件路径（filePath），例如：documents/2024-10-31/xxx.md",
                    required = true,
                    example = "documents/2024-10-31/abc123def456.md"
            )
            @RequestParam("fileName") String fileName) {
        // 可以添加权限验证：只能删除自己的文件
        fileService.deleteFile(fileName);
        return Result.success("文件软删除成功", null);
    }

    @Operation(
            summary = "硬删除文件",
            description = "硬删除文件，同时删除MinIO文件和数据库记录（需要登录）\n\n" +
                    "**说明：**\n" +
                    "- 会从MinIO物理删除文件\n" +
                    "- 会物理删除数据库记录（不可恢复）\n" +
                    "- **操作不可逆，请谨慎使用**\n" +
                    "- 参数为文件的完整路径（filePath）"
    )
    @DeleteMapping("/hard-delete")
    public Result<String> hardDeleteFile(
            @Parameter(
                    description = "文件路径（filePath），例如：documents/2024-10-31/xxx.md",
                    required = true,
                    example = "documents/2024-10-31/abc123def456.md"
            )
            @RequestParam("fileName") String fileName) {
        // 可以添加权限验证：只能删除自己的文件
        fileService.hardDeleteFile(fileName);
        return Result.success("文件硬删除成功", null);
    }
}

