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

// ==================== 来源管理 ====================

/** 来源列表 */
export function getOrderSources() {
  return request.get('/admin/system/order-sources');
}

/** 新增来源 */
export function createOrderSource(data: any) {
  return request.post('/admin/system/order-sources', data);
}

/** 更新来源 */
export function updateOrderSource(data: any) {
  return request.put('/admin/system/order-sources', data);
}

/** 删除来源 */
export function deleteOrderSource(id: number) {
  return request.delete(`/admin/system/order-sources/${id}`);
}

/** 切换来源状态 */
export function toggleOrderSourceStatus(id: number, status: number) {
  return request.put(`/admin/system/order-sources/${id}/status?status=${status}`);
}

// ==================== 支付方式管理 ====================

/** 支付方式列表 */
export function getPaymentMethods() {
  return request.get('/admin/system/payment-methods');
}

/** 新增支付方式 */
export function createPaymentMethod(data: any) {
  return request.post('/admin/system/payment-methods', data);
}

/** 更新支付方式 */
export function updatePaymentMethod(data: any) {
  return request.put('/admin/system/payment-methods', data);
}

/** 删除支付方式 */
export function deletePaymentMethod(id: number) {
  return request.delete(`/admin/system/payment-methods/${id}`);
}

/** 切换支付方式状态 */
export function togglePaymentMethodStatus(id: number, status: number) {
  return request.put(`/admin/system/payment-methods/${id}/status?status=${status}`);
}
