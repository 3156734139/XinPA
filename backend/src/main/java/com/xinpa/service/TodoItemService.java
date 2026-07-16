package com.xinpa.service;

import com.xinpa.entity.TodoItem;

import java.util.List;

/**
 * 待办事项服务接口
 */
public interface TodoItemService {

    /**
     * 获取待办列表
     */
    List<TodoItem> listByUserId(Long userId, Integer status);

    /**
     * 创建待办
     */
    void create(TodoItem todoItem);

    /**
     * 切换待办状态
     */
    void toggle(Long id, Long userId);

    /**
     * 删除待办
     */
    void delete(Long id, Long userId);
}
