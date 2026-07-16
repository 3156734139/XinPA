<template>
  <div class="profile-page">
    <!-- 页面标题 -->
    <div class="page-header mb-16">
      <h2>人设配置</h2>
      <p>为每个游戏独立设置介绍文案和标签</p>
    </div>

    <!-- 游戏配置列表 -->
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>我的游戏配置</span>
          <el-button type="primary" :icon="Plus" @click="openAddDialog" round>
            添加游戏
          </el-button>
        </div>
      </template>

      <!-- 空状态 -->
      <el-empty v-if="list.length === 0" description="还没有游戏配置，点击右上角添加" :image-size="100">
        <el-button type="primary" :icon="Plus" @click="openAddDialog">添加游戏</el-button>
      </el-empty>

      <!-- 游戏卡片列表 -->
      <div v-else class="game-list">
        <div v-for="item in list" :key="item.id" class="game-card">
          <div class="game-card-header">
            <div class="game-card-title">
              <span class="game-icon">🎮</span>
              <span class="game-name">{{ item.gameName }}</span>
            </div>
            <div class="game-card-actions">
              <el-button text type="primary" :icon="Edit" @click="openEditDialog(item)">编辑</el-button>
              <el-button text type="danger" :icon="Delete" @click="handleDelete(item.id)">删除</el-button>
            </div>
          </div>
          <div class="game-card-body">
            <div class="game-detail">
              <div class="detail-label">介绍文案</div>
              <div class="detail-value text-ellipsis">{{ item.intro || '未设置' }}</div>
            </div>
            <div class="game-detail">
              <div class="detail-label">开场白</div>
              <div class="detail-value text-ellipsis">{{ item.openingLine || '未设置' }}</div>
            </div>
            <div class="game-detail" v-if="item.tags">
              <div class="detail-label">标签</div>
              <div class="detail-value">
                <el-tag
                  v-for="tag in (item.tags || '').split(',').filter(Boolean)"
                  :key="tag"
                  size="small"
                  round
                  class="tag-item"
                >{{ tag.trim() }}</el-tag>
                <span v-if="!item.tags" class="text-gray">未设置</span>
              </div>
            </div>
            <div class="game-detail" v-if="item.rankInfo || item.positionInfo">
              <div class="detail-label">段位 / 位置</div>
              <div class="detail-value">
                <span v-if="item.rankInfo" class="chip">{{ item.rankInfo }}</span>
                <span v-if="item.positionInfo" class="chip">{{ item.positionInfo }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑游戏配置' : '添加游戏配置'"
      width="560px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="游戏名称" prop="gameName">
          <el-input v-model="form.gameName" placeholder="例：英雄联盟、王者荣耀、绝地求生" maxlength="64" />
        </el-form-item>
        <el-form-item label="介绍文案" prop="intro">
          <el-input
            v-model="form.intro"
            type="textarea"
            :rows="4"
            placeholder="这个游戏下的个人介绍..."
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="开场白">
          <el-input
            v-model="form.openingLine"
            type="textarea"
            :rows="2"
            placeholder="招呼话术..."
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="标签">
          <el-input
            v-model="form.tags"
            placeholder="多个标签用逗号分隔，如：打野、中单、ADC"
          />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="段位">
              <el-input v-model="form.rankInfo" placeholder="如：最强王者、巅峰2000分" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="擅长位置">
              <el-input v-model="form.positionInfo" placeholder="如：打野、中单" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getGameConfigs, addGameConfig, updateGameConfig, deleteGameConfig } from '@/api/profile';
import { Plus, Edit, Delete } from '@element-plus/icons-vue';
import type { FormInstance } from 'element-plus';

interface GameConfig {
  id?: number;
  gameName: string;
  intro: string;
  openingLine: string;
  tags: string;
  rankInfo: string;
  positionInfo: string;
  sortOrder?: number;
}

const formRef = ref<FormInstance>();
const list = ref<GameConfig[]>([]);
const dialogVisible = ref(false);
const isEditing = ref(false);
const saving = ref(false);
const editingId = ref<number | null>(null);
const form = ref<GameConfig>({
  gameName: '',
  intro: '',
  openingLine: '',
  tags: '',
  rankInfo: '',
  positionInfo: '',
});

const rules = {
  gameName: [
    { required: true, message: '请输入游戏名称', trigger: 'blur' },
    { max: 64, message: '最长64个字符', trigger: 'blur' },
  ],
};

onMounted(async () => {
  await fetchList();
});

async function fetchList() {
  try {
    const res: any = await getGameConfigs();
    list.value = res.data || [];
  } catch {
    // ignore
  }
}

function openAddDialog() {
  isEditing.value = false;
  editingId.value = null;
  form.value = { gameName: '', intro: '', openingLine: '', tags: '', rankInfo: '', positionInfo: '' };
  dialogVisible.value = true;
}

function openEditDialog(item: GameConfig) {
  isEditing.value = true;
  editingId.value = item.id!;
  form.value = { ...item };
  dialogVisible.value = true;
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) return;

  saving.value = true;
  try {
    if (isEditing.value && editingId.value) {
      await updateGameConfig({ ...form.value, id: editingId.value });
      ElMessage.success('更新成功');
    } else {
      await addGameConfig(form.value);
      ElMessage.success('添加成功');
    }
    dialogVisible.value = false;
    await fetchList();
  } finally {
    saving.value = false;
  }
}

async function handleDelete(id: number | undefined) {
  if (id == null) return;
  try {
    await ElMessageBox.confirm('确定删除该游戏配置吗？', '确认删除', { type: 'warning' });
    await deleteGameConfig(id);
    ElMessage.success('已删除');
    await fetchList();
  } catch {
    // cancelled
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 960px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 4px;
}

.page-header p {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}

.mb-16 { margin-bottom: 16px; }

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 15px;
  font-weight: 600;
}

/* 游戏卡片列表 */
.game-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.game-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}

.game-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.game-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.game-card-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.game-icon {
  font-size: 22px;
}

.game-name {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.game-card-actions {
  display: flex;
  gap: 4px;
}

.game-card-body {
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.game-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-label {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-value {
  font-size: 14px;
  color: #1e293b;
  line-height: 1.5;
}

.text-ellipsis {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.text-gray {
  color: #94a3b8;
}

.tag-item {
  margin: 2px 4px 2px 0;
}

.chip {
  display: inline-block;
  padding: 2px 10px;
  background: #f1f5f9;
  border-radius: 20px;
  font-size: 13px;
  color: #475569;
  margin: 2px 6px 2px 0;
}
</style>
