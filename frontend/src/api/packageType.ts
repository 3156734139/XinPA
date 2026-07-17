import request from '@/utils/request';

/** 获取已启用的套餐类型列表（用户端下拉选单用） */
export function getEnabledPackageTypes() {
  return request.get('/package-types/list-enabled');
}
