<template>
  <el-container class="admin-layout">
    <el-aside :width="isCollapse ? '64px' : '240px'" class="admin-sidebar">
      <div class="sidebar-header">
        <div class="sidebar-logo">
          <span class="logo-icon">⬡</span>
          <span v-show="!isCollapse" class="logo-text">管理后台</span>
        </div>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        :router="true"
        class="sidebar-menu"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>平台总览</span>
        </el-menu-item>
        <el-menu-item index="/admin/stats">
          <el-icon><DataLine /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
        <el-menu-item index="/admin/admins">
          <el-icon><Setting /></el-icon>
          <span>管理员管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/order-sources">
          <el-icon><Coin /></el-icon>
          <span>来源管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/payment-methods">
          <el-icon><Coin /></el-icon>
          <span>支付方式管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/announcements">
          <el-icon><Bell /></el-icon>
          <span>公告管理</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer" v-show="!isCollapse">
        <div class="sidebar-footer-text">管理后台 v1.0.0</div>
      </div>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" :size="18">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb>
            <el-breadcrumb-item :to="'/admin/dashboard'">管理后台</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="admin-info">
              <el-avatar :size="34" icon="UserFilled" class="admin-avatar" />
              <span class="username">管理员</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="admin-main">
        <div class="page-container">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const isCollapse = ref(false);

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout();
    router.push('/admin/login');
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

/* ===== 侧边栏 ===== */
.admin-sidebar {
  background: #0f172a;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 10;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #fff;
}

.logo-icon {
  font-size: 22px;
  color: #f87171;
  flex-shrink: 0;
}

.logo-text {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

/* 侧边栏菜单 */
.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  background: transparent !important;
}

.sidebar-menu .el-menu-item {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  padding: 0 12px !important;
  border-radius: 8px;
  color: #94a3b8;
  transition: all 0.2s ease;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(255, 255, 255, 0.06) !important;
  color: #e2e8f0;
}

.sidebar-menu .el-menu-item.is-active {
  background: rgba(248, 113, 113, 0.15) !important;
  color: #f87171;
}

.sidebar-menu .el-menu-item .el-icon {
  font-size: 18px;
  margin-right: 8px;
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: 12px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}

.sidebar-footer-text {
  font-size: 11px;
  color: #475569;
  text-align: center;
}

/* ===== 顶部栏 ===== */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid #e2e8f0;
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 9;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 18px;
  cursor: pointer;
  color: #64748b;
  transition: color 0.2s;
  padding: 4px;
  border-radius: 6px;
}

.collapse-btn:hover {
  color: var(--el-color-primary);
  background: #f1f5f9;
}

.header-right {
  display: flex;
  align-items: center;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
}

.admin-info:hover {
  background: #f1f5f9;
}

.admin-avatar {
  border: 2px solid #e2e8f0;
  flex-shrink: 0;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

.dropdown-arrow {
  font-size: 12px;
  color: #94a3b8;
}

/* ===== 主内容区 ===== */
.admin-main {
  background: #f0f2f5;
  padding: 24px;
  overflow-y: auto;
}
</style>
