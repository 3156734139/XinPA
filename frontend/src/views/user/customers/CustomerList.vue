<template>
  <div class="customer-list">
    <el-card shadow="never" class="customer-card">
      <template #header>
        <div class="header-bar">
          <span class="header-title">
            <PixelSticker :size="18" style="margin-right:4px" />
            客户管理
          </span>
          <el-button size="small" type="primary" @click="openCreate" class="btn-add-customer">添加客户</el-button>
        </div>
      </template>

      <!-- 筛选 -->
      <el-form :inline="true" class="filter-bar" style="display:flex;align-items:flex-start;flex-wrap:wrap">
        <div style="display:flex;align-items:center;flex-wrap:wrap;gap:8px;flex:1;min-width:0">
          <!-- 基础筛选项 -->
          <el-form-item>
            <el-input v-model="filterForm.keyword" placeholder="搜索昵称/联系方式" clearable style="width:180px" @keyup.enter="search" />
          </el-form-item>
          <el-form-item label="来源">
            <el-select v-model="filterForm.sourceId" placeholder="全部" clearable style="width:120px">
              <el-option
                v-for="s in orderSources"
                :key="s.id"
                :value="s.id"
                :label="s.name"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="优惠等级">
            <el-select v-model="filterForm.spendLevel" placeholder="全部" clearable style="width:130px">
              <el-option
                v-for="v in vipConfigs"
                :key="v.level"
                :value="v.level"
                :label="v.name"
              />
            </el-select>
          </el-form-item>

          <!-- 右侧：按钮区 -->
          <div style="flex:1;min-width:120px" />
          <el-form-item>
            <el-button size="default" @click="showAdvanced = !showAdvanced">
              <template v-if="showAdvanced">收起高级</template>
              <template v-else>高级筛选</template>
            </el-button>
            <el-button type="primary" @click="search">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </div>

        <!-- 高级筛选区域 -->
        <div v-show="showAdvanced" style="width:100%;margin-top:4px">
          <el-divider style="margin:4px 0" />
          <div style="display:flex;align-items:center;flex-wrap:wrap;gap:8px;padding:4px 0">
            <el-form-item label="黑名单">
              <el-select v-model="filterForm.blacklist" placeholder="全部" clearable style="width:100px">
                <el-option :value="1" label="黑名单" />
              </el-select>
            </el-form-item>
            <el-form-item label="累计消费(元)">
              <div style="display:flex;align-items:center;gap:4px">
                <el-input-number v-model="filterForm.minSpend" :min="0" :precision="2" :value-on-clear="null" controls-position="right" placeholder="最低" />
                <span style="color:#999">~</span>
                <el-input-number v-model="filterForm.maxSpend" :min="0" :precision="2" :value-on-clear="null" controls-position="right" placeholder="最高" />
              </div>
            </el-form-item>
            <el-form-item label="下单次数">
              <div style="display:flex;align-items:center;gap:4px">
                <el-input-number v-model="filterForm.minOrders" :min="0" :value-on-clear="null" controls-position="right" placeholder="最少" />
                <span style="color:#999">~</span>
                <el-input-number v-model="filterForm.maxOrders" :min="0" :value-on-clear="null" controls-position="right" placeholder="最多" />
              </div>
            </el-form-item>
          </div>
        </div>
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
        <el-table-column prop="orderCount" label="下单次数" width="100" />
        <el-table-column label="累计消费" width="120">
          <template #default="{ row }">
            <el-link :underline="false" @click="showOrderHistory(row)" style="cursor: pointer;color:#E8789A;font-weight:600">
              {{ row.totalSpend ?? '-' }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column label="优惠等级" width="150">
          <template #header>
            <span>
              优惠等级
              <el-tooltip placement="top" trigger="hover">
                <template #content>
                  <div style="font-size:13px;line-height:1.8;white-space:nowrap">
                    <div v-for="v in vipConfigs" :key="v.level">
                      累计消费 ≥ {{ v.threshold }} → {{ v.name }}（{{ v.discount }}折）
                    </div>
                  </div>
                </template>
                <el-icon style="cursor:help;vertical-align:-2px;color:#C4B0CC;font-size:15px">
                  <InfoFilled />
                </el-icon>
              </el-tooltip>
            </span>
          </template>
          <template #default="{ row }">{{ getVipLabel(row.spendLevel) || '-' }}</template>
        </el-table-column>
        <el-table-column label="黑名单" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isBlacklist" size="small">黑名单</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="$router.push(`/customers/${row.id}`)">详情</el-button>
            <el-button size="small" link @click="openEdit(row)">编辑</el-button>
            <el-button v-if="!row.isBlacklist" size="small" link @click="blacklist(row.id)">拉黑</el-button>
            <el-button v-else size="small" link @click="removeBlacklist(row.id)">移出</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current="current"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[5, 10, 20]"
        layout="total, sizes, prev, pager, next"
        class="mt-16"
        @change="loadList"
      />
    </el-card>

    <!-- 创建/编辑客户弹窗 -->
    <el-dialog v-model="showDialog" :title="editId ? '编辑客户' : '添加客户'" width="500px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="客户昵称" />
        </el-form-item>
        <el-form-item label="联系方式" prop="contact">
          <el-input v-model="form.contact" placeholder="微信/QQ" />
        </el-form-item>
        <el-form-item label="来源渠道" prop="sourceId">
          <el-select v-model="form.sourceId" placeholder="选择来源" style="width:100%">
            <el-option
              v-for="s in orderSources"
              :key="s.id"
              :value="s.id"
              :label="s.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="性格/爱好/雷点">
          <el-input v-model="form.personality" placeholder="性格、兴趣爱好、雷点等" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">{{ editId ? '保存' : '添加' }}</el-button>
      </template>
    </el-dialog>

    <!-- 历史订单弹窗 -->
    <el-dialog v-model="showOrders" title="历史订单" width="900px">
      <el-table :data="orderList" stripe v-loading="loadingOrders">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="finalAmount" label="实付金额" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current="orderPage"
        v-model:page-size="orderPageSize"
        :total="orderTotal"
        :page-sizes="[5, 10, 20]"
        layout="total, sizes, prev, pager, next"
        class="mt-16"
        @change="() => loadOrders(customerIdForOrders)"
      />
      <template #footer>
        <el-button @click="showOrders = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { getCustomers, createCustomer, updateCustomer, addBlacklist as apiBlacklist, removeBlacklist as apiRemove, getVipConfigs } from '@/api/customers';
import { getEnabledSources } from '@/api/orderSource';
import { getOrders } from '@/api/orders';
import { formatDateTime } from '@/utils/format';
import { getStatusLabel as statusLabel, getStatusType as statusType } from '@/types';
import PixelSticker from '@/components/PixelSticker.vue';

const list = ref<any[]>([]);
const total = ref(0);
const current = ref(1);
const size = ref(5);
const showDialog = ref(false);
const editId = ref<number | null>(null);
const saving = ref(false);
const showAdvanced = ref(false);
const orderSources = ref<any[]>([]);
const sourceMap = ref<Record<number, string>>({});
const vipConfigs = ref<any[]>([]);
const formRef = ref<FormInstance>();
const form = reactive({ nickname: '', contact: '', sourceId: undefined as number | undefined, personality: '', remark: '' });
const formRules: FormRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  contact: [{ required: true, message: '请输入联系方式', trigger: 'blur' }],
  sourceId: [{ required: true, message: '请选择来源渠道', trigger: 'change' }],
};

// 订单弹窗
const showOrders = ref(false);
const orderList = ref<any[]>([]);
const orderTotal = ref(0);
const orderPage = ref(1);
const orderPageSize = ref(5);
const loadingOrders = ref(false);
const customerIdForOrders = ref(0);

const route = useRoute();

const filterForm = reactive({
  keyword: '',
  sourceId: undefined as number | undefined,
  spendLevel: undefined as number | undefined,
  blacklist: undefined as number | undefined,
  minSpend: undefined as number | undefined,
  maxSpend: undefined as number | undefined,
  minOrders: undefined as number | undefined,
  maxOrders: undefined as number | undefined,
});

function getSourceName(id: number): string {
  return sourceMap.value[id] || `ID:${id}`;
}

function getVipLabel(level: number): string {
  const v = vipConfigs.value.find(c => c.level === level);
  if (!v) return '';
  return v.discount != null && v.discount < 100 ? `${v.name}（${v.discount}折）` : v.name;
}

function companionDays(createdAt: string): string {
  const days = Math.floor((Date.now() - new Date(createdAt).getTime()) / 86400000) + 1;
  return `${days}天`;
}

onMounted(async () => {
  if (route.query.keyword) {
    filterForm.keyword = route.query.keyword as string;
  }
  await Promise.all([loadList(), loadSources(), loadVipConfigs()]);
});

// 监听路由参数变化：点击侧栏 Tab 回到 /customers 时重置筛选条件
watch(() => route.query.keyword, (newVal) => {
  if (newVal) {
    filterForm.keyword = newVal as string;
  } else {
    filterForm.keyword = '';
  }
  current.value = 1;
  loadList();
});

async function loadSources() {
  try {
    const res: any = await getEnabledSources();
    orderSources.value = res.data || [];
    orderSources.value.forEach(s => { sourceMap.value[s.id] = s.name; });
  } catch { /* ignore */ }
}

async function loadVipConfigs() {
  try {
    const res: any = await getVipConfigs();
    vipConfigs.value = res.data || [];
  } catch { /* ignore */ }
}

async function loadList() {
  const params: any = { current: current.value, size: size.value };
  if (filterForm.keyword) params.keyword = filterForm.keyword;
  if (filterForm.sourceId !== undefined) params.sourceId = filterForm.sourceId;
  if (filterForm.spendLevel !== undefined) params.spendLevel = filterForm.spendLevel;
  if (filterForm.blacklist !== undefined) params.blacklist = filterForm.blacklist;
  if (filterForm.minSpend !== undefined) params.minSpend = filterForm.minSpend;
  if (filterForm.maxSpend !== undefined) params.maxSpend = filterForm.maxSpend;
  if (filterForm.minOrders !== undefined) params.minOrders = filterForm.minOrders;
  if (filterForm.maxOrders !== undefined) params.maxOrders = filterForm.maxOrders;

  const res: any = await getCustomers(params);
  list.value = res.data?.records || [];
  total.value = res.data?.total || 0;
}

function search() {
  current.value = 1;
  loadList();
}

function resetFilter() {
  filterForm.keyword = '';
  filterForm.sourceId = undefined;
  filterForm.spendLevel = undefined;
  filterForm.blacklist = undefined;
  filterForm.minSpend = undefined;
  filterForm.maxSpend = undefined;
  filterForm.minOrders = undefined;
  filterForm.maxOrders = undefined;
  search();
}

function resetForm() {
  editId.value = null;
  form.nickname = '';
  form.contact = '';
  form.sourceId = undefined;
  form.personality = '';
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
  form.personality = row.personality || '';
  form.remark = row.remark || '';
  showDialog.value = true;
}

async function showOrderHistory(row: any) {
  showOrders.value = true;
  orderPage.value = 1;
  customerIdForOrders.value = row.id;
  await loadOrders(row.id);
}

async function loadOrders(customerId: number) {
  loadingOrders.value = true;
  try {
    const res: any = await getOrders({
      customerId,
      current: orderPage.value,
      size: orderPageSize.value,
    });
    orderList.value = res.data?.records || [];
    orderTotal.value = res.data?.total || 0;
  } finally {
    loadingOrders.value = false;
  }
}

async function handleSave() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }
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
  } catch {
    ElMessage.error('拉黑失败，请重试');
  }
}

async function removeBlacklist(id: number) {
  await apiRemove(id);
  ElMessage.success('已移出黑名单');
  loadList();
}
</script>

<style scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
.header-title { font-size: 15px; font-weight: 600; color: #5D4E6D; }

/* 卡片入场动画 */
.customer-card {
  animation: slideUp 0.4s ease both;
}

/* 添加客户按钮 */
.btn-add-customer {
  transition: all 0.3s ease !important;
}
.btn-add-customer:hover {
  transform: translateY(-2px) scale(1.03) !important;
  box-shadow: 0 6px 20px rgba(232, 130, 154, 0.2) !important;
}

.filter-bar { margin-bottom: 16px; }
.filter-bar .el-form-item { margin-bottom: 8px; }
.range-sep { display: inline-block; margin: 0 4px; color: #C4B0CC; }
.mt-16 { margin-top: 16px; }

/* 表格行悬停增强 */
:deep(.el-table__body tr) {
  transition: all 0.25s ease;
}
:deep(.el-table__body tr:hover) {
  transform: scale(1.002);
  box-shadow: 0 2px 12px rgba(232, 130, 154, 0.06);
}
</style>
