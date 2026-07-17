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
            <el-button :icon="UploadFilled" class="quick-btn quick-btn-material" @click="$router.push('/materials')">上传素材</el-button>
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
              <el-button text size="small" @click="showTodoDialog = true">新建待办</el-button>
            </div>
          </template>
          <div v-if="todos.length === 0" class="empty-state">
            <el-empty description="暂无待办事项" :image-size="80" />
          </div>
          <div class="scroll-list">
            <div v-for="(item, index) in todos" :key="item.id" class="todo-item" :style="{ animationDelay: index * 0.08 + 's' }">
              <el-checkbox :model-value="item.status === 1" @change="toggleTodo(item.id)" size="large">
                <span :class="{ 'todo-done': item.status === 1 }">{{ item.title }}</span>
              </el-checkbox>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 回访提醒 -->
      <el-col :xs="24" :md="12" class="mb-20">
        <el-card shadow="never" class="reminder-card">
          <template #header>
            <div class="card-header">
              <span>回访提醒 · 贴心小棉袄 ✿</span>
            </div>
          </template>
          <div v-if="reminders.length === 0" class="empty-state">
            <el-empty description="暂无回访提醒" :image-size="80" />
          </div>
          <div class="scroll-list">
            <div v-for="item in reminders" :key="item.id" class="reminder-item">
              <div class="reminder-info">
                <el-avatar :size="36" class="reminder-avatar" icon="UserFilled" />
                <div class="reminder-text">
                  <span class="reminder-customer">{{ getCustomerName(item.customerId) }}</span>
                  <div class="reminder-meta">
                    <el-tag size="small" :round>
                      {{ item.remindType === 1 ? '3天回访' : item.remindType === 2 ? '7天回访' : '其他' }}
                    </el-tag>
                    <span :class="['reminder-days', { 'reminder-overdue': getRemainingDays(item) < 0, 'reminder-today': getRemainingDays(item) === 0 }]">
                      {{ getDaysText(item) }}
                    </span>
                  </div>
                </div>
              </div>
              <div class="reminder-actions">
                <el-button size="small" plain @click="goToCustomer(item)">去处理</el-button>
                <el-button size="small" plain class="btn-reached" @click="markReached(item)">已触达</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 预约日历 -->
      <el-col :span="24" class="mb-20">
        <OrderCalendar />
      </el-col>
    </el-row>

    <!-- 新建待办对话框 -->
    <el-dialog v-model="showTodoDialog" title="新建待办" width="400px" destroy-on-close>
      <el-form @submit.prevent="handleCreateTodo">
        <el-form-item label="待办内容">
          <el-input v-model="newTodoTitle" placeholder="请输入待办事项" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTodoDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreateTodo">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { getProfile } from '@/api/profile';
import { getDashboardStats } from '@/api/dashboard';
import { getTodos, createTodo, toggleTodo as apiToggleTodo } from '@/api/tools';
import { getReminders, getCustomerList, handleReminder } from '@/api/customers';
import OrderCalendar from '@/views/user/orders/OrderCalendar.vue';
import { Plus, UploadFilled, UserFilled, Money, MagicStick, List } from '@element-plus/icons-vue';

const userStore = useUserStore();
const orderStatus = ref('在线接单');
const orderStatusTag = ref('success');
const todayStats = ref<any>({});
const todos = ref<any[]>([]);
const reminders = ref<any[]>([]);
const showTodoDialog = ref(false);
const newTodoTitle = ref('');
const router = useRouter();
const customerMap = ref<Record<number, string>>({});

onMounted(async () => {
  try {
    const [profileRes, statsRes, todoRes, remindRes, custRes] = await Promise.all([
      getProfile(),
      getDashboardStats(),
      getTodos(0),
      getReminders(0),
      getCustomerList(),
    ]);
    const profile: any = profileRes;
    const statusMap: Record<number, string> = { 1: '在线接单', 2: '休息中', 3: '通宵接单', 4: '仅熟客' };
    orderStatus.value = statusMap[profile.data?.orderStatus] || '在线接单';
    todayStats.value = (statsRes as any).data || {};
    todos.value = (todoRes as any).data || [];
    reminders.value = (remindRes as any).data || [];
    const customers: any[] = (custRes as any).data || [];
    customers.forEach(c => { customerMap.value[c.id] = c.nickname; });
  } catch (e) {
    console.warn('Dashboard 数据加载失败', e);
  }
});

async function toggleTodo(id: number) {
  await apiToggleTodo(id);
}

async function handleCreateTodo() {
  if (!newTodoTitle.value.trim()) return;
  await createTodo({ title: newTodoTitle.value.trim(), todoType: 3 });
  newTodoTitle.value = '';
  showTodoDialog.value = false;
  const res: any = await getTodos(0);
  todos.value = res.data || [];
}

function getCustomerName(id: number): string {
  return customerMap.value[id] || `客户#${id}`;
}

function getRemainingDays(item: any): number {
  if (!item.remindTime) return 0;
  const now = new Date();
  const remind = new Date(item.remindTime);
  const diff = remind.getTime() - now.getTime();
  return Math.round(diff / 86400000);
}

function getDaysText(item: any): string {
  const days = getRemainingDays(item);
  if (days > 0) return `${days}天后`;
  if (days === 0) return '今天';
  return `逾期${Math.abs(days)}天`;
}

function goToCustomer(item: any) {
  const name = getCustomerName(item.customerId);
  router.push(`/customers?keyword=${encodeURIComponent(name)}`);
}

async function markReached(item: any) {
  await handleReminder(item.id);
  reminders.value = reminders.value.filter((r: any) => r.id !== item.id);
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

.quick-btn-material {
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

.scroll-list {
  max-height: 300px;
  overflow-y: auto;
}

.scroll-list::-webkit-scrollbar {
  width: 4px;
}

.scroll-list::-webkit-scrollbar-thumb {
  background: #E8789A;
  border-radius: 2px;
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

.reminder-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
}

.reminder-days {
  font-size: 12px;
  color: #A890B0;
}

.reminder-today {
  color: #e6a23c;
  font-weight: 600;
}

.reminder-overdue {
  color: #f56c6c;
  font-weight: 600;
}

.reminder-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.btn-reached {
  transition: all 0.2s ease;
}
.btn-reached:hover {
  color: #67c23a !important;
  border-color: #67c23a !important;
  background: rgba(103, 194, 58, 0.04) !important;
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
