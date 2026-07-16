<template>
  <div class="admin-overview">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :xs="12" :sm="6" class="mb-20">
        <el-card shadow="never" class="stat-card-wrapper">
          <div class="stat-item">
            <div class="stat-icon stat-icon-users">
              <el-icon :size="22"><UserFilled /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value">{{ overview.totalUsers || 0 }}</div>
              <div class="stat-label">总注册用户</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" class="mb-20">
        <el-card shadow="never" class="stat-card-wrapper">
          <div class="stat-item">
            <div class="stat-icon stat-icon-new">
              <el-icon :size="22"><TrendCharts /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value text-success">{{ overview.todayNewUsers || 0 }}</div>
              <div class="stat-label">今日新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" class="mb-20">
        <el-card shadow="never" class="stat-card-wrapper">
          <div class="stat-item">
            <div class="stat-icon stat-icon-vip">
              <el-icon :size="22"><Star /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value text-warning">{{ overview.vipUsers || 0 }}</div>
              <div class="stat-label">付费会员</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" class="mb-20">
        <el-card shadow="never" class="stat-card-wrapper">
          <div class="stat-item">
            <div class="stat-icon stat-icon-revenue">
              <el-icon :size="22"><Coin /></el-icon>
            </div>
            <div class="stat-body">
              <div class="stat-value text-danger">{{ overview.yearRevenue || '0.00' }}</div>
              <div class="stat-label">年度营收（元）</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表 -->
    <el-row :gutter="20">
      <el-col :span="12" class="mb-20">
        <el-card shadow="never">
          <template #header><span>用户增长趋势（近30天）</span></template>
          <v-chart :option="userChartOption" style="height: 320px" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12" class="mb-20">
        <el-card shadow="never">
          <template #header><span>各品类陪玩占比</span></template>
          <v-chart :option="ratioChartOption" style="height: 320px" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getOverview, getUserGrowth, getTemplateRatio } from '@/api/admin';
import { UserFilled, TrendCharts, Star, Coin } from '@element-plus/icons-vue';
import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { BarChart, PieChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

use([BarChart, PieChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

const overview = ref<any>({});
const userChartOption = ref({});
const ratioChartOption = ref({});

onMounted(async () => {
  const [overviewRes, growthRes, ratioRes] = await Promise.all([
    getOverview(),
    getUserGrowth(),
    getTemplateRatio(),
  ]);
  overview.value = (overviewRes as any).data || {};

  // 用户增长柱状图
  const growthData = (growthRes as any).data || [];
  userChartOption.value = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: growthData.map((d: any) => d.date?.substring(5) || ''),
      axisLabel: { fontSize: 11, color: '#94a3b8' },
      axisLine: { lineStyle: { color: '#e2e8f0' } },
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
      axisLabel: { fontSize: 11, color: '#94a3b8' },
    },
    series: [{
      type: 'bar',
      data: growthData.map((d: any) => d.count),
      itemStyle: {
        color: {
          type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: '#818cf8' },
            { offset: 1, color: '#6366f1' },
          ],
        },
        borderRadius: [4, 4, 0, 0],
      },
      barMaxWidth: 32,
    }],
  };

  // 品类占比饼图
  const ratioData = (ratioRes as any).data || [];
  const typeMap: Record<string, string> = { '1': '游戏陪玩', '2': '声优树洞', '3': '线下陪伴' };
  ratioChartOption.value = {
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { fontSize: 13, color: '#475569' },
    },
    series: [{
      type: 'pie',
      radius: ['45%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.15)' },
      },
      data: ratioData.map((d: any, i: number) => ({
        name: typeMap[d.template_type] || '其他',
        value: d.count,
        itemStyle: { color: ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'][i % 5] },
      })),
    }],
  };
});
</script>

<style scoped>
.mb-20 { margin-bottom: 20px; }

.stat-card-wrapper {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card-wrapper:hover {
  transform: translateY(-2px);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 4px 0;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon-users { background: rgba(99, 102, 241, 0.1); color: var(--el-color-primary); }
.stat-icon-new { background: rgba(16, 185, 129, 0.1); color: var(--el-color-success); }
.stat-icon-vip { background: rgba(245, 158, 11, 0.1); color: var(--el-color-warning); }
.stat-icon-revenue { background: rgba(239, 68, 68, 0.1); color: var(--el-color-danger); }

.stat-body { flex: 1; }
.stat-value { font-size: 24px; font-weight: 700; line-height: 1.2; }
.stat-label { font-size: 13px; color: #94a3b8; margin-top: 2px; }
</style>
