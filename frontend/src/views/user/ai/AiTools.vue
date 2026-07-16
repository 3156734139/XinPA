<template>
  <div class="ai-tools">
    <el-row :gutter="16">
      <el-col :span="24" class="mb-16">
        <el-card shadow="never">
          <template #header>
            <span>AI 辅助工具 <el-tag size="small" type="warning">今日剩余: {{ quota.remaining }} / {{ quota.dailyLimit }} 次</el-tag></span>
          </template>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="生成简介" name="intro">
              <el-input v-model="introForm.games" placeholder="擅长游戏（逗号分隔）" class="mb-12" />
              <el-select v-model="introForm.templateType" class="mb-12">
                <el-option label="游戏陪玩" value="游戏陪玩" />
                <el-option label="声优树洞" value="声优树洞" />
                <el-option label="线下陪伴" value="线下陪伴" />
              </el-select>
              <el-button type="primary" @click="generate('intro')" :loading="loading">AI生成简介</el-button>
              <el-input v-if="introResult" :model-value="introResult" type="textarea" :rows="4" class="mt-12" />
            </el-tab-pane>
            <el-tab-pane label="安抚话术" name="comfort">
              <el-input v-model="comfortForm.customerName" placeholder="客户昵称" class="mb-12" />
              <el-input v-model="comfortForm.reason" placeholder="负面情绪原因" class="mb-12" />
              <el-button type="primary" @click="generate('comfort')" :loading="loading">生成安抚话术</el-button>
              <el-input v-if="comfortResult" :model-value="comfortResult" type="textarea" :rows="4" class="mt-12" />
            </el-tab-pane>
            <el-tab-pane label="砍价应对" name="bargain">
              <el-input v-model="bargainForm.packageName" placeholder="套餐名称" class="mb-12" />
              <el-input v-model="bargainForm.price" placeholder="套餐价格" class="mb-12" />
              <el-button type="primary" @click="generate('bargain')" :loading="loading">生成应对话术</el-button>
              <el-input v-if="bargainResult" :model-value="bargainResult" type="textarea" :rows="4" class="mt-12" />
            </el-tab-pane>
            <el-tab-pane label="活动方案" name="activity">
              <el-input v-model="activityForm.activityType" placeholder="如: 周末/包月/通宵" class="mb-12" />
              <el-button type="primary" @click="generate('activity')" :loading="loading">生成活动方案</el-button>
              <el-input v-if="activityResult" :model-value="activityResult" type="textarea" :rows="6" class="mt-12" />
            </el-tab-pane>
            <el-tab-pane label="风险识别" name="risk">
              <el-input v-model="riskContent" type="textarea" :rows="6" placeholder="粘贴聊天内容或描述" class="mb-12" />
              <el-button type="danger" @click="generate('risk')" :loading="loading">识别风险</el-button>
              <el-input v-if="riskResult" :model-value="riskResult" type="textarea" :rows="6" class="mt-12" />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { getAiQuota, generateIntro, generateComfort, generateBargain, generateActivity, riskDetection } from '@/api/ai';

const activeTab = ref('intro');
const loading = ref(false);
const quota = ref<any>({ remaining: 0, dailyLimit: 10 });

// intro
const introForm = reactive({ games: '', templateType: '游戏陪玩' });
const introResult = ref('');
// comfort
const comfortForm = reactive({ customerName: '', reason: '' });
const comfortResult = ref('');
// bargain
const bargainForm = reactive({ packageName: '', price: '' });
const bargainResult = ref('');
// activity
const activityForm = reactive({ activityType: '周末活动' });
const activityResult = ref('');
// risk
const riskContent = ref('');
const riskResult = ref('');

onMounted(async () => {
  const res: any = await getAiQuota();
  quota.value = res.data || { remaining: 0, dailyLimit: 10 };
});

async function generate(type: string) {
  loading.value = true;
  try {
    let res: any;
    switch (type) {
      case 'intro':
        res = await generateIntro(introForm);
        introResult.value = res.data || '';
        break;
      case 'comfort':
        res = await generateComfort(comfortForm);
        comfortResult.value = res.data || '';
        break;
      case 'bargain':
        res = await generateBargain(bargainForm);
        bargainResult.value = res.data || '';
        break;
      case 'activity':
        res = await generateActivity(activityForm);
        activityResult.value = res.data || '';
        break;
      case 'risk':
        res = await riskDetection(riskContent.value);
        riskResult.value = res.data || '';
        break;
    }
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.mb-16 { margin-bottom: 16px; }
.mb-12 { margin-bottom: 12px; }
.mt-12 { margin-top: 12px; }
</style>
