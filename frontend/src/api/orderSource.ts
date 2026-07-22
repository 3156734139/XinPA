import request from '@/utils/request';

/** 获取用户的来源列表（订单下拉选单用，仅已启用） */
export function getEnabledSources() {
  return request.get('/order-sources/list-enabled');
}

/** 获取用户的来源列表（管理用，含禁用） */
export function getUserSources() {
  return request.get('/order-sources');
}

/** 新增来源 */
export function createUserSource(data: { name: string; sortOrder?: number }) {
  return request.post('/order-sources', data);
}

/** 更新来源 */
export function updateUserSource(data: { id: number; name?: string; sortOrder?: number; status?: number }) {
  return request.put('/order-sources', data);
}

/** 删除来源 */
export function deleteUserSource(id: number) {
  return request.delete(`/order-sources/${id}`);
}
