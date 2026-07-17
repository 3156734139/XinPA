<template>
  <div class="vip-level-manage">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>优惠等级配置</span>
          <el-button type="primary" size="small" @click="add">新增等级</el-button>
        </div>
      </template>

      <el-table :data="list" stripe>
        <el-table-column label="等级" width="80">
          <template #default="{ row }">
            <el-tag size="small" :style="levelStyle(row.level)">{{ row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="消费门槛" width="130">
          <template #default="{ row }">¥{{ formatNumber(row.threshold) }}</template>
        </el-table-column>
        <el-table-column label="折扣" width="80">
          <template #default="{ row }">{{ row.discount }}折</template>
        </el-table-column>
        <el-table-column label="等级福利" min-width="200">
          <template #default="{ row }">{{ row.benefits || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link @click="edit(row)">编辑</el-button>
            <el-button size="small" link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDialog" :title="editId ? '编辑等级' : '新增等级'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="等级名称" prop="name">
          <el-input v-model="form.name" placeholder="如 VIP1" />
        </el-form-item>
        <el-form-item label="消费门槛">
          <el-input-number v-model="form.threshold" :precision="2" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="折扣">
          <el-input-number v-model="form.discount" :min="0" :max="100" style="width:100%" />
          <span style="margin-left:8px;color:#999">如 98 = 98折</span>
        </el-form-item>
        <el-form-item label="等级福利">
          <el-input v-model="form.benefits" type="textarea" :rows="2" placeholder="描述该等级福利，如：全场98折、优先派单" maxlength="500" />
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
import { getVipLevels, createVipLevel, updateVipLevel, deleteVipLevel } from '@/api/admin';

const list = ref<any[]>([]);
const showDialog = ref(false);
const editId = ref<number | null>(null);
const saving = ref(false);
const formRef = ref<FormInstance>();
const form = reactive({ name: '', level: 1, threshold: 0, discount: 100, benefits: '' });

const rules: FormRules = {
  name: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
};

onMounted(() => { loadList(); });

function levelStyle(level: number): Record<string, string> {
  const max = 20;
  const idx = Math.min(level, max);
  // 等级1 → 浅粉(92%)，等级20 → 深粉(50%)
  const lightness = 92 - (idx - 1) * (42 / (max - 1));
  return {
    backgroundColor: `hsl(340, 70%, ${lightness}%)`,
    borderColor: `hsl(340, 70%, ${lightness - 6}%)`,
    color: lightness < 55 ? '#fff' : `hsl(340, 50%, ${Math.max(lightness - 55, 18)}%)`,
  };
}

function formatNumber(v: number | string): string {
  return Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

async function loadList() {
  const res: any = await getVipLevels();
  list.value = res.data || [];
}

function resetForm() {
  form.name = ''; form.level = 1; form.threshold = 0; form.discount = 100; form.benefits = '';
}

function add() {
  editId.value = null;
  resetForm();
  // 自动取下一个可用等级号
  const maxLevel = Math.max(...list.value.map(i => i.level), 0);
  form.level = maxLevel + 1;
  showDialog.value = true;
}

function edit(row: any) {
  editId.value = row.id;
  form.name = row.name; form.level = row.level; form.threshold = row.threshold;
  form.discount = row.discount; form.benefits = row.benefits || '';
  showDialog.value = true;
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;
  saving.value = true;
  try {
    if (editId.value) {
      await updateVipLevel({ id: editId.value, ...form });
    } else {
      await createVipLevel(form);
    }
    ElMessage.success('保存成功');
    showDialog.value = false;
    loadList();
  } finally {
    saving.value = false;
  }
}

async function handleDelete(id: number) {
  await deleteVipLevel(id);
  ElMessage.success('已删除');
  loadList();
}
</script>

<style scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
