import request from '@/utils/request';

/** 平台概览 */
export function getOverview() {
  return request.get('/admin/stats/overview');
}

/** 用户增长 */
export function getUserGrowth() {
  return request.get('/admin/stats/user-growth');
}

/** 模板占比 */
export function getTemplateRatio() {
  return request.get('/admin/stats/template-ratio');
}

/** 套餐占比 */
export function getPackageRatio() {
  return request.get('/admin/stats/package-ratio');
}

/** 管理员列表 */
export function getAdmins() {
  return request.get('/admin/system/admins');
}

/** 新增管理员 */
export function createAdmin(data: any) {
  return request.post('/admin/system/admins', data);
}

/** 更新管理员 */
export function updateAdmin(data: any) {
  return request.put('/admin/system/admins', data);
}

/** 切换管理员状态 */
export function toggleAdminStatus(id: number, status: number) {
  return request.put(`/admin/system/admins/${id}/status?status=${status}`);
}

/** AI调用日志 */
export function getAiLogs(params: any) {
  return request.get('/admin/system/ai-logs', { params });
}

/** 公告管理列表 */
export function getAdminAnnouncements() {
  return request.get('/admin/system/announcements');
}

/** 发布公告 */
export function createAnnouncement(data: any) {
  return request.post('/admin/system/announcements', data);
}

/** 公告上下架 */
export function toggleAnnouncement(id: number, status: number) {
  return request.put(`/admin/system/announcements/${id}/status?status=${status}`);
}

/** 删除公告 */
export function deleteAnnouncement(id: number) {
  return request.delete(`/admin/system/announcements/${id}`);
}
