import request from '@/utils/request';

/** 获取已启用的套餐类型列表（下拉选单用） */
export function getEnabledPackageTypes() {
  return request.get('/package-types/list-enabled');
}

/** 获取用户的套餐类型列表（管理用，含禁用） */
export function getUserPackageTypes() {
  return request.get('/package-types');
}

/** 新增套餐类型 */
export function createUserPackageType(data: any) {
  return request.post('/package-types', data);
}

/** 更新套餐类型 */
export function updateUserPackageType(data: any) {
  return request.put('/package-types', data);
}

/** 删除套餐类型 */
export function deleteUserPackageType(id: number) {
  return request.delete(`/package-types/${id}`);
}
