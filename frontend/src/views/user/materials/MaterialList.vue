<template>
  <div class="material-list">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>
            <PixelSticker :size="18" /> 素材库
          </span>
          <div>
            <el-upload
              :show-file-list="false"
              :http-request="handleUpload"
              accept="image/*,audio/*,video/*"
            >
              <el-button size="small" type="primary">上传素材</el-button>
            </el-upload>
          </div>
        </div>
      </template>
      <el-radio-group v-model="filterType" class="mb-16" @change="onFilterChange">
        <el-radio-button :value="undefined">全部</el-radio-button>
        <el-radio-button value="image">图片</el-radio-button>
        <el-radio-button value="audio">音频</el-radio-button>
        <el-radio-button value="video">视频</el-radio-button>
      </el-radio-group>

      <div v-if="loading" class="empty">加载中...</div>
      <div v-else-if="list.length === 0" class="empty">暂无素材</div>
      <el-row v-else :gutter="16">
        <el-col
          v-for="item in list"
          :key="item.id"
          :xs="12"
          :sm="8"
          :md="6"
          :lg="4"
          class="mb-16"
        >
          <el-card shadow="hover" class="material-card" :body-style="{ padding: '0' }">
            <!-- 图片预览 -->
            <div v-if="item.materialType === 'image'" class="card-image">
              <el-image
                :src="getPreviewUrl(item.fileUrl)"
                fit="cover"
                class="preview-img"
                :preview-src-list="[getPreviewUrl(item.fileUrl)]"
                preview-teleported
              >
                <template #error>
                  <div class="img-placeholder">
                    <el-icon :size="32"><PictureFilled /></el-icon>
                  </div>
                </template>
              </el-image>
            </div>
            <!-- 音频图标 -->
            <div v-else-if="item.materialType === 'audio'" class="card-icon">
              <el-icon :size="40"><Headset /></el-icon>
            </div>
            <!-- 视频缩略图（截帧）+ 播放按钮 -->
            <div v-else-if="item.materialType === 'video'" class="card-video" @click="playVideo(item)">
              <el-image
                :src="getPreviewUrl(item.fileUrl)"
                fit="cover"
                class="preview-img"
              >
                <template #error>
                  <div class="img-placeholder">
                    <el-icon :size="32"><VideoCameraFilled /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="play-overlay">
                <el-icon :size="36"><VideoPlay /></el-icon>
              </div>
            </div>
            <div class="card-info">
              <div class="card-name" :title="item.name">{{ item.name }}</div>
              <div class="card-meta">
                <span>{{ formatSize(item.fileSize) }}</span>
                <span v-if="item.watermark"> · 水印</span>
              </div>
            </div>
            <div class="card-actions">
              <el-button size="small" link @click="handleDelete(item.id)">删除</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <div v-if="total > 0" class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20]"
          layout="total, sizes, prev, pager, next"
          @change="loadList"
        />
      </div>
    </el-card>

    <!-- 视频播放弹窗 -->
    <el-dialog v-model="videoDialogVisible" title="视频预览" width="640px" top="5vh" destroy-on-close>
      <div class="video-player-wrap">
        <video
          v-if="videoUrl"
          :src="videoUrl"
          controls
          preload="none"
          class="video-player"
        ></video>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { PictureFilled, Headset, VideoCameraFilled, VideoPlay } from '@element-plus/icons-vue';
import { getMaterials, getUploadToken, notifyUploadComplete, deleteMaterial, getVideoUrl } from '@/api/materials';
import PixelSticker from '@/components/PixelSticker.vue';
import OSS from 'ali-oss';

const list = ref<any[]>([]);
const filterType = ref<string | undefined>(undefined);
const currentPage = ref(1);
const pageSize = ref(5);
const total = ref(0);
const loading = ref(false);

// 上传限制
const MAX_FILE_SIZE = 500 * 1024 * 1024; // 500MB
const ALLOWED_EXTENSIONS = [
  '.jpg', '.jpeg', '.png', '.gif', '.webp',
  '.mp4', '.mov', '.avi', '.mkv', '.flv',
  '.mp3', '.wav', '.flac', '.ogg',
];

// 视频播放
const videoDialogVisible = ref(false);
const videoUrl = ref('');

onMounted(() => { loadList(); });

function onFilterChange() {
  currentPage.value = 1;
  loadList();
}

async function loadList() {
  loading.value = true;
  try {
    const res: any = await getMaterials(filterType.value, currentPage.value, pageSize.value);
    const data = res.data || {};
    list.value = data.page?.records || [];
    total.value = data.page?.total || 0;
  } catch (e) {
    console.error('加载素材失败', e);
  } finally {
    loading.value = false;
  }
}

function getPreviewUrl(url: string): string {
  return url || '';
}

function formatSize(bytes: number): string {
  if (!bytes) return '0 B';
  const mb = bytes / 1024 / 1024;
  if (mb >= 1) return mb.toFixed(2) + ' MB';
  const kb = bytes / 1024;
  if (kb >= 1) return kb.toFixed(1) + ' KB';
  return bytes + ' B';
}

async function playVideo(item: any) {
  try {
    const res: any = await getVideoUrl(item.id);
    videoUrl.value = res.data?.url || '';
    videoDialogVisible.value = true;
  } catch (e) {
    console.error('获取视频地址失败', e);
    ElMessage.error('获取视频地址失败');
  }
}

async function handleUpload(options: any) {
  const file = options.file as File;
  const fileType = filterType.value || 'image';

  // 校验文件大小
  if (file.size > MAX_FILE_SIZE) {
    ElMessage.error('文件大小不能超过 500MB');
    return;
  }
  // 校验文件格式
  const ext = getFileExt(file.name).toLowerCase();
  if (!ALLOWED_EXTENSIONS.includes(ext)) {
    ElMessage.error('不支持的文件格式: ' + ext);
    return;
  }

  try {
    const tokenRes: any = await getUploadToken(fileType);
    const creds = tokenRes.data;
    const objectKey = creds.objectKey + getFileExt(file.name);

    const client = new OSS({
      region: creds.region,
      accessKeyId: creds.accessKeyId,
      accessKeySecret: creds.accessKeySecret,
      stsToken: creds.securityToken,
      bucket: creds.bucket,
      secure: true,
    });
    await client.put(objectKey, file);

    await notifyUploadComplete({
      name: file.name,
      type: fileType,
      objectKey,
      fileSize: file.size,
      watermark: 0,
    });

    ElMessage.success('上传成功');
    loadList();
  } catch (e: any) {
    console.error('上传失败', e);
    ElMessage.error('上传失败: ' + (e.message || '未知错误'));
  }
}

function getFileExt(fileName: string): string {
  const idx = fileName.lastIndexOf('.');
  return idx >= 0 ? fileName.substring(idx) : '';
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该素材？', '确认删除', { type: 'warning' });
    await deleteMaterial(id);
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
.mb-16 {
  margin-bottom: 16px;
}
.empty {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 14px;
}
.material-card {
  border-radius: 8px;
  transition: transform 0.2s;
}
.material-card:hover {
  transform: translateY(-2px);
}
.card-image {
  width: 100%;
  height: 140px;
  overflow: hidden;
  background: #f5f5f5;
}
.preview-img {
  width: 100%;
  height: 100%;
  display: block;
}
.img-placeholder {
  width: 100%;
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bbb;
}
.card-icon {
  width: 100%;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}
.card-video {
  width: 100%;
  height: 140px;
  overflow: hidden;
  background: #1a1a2e;
  position: relative;
  cursor: pointer;
}
.play-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  transition: background 0.2s;
}
.play-overlay:hover {
  background: rgba(0, 0, 0, 0.5);
}
.video-player-wrap {
  line-height: 0;
}
.video-player {
  width: 100%;
  max-height: 70vh;
  border-radius: 4px;
}
.card-info {
  padding: 10px 12px 4px;
}
.card-name {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-meta {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.card-actions {
  padding: 4px 12px 10px;
}
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
