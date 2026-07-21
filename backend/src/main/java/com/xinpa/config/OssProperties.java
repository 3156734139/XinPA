package com.xinpa.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "oss")
public class OssProperties {

    /** 内网 Endpoint（后端专用，免费流量） */
    private String internalEndpoint;
    /** 公网 Endpoint（fallback） */
    private String publicEndpoint;
    /** CDN 加速域名（如 https://cdn.xinpa.com），配置后前端上传/预览用 */
    private String cdnDomain;
    /** Bucket 名称 */
    private String bucket;
    private String accessKeyId;
    private String accessKeySecret;

    // ========== STS 配置 ==========
    /** RAM 角色 ARN（格式: acs:ram::xxx:role/xxx） */
    private String roleArn;
    /** 角色会话名称 */
    private String roleSessionName = "xinpa-material-upload";
    /** OSS 回调 URL（生产环境配置，开发环境由前端通知入库） */
    private String callbackUrl;

    // ========== 兼容旧字段（保留端点、CDN） ==========

    public String getEndpoint() {
        return publicEndpoint != null ? publicEndpoint : internalEndpoint;
    }
}
