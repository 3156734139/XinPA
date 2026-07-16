<template>
  <div class="announcement-manage">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>公告管理</span>
          <el-button type="primary" size="small" @click="showCreate = true">发布公告</el-button>
        </div>
      </template>

      <el-table :data="list" stripe>
        <el-table-column prop="title" label="标题" min-width="160" />
        <el-table-column prop="content" label="内容" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已发布' : '已下线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link :type="row.status === 1 ? 'warning' : 'success'" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下线' : '发布' }}
            </el-button>
            <el-button size="small" link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreate" title="发布公告" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getAdminAnnouncements, createAnnouncement, toggleAnnouncement, deleteAnnouncement } from '@/api/admin';

const list = ref<any[]>([]);
const showCreate = ref(false);
const form = ref({ title: '', content: '' });

onMounted(() => { loadList(); });

async function loadList() {
  const res: any = await getAdminAnnouncements();
  list.value = res.data || [];
}

async function handleCreate() {
  await createAnnouncement(form.value);
  ElMessage.success('发布成功');
  showCreate.value = false;
  loadList();
}

async function toggleStatus(row: any) {
  await toggleAnnouncement(row.id, row.status === 1 ? 0 : 1);
  ElMessage.success('状态已更新');
  loadList();
}

async function handleDelete(id: number) {
  await deleteAnnouncement(id);
  ElMessage.success('已删除');
  loadList();
}
</script>

<style scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
</style>
