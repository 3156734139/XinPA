import request from '@/utils/request';

/** 获取用户的VIP等级列表（下拉选单用，仅已启用） */
export function getEnabledVipLevels() {
  return request.get('/vip-levels/list-enabled');
}

/** 获取用户的VIP等级列表（管理用，含禁用） */
export function getUserVipLevels() {
  return request.get('/vip-levels');
}

/** 新增VIP等级 */
export function createUserVipLevel(data: any) {
  return request.post('/vip-levels', data);
}

/** 更新VIP等级 */
export function updateUserVipLevel(data: any) {
  return request.put('/vip-levels', data);
}

/** 删除VIP等级 */
export function deleteUserVipLevel(id: number) {
  return request.delete(`/vip-levels/${id}`);
}
