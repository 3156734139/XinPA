<template>
  <div class="admin-manage">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>管理员管理</span>
          <el-button type="primary" size="small" @click="showCreate = true">新增管理员</el-button>
        </div>
      </template>

      <el-table :data="list" stripe>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="真实姓名" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'SUPER_ADMIN' ? 'danger' : 'primary'">
              {{ row.role === 'SUPER_ADMIN' ? '超级管理员' : '管理员' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="160">
          <template #default="{ row }">{{ row.lastLoginTime ? formatDateTime(row.lastLoginTime) : '-' }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
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
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreate" :title="editId ? '编辑管理员' : '新增管理员'" width="450px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!editId" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" :placeholder="editId ? '不填则不修改' : ''" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="超级管理员" value="SUPER_ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance } from 'element-plus';
import { getAdmins, createAdmin, updateAdmin, toggleAdminStatus } from '@/api/admin';
import { formatDateTime } from '@/utils/format';

const list = ref<any[]>([]);
const showCreate = ref(false);
const editId = ref<number | null>(null);
const formRef = ref<FormInstance>();
const form = ref<any>({ username: '', password: '', realName: '', role: 'ADMIN' });
const formRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: !editId.value, message: '请输入密码', trigger: 'blur' }],
};

onMounted(() => { loadList(); });

async function loadList() {
  const res: any = await getAdmins();
  list.value = res.data || [];
}

function edit(row: any) {
  editId.value = row.id;
  form.value = { ...row, password: '' };
  showCreate.value = true;
}

async function handleSave() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }
  if (editId.value) {
    await updateAdmin({ id: editId.value, ...form.value });
  } else {
    await createAdmin(form.value);
  }
  ElMessage.success('保存成功');
  showCreate.value = false;
  loadList();
}

async function toggleStatus(row: any) {
  await toggleAdminStatus(row.id, row.status === 1 ? 0 : 1);
  ElMessage.success('状态已更新');
  loadList();
}
</script>

<style scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
