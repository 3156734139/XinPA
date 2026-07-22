<template>
  <div class="order-detail">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>订单详情 #{{ order?.orderNo }}</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>
      <div v-if="order">
        <el-descriptions :column="2" border>
          <!-- 基本信息 -->
          <el-descriptions-item label="客户">{{ order.customerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="订单标题">{{ order.title }}</el-descriptions-item>
          <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ getSourceName(order.orderSource) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="实付金额"><strong>{{ order.finalAmount }} 元</strong></el-descriptions-item>

          <!-- 费用明细 -->
          <el-descriptions-item label="费用明细" :span="2">
            <div class="fee-row">
              <span class="fee-amount">¥{{ order.finalAmount }}</span>
              <span class="fee-detail">
                <template v-if="order.unit === '小时'">
                  （单价{{ order.unitPrice }}元/时 × 计费{{ billableHours }}时 到手{{ order.settleRatio }}%
                </template>
                <template v-else>
                  （单价{{ order.unitPrice }}元/{{ order.unit || '次' }} 到手{{ order.settleRatio }}%
                </template>
                <template v-if="vipLabel"> · {{ vipLabel }}</template>
                <template v-if="order.discountAmount > 0"> · 优惠{{ order.discountAmount }}元</template>）
              </span>
            </div>
          </el-descriptions-item>

          <el-descriptions-item label="单价">{{ order.unitPrice }} 元/{{ order.unit || '时' }}</el-descriptions-item>
          <el-descriptions-item label="结算比例">{{ order.settleRatio || 100 }}%</el-descriptions-item>
          <el-descriptions-item label="实际时长">{{ actualMinutesText }}</el-descriptions-item>
          <el-descriptions-item label="时长偏差">
            <span v-if="extraMinutes > 0" style="color:#e6a23c">补时 {{ extraMinutes }} 分钟</span>
            <span v-else-if="extraMinutes < 0" style="color:#67c23a">赠送 {{ Math.abs(extraMinutes) }} 分钟</span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="优惠">{{ order.discountAmount > 0 ? '-' + order.discountAmount + '元' : '无' }}</el-descriptions-item>
          <el-descriptions-item label="订单类型">{{ order.unit === '小时' ? '小时单' : '非小时单' }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatTime(order.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatTime(order.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="结算时间">{{ formatTime(order.settleTime) }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ order.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import dayjs from 'dayjs';
import { getOrderDetail } from '@/api/orders';
import { getEnabledSources } from '@/api/orderSource';
import { getVipConfigs } from '@/api/customers';
import { getStatusLabel as statusText, getStatusType as statusType } from '@/types';

const route = useRoute();
const order = ref<any>(null);
const sourceMap = ref<Record<number, string>>({});
const vipConfigs = ref<any[]>([]);

const billableHours = computed(() => {
  if (!order.value?.actualMinutes) return 0;
  const hours = Math.floor(order.value.actualMinutes / 60);
  const remainder = order.value.actualMinutes % 60;
  let extra = 0;
  if (remainder > 45) extra = 1;
  else if (remainder > 15) extra = 0.5;
  return hours + extra;
});

const extraMinutes = computed(() => {
  if (!order.value?.actualMinutes) return 0;
  return billableHours.value * 60 - order.value.actualMinutes;
});

const actualMinutesText = computed(() => {
  if (!order.value?.actualMinutes && order.value?.actualMinutes !== 0) return '-';
  const h = Math.floor(order.value.actualMinutes / 60);
  const m = order.value.actualMinutes % 60;
  if (h > 0 && m > 0) return `${h}小时${m}分`;
  if (h > 0) return `${h}小时`;
  return `${m}分钟`;
});

/** 根据订单的折扣信息反推VIP等级描述 */
const vipLabel = computed(() => {
  if (!order.value || !vipConfigs.value.length || !order.value.discountAmount || order.value.discountAmount <= 0) return '';
  const subtotal = (order.value.unitPrice || 0) * billableHours.value;
  const ratio = (order.value.settleRatio || 100) / 100;
  const afterRatio = subtotal * ratio;
  if (afterRatio <= 0) return '';
  // 计算实际折扣率：discount = afterRatio * (100 - rate) / 100 → rate = 100 - discount * 100 / afterRatio
  const rate = Math.round(100 - (order.value.discountAmount / afterRatio) * 100);
  const matched = vipConfigs.value.find((v: any) => v.discount === rate);
  return matched ? `${matched.name}打${matched.discount}折` : '';
});

onMounted(async () => {
  const [res, srcRes, vipRes] = await Promise.all([
    getOrderDetail(Number(route.params.id)),
    getEnabledSources().catch(() => ({ data: [] })),
    getVipConfigs().catch(() => ({ data: [] })),
  ]);
  order.value = res.data;
  const sources: any[] = srcRes.data || [];
  sources.forEach(s => { sourceMap.value[s.id] = s.name; });
  vipConfigs.value = (vipRes as any)?.data || [];
});

function getSourceName(id: number | null | undefined): string {
  if (!id) return '-';
  return sourceMap.value[id] || `来源#${id}`;
}

function formatTime(dt: string): string {
  if (!dt) return '-';
  return dayjs(dt).format('YYYY-MM-DD HH:mm');
}
</script>

<style scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
.fee-row { display: flex; align-items: baseline; flex-wrap: wrap; gap: 4px; }
.fee-amount { color: #e8789a; font-size: 18px; font-weight: bold; flex-shrink: 0; }
.fee-detail { color: #999; font-size: 13px; white-space: nowrap; }
</style>
