<template>
  <div class="material-list">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>素材库</span>
          <div>
            <el-upload :show-file-list="false" :http-request="handleUpload" accept="image/*,audio/*,video/*">
              <el-button type="primary" size="small">上传素材</el-button>
            </el-upload>
          </div>
        </div>
      </template>
      <el-radio-group v-model="filterType" class="mb-16" @change="loadList">
        <el-radio-button :value="undefined">全部</el-radio-button>
        <el-radio-button :value="1">截图</el-radio-button>
        <el-radio-button :value="2">语音</el-radio-button>
        <el-radio-button :value="3">短视频</el-radio-button>
      </el-radio-group>
      <div v-if="list.length === 0" class="empty">暂无素材</div>
      <el-table v-else :data="list" stripe>
        <el-table-column prop="name" label="素材名称" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">{{ ['', '截图', '语音', '短视频'][row.materialType] }}</template>
        </el-table-column>
        <el-table-column :formatter="(r:any) => (r.fileSize / 1024 / 1024).toFixed(2) + ' MB'" label="大小" width="100" />
        <el-table-column label="水印" width="80">
          <template #default="{ row }">{{ row.watermark ? '已添加' : '未添加' }}</template>
        </el-table-column>
        <el-table-column prop="createdAt" label="上传时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getMaterials, uploadMaterial, deleteMaterial } from '@/api/profile';

const list = ref<any[]>([]);
const filterType = ref<number | undefined>(undefined);

onMounted(() => { loadList(); });

async function loadList() {
  const res: any = await getMaterials(filterType.value);
  list.value = res.data || [];
}

async function handleUpload(options: any) {
  const fd = new FormData();
  fd.append('file', options.file);
  fd.append('name', options.file.name);
  fd.append('type', String(filterType.value || 1));
  fd.append('watermark', '0');
  await uploadMaterial(fd);
  ElMessage.success('上传成功');
  loadList();
}

async function handleDelete(id: number) {
  await deleteMaterial(id);
  ElMessage.success('已删除');
  loadList();
}
</script>

<style scoped>
.header-bar { display: flex; justify-content: space-between; align-items: center; }
.mb-16 { margin-bottom: 16px; }
.empty { text-align: center; padding: 40px; color: #999; }
</style>
