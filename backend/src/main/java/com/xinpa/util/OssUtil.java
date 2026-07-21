package com.xinpa.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.ResponseHeaderOverrides;
import com.xinpa.config.OssProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 阿里云 OSS 工具类
 * <p>
 * 后端使用内网 Endpoint 操作 OSS（免费流量），负责：
 * - 生成 STS 临时凭证（供前端直传）
 * - 删除 OSS 文件
 * - 验证文件是否存在
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssUtil {

    private final OssProperties ossProperties;
    /** 内网 Endpoint OSS 客户端（后端操作，免费流量） */
    private OSS ossClient;
    /** 公网 Endpoint OSS 客户端（生成 presigned URL，浏览器可访问） */
    private OSS publicOssClient;

    @PostConstruct
    public void init() {
        try {
            // 内网客户端（后端文件操作，免费流量）
            String internalEndpoint = ossProperties.getInternalEndpoint();
            ossClient = new OSSClientBuilder().build(
                    internalEndpoint,
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret());

            // 公网客户端（生成 presigned URL，浏览器可访问）
            String publicEndpoint = ossProperties.getPublicEndpoint();
            if (publicEndpoint != null) {
                if (!publicEndpoint.startsWith("http://") && !publicEndpoint.startsWith("https://")) {
                    publicEndpoint = "https://" + publicEndpoint;
                }
                publicOssClient = new OSSClientBuilder().build(
                        publicEndpoint,
                        ossProperties.getAccessKeyId(),
                        ossProperties.getAccessKeySecret());
            } else {
                publicOssClient = ossClient;
            }

            log.info("OSS 客户端初始化成功: internalEndpoint={}, bucket={}",
                    internalEndpoint, ossProperties.getBucket());
        } catch (Exception e) {
            log.error("OSS 客户端初始化失败", e);
        }
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
        if (publicOssClient != null && publicOssClient != ossClient) {
            publicOssClient.shutdown();
        }
        log.info("OSS 客户端已关闭");
    }

    // ==================== STS 临时凭证 ====================

    /**
     * 生成 STS 临时上传凭证（前端直传 OSS 用）
     *
     * @param objectKey 预生成的对象路径
     * @param durationSeconds 凭证有效期（秒），默认 900
     * @return STS 凭证信息
     */
    public StsCredentials generateStsCredentials(String objectKey, Long durationSeconds) {
        String roleArn = ossProperties.getRoleArn();
        if (roleArn == null || roleArn.isBlank()) {
            throw new RuntimeException("OSS role-arn 未配置，无法生成 STS 凭证");
        }

        try {
            DefaultProfile profile = DefaultProfile.getProfile(
                    getRegion(), ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());
            DefaultAcsClient client = new DefaultAcsClient(profile);

            AssumeRoleRequest request = new AssumeRoleRequest();
            request.setSysMethod(MethodType.POST);
            request.setRoleArn(roleArn);
            request.setRoleSessionName(ossProperties.getRoleSessionName());
            request.setDurationSeconds(durationSeconds != null ? durationSeconds : 900L);

            AssumeRoleResponse response = client.getAcsResponse(request);
            AssumeRoleResponse.Credentials creds = response.getCredentials();

            StsCredentials result = new StsCredentials();
            result.setAccessKeyId(creds.getAccessKeyId());
            result.setAccessKeySecret(creds.getAccessKeySecret());
            result.setSecurityToken(creds.getSecurityToken());
            result.setRegion(getOssRegion());
            result.setBucket(ossProperties.getBucket());
            result.setCdnDomain(getPublicBaseUrl());
            result.setObjectKey(objectKey);

            log.info("STS 凭证生成成功: objectKey={}, 过期时间={}", objectKey, creds.getExpiration());
            return result;
        } catch (ClientException e) {
            log.error("STS AssumeRole 失败: {}", e.getMessage());
            throw new RuntimeException("获取 STS 凭证失败: " + e.getMessage());
        }
    }

    // ==================== 文件操作 ====================

    /**
     * 上传字节数据到 OSS（用于头像等小文件）
     */
    public void uploadBytes(String objectName, byte[] data, String contentType) {
        if (objectName == null || data == null) return;
        try {
            com.aliyun.oss.model.ObjectMetadata meta = new com.aliyun.oss.model.ObjectMetadata();
            meta.setContentType(contentType);
            ossClient.putObject(ossProperties.getBucket(), objectName, new java.io.ByteArrayInputStream(data), meta);
            log.info("OSS 文件上传成功: {}", objectName);
        } catch (Exception e) {
            log.error("OSS 文件上传失败: {}", objectName, e);
            throw new RuntimeException("OSS 文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除 OSS 文件
     */
    public void delete(String objectName) {
        if (objectName == null || objectName.isBlank()) return;
        try {
            ossClient.deleteObject(ossProperties.getBucket(), objectName);
            log.info("OSS 文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("OSS 文件删除失败: {}", objectName, e);
        }
    }

    /**
     * 验证 OSS 中文件是否存在
     */
    public boolean doesObjectExist(String objectName) {
        try {
            return ossClient.doesObjectExist(ossProperties.getBucket(), objectName);
        } catch (Exception e) {
            log.error("OSS 文件检查失败: {}", objectName, e);
            return false;
        }
    }

    /**
     * 获取公开访问的基础 URL（不含 objectKey），用于前端拼接预览地址
     * 优先 CDN，无 CDN 则使用 OSS 公网 Endpoint + bucket
     */
    public String getPublicBaseUrl() {
        String cdn = ossProperties.getCdnDomain();
        if (cdn != null && !cdn.isBlank()) {
            return normalizeCdnDomain(cdn);
        }
        String pubEndpoint = ossProperties.getPublicEndpoint();
        if (pubEndpoint == null) pubEndpoint = ossProperties.getInternalEndpoint();
        if (pubEndpoint == null) return "";
        if (!pubEndpoint.startsWith("http://") && !pubEndpoint.startsWith("https://")) {
            pubEndpoint = "https://" + pubEndpoint;
        }
        if (pubEndpoint.endsWith("/")) pubEndpoint = pubEndpoint.substring(0, pubEndpoint.length() - 1);
        return pubEndpoint + "/" + ossProperties.getBucket();
    }

    /**
     * 生成 OSS 对象的公开访问 URL
     */
    public String buildPublicUrl(String objectKey) {
        String cdn = ossProperties.getCdnDomain();
        if (cdn != null && !cdn.isBlank()) {
            String domain = cdn.trim();
            if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
                domain = "https://" + domain;
            }
            if (domain.endsWith("/")) domain = domain.substring(0, domain.length() - 1);
            return domain + "/" + objectKey;
        }
        String pubEndpoint = ossProperties.getPublicEndpoint();
        if (pubEndpoint == null) pubEndpoint = ossProperties.getInternalEndpoint();
        if (pubEndpoint == null) return objectKey;
        if (!pubEndpoint.startsWith("http://") && !pubEndpoint.startsWith("https://")) {
            pubEndpoint = "https://" + pubEndpoint;
        }
        if (pubEndpoint.endsWith("/")) pubEndpoint = pubEndpoint.substring(0, pubEndpoint.length() - 1);
        return pubEndpoint + "/" + ossProperties.getBucket() + "/" + objectKey;
    }

    /**
     * 生成 OSS 视频截帧 URL（视频封面图）
     * <p>
     * 使用 OSS 的 video/snapshot 处理参数，取视频第 0 帧缩略图。
     * 返回签名 URL，同一天内同一文件的截帧 URL 相同。
     *
     * @param objectKey         视频对象路径
     * @param expirationMinutes 过期时间（分钟）
     * @return 完整签名截帧 URL（JPEG 格式，宽 400px）
     */
    public String generateVideoSnapshotUrl(String objectKey, long expirationMinutes) {
        if (objectKey == null || objectKey.isBlank()) return "";

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        java.util.Date expiration = cal.getTime();

        java.util.Date nowMin = new java.util.Date(System.currentTimeMillis() + expirationMinutes * 60_000);
        if (nowMin.before(expiration)) {
            expiration = nowMin;
        }

        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(
                ossProperties.getBucket(), objectKey);
        req.setExpiration(expiration);
        req.setProcess("video/snapshot,t_0,f_jpg,w_400");

        ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
        overrides.setCacheControl("public, max-age=86400");
        req.setResponseHeaders(overrides);

        java.net.URL url = publicOssClient.generatePresignedUrl(req);
        log.debug("生成视频截帧 URL: objectKey={}", objectKey);
        return url.toString();
    }

    /**
     * 生成 OSS 签名 URL（带过期时间），浏览器可直接访问，不依赖 bucket 公共读
     * <p>
     * 过期时间统一到当日 23:59:59，同一天内同一文件的签名 URL 相同，浏览器可命中缓存。
     * OSS 会返回 Cache-Control 头，浏览器缓存 1 天。
     *
     * @param objectKey         对象路径（如 avatars/1/xxx.jpg）
     * @param expirationMinutes 过期时间（分钟，取当天剩余分钟与它的较小值）
     * @return 完整签名 URL
     */
    public String generatePresignedUrl(String objectKey, long expirationMinutes) {
        if (objectKey == null || objectKey.isBlank()) return "";

        // 过期时间 = 当天 23:59:59，保证同一文件同一天 URL 不变
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        java.util.Date expiration = cal.getTime();

        // 如果用户指定的分钟数更短，则使用它
        java.util.Date nowMin = new java.util.Date(System.currentTimeMillis() + expirationMinutes * 60_000);
        if (nowMin.before(expiration)) {
            expiration = nowMin;
        }

        GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(
                ossProperties.getBucket(), objectKey);
        req.setExpiration(expiration);

        ResponseHeaderOverrides overrides = new ResponseHeaderOverrides();
        overrides.setCacheControl("public, max-age=86400");
        req.setResponseHeaders(overrides);

        java.net.URL url = publicOssClient.generatePresignedUrl(req);
        String result = url.toString();
        log.debug("生成 presigned URL: objectKey={}, expire={}", objectKey, expiration);
        return result;
    }


    // ==================== 辅助方法 ====================

    /**
     * 规范化 CDN 域名：如 "cdn.xinpa.com" → "https://cdn.xinpa.com"
     */
    public String normalizeCdnDomain(String domain) {
        if (domain == null || domain.isBlank()) return domain;
        domain = domain.trim();
        if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
            domain = "https://" + domain;
        }
        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        return domain;
    }

    /**
     * 获取 OSS 地域 ID（用于 STS AssumeRole API，如 cn-chengdu）
     */
    private String getRegion() {
        String raw = getRawRegion();
        return raw != null ? raw : "cn-chengdu";
    }

    /**
     * 获取 OSS 地域前缀（用于前端 ali-oss SDK，如 oss-cn-chengdu）
     * <p>
     * ali-oss 构造 Endpoint 为: https://{bucket}.{region}.aliyuncs.com
     * 必须使用 oss-cn-xxx 格式才能得到正确 URL: https://xinpa-bucket.oss-cn-chengdu.aliyuncs.com
     */
    private String getOssRegion() {
        String raw = getRawRegion();
        return raw != null ? "oss-" + raw : "oss-cn-chengdu";
    }

    /**
     * 从内网 Endpoint 中提取裸地域名
     * 如 oss-cn-chengdu-internal.aliyuncs.com → cn-chengdu
     */
    private String getRawRegion() {
        String endpoint = ossProperties.getEndpoint();
        if (endpoint == null) return null;
        if (endpoint.startsWith("https://")) endpoint = endpoint.substring(8);
        if (endpoint.startsWith("http://")) endpoint = endpoint.substring(7);
        if (endpoint.startsWith("oss-")) {
            String rest = endpoint.substring(4);
            int dot = rest.indexOf('.');
            if (dot > 0) {
                String region = rest.substring(0, dot);
                if (region.endsWith("-internal")) {
                    region = region.substring(0, region.length() - "-internal".length());
                }
                return region;
            }
        }
        return null;
    }

    // ==================== 内部 DTO ====================

    @Data
    public static class StsCredentials {
        /** 临时 AccessKeyId */
        private String accessKeyId;
        /** 临时 AccessKeySecret */
        private String accessKeySecret;
        /** STS SecurityToken（直签模式为 null） */
        private String securityToken;
        /** OSS 地域（如 cn-chengdu） */
        private String region;
        /** Bucket 名称 */
        private String bucket;
        /** CDN 域名（如 https://cdn.xinpa.com，可能为空） */
        private String cdnDomain;
        /** 预生成的对象路径 */
        private String objectKey;
    }
}
