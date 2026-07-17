import request from '@/utils/request';

/** 工作台统计（今日收入、今日订单、客户总数） */
export function getDashboardStats() {
  return request.get('/dashboard/stats');
}
