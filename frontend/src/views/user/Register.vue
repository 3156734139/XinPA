<template>
  <div class="register-page">
    <!-- 背景 -->
    <div class="bg-layer">
      <div class="bg-shape bg-shape-1"></div>
      <div class="bg-shape bg-shape-2"></div>
      <div class="bg-grid"></div>
    </div>

    <div class="register-wrapper">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-logo">
            <span class="logo-heart">♡</span>
          </div>
          <h1 class="brand-title">陪玩星助手</h1>
          <p class="brand-desc">为陪玩从业者打造的<br>全栈业务管理平台</p>
        </div>
      </div>

      <!-- 右侧注册区 -->
      <div class="form-section">
        <div class="form-card">
          <div class="form-header">
            <h2 class="form-title">创建账号</h2>
            <p class="form-subtitle">注册后即可开始使用</p>
          </div>

          <!-- 错误提示 -->
          <transition name="error-fade">
            <div v-if="errorMsg" class="error-bar" :class="{ 'shake-active': shaking }">
              <span>{{ errorMsg }}</span>
            </div>
          </transition>

          <form @submit.prevent="handleRegister" class="register-form">
            <!-- 手机号 -->
            <div class="phone-input-wrap">
              <div class="phone-region">+86</div>
              <el-input
                v-model="form.phone"
                placeholder="手机号"
                maxlength="11"
                class="phone-input"
                size="large"
                @input="onPhoneInput"
              />
            </div>

            <!-- 验证码 -->
            <div class="code-input-wrap">
              <el-input
                v-model="form.code"
                placeholder="验证码"
                maxlength="6"
                size="large"
              />
              <button
                type="button"
                class="code-btn"
                :class="{ sending: codeCountdown > 0 }"
                :disabled="codeSending || codeCountdown > 0"
                @click="handleSendCode"
              >
                {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
              </button>
            </div>

            <!-- 昵称 -->
            <el-input
              v-model="form.nickname"
              placeholder="昵称 / 艺名（选填）"
              size="large"
            />

            <!-- 密码 -->
            <el-input
              v-model="form.password"
              placeholder="设置密码（选填，留空只能用验证码登录）"
              show-password
              size="large"
            />

            <button type="submit" class="submit-btn" :disabled="loading">
              <span v-if="loading" class="btn-loading"></span>
              <span v-else>注册</span>
            </button>
          </form>

          <div class="form-footer">
            <router-link to="/login" class="footer-link">已有账号？去登录</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { register, sendSmsCode } from '@/api/auth';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const errorMsg = ref('');
const shaking = ref(false);
const codeSending = ref(false);
const codeCountdown = ref(0);
let countdownTimer: ReturnType<typeof setInterval> | null = null;

const form = reactive({ phone: '', code: '', nickname: '', password: '' });

function onPhoneInput() {
  form.phone = form.phone.replace(/\D/g, '').slice(0, 11);
}

function showError(msg: string) {
  errorMsg.value = msg;
  shaking.value = true;
  setTimeout(() => { shaking.value = false; }, 500);
}

async function handleSendCode() {
  if (!form.phone || !/^1\d{10}$/.test(form.phone)) { showError('请输入正确的手机号'); return; }
  codeSending.value = true;
  try {
    const res: any = await sendSmsCode(form.phone);
    if (res.code !== 200) { showError(res.message || '发送失败'); return; }
    ElMessage.success('验证码已发送');
    codeCountdown.value = 60;
    countdownTimer = setInterval(() => { codeCountdown.value--; if (codeCountdown.value <= 0 && countdownTimer) clearInterval(countdownTimer); }, 1000);
  } catch (e: any) { showError(e?.response?.data?.message || '发送失败，请稍后重试'); }
  finally { codeSending.value = false; }
}

async function handleRegister() {
  if (!form.phone || !form.code) { showError('请填写手机号和验证码'); return; }
  if (!/^1\d{10}$/.test(form.phone)) { showError('手机号格式不正确'); return; }
  loading.value = true; errorMsg.value = '';
  try {
    const res: any = await register({
      phone: form.phone, code: form.code,
      nickname: form.nickname || undefined,
      password: form.password || undefined,
    });
    if (res.code !== 200) { showError(res.message || '注册失败'); return; }
    userStore.setToken(res.data.token);
    if (res.data.refreshToken) userStore.setRefreshToken(res.data.refreshToken);
    userStore.setUserType('USER');
    userStore.setUserInfo({ nickname: res.data.nickname });
    ElMessage.success('注册成功');
    router.push('/dashboard');
  } catch (e: any) { showError(e?.response?.data?.message || '注册失败，请稍后重试'); }
  finally { loading.value = false; }
}

onUnmounted(() => { if (countdownTimer) clearInterval(countdownTimer); });
</script>

<style scoped>
.register-page {
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
  background: radial-gradient(circle, rgba(232, 120, 154, 0.15), transparent);
  top: -200px; right: -100px;
}

.bg-shape-2 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, rgba(160, 120, 200, 0.12), transparent);
  bottom: -150px; left: -100px;
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(232, 120, 154, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(232, 120, 154, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
}

.register-wrapper {
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

.brand-content {
  text-align: center;
}

.logo-heart {
  font-size: 56px;
  display: inline-block;
  animation: float 3s ease-in-out infinite;
  filter: drop-shadow(0 0 30px rgba(232, 120, 154, 0.3));
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
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
  line-height: 1.7;
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

.form-header {
  margin-bottom: 28px;
}

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

/* 错误 */
.error-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.15);
  border-radius: 10px;
  padding: 8px 14px;
  margin-bottom: 14px;
  font-size: 13px;
  color: #FF6B6B;
}

.error-fade-enter-active, .error-fade-leave-active {
  transition: all 0.3s ease;
}
.error-fade-enter-from, .error-fade-leave-to {
  opacity: 0; transform: translateY(-6px);
}

.shake-active { animation: shakeX 0.4s ease; }
@keyframes shakeX {
  0%, 100% { transform: translateX(0); }
  20% { transform: translateX(-8px); }
  40% { transform: translateX(8px); }
  60% { transform: translateX(-5px); }
  80% { transform: translateX(5px); }
}

/* 表单 */
.register-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.register-form :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.06) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08) inset !important;
  border-radius: 12px !important;
  padding: 0 14px !important;
}

.register-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(232, 120, 154, 0.3) inset !important;
}

.register-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px #E8789A inset !important;
}

.register-form :deep(.el-input__inner) {
  color: #fff !important;
  height: 44px !important;
}

.register-form :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.2) !important;
}

/* 手机号 */
.phone-input-wrap {
  display: flex;
  align-items: center;
}

.phone-region {
  height: 44px;
  line-height: 44px;
  padding: 0 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-right: none;
  border-radius: 12px 0 0 12px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.phone-input {
  flex: 1;
}

:deep(.phone-input .el-input__wrapper) {
  background: rgba(255, 255, 255, 0.06) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.08) inset !important;
  border-radius: 0 12px 12px 0 !important;
  padding: 0 14px !important;
}

:deep(.phone-input .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(232, 120, 154, 0.3) inset !important;
}

:deep(.phone-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px #E8789A inset !important;
}

:deep(.phone-input .el-input__inner) {
  color: #fff !important;
  height: 44px !important;
}

:deep(.phone-input .el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.2) !important;
}

/* 验证码 */
.code-input-wrap {
  display: flex;
  gap: 10px;
}

.code-input-wrap :deep(.el-input__wrapper) {
  border-radius: 12px !important;
}

.code-btn {
  flex-shrink: 0;
  width: 110px;
  height: 44px;
  border-radius: 12px;
  border: 1px solid rgba(232, 120, 154, 0.2);
  background: rgba(232, 120, 154, 0.08);
  color: #E8789A;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
  font-weight: 500;
}

.code-btn:hover:not(:disabled) {
  background: rgba(232, 120, 154, 0.15);
  border-color: rgba(232, 120, 154, 0.35);
}

.code-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.code-btn.sending {
  color: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
}

/* 提交 */
.submit-btn {
  width: 100%;
  height: 46px;
  border-radius: 12px;
  border: none;
  background: linear-gradient(135deg, #E8789A, #D06080);
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
  margin-top: 2px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 24px rgba(232, 120, 154, 0.25);
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

/* 底部 */
.form-footer {
  text-align: center;
  margin-top: 20px;
}

.footer-link {
  color: rgba(255, 255, 255, 0.25);
  font-size: 13px;
  text-decoration: none;
  transition: color 0.2s;
  font-weight: 500;
}

.footer-link:hover { color: #E8789A; }

/* 响应式 */
@media (max-width: 900px) {
  .brand-section { display: none; }
  .form-section { width: 100%; padding: 20px; justify-content: center; }
  .form-card { max-width: 420px; }
}
</style>
