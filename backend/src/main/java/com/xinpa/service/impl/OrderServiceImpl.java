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
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        // 生成订单编号: XO + SHA-256取前16位
        try {
            String raw = System.currentTimeMillis() + String.format("%06d", (int)(Math.random() * 1000000));
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            order.setOrderNo("XO" + hex.substring(0, 16).toUpperCase());
        } catch (NoSuchAlgorithmException e) {
            // fallback
            String fallback = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                    + String.format("%04d", (int)(Math.random() * 10000));
            order.setOrderNo("XO" + fallback);
        }

        // 如果同时填了开始和结束时间，创建即完成
        if (order.getStartTime() != null && order.getEndTime() != null) {
            order.setStatus(4); // 已完结

            // 计算实际分钟数（支持跨零点）
            long rawMinutes = ChronoUnit.MINUTES.between(order.getStartTime(), order.getEndTime());
            boolean overnight = rawMinutes < 0;
            int minutes = overnight ? (int) (rawMinutes + 1440) : (int) rawMinutes;
            order.setActualMinutes(minutes);
            order.setIsOvernight(overnight ? 1 : 0);

            // 自动核算费用（计费规则：超过15min算0.5h，超过45min算1h）
            double billableHours = calcBillableHours(minutes);
            BigDecimal total = order.getUnitPrice().multiply(BigDecimal.valueOf(billableHours));
            order.setTotalAmount(total);

            BigDecimal ratio = order.getSettleRatio() != null ? order.getSettleRatio() : new BigDecimal("100");
            BigDecimal afterRatio = total.multiply(ratio).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (order.getDiscountAmount() == null) {
                order.setDiscountAmount(BigDecimal.ZERO);
            }
            order.setFinalAmount(afterRatio.subtract(order.getDiscountAmount()));

            order.setSettleTime(LocalDateTime.now());
            orderMapper.insert(order);

            // 自动产生收入记录（同 settle() 逻辑）
            FinanceRecord record = new FinanceRecord();
            record.setUserId(order.getUserId());
            record.setOrderId(order.getId());
            record.setRecordType(1);
            record.setCategory("陪玩收入");
            record.setAmount(order.getFinalAmount() != null ? order.getFinalAmount() : BigDecimal.ZERO);
            record.setRecordDate(LocalDate.now());
            financeRecordMapper.insert(record);
        } else {
            order.setStatus(1); // 待接单
            orderMapper.insert(order);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Order order) {
        Order existing = orderMapper.selectById(order.getId());
        if (existing == null) throw new BusinessException("订单不存在");

        // 如果传了开始/结束时间，重新核算费用（支持跨零点）
        if (order.getStartTime() != null && order.getEndTime() != null) {
            long rawMinutes = ChronoUnit.MINUTES.between(order.getStartTime(), order.getEndTime());
            boolean overnight = rawMinutes < 0;
            int minutes = overnight ? (int) (rawMinutes + 1440) : (int) rawMinutes;
            order.setActualMinutes(minutes);
            order.setIsOvernight(overnight ? 1 : 0);

            double billableHours = calcBillableHours(minutes);
            BigDecimal unitPrice = order.getUnitPrice() != null ? order.getUnitPrice() : existing.getUnitPrice();
            BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(billableHours));
            order.setTotalAmount(total);

            BigDecimal ratio = order.getSettleRatio() != null ? order.getSettleRatio()
                    : (existing.getSettleRatio() != null ? existing.getSettleRatio() : new BigDecimal("100"));
            BigDecimal afterRatio = total.multiply(ratio).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal discount = existing.getDiscountAmount() != null ? existing.getDiscountAmount() : BigDecimal.ZERO;
            order.setDiscountAmount(discount);
            order.setFinalAmount(afterRatio.subtract(discount));

            // 待接单→已完结
            if (existing.getStatus() == 1) {
                order.setStatus(4);
                order.setSettleTime(LocalDateTime.now());
            }
        }

        orderMapper.updateById(order);

        // 同步更新财务记录
        BigDecimal finalAmount = order.getFinalAmount() != null ? order.getFinalAmount() : existing.getFinalAmount();
        Integer newStatus = order.getStatus() != null ? order.getStatus() : existing.getStatus();
        if (finalAmount != null && finalAmount.compareTo(BigDecimal.ZERO) > 0) {
            FinanceRecord record = financeRecordMapper.selectOne(
                    new LambdaQueryWrapper<FinanceRecord>()
                            .eq(FinanceRecord::getOrderId, order.getId())
                            .eq(FinanceRecord::getRecordType, 1)
                            .last("LIMIT 1"));
            if (record != null) {
                // 更新已有财务记录
                record.setAmount(finalAmount);
                financeRecordMapper.updateById(record);
            } else if (existing.getStatus() == 1 && newStatus == 4) {
                // 待接单→已完结，新增财务记录
                FinanceRecord newRecord = new FinanceRecord();
                newRecord.setUserId(existing.getUserId());
                newRecord.setOrderId(order.getId());
                newRecord.setRecordType(1);
                newRecord.setCategory("陪玩收入");
                newRecord.setAmount(finalAmount);
                newRecord.setRecordDate(LocalDate.now());
                financeRecordMapper.insert(newRecord);
            }
        }
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

        // 自动核算费用（计费规则：超过15min算0.5h，超过45min算1h）
        double billableHours = calcBillableHours(order.getActualMinutes() + order.getExtraMinutes());
        BigDecimal total = order.getUnitPrice().multiply(BigDecimal.valueOf(billableHours));
        order.setTotalAmount(total);

        BigDecimal ratio = order.getSettleRatio() != null ? order.getSettleRatio() : new BigDecimal("100");
        BigDecimal afterRatio = total.multiply(ratio).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        if (order.getDiscountAmount() == null) {
            order.setDiscountAmount(BigDecimal.ZERO);
        }
        order.setFinalAmount(afterRatio.subtract(order.getDiscountAmount()));

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

        double billableHours = calcBillableHours(order.getActualMinutes() + order.getExtraMinutes());
        BigDecimal total = order.getUnitPrice().multiply(BigDecimal.valueOf(billableHours));
        order.setTotalAmount(total);

        BigDecimal ratio = order.getSettleRatio() != null ? order.getSettleRatio() : new BigDecimal("100");
        BigDecimal afterRatio = total.multiply(ratio).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        if (order.getDiscountAmount() == null) {
            order.setDiscountAmount(BigDecimal.ZERO);
        }
        order.setFinalAmount(afterRatio.subtract(order.getDiscountAmount()));

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

        double billableHours = calcBillableHours(order.getActualMinutes() + order.getExtraMinutes());
        BigDecimal total = order.getUnitPrice().multiply(BigDecimal.valueOf(billableHours));
        order.setTotalAmount(total);

        BigDecimal ratio = order.getSettleRatio() != null ? order.getSettleRatio() : new BigDecimal("100");
        BigDecimal afterRatio = total.multiply(ratio).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        if (order.getDiscountAmount() == null) {
            order.setDiscountAmount(BigDecimal.ZERO);
        }
        order.setFinalAmount(afterRatio.subtract(order.getDiscountAmount()));
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

    /**
     * 计费规则：超过15min算0.5小时，超过45min算1小时
     */
    private static double calcBillableHours(int totalMinutes) {
        int hours = totalMinutes / 60;
        int remainder = totalMinutes % 60;
        double extra = 0;
        if (remainder > 45) {
            extra = 1;
        } else if (remainder > 15) {
            extra = 0.5;
        }
        return hours + extra;
    }
}
