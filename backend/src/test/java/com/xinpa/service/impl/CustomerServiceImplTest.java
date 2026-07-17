package com.xinpa.service.impl;

import com.xinpa.common.BusinessException;
import com.xinpa.entity.Customer;
import com.xinpa.entity.VipLevel;
import com.xinpa.mapper.CustomerMapper;
import com.xinpa.mapper.OrderMapper;
import com.xinpa.service.VipLevelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CustomerServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerMapper customerMapper;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private VipLevelService vipLevelService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    private CustomerServiceImpl customerService;
    private Customer existingCustomer;

    private List<VipLevel> defaultVipLevels;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerMapper, orderMapper, vipLevelService);

        existingCustomer = new Customer();
        existingCustomer.setId(1L);
        existingCustomer.setUserId(100L);
        existingCustomer.setNickname("测试客户");
        existingCustomer.setOrderCount(0);
        existingCustomer.setTotalSpend(BigDecimal.ZERO);

        // 默认 VIP 等级配置
        VipLevel v1 = new VipLevel(); v1.setLevel(1); v1.setThreshold(new BigDecimal("500"));
        VipLevel v2 = new VipLevel(); v2.setLevel(2); v2.setThreshold(new BigDecimal("1500"));
        VipLevel v3 = new VipLevel(); v3.setLevel(3); v3.setThreshold(new BigDecimal("6888"));
        VipLevel v4 = new VipLevel(); v4.setLevel(4); v4.setThreshold(new BigDecimal("10000"));
        VipLevel v5 = new VipLevel(); v5.setLevel(5); v5.setThreshold(new BigDecimal("28888"));
        VipLevel v6 = new VipLevel(); v6.setLevel(6); v6.setThreshold(new BigDecimal("50000"));
        defaultVipLevels = Arrays.asList(v1, v2, v3, v4, v5, v6);
    }

    // ==================== create 测试 ====================

    @Nested
    @DisplayName("创建客户 create()")
    class CreateTest {

        @Test
        @DisplayName("创建客户时初始化订单数和消费金额为0")
        void shouldInitOrderCountAndTotalSpend() {
            Customer input = new Customer();
            input.setUserId(100L);
            input.setNickname("新客户");

            customerService.create(input);

            verify(customerMapper).insert(customerCaptor.capture());
            Customer saved = customerCaptor.getValue();
            assertEquals(0, saved.getOrderCount().intValue());
            assertEquals(BigDecimal.ZERO, saved.getTotalSpend());
        }
    }

    // ==================== delete 测试 ====================

    @Nested
    @DisplayName("删除客户 delete()")
    class DeleteTest {

        @Test
        @DisplayName("正常删除")
        void success() {
            when(customerMapper.selectById(1L)).thenReturn(existingCustomer);

            customerService.delete(1L, 100L);

            verify(customerMapper).deleteById(1L);
        }

        @Test
        @DisplayName("客户不存在时抛出异常")
        void notFound_shouldThrow() {
            when(customerMapper.selectById(999L)).thenReturn(null);
            assertThrows(BusinessException.class, () -> customerService.delete(999L, 100L));
        }

        @Test
        @DisplayName("用户ID不匹配时抛出异常（权限校验）")
        void userIdMismatch_shouldThrow() {
            when(customerMapper.selectById(1L)).thenReturn(existingCustomer);
            assertThrows(BusinessException.class, () -> customerService.delete(1L, 999L));
        }
    }

    // ==================== blacklist 测试 ====================

    @Nested
    @DisplayName("黑名单操作")
    class BlacklistTest {

        @Test
        @DisplayName("加入黑名单")
        void add() {
            when(customerMapper.selectById(1L)).thenReturn(existingCustomer);

            customerService.addBlacklist(1L, 100L, "恶意退款");

            verify(customerMapper).updateById(customerCaptor.capture());
            Customer saved = customerCaptor.getValue();
            assertEquals(1, saved.getIsBlacklist().intValue());
            assertEquals("恶意退款", saved.getBlacklistReason());
        }

        @Test
        @DisplayName("移出黑名单")
        void remove() {
            existingCustomer.setIsBlacklist(1);
            when(customerMapper.selectById(1L)).thenReturn(existingCustomer);

            customerService.removeBlacklist(1L, 100L);

            verify(customerMapper).updateById(customerCaptor.capture());
            Customer saved = customerCaptor.getValue();
            assertEquals(0, saved.getIsBlacklist().intValue());
            assertNull(saved.getBlacklistReason());
        }
    }

    // ==================== refreshCustomerStats 测试 ====================

    @Nested
    @DisplayName("刷新客户统计 refreshCustomerStats()")
    class RefreshCustomerStatsTest {

        @Test
        @DisplayName("customerId为空时直接返回")
        void nullCustomerId_noop() {
            customerService.refreshCustomerStats(null);
            verify(customerMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("客户不存在时直接返回")
        void customerNotFound_noop() {
            when(customerMapper.selectById(999L)).thenReturn(null);
            customerService.refreshCustomerStats(999L);
            verify(customerMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("有已完结订单时更新统计数据")
        void withCompletedOrders() {
            when(customerMapper.selectById(1L)).thenReturn(existingCustomer);

            Map<String, Object> stats = new HashMap<>();
            stats.put("order_count", 5L);
            stats.put("total_spend", new BigDecimal("3000"));
            when(orderMapper.selectCustomerStats(1L)).thenReturn(stats);

            when(vipLevelService.listEnabled()).thenReturn(defaultVipLevels);

            customerService.refreshCustomerStats(1L);

            verify(customerMapper).updateById(customerCaptor.capture());
            Customer saved = customerCaptor.getValue();
            assertEquals(5, saved.getOrderCount().intValue());
            assertEquals(0, new BigDecimal("3000").compareTo(saved.getTotalSpend()));
            assertEquals(2, saved.getSpendLevel().intValue()); // VIP2 (>=1500)
            assertNotNull(saved.getLastOrderTime());
        }

        @Test
        @DisplayName("优惠等级计算：低于VIP1门槛")
        void spendLevel_none() {
            when(customerMapper.selectById(1L)).thenReturn(existingCustomer);

            Map<String, Object> stats = new HashMap<>();
            stats.put("order_count", 1L);
            stats.put("total_spend", new BigDecimal("300"));
            when(orderMapper.selectCustomerStats(1L)).thenReturn(stats);

            when(vipLevelService.listEnabled()).thenReturn(defaultVipLevels);

            customerService.refreshCustomerStats(1L);

            verify(customerMapper).updateById(customerCaptor.capture());
            assertEquals(0, customerCaptor.getValue().getSpendLevel().intValue()); // 无等级
        }

        @Test
        @DisplayName("优惠等级计算：VIP1")
        void spendLevel_vip1() {
            when(customerMapper.selectById(1L)).thenReturn(existingCustomer);

            Map<String, Object> stats = new HashMap<>();
            stats.put("order_count", 3L);
            stats.put("total_spend", new BigDecimal("800"));
            when(orderMapper.selectCustomerStats(1L)).thenReturn(stats);

            when(vipLevelService.listEnabled()).thenReturn(defaultVipLevels);

            customerService.refreshCustomerStats(1L);

            verify(customerMapper).updateById(customerCaptor.capture());
            assertEquals(1, customerCaptor.getValue().getSpendLevel().intValue()); // VIP1
        }

        @Test
        @DisplayName("优惠等级计算：VIP6")
        void spendLevel_vip6() {
            when(customerMapper.selectById(1L)).thenReturn(existingCustomer);

            Map<String, Object> stats = new HashMap<>();
            stats.put("order_count", 10L);
            stats.put("total_spend", new BigDecimal("50000"));
            when(orderMapper.selectCustomerStats(1L)).thenReturn(stats);

            when(vipLevelService.listEnabled()).thenReturn(defaultVipLevels);

            customerService.refreshCustomerStats(1L);

            verify(customerMapper).updateById(customerCaptor.capture());
            assertEquals(6, customerCaptor.getValue().getSpendLevel().intValue()); // VIP6
        }

        @Test
        @DisplayName("统计为空时，取零值")
        void statsNull_useZero() {
            when(customerMapper.selectById(1L)).thenReturn(existingCustomer);
            when(orderMapper.selectCustomerStats(1L)).thenReturn(null);

            when(vipLevelService.listEnabled()).thenReturn(defaultVipLevels);

            customerService.refreshCustomerStats(1L);

            verify(customerMapper).updateById(customerCaptor.capture());
            Customer saved = customerCaptor.getValue();
            assertEquals(0, saved.getOrderCount().intValue());
            assertEquals(BigDecimal.ZERO, saved.getTotalSpend());
            assertEquals(0, saved.getSpendLevel().intValue()); // 无等级
        }
    }
}
