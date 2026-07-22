package com.xinpa.controller.admin;

import com.xinpa.common.BusinessException;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.SysAdmin;
import com.xinpa.entity.SysAnnouncement;
import com.xinpa.mapper.SysAdminMapper;
import com.xinpa.service.SysAnnouncementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/system")
@RequiredArgsConstructor
public class AdminSystemController {

    private final SysAdminMapper sysAdminMapper;
    private final SysAnnouncementService sysAnnouncementService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/admins")
    public Result<List<SysAdmin>> listAdmins() {
        List<SysAdmin> admins = sysAdminMapper.selectList(
                new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getDeleted, 0));
        admins.forEach(a -> a.setPassword(null));
        return Result.ok(admins);
    }

    @PostMapping("/admins")
    public Result<Void> createAdmin(@RequestBody SysAdmin admin) {
        SysAdmin exist = sysAdminMapper.selectOne(
                new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getUsername, admin.getUsername()));
        if (exist != null) throw new BusinessException("用户名已存在");
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        if (admin.getRole() == null) admin.setRole("ADMIN");
        admin.setStatus(1);
        sysAdminMapper.insert(admin);
        return Result.ok();
    }

    @PutMapping("/admins")
    public Result<Void> updateAdmin(@RequestBody SysAdmin admin) {
        if (admin.getPassword() != null && !admin.getPassword().isEmpty())
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        sysAdminMapper.updateById(admin);
        return Result.ok();
    }

    @PutMapping("/admins/{id}/status")
    public Result<Void> toggleAdminStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysAdmin a = new SysAdmin(); a.setId(id); a.setStatus(status);
        sysAdminMapper.updateById(a);
        return Result.ok();
    }

    // ==================== 公告 ====================

    @GetMapping("/announcements")
    public Result<List<SysAnnouncement>> listAnnouncements() {
        return Result.ok(sysAnnouncementService.listAll());
    }

    @PostMapping("/announcements")
    public Result<Void> createAnnouncement(@RequestBody SysAnnouncement announcement) {
        announcement.setAdminId(UserContext.getUserId());
        sysAnnouncementService.create(announcement);
        return Result.ok();
    }

    @PutMapping("/announcements/{id}/status")
    public Result<Void> toggleAnnouncement(@PathVariable Long id, @RequestParam Integer status) {
        sysAnnouncementService.updateStatus(id, status);
        return Result.ok();
    }

    @DeleteMapping("/announcements/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        sysAnnouncementService.delete(id);
        return Result.ok();
    }

}
