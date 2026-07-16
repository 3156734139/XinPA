import request from '@/utils/request';

/** 待办列表 */
export function getTodos(status?: number) {
  return request.get('/tools/todos', { params: { status } });
}

/** 创建待办 */
export function createTodo(data: any) {
  return request.post('/tools/todos', data);
}

/** 切换待办 */
export function toggleTodo(id: number) {
  return request.post(`/tools/todos/${id}/toggle`);
}

/** 删除待办 */
export function deleteTodo(id: number) {
  return request.delete(`/tools/todos/${id}`);
}

/** 获取公告 */
export function getAnnouncements() {
  return request.get('/announcements');
}
