<template>
  <div class="todo-manage">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>
            <PixelSticker :size="18" style="margin-right:4px" />
            待办事项
          </span>
          <el-button size="small" type="primary" @click="showCreate = true">新建待办</el-button>
        </div>
      </template>

      <div v-if="list.length === 0" class="empty-state">
        <el-empty description="暂无待办事项" :image-size="100" />
      </div>

      <div v-for="item in list" :key="item.id" class="todo-item">
        <el-checkbox
          :model-value="item.status === 1"
          @change="handleToggle(item.id)"
          size="large"
        >
          <span :class="{ 'todo-done': item.status === 1 }">{{ item.title }}</span>
        </el-checkbox>
        <div class="todo-meta">
          <el-tag v-if="item.todoType" size="small" round>
            {{ ['', '素材更新', '平台签到', '自定义'][item.todoType] }}
          </el-tag>
          <span v-if="item.dueDate" class="todo-due">{{ item.dueDate }}</span>
          <el-button size="small" link @click="handleDelete(item.id)">删除</el-button>
        </div>
      </div>
    </el-card>

    <!-- 新建待办弹窗 -->
    <el-dialog v-model="showCreate" title="新建待办" width="400px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="内容" prop="title">
          <el-input v-model="form.title" placeholder="待办内容" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.todoType" placeholder="选择类型" style="width:100%">
            <el-option :value="1" label="素材更新" />
            <el-option :value="2" label="平台签到" />
            <el-option :value="3" label="自定义" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="form.dueDate" placeholder="选填" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="saving">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { getTodos, createTodo, toggleTodo, deleteTodo } from '@/api/tools';
import PixelSticker from '@/components/PixelSticker.vue';

const list = ref<any[]>([]);
const showCreate = ref(false);
const saving = ref(false);
const formRef = ref<FormInstance>();
const form = reactive({ title: '', todoType: 3, dueDate: null as string | null });

const formRules: FormRules = {
  title: [{ required: true, message: '请输入待办内容', trigger: 'blur' }],
};

onMounted(loadList);

async function loadList() {
  const res: any = await getTodos();
  list.value = res.data || [];
}

async function handleToggle(id: number) {
  await toggleTodo(id);
  loadList();
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该待办事项？', '确认删除', { type: 'warning' });
    await deleteTodo(id);
    ElMessage.success('已删除');
    loadList();
  } catch {
    // 取消删除
  }
}

async function handleCreate() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }
  saving.value = true;
  try {
    await createTodo(form);
    ElMessage.success('创建成功');
    showCreate.value = false;
    form.title = '';
    form.dueDate = null;
    loadList();
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 1px solid rgba(232, 130, 154, 0.08);
}
.todo-item:last-child { border-bottom: none; }
.todo-done { text-decoration: line-through; color: #C4B0CC; }
.todo-meta { display: flex; align-items: center; gap: 10px; flex-shrink: 0; }
.todo-due { font-size: 12px; color: #999; }
.empty-state { padding: 40px 0; }
</style>
