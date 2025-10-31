package com.contenthub.file.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * MinIO初始化器
 * 启动时自动创建Bucket并设置为公开访问
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioInitializer implements CommandLineRunner {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Override
    public void run(String... args) throws Exception {
        String bucketName = minioConfig.getBucketName();

        try {
            // 检查bucket是否存在
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                // 创建bucket
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("MinIO Bucket创建成功: {}", bucketName);

                // 设置bucket为公开访问
                String policy = "{"
                        + "\"Version\":\"2012-10-17\","
                        + "\"Statement\":[{"
                        + "\"Effect\":\"Allow\","
                        + "\"Principal\":{\"AWS\":[\"*\"]},"
                        + "\"Action\":[\"s3:GetObject\"],"
                        + "\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]"
                        + "}]"
                        + "}";

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucketName)
                                .config(policy)
                                .build()
                );
                log.info("MinIO Bucket访问策略设置成功: 公开读取");
            } else {
                log.info("MinIO Bucket已存在: {}", bucketName);
            }

        } catch (Exception e) {
            log.error("MinIO初始化失败: {}", e.getMessage(), e);
            throw new RuntimeException("MinIO初始化失败", e);
        }
    }
}

