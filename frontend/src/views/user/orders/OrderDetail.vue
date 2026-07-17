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
            <span class="calc-chain">{{ order.unitPrice }}元/时 × {{ billableHours }}时<template v-if="order.settleRatio && order.settleRatio < 100"> (到手{{ order.settleRatio }}%)</template><template v-if="order.discountAmount > 0"> - 优惠{{ order.discountAmount }}元</template> = <strong>{{ order.finalAmount }}元</strong></span>
          </el-descriptions-item>

          <el-descriptions-item label="单价">{{ order.unitPrice }} 元/时</el-descriptions-item>
          <el-descriptions-item label="结算比例">{{ order.settleRatio || 100 }}%</el-descriptions-item>
          <el-descriptions-item label="实际时长">{{ actualMinutesText }}</el-descriptions-item>
          <el-descriptions-item label="时长偏差">
            <span v-if="extraMinutes > 0" style="color:#e6a23c">补时 {{ extraMinutes }} 分钟</span>
            <span v-else-if="extraMinutes < 0" style="color:#67c23a">赠送 {{ Math.abs(extraMinutes) }} 分钟</span>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="优惠">{{ order.discountAmount > 0 ? '-' + order.discountAmount + '元' : '无' }}</el-descriptions-item>
          <el-descriptions-item label="通宵单">{{ order.isOvernight ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="线下单">{{ order.isOffline ? '是' : '否' }}</el-descriptions-item>
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
import { getStatusLabel as statusText, getStatusType as statusType } from '@/types';

const route = useRoute();
const order = ref<any>(null);
const sourceMap = ref<Record<number, string>>({});

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

onMounted(async () => {
  const [res, srcRes] = await Promise.all([
    getOrderDetail(Number(route.params.id)),
    getEnabledSources().catch(() => ({ data: [] })),
  ]);
  order.value = res.data;
  const sources: any[] = srcRes.data || [];
  sources.forEach(s => { sourceMap.value[s.id] = s.name; });
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
.calc-chain { font-size: 14px; color: #5D4E6D; }
</style>
