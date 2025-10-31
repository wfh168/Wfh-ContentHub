package com.contenthub.file.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contenthub.common.exception.BusinessException;
import com.contenthub.file.config.MinioConfig;
import com.contenthub.file.domain.FileInfo;
import com.contenthub.file.mapper.FileMapper;
import com.contenthub.file.service.FileService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * 文件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final FileMapper fileMapper;

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        try {
            // 1. 查询该用户是否已有头像记录
            LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FileInfo::getUserId, userId)
                       .eq(FileInfo::getFileCategory, "AVATAR");
            FileInfo existingAvatar = fileMapper.selectOne(queryWrapper);

            // 2. 如果存在旧头像，删除MinIO中的旧文件
            if (existingAvatar != null) {
                try {
                    minioClient.removeObject(
                            RemoveObjectArgs.builder()
                                    .bucket(existingAvatar.getBucketName())
                                    .object(existingAvatar.getFilePath())
                                    .build()
                    );
                    log.info("删除旧头像文件成功: userId={}, oldFilePath={}", userId, existingAvatar.getFilePath());
                } catch (Exception e) {
                    log.warn("删除旧头像文件失败（继续上传新头像）: userId={}, error={}", userId, e.getMessage());
                }
            }

            // 3. 上传新头像到MinIO
            String originalFilename = file.getOriginalFilename();
            if (StrUtil.isBlank(originalFilename)) {
                throw new BusinessException("文件名不能为空");
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileNameOnly = IdUtil.simpleUUID() + extension;
            String filePath = "avatars/" + DateUtil.today() + "/" + fileNameOnly;

            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String fileUrl = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + filePath;

            // 4. 更新或插入数据库记录
            if (existingAvatar != null) {
                // 更新现有记录
                existingAvatar.setFileName(fileNameOnly);
                existingAvatar.setOriginalName(originalFilename);
                existingAvatar.setFilePath(filePath);
                existingAvatar.setFileUrl(fileUrl);
                existingAvatar.setFileSize(file.getSize());
                existingAvatar.setFileType(file.getContentType());
                existingAvatar.setFileExt(extension.substring(1));
                existingAvatar.setUpdatedAt(LocalDateTime.now());
                
                fileMapper.updateById(existingAvatar);
                log.info("更新头像记录成功: userId={}, fileId={}, fileUrl={}", userId, existingAvatar.getId(), fileUrl);
            } else {
                // 插入新记录
                FileInfo newAvatar = new FileInfo();
                newAvatar.setUserId(userId);
                newAvatar.setFileName(fileNameOnly);
                newAvatar.setOriginalName(originalFilename);
                newAvatar.setFilePath(filePath);
                newAvatar.setFileUrl(fileUrl);
                newAvatar.setFileSize(file.getSize());
                newAvatar.setFileType(file.getContentType());
                newAvatar.setFileExt(extension.substring(1));
                newAvatar.setFileCategory("AVATAR");
                newAvatar.setBucketName(minioConfig.getBucketName());
                newAvatar.setStorageType("minio");
                newAvatar.setStatus(1);
                
                fileMapper.insert(newAvatar);
                log.info("创建头像记录成功: userId={}, fileId={}, fileUrl={}", userId, newAvatar.getId(), fileUrl);
            }

            return fileUrl;

        } catch (Exception e) {
            log.error("上传头像失败: userId={}, error={}", userId, e.getMessage(), e);
            throw new BusinessException("头像上传失败: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        return uploadFile(file, directory, null);
    }

    @Override
    public String uploadFile(MultipartFile file, String directory, Long userId) {
        try {
            // 1. 验证文件
            if (file == null || file.isEmpty()) {
                throw new BusinessException("文件不能为空");
            }
            
            // 验证文件大小（最大100MB，可根据需求调整）
            long maxFileSize = 100 * 1024 * 1024; // 100MB
            if (file.getSize() > maxFileSize) {
                throw new BusinessException("文件大小不能超过100MB");
            }

            // 2. 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            if (StrUtil.isBlank(originalFilename)) {
                throw new BusinessException("文件名不能为空");
            }

            // 3. 生成唯一文件名（支持无扩展名的文件）
            String extension = "";
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex > 0 && lastDotIndex < originalFilename.length() - 1) {
                extension = originalFilename.substring(lastDotIndex);
            }
            String fileNameOnly = IdUtil.simpleUUID() + extension;
            String filePath = directory + "/" + DateUtil.today() + "/" + fileNameOnly;

            // 4. 上传到MinIO
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            // 5. 构造访问URL
            String fileUrl = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + filePath;

            // 6. 保存文件信息到数据库
            FileInfo fileInfo = new FileInfo();
            fileInfo.setUserId(userId); // 设置用户ID
            fileInfo.setFileName(fileNameOnly);
            fileInfo.setOriginalName(originalFilename);
            fileInfo.setFilePath(filePath);
            fileInfo.setFileUrl(fileUrl);
            fileInfo.setFileSize(file.getSize());
            fileInfo.setFileType(file.getContentType());
            // 去掉扩展名的点号（如果有扩展名）
            if (StrUtil.isNotBlank(extension) && extension.length() > 1) {
                fileInfo.setFileExt(extension.substring(1));
            } else {
                fileInfo.setFileExt("");
            }
            // 根据文件扩展名和目录确定文件分类
            fileInfo.setFileCategory(determineFileCategory(directory, extension, file.getContentType()));
            fileInfo.setBucketName(minioConfig.getBucketName());
            fileInfo.setStorageType("minio");
            fileInfo.setStatus(1);

            fileMapper.insert(fileInfo);

            log.info("文件上传成功: userId={}, fileName={}, fileUrl={}, fileId={}", userId, filePath, fileUrl, fileInfo.getId());
            return fileUrl;

        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 根据目录、文件扩展名和MIME类型确定文件分类
     * 
     * @param directory 目录名
     * @param extension 文件扩展名（包含点号，如 .md, .jpg）
     * @param contentType MIME类型（如 text/markdown, image/jpeg）
     * @return 文件分类
     */
    private String determineFileCategory(String directory, String extension, String contentType) {
        // 优先根据目录判断
        if ("avatars".equals(directory)) {
            return "AVATAR";
        }
        
        // 根据目录名称判断
        if (directory != null) {
            String dirLower = directory.toLowerCase();
            if (dirLower.contains("image")) {
                return "IMAGE";
            } else if (dirLower.contains("video")) {
                return "VIDEO";
            } else if (dirLower.contains("audio")) {
                return "AUDIO";
            } else if (dirLower.contains("document") || dirLower.contains("doc")) {
                return "DOCUMENT";
            }
        }
        
        // 根据文件扩展名判断
        if (StrUtil.isNotBlank(extension)) {
            String extLower = extension.toLowerCase();
            
            // 图片格式
            if (extLower.matches("\\.(jpg|jpeg|png|gif|bmp|webp|svg|ico)$")) {
                return "IMAGE";
            }
            // 视频格式
            if (extLower.matches("\\.(mp4|avi|mov|wmv|flv|mkv|webm|m4v)$")) {
                return "VIDEO";
            }
            // 音频格式
            if (extLower.matches("\\.(mp3|wav|flac|aac|ogg|wma|m4a)$")) {
                return "AUDIO";
            }
            // 文档格式（包括 md, txt, doc, docx, pdf, xls, xlsx, ppt, pptx 等）
            if (extLower.matches("\\.(md|txt|doc|docx|pdf|xls|xlsx|ppt|pptx|csv|rtf|odt|ods|odp)$")) {
                return "DOCUMENT";
            }
        }
        
        // 根据MIME类型判断
        if (StrUtil.isNotBlank(contentType)) {
            String mimeLower = contentType.toLowerCase();
            if (mimeLower.startsWith("image/")) {
                return "IMAGE";
            } else if (mimeLower.startsWith("video/")) {
                return "VIDEO";
            } else if (mimeLower.startsWith("audio/")) {
                return "AUDIO";
            } else if (mimeLower.startsWith("text/") || 
                       mimeLower.contains("document") || 
                       mimeLower.equals("application/pdf") ||
                       mimeLower.equals("application/msword") ||
                       mimeLower.equals("application/vnd.openxmlformats-officedocument")) {
                return "DOCUMENT";
            }
        }
        
        return "OTHER";
    }

    @Override
    public void deleteFile(String fileName) {
        try {
            // 1. 根据 filePath 查询数据库记录（fileName 实际是 filePath）
            LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FileInfo::getFilePath, fileName)
                       .eq(FileInfo::getDeleted, 0); // 只查询未删除的记录
            
            FileInfo fileInfo = fileMapper.selectOne(queryWrapper);
            
            if (fileInfo == null) {
                throw new BusinessException("文件记录不存在或已被删除");
            }

            // 2. 软删除：只更新数据库标记，不删除MinIO文件
            // 使用 MyBatis-Plus 的软删除机制（自动更新 deleted=1）
            fileMapper.deleteById(fileInfo.getId());

            log.info("文件软删除成功: fileId={}, filePath={}", fileInfo.getId(), fileInfo.getFilePath());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件软删除失败: {}", e.getMessage(), e);
            throw new BusinessException("文件软删除失败: " + e.getMessage());
        }
    }

    @Override
    public void hardDeleteFile(String fileName) {
        try {
            // 1. 根据 filePath 查询数据库记录（fileName 实际是 filePath）
            LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FileInfo::getFilePath, fileName)
                       .eq(FileInfo::getDeleted, 0); // 只查询未删除的记录
            
            FileInfo fileInfo = fileMapper.selectOne(queryWrapper);
            
            if (fileInfo == null) {
                throw new BusinessException("文件记录不存在或已被删除");
            }

            // 2. 从 MinIO 物理删除文件
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(fileInfo.getBucketName() != null ? fileInfo.getBucketName() : minioConfig.getBucketName())
                                .object(fileInfo.getFilePath())
                                .build()
                );
                log.info("从MinIO删除文件成功: filePath={}", fileInfo.getFilePath());
            } catch (Exception e) {
                log.error("从MinIO删除文件失败: filePath={}, error={}", fileInfo.getFilePath(), e.getMessage(), e);
                throw new BusinessException("从MinIO删除文件失败: " + e.getMessage());
            }

            // 3. 硬删除：物理删除数据库记录（绕过逻辑删除）
            fileMapper.physicalDeleteById(fileInfo.getId());
            
            log.info("文件硬删除成功: fileId={}, filePath={}, MinIO文件和数据库记录已删除", fileInfo.getId(), fileInfo.getFilePath());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件硬删除失败: {}", e.getMessage(), e);
            throw new BusinessException("文件硬删除失败: " + e.getMessage());
        }
    }
}

