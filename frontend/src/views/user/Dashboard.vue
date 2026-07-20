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

      <!-- 客户消费排行榜 -->
      <el-col :xs="24" :md="12" class="mb-20">
        <el-card shadow="never" class="ranking-card">
          <template #header>
            <div class="card-header">
              <span>客户消费排行榜</span>
            </div>
          </template>
          <div v-if="spendingRanking.length === 0" class="empty-state">
            <el-empty description="暂无客户数据" :image-size="80" />
          </div>
          <div class="scroll-list">
            <div
              v-for="(item, index) in spendingRanking"
              :key="item.customerId"
              :class="['ranking-item', { 'ranking-item-top3': index < 3 }]"
            >
              <div class="ranking-left">
                <span :class="['ranking-badge', 'ranking-badge-' + Math.min(index + 1, 4)]">
                  {{ index + 1 }}
                </span>
                <div class="ranking-info">
                  <el-link :underline="false" class="ranking-name" @click="goToCustomerDetail(item)">
                    {{ item.nickname }}
                  </el-link>
                  <div class="ranking-meta">
                    <span>{{ item.orderCount }}单</span>
                    <span class="ranking-sep">|</span>
                    <span>占比 {{ item.percentage }}%</span>
                  </div>
                </div>
              </div>
              <div class="ranking-right">
                <div class="ranking-amount">¥{{ formatAmount(item.totalSpend) }}</div>
                <div class="ranking-time">{{ item.lastOrderTime ? item.lastOrderTime.slice(0, 10) : '-' }}</div>
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
import { getSpendingRanking } from '@/api/customers';
import OrderCalendar from '@/views/user/orders/OrderCalendar.vue';
import { Plus, UploadFilled, UserFilled, Money, MagicStick, List } from '@element-plus/icons-vue';

const userStore = useUserStore();
const orderStatus = ref('在线接单');
const orderStatusTag = ref('success');
const todayStats = ref<any>({});
const todos = ref<any[]>([]);
const spendingRanking = ref<any[]>([]);
const showTodoDialog = ref(false);
const newTodoTitle = ref('');
const router = useRouter();

onMounted(async () => {
  try {
    const [profileRes, statsRes, todoRes, rankRes] = await Promise.all([
      getProfile(),
      getDashboardStats(),
      getTodos(0),
      getSpendingRanking(),
    ]);
    const profile: any = profileRes;
    const statusMap: Record<number, string> = { 1: '在线接单', 2: '休息中', 3: '通宵接单', 4: '仅熟客' };
    orderStatus.value = statusMap[profile.data?.orderStatus] || '在线接单';
    todayStats.value = (statsRes as any).data || {};
    todos.value = (todoRes as any).data || [];
    spendingRanking.value = (rankRes as any).data || [];
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

function goToCustomerDetail(item: any) {
  router.push(`/customers/${item.customerId}`);
}

function formatAmount(amount: any): string {
  if (amount === null || amount === undefined) return '0.00';
  return Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
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
.todo-card, .ranking-card {
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

/* ===== 消费排行榜 ===== */
.ranking-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid rgba(232, 130, 154, 0.06);
  transition: background 0.2s ease;
}

.ranking-item:last-child {
  border-bottom: none;
}

.ranking-item:hover {
  background: rgba(232, 130, 154, 0.03);
}

.ranking-item-top3 .ranking-badge {
  font-weight: 700;
}

.ranking-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.ranking-badge {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
  color: #A890B0;
  background: rgba(200, 180, 210, 0.15);
}

.ranking-badge-1 {
  color: #d4a017;
  background: rgba(212, 160, 23, 0.12);
}

.ranking-badge-2 {
  color: #8a8a8a;
  background: rgba(138, 138, 138, 0.12);
}

.ranking-badge-3 {
  color: #cd7f32;
  background: rgba(205, 127, 50, 0.12);
}

.ranking-info {
  min-width: 0;
}

.ranking-name {
  font-size: 14px;
  font-weight: 500;
  color: #5D4E6D;
}

.ranking-meta {
  font-size: 11px;
  color: #A890B0;
  margin-top: 2px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.ranking-sep {
  color: #d0c0d8;
}

.ranking-right {
  text-align: right;
  flex-shrink: 0;
  margin-left: 12px;
}

.ranking-amount {
  font-size: 15px;
  font-weight: 700;
  color: #E8789A;
  white-space: nowrap;
}

.ranking-time {
  font-size: 11px;
  color: #C4B0CC;
  margin-top: 1px;
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
