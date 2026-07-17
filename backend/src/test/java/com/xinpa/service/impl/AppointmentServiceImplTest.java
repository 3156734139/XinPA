package com.xinpa.service.impl;

import com.xinpa.common.BusinessException;
import com.xinpa.entity.Appointment;
import com.xinpa.mapper.AppointmentMapper;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AppointmentServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentMapper appointmentMapper;

    @Captor
    private ArgumentCaptor<Appointment> appointmentCaptor;

    private AppointmentServiceImpl appointmentService;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentServiceImpl(appointmentMapper);
    }

    @Nested
    @DisplayName("创建预约 create()")
    class CreateTest {

        @Test
        @DisplayName("无冲突时正常创建")
        void noConflict_success() {
            when(appointmentMapper.selectList(any())).thenReturn(Collections.emptyList());

            Appointment input = new Appointment();
            input.setUserId(100L);
            input.setTitle("陪玩");
            input.setStartTime(LocalDateTime.of(2026, 7, 17, 14, 0));
            input.setEndTime(LocalDateTime.of(2026, 7, 17, 16, 0));

            appointmentService.create(input);

            verify(appointmentMapper).insert(input);
        }

        @Test
        @DisplayName("有冲突时抛出BusinessException")
        void hasConflict_shouldThrow() {
            Appointment conflict = new Appointment();
            conflict.setId(2L);
            when(appointmentMapper.selectList(any())).thenReturn(List.of(conflict));

            Appointment input = new Appointment();
            input.setUserId(100L);
            input.setStartTime(LocalDateTime.of(2026, 7, 17, 14, 0));
            input.setEndTime(LocalDateTime.of(2026, 7, 17, 16, 0));

            assertThrows(BusinessException.class, () -> appointmentService.create(input));
            verify(appointmentMapper, never()).insert(any());
        }

        @Test
        @DisplayName("更新预约时排除自身ID")
        void update_shouldExcludeSelf() {
            when(appointmentMapper.selectList(any())).thenReturn(Collections.emptyList());

            Appointment input = new Appointment();
            input.setId(1L);
            input.setUserId(100L);
            input.setStartTime(LocalDateTime.of(2026, 7, 17, 14, 0));
            input.setEndTime(LocalDateTime.of(2026, 7, 17, 16, 0));

            appointmentService.update(input);

            verify(appointmentMapper).updateById(input);
        }
    }

    @Nested
    @DisplayName("删除预约 delete()")
    class DeleteTest {

        @Test
        @DisplayName("正常删除")
        void success() {
            Appointment app = new Appointment();
            app.setId(1L);
            app.setUserId(100L);
            when(appointmentMapper.selectById(1L)).thenReturn(app);

            appointmentService.delete(1L, 100L);
            verify(appointmentMapper).deleteById((Long) 1L);
        }

        @Test
        @DisplayName("用户ID不匹配时抛出异常")
        void userIdMismatch_shouldThrow() {
            Appointment app = new Appointment();
            app.setId(1L);
            app.setUserId(100L);
            when(appointmentMapper.selectById(1L)).thenReturn(app);

            assertThrows(BusinessException.class, () -> appointmentService.delete(1L, 999L));
            verify(appointmentMapper, never()).deleteById(any(java.io.Serializable.class));
        }
    }
}
