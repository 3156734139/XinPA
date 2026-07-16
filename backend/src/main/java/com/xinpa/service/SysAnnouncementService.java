package com.xinpa.service;

import com.xinpa.entity.SysAnnouncement;

import java.util.List;

/**
 * 系统公告服务接口
 */
public interface SysAnnouncementService {

    /**
     * 获取已发布的公告列表
     */
    List<SysAnnouncement> listPublished();

    /**
     * 管理员获取所有公告
     */
    List<SysAnnouncement> listAll();

    /**
     * 创建公告
     */
    void create(SysAnnouncement announcement);

    /**
     * 更新公告状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除公告
     */
    void delete(Long id);
}
