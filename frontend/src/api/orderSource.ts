import request from '@/utils/request';

/** 获取已启用的来源列表（用户端下拉选单用） */
export function getEnabledSources() {
  return request.get('/order-sources/list-enabled');
}
