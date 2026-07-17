import request from '@/utils/request';

/** 分页查询订单 */
export function getOrders(params: any) {
  return request.get('/orders', { params });
}

/** 订单详情 */
export function getOrderDetail(id: number) {
  return request.get(`/orders/${id}`);
}

/** 创建订单 */
export function createOrder(data: any) {
  return request.post('/orders', data);
}

/** 更新订单 */
export function updateOrder(data: any) {
  return request.put('/orders', data);
}

/** 获取预约列表 */
export function getAppointments(params: any) {
  return request.get('/orders/appointments', { params });
}

/** 创建预约 */
export function createAppointment(data: any) {
  return request.post('/orders/appointments', data);
}

/** 更新预约 */
export function updateAppointment(data: any) {
  return request.put('/orders/appointments', data);
}

/** 删除预约 */
export function deleteAppointment(id: number) {
  return request.delete(`/orders/appointments/${id}`);
}

/** 检查预约冲突 */
export function checkConflict(params: any) {
  return request.get('/orders/appointments/conflict', { params });
}
