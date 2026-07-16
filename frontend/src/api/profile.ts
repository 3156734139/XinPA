import request from '@/utils/request';

/** 获取主页信息 */
export function getProfile() {
  return request.get('/profile');
}

/** 更新主页信息 */
export function updateProfile(data: any) {
  return request.put('/profile', data);
}

/** 切换接单状态 */
export function switchOrderStatus(status: number) {
  return request.put(`/profile/order-status?status=${status}`);
}

/** 获取套餐列表 */
export function getPackages() {
  return request.get('/profile/packages');
}

/** 添加套餐 */
export function addPackage(data: any) {
  return request.post('/profile/packages', data);
}

/** 更新套餐 */
export function updatePackage(data: any) {
  return request.put('/profile/packages', data);
}

/** 删除套餐 */
export function deletePackage(id: number) {
  return request.delete(`/profile/packages/${id}`);
}

/** 获取素材列表 */
export function getMaterials(type?: number) {
  return request.get('/profile/materials', { params: { type } });
}

/** 上传素材 */
export function uploadMaterial(data: FormData) {
  return request.post('/profile/materials/upload', data, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}

/** 删除素材 */
export function deleteMaterial(id: number) {
  return request.delete(`/profile/materials/${id}`);
}

/** ========== 游戏配置 ========== */

/** 获取游戏配置列表 */
export function getGameConfigs() {
  return request.get('/profile/game-configs');
}

/** 添加游戏配置 */
export function addGameConfig(data: any) {
  return request.post('/profile/game-configs', data);
}

/** 更新游戏配置 */
export function updateGameConfig(data: any) {
  return request.put('/profile/game-configs', data);
}

/** 删除游戏配置 */
export function deleteGameConfig(id: number) {
  return request.delete(`/profile/game-configs/${id}`);
}
