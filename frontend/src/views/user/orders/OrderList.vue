<template>
  <div class="order-list">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>♡ 订单管理</span>
          <el-button type="primary" size="small" @click="showCreate = true">创建订单</el-button>
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
          <el-select v-model="query.source" placeholder="全部" clearable @change="loadList">
            <el-option :value="1" label="平台派单" />
            <el-option :value="2" label="微信/QQ私域" />
            <el-option :value="3" label="线下预约" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="query.keyword" placeholder="搜索订单" clearable @change="loadList" />
        </el-form-item>
      </el-form>

      <el-table :data="list" stripe>
        <el-table-column prop="orderNo" label="订单号" width="160" />
        <el-table-column prop="title" label="标题" min-width="130" />
        <el-table-column label="来源" width="90">
          <template #default="{ row }">{{ ['', '平台派单', '微信QQ', '线下预约'][row.orderSource] }}</template>
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
        <el-table-column prop="finalAmount" label="金额(元)" width="100" />
        <el-table-column prop="actualMinutes" label="计时(分)" width="90" />
        <el-table-column prop="createdAt" label="创建时间" width="155" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="$router.push(`/orders/${row.id}`)">详情</el-button>
            <el-button v-if="row.status === 1" size="small" link type="primary" @click="startTimer(row.id)">开始</el-button>
            <el-button v-if="row.status === 2" size="small" link type="warning" @click="pauseTimer(row.id)">暂停</el-button>
            <el-button v-if="row.status === 3" size="small" link type="success" @click="settleOrder(row.id)">结算</el-button>
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

    <!-- 创建订单弹窗 -->
    <el-dialog v-model="showCreate" title="♡ 创建订单" width="520px" class="create-order-dialog">
      <el-form :model="createForm" label-width="100px">
        <!-- 客户选择（与客户管理联动） -->
        <el-form-item label="客户" prop="customerId">
          <el-select
            v-model="createForm.customerId"
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

        <el-form-item label="标题">
          <el-input v-model="createForm.title" placeholder="订单标题" />
        </el-form-item>

        <el-form-item label="来源">
          <el-select v-model="createForm.orderSource" style="width:100%">
            <el-option :value="1" label="平台派单" />
            <el-option :value="2" label="微信/QQ私域" />
            <el-option :value="3" label="线下预约" />
          </el-select>
        </el-form-item>

        <!-- 套餐选择（与价目套餐联动） -->
        <el-form-item label="选择套餐">
          <el-select
            v-model="createForm.packageId"
            placeholder="选择套餐自动填价"
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
          <el-input-number v-model="createForm.unitPrice" :precision="2" :min="0" style="width:100%" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" placeholder="订单备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="creating">♡ 创建订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getOrders, createOrder, startTimer as apiStart, pauseTimer as apiPause, settleOrder as apiSettle } from '@/api/orders';
import { getCustomerList } from '@/api/customers';
import { getPackages } from '@/api/profile';

/* ===== 类型 ===== */
interface CustomerItem { id: number; nickname: string; contact: string }
interface PackageItem { id: number; name: string; price: number; unit: string }

/* ===== 状态 ===== */
const list = ref<any[]>([]);
const total = ref(0);
const showCreate = ref(false);
const creating = ref(false);
const customerList = ref<CustomerItem[]>([]);
const packageList = ref<PackageItem[]>([]);

const query = reactive({
  status: undefined as number | undefined,
  source: undefined as number | undefined,
  keyword: '',
  current: 1,
  size: 20,
});

const createForm = reactive({
  title: '',
  orderSource: 1,
  packageId: undefined as number | undefined,
  unitPrice: 0,
  customerId: undefined as number | undefined,
  remark: '',
});

/* ===== 客户名映射（表格展示用） ===== */
const customerMap = ref<Record<number, string>>({});
function getCustomerName(id: number | undefined | null): string {
  if (!id) return '';
  return customerMap.value[id] || `ID:${id}`;
}

/* ===== 联动：套餐选择自动填价 ===== */
function onPackageChange(val: number | undefined) {
  if (!val) return;
  const pkg = packageList.value.find(p => p.id === val);
  if (pkg) {
    createForm.unitPrice = pkg.price;
    if (!createForm.title) {
      createForm.title = pkg.name;
    }
  }
}

/* ===== 联动：客户选择 ===== */
function onCustomerChange(val: number | undefined) {
  if (!val) return;
  const c = customerList.value.find(c => c.id === val);
  if (c && !createForm.title) {
    // 可选的：自动填充标题
  }
}

/* ===== 加载数据 ===== */
onMounted(async () => {
  await Promise.all([loadList(), loadCustomers(), loadPackages()]);
});

async function loadList() {
  const res: any = await getOrders(query);
  list.value = res.data?.records || [];
  total.value = res.data?.total || 0;
}

/** 加载客户列表（用于下拉框） */
async function loadCustomers() {
  try {
    const res: any = await getCustomerList();
    const items: CustomerItem[] = res.data?.records || [];
    customerList.value = items;
    // 建立id→name映射
    items.forEach(c => { customerMap.value[c.id] = c.nickname; });
  } catch {
    // ignore
  }
}

/** 加载套餐列表（用于下拉框） */
async function loadPackages() {
  try {
    const res: any = await getPackages();
    packageList.value = (res.data || []).filter((p: any) => p.status === 1);
  } catch {
    // ignore
  }
}

/* ===== 操作 ===== */
function statusText(s: number) {
  return ['', '待接单', '进行中', '待结算', '已完结', '售后退款'][s] || '';
}
function statusType(s: number) {
  return ['', 'info', 'primary', 'warning', 'success', 'danger'][s] || '';
}

async function startTimer(id: number) {
  await apiStart(id);
  ElMessage.success('计时已开始');
  loadList();
}
async function pauseTimer(id: number) {
  await apiPause(id);
  ElMessage.success('计时已暂停');
  loadList();
}
async function settleOrder(id: number) {
  await apiSettle(id);
  ElMessage.success('已结算');
  loadList();
}

async function handleCreate() {
  creating.value = true;
  try {
    // 提交订单（含 packageId, customerId 传给后端）
    await createOrder({
      title: createForm.title,
      orderSource: createForm.orderSource,
      packageId: createForm.packageId || undefined,
      unitPrice: createForm.unitPrice,
      customerId: createForm.customerId || undefined,
      remark: createForm.remark,
    });
    ElMessage.success('♡ 订单创建成功');
    showCreate.value = false;
    loadList();
  } catch {
    // error handled by interceptor
  } finally {
    creating.value = false;
  }
}
</script>

<style scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filter-bar { margin-bottom: 16px; }
.mt-16 { margin-top: 16px; }
</style>
