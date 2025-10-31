package com.contenthub.file.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件信息实体
 */
@Data
@TableName("files")
public class FileInfo {

    /**
     * 文件ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 上传用户ID
     */
    private Long userId;

    /**
     * 文件名（MinIO存储的文件名）
     */
    private String fileName;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件访问URL
     */
    private String fileUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * MIME类型
     */
    private String fileType;

    /**
     * 文件扩展名
     */
    private String fileExt;

    /**
     * 文件分类（AVATAR:头像, IMAGE:图片, DOCUMENT:文档, VIDEO:视频, AUDIO:音频, OTHER:其他）
     */
    private String fileCategory;

    /**
     * MinIO桶名称
     */
    private String bucketName;

    /**
     * 存储类型（minio-MinIO对象存储，local-本地）
     */
    private String storageType;

    /**
     * 状态（0:已删除, 1:正常）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除（0:未删除, 1:已删除）
     */
    @TableLogic
    private Integer deleted;
}

