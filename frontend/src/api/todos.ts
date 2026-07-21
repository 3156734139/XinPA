import request from '@/utils/request';

/** 待办列表 */
export function getTodos(status?: number) {
  return request.get('/todos', { params: { status } });
}

/** 创建待办 */
export function createTodo(data: any) {
  return request.post('/todos', data);
}

/** 切换待办 */
export function toggleTodo(id: number) {
  return request.post(`/todos/${id}/toggle`);
}

/** 删除待办 */
export function deleteTodo(id: number) {
  return request.delete(`/todos/${id}`);
}
