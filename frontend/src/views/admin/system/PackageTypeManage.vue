<template>
  <div class="package-type-manage">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>套餐类型管理</span>
          <el-button type="primary" size="small" @click="add">新增类型</el-button>
        </div>
      </template>

      <el-table :data="list" stripe>
        <el-table-column prop="name" label="类型名称" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="edit(row)">编辑</el-button>
            <el-button
              size="small"
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button size="small" link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="editId ? '编辑类型' : '新增类型'" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="类型名称" prop="name">
          <el-input v-model="form.name" placeholder="如: 小时单/局数单" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
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
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import {
  getPackageTypes, createPackageType, updatePackageType,
  deletePackageType, togglePackageTypeStatus,
} from '@/api/admin';
import { formatDateTime } from '@/utils/format';

const list = ref<any[]>([]);
const showDialog = ref(false);
const editId = ref<number | null>(null);
const saving = ref(false);
const formRef = ref<FormInstance>();
const form = reactive({ name: '', sortOrder: 0, status: 1 });

const rules: FormRules = {
  name: [{ required: true, message: '请输入类型名称', trigger: 'blur' }],
};

onMounted(() => { loadList(); });

async function loadList() {
  const res: any = await getPackageTypes();
  list.value = res.data || [];
}

function add() {
  editId.value = null;
  form.name = '';
  form.sortOrder = 0;
  form.status = 1;
  showDialog.value = true;
}

function edit(row: any) {
  editId.value = row.id;
  form.name = row.name;
  form.sortOrder = row.sortOrder;
  form.status = row.status;
  showDialog.value = true;
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  saving.value = true;
  try {
    if (editId.value) {
      await updatePackageType({ id: editId.value, ...form });
    } else {
      await createPackageType(form);
    }
    ElMessage.success('保存成功');
    showDialog.value = false;
    loadList();
  } finally {
    saving.value = false;
  }
}

async function toggleStatus(row: any) {
  const newStatus = row.status === 1 ? 0 : 1;
  await togglePackageTypeStatus(row.id, newStatus);
  ElMessage.success(newStatus === 1 ? '已启用' : '已禁用');
  loadList();
}

async function handleDelete(id: number) {
  await deletePackageType(id);
  ElMessage.success('已删除');
  loadList();
}
</script>

<style scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
