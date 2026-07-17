<template>
  <div class="package-list">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span><PixelSticker :size="18" /> 价目套餐</span>
          <el-button size="small" type="primary" @click="showDialog = true">添加套餐</el-button>
        </div>
      </template>
      <el-table :data="list" stripe>
        <el-table-column prop="name" label="套餐名称" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ typeMap[row.packageType] || '-' }}</template>
        </el-table-column>
        <el-table-column prop="price" label="价格(元)" width="100" />
        <el-table-column prop="unit" label="计价单位" width="80" />
        <!-- 联动：订单数 -->
        <el-table-column label="订单数" width="90" align="center">
          <template #default="{ row }">
            <span class="link-text" @click="viewOrders(row)">{{ row.orderCount ?? '-' }}</span>
          </template>
        </el-table-column>
        <!-- 联动：收入 -->
        <el-table-column label="收入(元)" width="110" align="center">
          <template #default="{ row }">
            <span class="link-text" @click="viewFinance(row)">{{ formatMoney(row.totalRevenue) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="edit(row)">编辑</el-button>
            <el-button size="small" link type="primary" @click="quickOrder(row)">快速下单</el-button>
            <el-button size="small" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="showDialog" :title="editId ? '编辑套餐' : '添加套餐'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="套餐名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="套餐类型">
          <el-select v-model="form.packageType">
            <el-option
              v-for="t in typeOptions"
              :key="t.id"
              :value="t.id"
              :label="t.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格(元)">
          <el-input-number v-model="form.price" :precision="2" :min="0" />
        </el-form-item>
        <el-form-item label="计价单位">
          <el-input v-model="form.unit" placeholder="如: 小时/局/天" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" :rows="2" />
        </el-form-item>
        <el-form-item label="上架">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getPackages, addPackage, updatePackage, deletePackage } from '@/api/profile';
import { getEnabledPackageTypes } from '@/api/packageType';

import PixelSticker from '@/components/PixelSticker.vue';

const router = useRouter();
const typeMap = ref<Record<number, string>>({});
const typeOptions = ref<any[]>([]);

function formatMoney(val: number | string | null | undefined): string {
  if (val == null) return '-';
  const num = typeof val === 'string' ? parseFloat(val) : val;
  return `¥${num.toFixed(2)}`;
}
const list = ref<any[]>([]);
const showDialog = ref(false);
const editId = ref<number | null>(null);
const saving = ref(false);
const form = reactive({ name: '', packageType: 1, price: 0, unit: '小时', description: '', status: 1 });

onMounted(() => { loadList(); loadTypes(); });

async function loadTypes() {
  try {
    const res: any = await getEnabledPackageTypes();
    const list = res.data || [];
    typeOptions.value = list;
    list.forEach((t: any) => { typeMap.value[t.id] = t.name; });
  } catch { /* ignore */ }
}

/** 加载套餐列表 —— orderCount 和 totalRevenue 由后端从订单表实时统计 */
async function loadList() {
  const res: any = await getPackages();
  list.value = (res.data || []).map((item: any) => ({
    ...item,
    orderCount: item.orderCount ?? 0,
    totalRevenue: item.totalRevenue ?? 0,
  }));
}

function edit(row: any) {
  editId.value = row.id;
  Object.assign(form, row);
  showDialog.value = true;
}

/** 快速下单 —— 跳转订单页并传入套餐信息 */
function quickOrder(row: any) {
  router.push({
    path: '/orders',
    query: { packageId: row.id, packageName: row.name, price: row.price },
  });
}

/** 查看该套餐的订单 */
function viewOrders(row: any) {
  router.push({
    path: '/orders',
    query: { packageId: row.id },
  });
}

/** 查看该套餐的财务 */
function viewFinance(row: any) {
  router.push({
    path: '/finance',
    query: { packageName: row.name },
  });
}

async function handleSave() {
  saving.value = true;
  try {
    if (editId.value) {
      await updatePackage({ ...form, id: editId.value });
    } else {
      await addPackage(form);
    }
    ElMessage.success('保存成功');
    showDialog.value = false;
    loadList();
  } finally {
    saving.value = false;
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该套餐？', '确认删除', { type: 'warning' });
    await deletePackage(id);
    ElMessage.success('已删除');
    loadList();
  } catch {
    // 取消删除
  }
}
</script>

<style scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.link-text {
  color: #E8789A;
  cursor: pointer;
  font-weight: 600;
  transition: color 0.2s;
}

.link-text:hover {
  color: #F0A0B8;
  text-decoration: underline;
}
</style>
