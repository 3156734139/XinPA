package com.xinpa.util;

import com.xinpa.config.MinioProperties;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 文件存储工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtil {

    private final MinioProperties minioProperties;
    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(minioProperties.getEndpoint())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();

            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
                log.info("MinIO bucket '{}' 已创建", minioProperties.getBucket());
            }
        } catch (Exception e) {
            log.error("MinIO 初始化失败", e);
        }
    }

    /**
     * 上传文件
     */
    public String upload(String prefix, MultipartFile file) throws Exception {
        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }
        String objectName = prefix + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;

        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectName)
                            .stream(in, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
        }

        log.info("文件上传成功: {}", objectName);
        return objectName;
    }

    /**
     * 获取文件临时访问URL
     */
    public String getPreviewUrl(String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(objectName)
                        .method(Method.GET)
                        .expiry(1, TimeUnit.DAYS)
                        .build());
    }

    /**
     * 获取文件永久访问URL（拼接方式）
     */
    public String getPermanentUrl(String objectName) {
        return minioProperties.getEndpoint() + "/" + minioProperties.getBucket() + "/" + objectName;
    }

    /**
     * 删除文件
     */
    public void delete(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(objectName)
                        .build());
        log.info("文件删除成功: {}", objectName);
    }
}
