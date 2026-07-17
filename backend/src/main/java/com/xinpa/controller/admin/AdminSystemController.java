package com.xinpa.controller.admin;

import com.xinpa.common.BusinessException;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.OrderSource;
import com.xinpa.entity.PackageType;
import com.xinpa.entity.PaymentMethod;
import com.xinpa.entity.SysAdmin;
import com.xinpa.entity.SysAnnouncement;
import com.xinpa.entity.VipLevel;
import com.xinpa.mapper.SysAdminMapper;
import com.xinpa.service.OrderSourceService;
import com.xinpa.service.PackageTypeService;
import com.xinpa.service.PaymentMethodService;
import com.xinpa.service.SysAnnouncementService;
import com.xinpa.service.VipLevelService;
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
    private final OrderSourceService orderSourceService;
    private final PaymentMethodService paymentMethodService;
    private final PackageTypeService packageTypeService;
    private final VipLevelService vipLevelService;
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

    // ==================== 来源 ====================

    @GetMapping("/order-sources")
    public Result<List<OrderSource>> listOrderSources() { return Result.ok(orderSourceService.listAll()); }

    @GetMapping("/order-sources/list-enabled")
    public Result<List<OrderSource>> listEnabledSources() { return Result.ok(orderSourceService.listEnabled()); }

    @PostMapping("/order-sources")
    public Result<Void> createOrderSource(@RequestBody OrderSource s) { orderSourceService.create(s); return Result.ok(); }

    @PutMapping("/order-sources")
    public Result<Void> updateOrderSource(@RequestBody OrderSource s) { orderSourceService.update(s); return Result.ok(); }

    @DeleteMapping("/order-sources/{id}")
    public Result<Void> deleteOrderSource(@PathVariable Long id) { orderSourceService.delete(id); return Result.ok(); }

    @PutMapping("/order-sources/{id}/status")
    public Result<Void> toggleOrderSourceStatus(@PathVariable Long id, @RequestParam Integer status) {
        OrderSource s = new OrderSource(); s.setId(id); s.setStatus(status);
        orderSourceService.update(s);
        return Result.ok();
    }

    // ==================== 支付方式 ====================

    @GetMapping("/payment-methods")
    public Result<List<PaymentMethod>> listPaymentMethods() { return Result.ok(paymentMethodService.listAll()); }

    @PostMapping("/payment-methods")
    public Result<Void> createPaymentMethod(@RequestBody PaymentMethod pm) { paymentMethodService.create(pm); return Result.ok(); }

    @PutMapping("/payment-methods")
    public Result<Void> updatePaymentMethod(@RequestBody PaymentMethod pm) { paymentMethodService.update(pm); return Result.ok(); }

    @DeleteMapping("/payment-methods/{id}")
    public Result<Void> deletePaymentMethod(@PathVariable Long id) { paymentMethodService.delete(id); return Result.ok(); }

    @PutMapping("/payment-methods/{id}/status")
    public Result<Void> togglePaymentMethodStatus(@PathVariable Long id, @RequestParam Integer status) {
        PaymentMethod pm = new PaymentMethod(); pm.setId(id); pm.setStatus(status);
        paymentMethodService.update(pm);
        return Result.ok();
    }

    // ==================== 套餐类型 ====================

    @GetMapping("/package-types")
    public Result<List<PackageType>> listPackageTypes() { return Result.ok(packageTypeService.listAll()); }

    @PostMapping("/package-types")
    public Result<Void> createPackageType(@RequestBody PackageType pt) { packageTypeService.create(pt); return Result.ok(); }

    @PutMapping("/package-types")
    public Result<Void> updatePackageType(@RequestBody PackageType pt) { packageTypeService.update(pt); return Result.ok(); }

    @DeleteMapping("/package-types/{id}")
    public Result<Void> deletePackageType(@PathVariable Long id) { packageTypeService.delete(id); return Result.ok(); }

    @PutMapping("/package-types/{id}/status")
    public Result<Void> togglePackageTypeStatus(@PathVariable Long id, @RequestParam Integer status) {
        PackageType pt = new PackageType(); pt.setId(id); pt.setStatus(status);
        packageTypeService.update(pt);
        return Result.ok();
    }

    // ==================== VIP等级 ====================

    @GetMapping("/vip-levels")
    public Result<List<VipLevel>> listVipLevels() { return Result.ok(vipLevelService.listAll()); }

    @PostMapping("/vip-levels")
    public Result<Void> createVipLevel(@RequestBody VipLevel vl) { vipLevelService.create(vl); return Result.ok(); }

    @PutMapping("/vip-levels")
    public Result<Void> updateVipLevel(@RequestBody VipLevel vl) { vipLevelService.update(vl); return Result.ok(); }

    @DeleteMapping("/vip-levels/{id}")
    public Result<Void> deleteVipLevel(@PathVariable Long id) { vipLevelService.delete(id); return Result.ok(); }

    @PutMapping("/vip-levels/{id}/status")
    public Result<Void> toggleVipLevelStatus(@PathVariable Long id, @RequestParam Integer status) {
        VipLevel vl = new VipLevel(); vl.setId(id); vl.setStatus(status);
        vipLevelService.update(vl);
        return Result.ok();
    }

    @GetMapping("/vip-levels/list-enabled")
    public Result<List<VipLevel>> listEnabledVipLevels() { return Result.ok(vipLevelService.listEnabled()); }
}
