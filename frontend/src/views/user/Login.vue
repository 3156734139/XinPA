<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-orb bg-orb-3"></div>
    </div>

    <div class="login-card">
      <div class="login-card-inner">
        <!-- 品牌 -->
        <div class="login-top">
          <div class="brand-icon heartbeat">♡</div>
          <h2 class="title">陪玩星助手</h2>
          <p class="subtitle">陪玩个人助理工作台</p>
        </div>

        <!-- 登录方式切换 -->
        <div class="login-tabs">
          <span
            v-for="tab in tabs"
            :key="tab.key"
            class="tab-item"
            :class="{ active: activeTab === tab.key }"
            @click="switchTab(tab.key)"
          >{{ tab.label }}</span>
        </div>

        <!-- 错误提示 -->
        <transition name="error-fade">
          <div v-if="errorMsg" class="error-bar" :class="{ 'shake-active': shaking }">
            <span>{{ errorMsg }}</span>
          </div>
        </transition>

        <!-- 表单区域 -->
        <!-- 密码登录 -->
        <el-form
          v-show="activeTab === 'password'"
          ref="pwdFormRef"
          :model="pwdForm"
          :rules="pwdRules"
          size="large"
          @submit.prevent="handlePasswordLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="pwdForm.username"
              placeholder="用户名"
              :prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="pwdForm.password"
              placeholder="密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <div class="form-extra">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <span class="forgot-link" @click="handleForgot">忘记密码？</span>
          </div>
          <el-form-item>
            <el-button size="large" type="primary" :loading="loading" @click="handlePasswordLogin">
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 验证码登录 -->
        <el-form
          v-show="activeTab === 'sms'"
          ref="smsFormRef"
          :model="smsForm"
          :rules="smsRules"
          size="large"
          @submit.prevent="handleSmsLogin"
        >
          <el-form-item prop="phone">
            <el-input
              v-model="smsForm.phone"
              placeholder="手机号"
              :prefix-icon="Iphone"
              maxlength="11"
            />
          </el-form-item>
          <el-form-item prop="code" class="code-item">
            <el-input
              v-model="smsForm.code"
              placeholder="验证码"
              :prefix-icon="Key"
              maxlength="6"
              class="code-input"
            />
            <el-button
              class="code-btn"
              :disabled="codeSending || codeCountdown > 0"
              @click="handleSendCode"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
            </el-button>
          </el-form-item>
          <el-form-item>
            <el-button size="large" type="primary" :loading="loading" @click="handleSmsLogin">
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 扫码登录 -->
        <div v-show="activeTab === 'qrcode'" class="qr-section">
          <div class="qr-placeholder">
            <div class="qr-icon">
              <el-icon :size="48"><Monitor /></el-icon>
            </div>
            <p class="qr-hint">扫码登录</p>
            <p class="qr-subhint">使用陪玩星助手 App 扫码</p>
            <div class="qr-box">
              <canvas ref="qrCanvasRef" width="160" height="160"></canvas>
            </div>
            <el-button size="small" class="qr-refresh-btn" @click="refreshQrCode">
              刷新二维码
            </el-button>
          </div>
        </div>

        <!-- 社交登录 -->
        <div class="social-section">
          <div class="social-divider">
            <span>其他登录方式</span>
          </div>
          <div class="social-icons">
            <div class="social-icon wechat" @click="handleSocial('微信')">
              <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M8.691 2.188C3.891 2.188 0 5.476 0 9.53c0 2.212 1.17 4.203 3.002 5.55a.59.59 0 0 1 .213.665l-.39 1.48c-.019.07-.048.141-.048.213 0 .163.13.295.29.295a.326.326 0 0 0 .167-.054l1.903-1.114a.864.864 0 0 1 .717-.098 10.16 10.16 0 0 0 2.837.403c.276 0 .543-.027.811-.05-.857-2.578.157-4.972 1.932-6.446 1.703-1.415 3.882-1.98 5.853-1.838-.576-3.583-4.196-6.348-8.596-6.348zM5.785 5.991c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178A1.17 1.17 0 0 1 4.623 7.17c0-.651.52-1.18 1.162-1.18zm5.813 0c.642 0 1.162.529 1.162 1.18a1.17 1.17 0 0 1-1.162 1.178 1.17 1.17 0 0 1-1.162-1.178c0-.651.52-1.18 1.162-1.18zm5.34 2.867c-1.797-.052-3.746.512-5.28 1.786-1.72 1.428-2.687 3.72-1.78 6.22.942 2.453 3.666 4.229 6.884 4.229.826 0 1.622-.12 2.361-.336a.722.722 0 0 1 .598.082l1.584.926a.272.272 0 0 0 .14.045c.134 0 .24-.11.24-.245 0-.06-.024-.12-.04-.178l-.325-1.233a.59.59 0 0 1-.178-.553 4.992 4.992 0 0 0 1.723-3.787c0-2.732-2.38-4.955-5.928-4.955zm-2.394 3.08c.535 0 .969.44.969.982a.976.976 0 0 1-.97.982.976.976 0 0 1-.969-.982c0-.542.434-.982.97-.982zm4.844 0c.535 0 .969.44.969.982a.976.976 0 0 1-.97.982.976.976 0 0 1-.968-.982c0-.542.434-.982.97-.982z"/></svg>
            </div>
            <div class="social-icon qq" @click="handleSocial('QQ')">
              <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M12 2c-4.97 0-9 3.185-9 7.115 0 2.19.967 4.146 2.5 5.513-.17.838-.5 2.002-.5 2.002s2.444-.562 3.5-1.046c.818.27 1.72.43 2.657.466.208.037.2.255.008.37-.966.57-2.14 1.053-3.165 1.58-.19.095-.08.36.14.36h7.72c.22 0 .33-.265.14-.36-1.025-.527-2.199-1.01-3.165-1.58-.192-.115-.2-.333.008-.37.937-.036 1.84-.196 2.657-.467 1.056.484 3.5 1.047 3.5 1.047s-.33-1.164-.5-2.002C20.033 13.261 21 11.305 21 9.115 21 5.185 16.97 2 12 2z"/></svg>
            </div>
            <div class="social-icon github" @click="handleSocial('GitHub')">
              <svg viewBox="0 0 24 24" fill="currentColor" width="22" height="22"><path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0 1 12 6.844a9.59 9.59 0 0 1 2.504.337c1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.02 10.02 0 0 0 22 12.017C22 6.484 17.522 2 12 2z"/></svg>
            </div>
          </div>
        </div>

        <!-- 底部链接 -->
        <div class="links">
          <router-link to="/register">注册账号</router-link>
          <router-link to="/admin/login">管理员入口</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { login } from '@/api/auth';
import { User, Lock, Iphone, Key, Monitor } from '@element-plus/icons-vue';
import type { FormInstance } from 'element-plus';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();

// ===== 标签页 =====
const tabs = [
  { key: 'password', label: '密码登录' },
  { key: 'sms', label: '验证码登录' },
  { key: 'qrcode', label: '扫码登录' },
];
const activeTab = ref('password');

// ===== 密码登录 =====
const pwdFormRef = ref<FormInstance>();
const pwdForm = reactive({ username: '', password: '' });
const pwdRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

// ===== 验证码登录 =====
const smsFormRef = ref<FormInstance>();
const smsForm = reactive({ phone: '', code: '' });
const smsRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 6, message: '验证码长度不正确', trigger: 'blur' },
  ],
};

// ===== 公共状态 =====
const loading = ref(false);
const rememberMe = ref(false);
const errorMsg = ref('');
const shaking = ref(false);
const codeSending = ref(false);
const codeCountdown = ref(0);
let countdownTimer: ReturnType<typeof setInterval> | null = null;
const qrCanvasRef = ref<HTMLCanvasElement | null>(null);

// ===== 初始化 =====
onMounted(() => {
  const saved = localStorage.getItem('remembered_user');
  if (saved) {
    pwdForm.username = saved;
    rememberMe.value = true;
  }
});

// ===== 切换标签 =====
function switchTab(key: string) {
  activeTab.value = key;
  errorMsg.value = '';
  if (key === 'qrcode') {
    setTimeout(() => drawQrCode(), 100);
  }
}

// ===== 显示错误 =====
function showError(msg: string) {
  errorMsg.value = msg;
  shaking.value = true;
  setTimeout(() => { shaking.value = false; }, 500);
}

// ===== 密码登录 =====
async function handlePasswordLogin() {
  const valid = await pwdFormRef.value?.validate().catch(() => false);
  if (!valid) return;
  loading.value = true;
  errorMsg.value = '';
  try {
    const res: any = await login(pwdForm);
    if (res.code !== 200) {
      showError(res.message || '登录失败，请检查账号密码');
      return;
    }
    userStore.setToken(res.data.token);
    userStore.setUserType('USER');
    userStore.setUserInfo({ nickname: res.data.nickname });
    if (rememberMe.value) {
      localStorage.setItem('remembered_user', pwdForm.username);
    } else {
      localStorage.removeItem('remembered_user');
    }
    router.push('/dashboard');
  } catch (e: any) {
    showError(e?.response?.data?.message || '登录失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
}

// ===== 验证码登录 =====
async function handleSmsLogin() {
  const valid = await smsFormRef.value?.validate().catch(() => false);
  if (!valid) return;
  showError('验证码登录功能暂未开放，请使用密码登录');
}

async function handleSendCode() {
  const phoneValid = smsForm.phone.length === 11;
  if (!phoneValid) {
    showError('请输入正确的手机号');
    return;
  }
  codeSending.value = true;
  try {
    // 模拟发送验证码
    await new Promise(resolve => setTimeout(resolve, 800));
    ElMessage.success('验证码已发送');
    codeCountdown.value = 60;
    countdownTimer = setInterval(() => {
      codeCountdown.value--;
      if (codeCountdown.value <= 0) {
        if (countdownTimer) clearInterval(countdownTimer);
      }
    }, 1000);
  } finally {
    codeSending.value = false;
  }
}

// ===== 扫码登录 =====
function drawQrCode() {
  const canvas = qrCanvasRef.value;
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  if (!ctx) return;
  const size = 160;
  ctx.clearRect(0, 0, size, size);
  // 绘制二维码样式的装饰图案
  ctx.fillStyle = '#5D4E6D';
  const cell = 8;
  // 绘制随机点阵模拟二维码
  for (let y = 0; y < 20; y++) {
    for (let x = 0; x < 20; x++) {
      if (Math.random() > 0.55) {
        ctx.fillRect(x * cell, y * cell, cell - 1, cell - 1);
      }
    }
  }
  // 定位图案
  ctx.fillStyle = '#5D4E6D';
  // 左上
  ctx.fillRect(0, 0, 5 * cell, 5 * cell);
  ctx.fillStyle = '#FFF';
  ctx.fillRect(cell, cell, 3 * cell, 3 * cell);
  ctx.fillStyle = '#5D4E6D';
  ctx.fillRect(2 * cell, 2 * cell, cell, cell);
  // 右上
  ctx.fillStyle = '#5D4E6D';
  ctx.fillRect(15 * cell, 0, 5 * cell, 5 * cell);
  ctx.fillStyle = '#FFF';
  ctx.fillRect(16 * cell, cell, 3 * cell, 3 * cell);
  ctx.fillStyle = '#5D4E6D';
  ctx.fillRect(17 * cell, 2 * cell, cell, cell);
  // 左下
  ctx.fillStyle = '#5D4E6D';
  ctx.fillRect(0, 15 * cell, 5 * cell, 5 * cell);
  ctx.fillStyle = '#FFF';
  ctx.fillRect(cell, 16 * cell, 3 * cell, 3 * cell);
  ctx.fillStyle = '#5D4E6D';
  ctx.fillRect(2 * cell, 17 * cell, cell, cell);
  // 中心logo
  ctx.fillStyle = '#E8789A';
  ctx.beginPath();
  ctx.arc(size / 2, size / 2, 10, 0, Math.PI * 2);
  ctx.fill();
  ctx.fillStyle = '#FFF';
  ctx.font = '14px serif';
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText('♡', size / 2, size / 2);
}

function refreshQrCode() {
  drawQrCode();
}

// ===== 忘记密码 =====
function handleForgot() {
  ElMessage.info('请联系管理员重置密码');
}

// ===== 社交登录 =====
function handleSocial(platform: string) {
  ElMessage.info(`${platform}登录功能开发中`);
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer);
});
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

/* 登录卡片 + 入场动画 */
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
  animation: cardIn 0.6s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes cardIn {
  0% {
    opacity: 0;
    transform: translateY(30px) scale(0.96);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.login-card-inner {
  background: rgba(255, 255, 255, 0.85);
  border-radius: 22px;
  padding: 40px 40px 28px;
}

.login-top {
  text-align: center;
  margin-bottom: 28px;
}

.brand-icon {
  font-size: 40px;
  color: #E8789A;
  margin-bottom: 10px;
  display: inline-block;
}

.heartbeat {
  animation: heartbeat 2s ease-in-out infinite;
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  15% { transform: scale(1.15); }
  30% { transform: scale(1); }
  45% { transform: scale(1.08); }
  60% { transform: scale(1); }
}

.title {
  color: #5D4E6D;
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 6px;
  letter-spacing: 1px;
}

.subtitle {
  color: #A890B0;
  font-size: 14px;
  margin: 0;
}

/* ===== 标签切换 ===== */
.login-tabs {
  display: flex;
  border-bottom: 1px solid rgba(232, 130, 154, 0.12);
  margin-bottom: 24px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 10px 0 12px;
  cursor: pointer;
  color: #A890B0;
  font-size: 14px;
  font-weight: 500;
  position: relative;
  transition: color 0.25s;
  user-select: none;
}

.tab-item::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 50%;
  width: 0;
  height: 2px;
  background: #E8789A;
  border-radius: 2px;
  transition: all 0.25s;
  transform: translateX(-50%);
}

.tab-item.active {
  color: #5D4E6D;
  font-weight: 600;
}

.tab-item.active::after {
  width: 40px;
}

.tab-item:hover:not(.active) {
  color: #E8789A;
}

/* ===== 错误提示 ===== */
.error-bar {
  background: rgba(245, 108, 108, 0.08);
  border: 1px solid rgba(245, 108, 108, 0.2);
  border-radius: 10px;
  padding: 8px 14px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #E64A4A;
  text-align: center;
}

.error-fade-enter-active,
.error-fade-leave-active {
  transition: all 0.3s ease;
}
.error-fade-enter-from,
.error-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.shake-active {
  animation: shakeX 0.4s ease;
}

@keyframes shakeX {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-8px); }
  40% { transform: translateX(8px); }
  60% { transform: translateX(-5px); }
  80% { transform: translateX(5px); }
}

/* ===== 表单 ===== */
.form-extra {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: -8px 0 18px;
}

.forgot-link {
  font-size: 13px;
  color: #A890B0;
  cursor: pointer;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #E8789A;
}

/* ===== 验证码 ===== */
.code-item {
  display: flex;
  align-items: flex-start;
}

.code-input {
  flex: 1;
}

.code-btn {
  flex-shrink: 0;
  width: 110px;
  margin-left: 10px;
  height: 40px;
  border-radius: 10px;
  color: #E8789A;
  border-color: rgba(232, 130, 154, 0.25);
}

.code-btn:hover {
  border-color: #E8789A;
  color: #E8789A;
  background: rgba(232, 130, 154, 0.05);
}

.code-btn:disabled {
  color: #C4B0CC;
  border-color: rgba(196, 176, 204, 0.3);
}

/* ===== 扫码 ===== */
.qr-section {
  text-align: center;
  padding: 8px 0 16px;
}

.qr-icon {
  color: #E8789A;
  margin-bottom: 6px;
}

.qr-hint {
  color: #5D4E6D;
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px;
}

.qr-subhint {
  color: #A890B0;
  font-size: 13px;
  margin: 0 0 20px;
}

.qr-box {
  display: inline-block;
  padding: 12px;
  background: #FFF;
  border-radius: 12px;
  border: 1px solid rgba(232, 130, 154, 0.12);
  box-shadow: 0 2px 12px rgba(232, 130, 154, 0.06);
}

.qr-box canvas {
  display: block;
}

.qr-refresh-btn {
  margin-top: 16px;
  color: #A890B0;
}

/* ===== 社交登录 ===== */
.social-section {
  margin-top: 20px;
}

.social-divider {
  text-align: center;
  position: relative;
  margin-bottom: 14px;
}

.social-divider::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 100%;
  height: 1px;
  background: rgba(232, 130, 154, 0.1);
}

.social-divider span {
  position: relative;
  background: rgba(255, 255, 255, 0.85);
  padding: 0 12px;
  font-size: 12px;
  color: #C4B0CC;
}

.social-icons {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.social-icon {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s;
  border: 1px solid rgba(232, 130, 154, 0.1);
  color: #A890B0;
}

.social-icon:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.social-icon.wechat:hover {
  color: #07C160;
  border-color: rgba(7, 193, 96, 0.2);
  background: rgba(7, 193, 96, 0.04);
}

.social-icon.qq:hover {
  color: #12B7F5;
  border-color: rgba(18, 183, 245, 0.2);
  background: rgba(18, 183, 245, 0.04);
}

.social-icon.github:hover {
  color: #333;
  border-color: rgba(51, 51, 51, 0.2);
  background: rgba(51, 51, 51, 0.04);
}

/* ===== 底部 ===== */
.links {
  display: flex;
  justify-content: space-between;
  margin-top: 18px;
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

/* ===== 输入框全局样式 ===== */
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

:deep(.el-checkbox__label) {
  color: #A890B0 !important;
  font-size: 13px;
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
