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
          <el-descriptions-item label="订单标题">{{ order.title }}</el-descriptions-item>
          <el-descriptions-item label="来源">{{ ['', '平台派单', '微信QQ', '线下预约'][order.orderSource] }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(order.status)">{{ statusText(order.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="金额">{{ order.finalAmount }} 元</el-descriptions-item>
          <el-descriptions-item label="单价">{{ order.unitPrice }} 元</el-descriptions-item>
          <el-descriptions-item label="实际计时">{{ order.actualMinutes }} 分钟</el-descriptions-item>
          <el-descriptions-item label="补时">{{ order.extraMinutes }} 分钟</el-descriptions-item>
          <el-descriptions-item label="优惠">{{ order.discountAmount }} 元</el-descriptions-item>
          <el-descriptions-item label="通宵单">{{ order.isOvernight ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="线下单">{{ order.isOffline ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ order.startTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ order.endTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="结算时间">{{ order.settleTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ order.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getOrderDetail } from '@/api/orders';

const route = useRoute();
const order = ref<any>(null);

onMounted(async () => {
  const res: any = await getOrderDetail(Number(route.params.id));
  order.value = res.data;
});

function statusText(s: number) {
  return ['', '待接单', '进行中', '待结算', '已完结', '售后退款'][s] || '';
}
function statusType(s: number) {
  return ['', 'info', 'primary', 'warning', 'success', 'danger'][s] || '';
}
</script>

<style scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
