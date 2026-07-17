<template>
  <div class="order-list">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>♡ 订单管理</span>
          <el-button type="primary" size="small" @click="openCreate">创建订单</el-button>
        </div>
      </template>

      <!-- 筛选 -->
      <el-form :inline="true" class="filter-bar">
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable @change="loadList">
            <el-option :value="1" label="待接单" />
            <el-option :value="2" label="进行中" />
            <el-option :value="3" label="待结算" />
            <el-option :value="4" label="已完结" />
            <el-option :value="5" label="售后退款" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="query.source" placeholder="全部" clearable @change="loadList" style="width:140px">
            <el-option
              v-for="s in orderSources"
              :key="s.id"
              :value="s.id"
              :label="s.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="query.keyword" placeholder="搜索订单" clearable @change="loadList" />
        </el-form-item>
      </el-form>

      <el-table :data="list" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="套餐名称" min-width="130">
          <template #default="{ row }">{{ row.packageName || '-' }}</template>
        </el-table-column>
        <el-table-column label="来源" width="90">
          <template #default="{ row }">{{ getSourceName(row.orderSource) }}</template>
        </el-table-column>
        <el-table-column label="客户" width="120">
          <template #default="{ row }">
            {{ getCustomerName(row.customerId) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时长" width="80">
          <template #default="{ row }">{{ formatDuration(row) }}</template>
        </el-table-column>
        <el-table-column prop="finalAmount" label="金额(元)" width="100" />
        <el-table-column label="支付方式" width="90">
          <template #default="{ row }">{{ getPaymentMethodName(row.paymentMethodId) || row.paymentMethod || '-' }}</template>
        </el-table-column>
        <el-table-column label="到手比例" width="90">
          <template #default="{ row }">{{ row.settleRatio ?? 100 }}%</template>
        </el-table-column>
        <el-table-column label="开始时间" width="140">
          <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="140">
          <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="140">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="$router.push(`/orders/${row.id}`)">详情</el-button>
            <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current="query.current"
        v-model:page-size="query.size"
        :total="total"
        layout="prev, pager, next"
        class="mt-16"
        @change="loadList"
      />
    </el-card>

    <!-- 创建/编辑订单弹窗 -->
    <el-dialog v-model="showDialog" :title="editId ? '编辑订单' : '♡ 创建订单'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="客户">
          <el-select
            v-model="form.customerId"
            placeholder="请选择客户"
            filterable
            clearable
            style="width:100%"
            @change="onCustomerChange"
          >
            <el-option
              v-for="c in customerList"
              :key="c.id"
              :label="`${c.nickname} (${c.contact || '无联系方式'})`"
              :value="c.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="来源">
          <el-select v-model="form.orderSource" style="width:100%">
            <el-option
              v-for="s in orderSources"
              :key="s.id"
              :value="s.id"
              :label="s.name"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="日期">
          <el-date-picker
            v-model="selectedDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>

        <el-form-item label="时间段">
          <div class="time-range-row">
            <el-time-picker
              v-model="startTimeVal"
              placeholder="开始"
              value-format="HH:mm:ss"
              style="flex:1"
            />
            <span class="time-sep">至</span>
            <el-time-picker
              v-model="endTimeVal"
              placeholder="结束"
              value-format="HH:mm:ss"
              style="flex:1"
            />
          </div>
        </el-form-item>

        <!-- 时长展示 -->
        <el-form-item v-if="actualMinutes > 0" label="实际时长">
          <span style="color:#666">{{ actualDuration }}</span>
        </el-form-item>
        <el-form-item v-if="actualMinutes > 0" label="计费时长">
          <span style="color:#e8789a;font-weight:bold">{{ billableDuration }}</span>
        </el-form-item>

        <el-form-item label="支付方式">
          <el-select v-model="form.paymentMethodId" clearable style="width:100%">
            <el-option
              v-for="m in paymentMethods"
              :key="m.id"
              :value="m.id"
              :label="m.name"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="套餐">
          <el-select
            v-model="form.packageId"
            placeholder="选套餐自动填价"
            clearable
            style="width:100%"
            @change="onPackageChange"
          >
            <el-option
              v-for="pkg in packageList"
              :key="pkg.id"
              :label="`${pkg.name} (¥${pkg.price}/${pkg.unit})`"
              :value="pkg.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="单价(元)">
          <el-input-number v-model="form.unitPrice" :precision="2" :min="0" style="width:100%" />
        </el-form-item>

        <el-form-item label="到手比例">
          <el-input-number v-model="form.settleRatio" :min="0" :max="100" :precision="0" style="width:100%">
          </el-input-number>
          <span style="margin-left:4px;color:#999">%</span>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="订单备注" />
        </el-form-item>
        <el-form-item v-if="editingRow && (editingRow.actualMinutes || editingRow.extraMinutes)" label="累计计时">
          <span style="color:#666;font-size:13px">
            原始 {{ formatDuration({ actualMinutes: editingRow.actualMinutes }) }}
            <template v-if="editingRow.extraMinutes"> + 补时 {{ formatDuration({ actualMinutes: editingRow.extraMinutes }) }}</template>
            = <b style="color:#e8789a">{{ formatDuration({ actualMinutes: editingRow.actualMinutes, extraMinutes: editingRow.extraMinutes }) }}</b>
          </span>
        </el-form-item>
        <el-form-item v-if="actualMinutes > 0" label=" ">
          <div class="summary-row">
            <span class="summary-label">费用合计</span>
            <span class="summary-amount">¥{{ rawAmount.toFixed(2) }}</span>
            <span class="summary-parenthesis">（到手比例{{ form.settleRatio }}%：¥{{ estimatedAmount.toFixed(2) }}）</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">{{ editId ? '保存修改' : '♡ 创建订单' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getOrders, createOrder, updateOrder } from '@/api/orders';
import { getCustomerList } from '@/api/customers';
import { getPackages } from '@/api/profile';
import { getEnabledSources } from '@/api/orderSource';
import { getEnabledPaymentMethods } from '@/api/paymentMethod';
import { formatDateTime, formatDuration } from '@/utils/format';

/* ===== 类型 ===== */
interface CustomerItem { id: number; nickname: string; contact: string; sourceId?: number }
interface PackageItem { id: number; name: string; price: number; unit: string }
interface SourceItem { id: number; name: string }
interface PaymentMethodItem { id: number; name: string }

/* ===== 状态 ===== */
const list = ref<any[]>([]);
const total = ref(0);
const showDialog = ref(false);
const editId = ref<number | null>(null);
const editingRow = ref<any>(null);
const saving = ref(false);
const customerList = ref<CustomerItem[]>([]);
const packageList = ref<PackageItem[]>([]);
const orderSources = ref<SourceItem[]>([]);
const paymentMethods = ref<PaymentMethodItem[]>([]);
const paymentMethodMap = ref<Record<number, string>>({});
const selectedDate = ref<string | null>(null);
const startTimeVal = ref<string | null>(null);
const endTimeVal = ref<string | null>(null);

const query = reactive({
  status: undefined as number | undefined,
  source: undefined as number | undefined,
  keyword: '',
  current: 1,
  size: 20,
});

const form = reactive({
  orderSource: 1,
  paymentMethodId: undefined as number | undefined,
  packageId: undefined as number | undefined,
  unitPrice: 0,
  settleRatio: 100,
  customerId: undefined as number | undefined,
  remark: '',
});

/** 计费规则：超过15min算0.5h，超过45min算1h */
function calcBillableHours(minutes: number): number {
  const hours = Math.floor(minutes / 60);
  const remainder = minutes % 60;
  let extra = 0;
  if (remainder > 45) extra = 1;
  else if (remainder > 15) extra = 0.5;
  return hours + extra;
}

/** 实际分钟数（支持跨零点） */
const actualMinutes = computed(() => {
  if (!selectedDate.value || !startTimeVal.value || !endTimeVal.value) return 0;
  const [sh, sm] = startTimeVal.value.split(':').map(Number);
  const [eh, em] = endTimeVal.value.split(':').map(Number);
  let diff = (eh * 60 + em) - (sh * 60 + sm);
  if (diff < 0) diff += 1440; // 跨零点加24h
  return diff;
});

/** 实际时长显示 */
const actualDuration = computed(() => {
  const m = actualMinutes.value;
  if (m === 0) return '-';
  const h = Math.floor(m / 60);
  const r = m % 60;
  if (h === 0) return `${r}min`;
  if (r === 0) return `${h}h`;
  return `${h}h${r}min`;
});

/** 计费时长（按规则取整后） */
const billableDuration = computed(() => {
  const h = calcBillableHours(actualMinutes.value);
  if (h === 0) return '-';
  const hours = Math.floor(h);
  const mins = Math.round((h - hours) * 60);
  if (hours === 0) return `${mins}min`;
  if (mins === 0) return `${hours}h`;
  return `${hours}h${mins}min`;
});

/** 原始费用（不计到手比例） */
const rawAmount = computed(() => {
  if (actualMinutes.value <= 0) return 0;
  return calcBillableHours(actualMinutes.value) * (form.unitPrice || 0);
});

/** 预估费用（含到手比例） */
const estimatedAmount = computed(() => {
  if (actualMinutes.value <= 0) return 0;
  const ratio = (form.settleRatio ?? 100) / 100;
  return rawAmount.value * ratio;
});

/* ===== 支付方式名映射 ===== */
function getPaymentMethodName(id: number | undefined | null): string {
  if (!id) return '';
  return paymentMethodMap.value[id] || '';
}

/* ===== 客户名映射 ===== */
const customerMap = ref<Record<number, string>>({});
function getCustomerName(id: number | undefined | null): string {
  if (!id) return '';
  return customerMap.value[id] || `ID:${id}`;
}

/* ===== 来源名映射 ===== */
const sourceMap = ref<Record<number, string>>({});
function getSourceName(id: number | undefined | null): string {
  if (!id) return '-';
  return sourceMap.value[id] || `ID:${id}`;
}

/* ===== 客户选择 → 自动锁定来源 ===== */
function onCustomerChange(val: number | undefined) {
  if (!val) return;
  const c = customerList.value.find(c => c.id === val);
  if (c && c.sourceId) {
    form.orderSource = c.sourceId;
  }
}

/* ===== 套餐选择 ===== */
function onPackageChange(val: number | undefined) {
  if (!val) return;
  const pkg = packageList.value.find(p => p.id === val);
  if (pkg) {
    form.unitPrice = pkg.price;
  }
}

/* ===== 加载数据 ===== */
onMounted(async () => {
  await Promise.all([loadList(), loadCustomers(), loadPackages(), loadSources(), loadPaymentMethods()]);
});

async function loadList() {
  const res: any = await getOrders(query);
  list.value = res.data?.records || [];
  total.value = res.data?.total || 0;
}

async function loadCustomers() {
  try {
    const res: any = await getCustomerList();
    const items: CustomerItem[] = res.data || [];
    customerList.value = items;
    items.forEach(c => { customerMap.value[c.id] = c.nickname; });
  } catch { /* ignore */ }
}

async function loadPackages() {
  try {
    const res: any = await getPackages();
    packageList.value = (res.data || []).filter((p: any) => p.status === 1);
  } catch { /* ignore */ }
}

async function loadSources() {
  try {
    const res: any = await getEnabledSources();
    orderSources.value = res.data || [];
    orderSources.value.forEach(s => { sourceMap.value[s.id] = s.name; });
  } catch { /* ignore */ }
}

async function loadPaymentMethods() {
  try {
    const res: any = await getEnabledPaymentMethods();
    paymentMethods.value = res.data || [];
    paymentMethods.value.forEach(m => { paymentMethodMap.value[m.id] = m.name; });
  } catch { /* ignore */ }
}

/* ===== 操作 ===== */
function statusText(s: number) {
  return ['', '待接单', '进行中', '待结算', '已完结', '售后退款'][s] || '';
}
function statusType(s: number) {
  return ['', 'info', 'primary', 'warning', 'success', 'danger'][s] || '';
}

function resetForm() {
  editId.value = null;
  editingRow.value = null;
  form.orderSource = orderSources.value.length > 0 ? orderSources.value[0].id : 1;
  form.paymentMethodId = undefined;
  form.packageId = undefined;
  form.unitPrice = 0;
  form.settleRatio = 100;
  form.customerId = undefined;
  form.remark = '';
  selectedDate.value = null;
  startTimeVal.value = null;
  endTimeVal.value = null;
}

function openCreate() {
  resetForm();
  showDialog.value = true;
}

function openEdit(row: any) {
  editId.value = row.id;
  editingRow.value = row;
  form.orderSource = row.orderSource || (orderSources.value.length > 0 ? orderSources.value[0].id : 1);
  form.paymentMethodId = row.paymentMethodId || undefined;
  form.packageId = row.packageId || undefined;
  form.unitPrice = row.unitPrice || 0;
  form.settleRatio = row.settleRatio ?? 100;
  form.customerId = row.customerId || undefined;
  form.remark = row.remark || '';

  // 回填日期和时分
  if (row.startTime) {
    const dt = new Date(row.startTime);
    selectedDate.value = `${dt.getFullYear()}-${(dt.getMonth() + 1).toString().padStart(2, '0')}-${dt.getDate().toString().padStart(2, '0')}`;
    startTimeVal.value = `${dt.getHours().toString().padStart(2, '0')}:${dt.getMinutes().toString().padStart(2, '0')}:${dt.getSeconds().toString().padStart(2, '0')}`;
  } else {
    selectedDate.value = null;
    startTimeVal.value = null;
  }
  if (row.endTime) {
    const dt = new Date(row.endTime);
    endTimeVal.value = `${dt.getHours().toString().padStart(2, '0')}:${dt.getMinutes().toString().padStart(2, '0')}:${dt.getSeconds().toString().padStart(2, '0')}`;
  } else {
    endTimeVal.value = null;
  }
  showDialog.value = true;
}

async function handleSave() {
  let startTime: string | undefined;
  let endTime: string | undefined;
  if (selectedDate.value && startTimeVal.value && endTimeVal.value) {
    const [sh, sm] = startTimeVal.value.split(':').map(Number);
    const [eh, em] = endTimeVal.value.split(':').map(Number);
    const isOvernight = eh * 60 + em < sh * 60 + sm;

    startTime = `${selectedDate.value}T${startTimeVal.value}`;
    if (isOvernight) {
      // 跨零点，结束日期加1天
      const d = new Date(selectedDate.value);
      d.setDate(d.getDate() + 1);
      const nextDate = `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`;
      endTime = `${nextDate}T${endTimeVal.value}`;
    } else {
      endTime = `${selectedDate.value}T${endTimeVal.value}`;
    }
  }

  const payload: any = {
    orderSource: form.orderSource,
    startTime,
    endTime,
    paymentMethodId: form.paymentMethodId,
    packageId: form.packageId || undefined,
    unitPrice: form.unitPrice,
    settleRatio: form.settleRatio,
    customerId: form.customerId || undefined,
    remark: form.remark,
  };

  saving.value = true;
  try {
    if (editId.value) {
      payload.id = editId.value;
      await updateOrder(payload);
      ElMessage.success('订单已更新');
    } else {
      await createOrder(payload);
      ElMessage.success('♡ 订单创建成功');
    }
    showDialog.value = false;
    loadList();
  } catch {
    // error handled by interceptor
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
.filter-bar { margin-bottom: 16px; }
.mt-16 { margin-top: 16px; }
.time-range-row { display: flex; align-items: center; width: 100%; gap: 0; }
.time-sep { margin: 0 8px; color: #999; flex-shrink: 0; }
.summary-row { text-align: right; padding: 8px 0; border-top: 1px solid #eee; width: 100%; }
.summary-label { color: #999; font-size: 13px; margin-right: 8px; }
.summary-amount { color: #e8789a; font-size: 16px; font-weight: bold; }
.summary-parenthesis { color: #999; font-size: 13px; margin-left: 4px; }
</style>
