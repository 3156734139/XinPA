<template>
  <div class="customer-detail">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>客户详情 - {{ customer?.nickname }}</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>
      <div v-if="customer">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="昵称">{{ customer.nickname }}</el-descriptions-item>
          <el-descriptions-item label="联系方式">{{ customer.contact || '-' }}</el-descriptions-item>
          <el-descriptions-item label="来源渠道">{{ getCustomerSource(customer) }}</el-descriptions-item>
          <el-descriptions-item label="陪伴天数">
            {{ customer.createdAt ? companionDays(customer.createdAt) : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="优惠等级">
            <template v-if="customer.spendLevel && customer.spendLevel > 0">
              <el-tag size="small" :style="vipTagStyle(customer)">{{ vipLabel(customer) }}</el-tag>
            </template>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="累计消费">
            <el-link type="primary" @click="showOrderHistory" :underline="false" style="cursor: pointer;">
              {{ customer.totalSpend }} 元
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="下单次数">{{ customer.orderCount }} 次</el-descriptions-item>
          <el-descriptions-item label="最后下单">{{ formatDateTime(customer.lastOrderTime) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="黑名单">
            <el-tag v-if="customer.isBlacklist" type="danger">已拉黑</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="customer.isBlacklist" label="拉黑原因">{{ customer.blacklistReason }}</el-descriptions-item>
          <el-descriptions-item label="性格/雷点" :span="2" label-class-name="no-wrap">{{ customer.personality || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ customer.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- 历史订单弹窗 -->
    <el-dialog v-model="showOrders" title="历史订单" width="900px">
      <el-table :data="orderList" stripe v-loading="loadingOrders">
        <el-table-column prop="orderNo" label="订单号" width="200">
          <template #default="{ row }">
            <el-link type="primary" :underline="false" @click="$router.push(`/orders/${row.id}`)">{{ row.orderNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="finalAmount" label="实付金额" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="orderPage"
        v-model:page-size="orderPageSize"
        :total="orderTotal"
        layout="prev, pager, next"
        class="mt-16"
        @current-change="loadOrders"
      />
      <template #footer>
        <el-button @click="showOrders = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getCustomerDetail } from '@/api/customers';
import { getEnabledSources } from '@/api/orderSource';
import { getOrders } from '@/api/orders';
import { formatDateTime } from '@/utils/format';
import { getStatusLabel as statusLabel, getStatusType as statusType } from '@/types';

const route = useRoute();
const customer = ref<any>(null);
const sourceMap = ref<Record<number, string>>({});

// 订单弹窗
const showOrders = ref(false);
const orderList = ref<any[]>([]);
const orderTotal = ref(0);
const orderPage = ref(1);
const orderPageSize = ref(10);
const loadingOrders = ref(false);

function companionDays(createdAt: string): string {
  const days = Math.floor((Date.now() - new Date(createdAt).getTime()) / 86400000) + 1;
  return `${days}天`;
}

function vipLabel(c: any): string {
  if (c.spendLevelName) {
    return c.spendLevelDiscount != null && c.spendLevelDiscount < 100
      ? `${c.spendLevelName}（${c.spendLevelDiscount}折）`
      : c.spendLevelName;
  }
  return `VIP${c.spendLevel}`;
}

function vipTagStyle(c: any): Record<string, string> {
  const level = c.spendLevel || 1;
  const max = 20;
  const idx = Math.min(level, max);
  const lightness = 92 - (idx - 1) * (42 / (max - 1));
  return {
    backgroundColor: `hsl(340, 70%, ${lightness}%)`,
    borderColor: `hsl(340, 70%, ${lightness - 6}%)`,
    color: lightness < 55 ? '#fff' : `hsl(340, 50%, ${Math.max(lightness - 55, 18)}%)`,
  };
}

function getCustomerSource(c: any): string {
  if (c.sourceId && sourceMap.value[c.sourceId]) {
    return sourceMap.value[c.sourceId];
  }
  return c.source || '-';
}

async function showOrderHistory() {
  showOrders.value = true;
  orderPage.value = 1;
  await loadOrders();
}

async function loadOrders() {
  loadingOrders.value = true;
  try {
    const res: any = await getOrders({
      customerId: customer.value?.id,
      current: orderPage.value,
      size: orderPageSize.value,
    });
    orderList.value = res.data?.records || [];
    orderTotal.value = res.data?.total || 0;
  } finally {
    loadingOrders.value = false;
  }
}

onMounted(async () => {
  const [res, srcRes] = await Promise.all([
    getCustomerDetail(Number(route.params.id)),
    getEnabledSources().catch(() => ({ data: [] })),
  ]);
  customer.value = res.data;
  const sources: any[] = (srcRes as any).data || [];
  sources.forEach(s => { sourceMap.value[s.id] = s.name; });
});
</script>

<style scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
.mt-16 { margin-top: 16px; }
:deep(.no-wrap) { white-space: nowrap; }
</style>
