import request from '@/utils/request';

/** 分页查询流水 */
export function getFinanceRecords(params: any) {
  return request.get('/finance/records', { params });
}

/** 新增流水 */
export function createFinanceRecord(data: any) {
  return request.post('/finance/records', data);
}

/** 删除流水 */
export function deleteFinanceRecord(id: number) {
  return request.delete(`/finance/records/${id}`);
}

/** 收益统计 */
export function getFinanceStats() {
  return request.get('/finance/stats');
}

/** 收支趋势（支持日/周/月聚合） */
export function getFinanceTrend(mode?: string, start?: string, end?: string) {
  return request.get('/finance/trend', { params: { mode, start, end } });
}

/** 获取财务设置 */
export function getFinanceSetting() {
  return request.get('/finance/setting');
}

/** 更新财务设置 */
export function updateFinanceSetting(data: any) {
  return request.put('/finance/setting', data);
}

