package com.contenthub.file.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传头像
     * @param file 文件
     * @param userId 用户ID
     */
    String uploadAvatar(MultipartFile file, Long userId);

    /**
     * 上传文件
     * @param file 文件
     * @param directory 目录
     */
    String uploadFile(MultipartFile file, String directory);

    /**
     * 上传文件（带用户ID）
     * @param file 文件
     * @param directory 目录
     * @param userId 用户ID
     */
    String uploadFile(MultipartFile file, String directory, Long userId);

    /**
     * 软删除文件（只更新数据库标记，不删除MinIO文件）
     * @param fileName 文件路径（filePath）
     */
    void deleteFile(String fileName);

    /**
     * 硬删除文件（同时删除MinIO文件和数据库记录）
     * @param fileName 文件路径（filePath）
     */
    void hardDeleteFile(String fileName);
}

