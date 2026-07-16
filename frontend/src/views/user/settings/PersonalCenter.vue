<template>
  <div class="personal-center">
    <!-- 页面标题 -->
    <div class="page-header mb-24">
      <div>
        <h2>个人中心</h2>
        <p>管理你的账号信息和资料</p>
      </div>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：头像 + 账号概览 -->
      <el-col :xs="24" :md="8" class="mb-20">
        <div class="profile-card">
          <!-- 头像区域 -->
          <div class="avatar-section">
            <div class="avatar-wrapper" @click="triggerUpload">
              <el-avatar
                :size="100"
                :src="avatarPreview || userStore.userInfo?.avatar || undefined"
                class="main-avatar"
              >
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="avatar-overlay">
                <el-icon :size="24"><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
            <input
              ref="fileInputRef"
              type="file"
              accept="image/*"
              style="display:none"
              @change="handleFileChange"
            />
            <h3 class="user-nickname">{{ userStore.userInfo?.nickname || '用户' }}</h3>
            <p class="user-username">@{{ userStore.userInfo?.username || '' }}</p>
          </div>

          <!-- 账号信息 -->
          <div class="account-info">
            <div class="info-divider"></div>
            <div class="info-row">
              <span class="info-label">会员类型</span>
              <el-tag :type="userStore.userInfo?.memberType === 1 ? 'warning' : 'info'" size="small" effect="plain" round>
                {{ userStore.userInfo?.memberType === 1 ? '付费会员' : '免费用户' }}
              </el-tag>
            </div>
            <div class="info-row">
              <span class="info-label">最后登录</span>
              <span class="info-value">{{ formatTime(userStore.userInfo?.lastLoginTime) || '首次登录' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">注册邮箱</span>
              <span class="info-value">{{ userStore.userInfo?.email || '未设置' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">手机号</span>
              <span class="info-value">{{ userStore.userInfo?.phone || '未设置' }}</span>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧：编辑资料 -->
      <el-col :xs="24" :md="16">
        <!-- 编辑资料 -->
        <div class="content-card mb-20">
          <div class="card-section-header">
            <el-icon :size="18"><Edit /></el-icon>
            <span>编辑资料</span>
          </div>
          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-position="top"
            class="profile-form"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="profileForm.nickname" placeholder="输入你的昵称/艺名" maxlength="64" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="选填" maxlength="20" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="选填" maxlength="128" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" :loading="profileSaving" @click="handleSaveProfile" round>
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 修改密码 -->
        <div class="content-card mb-20">
          <div class="card-section-header">
            <el-icon :size="18"><Lock /></el-icon>
            <span>修改密码</span>
          </div>
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-position="top"
            class="profile-form"
          >
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input
                    v-model="passwordForm.oldPassword"
                    type="password"
                    placeholder="输入当前密码"
                    show-password
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="新密码" prop="newPassword">
                  <el-input
                    v-model="passwordForm.newPassword"
                    type="password"
                    placeholder="至少6位"
                    show-password
                  />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="确认新密码" prop="confirmPassword">
                  <el-input
                    v-model="passwordForm.confirmPassword"
                    type="password"
                    placeholder="再次输入新密码"
                    show-password
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button type="primary" :loading="passwordSaving" @click="handleChangePassword" round>
                更新密码
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/store/user';
import { getUserInfo, updateUserInfo, changePassword, uploadAvatar } from '@/api/auth';
import { Camera, Edit, Lock } from '@element-plus/icons-vue';
import type { FormInstance } from 'element-plus';

const userStore = useUserStore();
const fileInputRef = ref<HTMLInputElement>();
const profileFormRef = ref<FormInstance>();
const passwordFormRef = ref<FormInstance>();
const profileSaving = ref(false);
const passwordSaving = ref(false);
const avatarPreview = ref<string>('');

const profileForm = reactive({
  nickname: '',
  phone: '',
  email: '',
});

const profileRules = {
  nickname: [{ max: 64, message: '最长64个字符', trigger: 'blur' }],
};

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: Function) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
};

onMounted(async () => {
  await loadUserInfo();
});

async function loadUserInfo() {
  try {
    const res: any = await getUserInfo();
    userStore.setUserInfo(res.data);
    profileForm.nickname = res.data?.nickname || '';
    profileForm.phone = res.data?.phone || '';
    profileForm.email = res.data?.email || '';
  } catch {
    // ignore
  }
}

function triggerUpload() {
  fileInputRef.value?.click();
}

async function handleFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  // 本地预览
  const reader = new FileReader();
  reader.onload = (e) => {
    avatarPreview.value = e.target?.result as string;
  };
  reader.readAsDataURL(file);

  try {
    const res: any = await uploadAvatar(file);
    const url = res.data?.url;
    if (url) {
      userStore.setUserInfo({ ...userStore.userInfo, avatar: url });
      avatarPreview.value = '';
      ElMessage.success('头像更新成功');
    }
  } catch {
    avatarPreview.value = '';
    ElMessage.error('头像上传失败');
  }

  input.value = '';
}

async function handleSaveProfile() {
  const valid = await profileFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  profileSaving.value = true;
  try {
    await updateUserInfo({
      nickname: profileForm.nickname,
      phone: profileForm.phone,
      email: profileForm.email,
    });
    userStore.setUserInfo({
      ...userStore.userInfo,
      nickname: profileForm.nickname,
      phone: profileForm.phone,
      email: profileForm.email,
    });
    ElMessage.success('资料已更新');
  } finally {
    profileSaving.value = false;
  }
}

async function handleChangePassword() {
  const valid = await passwordFormRef.value?.validate().catch(() => false);
  if (!valid) return;

  passwordSaving.value = true;
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    });
    ElMessage.success('密码修改成功');
    passwordForm.oldPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
  } finally {
    passwordSaving.value = false;
  }
}

function formatTime(time: string | undefined | null): string {
  if (!time) return '';
  return time.substring(0, 10) + ' ' + time.substring(11, 16);
}
</script>

<style scoped>
.personal-center {
  max-width: 1100px;
}

/* 页面标题 */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #5D4E6D;
  margin: 0 0 4px;
}

.page-header p {
  font-size: 14px;
  color: #A890B0;
  margin: 0;
}

.mb-20 { margin-bottom: 20px; }
.mb-24 { margin-bottom: 24px; }

/* ===== 左侧个人信息卡片 ===== */
.profile-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 4px rgba(232, 130, 154, 0.06);
  overflow: hidden;
  transition: box-shadow 0.35s ease, transform 0.35s ease;
}

.profile-card:hover {
  box-shadow: 0 4px 20px rgba(232, 130, 154, 0.12);
}

/* 头像区域 */
.avatar-section {
  padding: 32px 24px 20px;
  text-align: center;
  background: linear-gradient(180deg, #FFF5F7 0%, #fff 100%);
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
  border-radius: 50%;
  margin-bottom: 16px;
}

.main-avatar {
  border: 3px solid rgba(232, 130, 154, 0.15);
  transition: filter 0.25s ease;
  font-size: 36px;
  font-weight: 600;
  background: #FFF5F7;
  color: #E8789A;
}

.avatar-wrapper:hover .main-avatar {
  filter: brightness(0.7);
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.25s ease;
  border-radius: 50%;
  gap: 4px;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay span {
  font-size: 11px;
  font-weight: 500;
}

.user-nickname {
  font-size: 18px;
  font-weight: 700;
  color: #5D4E6D;
  margin: 0 0 4px;
}

.user-username {
  font-size: 13px;
  color: #A890B0;
  margin: 0;
}

/* 账号信息 */
.account-info {
  padding: 0 24px 20px;
}

.info-divider {
  height: 1px;
  background: rgba(232, 130, 154, 0.08);
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
}

.info-row + .info-row {
  border-top: 1px solid rgba(232, 130, 154, 0.06);
}

.info-label {
  font-size: 13px;
  color: #A890B0;
}

.info-value {
  font-size: 13px;
  font-weight: 500;
  color: #5D4E6D;
}

/* ===== 右侧内容卡片 ===== */
.content-card {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 4px rgba(232, 130, 154, 0.06);
  padding: 28px 32px;
  transition: box-shadow 0.35s ease, transform 0.35s ease;
}

.content-card:hover {
  box-shadow: 0 4px 20px rgba(232, 130, 154, 0.12);
}

.card-section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #5D4E6D;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(232, 130, 154, 0.08);
}

.profile-form {
  max-width: 640px;
}

@media (max-width: 768px) {
  .content-card {
    padding: 20px;
  }
  .profile-form {
    max-width: 100%;
  }
}
</style>
