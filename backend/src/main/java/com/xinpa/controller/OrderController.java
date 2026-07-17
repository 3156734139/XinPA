package com.xinpa.controller;

import com.xinpa.common.PageResult;
import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.dto.OrderQueryDTO;
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
import java.time.LocalDate;
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
            @RequestParam(required = false) Integer source,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) Integer minMinutes,
            @RequestParam(required = false) Integer maxMinutes,
            @RequestParam(required = false) String packageName,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size) {
        OrderQueryDTO query = new OrderQueryDTO();
        query.setUserId(UserContext.getUserId());
        query.setOrderSource(source);
        query.setKeyword(keyword);
        query.setCustomerId(customerId);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        query.setMinAmount(minAmount);
        query.setMaxAmount(maxAmount);
        query.setMinMinutes(minMinutes);
        query.setMaxMinutes(maxMinutes);
        query.setPackageName(packageName);
        query.setCurrent(current);
        query.setSize(size);
        return Result.ok(PageResult.of(orderService.page(query)));
    }

    /**
     * 订单详情
     */
    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(UserContext.getUserId())) {
            return Result.fail("订单不存在");
        }
        return Result.ok(order);
    }

    /**
     * 创建订单
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody Order order) {
        order.setUserId(UserContext.getUserId());
        if (order.getPackageId() != null) {
            PricePackage pkg = pricePackageService.getById(order.getPackageId());
            if (pkg != null && pkg.getUserId().equals(UserContext.getUserId())) {
                order.setUnitPrice(pkg.getPrice());
                order.setPackageName(pkg.getName());
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
        order.setUserId(UserContext.getUserId());
        orderService.update(order);
        return Result.ok();
    }

    // ==================== 预约日历 ====================

    @GetMapping("/appointments")
    public Result<?> appointments(
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end) {
        return Result.ok(appointmentService.listByDateRange(UserContext.getUserId(), start, end));
    }

    @PostMapping("/appointments")
    public Result<Void> createAppointment(@RequestBody Appointment appointment) {
        appointment.setUserId(UserContext.getUserId());
        appointmentService.create(appointment);
        return Result.ok();
    }

    @PutMapping("/appointments")
    public Result<Void> updateAppointment(@RequestBody Appointment appointment) {
        appointmentService.update(appointment);
        return Result.ok();
    }

    @DeleteMapping("/appointments/{id}")
    public Result<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.delete(id, UserContext.getUserId());
        return Result.ok();
    }

    @GetMapping("/appointments/conflict")
    public Result<?> checkConflict(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            @RequestParam(required = false) Long excludeId) {
        return Result.ok(appointmentService.checkConflict(UserContext.getUserId(), start, end, excludeId));
    }
}
