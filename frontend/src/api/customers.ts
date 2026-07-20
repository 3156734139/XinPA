import request from '@/utils/request';

/** 分页查询客户 */
export function getCustomers(params: any) {
  return request.get('/customers', { params });
}

/** 获取全部客户简要列表（订单下拉选单用） */
export function getCustomerList() {
  return request.get('/customers/list-all');
}

/** 客户详情 */
export function getCustomerDetail(id: number) {
  return request.get(`/customers/${id}`);
}

/** 创建客户 */
export function createCustomer(data: any) {
  return request.post('/customers', data);
}

/** 更新客户 */
export function updateCustomer(data: any) {
  return request.put('/customers', data);
}

/** 删除客户 */
export function deleteCustomer(id: number) {
  return request.delete(`/customers/${id}`);
}

/** 加入黑名单 */
export function addBlacklist(id: number, reason: string) {
  return request.post(`/customers/${id}/blacklist?reason=${encodeURIComponent(reason)}`);
}

/** 移出黑名单 */
export function removeBlacklist(id: number) {
  return request.delete(`/customers/${id}/blacklist`);
}

/** 优惠券列表 */
export function getCoupons(customerId?: number) {
  return request.get('/customers/coupons', { params: { customerId } });
}

/** 发放优惠券 */
export function createCoupon(data: any) {
  return request.post('/customers/coupons', data);
}

/** 作废优惠券 */
export function cancelCoupon(id: number) {
  return request.delete(`/customers/coupons/${id}`);
}

/** 客户消费排行榜 */
export function getSpendingRanking() {
  return request.get('/customers/spending-ranking');
}

/** 获取 VIP 等级配置 */
export function getVipConfigs() {
  return request.get('/customers/vip-configs');
}
