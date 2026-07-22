import request from '@/utils/request';

/** 已启用的支付方式列表（下拉选单用） */
export function getEnabledPaymentMethods() {
  return request.get('/payment-methods/list-enabled');
}

/** 获取用户的支付方式列表（管理用，含禁用） */
export function getUserPaymentMethods() {
  return request.get('/payment-methods');
}

/** 新增支付方式 */
export function createUserPaymentMethod(data: any) {
  return request.post('/payment-methods', data);
}

/** 更新支付方式 */
export function updateUserPaymentMethod(data: any) {
  return request.put('/payment-methods', data);
}

/** 删除支付方式 */
export function deleteUserPaymentMethod(id: number) {
  return request.delete(`/payment-methods/${id}`);
}
