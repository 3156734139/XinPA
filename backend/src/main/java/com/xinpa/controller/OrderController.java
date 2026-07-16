package com.xinpa.controller;

import com.xinpa.common.PageResult;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.dto.OrderQueryDTO;
import com.xinpa.dto.OrderTimerDTO;
import com.xinpa.entity.Appointment;
import com.xinpa.entity.Order;
import com.xinpa.entity.PricePackage;
import com.xinpa.service.AppointmentService;
import com.xinpa.service.OrderService;
import com.xinpa.service.PricePackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单管理接口
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AppointmentService appointmentService;
    private final PricePackageService pricePackageService;

    /**
     * 分页查询订单
     */
    @GetMapping
    public Result<PageResult<Order>> page(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer source,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size) {
        OrderQueryDTO query = new OrderQueryDTO();
        query.setUserId(UserContext.getUserId());
        query.setStatus(status);
        query.setOrderSource(source);
        query.setKeyword(keyword);
        query.setCurrent(current);
        query.setSize(size);
        return Result.ok(PageResult.of(orderService.page(query)));
    }

    /**
     * 订单详情
     */
    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        return Result.ok(orderService.getById(id));
    }

    /**
     * 创建订单（与价目套餐联动：传入packageId自动填充单价）
     */
    @PostMapping
    public Result<Void> create(@RequestBody Order order) {
        order.setUserId(UserContext.getUserId());
        // 如果传入了套餐ID，自动从套餐表中读取单价和套餐名称并填充
        if (order.getPackageId() != null) {
            PricePackage pkg = pricePackageService.getById(order.getPackageId());
            if (pkg != null && pkg.getUserId().equals(UserContext.getUserId())) {
                order.setUnitPrice(pkg.getPrice());
                order.setPackageName(pkg.getName());
                if (order.getTitle() == null) {
                    order.setTitle(pkg.getName());
                }
            }
        }
        orderService.create(order);
        return Result.ok();
    }

    /**
     * 更新订单
     */
    @PutMapping
    public Result<Void> update(@RequestBody Order order) {
        orderService.update(order);
        return Result.ok();
    }

    /**
     * 开始计时
     */
    @PostMapping("/timer/start")
    public Result<Void> startTimer(@Valid @RequestBody OrderTimerDTO dto) {
        orderService.startTimer(dto);
        return Result.ok();
    }

    /**
     * 暂停计时
     */
    @PostMapping("/timer/pause")
    public Result<Void> pauseTimer(@Valid @RequestBody OrderTimerDTO dto) {
        orderService.pauseTimer(dto);
        return Result.ok();
    }

    /**
     * 结束计时
     */
    @PostMapping("/timer/end")
    public Result<Void> endTimer(@Valid @RequestBody OrderTimerDTO dto) {
        orderService.endTimer(dto);
        return Result.ok();
    }

    /**
     * 补时
     */
    @PostMapping("/timer/extra")
    public Result<Void> addExtra(@Valid @RequestBody OrderTimerDTO dto) {
        orderService.addExtraMinutes(dto);
        return Result.ok();
    }

    /**
     * 结算
     */
    @PostMapping("/{id}/settle")
    public Result<Void> settle(@PathVariable Long id) {
        orderService.settle(id, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 退款
     */
    @PostMapping("/{id}/refund")
    public Result<Void> refund(@PathVariable Long id) {
        orderService.refund(id, UserContext.getUserId());
        return Result.ok();
    }

    // ==================== 预约日历 ====================

    /**
     * 获取预约列表
     */
    @GetMapping("/appointments")
    public Result<?> appointments(
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end) {
        return Result.ok(appointmentService.listByDateRange(UserContext.getUserId(), start, end));
    }

    /**
     * 创建预约
     */
    @PostMapping("/appointments")
    public Result<Void> createAppointment(@RequestBody Appointment appointment) {
        appointment.setUserId(UserContext.getUserId());
        appointmentService.create(appointment);
        return Result.ok();
    }

    /**
     * 更新预约
     */
    @PutMapping("/appointments")
    public Result<Void> updateAppointment(@RequestBody Appointment appointment) {
        appointmentService.update(appointment);
        return Result.ok();
    }

    /**
     * 删除预约
     */
    @DeleteMapping("/appointments/{id}")
    public Result<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.delete(id, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 检查预约冲突
     */
    @GetMapping("/appointments/conflict")
    public Result<?> checkConflict(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            @RequestParam(required = false) Long excludeId) {
        return Result.ok(appointmentService.checkConflict(UserContext.getUserId(), start, end, excludeId));
    }
}
