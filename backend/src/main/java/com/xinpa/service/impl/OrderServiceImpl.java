package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinpa.common.BusinessException;
import com.xinpa.dto.OrderQueryDTO;
import com.xinpa.dto.OrderTimerDTO;
import com.xinpa.entity.FinanceRecord;
import com.xinpa.entity.Order;
import com.xinpa.entity.OrderTimerLog;
import com.xinpa.mapper.FinanceRecordMapper;
import com.xinpa.mapper.OrderMapper;
import com.xinpa.mapper.OrderTimerLogMapper;
import com.xinpa.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * 订单服务实现
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderTimerLogMapper timerLogMapper;
    private final FinanceRecordMapper financeRecordMapper;

    @Override
    public Page<Order> page(OrderQueryDTO query) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, query.getUserId())
                .eq(query.getStatus() != null, Order::getStatus, query.getStatus())
                .eq(query.getOrderSource() != null, Order::getOrderSource, query.getOrderSource())
                .like(query.getKeyword() != null, Order::getTitle, query.getKeyword())
                .eq(Order::getDeleted, 0)
                .orderByDesc(Order::getCreatedAt);
        return orderMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()), wrapper);
    }

    @Override
    public Order getById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Order order) {
        // 生成订单编号
        String orderNo = "XO" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + (int)(Math.random() * 1000);
        order.setOrderNo(orderNo);
        order.setStatus(1); // 待接单
        orderMapper.insert(order);
    }

    @Override
    public void update(Order order) {
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startTimer(OrderTimerDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() != 1 && order.getStatus() != 3) {
            throw new BusinessException("当前状态无法开始计时");
        }

        order.setStartTime(LocalDateTime.now());
        order.setStatus(2); // 进行中
        orderMapper.updateById(order);

        OrderTimerLog log = new OrderTimerLog();
        log.setOrderId(dto.getOrderId());
        log.setUserId(order.getUserId());
        log.setAction(1);
        timerLogMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pauseTimer(OrderTimerDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() != 2) throw new BusinessException("订单未在进行中");

        int minutes = (int) ChronoUnit.MINUTES.between(order.getStartTime(), LocalDateTime.now());
        order.setActualMinutes(order.getActualMinutes() + minutes);
        order.setStatus(3); // 待结算
        order.setEndTime(LocalDateTime.now());

        // 自动核算费用
        BigDecimal totalHours = BigDecimal.valueOf((order.getActualMinutes() + order.getExtraMinutes()) / 60.0);
        order.setTotalAmount(order.getUnitPrice().multiply(totalHours));
        order.setFinalAmount(order.getTotalAmount().subtract(order.getDiscountAmount()));

        orderMapper.updateById(order);

        OrderTimerLog log = new OrderTimerLog();
        log.setOrderId(dto.getOrderId());
        log.setUserId(order.getUserId());
        log.setAction(2);
        log.setMinutes(minutes);
        timerLogMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endTimer(OrderTimerDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) throw new BusinessException("订单不存在");
        if (order.getStatus() != 2) throw new BusinessException("订单未在进行中");

        int minutes = (int) ChronoUnit.MINUTES.between(order.getStartTime(), LocalDateTime.now());
        order.setActualMinutes(order.getActualMinutes() + minutes);
        order.setEndTime(LocalDateTime.now());
        order.setSettleTime(LocalDateTime.now());
        order.setStatus(4); // 已完结

        BigDecimal totalHours = BigDecimal.valueOf((order.getActualMinutes() + order.getExtraMinutes()) / 60.0);
        order.setTotalAmount(order.getUnitPrice().multiply(totalHours));
        order.setFinalAmount(order.getTotalAmount().subtract(order.getDiscountAmount()));

        orderMapper.updateById(order);

        // 自动产生收入记录
        FinanceRecord record = new FinanceRecord();
        record.setUserId(order.getUserId());
        record.setOrderId(order.getId());
        record.setRecordType(1);
        record.setCategory("陪玩收入");
        record.setAmount(order.getFinalAmount());
        record.setRecordDate(LocalDate.now());
        financeRecordMapper.insert(record);

        OrderTimerLog log = new OrderTimerLog();
        log.setOrderId(dto.getOrderId());
        log.setUserId(order.getUserId());
        log.setAction(4);
        log.setMinutes(minutes);
        timerLogMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addExtraMinutes(OrderTimerDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null) throw new BusinessException("订单不存在");
        order.setExtraMinutes(order.getExtraMinutes() + dto.getExtraMinutes());

        BigDecimal totalHours = BigDecimal.valueOf((order.getActualMinutes() + order.getExtraMinutes()) / 60.0);
        order.setTotalAmount(order.getUnitPrice().multiply(totalHours));
        order.setFinalAmount(order.getTotalAmount().subtract(order.getDiscountAmount()));
        orderMapper.updateById(order);

        OrderTimerLog log = new OrderTimerLog();
        log.setOrderId(dto.getOrderId());
        log.setUserId(order.getUserId());
        log.setAction(5);
        log.setMinutes(dto.getExtraMinutes());
        timerLogMapper.insert(log);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settle(Long id, Long userId) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        order.setSettleTime(LocalDateTime.now());
        order.setStatus(4); // 已完结
        orderMapper.updateById(order);

        // 产生收入记录
        FinanceRecord record = new FinanceRecord();
        record.setUserId(userId);
        record.setOrderId(order.getId());
        record.setRecordType(1);
        record.setCategory("陪玩收入");
        record.setAmount(order.getFinalAmount());
        record.setRecordDate(LocalDate.now());
        financeRecordMapper.insert(record);
    }

    @Override
    public List<Map<String, Object>> getPackageStats(Long userId) {
        return orderMapper.selectPackageStats(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long id, Long userId) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException("订单不存在");
        order.setStatus(5); // 售后退款
        orderMapper.updateById(order);
    }
}
