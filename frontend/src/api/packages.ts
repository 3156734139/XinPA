import request from '@/utils/request';

/** 获取套餐列表 */
export function getPackages() {
  return request.get('/packages');
}

/** 添加套餐 */
export function addPackage(data: any) {
  return request.post('/packages', data);
}

/** 更新套餐 */
export function updatePackage(data: any) {
  return request.put('/packages', data);
}

/** 删除套餐 */
export function deletePackage(id: number) {
  return request.delete(`/packages/${id}`);
}
