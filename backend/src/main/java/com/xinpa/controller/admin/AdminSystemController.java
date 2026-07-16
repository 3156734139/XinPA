package com.xinpa.controller.admin;

import com.xinpa.common.BusinessException;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.OrderSource;
import com.xinpa.entity.SysAdmin;
import com.xinpa.entity.SysAnnouncement;
import com.xinpa.mapper.SysAdminMapper;
import com.xinpa.service.OrderSourceService;
import com.xinpa.service.SysAnnouncementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    private final SysAnnouncementService sysAnnouncementService;
    private final OrderSourceService orderSourceService;
    private final com.xinpa.service.PaymentMethodService paymentMethodService;
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

    // ==================== 订单来源管理 ====================

    /**
     * 来源列表
     */
    @GetMapping("/order-sources")
    public Result<List<OrderSource>> listOrderSources() {
        return Result.ok(orderSourceService.listAll());
    }

    /**
     * 已启用的来源列表（供用户端下拉选单）
     */
    @GetMapping("/order-sources/list-enabled")
    public Result<List<OrderSource>> listEnabledSources() {
        return Result.ok(orderSourceService.listEnabled());
    }

    /**
     * 新增来源
     */
    @PostMapping("/order-sources")
    public Result<Void> createOrderSource(@RequestBody OrderSource source) {
        orderSourceService.create(source);
        return Result.ok();
    }

    /**
     * 更新来源
     */
    @PutMapping("/order-sources")
    public Result<Void> updateOrderSource(@RequestBody OrderSource source) {
        orderSourceService.update(source);
        return Result.ok();
    }

    /**
     * 删除来源
     */
    @DeleteMapping("/order-sources/{id}")
    public Result<Void> deleteOrderSource(@PathVariable Long id) {
        orderSourceService.delete(id);
        return Result.ok();
    }

    /**
     * 启用/禁用来源
     */
    @PutMapping("/order-sources/{id}/status")
    public Result<Void> toggleOrderSourceStatus(@PathVariable Long id, @RequestParam Integer status) {
        OrderSource source = new OrderSource();
        source.setId(id);
        source.setStatus(status);
        orderSourceService.update(source);
        return Result.ok();
    }

    // ==================== 支付方式管理 ====================

    /**
     * 支付方式列表
     */
    @GetMapping("/payment-methods")
    public Result<List<com.xinpa.entity.PaymentMethod>> listPaymentMethods() {
        return Result.ok(paymentMethodService.listAll());
    }

    /**
     * 新增支付方式
     */
    @PostMapping("/payment-methods")
    public Result<Void> createPaymentMethod(@RequestBody com.xinpa.entity.PaymentMethod paymentMethod) {
        paymentMethodService.create(paymentMethod);
        return Result.ok();
    }

    /**
     * 更新支付方式
     */
    @PutMapping("/payment-methods")
    public Result<Void> updatePaymentMethod(@RequestBody com.xinpa.entity.PaymentMethod paymentMethod) {
        paymentMethodService.update(paymentMethod);
        return Result.ok();
    }

    /**
     * 删除支付方式
     */
    @DeleteMapping("/payment-methods/{id}")
    public Result<Void> deletePaymentMethod(@PathVariable Long id) {
        paymentMethodService.delete(id);
        return Result.ok();
    }

    /**
     * 启用/禁用支付方式
     */
    @PutMapping("/payment-methods/{id}/status")
    public Result<Void> togglePaymentMethodStatus(@PathVariable Long id, @RequestParam Integer status) {
        com.xinpa.entity.PaymentMethod pm = new com.xinpa.entity.PaymentMethod();
        pm.setId(id);
        pm.setStatus(status);
        paymentMethodService.update(pm);
        return Result.ok();
    }
}
