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
          <el-descriptions-item label="消费水平">{{ ['', '低', '中', '高'][customer.spendLevel] || '-' }}</el-descriptions-item>
          <el-descriptions-item label="累计消费">{{ customer.totalSpend }} 元</el-descriptions-item>
          <el-descriptions-item label="下单次数">{{ customer.orderCount }} 次</el-descriptions-item>
          <el-descriptions-item label="最后下单">{{ customer.lastOrderTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="生日">{{ customer.birthday || '-' }}</el-descriptions-item>
          <el-descriptions-item label="黑名单">
            <el-tag v-if="customer.isBlacklist" type="danger">已拉黑</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="customer.isBlacklist" label="拉黑原因">{{ customer.blacklistReason }}</el-descriptions-item>
          <el-descriptions-item label="性格/雷点" :span="2">{{ customer.personality || '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ customer.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getCustomerDetail } from '@/api/customers';
import { getEnabledSources } from '@/api/orderSource';

const route = useRoute();
const customer = ref<any>(null);
const sourceMap = ref<Record<number, string>>({});

function companionDays(createdAt: string): string {
  const days = Math.floor((Date.now() - new Date(createdAt).getTime()) / 86400000);
  return days === 0 ? '今天' : `${days}天`;
}

function getCustomerSource(c: any): string {
  if (c.sourceId && sourceMap.value[c.sourceId]) {
    return sourceMap.value[c.sourceId];
  }
  return c.source || '-';
}

onMounted(async () => {
  // 并行加载客户详情和来源列表
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
</style>
