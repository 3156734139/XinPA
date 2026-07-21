<template>
  <div class="order-list">
    <el-card shadow="never" class="order-card">
      <template #header>
        <div class="header-bar">
          <span class="header-title">
            <PixelSticker :size="18" style="margin-right:6px" />
            订单管理
          </span>
          <el-button size="small" type="primary" @click="openCreate" class="btn-create-order">创建订单</el-button>
        </div>
      </template>

      <!-- 筛选 -->
      <el-form :inline="true" class="filter-bar" style="display:flex;align-items:flex-start;flex-wrap:wrap">
        <div style="display:flex;align-items:center;flex-wrap:wrap;gap:8px;flex:1;min-width:0">
          <!-- 基础筛选项 -->
          <el-form-item>
            <el-input v-model="query.keyword" placeholder="搜索订单号/套餐名称" clearable style="width:200px" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="客户">
            <el-select v-model="query.customerId" placeholder="全部" clearable filterable style="width:140px">
              <el-option
                v-for="c in customerList"
                :key="c.id"
                :value="c.id"
                :label="`${c.nickname}${c.contact ? ' (' + c.contact + ')' : ''}`"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="来源">
            <el-select v-model="query.source" placeholder="全部" clearable style="width:120px">
              <el-option
                v-for="s in orderSources"
                :key="s.id"
                :value="s.id"
                :label="s.name"
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
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </div>

        <!-- 高级筛选区域 -->
        <div v-show="showAdvanced" style="width:100%;margin-top:4px">
          <el-divider style="margin:4px 0" />
          <div style="display:flex;align-items:center;flex-wrap:wrap;gap:8px;padding:4px 0">
            <el-form-item label="创建时间">
              <el-date-picker
                v-model="query.dateRange"
               
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width:230px"
              />
            </el-form-item>
            <el-form-item label="金额(元)">
              <div class="range-input">
                <el-input-number v-model="query.minAmount" :min="0" :precision="2" :value-on-clear="null" controls-position="right" placeholder="最低" />
                <span class="range-sep">~</span>
                <el-input-number v-model="query.maxAmount" :min="0" :precision="2" :value-on-clear="null" controls-position="right" placeholder="最高" />
              </div>
            </el-form-item>
            <el-form-item label="时长(分)">
              <div class="range-input">
                <el-input-number v-model="query.minMinutes" :min="0" :precision="0" :value-on-clear="null" controls-position="right" placeholder="最少" />
                <span class="range-sep">~</span>
                <el-input-number v-model="query.maxMinutes" :min="0" :precision="0" :value-on-clear="null" controls-position="right" placeholder="最多" />
              </div>
            </el-form-item>
          </div>
        </div>
      </el-form>

      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column label="套餐名称" min-width="130">
          <template #default="{ row }">{{ row.packageName || '-' }}</template>
        </el-table-column>
        <el-table-column label="来源" width="90">
          <template #default="{ row }">{{ getSourceName(row.orderSource) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" :size="small" class="status-tag-pulse">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="客户" width="120">
          <template #default="{ row }">
            {{ getCustomerName(row.customerId) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="时长" width="80">
          <template #default="{ row }">{{ formatDuration({ actualMinutes: row.actualMinutes }) }}</template>
        </el-table-column>
        <el-table-column prop="finalAmount" label="金额(元)" width="100" />
        <el-table-column label="支付方式" width="90">
          <template #default="{ row }">{{ getPaymentMethodName(row.paymentMethodId) || row.paymentMethod || '-' }}</template>
        </el-table-column>
        <el-table-column label="到手比例" width="90">
          <template #default="{ row }">{{ row.settleRatio ?? 100 }}%</template>
        </el-table-column>
        <el-table-column label="开始时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="结束时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.endTime) }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="$router.push(`/orders/${row.id}`)">详情</el-button>
            <el-button size="small" link @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current="query.current"
        v-model:page-size="query.size"
        :total="total"
        :page-sizes="[5, 10, 20]"
        layout="total, sizes, prev, pager, next"
        class="mt-16"
        @change="loadList"
      />
    </el-card>

    <!-- 创建/编辑订单弹窗 -->
    <el-dialog v-model="showDialog" :title="editId ? '编辑订单' : '♡ 创建订单'" width="520px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="客户" prop="customerId">
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

        <el-form-item label="来源" prop="orderSource">
          <el-select v-model="form.orderSource" disabled style="width:100%">
            <el-option
              v-for="s in orderSources"
              :key="s.id"
              :value="s.id"
              :label="s.name"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="日期" prop="selectedDate">
          <el-date-picker
            v-model="form.selectedDate"
           
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width:100%"
          />
        </el-form-item>

        <el-form-item label="时间段" prop="timeRange">
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
        <el-form-item v-if="selectedPackageType === 1 && actualMinutes > 0" label="计费时长">
          <span style="color:#e8789a;font-weight:bold">{{ billableDuration }}</span>
        </el-form-item>

        <el-form-item label="支付方式" prop="paymentMethodId">
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
              :label="`${pkg.name} (¥${pkg.price}/${getPackageUnitName(pkg)})`"
              :value="pkg.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="单价(元)" prop="unitPrice">
          <el-input-number v-model="form.unitPrice" :precision="2" :min="0" style="width:100%" />
        </el-form-item>

        <el-form-item label="到手比例" prop="settleRatio">
          <el-input-number v-model="form.settleRatio" :min="0" :max="100" :precision="0" style="width:100%">
          </el-input-number>
          <span style="margin-left:4px;color:#999">%</span>
        </el-form-item>

        <el-form-item v-if="currentVip" label="VIP优惠">
          <div style="display:flex;align-items:center;gap:8px;width:100%">
            <el-switch v-model="form.applyVipDiscount" />
            <span style="color:#e8789a;font-weight:bold">
              {{ currentVip.name }} — {{ currentVip.discount }}折
            </span>
            <span style="color:#999;font-size:13px">（减免 {{ (100 - currentVip.discount) }}%）</span>
          </div>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" :rows="2" placeholder="订单备注（选填）" />
        </el-form-item>
        <el-form-item v-if="editingRow && (editingRow.actualMinutes || editingRow.extraMinutes)" label="累计计时">
          <span style="color:#666;font-size:13px">
            原始 {{ formatDuration({ actualMinutes: editingRow.actualMinutes }) }}
            <template v-if="editingRow.extraMinutes"> + 补时 {{ formatDuration({ actualMinutes: editingRow.extraMinutes }) }}</template>
            = <b style="color:#e8789a">{{ formatDuration({ actualMinutes: editingRow.actualMinutes, extraMinutes: editingRow.extraMinutes }) }}</b>
          </span>
        </el-form-item>
        <el-form-item v-if="actualMinutes > 0" style="margin-bottom:0">
          <div class="summary-row">
            <span class="summary-label">费用合计</span>
            <span class="summary-amount">¥{{ finalAmount.toFixed(2) }}</span>
            <span class="summary-parenthesis">
              <template v-if="currentVip && form.applyVipDiscount">
                （到手比例{{ form.settleRatio }}%，{{ currentVip.name }}打{{ currentVip.discount }}折 → ¥{{ finalAmount.toFixed(2) }}）
              </template>
              <template v-else>
                （到手比例{{ form.settleRatio }}%）
              </template>
            </span>
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
import { useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { getOrders, createOrder, updateOrder } from '@/api/orders';
import { getCustomerList } from '@/api/customers';
import { getVipConfigs } from '@/api/customers';
import { getPackages } from '@/api/packages';
import { getEnabledSources } from '@/api/orderSource';
import { getEnabledPaymentMethods } from '@/api/paymentMethod';
import { formatDateTime, formatDuration } from '@/utils/format';
import { getStatusLabel as statusLabel, getStatusType as statusType } from '@/types';
import PixelSticker from '@/components/PixelSticker.vue';

/* ===== 类型 ===== */
interface CustomerItem { id: number; nickname: string; contact: string; sourceId?: number; spendLevel?: number }
interface PackageItem { id: number; name: string; price: number; unit: string; packageType: number }
interface SourceItem { id: number; name: string }
interface PaymentMethodItem { id: number; name: string }

/* ===== 状态 ===== */
const list = ref<any[]>([]);
const total = ref(0);
const showDialog = ref(false);
const editId = ref<number | null>(null);
const editingRow = ref<any>(null);
const saving = ref(false);
const showAdvanced = ref(false);
const customerList = ref<CustomerItem[]>([]);
const packageList = ref<PackageItem[]>([]);
const orderSources = ref<SourceItem[]>([]);
const paymentMethods = ref<PaymentMethodItem[]>([]);
const paymentMethodMap = ref<Record<number, string>>({});
const vipConfigs = ref<any[]>([]);
const startTimeVal = ref<string | null>(null);
const endTimeVal = ref<string | null>(null);
const formRef = ref<FormInstance>();
const selectedPackageType = ref<number>(1); // 1=小时单, 其他=统一定价

/** 根据套餐类型获取计价单位显示 */
function getPackageUnitName(pkg: PackageItem): string {
  if (pkg.packageType === 1) return pkg.unit || '小时';
  const typeNames: Record<number, string> = { 2: '包夜', 3: '教学', 4: '月', 5: '次' };
  return typeNames[pkg.packageType] || pkg.unit || '次';
}

const route = useRoute();

const query = reactive({
  source: undefined as number | undefined,
  customerId: undefined as number | undefined,
  keyword: '',
  packageName: '',
  dateRange: null as string[] | null,
  minAmount: undefined as number | undefined,
  maxAmount: undefined as number | undefined,
  minMinutes: undefined as number | undefined,
  maxMinutes: undefined as number | undefined,
  current: 1,
  size: 5,
});

const form = reactive({
  orderSource: undefined as number | undefined,
  paymentMethodId: undefined as number | undefined,
  packageId: undefined as number | undefined,
  unitPrice: 0,
  settleRatio: 100,
  customerId: undefined as number | undefined,
  selectedDate: null as string | null,
  remark: '',
  applyVipDiscount: false,
});

const formRules: FormRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  orderSource: [{ required: true, message: '请选择来源', trigger: 'change' }],
  selectedDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  timeRange: [{ validator: (_rule: any, _value: any, callback: any) => {
    if (!startTimeVal.value || !endTimeVal.value) {
      callback(new Error('请选择开始和结束时间'));
    } else {
      callback();
    }
  }, trigger: 'change' }],
  paymentMethodId: [{ required: true, message: '请选择支付方式', trigger: 'change' }],
  unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
  settleRatio: [{ required: true, message: '请输入到手比例', trigger: 'blur' }],
};

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
  if (!form.selectedDate || !startTimeVal.value || !endTimeVal.value) return 0;
  const [sh, sm] = startTimeVal.value.split(':').map(Number);
  const [eh, em] = endTimeVal.value.split(':').map(Number);
  let diff = (eh * 60 + em) - (sh * 60 + sm);
  if (diff < 0) diff += 1440;
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

/** 计费时长 */
const billableDuration = computed(() => {
  const h = calcBillableHours(actualMinutes.value);
  if (h === 0) return '-';
  const hours = Math.floor(h);
  const mins = Math.round((h - hours) * 60);
  if (hours === 0) return `${mins}min`;
  if (mins === 0) return `${hours}h`;
  return `${hours}h${mins}min`;
});

/** 原始费用：非小时单按统一定价，小时单按计费小时算 */
const rawAmount = computed(() => {
  if (actualMinutes.value <= 0) return 0;
  if (selectedPackageType.value !== 1) return form.unitPrice || 0;
  return calcBillableHours(actualMinutes.value) * (form.unitPrice || 0);
});

/** 预估费用（含到手比例） */
const estimatedAmount = computed(() => {
  if (actualMinutes.value <= 0) return 0;
  const ratio = (form.settleRatio ?? 100) / 100;
  return rawAmount.value * ratio;
});

/** 最终金额（含到手比例，可选VIP折扣） */
const finalAmount = computed(() => {
  if (actualMinutes.value <= 0) return 0;
  const afterRatio = estimatedAmount.value;
  if (currentVip.value && form.applyVipDiscount) {
    return afterRatio * (currentVip.value.discount / 100);
  }
  return afterRatio;
});

/** 当前所选客户的VIP等级折扣 */
const currentVip = computed(() => {
  if (!form.customerId || !vipConfigs.value.length) return null;
  const c = customerList.value.find(c => c.id === form.customerId);
  if (!c || !c.spendLevel || c.spendLevel <= 0) return null;
  return vipConfigs.value.find((v: any) => v.level === c.spendLevel) || null;
});

function getPaymentMethodName(id: number | undefined | null): string {
  if (!id) return '';
  return paymentMethodMap.value[id] || '';
}

const customerMap = ref<Record<number, string>>({});
function getCustomerName(id: number | undefined | null): string {
  if (!id) return '';
  return customerMap.value[id] || `ID:${id}`;
}

const sourceMap = ref<Record<number, string>>({});
function getSourceName(id: number | undefined | null): string {
  if (!id) return '-';
  return sourceMap.value[id] || `ID:${id}`;
}

/* ===== 数据加载 ===== */
const loading = ref(false);

function onCustomerChange(val: number | undefined) {
  if (!val) return;
  const c = customerList.value.find(c => c.id === val);
  if (c && c.sourceId) {
    form.orderSource = c.sourceId;
  }
}

function onPackageChange(val: number | undefined) {
  if (!val) {
    selectedPackageType.value = 1;
    return;
  }
  const pkg = packageList.value.find(p => p.id === val);
  if (pkg) {
    form.unitPrice = pkg.price;
    selectedPackageType.value = pkg.packageType;
  }
}

onMounted(async () => {
  await Promise.all([loadList(), loadCustomers(), loadPackages(), loadSources(), loadPaymentMethods(), loadVipConfigs()]);
});

async function loadList() {
  loading.value = true;
  try {
    const params: any = {
      source: query.source,
      customerId: query.customerId,
      keyword: query.keyword || undefined,
      packageName: query.packageName || undefined,
      current: query.current,
      size: query.size,
    };
    if (query.minAmount != null) params.minAmount = query.minAmount;
    if (query.maxAmount != null) params.maxAmount = query.maxAmount;
    if (query.minMinutes != null) params.minMinutes = query.minMinutes;
    if (query.maxMinutes != null) params.maxMinutes = query.maxMinutes;
    if (query.dateRange) {
      params.startDate = query.dateRange[0];
      params.endDate = query.dateRange[1];
    }
    const res: any = await getOrders(params);
    list.value = res.data?.records || [];
    total.value = res.data?.total || 0;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.current = 1;
  loadList();
}

function handleReset() {
  query.source = undefined;
  query.customerId = undefined;
  query.keyword = '';
  query.packageName = '';
  query.dateRange = null;
  query.minAmount = undefined;
  query.maxAmount = undefined;
  query.minMinutes = undefined;
  query.maxMinutes = undefined;
  query.current = 1;
  loadList();
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

async function loadVipConfigs() {
  try {
    const res: any = await getVipConfigs();
    vipConfigs.value = res.data || [];
  } catch { /* ignore */ }
}

function resetForm() {
  editId.value = null;
  editingRow.value = null;
  form.orderSource = undefined;
  form.paymentMethodId = undefined;
  form.packageId = undefined;
  form.unitPrice = 0;
  form.settleRatio = 100;
  form.customerId = undefined;
  form.selectedDate = null;
  form.remark = '';
  form.applyVipDiscount = false;
  startTimeVal.value = null;
  endTimeVal.value = null;
  selectedPackageType.value = 1;
}

function openCreate() {
  resetForm();
  showDialog.value = true;
}

function openEdit(row: any) {
  editId.value = row.id;
  editingRow.value = row;
  form.orderSource = row.orderSource || undefined;
  form.paymentMethodId = row.paymentMethodId || undefined;
  form.packageId = row.packageId || undefined;
  form.unitPrice = row.unitPrice || 0;
  form.settleRatio = row.settleRatio ?? 100;
  form.customerId = row.customerId || undefined;
  form.remark = row.remark || '';

  // 解析套餐类型
  if (form.packageId) {
    const pkg = packageList.value.find(p => p.id === form.packageId);
    selectedPackageType.value = pkg ? pkg.packageType : 1;
  } else {
    selectedPackageType.value = 1;
  }

  if (row.startTime) {
    const dt = new Date(row.startTime);
    form.selectedDate = `${dt.getFullYear()}-${(dt.getMonth() + 1).toString().padStart(2, '0')}-${dt.getDate().toString().padStart(2, '0')}`;
    startTimeVal.value = `${dt.getHours().toString().padStart(2, '0')}:${dt.getMinutes().toString().padStart(2, '0')}:${dt.getSeconds().toString().padStart(2, '0')}`;
  } else {
    form.selectedDate = null;
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
  if (!formRef.value) return;
  await formRef.value.validate();

  let startTime: string | undefined;
  let endTime: string | undefined;
  if (form.selectedDate && startTimeVal.value && endTimeVal.value) {
    const [sh, sm] = startTimeVal.value.split(':').map(Number);
    const [eh, em] = endTimeVal.value.split(':').map(Number);
    const isOvernight = eh * 60 + em < sh * 60 + sm;

    startTime = `${form.selectedDate}T${startTimeVal.value}`;
    if (isOvernight) {
      const d = new Date(form.selectedDate);
      d.setDate(d.getDate() + 1);
      const nextDate = `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`;
      endTime = `${nextDate}T${endTimeVal.value}`;
    } else {
      endTime = `${form.selectedDate}T${endTimeVal.value}`;
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
    applyVipDiscount: form.applyVipDiscount,
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
.header-title { font-size: 15px; font-weight: 600; color: #5D4E6D; }

/* 卡片入场动画 */
.order-card {
  animation: slideUp 0.4s ease both;
}

/* 创建订单按钮 */
.btn-create-order {
  transition: all 0.3s ease !important;
}
.btn-create-order:hover {
  transform: translateY(-2px) scale(1.03) !important;
  box-shadow: 0 6px 20px rgba(232, 130, 154, 0.2) !important;
}

.filter-bar { margin-bottom: 16px; }
.filter-bar .el-form-item { margin-bottom: 4px; }
.mt-16 { margin-top: 16px; }
.time-range-row { display: flex; align-items: center; width: 100%; gap: 0; }
.time-sep { margin: 0 8px; color: #999; flex-shrink: 0; }
.range-input { display: flex; align-items: center; gap: 4px; }
.range-input .el-input-number { width: 110px; }
.range-sep { color: #999; flex-shrink: 0; }
.summary-row { display: flex; align-items: baseline; padding: 8px 20px 8px 0; border-top: 1px solid #eee; width: 100%; box-sizing: border-box; }
.summary-label { color: #999; font-size: 14px; margin-right: 8px; flex-shrink: 0; }
.summary-amount { color: #e8789a; font-size: 18px; font-weight: bold; flex-shrink: 0; }
.summary-parenthesis { color: #999; font-size: 13px; margin-left: 4px; white-space: nowrap; }

/* 表格行悬停增强 */
:deep(.el-table__body tr) {
  transition: all 0.25s ease;
}
:deep(.el-table__body tr:hover) {
  transform: scale(1.002);
  box-shadow: 0 2px 12px rgba(232, 130, 154, 0.06);
}

/* 状态标签脉冲动画 */
.status-tag-pulse {
  animation: pulse 2s ease-in-out infinite;
}
</style>
