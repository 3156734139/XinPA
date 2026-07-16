<template>
  <el-container class="user-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '240px'" class="user-sidebar">
      <div class="sidebar-header">
        <div class="sidebar-logo">
          <span class="logo-icon">♡</span>
          <span v-show="!isCollapse" class="logo-text">陪玩星助手</span>
        </div>
      </div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        :router="true"
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Monitor /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <span>人设主页</span>
        </el-menu-item>
        <el-menu-item index="/packages">
          <el-icon><PriceTag /></el-icon>
          <span>价目套餐</span>
        </el-menu-item>
        <el-menu-item index="/materials">
          <el-icon><Folder /></el-icon>
          <span>素材库</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/customers">
          <el-icon><UserFilled /></el-icon>
          <span>客户管理</span>
        </el-menu-item>
        <el-menu-item index="/finance">
          <el-icon><Money /></el-icon>
          <span>财务记账</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer" v-show="!isCollapse">
        <div class="sidebar-footer-text">v1.0.0</div>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <el-header class="user-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" :size="18">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb>
            <el-breadcrumb-item :to="'/dashboard'">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="34" :src="userStore.userInfo?.avatar || undefined" class="user-avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname || '用户' }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="user-main">
        <div class="page-container">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { getUserInfo } from '@/api/auth';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const isCollapse = ref(window.innerWidth < 768);

onMounted(async () => {
  try {
    const res: any = await getUserInfo();
    userStore.setUserInfo(res.data);
  } catch (e) {
    // ignore
  }
});

function handleCommand(command: string) {
  if (command === 'logout') {
    userStore.logout();
    router.push('/login');
  } else if (command === 'profile') {
    router.push('/settings');
  }
}
</script>

<style scoped>
.user-layout {
  height: 100vh;
}

/* ===== 侧边栏 ===== */
.user-sidebar {
  background: #FFF9FB;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 10;
  border-right: 1px solid rgba(232, 130, 154, 0.08);
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border-bottom: 1px solid rgba(232, 130, 154, 0.08);
  flex-shrink: 0;
}

.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #5D4E6D;
}

.logo-icon {
  font-size: 22px;
  color: #E8789A;
  flex-shrink: 0;
}

.logo-text {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.5px;
  white-space: nowrap;
  color: #5D4E6D;
}

/* 侧边栏菜单 */
.sidebar-menu {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  background: transparent !important;
  border-right: none !important;
}

.sidebar-menu .el-menu-item {
  height: 44px;
  line-height: 44px;
  margin: 2px 12px;
  padding: 0 12px !important;
  border-radius: 12px;
  color: #A890B0;
  transition: all 0.25s ease;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(232, 130, 154, 0.06) !important;
  color: #5D4E6D;
}

.sidebar-menu .el-menu-item.is-active {
  background: rgba(232, 130, 154, 0.12) !important;
  color: #E8789A;
}

.sidebar-menu .el-menu-item .el-icon {
  font-size: 18px;
  margin-right: 8px;
}

/* 折叠时的菜单 */
.sidebar-menu.is-collapse .el-menu-item {
  margin: 2px 0;
  border-radius: 0;
  justify-content: center;
}

.sidebar-menu.is-collapse .el-menu-item .el-icon {
  margin-right: 0;
}

/* 侧边栏底部 */
.sidebar-footer {
  padding: 12px 20px;
  border-top: 1px solid rgba(232, 130, 154, 0.08);
  flex-shrink: 0;
}

.sidebar-footer-text {
  font-size: 11px;
  color: #C4B0CC;
  text-align: center;
}

/* ===== 顶部栏 ===== */
.user-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: rgba(255, 249, 251, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(232, 130, 154, 0.08);
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
  color: #A890B0;
  transition: color 0.2s;
  padding: 4px;
  border-radius: 8px;
}

.collapse-btn:hover {
  color: #E8789A;
  background: rgba(232, 130, 154, 0.08);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 12px;
  transition: background 0.2s;
}

.user-info:hover {
  background: rgba(232, 130, 154, 0.06);
}

.user-avatar {
  border: 2px solid rgba(232, 130, 154, 0.15);
  flex-shrink: 0;
  background: #FFF5F7;
  color: #E8789A;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #5D4E6D;
}

.dropdown-arrow {
  font-size: 12px;
  color: #C4B0CC;
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu) {
  border-radius: 16px;
  padding: 6px;
  border: 1px solid rgba(232, 130, 154, 0.1);
  box-shadow: 0 4px 20px rgba(232, 130, 154, 0.12);
}

:deep(.el-dropdown-menu__item) {
  border-radius: 10px;
  padding: 8px 16px;
  color: #5D4E6D;
}

:deep(.el-dropdown-menu__item:hover) {
  background: rgba(232, 130, 154, 0.08);
  color: #E8789A;
}

:deep(.el-dropdown-menu__item .el-icon) {
  color: #A890B0;
}

:deep(.el-dropdown-menu__item:hover .el-icon) {
  color: #E8789A;
}

:deep(.el-dropdown-menu__item--divided) {
  border-top: 1px solid rgba(232, 130, 154, 0.08);
}

/* ===== 主内容区 ===== */
.user-main {
  background: linear-gradient(180deg, #FFF5F7 0%, #FFF9FB 100%);
  padding: 24px;
  overflow-y: auto;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .user-sidebar {
    width: 64px !important;
  }
  .user-main {
    padding: 12px;
  }
  .user-header {
    padding: 0 12px;
    height: 56px;
  }
}
</style>
