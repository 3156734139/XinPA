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

/** 开始计时 */
export function startTimer(orderId: number) {
  return request.post('/orders/timer/start', { orderId });
}

/** 暂停计时 */
export function pauseTimer(orderId: number) {
  return request.post('/orders/timer/pause', { orderId });
}

/** 结束计时 */
export function endTimer(orderId: number) {
  return request.post('/orders/timer/end', { orderId });
}

/** 补时 */
export function addExtraMinutes(orderId: number, extraMinutes: number) {
  return request.post('/orders/timer/extra', { orderId, extraMinutes });
}

/** 结算 */
export function settleOrder(id: number) {
  return request.post(`/orders/${id}/settle`);
}

/** 退款 */
export function refundOrder(id: number) {
  return request.post(`/orders/${id}/refund`);
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
