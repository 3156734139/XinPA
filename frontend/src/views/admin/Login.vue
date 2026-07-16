<template>
  <div class="admin-login-page">
    <div class="login-bg">
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-orb bg-orb-3"></div>
    </div>
    <div class="admin-login-card">
      <div class="login-card-inner">
        <div class="login-top">
          <div class="brand-icon">⬡</div>
          <h2 class="title">管理后台</h2>
          <p class="subtitle">陪玩星助手 · 管理员专用</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="管理员账号"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button size="large" class="login-btn" :loading="loading" @click="handleLogin">
              管理员登录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="links">
          <router-link to="/login">用户前台入口</router-link>
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
import { User, Lock } from '@element-plus/icons-vue';
import type { FormInstance } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const formRef = ref<FormInstance>();
const loading = ref(false);
const form = reactive({ username: '', password: '' });
const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    const res: any = await adminLogin(form);
    userStore.setToken(res.data.token);
    userStore.setUserType('ADMIN');
    router.push('/admin/dashboard');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.admin-login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #F5F0F7 0%, #F8F0FF 30%, #F0E8FF 70%, #F5F0F7 100%);
}

/* 背景装饰 */
.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.25;
}

.bg-orb-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #E8DFF5, transparent);
  top: -10%;
  right: -5%;
}

.bg-orb-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #D5C8E8, transparent);
  bottom: -10%;
  left: -5%;
}

.bg-orb-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #F0E8FF, transparent);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0.15;
}

/* 登录卡片 */
.admin-login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 24px;
  border: 1px solid rgba(180, 150, 210, 0.15);
  padding: 3px;
  box-shadow: 0 8px 40px rgba(180, 150, 210, 0.1);
}

.login-card-inner {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 22px;
  padding: 48px 40px 36px;
}

.login-top {
  text-align: center;
  margin-bottom: 36px;
}

.brand-icon {
  font-size: 36px;
  color: #A07FC0;
  margin-bottom: 12px;
}

.title {
  color: #5D4E6D;
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: 1px;
}

.subtitle {
  color: #A890B0;
  font-size: 14px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  border-radius: 12px;
  background: linear-gradient(135deg, #E8DFF5, #D5C8E8);
  color: #5D4E6D;
  font-weight: 700;
  border: none;
}

.login-btn:hover {
  background: linear-gradient(135deg, #E0D5F0, #CBB8E0) !important;
  color: #5D4E6D !important;
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(160, 127, 192, 0.2);
}

.links {
  text-align: center;
  margin-top: 20px;
}

.links a {
  color: #A890B0;
  text-decoration: none;
  font-size: 13px;
  transition: color 0.2s;
  font-weight: 500;
}

.links a:hover {
  color: #A07FC0;
}

:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8) !important;
  box-shadow: 0 0 0 1px rgba(180, 150, 210, 0.15) inset !important;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(180, 150, 210, 0.3) inset !important;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #A07FC0 inset !important;
}

:deep(.el-input__inner) {
  color: #5D4E6D !important;
}

:deep(.el-input__inner::placeholder) {
  color: #C4B0CC !important;
}

:deep(.el-input__prefix-inner) {
  color: #C4B0CC !important;
}

@media (max-width: 480px) {
  .admin-login-card {
    width: 92%;
  }
  .login-card-inner {
    padding: 32px 24px 24px;
  }
}
</style>
