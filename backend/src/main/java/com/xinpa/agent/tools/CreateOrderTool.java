package com.xinpa.agent.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.Customer;
import com.xinpa.entity.Order;
import com.xinpa.entity.VipLevel;
import com.xinpa.service.CustomerService;
import com.xinpa.service.OrderService;
import com.xinpa.service.PricePackageService;
import com.xinpa.service.VipLevelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateOrderTool implements AgentTool {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OrderService orderService;
    private final CustomerService customerService;
    private final VipLevelService vipLevelService;
    private final PricePackageService pricePackageService;
    private final ObjectMapper objectMapper;

    /** 预览数据暂存 */
    private final ConcurrentHashMap<String, PreviewData> previews = new ConcurrentHashMap<>();

    @Override
    public String getName() {
        return "create_order";
    }

    @Override
    public String getDescription() {
        return "创建订单，支持两阶段流程：第一次调用返回预确认信息，确认后第二次调用真正创建。" +
                "参数包括客户ID、开始/结束时间、单价、支付方式等。";
    }

    @Override
    public Map<String, Object> getParameters() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("customerId", Map.of("type", "number", "description", "客户ID（必填）"));
        properties.put("startTime", Map.of("type", "string", "description", "开始时间，格式 yyyy-MM-dd HH:mm（必填）"));
        properties.put("endTime", Map.of("type", "string", "description", "结束时间，格式 yyyy-MM-dd HH:mm（必填）"));
        properties.put("unitPrice", Map.of("type", "number", "description", "单价（必填）"));
        properties.put("packageId", Map.of("type", "number", "description", "套餐ID（必填）"));
        properties.put("orderSource", Map.of("type", "number", "description", "来源ID（必填，如 1=pw店 2=抖音 3=小红书 4=其他）"));
        properties.put("paymentMethodId", Map.of("type", "number", "description", "支付方式ID（必填）"));
        properties.put("settleRatio", Map.of("type", "number", "description", "到手比例，默认100（可选）"));
        properties.put("title", Map.of("type", "string", "description", "订单标题（可选）"));
        // confirm 模式专用
        properties.put("confirmToken", Map.of("type", "string", "description", "确认凭证，传入此项表示确认创建"));
        properties.put("edits", Map.of("type", "object", "description", "用户编辑过的字段，仅确认时传入"));
        return Map.of(
                "type", "object",
                "properties", properties,
                "required", List.of("customerId", "startTime", "endTime", "unitPrice", "packageId", "orderSource", "paymentMethodId")
        );
    }

    @Override
    public String execute(Long userId, String argsJson) {
        try {
            Map<String, Object> args = objectMapper.readValue(argsJson,
                    new TypeReference<Map<String, Object>>() {});

            // 确认模式
            if (args.containsKey("confirmToken")) {
                return confirm(userId, args);
            }

            // ====== 预览模式 ======
            Long customerId = args.get("customerId") != null
                    ? ((Number) args.get("customerId")).longValue() : null;
            String startTimeStr = (String) args.get("startTime");
            String endTimeStr = (String) args.get("endTime");
            BigDecimal unitPrice = args.get("unitPrice") != null
                    ? new BigDecimal(args.get("unitPrice").toString()) : null;
            Long packageId = args.get("packageId") != null
                    ? ((Number) args.get("packageId")).longValue() : null;

            Integer orderSource = args.get("orderSource") != null
                    ? ((Number) args.get("orderSource")).intValue() : null;
            Long paymentMethodId = args.get("paymentMethodId") != null
                    ? ((Number) args.get("paymentMethodId")).longValue() : null;

            if (customerId == null) return err("缺少必填字段：customerId");
            if (startTimeStr == null) return err("缺少必填字段：startTime");
            if (endTimeStr == null) return err("缺少必填字段：endTime");
            if (unitPrice == null) return err("缺少必填字段：unitPrice");
            if (packageId == null) return err("缺少必填字段：packageId");
            if (orderSource == null) return err("缺少必填字段：orderSource");
            if (paymentMethodId == null) return err("缺少必填字段：paymentMethodId");

            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DTF);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, DTF);

            // 查询客户
            Customer customer = customerService.getById(customerId);
            if (customer == null) return err("客户不存在（ID: " + customerId + "）");

            // 计算时长
            long rawMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
            boolean overnight = rawMinutes < 0;
            int minutes = overnight ? (int) (rawMinutes + 1440) : (int) rawMinutes;

            // 判断是否为统一定价套餐（非小时单）
            boolean isPackageFlatRate = false;
            String packageName = null;
            if (packageId != null) {
                var pkg = pricePackageService.getById(packageId);
                if (pkg != null) {
                    packageName = pkg.getName();
                    isPackageFlatRate = !"小时".equals(pkg.getUnit());
                }
            }

            int billedMinutes;
            int extraMinutes;
            BigDecimal total;
            double billableHours = 0;

            if (isPackageFlatRate) {
                // 包夜/包月/教学/线下：统一定价
                billedMinutes = minutes;
                extraMinutes = 0;
                total = unitPrice;
            } else {
                // 小时单：按小时计费
                billableHours = calcBillableHours(minutes);
                extraMinutes = (int) (billableHours * 60) - minutes;
                billedMinutes = (int) (billableHours * 60);
                total = unitPrice.multiply(BigDecimal.valueOf(billableHours));
            }

            BigDecimal ratio = args.get("settleRatio") != null
                    ? new BigDecimal(args.get("settleRatio").toString())
                    : new BigDecimal("100");
            BigDecimal afterRatio = total.multiply(ratio)
                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            // VIP 折扣
            BigDecimal discount = BigDecimal.ZERO;
            boolean applyVip =
                    args.get("applyVipDiscount") instanceof Boolean
                            ? (Boolean) args.get("applyVipDiscount") : true;
            if (applyVip) {
                BigDecimal vipDiscount = calcVipDiscount(userId, customerId, afterRatio);
                discount = discount.add(vipDiscount);
            }

            BigDecimal finalAmount = afterRatio.subtract(discount);

            // 存储预览数据
            String token = UUID.randomUUID().toString();
            String paymentMethod = (String) args.get("paymentMethod");

            PreviewData pd = new PreviewData();
            pd.userId = userId;
            pd.customerId = customerId;
            pd.customerName = customer.getNickname();
            pd.packageId = packageId;
            pd.packageName = packageName;
            pd.orderSource = orderSource;
            pd.paymentMethodId = paymentMethodId;
            pd.startTime = startTimeStr;
            pd.endTime = endTimeStr;
            pd.unitPrice = unitPrice;
            pd.paymentMethod = paymentMethod;
            pd.settleRatio = ratio;
            pd.applyVipDiscount = applyVip;
            pd.title = (String) args.get("title");
            pd.actualMinutes = minutes;
            pd.billedMinutes = billedMinutes;
            pd.totalAmount = total;
            pd.discountAmount = discount;
            pd.finalAmount = finalAmount;

            previews.put(token, pd);

            // 构建返回
            Map<String, Object> previewMap = new LinkedHashMap<>();
            previewMap.put("confirmToken", token);
            previewMap.put("customerName", pd.customerName);
            previewMap.put("customerId", pd.customerId);
            previewMap.put("packageName", pd.packageName);
            previewMap.put("startTime", pd.startTime);
            previewMap.put("endTime", pd.endTime);
            previewMap.put("unitPrice", pd.unitPrice);
            previewMap.put("actualMinutes", pd.actualMinutes);
            previewMap.put("billedMinutes", pd.billedMinutes);
            previewMap.put("extraMinutes", extraMinutes);
            previewMap.put("totalAmount", pd.totalAmount);
            previewMap.put("discountAmount", pd.discountAmount);
            previewMap.put("finalAmount", pd.finalAmount);
            previewMap.put("paymentMethod", pd.paymentMethod);
            previewMap.put("settleRatio", pd.settleRatio);
            previewMap.put("applyVipDiscount", pd.applyVipDiscount);
            previewMap.put("title", pd.title);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("_type", "order_preview");
            result.put("confirmToken", token);
            result.put("preview", previewMap);

            return objectMapper.writeValueAsString(result);

        } catch (Exception e) {
            log.error("create_order preview error", e);
            return err(e.getMessage());
        }
    }

    /**
     * 确认创建订单
     */
    public Map<String, Object> confirmOrder(String token, Map<String, Object> edits, Long userId) {
        PreviewData pd = previews.get(token);
        if (pd == null) {
            return Map.of("success", false, "error", "确认凭证无效或已过期");
        }
        if (!pd.userId.equals(userId)) {
            return Map.of("success", false, "error", "无权操作");
        }

        // 应用编辑
        if (edits != null) {
            if (edits.containsKey("startTime")) pd.startTime = (String) edits.get("startTime");
            if (edits.containsKey("endTime")) pd.endTime = (String) edits.get("endTime");
            if (edits.containsKey("unitPrice")) pd.unitPrice = new BigDecimal(edits.get("unitPrice").toString());
            if (edits.containsKey("paymentMethod")) pd.paymentMethod = (String) edits.get("paymentMethod");
            if (edits.containsKey("settleRatio")) pd.settleRatio = new BigDecimal(edits.get("settleRatio").toString());
            if (edits.containsKey("applyVipDiscount")) pd.applyVipDiscount = (Boolean) edits.get("applyVipDiscount");
            if (edits.containsKey("title")) pd.title = (String) edits.get("title");
            if (edits.containsKey("packageId")) {
                pd.packageId = ((Number) edits.get("packageId")).longValue();
            }
            if (edits.containsKey("orderSource")) {
                pd.orderSource = ((Number) edits.get("orderSource")).intValue();
            }
            if (edits.containsKey("paymentMethodId")) {
                pd.paymentMethodId = ((Number) edits.get("paymentMethodId")).longValue();
            }
        }

        try {
            Order order = new Order();
            order.setUserId(userId);
            order.setCustomerId(pd.customerId);
            order.setPackageId(pd.packageId);
            order.setOrderSource(pd.orderSource);
            order.setTitle(pd.title);
            order.setUnitPrice(pd.unitPrice);
            order.setPaymentMethod(pd.paymentMethod);
            order.setPaymentMethodId(pd.paymentMethodId);
            order.setSettleRatio(pd.settleRatio);
            order.setApplyVipDiscount(pd.applyVipDiscount);
            order.setStartTime(LocalDateTime.parse(pd.startTime, DTF));
            order.setEndTime(LocalDateTime.parse(pd.endTime, DTF));

            orderService.create(order);

            previews.remove(token);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("orderNo", order.getOrderNo());
            result.put("finalAmount", order.getFinalAmount());
            result.put("customerName", pd.customerName);
            return result;

        } catch (Exception e) {
            log.error("confirm order error", e);
            return Map.of("success", false, "error", "创建订单失败: " + e.getMessage());
        }
    }

    private String confirm(Long userId, Map<String, Object> args) {
        String token = (String) args.get("confirmToken");
        @SuppressWarnings("unchecked")
        Map<String, Object> edits = args.get("edits") instanceof Map
                ? (Map<String, Object>) args.get("edits") : null;

        Map<String, Object> result = confirmOrder(token, edits, userId);
        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return err(e.getMessage());
        }
    }

    // ========== 计算逻辑（与 OrderServiceImpl 保持一致） ==========

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

    private BigDecimal calcVipDiscount(Long userId, Long customerId, BigDecimal afterRatio) {
        if (customerId == null) return BigDecimal.ZERO;
        Customer customer = customerService.getById(customerId);
        if (customer == null || customer.getSpendLevel() == null || customer.getSpendLevel() <= 0) {
            return BigDecimal.ZERO;
        }
        List<VipLevel> vipLevels = vipLevelService.listEnabled(userId);
        for (VipLevel vl : vipLevels) {
            if (vl.getLevel().equals(customer.getSpendLevel())
                    && vl.getDiscount() != null && vl.getDiscount() < 100) {
                return afterRatio.multiply(BigDecimal.valueOf(100 - vl.getDiscount()))
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ZERO;
    }

    private static String err(String msg) {
        return "{\"error\": \"" + msg + "\"}";
    }

    // ========== 预览数据内部类 ==========

    private static class PreviewData {
        Long userId;
        Long customerId;
        String customerName;
        Long packageId;
        String packageName;
        Integer orderSource;
        Long paymentMethodId;
        String startTime;
        String endTime;
        BigDecimal unitPrice;
        String paymentMethod;
        BigDecimal settleRatio;
        Boolean applyVipDiscount;
        String title;
        int actualMinutes;
        int billedMinutes;
        BigDecimal totalAmount;
        BigDecimal discountAmount;
        BigDecimal finalAmount;
    }
}
