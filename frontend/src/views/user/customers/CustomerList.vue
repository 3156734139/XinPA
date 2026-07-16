<template>
  <div class="customer-list">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>客户管理</span>
          <el-button type="primary" size="small" @click="openCreate">添加客户</el-button>
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
          <el-select v-model="filterForm.sourceId" placeholder="全部" clearable @change="search" style="width:140px">
            <el-option
              v-for="s in orderSources"
              :key="s.id"
              :value="s.id"
              :label="s.name"
            />
          </el-select>
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
        <el-table-column label="来源" width="100">
          <template #default="{ row }">{{ row.sourceId ? getSourceName(row.sourceId) : (row.source || '-') }}</template>
        </el-table-column>
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
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="$router.push(`/customers/${row.id}`)">详情</el-button>
            <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
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

    <!-- 创建/编辑客户弹窗 -->
    <el-dialog v-model="showDialog" :title="editId ? '编辑客户' : '添加客户'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="form.contact" placeholder="微信/QQ" />
        </el-form-item>
        <el-form-item label="来源渠道">
          <el-select v-model="form.sourceId" placeholder="选择来源" clearable style="width:100%">
            <el-option
              v-for="s in orderSources"
              :key="s.id"
              :value="s.id"
              :label="s.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">{{ editId ? '保存' : '添加' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getCustomers, createCustomer, updateCustomer, addBlacklist as apiBlacklist, removeBlacklist as apiRemove } from '@/api/customers';
import { getEnabledSources } from '@/api/orderSource';

const list = ref<any[]>([]);
const total = ref(0);
const current = ref(1);
const size = ref(20);
const showDialog = ref(false);
const editId = ref<number | null>(null);
const saving = ref(false);
const orderSources = ref<any[]>([]);
const sourceMap = ref<Record<number, string>>({});
const form = reactive({ nickname: '', contact: '', sourceId: undefined as number | undefined, remark: '' });

const filterForm = reactive({
  nickname: '',
  contact: '',
  sourceId: undefined as number | undefined,
  spendLevel: undefined as number | undefined,
  minSpend: undefined as number | undefined,
  maxSpend: undefined as number | undefined,
  minOrders: undefined as number | undefined,
  maxOrders: undefined as number | undefined,
  blacklist: undefined as number | undefined,
});

function getSourceName(id: number): string {
  return sourceMap.value[id] || `ID:${id}`;
}

function companionDays(createdAt: string): string {
  const days = Math.floor((Date.now() - new Date(createdAt).getTime()) / 86400000);
  return days === 0 ? '今天' : `${days}天`;
}

onMounted(async () => {
  await Promise.all([loadList(), loadSources()]);
});

async function loadSources() {
  try {
    const res: any = await getEnabledSources();
    orderSources.value = res.data || [];
    orderSources.value.forEach(s => { sourceMap.value[s.id] = s.name; });
  } catch { /* ignore */ }
}

async function loadList() {
  const params: any = { current: current.value, size: size.value };
  if (filterForm.nickname) params.nickname = filterForm.nickname;
  if (filterForm.contact) params.contact = filterForm.contact;
  if (filterForm.sourceId !== undefined) params.sourceId = filterForm.sourceId;
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
  filterForm.sourceId = undefined;
  filterForm.spendLevel = undefined;
  filterForm.minSpend = undefined;
  filterForm.maxSpend = undefined;
  filterForm.minOrders = undefined;
  filterForm.maxOrders = undefined;
  filterForm.blacklist = undefined;
  search();
}

function resetForm() {
  editId.value = null;
  form.nickname = '';
  form.contact = '';
  form.sourceId = undefined;
  form.remark = '';
}

function openCreate() {
  resetForm();
  showDialog.value = true;
}

function openEdit(row: any) {
  editId.value = row.id;
  form.nickname = row.nickname || '';
  form.contact = row.contact || '';
  form.sourceId = row.sourceId || undefined;
  form.remark = row.remark || '';
  showDialog.value = true;
}

async function handleSave() {
  saving.value = true;
  try {
    if (editId.value) {
      await updateCustomer({ id: editId.value, ...form });
      ElMessage.success('客户已更新');
    } else {
      await createCustomer(form);
      ElMessage.success('添加成功');
    }
    showDialog.value = false;
    current.value = 1;
    loadList();
  } finally {
    saving.value = false;
  }
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
