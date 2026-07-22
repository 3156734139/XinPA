package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xinpa.dto.OrderQueryDTO;
import com.xinpa.entity.Customer;
import com.xinpa.entity.FinanceRecord;
import com.xinpa.entity.Order;
import com.xinpa.entity.PaymentMethod;
import com.xinpa.entity.PricePackage;
import com.xinpa.entity.VipLevel;
import com.xinpa.mapper.FinanceRecordMapper;
import com.xinpa.mapper.OrderMapper;
import com.xinpa.mapper.PaymentMethodMapper;
import com.xinpa.mapper.PricePackageMapper;
import com.xinpa.service.CustomerService;
import com.xinpa.service.OrderService;
import com.xinpa.service.VipLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单服务实现
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final AtomicInteger SEQ = new AtomicInteger(0);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final OrderMapper orderMapper;
    private final FinanceRecordMapper financeRecordMapper;
    private final PaymentMethodMapper paymentMethodMapper;
    private final CustomerService customerService;
    private final VipLevelService vipLevelService;
    private final PricePackageMapper pricePackageMapper;

    @Override
    public Page<Order> page(OrderQueryDTO query) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, query.getUserId())
                .eq(query.getOrderSource() != null, Order::getOrderSource, query.getOrderSource())
                .eq(query.getCustomerId() != null, Order::getCustomerId, query.getCustomerId())
                .eq(Order::getDeleted, 0);

        // 动态排序：默认按开始时间倒序
        String sortField = query.getSortField();
        boolean asc = "asc".equalsIgnoreCase(query.getSortOrder());
        if (sortField != null && !sortField.isEmpty()) {
            switch (sortField) {
                case "startTime" -> wrapper.orderBy(true, asc, Order::getStartTime);
                case "endTime" -> wrapper.orderBy(true, asc, Order::getEndTime);
                case "finalAmount" -> wrapper.orderBy(true, asc, Order::getFinalAmount);
                case "actualMinutes" -> wrapper.orderBy(true, asc, Order::getActualMinutes);
                case "createdAt" -> wrapper.orderBy(true, asc, Order::getCreatedAt);
                default -> wrapper.orderByDesc(Order::getStartTime);
            }
        } else {
            wrapper.orderByDesc(Order::getStartTime);
        }

        if (query.getStartDate() != null) {
            wrapper.ge(Order::getCreatedAt, query.getStartDate().atStartOfDay());
        }
        if (query.getEndDate() != null) {
            wrapper.le(Order::getCreatedAt, query.getEndDate().plusDays(1).atStartOfDay());
        }
        if (query.getMinAmount() != null) {
            wrapper.ge(Order::getFinalAmount, query.getMinAmount());
        }
        if (query.getMaxAmount() != null) {
            wrapper.le(Order::getFinalAmount, query.getMaxAmount());
        }
        if (query.getMinMinutes() != null) {
            wrapper.ge(Order::getActualMinutes, query.getMinMinutes());
        }
        if (query.getMaxMinutes() != null) {
            wrapper.le(Order::getActualMinutes, query.getMaxMinutes());
        }
        if (StringUtils.hasText(query.getPackageName())) {
            wrapper.like(Order::getPackageName, query.getPackageName().trim());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Order::getOrderNo, kw)
                    .or().like(Order::getPackageName, kw)
                    .or().like(Order::getTitle, kw));
        }
        if (query.getStartTimeStart() != null) {
            wrapper.ge(Order::getStartTime, query.getStartTimeStart().atStartOfDay());
        }
        if (query.getStartTimeEnd() != null) {
            wrapper.le(Order::getStartTime, query.getStartTimeEnd().plusDays(1).atStartOfDay());
        }
        return orderMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()), wrapper);
    }

    @Override
    public Order getById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order != null && order.getCustomerId() != null) {
            Customer customer = customerService.getById(order.getCustomerId());
            if (customer != null) {
                order.setCustomerName(customer.getNickname());
            }
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Order order) {
        // 生成订单编号
        order.setOrderNo("XO" + LocalDateTime.now().format(FMT)
                + String.format("%03X", SEQ.getAndIncrement() & 0xFFF));

        // 自动填充套餐名称和标题，并判断是否为统一定价（非小时单）
        boolean flatRate = false;
        if (order.getPackageId() != null) {
            PricePackage pkg = pricePackageMapper.selectById(order.getPackageId());
            if (pkg != null) {
                order.setPackageName(pkg.getName());
                order.setUnit(pkg.getUnit());
                flatRate = !"小时".equals(pkg.getUnit());
            }
        }
        if (order.getTitle() == null || order.getTitle().isEmpty()) {
            order.setTitle(order.getPackageName() != null ? order.getPackageName() : "临时订单");
        }

        order.setStatus(4); // 已完结

        // 计算实际分钟数（支持跨零点）
        long rawMinutes = ChronoUnit.MINUTES.between(order.getStartTime(), order.getEndTime());
        boolean overnight = rawMinutes < 0;
        int minutes = overnight ? (int) (rawMinutes + 1440) : (int) rawMinutes;
        order.setActualMinutes(minutes);
        order.setIsOvernight(overnight ? 1 : 0);

        // 自动核算费用
        if (flatRate) {
            // 包夜/包月/教学/线下：统一定价，不计小时
            order.setExtraMinutes(0);
            order.setBilledMinutes(minutes);
            order.setTotalAmount(order.getUnitPrice());
        } else {
            // 小时单或无套餐：按小时计费，15/45 分钟舍入
            double billableHours = calcBillableHours(minutes);
            int extraMinutes = (int) (billableHours * 60) - minutes;
            order.setExtraMinutes(extraMinutes);
            order.setBilledMinutes((int) (billableHours * 60));

            BigDecimal total = order.getUnitPrice().multiply(BigDecimal.valueOf(billableHours));
            order.setTotalAmount(total);
        }

        // 无套餐时自动推断计价单位
        if (order.getUnit() == null) {
            order.setUnit(overnight ? "次" : "小时");
        }

        BigDecimal ratio = order.getSettleRatio() != null ? order.getSettleRatio() : new BigDecimal("100");
        BigDecimal afterRatio = order.getTotalAmount().multiply(ratio).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;
        // 手动选择是否应用VIP折扣
        if (Boolean.TRUE.equals(order.getApplyVipDiscount())) {
            BigDecimal vipDiscount = calcVipDiscount(order.getCustomerId(), afterRatio);
            discount = discount.add(vipDiscount);
        }
        order.setDiscountAmount(discount);
        order.setFinalAmount(afterRatio.subtract(discount));
        order.setSettleTime(LocalDateTime.now());

        orderMapper.insert(order);

        // 自动产生收入记录
        FinanceRecord record = new FinanceRecord();
        record.setUserId(order.getUserId());
        record.setOrderId(order.getId());
        record.setRecordType(1);
        record.setCategory("陪玩收入");
        record.setPaymentMethod(resolvePaymentMethod(order, null));
        record.setAmount(order.getFinalAmount() != null ? order.getFinalAmount() : BigDecimal.ZERO);
        record.setRecordDate(order.getStartTime() != null ? order.getStartTime().toLocalDate() : LocalDate.now());
        financeRecordMapper.insert(record);

        customerService.refreshCustomerStats(order.getCustomerId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Order order) {
        Order existing = orderMapper.selectById(order.getId());
        if (existing == null) throw new com.xinpa.common.BusinessException("订单不存在");
        if (!existing.getUserId().equals(order.getUserId())) {
            throw new com.xinpa.common.BusinessException("无权操作该订单");
        }

        // 重新核算费用
        if (order.getStartTime() != null && order.getEndTime() != null) {
            long rawMinutes = ChronoUnit.MINUTES.between(order.getStartTime(), order.getEndTime());
            boolean overnight = rawMinutes < 0;
            int minutes = overnight ? (int) (rawMinutes + 1440) : (int) rawMinutes;
            order.setActualMinutes(minutes);
            order.setIsOvernight(overnight ? 1 : 0);

            // 判断套餐是否为统一定价（非小时单）
            Long pkgId = order.getPackageId() != null ? order.getPackageId() : existing.getPackageId();
            boolean flatRate = false;
            if (pkgId != null) {
                PricePackage pkg = pricePackageMapper.selectById(pkgId);
                if (pkg != null) {
                    order.setUnit(pkg.getUnit());
                    flatRate = !"小时".equals(pkg.getUnit());
                }
            }

            BigDecimal unitPrice = order.getUnitPrice() != null ? order.getUnitPrice() : existing.getUnitPrice();

            if (flatRate) {
                order.setExtraMinutes(0);
                order.setBilledMinutes(minutes);
                order.setTotalAmount(unitPrice);
            } else {
                double billableHours = calcBillableHours(minutes);
                int extraMinutes = (int) (billableHours * 60) - minutes;
                order.setExtraMinutes(extraMinutes);
                order.setBilledMinutes((int) (billableHours * 60));
                BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(billableHours));
                order.setTotalAmount(total);
            }

            // 无套餐时保留原有计价单位或推断
            if (order.getUnit() == null) {
                order.setUnit(existing.getUnit() != null ? existing.getUnit() : (overnight ? "次" : "小时"));
            }

            BigDecimal ratio = order.getSettleRatio() != null ? order.getSettleRatio()
                    : (existing.getSettleRatio() != null ? existing.getSettleRatio() : new BigDecimal("100"));
            BigDecimal afterRatio = order.getTotalAmount().multiply(ratio).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount()
                    : (existing.getDiscountAmount() != null ? existing.getDiscountAmount() : BigDecimal.ZERO);
            // 手动选择是否应用VIP折扣
            if (Boolean.TRUE.equals(order.getApplyVipDiscount())) {
                Long customerId = order.getCustomerId() != null ? order.getCustomerId() : existing.getCustomerId();
                BigDecimal vipDiscount = calcVipDiscount(customerId, afterRatio);
                discount = discount.add(vipDiscount);
            }
            order.setDiscountAmount(discount);
            order.setFinalAmount(afterRatio.subtract(discount));
        }

        order.setStatus(4);
        orderMapper.updateById(order);

        // 同步更新财务记录
        BigDecimal finalAmount = order.getFinalAmount() != null ? order.getFinalAmount() : existing.getFinalAmount();
        if (finalAmount != null && finalAmount.compareTo(BigDecimal.ZERO) > 0) {
            FinanceRecord record = financeRecordMapper.selectOne(
                    new LambdaQueryWrapper<FinanceRecord>()
                            .eq(FinanceRecord::getOrderId, order.getId())
                            .eq(FinanceRecord::getRecordType, 1)
                            .last("LIMIT 1"));
            if (record != null) {
                record.setAmount(finalAmount);
                record.setPaymentMethod(resolvePaymentMethod(order, existing));
                financeRecordMapper.updateById(record);
            } else {
                FinanceRecord newRecord = new FinanceRecord();
                newRecord.setUserId(existing.getUserId());
                newRecord.setOrderId(order.getId());
                newRecord.setRecordType(1);
                newRecord.setCategory("陪玩收入");
                newRecord.setPaymentMethod(resolvePaymentMethod(order, existing));
                newRecord.setAmount(finalAmount);
                LocalDateTime startTime = order.getStartTime() != null ? order.getStartTime() : existing.getStartTime();
                newRecord.setRecordDate(startTime != null ? startTime.toLocalDate() : LocalDate.now());
                financeRecordMapper.insert(newRecord);
            }
        }

        customerService.refreshCustomerStats(existing.getCustomerId());
    }

    @Override
    public List<Map<String, Object>> getPackageStats(Long userId) {
        return orderMapper.selectPackageStats(userId);
    }

    /**
     * 根据客户的VIP等级计算折扣金额
     */
    private BigDecimal calcVipDiscount(Long customerId, BigDecimal afterRatio) {
        if (customerId == null) return BigDecimal.ZERO;
        Customer customer = customerService.getById(customerId);
        if (customer == null || customer.getSpendLevel() == null || customer.getSpendLevel() <= 0) {
            return BigDecimal.ZERO;
        }
        List<VipLevel> vipLevels = vipLevelService.listEnabled(customer.getUserId());
        for (VipLevel vl : vipLevels) {
            if (vl.getLevel().equals(customer.getSpendLevel()) && vl.getDiscount() != null && vl.getDiscount() < 100) {
                return afterRatio.multiply(BigDecimal.valueOf(100 - vl.getDiscount()))
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ZERO;
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

    /**
     * 解析支付方式名称：优先用 order 上的值，若为空则从 existing 回退
     */
    private String resolvePaymentMethod(Order order, Order fallback) {
        String pm = order.getPaymentMethod();
        Long pmId = order.getPaymentMethodId();
        if (pm == null && pmId == null && fallback != null) {
            pm = fallback.getPaymentMethod();
            pmId = fallback.getPaymentMethodId();
        }
        if (pm != null && !pm.isEmpty()) return pm;
        if (pmId != null) {
            PaymentMethod p = paymentMethodMapper.selectById(pmId);
            if (p != null) return p.getName();
        }
        return null;
    }
}
