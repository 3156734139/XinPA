package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.entity.SysAnnouncement;
import com.xinpa.mapper.SysAnnouncementMapper;
import com.xinpa.service.SysAnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统公告服务实现
 */
@Service
@RequiredArgsConstructor
public class SysAnnouncementServiceImpl implements SysAnnouncementService {

    private final SysAnnouncementMapper sysAnnouncementMapper;

    @Override
    public List<SysAnnouncement> listPublished() {
        return sysAnnouncementMapper.selectList(
                new LambdaQueryWrapper<SysAnnouncement>()
                        .eq(SysAnnouncement::getStatus, 1)
                        .eq(SysAnnouncement::getDeleted, 0)
                        .orderByDesc(SysAnnouncement::getCreatedAt));
    }

    @Override
    public List<SysAnnouncement> listAll() {
        return sysAnnouncementMapper.selectList(
                new LambdaQueryWrapper<SysAnnouncement>()
                        .eq(SysAnnouncement::getDeleted, 0)
                        .orderByDesc(SysAnnouncement::getCreatedAt));
    }

    @Override
    public void create(SysAnnouncement announcement) {
        sysAnnouncementMapper.insert(announcement);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        SysAnnouncement ann = new SysAnnouncement();
        ann.setId(id);
        ann.setStatus(status);
        sysAnnouncementMapper.updateById(ann);
    }

    @Override
    public void delete(Long id) {
        sysAnnouncementMapper.deleteById(id);
    }
}
