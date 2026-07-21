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
            <div class="welcome-summary">
              <span class="summary-text">
                今天已接 <b>{{ todayStats.todayOrders ?? 0 }}</b> 单，
                入账 <b>¥{{ todayStats.todayIncome || '0.00' }}</b>，
                {{ todayStats.todayOrders > 0 ? '状态不错哦 ~' : '今天还没有订单哦 ~' }}
              </span>
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
          </div>
        </el-card>
      </el-col>

      <!-- 快捷操作已经在了，不需要统计卡片 -->
    </el-row>

    <!-- AI + 待办/排行榜 等高二列 -->
    <div class="equal-row">
        <!-- AI 助理 -->
        <div class="equal-col">
          <div class="dashboard-chat">
            <ChatView />
          </div>
        </div>

        <!-- 待办事项 & 客户消费排行榜 -->
        <div class="equal-col">
          <div class="right-column">
            <!-- 待办事项 -->
            <el-card shadow="never" class="todo-card">
              <template #header>
                <div class="card-header">
                  <span>待办事项 · 努力工作中 ♡</span>
                  <el-button text size="small" @click="showTodoDialog = true">新建待办</el-button>
                </div>
              </template>
              <div v-if="todos.length === 0" class="empty-state scroll-list">
                <el-empty description="暂无待办事项" :image-size="80" />
              </div>
              <div v-else class="scroll-list">
                <div v-for="(item, index) in todos" :key="item.id" class="todo-item" :style="{ animationDelay: index * 0.08 + 's' }">
                  <el-checkbox :model-value="item.status === 1" @change="toggleTodo(item.id)" size="large">
                    <span :class="{ 'todo-done': item.status === 1 }">{{ item.title }}</span>
                  </el-checkbox>
                </div>
              </div>
            </el-card>

            <!-- 客户消费排行榜 -->
            <el-card shadow="never" class="ranking-card">
              <template #header>
                <div class="card-header">
                  <span>老板消费排行榜 TOP10</span>
                </div>
              </template>
              <div v-if="spendingRanking.length === 0" class="empty-state scroll-list">
                <el-empty description="暂无客户数据" :image-size="60" />
              </div>
              <VChart v-else :option="chartOption" autoresize class="chart-container" />
            </el-card>
          </div>
        </div>
      </div>

    <!-- 预约日历 -->
    <el-row :gutter="20">
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
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/user';
import { getDashboardStats } from '@/api/dashboard';
import { getTodos, createTodo, toggleTodo as apiToggleTodo } from '@/api/todos';
import { getSpendingRanking } from '@/api/customers';
import OrderCalendar from '@/views/user/orders/OrderCalendar.vue';
import ChatView from '@/views/user/agent/ChatView.vue';
import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { BarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { Plus, UploadFilled, UserFilled, Money } from '@element-plus/icons-vue';

use([BarChart, GridComponent, TooltipComponent, CanvasRenderer]);

const userStore = useUserStore();
const todayStats = ref<any>({});
const todos = ref<any[]>([]);
const spendingRanking = ref<any[]>([]);
const showTodoDialog = ref(false);
const newTodoTitle = ref('');
const router = useRouter();

const chartOption = computed(() => {
  const names = [...spendingRanking.value].map(i => i.nickname).reverse();
  const values = [...spendingRanking.value].map(i => Number(i.totalSpend)).reverse();
  return {
    tooltip: {
      trigger: 'axis' as const,
      axisPointer: { type: 'shadow' as const },
      confine: true,
      formatter: (params: any) => {
        const p = params[0];
        const item = spendingRanking.value[spendingRanking.value.length - 1 - p.dataIndex];
        return `<b>${item.nickname}</b><br/>消费金额：¥${Number(item.totalSpend).toLocaleString()}<br/>下单：${item.orderCount}单<br/>占比：${item.percentage}%`;
      },
    },
    grid: { left: 10, right: 100, top: 10, bottom: 10, containLabel: true },
    xAxis: {
      type: 'value' as const,
      axisLabel: {
        formatter: (v: number) => v >= 10000 ? `${(v / 10000).toFixed(1)}w` : `${v}`,
        fontSize: 10,
        color: '#A890B0',
      },
      splitLine: { lineStyle: { color: 'rgba(232,130,154,0.06)' } },
    },
    yAxis: {
      type: 'category' as const,
      data: names,
      axisLabel: { fontSize: 12, fontWeight: 600, color: '#5D4E6D' },
      axisLine: { show: false },
      axisTick: { show: false },
    },
    series: [{
      type: 'bar' as const,
      data: values.map((v: number, i: number) => ({
        value: v,
        itemStyle: {
          borderRadius: [0, 6, 6, 0],
          color: `rgba(232,130,154,${0.4 + (i / values.length) * 0.5})`,
        },
      })),
      barMaxWidth: 22,
      barMinHeight: 4,
      label: {
        show: true,
        position: 'right',
        formatter: (params: any) => {
          const item = spendingRanking.value[spendingRanking.value.length - 1 - params.dataIndex];
          return `¥${Number(item.totalSpend).toLocaleString()}`;
        },
        fontSize: 11,
        color: '#5D4E6D',
      },
    }],
  };
});

onMounted(async () => {
  try {
    const [statsRes, todoRes, rankRes] = await Promise.all([
      getDashboardStats(),
      getTodos(0),
      getSpendingRanking(),
    ]);
    todayStats.value = (statsRes as any).data || {};
    todos.value = (todoRes as any).data || [];
    spendingRanking.value = (rankRes as any).data || [];
  } catch (e) {
    console.warn('Dashboard 数据加载失败', e);
  }
});

async function toggleTodo(id: number) {
  await apiToggleTodo(id);
  const res: any = await getTodos(0);
  todos.value = res.data || [];
}

async function handleCreateTodo() {
  if (!newTodoTitle.value.trim()) return;
  await createTodo({ title: newTodoTitle.value.trim(), todoType: 3 });
  newTodoTitle.value = '';
  showTodoDialog.value = false;
  const res: any = await getTodos(0);
  todos.value = res.data || [];
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

.welcome-summary {
  text-align: right;
}
.summary-text {
  font-size: 14px;
  color: #5D4E6D;
}
.summary-text b {
  color: #E8789A;
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

/* ===== 等高二列布局 ===== */
.equal-row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}
.equal-col {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 400px;
  height: calc(100vh - 360px);
}
.equal-col .dashboard-chat {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  border-radius: 16px;
}
.equal-col .dashboard-chat :deep(.chat-page) {
  border-radius: 0;
}
.equal-col .dashboard-chat :deep(.chat-container) {
  border-radius: 0;
}

/* ===== 右侧列（待办 + 排行榜） ===== */
.right-column {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow: hidden;
}

.right-column .el-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.right-column .el-card :deep(.el-card__body) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.right-column .scroll-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.chart-container {
  flex: 1;
  min-height: 0;
  width: 100%;
  overflow: hidden;
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
