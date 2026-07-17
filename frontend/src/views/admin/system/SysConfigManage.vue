<template>
  <div class="sys-config">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>优惠等级配置</span>
          <el-button size="small" type="primary" @click="openCreate">新增配置</el-button>
        </div>
      </template>
      <el-alert title="优惠等级根据累计消费金额自动计算，可在下方调整各级别门槛和折扣" type="info" :closable="false" show-icon style="margin-bottom:16px" />
      <el-table :data="list" stripe>
        <el-table-column prop="configKey" label="配置键" width="220">
          <template #default="{ row }">
            <el-tag>{{ row.configKey }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" />
        <el-table-column label="当前值" width="140">
          <template #default="{ row }">
            {{ row.configValue }}{{ row.configKey.includes('discount') ? '折' : '' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="editId ? '编辑配置' : '新增配置'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="配置键">
          <el-input v-model="form.configKey" placeholder="如: vip1_threshold" :disabled="!!editId" />
        </el-form-item>
        <el-form-item label="配置值">
          <el-input-number v-model="form.configValue" :min="0" :precision="form.configKey.includes('discount') ? 0 : 2" style="width:100%" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" placeholder="配置说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleDialogSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getConfigs, createConfig, updateConfig, deleteConfig } from '@/api/admin';

const list = ref<any[]>([]);
const showDialog = ref(false);
const editId = ref<number | null>(null);
const saving = ref(false);
const form = reactive({ configKey: '', configValue: 0, description: '' });

onMounted(() => loadList());

async function loadList() {
  const res: any = await getConfigs();
  list.value = (res.data || []).map((c: any) => ({ ...c, configValue: Number(c.configValue) }));
}

function openCreate() {
  editId.value = null;
  form.configKey = '';
  form.configValue = 0;
  form.description = '';
  showDialog.value = true;
}

function handleEdit(row: any) {
  editId.value = row.id;
  form.configKey = row.configKey;
  form.configValue = Number(row.configValue);
  form.description = row.description || '';
  showDialog.value = true;
}

async function handleDialogSave() {
  saving.value = true;
  try {
    if (editId.value) {
      await updateConfig({ id: editId.value, configKey: form.configKey, configValue: String(form.configValue), description: form.description });
      ElMessage.success('已更新');
    } else {
      await createConfig({ configKey: form.configKey, configValue: String(form.configValue), description: form.description });
      ElMessage.success('已创建');
    }
    showDialog.value = false;
    loadList();
  } finally {
    saving.value = false;
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该配置？', '确认删除', { type: 'warning' });
    await deleteConfig(id);
    ElMessage.success('已删除');
    loadList();
  } catch {
    // 取消删除
  }
}
</script>

<style scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
