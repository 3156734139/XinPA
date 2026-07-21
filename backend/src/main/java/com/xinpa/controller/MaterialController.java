package com.xinpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinpa.common.PageResult;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.Material;
import com.xinpa.entity.MaterialType;
import com.xinpa.service.MaterialService;
import com.xinpa.util.OssUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 素材管理接口
 */
@RestController
@RequestMapping("/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final OssUtil ossUtil;

    /**
     * 获取素材列表（分页，fileUrl 返回 presigned URL）
     * <p>
     * 视频素材返回截帧缩略图（不原图），避免列表页产生大量下行流量。
     * 播放完整视频需调用 {@link #getVideoUrl(Long)} 接口获取实时 presigned URL。
     */
    @GetMapping
    public Result<Map<String, Object>> listMaterials(
            @RequestParam(required = false) MaterialType type,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        Page<Material> page = materialService.pageByUserId(UserContext.getUserId(), type, current, size);
        for (Material m : page.getRecords()) {
            if (m.getFileUrl() != null && !m.getFileUrl().isEmpty()
                    && !m.getFileUrl().startsWith("http://") && !m.getFileUrl().startsWith("https://")) {
                if (m.getMaterialType() == MaterialType.VIDEO) {
                    // 视频：返回截帧缩略图，不返回原视频
                    m.setFileUrl(ossUtil.generateVideoSnapshotUrl(m.getFileUrl(), 1440));
                } else {
                    m.setFileUrl(ossUtil.generatePresignedUrl(m.getFileUrl(), 1440));
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("page", PageResult.of(page));
        return Result.ok(result);
    }

    /**
     * 获取视频播放地址（实时生成 presigned URL，仅当次有效）
     */
    @GetMapping("/{id}/video-url")
    public Result<Map<String, String>> getVideoUrl(@PathVariable Long id) {
        Material material = materialService.getById(id);
        if (material == null || !material.getUserId().equals(UserContext.getUserId())) {
            return Result.fail("素材不存在");
        }
        if (material.getMaterialType() != MaterialType.VIDEO) {
            return Result.fail("非视频素材");
        }
        String url = ossUtil.generatePresignedUrl(material.getFileUrl(), 1440);
        return Result.ok(Map.of("url", url));
    }

    /**
     * 获取 STS 临时上传凭证（前端直传 OSS 用）
     */
    @GetMapping("/upload-token")
    public Result<OssUtil.StsCredentials> getUploadToken(@RequestParam String type) {
        Long userId = UserContext.getUserId();
        String objectKey = "user/" + userId + "/" + type + "/" + UUID.randomUUID().toString().replace("-", "");
        OssUtil.StsCredentials creds = ossUtil.generateStsCredentials(objectKey, 900L);
        return Result.ok(creds);
    }

    /**
     * 前端通知素材上传完成
     */
    @PostMapping("/notify-complete")
    public Result<Material> notifyUploadComplete(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getUserId();
        String name = (String) body.get("name");
        String typeStr = (String) body.get("type");
        MaterialType type = typeStr != null ? MaterialType.fromType(typeStr) : null;
        String objectKey = (String) body.get("objectKey");
        Long fileSize = body.get("fileSize") != null ? ((Number) body.get("fileSize")).longValue() : 0L;
        Integer watermark = body.get("watermark") != null ? ((Number) body.get("watermark")).intValue() : 0;
        return Result.ok(materialService.saveFromCallback(userId, name, type, objectKey, fileSize, watermark));
    }

    /**
     * 删除素材
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMaterial(@PathVariable Long id) {
        materialService.delete(id, UserContext.getUserId());
        return Result.ok();
    }
}
