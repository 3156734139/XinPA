import request from '@/utils/request';

/** AI配额查询 */
export function getAiQuota() {
  return request.get('/ai/quota');
}

/** 生成简介 */
export function generateIntro(params: any) {
  return request.post('/ai/generate-intro', params);
}

/** 生成开场白 */
export function generateOpening(params: any) {
  return request.post('/ai/generate-opening', params);
}

/** 生成安抚话术 */
export function generateComfort(params: any) {
  return request.post('/ai/generate-comfort', params);
}

/** 生成砍价应对 */
export function generateBargain(params: any) {
  return request.post('/ai/generate-bargain', params);
}

/** 生成活动方案 */
export function generateActivity(params: any) {
  return request.post('/ai/generate-activity', params);
}

/** 风险识别 */
export function riskDetection(content: string) {
  return request.post('/ai/risk-detection', { content });
}
