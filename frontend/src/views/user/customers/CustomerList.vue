<template>
  <div class="customer-list">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>客户管理</span>
          <el-button type="primary" size="small" @click="showCreate = true">添加客户</el-button>
        </div>
      </template>

      <!-- 多维度筛选 -->
      <el-form :inline="true" class="filter-bar" :model="filterForm">
        <el-form-item label="昵称">
          <el-input v-model="filterForm.nickname" placeholder="昵称" clearable @change="search" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="filterForm.contact" placeholder="联系方式" clearable @change="search" />
        </el-form-item>
        <el-form-item label="来源">
          <el-input v-model="filterForm.source" placeholder="来源渠道" clearable @change="search" />
        </el-form-item>
        <el-form-item label="消费等级">
          <el-select v-model="filterForm.spendLevel" placeholder="全部" clearable @change="search">
            <el-option :value="1" label="低" />
            <el-option :value="2" label="中" />
            <el-option :value="3" label="高" />
          </el-select>
        </el-form-item>
        <el-form-item label="累计消费">
          <el-input-number v-model="filterForm.minSpend" :min="0" :precision="2" placeholder="最低" style="width:110px" controls-position="right" />
          <span class="range-sep">—</span>
          <el-input-number v-model="filterForm.maxSpend" :min="0" :precision="2" placeholder="最高" style="width:110px" controls-position="right" />
        </el-form-item>
        <el-form-item label="下单次数">
          <el-input-number v-model="filterForm.minOrders" :min="0" placeholder="最少" style="width:100px" controls-position="right" />
          <span class="range-sep">—</span>
          <el-input-number v-model="filterForm.maxOrders" :min="0" placeholder="最多" style="width:100px" controls-position="right" />
        </el-form-item>
        <el-form-item label="黑名单">
          <el-select v-model="filterForm.blacklist" placeholder="全部" clearable @change="search">
            <el-option :value="1" label="黑名单" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" stripe>
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column label="陪伴天数" width="100">
          <template #default="{ row }">
            {{ row.createdAt ? companionDays(row.createdAt) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="contact" label="联系方式" width="160" />
        <el-table-column prop="source" label="来源" width="100" />
        <el-table-column label="消费等级" width="100">
          <template #default="{ row }">{{ ['', '低', '中', '高'][row.spendLevel] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="totalSpend" label="累计消费" width="120" />
        <el-table-column prop="orderCount" label="下单次数" width="100" />
        <el-table-column label="黑名单" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isBlacklist" type="danger" size="small">黑名单</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="$router.push(`/customers/${row.id}`)">详情</el-button>
            <el-button v-if="!row.isBlacklist" size="small" link type="danger" @click="blacklist(row.id)">拉黑</el-button>
            <el-button v-else size="small" link type="success" @click="removeBlacklist(row.id)">移出</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current="current"
        v-model:page-size="size"
        :total="total"
        layout="prev, pager, next"
        class="mt-16"
        @change="loadList"
      />
    </el-card>

    <el-dialog v-model="showCreate" title="添加客户" width="500px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="昵称">
          <el-input v-model="createForm.nickname" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="createForm.contact" placeholder="微信/QQ" />
        </el-form-item>
        <el-form-item label="来源渠道">
          <el-input v-model="createForm.source" placeholder="如: 平台/朋友推荐" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getCustomers, createCustomer, addBlacklist as apiBlacklist, removeBlacklist as apiRemove } from '@/api/customers';

const list = ref<any[]>([]);
const total = ref(0);
const current = ref(1);
const size = ref(20);
const showCreate = ref(false);
const createForm = ref<any>({ nickname: '', contact: '', source: '', remark: '' });

const filterForm = reactive({
  nickname: '',
  contact: '',
  source: '',
  spendLevel: undefined as number | undefined,
  minSpend: undefined as number | undefined,
  maxSpend: undefined as number | undefined,
  minOrders: undefined as number | undefined,
  maxOrders: undefined as number | undefined,
  blacklist: undefined as number | undefined,
});

function companionDays(createdAt: string): string {
  const days = Math.floor((Date.now() - new Date(createdAt).getTime()) / 86400000);
  return days === 0 ? '今天' : `${days}天`;
}

onMounted(() => { loadList(); });

async function loadList() {
  const params: any = { current: current.value, size: size.value };
  // 只传有值的筛选条件
  if (filterForm.nickname) params.nickname = filterForm.nickname;
  if (filterForm.contact) params.contact = filterForm.contact;
  if (filterForm.source) params.source = filterForm.source;
  if (filterForm.spendLevel !== undefined) params.spendLevel = filterForm.spendLevel;
  if (filterForm.minSpend !== undefined) params.minSpend = filterForm.minSpend;
  if (filterForm.maxSpend !== undefined) params.maxSpend = filterForm.maxSpend;
  if (filterForm.minOrders !== undefined) params.minOrders = filterForm.minOrders;
  if (filterForm.maxOrders !== undefined) params.maxOrders = filterForm.maxOrders;
  if (filterForm.blacklist !== undefined) params.blacklist = filterForm.blacklist;

  const res: any = await getCustomers(params);
  list.value = res.data?.records || [];
  total.value = res.data?.total || 0;
}

function search() {
  current.value = 1;
  loadList();
}

function resetFilter() {
  filterForm.nickname = '';
  filterForm.contact = '';
  filterForm.source = '';
  filterForm.spendLevel = undefined;
  filterForm.minSpend = undefined;
  filterForm.maxSpend = undefined;
  filterForm.minOrders = undefined;
  filterForm.maxOrders = undefined;
  filterForm.blacklist = undefined;
  search();
}

async function handleCreate() {
  await createCustomer(createForm.value);
  ElMessage.success('添加成功');
  showCreate.value = false;
  current.value = 1; // 重置到第一页，新客户可见
  loadList();
}

async function blacklist(id: number) {
  try {
    const { value } = await ElMessageBox.prompt('请输入拉黑原因', '拉黑确认');
    await apiBlacklist(id, value);
    ElMessage.success('已拉黑');
    loadList();
  } catch {}
}

async function removeBlacklist(id: number) {
  await apiRemove(id);
  ElMessage.success('已移出黑名单');
  loadList();
}
</script>

<style scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
.filter-bar { margin-bottom: 16px; }
.filter-bar .el-form-item { margin-bottom: 8px; }
.range-sep { display: inline-block; margin: 0 4px; color: #C4B0CC; }
.mt-16 { margin-top: 16px; }
</style>
