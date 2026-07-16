<template>
  <div class="register-page">
    <div class="login-bg">
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-orb bg-orb-3"></div>
      <span class="deco deco-1">♡</span>
      <span class="deco deco-2">✿</span>
      <span class="deco deco-3">✦</span>
    </div>
    <div class="register-card">
      <div class="register-card-inner">
        <div class="register-top">
          <div class="brand-icon">♡</div>
          <h2 class="title">注册陪玩账号</h2>
          <p class="subtitle">加入陪玩星助手</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" size="large" @submit.prevent="handleRegister">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名（3-64位）" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="nickname">
            <el-input v-model="form.nickname" placeholder="昵称/艺名（选填）" :prefix-icon="Edit" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码（6-64位）" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item>
            <el-button size="large" class="register-btn" :loading="loading" @click="handleRegister">
              注册
            </el-button>
          </el-form-item>
        </el-form>
        <div class="links">
          <router-link to="/login">已有账号？去登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { register } from '@/api/auth';
import { ElMessage } from 'element-plus';
import { User, Lock, Edit } from '@element-plus/icons-vue';
import type { FormInstance } from 'element-plus';

const router = useRouter();
const formRef = ref<FormInstance>();
const loading = ref(false);
const form = reactive({ username: '', password: '', nickname: '' });
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '长度3-64位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 64, message: '长度6-64位', trigger: 'blur' },
  ],
};

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  try {
    await register(form);
    ElMessage.success('注册成功');
    router.push('/login');
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.register-page {
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

.register-card {
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

.register-card-inner {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 22px;
  padding: 48px 40px 36px;
}

.register-top {
  text-align: center;
  margin-bottom: 32px;
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

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  border-radius: 12px;
  background: linear-gradient(135deg, #FFD6E0, #E8DFF5);
  color: #5D4E6D;
  font-weight: 700;
  border: none;
}

.register-btn:hover {
  background: linear-gradient(135deg, #FFC8D6, #E0D0F0) !important;
  color: #5D4E6D !important;
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(232, 130, 154, 0.2);
}

.links {
  text-align: center;
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
  .register-card {
    width: 92%;
  }
  .register-card-inner {
    padding: 32px 24px 24px;
  }
}
</style>
