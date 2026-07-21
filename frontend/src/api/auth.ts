import request from '@/utils/request';

export interface LoginForm {
  username: string;
  password: string;
}

export interface RegisterForm {
  username: string;
  password: string;
  nickname?: string;
}

/** 发送短信验证码 */
export function sendSmsCode(phone: string) {
  return request.post('/auth/send-code', { phone });
}

/** 用户登录 */
export function login(data: { phone: string; code?: string; password?: string; loginType: string }) {
  return request.post('/auth/login', data);
}

/** 用户注册（手机号+验证码） */
export function register(data: { phone: string; code: string; nickname?: string; password?: string }) {
  return request.post('/auth/register', data);
}

/** 获取用户信息 */
export function getUserInfo() {
  return request.get('/auth/me');
}

/** 更新用户信息 */
export function updateUserInfo(data: any) {
  return request.put('/auth/me', data);
}

/** 管理员登录 */
export function adminLogin(data: LoginForm) {
  return request.post('/admin/auth/login', data);
}

/** 获取管理员信息 */
export function getAdminInfo() {
  return request.get('/admin/auth/me');
}

/** 刷新 accessToken */
export function refreshToken(refreshToken: string) {
  return request.post('/auth/refresh', { refreshToken });
}

/** 修改密码 */
export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/auth/change-password', data);
}

/** 获取头像 STS 上传凭证 */
export function getAvatarUploadToken() {
  return request.get('/auth/avatar/upload-token');
}

/** 通知后端头像上传完成 */
export function notifyAvatarComplete(objectKey: string) {
  return request.post('/auth/avatar/notify-complete', { objectKey });
}

/** 后端代理上传到 OSS */
export function uploadAvatar(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/auth/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}
