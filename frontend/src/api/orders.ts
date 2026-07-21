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

