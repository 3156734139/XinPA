<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-orb bg-orb-3"></div>
      <span class="deco deco-1">♡</span>
      <span class="deco deco-2">✿</span>
      <span class="deco deco-3">✦</span>
    </div>
    <div class="login-card">
      <div class="login-card-inner">
        <div class="login-top">
          <div class="brand-icon">♡</div>
          <h2 class="title">陪玩星助手</h2>
          <p class="subtitle">陪玩个人助理工作台</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
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
              登录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="links">
          <router-link to="/register">注册账号</router-link>
          <router-link to="/admin/login">管理员入口</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { login } from '@/api/auth';
import { User, Lock } from '@element-plus/icons-vue';
import type { FormInstance } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const formRef = ref<FormInstance>();
const loading = ref(false);
const form = reactive({ username: '', password: '' });
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    const res: any = await login(form);
    userStore.setToken(res.data.token);
    userStore.setUserType('USER');
    userStore.setUserInfo({ nickname: res.data.nickname });
    router.push('/dashboard');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #FFF9FB 0%, #FFF5F7 30%, #FFF0F5 70%, #FFF9FB 100%);
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
  opacity: 0.3;
}

.bg-orb-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #FFD6E0, transparent);
  top: -10%;
  right: -5%;
}

.bg-orb-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #E8DFF5, transparent);
  bottom: -10%;
  left: -5%;
}

.bg-orb-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, #FFE8EF, transparent);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0.2;
}

/* 装饰图标 */
.deco {
  position: absolute;
  opacity: 0.15;
  font-size: 24px;
  animation: float 6s ease-in-out infinite;
  pointer-events: none;
}

.deco-1 { top: 18%; left: 12%; font-size: 28px; animation-delay: 0s; }
.deco-2 { top: 22%; right: 15%; font-size: 20px; animation-delay: 1.5s; }
.deco-3 { bottom: 25%; left: 18%; font-size: 22px; animation-delay: 3s; }

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-10px) rotate(6deg); }
}

/* 登录卡片 */
.login-card {
  position: relative;
  z-index: 1;
  width: 420px;
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 24px;
  border: 1px solid rgba(232, 130, 154, 0.12);
  padding: 3px;
  box-shadow: 0 8px 40px rgba(232, 130, 154, 0.1);
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
  font-size: 40px;
  color: #E8789A;
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

/* 表单 */
.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  border-radius: 12px;
  background: linear-gradient(135deg, #FFD6E0, #E8DFF5);
  color: #5D4E6D;
  font-weight: 700;
  border: none;
}

.login-btn:hover {
  background: linear-gradient(135deg, #FFC8D6, #E0D0F0) !important;
  color: #5D4E6D !important;
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(232, 130, 154, 0.2);
}

.links {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  font-size: 13px;
}

.links a {
  color: #A890B0;
  text-decoration: none;
  transition: color 0.2s;
  font-weight: 500;
}

.links a:hover {
  color: #E8789A;
}

/* 覆盖输入框样式 */
:deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.8) !important;
  box-shadow: 0 0 0 1px rgba(232, 130, 154, 0.15) inset !important;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(232, 130, 154, 0.3) inset !important;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #E8789A inset !important;
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
  .login-card {
    width: 92%;
  }
  .login-card-inner {
    padding: 32px 24px 24px;
  }
}
</style>
