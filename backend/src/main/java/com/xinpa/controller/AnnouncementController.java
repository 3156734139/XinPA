package com.xinpa.controller;

import com.xinpa.common.Result;
import com.xinpa.service.SysAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公告接口（公开）
 */
@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final SysAnnouncementService sysAnnouncementService;

    /**
     * 获取已发布公告
     */
    @GetMapping
    public Result<?> list() {
        return Result.ok(sysAnnouncementService.listPublished());
    }
}
