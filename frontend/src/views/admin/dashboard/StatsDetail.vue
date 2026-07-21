<template>
  <div class="admin-stats">
    <el-row :gutter="20" class="mb-20">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><span>套餐类型分布</span></template>
          <v-chart :option="packageChartOption" style="height: 320px" autoresize />
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span>配额用量概览</span></template>
          <div class="stats-info">
            <div v-for="(val, key) in overview" :key="key" class="info-item">
              <span class="info-label">{{ key }}</span>
              <span class="info-value">{{ val }}</span>
            </div>
            <div v-if="Object.keys(overview).length === 0" class="empty-state">
              <el-empty description="暂无数据" :image-size="60" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getOverview, getPackageRatio } from '@/api/admin';
import { getEnabledPackageTypes } from '@/api/packageType';
import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { PieChart } from 'echarts/charts';
import { TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer]);

const overview = ref<any>({});
const packageChartOption = ref({});

const colorPalette = ['#6366f1', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4'];

onMounted(async () => {
  const [overviewRes, pkgRes, typesRes] = await Promise.all([
    getOverview(),
    getPackageRatio(),
    getEnabledPackageTypes(),
  ]);
  overview.value = (overviewRes as any).data || {};

  // 动态构建套餐类型映射
  const typeMap: Record<string, string> = {};
  const types = (typesRes as any).data || [];
  types.forEach((t: any) => { typeMap[t.id] = t.name; });

  const data = (pkgRes as any).data || [];

  packageChartOption.value = {
    tooltip: { trigger: 'item', formatter: '{b}: {c}个 ({d}%)' },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { fontSize: 13, color: '#475569' },
    },
    series: [{
      type: 'pie',
      radius: ['40%', '68%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      padAngle: 2,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' },
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.15)' },
      },
      data: data.map((d: any, i: number) => ({
        name: typeMap[d.package_type] || d.package_type,
        value: d.count,
        itemStyle: { color: colorPalette[i % colorPalette.length] },
      })),
    }],
  };
});
</script>

<style scoped>
.mb-20 { margin-bottom: 20px; }
.stats-info { padding: 4px 0; }
.info-item { display: flex; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #f1f5f9; }
.info-item:last-child { border-bottom: none; }
.info-label { color: #64748b; font-size: 13px; }
.info-value { font-weight: 600; color: #0f172a; font-size: 14px; }
.empty-state { padding: 20px 0; }
</style>
