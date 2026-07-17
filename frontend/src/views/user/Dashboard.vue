<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <el-row :gutter="20">
      <el-col :span="24" class="mb-20">
        <el-card shadow="never" class="welcome-card">
          <div class="welcome">
            <div class="welcome-text">
              <h3>欢迎回来，{{ userStore.userInfo?.nickname || '用户' }}</h3>
              <p>今日又是元气满满的一天 ~</p>
            </div>
            <div class="welcome-status">
              <el-tag
                :type="orderStatusTag === '在线' ? 'success' : 'warning'"
                size="large"
                effect="dark"
                round
              >
                当前状态：{{ orderStatus }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 快捷操作 -->
      <el-col :span="24" class="mb-20">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>♡ 快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button :icon="Plus" class="quick-btn quick-btn-order" @click="$router.push('/orders')">创建订单</el-button>
            <el-button :icon="Calendar" class="quick-btn quick-btn-appt" @click="scrollToCalendar">新建预约</el-button>
            <el-button :icon="UserFilled" class="quick-btn quick-btn-customer" @click="$router.push('/customers')">客户管理</el-button>
            <el-button :icon="Money" class="quick-btn quick-btn-finance" @click="$router.push('/finance')">记账</el-button>
            <el-button :icon="MagicStick" class="quick-btn quick-btn-ai" @click="$router.push('/ai')">AI工具</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 今日统计 -->
      <el-col :xs="24" :sm="8" class="mb-20">
        <el-card shadow="never" class="stat-card-wrapper">
          <div class="stat-card-inner">
            <div class="stat-icon stat-icon-income">
              <el-icon :size="24"><Money /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value">{{ todayStats.todayIncome || '0.00' }}</div>
              <div class="stat-label">今日收入（元）</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8" class="mb-20">
        <el-card shadow="never" class="stat-card-wrapper">
          <div class="stat-card-inner">
            <div class="stat-icon stat-icon-orders">
              <el-icon :size="24"><List /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value">{{ todayStats.todayOrders ?? 0 }}</div>
              <div class="stat-label">今日订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8" class="mb-20">
        <el-card shadow="never" class="stat-card-wrapper">
          <div class="stat-card-inner">
            <div class="stat-icon stat-icon-customers">
              <el-icon :size="24"><UserFilled /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value">{{ todayStats.totalCustomers ?? 0 }}</div>
              <div class="stat-label">客户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 待办事项 -->
      <el-col :xs="24" :md="12" class="mb-20">
        <el-card shadow="never" class="todo-card">
          <template #header>
            <div class="card-header">
              <span>待办事项 · 努力工作中 ♡</span>
              <el-button text size="small" @click="$router.push('/todos')">查看全部</el-button>
            </div>
          </template>
          <div v-if="todos.length === 0" class="empty-state">
            <el-empty description="暂无待办事项" :image-size="80" />
          </div>
          <div v-for="(item, index) in todos" :key="item.id" class="todo-item" :style="{ animationDelay: index * 0.08 + 's' }">
            <el-checkbox :model-value="item.status === 1" @change="toggleTodo(item.id)" size="large">
              <span :class="{ 'todo-done': item.status === 1 }">{{ item.title }}</span>
            </el-checkbox>
          </div>
        </el-card>
      </el-col>

      <!-- 回访提醒 -->
      <el-col :xs="24" :md="12" class="mb-20">
        <el-card shadow="never" class="reminder-card">
          <template #header>
            <div class="card-header">
              <span>回访提醒 · 贴心小棉袄 ✿</span>
              <el-button text size="small" @click="$router.push('/customers')">查看全部</el-button>
            </div>
          </template>
          <div v-if="reminders.length === 0" class="empty-state">
            <el-empty description="暂无回访提醒" :image-size="80" />
          </div>
          <div v-for="item in reminders" :key="item.id" class="reminder-item">
            <div class="reminder-info">
              <el-avatar :size="36" class="reminder-avatar" icon="UserFilled" />
              <div class="reminder-text">
                <span class="reminder-customer">客户ID: {{ item.customerId }}</span>
                <el-tag size="small" :round>
                  {{ item.remindType === 1 ? '3天回访' : item.remindType === 2 ? '7天回访' : '其他' }}
                </el-tag>
              </div>
            </div>
            <el-button size="small" plain @click="handleRemind(item.id)">处理</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 预约日历 -->
      <el-col :span="24" class="mb-20">
        <OrderCalendar />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useUserStore } from '@/store/user';
import { getProfile } from '@/api/profile';
import { getDashboardStats } from '@/api/dashboard';
import { getTodos, toggleTodo as apiToggleTodo } from '@/api/tools';
import { getReminders, handleReminder } from '@/api/customers';
import OrderCalendar from '@/views/user/orders/OrderCalendar.vue';
import { Plus, Calendar, UserFilled, Money, MagicStick, List } from '@element-plus/icons-vue';

const userStore = useUserStore();
const orderStatus = ref('在线接单');
const orderStatusTag = ref('success');
const todayStats = ref<any>({});
const todos = ref<any[]>([]);
const reminders = ref<any[]>([]);

onMounted(async () => {
  try {
    const [profileRes, statsRes, todoRes, remindRes] = await Promise.all([
      getProfile(),
      getDashboardStats(),
      getTodos(0),
      getReminders(0),
    ]);
    const profile: any = profileRes;
    const statusMap: Record<number, string> = { 1: '在线接单', 2: '休息中', 3: '通宵接单', 4: '仅熟客' };
    orderStatus.value = statusMap[profile.data?.orderStatus] || '在线接单';
    todayStats.value = (statsRes as any).data || {};
    todos.value = (todoRes as any).data || [];
    reminders.value = (remindRes as any).data || [];
  } catch (e) {
    console.warn('Dashboard 数据加载失败', e);
  }
});

async function toggleTodo(id: number) {
  await apiToggleTodo(id);
}

async function handleRemind(id: number) {
  await handleReminder(id);
  reminders.value = reminders.value.filter((r: any) => r.id !== id);
}

function scrollToCalendar() {
  const el = document.querySelector('.order-calendar');
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
}
</script>

<style scoped>
.dashboard {
  padding: 0;
  position: relative;
}

.mb-20 { margin-bottom: 20px; }

/* ===== 欢迎卡片 ===== */
.welcome-card {
  background: linear-gradient(135deg, #FFD6E0 0%, #E8DFF5 50%, #FFD6E0 100%) !important;
  background-size: 200% 200% !important;
  position: relative;
  overflow: hidden;
}

.welcome {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text h3 {
  margin: 0 0 4px;
  font-size: 20px;
  color: #5D4E6D;
}

.welcome-text p {
  color: rgba(93, 78, 109, 0.6);
  font-size: 14px;
  margin: 0;
}

/* ===== 卡片头部 ===== */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 15px;
  font-weight: 600;
  color: #5D4E6D;
}

/* ===== 快捷操作 ===== */
.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.quick-btn {
  border: none !important;
  color: #fff !important;
  font-weight: 500;
  transition: all 0.3s ease !important;
}

.quick-btn:hover {
  transform: translateY(-2px) scale(1.03) !important;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12) !important;
}

.quick-btn-order {
  background: linear-gradient(135deg, #E8789A, #f09bb6) !important;
}

.quick-btn-appt {
  background: linear-gradient(135deg, #7BC47F, #9fd9a2) !important;
}

.quick-btn-customer {
  background: linear-gradient(135deg, #F0B070, #f5c694) !important;
}

.quick-btn-finance {
  background: linear-gradient(135deg, #7fa3e0, #a3bef0) !important;
}

.quick-btn-ai {
  background: linear-gradient(135deg, #b088d6, #ccaceb) !important;
}

/* ===== 统计卡片 ===== */
.stat-card-wrapper {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  position: relative;
  overflow: hidden;
}

.stat-card-wrapper:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 28px rgba(232, 130, 154, 0.15) !important;
}

.stat-card-wrapper:hover .stat-icon {
  transform: scale(1.1) rotate(4deg);
}

.stat-card-inner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 4px 0;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.stat-icon-income {
  background: rgba(232, 130, 154, 0.1);
  color: #E8789A;
}

.stat-icon-orders {
  background: rgba(123, 196, 127, 0.1);
  color: #7BC47F;
}

.stat-icon-customers {
  background: rgba(240, 176, 112, 0.1);
  color: #F0B070;
}

.stat-body {
  flex: 1;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
  color: #5D4E6D;
}

.stat-label {
  font-size: 13px;
  color: #A890B0;
  margin-top: 2px;
}

/* ===== 待办 ===== */
.todo-card, .reminder-card {
  height: 100%;
}

.todo-item {
  padding: 12px 0;
  border-bottom: 1px solid rgba(232, 130, 154, 0.08);
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-done {
  text-decoration: line-through;
  color: #C4B0CC;
}

/* ===== 回访提醒 ===== */
.reminder-item {
  padding: 14px 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(232, 130, 154, 0.08);
}

.reminder-item:last-child {
  border-bottom: none;
}

.reminder-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.reminder-avatar {
  background: rgba(232, 130, 154, 0.1);
  color: #E8789A;
  flex-shrink: 0;
}

.reminder-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reminder-customer {
  font-size: 14px;
  font-weight: 500;
  color: #5D4E6D;
}

.empty-state {
  padding: 8px 0;
}

@media (max-width: 768px) {
  .welcome {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
