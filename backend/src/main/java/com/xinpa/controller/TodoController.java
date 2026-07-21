package com.xinpa.controller;

import com.xinpa.common.Result;
import com.xinpa.common.UserContext;
import com.xinpa.entity.TodoItem;
import com.xinpa.service.TodoItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 待办看板接口
 */
@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoItemService todoItemService;

    // ==================== 待办看板 ====================

    /**
     * 待办列表
     */
    @GetMapping
    public Result<?> todos(@RequestParam(required = false) Integer status) {
        return Result.ok(todoItemService.listByUserId(UserContext.getUserId(), status));
    }

    /**
     * 创建待办
     */
    @PostMapping
    public Result<Void> createTodo(@RequestBody TodoItem todoItem) {
        todoItem.setUserId(UserContext.getUserId());
        todoItemService.create(todoItem);
        return Result.ok();
    }

    /**
     * 切换待办状态
     */
    @PostMapping("/{id}/toggle")
    public Result<Void> toggleTodo(@PathVariable Long id) {
        todoItemService.toggle(id, UserContext.getUserId());
        return Result.ok();
    }

    /**
     * 删除待办
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTodo(@PathVariable Long id) {
        todoItemService.delete(id, UserContext.getUserId());
        return Result.ok();
    }
}
