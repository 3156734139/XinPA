import axios from 'axios';
import { ElMessage } from 'element-plus';

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
});

// 是否正在刷新 token
let isRefreshing = false;
// 等待刷新 token 的请求队列
let refreshSubscribers: ((token: string) => void)[] = [];

function onRefreshed(token: string) {
  refreshSubscribers.forEach(cb => cb(token));
  refreshSubscribers = [];
}

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data;
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败');
      return Promise.reject(new Error(res.message));
    }
    return res;
  },
  async (error) => {
    const originalRequest = error.config;

    // 401 且不是刷新接口本身 → 尝试刷新 token
    if (error.response?.status === 401
        && !originalRequest._retry
        && !originalRequest.url?.includes('/auth/refresh')) {

      const refreshToken = localStorage.getItem('refreshToken');
      if (!refreshToken) {
        // 没有 refreshToken，直接踢到登录页
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userInfo');
        window.location.href = '/login';
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // 正在刷新中，把请求排队
        return new Promise((resolve) => {
          refreshSubscribers.push((newToken: string) => {
            originalRequest.headers.Authorization = `Bearer ${newToken}`;
            resolve(request(originalRequest));
          });
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const res = await axios.post('/api/auth/refresh', { refreshToken });
        const newToken = res.data.data?.token;
        if (newToken) {
          localStorage.setItem('token', newToken);
          onRefreshed(newToken);
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return request(originalRequest);
        }
      } catch {
        // 刷新失败，清除登录态
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userInfo');
        window.location.href = '/login';
      } finally {
        isRefreshing = false;
      }
      return Promise.reject(error);
    }

    // 其他错误
    ElMessage.error(error.response?.data?.message || '网络错误');
    return Promise.reject(error);
  }
);

export default request;
