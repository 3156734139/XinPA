package com.xinpa.controller.admin;

import com.xinpa.common.BusinessException;
import com.xinpa.common.PageResult;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.AiCallLog;
import com.xinpa.entity.SysAdmin;
import com.xinpa.entity.SysAnnouncement;
import com.xinpa.mapper.AiCallLogMapper;
import com.xinpa.mapper.SysAdminMapper;
import com.xinpa.service.SysAnnouncementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员 - 系统管理接口
 */
@RestController
@RequestMapping("/admin/system")
@RequiredArgsConstructor
public class AdminSystemController {

    private final SysAdminMapper sysAdminMapper;
    private final AiCallLogMapper aiCallLogMapper;
    private final SysAnnouncementService sysAnnouncementService;
    private final PasswordEncoder passwordEncoder;

    // ==================== 管理员管理 ====================

    /**
     * 管理员列表
     */
    @GetMapping("/admins")
    public Result<List<SysAdmin>> listAdmins() {
        List<SysAdmin> admins = sysAdminMapper.selectList(
                new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getDeleted, 0));
        admins.forEach(a -> a.setPassword(null));
        return Result.ok(admins);
    }

    /**
     * 新增管理员
     */
    @PostMapping("/admins")
    public Result<Void> createAdmin(@RequestBody SysAdmin admin) {
        // 检查用户名是否存在
        SysAdmin exist = sysAdminMapper.selectOne(
                new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getUsername, admin.getUsername()));
        if (exist != null) {
            throw new BusinessException("用户名已存在");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        if (admin.getRole() == null) {
            admin.setRole("ADMIN");
        }
        admin.setStatus(1);
        sysAdminMapper.insert(admin);
        return Result.ok();
    }

    /**
     * 更新管理员
     */
    @PutMapping("/admins")
    public Result<Void> updateAdmin(@RequestBody SysAdmin admin) {
        if (admin.getPassword() != null && !admin.getPassword().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        }
        sysAdminMapper.updateById(admin);
        return Result.ok();
    }

    /**
     * 禁用/启用管理员
     */
    @PutMapping("/admins/{id}/status")
    public Result<Void> toggleAdminStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysAdmin admin = new SysAdmin();
        admin.setId(id);
        admin.setStatus(status);
        sysAdminMapper.updateById(admin);
        return Result.ok();
    }

    // ==================== AI调用日志 ====================

    /**
     * AI调用日志分页
     */
    @GetMapping("/ai-logs")
    public Result<PageResult<AiCallLog>> aiLogs(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size) {
        Page<AiCallLog> page = aiCallLogMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<AiCallLog>().orderByDesc(AiCallLog::getCreatedAt));
        return Result.ok(PageResult.of(page));
    }

    // ==================== 公告管理 ====================

    /**
     * 公告列表
     */
    @GetMapping("/announcements")
    public Result<List<SysAnnouncement>> listAnnouncements() {
        return Result.ok(sysAnnouncementService.listAll());
    }

    /**
     * 发布公告
     */
    @PostMapping("/announcements")
    public Result<Void> createAnnouncement(@RequestBody SysAnnouncement announcement) {
        announcement.setAdminId(UserContext.getUserId());
        sysAnnouncementService.create(announcement);
        return Result.ok();
    }

    /**
     * 公告上架/下架
     */
    @PutMapping("/announcements/{id}/status")
    public Result<Void> toggleAnnouncement(@PathVariable Long id, @RequestParam Integer status) {
        sysAnnouncementService.updateStatus(id, status);
        return Result.ok();
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/announcements/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        sysAnnouncementService.delete(id);
        return Result.ok();
    }
}
