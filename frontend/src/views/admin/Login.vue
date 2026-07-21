<template>
  <div class="admin-login-page">
    <div class="bg-layer">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="login-wrapper">
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-logo"><span class="logo-icon">⬡</span></div>
          <h1 class="brand-title">管理后台</h1>
          <p class="brand-desc">陪玩星助手 · 管理员专用</p>
        </div>
      </div>

      <div class="form-section">
        <div class="form-card">
          <div class="form-header">
            <h2 class="form-title">管理员登录</h2>
            <p class="form-subtitle">请输入账号密码</p>
          </div>

          <form @submit.prevent="handleLogin" class="login-form">
            <el-input
              v-model="form.username"
              placeholder="管理员账号"
              size="large"
            />
            <el-input
              v-model="form.password"
              placeholder="密码"
              show-password
              size="large"
            />
            <button type="submit" class="submit-btn" :disabled="loading">
              <span v-if="loading" class="btn-loading"></span>
              <span v-else>管理员登录</span>
            </button>
          </form>

          <div class="form-footer">
            <router-link to="/login" class="footer-link">用户前台入口</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { adminLogin } from '@/api/auth';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const form = reactive({ username: '', password: '' });

async function handleLogin() {
  if (!form.username || !form.password) return;
  loading.value = true;
  try {
    const res: any = await adminLogin(form);
    userStore.setToken(res.data.token);
    if (res.data.refreshToken) userStore.setRefreshToken(res.data.refreshToken);
    userStore.setUserType('ADMIN');
    router.push('/admin/dashboard');
  } finally { loading.value = false; }
}
</script>

<style scoped>
.admin-login-page {
  height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  background: #0F0A12;
}

.bg-layer {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
}

.bg-shape-1 {
  width: 600px; height: 600px;
  background: radial-gradient(circle, rgba(160, 120, 200, 0.15), transparent);
  top: -200px; right: -100px;
}

.bg-shape-2 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, rgba(120, 80, 180, 0.12), transparent);
  bottom: -150px; left: -100px;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(160, 120, 200, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(160, 120, 200, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
}

.login-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  width: 100%;
  height: 100%;
}

.brand-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.brand-content { text-align: center; }

.logo-icon {
  font-size: 56px;
  color: #B088D0;
  display: inline-block;
  filter: drop-shadow(0 0 30px rgba(160, 120, 200, 0.3));
}

.brand-title {
  color: #fff;
  font-size: 36px;
  font-weight: 700;
  margin: 16px 0 8px;
  letter-spacing: 2px;
}

.brand-desc {
  color: rgba(255, 255, 255, 0.5);
  font-size: 15px;
  margin: 0;
}

.form-section {
  width: 480px;
  display: flex;
  align-items: center;
  padding: 40px 60px 40px 0;
}

.form-card {
  width: 100%;
  background: rgba(255, 255, 255, 0.04);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.06);
  padding: 36px;
}

.form-header { margin-bottom: 28px; }

.form-title {
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 4px;
}

.form-subtitle {
  color: rgba(255, 255, 255, 0.35);
  font-size: 14px;
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.06) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08) inset !important;
  border-radius: 12px !important;
  padding: 0 14px !important;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(160, 120, 200, 0.3) inset !important;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px #B088D0 inset !important;
}

.login-form :deep(.el-input__inner) {
  color: #fff !important;
  height: 44px !important;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.2) !important;
}

.submit-btn {
  width: 100%;
  height: 46px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #B088D0, #9068B0);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: inherit;
  letter-spacing: 0.5px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 24px rgba(160, 120, 200, 0.25);
}

.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.btn-loading {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.form-footer { text-align: center; margin-top: 20px; }

.footer-link {
  color: rgba(255, 255, 255, 0.25);
  font-size: 13px;
  text-decoration: none;
  transition: color 0.2s;
  font-weight: 500;
}

.footer-link:hover { color: #B088D0; }

@media (max-width: 900px) {
  .brand-section { display: none; }
  .form-section { width: 100%; padding: 20px; justify-content: center; }
  .form-card { max-width: 420px; }
}
</style>
