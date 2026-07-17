package com.xinpa.service.impl;

import com.xinpa.common.BusinessException;
import com.xinpa.entity.FollowUpReminder;
import com.xinpa.mapper.FollowUpReminderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * FollowUpReminderServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class FollowUpReminderServiceImplTest {

    @Mock
    private FollowUpReminderMapper followUpReminderMapper;

    @Captor
    private ArgumentCaptor<FollowUpReminder> reminderCaptor;

    private FollowUpReminderServiceImpl followUpReminderService;

    @BeforeEach
    void setUp() {
        followUpReminderService = new FollowUpReminderServiceImpl(followUpReminderMapper);
    }

    @Nested
    @DisplayName("处理回访 handle()")
    class HandleTest {

        @Test
        @DisplayName("正常处理：状态变为1")
        void success() {
            FollowUpReminder reminder = new FollowUpReminder();
            reminder.setId(1L);
            reminder.setUserId(100L);
            reminder.setStatus(0);
            when(followUpReminderMapper.selectById(1L)).thenReturn(reminder);

            followUpReminderService.handle(1L, 100L);

            verify(followUpReminderMapper).updateById(reminderCaptor.capture());
            assertEquals(1, reminderCaptor.getValue().getStatus().intValue());
        }

        @Test
        @DisplayName("提醒不存在时抛出异常")
        void notFound_shouldThrow() {
            when(followUpReminderMapper.selectById(anyLong())).thenReturn(null);
            assertThrows(BusinessException.class, () -> followUpReminderService.handle(999L, 100L));
        }
    }

    @Nested
    @DisplayName("忽略回访 ignore()")
    class IgnoreTest {

        @Test
        @DisplayName("正常忽略：状态变为2")
        void success() {
            FollowUpReminder reminder = new FollowUpReminder();
            reminder.setId(1L);
            reminder.setUserId(100L);
            reminder.setStatus(0);
            when(followUpReminderMapper.selectById(1L)).thenReturn(reminder);

            followUpReminderService.ignore(1L, 100L);

            verify(followUpReminderMapper).updateById(reminderCaptor.capture());
            assertEquals(2, reminderCaptor.getValue().getStatus().intValue());
        }
    }

    @Nested
    @DisplayName("自动生成回访 autoGenerate()")
    class AutoGenerateTest {

        @Test
        @DisplayName("下单后创建3天回访")
        void shouldCreateThreeDayReminder() {
            followUpReminderService.autoGenerate(100L, 200L, 1L);

            verify(followUpReminderMapper, times(1)).insert(reminderCaptor.capture());

            FollowUpReminder r1 = reminderCaptor.getValue();
            assertEquals(100L, r1.getUserId().longValue());
            assertEquals(200L, r1.getCustomerId().longValue());
            assertEquals(1L, r1.getOrderId().longValue());
            assertEquals(1, r1.getRemindType().intValue());
            assertEquals(0, r1.getStatus().intValue());
            // remindTime应该是3天后
            LocalDateTime expectedR1 = LocalDateTime.now().plusDays(3);
            assertTrue(r1.getRemindTime().isAfter(expectedR1.minusMinutes(1)));
            assertTrue(r1.getRemindTime().isBefore(expectedR1.plusMinutes(1)));
        }
    }
}
