package com.xinpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinpa.common.BusinessException;
import com.xinpa.entity.TodoItem;
import com.xinpa.mapper.TodoItemMapper;
import com.xinpa.service.TodoItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 待办事项服务实现
 */
@Service
@RequiredArgsConstructor
public class TodoItemServiceImpl implements TodoItemService {

    private final TodoItemMapper todoItemMapper;

    @Override
    public List<TodoItem> listByUserId(Long userId, Integer status) {
        return todoItemMapper.selectList(
                new LambdaQueryWrapper<TodoItem>()
                        .eq(TodoItem::getUserId, userId)
                        .eq(TodoItem::getDeleted, 0)
                        .eq(status != null, TodoItem::getStatus, status)
                        .orderByAsc(TodoItem::getDueDate)
                        .orderByAsc(TodoItem::getCreatedAt));
    }

    @Override
    public void create(TodoItem todoItem) {
        if (todoItem.getTodoType() == null) {
            todoItem.setTodoType(3); // 默认自定义
        }
        if (todoItem.getStatus() == null) {
            todoItem.setStatus(0);
        }
        todoItemMapper.insert(todoItem);
    }

    @Override
    public void toggle(Long id, Long userId) {
        TodoItem item = todoItemMapper.selectById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("待办不存在");
        }
        item.setStatus(item.getStatus() == 0 ? 1 : 0);
        todoItemMapper.updateById(item);
    }

    @Override
    public void delete(Long id, Long userId) {
        TodoItem item = todoItemMapper.selectById(id);
        if (item == null || !item.getUserId().equals(userId)) {
            throw new BusinessException("待办不存在");
        }
        todoItemMapper.deleteById(id);
    }
}
