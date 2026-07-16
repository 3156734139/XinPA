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

/** 用户登录 */
export function login(data: LoginForm) {
  return request.post('/auth/login', data);
}

/** 用户注册 */
export function register(data: RegisterForm) {
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

/** 修改密码 */
export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put('/auth/change-password', data);
}

/** 上传头像 */
export function uploadAvatar(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return request.post('/auth/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}
