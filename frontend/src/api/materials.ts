import request from '@/utils/request';

/** 获取素材列表（分页） */
export function getMaterials(type?: string, current = 1, size = 5) {
  return request.get('/materials', { params: { type, current, size } });
}

/** 获取 STS 临时上传凭证（前端直传 OSS 用） */
export function getUploadToken(type: string) {
  return request.get('/materials/upload-token', { params: { type } });
}

/** 通知后端素材上传完成 */
export function notifyUploadComplete(data: {
  name: string;
  type: string;
  objectKey: string;
  fileSize: number;
  watermark?: number;
}) {
  return request.post('/materials/notify-complete', data);
}

/** 获取视频播放地址（实时生成 presigned URL） */
export function getVideoUrl(id: number) {
  return request.get(`/materials/${id}/video-url`);
}

/** 删除素材 */
export function deleteMaterial(id: number) {
  return request.delete(`/materials/${id}`);
}
