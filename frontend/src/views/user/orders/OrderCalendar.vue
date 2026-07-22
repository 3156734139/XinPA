<template>
  <div class="order-calendar">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <div class="header-left">
            <span class="header-title">♡ 接单日历</span>
            <el-button v-if="!isCurrentWeek" text size="small" type="primary" @click="goToCurrentWeek">回到本周</el-button>
          </div>
          <div class="header-right">
            <span class="calendar-range">{{ weekRangeText }}</span>
            <div class="nav-buttons">
              <el-button text size="small" @click="prevWeek">&lt; 上一周</el-button>
              <el-button text size="small" @click="nextWeek">下一周 &gt;</el-button>
            </div>
          </div>
        </div>
      </template>
      <div class="week-grid">
        <div
          v-for="day in weekDays"
          :key="day.date"
          class="day-column"
          :class="{ 'is-today': day.isToday, 'is-empty': day.orders.length === 0 }"
        >
          <div class="day-head">
            <div class="day-name">{{ day.label }}</div>
            <div class="day-date" :class="{ 'today-text': day.isToday }">{{ day.dateStr }}</div>
          </div>
          <div class="day-body">
            <el-popover
              v-for="order in day.orders"
              :key="order.id"
              placement="right"
              :width="240"
              trigger="hover"
              :hide-after="0"
            >
              <template #reference>
                <div class="order-item" @click="$router.push(`/orders/${order.id}`)">
                  <div class="order-header">
                    <span class="order-customer">{{ customerMap[order.customerId] || `客户${order.customerId}` }}</span>
                    <el-tag v-if="order.status === 4" type="success" size="small" class="order-badge">已完结</el-tag>
                    <el-tag v-else :type="statusType(order.status)" size="small">{{ statusLabel(order.status) }}</el-tag>
                  </div>
                  <div class="order-meta">
                    <span class="order-package">{{ order.packageName || '无套餐' }}</span>
                    <span class="order-time">{{ formatTime(order.startTime) }}-{{ formatTime(order.endTime) }}</span>
                  </div>
                </div>
              </template>
              <div class="popover-body">
                <div class="popover-row"><span class="popover-label">客户</span><span class="popover-value">{{ customerMap[order.customerId] || `客户${order.customerId}` }}</span></div>
                <div class="popover-row"><span class="popover-label">套餐</span><span class="popover-value">{{ order.packageName || '无套餐' }}</span></div>
                <div class="popover-row"><span class="popover-label">时间</span><span class="popover-value">{{ formatTime(order.startTime) }}-{{ formatTime(order.endTime) }}</span></div>
                <div class="popover-row"><span class="popover-label">金额</span><span class="popover-value">¥{{ order.finalAmount || '0.00' }}</span></div>
                <div class="popover-row"><span class="popover-label">时长</span><span class="popover-value">{{ formatDuration(order.actualMinutes) }}</span></div>
                <div class="popover-row"><span class="popover-label">状态</span><span class="popover-value">{{ statusLabel(order.status) }}</span></div>
                <div class="popover-row popover-remark" v-if="order.remark">{{ order.remark }}</div>
              </div>
            </el-popover>
            <div v-if="day.orders.length === 0" class="no-order">-</div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { getOrders } from '@/api/orders';
import { getCustomerList } from '@/api/customers';
import { getStatusLabel as statusLabel, getStatusType as statusType } from '@/types';

const orders = ref<any[]>([]);
const customerMap = ref<Record<number, string>>({});

const DAY_NAMES = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];

function getMonday(d: Date): Date {
  const dayOfWeek = d.getDay();
  const offset = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;
  const monday = new Date(d);
  monday.setDate(d.getDate() + offset);
  monday.setHours(0, 0, 0, 0);
  return monday;
}

function formatDate(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

const today = new Date();
const currentMonday = ref<Date>(getMonday(today));

const isCurrentWeek = computed(() => {
  return currentMonday.value.getTime() === getMonday(today).getTime();
});

const weekDays = computed(() => {
  const monday = currentMonday.value;

  const days: Array<{
    date: string;
    label: string;
    dateStr: string;
    isToday: boolean;
    orders: any[];
  }> = [];

  for (let i = 0; i < 7; i++) {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    const dateStr = formatDate(d);
    const isToday = dateStr === formatDate(today);

    days.push({
      date: dateStr,
      label: DAY_NAMES[i],
      dateStr: `${d.getMonth() + 1}/${d.getDate()}`,
      isToday,
      orders: [],
    });
  }

  // 把订单分配到对应日期
  for (const o of orders.value) {
    const day = o.startTime?.substring(0, 10);
    const found = days.find(d => d.date === day);
    if (found) {
      found.orders.push(o);
    }
  }

  return days;
});

const weekRangeText = computed(() => {
  if (weekDays.value.length === 0) return '';
  return `${weekDays.value[0].dateStr} - ${weekDays.value[6].dateStr}`;
});

function prevWeek() {
  const d = new Date(currentMonday.value);
  d.setDate(d.getDate() - 7);
  currentMonday.value = d;
  loadOrders();
}

function nextWeek() {
  const d = new Date(currentMonday.value);
  d.setDate(d.getDate() + 7);
  currentMonday.value = d;
  loadOrders();
}

function goToCurrentWeek() {
  currentMonday.value = getMonday(today);
  loadOrders();
}

onMounted(() => {
  loadOrders();
  loadCustomers();
});

async function loadOrders() {
  const monday = currentMonday.value;
  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);

  const startDate = formatDate(monday);
  const endDate = formatDate(sunday);

  const res: any = await getOrders({ startTimeStart: startDate, startTimeEnd: endDate, current: 1, size: 100 });
  orders.value = res.data?.records || [];
}

async function loadCustomers() {
  try {
    const res: any = await getCustomerList();
    const list = res.data || [];
    list.forEach((c: any) => { customerMap.value[c.id] = c.nickname; });
  } catch {
    console.warn('加载客户列表失败');
  }
}

function formatTime(dt: string): string {
  if (!dt) return '';
  return dt.substring(11, 16);
}

function formatDuration(minutes: number): string {
  if (!minutes && minutes !== 0) return '';
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  if (h > 0 && m > 0) return `${h}小时${m}分`;
  if (h > 0) return `${h}小时`;
  return `${m}分钟`;
}

// statusLabel / statusType 已迁移至 @/types
</script>

<style scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.header-title {
  font-weight: 600;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.nav-buttons {
  display: flex;
  gap: 2px;
}
.calendar-range {
  font-size: 13px;
  color: #A890B0;
  font-weight: normal;
}
.week-grid {
  display: flex;
  gap: 6px;
  min-height: 120px;
}
.day-column {
  flex: 1;
  min-width: 0;
  background: rgba(232, 130, 154, 0.03);
  border-radius: 8px;
  border: 1px solid rgba(232, 130, 154, 0.08);
  display: flex;
  flex-direction: column;
}
.day-column.is-today {
  background: rgba(232, 130, 154, 0.06);
  border-color: rgba(232, 130, 154, 0.2);
}
.day-head {
  text-align: center;
  padding: 8px 4px 6px;
  border-bottom: 1px solid rgba(232, 130, 154, 0.08);
}
.day-name {
  font-size: 12px;
  color: #A890B0;
  font-weight: 500;
}
.day-date {
  font-size: 14px;
  color: #5D4E6D;
  font-weight: 600;
  margin-top: 2px;
}
.today-text {
  color: #E8789A;
}
.day-body {
  flex: 1;
  padding: 6px 4px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.order-item {
  background: #fff;
  border: 1px solid rgba(232, 130, 154, 0.12);
  border-radius: 6px;
  padding: 5px 7px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.order-item:hover {
  box-shadow: 0 1px 4px rgba(232, 130, 154, 0.15);
}
.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
}
.order-customer {
  font-size: 12px;
  font-weight: 600;
  color: #5D4E6D;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}
.order-badge {
  flex-shrink: 0;
}
.order-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
  margin-top: 3px;
}
.order-package {
  font-size: 11px;
  color: #E8789A;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}
.order-time {
  font-size: 11px;
  color: #A890B0;
  flex-shrink: 0;
}
.no-order {
  text-align: center;
  color: #D4C8DC;
  font-size: 14px;
  padding: 8px 0;
}

/* ===== 悬停弹窗 ===== */
.popover-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.popover-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
.popover-label {
  color: #A890B0;
  width: 36px;
  flex-shrink: 0;
}
.popover-value {
  color: #5D4E6D;
  font-weight: 500;
}
.popover-remark {
  margin-top: 4px;
  padding-top: 8px;
  border-top: 1px solid rgba(232, 130, 154, 0.1);
  color: #888;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
}

@media (max-width: 900px) {
  .week-grid {
    flex-wrap: wrap;
  }
  .day-column {
    flex: 0 0 calc(100% / 3 - 4px);
    margin-bottom: 6px;
  }
}

@media (max-width: 600px) {
  .day-column {
    flex: 0 0 calc(50% - 3px);
  }
}
</style>
