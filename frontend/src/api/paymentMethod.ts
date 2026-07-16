import request from '@/utils/request';

/** 已启用的支付方式列表 */
export function getEnabledPaymentMethods() {
  return request.get('/payment-methods/list-enabled');
}
