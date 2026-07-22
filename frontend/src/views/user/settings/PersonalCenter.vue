<template>
  <div class="personal-center">
    <!-- 页面标题 -->
    <div class="page-header mb-24">
      <div>
        <h2><PixelSticker :size="18" /> 个人中心</h2>
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
            <div class="user-days" v-if="userStore.userInfo?.createdAt">
              已使用星助手 {{ calcDays(userStore.userInfo.createdAt) }} 天
            </div>
          </div>

          <!-- 账号信息 -->
          <div class="account-info">
            <div class="info-divider"></div>
            <div class="info-row">
              <span class="info-label">会员类型</span>
              <el-tag size="small" effect="plain" round>
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

      <!-- 右侧：Tabs -->
      <el-col :xs="24" :md="16">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <!-- Tab 1: 基本资料 -->
          <el-tab-pane label="基本资料" name="profile">
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
                      <el-input v-model="profileForm.phone" placeholder="选填" maxlength="20" :disabled="!!profileForm.phone" />
                      <div v-if="profileForm.phone" class="form-tip">手机号绑定后不可修改</div>
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
                      <el-input v-model="passwordForm.oldPassword" placeholder="如未设置密码则留空" show-password />
                      <div class="form-tip">如注册时未设置密码，此项留空即可</div>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="新密码" prop="newPassword">
                      <el-input v-model="passwordForm.newPassword" placeholder="至少6位" show-password />
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="确认新密码" prop="confirmPassword">
                      <el-input v-model="passwordForm.confirmPassword" placeholder="再次输入新密码" show-password />
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
          </el-tab-pane>

          <!-- Tab 2: 配置管理 -->
          <el-tab-pane label="配置管理" name="configs">
            <div class="config-grid">
              <!-- 我的来源 -->
              <div class="content-card config-card">
                <div class="card-section-header">
                  <el-icon :size="16"><Coin /></el-icon>
                  <span>我的来源</span>
                </div>
                <div class="config-body">
                  <div class="add-row">
                    <el-input v-model="newSourceName" placeholder="输入来源名称" clearable size="small" @keyup.enter="handleAddSource" />
                    <el-button type="primary" size="small" :disabled="!newSourceName.trim()" @click="handleAddSource">新增</el-button>
                  </div>
                  <div v-if="sources.length === 0" class="empty-tip">暂无</div>
                  <div v-else class="tag-list">
                    <el-tag
                      v-for="item in sources" :key="item.id"
                      closable :disable-transitions="false"
                      :type="item.status === 1 ? '' : 'info'"
                      @close="handleDeleteSource(item.id)"
                    >
                      <span class="tag-name" @click="handleEditSource(item)">{{ item.name }}</span>
                    </el-tag>
                  </div>
                </div>
              </div>

              <!-- 我的套餐类型 -->
              <div class="content-card config-card">
                <div class="card-section-header">
                  <el-icon :size="16"><PriceTag /></el-icon>
                  <span>我的套餐类型</span>
                </div>
                <div class="config-body">
                  <div class="add-row">
                    <el-input v-model="newPackageTypeName" placeholder="输入类型名称" clearable size="small" @keyup.enter="handleAddPackageType" />
                    <el-button type="primary" size="small" :disabled="!newPackageTypeName.trim()" @click="handleAddPackageType">新增</el-button>
                  </div>
                  <div v-if="packageTypes.length === 0" class="empty-tip">暂无</div>
                  <div v-else class="tag-list">
                    <el-tag
                      v-for="item in packageTypes" :key="item.id"
                      closable :disable-transitions="false"
                      :type="item.status === 1 ? '' : 'info'"
                      @close="handleDeletePackageType(item.id)"
                    >
                      <span class="tag-name" @click="handleEditPackageType(item)">{{ item.name }}</span>
                    </el-tag>
                  </div>
                </div>
              </div>

              <!-- 我的支付方式 -->
              <div class="content-card config-card">
                <div class="card-section-header">
                  <el-icon :size="16"><Coin /></el-icon>
                  <span>我的支付方式</span>
                </div>
                <div class="config-body">
                  <div class="add-row">
                    <el-input v-model="newPaymentMethodName" placeholder="输入支付方式名称" clearable size="small" @keyup.enter="handleAddPaymentMethod" />
                    <el-button type="primary" size="small" :disabled="!newPaymentMethodName.trim()" @click="handleAddPaymentMethod">新增</el-button>
                  </div>
                  <div v-if="paymentMethods.length === 0" class="empty-tip">暂无</div>
                  <div v-else class="tag-list">
                    <el-tag
                      v-for="item in paymentMethods" :key="item.id"
                      closable :disable-transitions="false"
                      :type="item.status === 1 ? '' : 'info'"
                      @close="handleDeletePaymentMethod(item.id)"
                    >
                      <span class="tag-name" @click="handleEditPaymentMethod(item)">{{ item.name }}</span>
                    </el-tag>
                  </div>
                </div>
              </div>

              <!-- 我的优惠等级 -->
              <div class="content-card config-card config-card-wide">
                <div class="card-section-header">
                  <el-icon :size="16"><TrendCharts /></el-icon>
                  <span>我的优惠等级</span>
                </div>
                <div class="config-body">
                  <el-table :data="vipLevels" size="small" stripe style="width:100%">
                    <el-table-column prop="level" label="等级" width="60" />
                    <el-table-column prop="name" label="名称" width="80" />
                    <el-table-column prop="threshold" label="门槛(元)" width="100" />
                    <el-table-column prop="discount" label="折扣" width="70">
                      <template #default="{ row }">{{ row.discount }}折</template>
                    </el-table-column>
                    <el-table-column prop="benefits" label="福利" min-width="100" />
                    <el-table-column label="状态" width="70">
                      <template #default="{ row }">
                        <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作" width="110">
                      <template #default="{ row }">
                        <el-button size="small" link @click="handleEditVipLevel(row)">编辑</el-button>
                        <el-button size="small" link type="danger" @click="handleDeleteVipLevel(row.id)">删除</el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <el-button size="small" style="margin-top:12px" @click="handleAddVipLevel">+ 新增等级</el-button>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>

    <!-- ============ 全局弹窗 ============ -->

    <!-- 编辑来源 -->
    <el-dialog v-model="editDialogVisible" title="编辑来源" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="来源名称"><el-input v-model="editForm.name" maxlength="32" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="editForm.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="editForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSaving" @click="handleSaveSource">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑优惠等级 -->
    <el-dialog v-model="vipEditVisible" title="编辑优惠等级" width="500px">
      <el-form :model="vipEditForm" label-width="120px">
        <el-form-item label="等级数字"><el-input-number v-model="vipEditForm.level" :min="1" :max="20" /></el-form-item>
        <el-form-item label="等级名称"><el-input v-model="vipEditForm.name" maxlength="64" /></el-form-item>
        <el-form-item label="消费门槛(元)"><el-input-number v-model="vipEditForm.threshold" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="折扣(%)"><el-input-number v-model="vipEditForm.discount" :min="1" :max="100" /><div class="form-tip">如 98 表示 98折</div></el-form-item>
        <el-form-item label="福利描述"><el-input v-model="vipEditForm.benefits" type="textarea" :rows="2" maxlength="500" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="vipEditForm.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="vipEditForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="vipEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="vipSaving" @click="handleSaveVipLevel">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑套餐类型 -->
    <el-dialog v-model="ptEditVisible" title="编辑套餐类型" width="400px">
      <el-form :model="ptEditForm" label-width="80px">
        <el-form-item label="类型名称"><el-input v-model="ptEditForm.name" maxlength="32" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="ptEditForm.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="ptEditForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ptEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="ptSaving" @click="handleSavePackageType">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑支付方式 -->
    <el-dialog v-model="pmEditVisible" title="编辑支付方式" width="400px">
      <el-form :model="pmEditForm" label-width="80px">
        <el-form-item label="支付方式"><el-input v-model="pmEditForm.name" maxlength="32" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="pmEditForm.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="pmEditForm.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pmEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="pmSaving" @click="handleSavePaymentMethod">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/store/user';
import { getUserInfo, updateUserInfo, changePassword, getAvatarUploadToken, notifyAvatarComplete } from '@/api/auth';
import { getUserSources, createUserSource, updateUserSource, deleteUserSource } from '@/api/orderSource';
import { getUserVipLevels, createUserVipLevel, updateUserVipLevel, deleteUserVipLevel } from '@/api/vipLevel';
import { getUserPackageTypes, createUserPackageType, updateUserPackageType, deleteUserPackageType } from '@/api/packageType';
import { getUserPaymentMethods, createUserPaymentMethod, updateUserPaymentMethod, deleteUserPaymentMethod } from '@/api/paymentMethod';
import { Camera, Edit, Lock, Coin, TrendCharts, PriceTag } from '@element-plus/icons-vue';
import type { FormInstance } from 'element-plus';
import PixelSticker from '@/components/PixelSticker.vue';

const userStore = useUserStore();
const activeTab = ref('profile');
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
  nickname: [
    { min: 2, message: '昵称至少2个字', trigger: 'blur' },
    { max: 6, message: '昵称最多6个字', trigger: 'blur' },
  ],
};

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const passwordRules = {
  oldPassword: [{ max: 64, message: '最长64个字符', trigger: 'blur' }],
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

// 来源管理
const sources = ref<any[]>([]);
const newSourceName = ref('');
const editDialogVisible = ref(false);
const editSaving = ref(false);
const editForm = reactive({ id: 0, name: '', sortOrder: 0, status: 1 });

onMounted(async () => {
  await loadUserInfo();
  await loadSources();
  await loadVipLevels();
  await loadPackageTypes();
  await loadPaymentMethods();
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

function getFileExt(fileName: string): string {
  const idx = fileName.lastIndexOf('.');
  return idx >= 0 ? fileName.substring(idx) : '';
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

  // 优先 STS 前端直传 OSS
  try {
    await doStsUpload(file);
    ElMessage.success('头像更新成功');
  } catch (e: any) {
    console.error('=== 头像 OSS 上传失败 ===');
    console.error('错误对象:', e);
    console.error('错误消息:', e.message);
    console.error('错误状态码:', e.status || e.statusCode);
    console.error('错误请求ID:', e.requestId);
    console.error('错误堆栈:', e.stack);
    avatarPreview.value = '';
    ElMessage.error('头像上传失败: ' + (e.message || '未知错误'));
  }

  input.value = '';
}

/** STS 前端直传 OSS（使用原生 fetch + Web Crypto API，不依赖 ali-oss SDK） */
async function doStsUpload(file: File): Promise<string> {
  const tokenRes: any = await getAvatarUploadToken();
  const creds = tokenRes.data;
  const objectKey = creds.objectKey + getFileExt(file.name);

  const url = `https://${creds.bucket}.${creds.region}.aliyuncs.com/${objectKey}`;
  const date = new Date().toUTCString();
  const contentType = file.type || 'application/octet-stream';

  // OSS 签名要求：CanonicalizedOSSHeaders 按字典序排列
  // Date 头浏览器自动添加，签名中必须包含 Date 值，同时通过 x-oss-date 传入规范头
  const ossHeaders = `x-oss-date:${date}\nx-oss-security-token:${creds.securityToken}\n`;
  const stringToSign = `PUT\n\n${contentType}\n${date}\n${ossHeaders}/${creds.bucket}/${objectKey}`;
  const signature = await hmacSha1Base64(creds.accessKeySecret, stringToSign);

  const res = await fetch(url, {
    method: 'PUT',
    headers: {
      'Authorization': `OSS ${creds.accessKeyId}:${signature}`,
      'Content-Type': contentType,
      'x-oss-date': date,
      'x-oss-security-token': creds.securityToken,
    },
    body: file,
  });

  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`OSS ${res.status} ${res.statusText}: ${text}`);
  }

  const notifyRes: any = await notifyAvatarComplete(objectKey);
  const avatarUrl = notifyRes.data?.url;
  if (avatarUrl) {
    userStore.setUserInfo({ ...userStore.userInfo, avatar: avatarUrl });
  }
  return '';
}

/** Web Crypto API 计算 HMAC-SHA1 并返回 Base64 */
async function hmacSha1Base64(secret: string, data: string): Promise<string> {
  const encoder = new TextEncoder();
  const key = await crypto.subtle.importKey(
    'raw', encoder.encode(secret), { name: 'HMAC', hash: 'SHA-1' },
    false, ['sign']
  );
  const sig = await crypto.subtle.sign('HMAC', key, encoder.encode(data));
  return btoa(String.fromCharCode(...new Uint8Array(sig)));
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

function calcDays(createdAt: string): number {
  return Math.floor((Date.now() - new Date(createdAt).getTime()) / 86400000) + 1;
}

function formatTime(time: string | undefined | null): string {
  if (!time) return '';
  return time.substring(0, 10) + ' ' + time.substring(11, 16);
}

// ==================== 来源管理 ====================

async function loadSources() {
  try {
    const res: any = await getUserSources();
    sources.value = res.data || [];
  } catch { /* ignore */ }
}

async function handleAddSource() {
  const name = newSourceName.value.trim();
  if (!name) return;
  try {
    await createUserSource({ name, sortOrder: sources.value.length });
    newSourceName.value = '';
    await loadSources();
    ElMessage.success('来源已添加');
  } catch { /* ignore */ }
}

function handleEditSource(item: any) {
  editForm.id = item.id;
  editForm.name = item.name;
  editForm.sortOrder = item.sortOrder ?? 0;
  editForm.status = item.status ?? 1;
  editDialogVisible.value = true;
}

async function handleSaveSource() {
  editSaving.value = true;
  try {
    await updateUserSource({ id: editForm.id, name: editForm.name, sortOrder: editForm.sortOrder, status: editForm.status });
    editDialogVisible.value = false;
    await loadSources();
    ElMessage.success('来源已更新');
  } finally {
    editSaving.value = false;
  }
}

async function handleDeleteSource(id: number) {
  try {
    await deleteUserSource(id);
    await loadSources();
    ElMessage.success('来源已删除');
  } catch { /* ignore */ }
}

// ==================== 优惠等级管理 ====================

const vipLevels = ref<any[]>([]);
const vipEditVisible = ref(false);
const vipSaving = ref(false);
const vipEditForm = reactive({ id: 0, level: 1, name: '', threshold: 0, discount: 98, benefits: '', sortOrder: 0, status: 1 });

async function loadVipLevels() {
  try {
    const res: any = await getUserVipLevels();
    vipLevels.value = res.data || [];
  } catch { /* ignore */ }
}

function handleAddVipLevel() {
  vipEditForm.id = 0;
  vipEditForm.level = vipLevels.value.length + 1;
  vipEditForm.name = '';
  vipEditForm.threshold = 0;
  vipEditForm.discount = 98;
  vipEditForm.benefits = '';
  vipEditForm.sortOrder = vipLevels.value.length + 1;
  vipEditForm.status = 1;
  vipEditVisible.value = true;
}

function handleEditVipLevel(item: any) {
  vipEditForm.id = item.id;
  vipEditForm.level = item.level;
  vipEditForm.name = item.name;
  vipEditForm.threshold = item.threshold;
  vipEditForm.discount = item.discount;
  vipEditForm.benefits = item.benefits || '';
  vipEditForm.sortOrder = item.sortOrder ?? 0;
  vipEditForm.status = item.status ?? 1;
  vipEditVisible.value = true;
}

async function handleSaveVipLevel() {
  vipSaving.value = true;
  try {
    const data = { ...vipEditForm };
    if (vipEditForm.id) {
      await updateUserVipLevel(data);
    } else {
      await createUserVipLevel(data);
    }
    vipEditVisible.value = false;
    await loadVipLevels();
    ElMessage.success(vipEditForm.id ? '等级已更新' : '等级已添加');
  } finally {
    vipSaving.value = false;
  }
}

async function handleDeleteVipLevel(id: number) {
  try {
    await deleteUserVipLevel(id);
    await loadVipLevels();
    ElMessage.success('等级已删除');
  } catch { /* ignore */ }
}

// ==================== 套餐类型管理 ====================

const packageTypes = ref<any[]>([]);
const newPackageTypeName = ref('');
const ptEditVisible = ref(false);
const ptSaving = ref(false);
const ptEditForm = reactive({ id: 0, name: '', sortOrder: 0, status: 1 });

async function loadPackageTypes() {
  try {
    const res: any = await getUserPackageTypes();
    packageTypes.value = res.data || [];
  } catch { /* ignore */ }
}

async function handleAddPackageType() {
  const name = newPackageTypeName.value.trim();
  if (!name) return;
  try {
    await createUserPackageType({ name, sortOrder: packageTypes.value.length });
    newPackageTypeName.value = '';
    await loadPackageTypes();
    ElMessage.success('套餐类型已添加');
  } catch { /* ignore */ }
}

function handleEditPackageType(item: any) {
  ptEditForm.id = item.id;
  ptEditForm.name = item.name;
  ptEditForm.sortOrder = item.sortOrder ?? 0;
  ptEditForm.status = item.status ?? 1;
  ptEditVisible.value = true;
}

async function handleSavePackageType() {
  ptSaving.value = true;
  try {
    await updateUserPackageType({ id: ptEditForm.id, name: ptEditForm.name, sortOrder: ptEditForm.sortOrder, status: ptEditForm.status });
    ptEditVisible.value = false;
    await loadPackageTypes();
    ElMessage.success('套餐类型已更新');
  } finally {
    ptSaving.value = false;
  }
}

async function handleDeletePackageType(id: number) {
  try {
    await deleteUserPackageType(id);
    await loadPackageTypes();
    ElMessage.success('套餐类型已删除');
  } catch { /* ignore */ }
}

// ==================== 支付方式管理 ====================

const paymentMethods = ref<any[]>([]);
const newPaymentMethodName = ref('');
const pmEditVisible = ref(false);
const pmSaving = ref(false);
const pmEditForm = reactive({ id: 0, name: '', sortOrder: 0, status: 1 });

async function loadPaymentMethods() {
  try {
    const res: any = await getUserPaymentMethods();
    paymentMethods.value = res.data || [];
  } catch { /* ignore */ }
}

async function handleAddPaymentMethod() {
  const name = newPaymentMethodName.value.trim();
  if (!name) return;
  try {
    await createUserPaymentMethod({ name, sortOrder: paymentMethods.value.length });
    newPaymentMethodName.value = '';
    await loadPaymentMethods();
    ElMessage.success('支付方式已添加');
  } catch { /* ignore */ }
}

function handleEditPaymentMethod(item: any) {
  pmEditForm.id = item.id;
  pmEditForm.name = item.name;
  pmEditForm.sortOrder = item.sortOrder ?? 0;
  pmEditForm.status = item.status ?? 1;
  pmEditVisible.value = true;
}

async function handleSavePaymentMethod() {
  pmSaving.value = true;
  try {
    await updateUserPaymentMethod({ id: pmEditForm.id, name: pmEditForm.name, sortOrder: pmEditForm.sortOrder, status: pmEditForm.status });
    pmEditVisible.value = false;
    await loadPaymentMethods();
    ElMessage.success('支付方式已更新');
  } finally {
    pmSaving.value = false;
  }
}

async function handleDeletePaymentMethod(id: number) {
  try {
    await deleteUserPaymentMethod(id);
    await loadPaymentMethods();
    ElMessage.success('支付方式已删除');
  } catch { /* ignore */ }
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
  margin: 0 0 2px;
}

.user-days {
  font-size: 12px;
  color: #ccc;
  margin-top: 2px;
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

/* ===== 表单提示 ===== */
.form-tip {
  font-size: 12px;
  color: #A890B0;
  margin-top: 4px;
  line-height: 1.4;
}

/* ===== Tabs ===== */
.profile-tabs {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 1px 4px rgba(232, 130, 154, 0.06);
  padding: 8px 24px 24px;
}

.profile-tabs :deep(.el-tabs__header) {
  margin: 0 0 20px;
  border-bottom: 1px solid rgba(232, 130, 154, 0.08);
}

.profile-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 600;
  color: #A890B0;
  height: 48px;
  line-height: 48px;
  padding: 0 20px;
}

.profile-tabs :deep(.el-tabs__item.is-active) {
  color: #E8789A;
}

.profile-tabs :deep(.el-tabs__active-bar) {
  background: #E8789A;
  height: 3px;
  border-radius: 2px;
}

/* ===== 配置网格 ===== */
.config-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.config-card-wide {
  grid-column: 1 / -1;
}

.config-card {
  padding: 20px !important;
}

.config-card .card-section-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  font-size: 14px;
}

.config-body {
  min-height: 0;
}

/* ===== 标签列表 ===== */
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.tag-name {
  cursor: pointer;
  border-bottom: 1px dashed transparent;
  transition: border-color 0.2s;
}

.tag-name:hover {
  border-bottom-color: currentColor;
}

.add-row {
  display: flex;
  gap: 8px;
}

.add-row .el-input {
  flex: 1;
}

/* ===== 空态 ===== */
.empty-tip {
  font-size: 13px;
  color: #A890B0;
  padding: 12px 0;
}

@media (max-width: 768px) {
  .config-grid {
    grid-template-columns: 1fr;
  }
  .content-card {
    padding: 20px;
  }
  .profile-form {
    max-width: 100%;
  }
}
</style>
