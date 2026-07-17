package com.xinpa.service.impl;

import com.xinpa.entity.TodoItem;
import com.xinpa.mapper.TodoItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * TodoItemServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class TodoItemServiceImplTest {

    @Mock
    private TodoItemMapper todoItemMapper;

    @Captor
    private ArgumentCaptor<TodoItem> todoCaptor;

    private TodoItemServiceImpl todoItemService;

    @BeforeEach
    void setUp() {
        todoItemService = new TodoItemServiceImpl(todoItemMapper);
    }

    @Nested
    @DisplayName("创建待办 create()")
    class CreateTest {

        @Test
        @DisplayName("未设置类型时默认自定义类型(3)")
        void defaultType() {
            TodoItem input = new TodoItem();
            input.setUserId(100L);
            input.setTitle("测试待办");

            todoItemService.create(input);

            verify(todoItemMapper).insert(todoCaptor.capture());
            assertEquals(3, todoCaptor.getValue().getTodoType().intValue());
            assertEquals(0, todoCaptor.getValue().getStatus().intValue());
        }

        @Test
        @DisplayName("指定类型时不覆盖")
        void specifiedType() {
            TodoItem input = new TodoItem();
            input.setUserId(100L);
            input.setTitle("测试待办");
            input.setTodoType(1);

            todoItemService.create(input);

            verify(todoItemMapper).insert(todoCaptor.capture());
            assertEquals(1, todoCaptor.getValue().getTodoType().intValue());
        }
    }

    @Nested
    @DisplayName("切换状态 toggle()")
    class ToggleTest {

        @Test
        @DisplayName("未完成→已完成")
        void pendingToDone() {
            TodoItem item = new TodoItem();
            item.setId(1L);
            item.setUserId(100L);
            item.setStatus(0);
            when(todoItemMapper.selectById(1L)).thenReturn(item);

            todoItemService.toggle(1L, 100L);

            verify(todoItemMapper).updateById(todoCaptor.capture());
            assertEquals(1, todoCaptor.getValue().getStatus().intValue());
        }

        @Test
        @DisplayName("已完成→未完成")
        void doneToPending() {
            TodoItem item = new TodoItem();
            item.setId(1L);
            item.setUserId(100L);
            item.setStatus(1);
            when(todoItemMapper.selectById(1L)).thenReturn(item);

            todoItemService.toggle(1L, 100L);

            verify(todoItemMapper).updateById(todoCaptor.capture());
            assertEquals(0, todoCaptor.getValue().getStatus().intValue());
        }
    }
}
