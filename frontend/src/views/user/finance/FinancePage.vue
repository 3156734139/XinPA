<template>
  <div class="finance-page">
    <el-row :gutter="16" class="mb-16">
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value">{{ stats.todayIncome || '0.00' }}</div>
            <div class="stat-label">今日收入(元)</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value" style="color:#e6a23c">{{ stats.weekIncome || '0.00' }}</div>
            <div class="stat-label">本周收入(元)</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value" style="color:#67c23a">{{ stats.monthIncome || '0.00' }}</div>
            <div class="stat-label">本月收入(元)</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-value" style="color:#f56c6c">{{ stats.monthProfit || '0.00' }}</div>
            <div class="stat-label">本月利润(元)</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="16" class="mb-16 left-col">
        <el-card shadow="never">
          <template #header>
            <div style="display:flex;align-items:center;justify-content:space-between">
              <span>
                <PixelSticker :size="18" style="margin-right:4px" />
                收支流水
              </span>
              <el-button size="small" type="primary" @click="showCreate = true">记账</el-button>
            </div>
          </template>
          <el-form :inline="true" class="filter-bar">
            <el-form-item label="类型">
              <el-select v-model="query.type" placeholder="全部" clearable @change="loadRecords" style="width:120px">
                <el-option :value="1" label="收入" />
                <el-option :value="2" label="支出" />
              </el-select>
            </el-form-item>
          </el-form>
          <el-table :data="records" stripe>
            <el-table-column label="类型" width="70">
              <template #default="{ row }">
                <el-tag :type="row.recordType === 1 ? 'success' : 'danger'" :size="small">
                  {{ row.recordType === 1 ? '收入' : '支出' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" />
            <el-table-column prop="amount" label="金额(元)" width="120" />
            <el-table-column label="支付方式" width="100">
              <template #default="{ row }">
                {{ row.paymentMethod || (row.orderId ? '订单关联' : '-') }}
              </template>
            </el-table-column>
            <el-table-column prop="recordDate" label="日期" width="120" />
            <el-table-column label="操作" width="100" fixed="right" align="center">
              <template #default="{ row }">
                <el-tooltip v-if="row.orderId" content="订单关联记录，不可删除" placement="top">
                  <el-button size="small" link disabled>删除</el-button>
                </el-tooltip>
                <el-button v-else size="small" link @click="handleDelete(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-model:current="query.current"
            v-model:page-size="query.size"
            :total="recordTotal"
            layout="prev, pager, next"
            class="mt-16"
            @change="loadRecords"
          />
        </el-card>
      </el-col>

      <el-col :span="8" class="mb-16 right-col">
        <el-card shadow="never">
          <template #header>
            <div style="display:flex;align-items:center;justify-content:space-between">
              <span>月度目标</span>
              <el-button size="small" link @click="openTargetEdit">编辑</el-button>
            </div>
          </template>
          <div v-if="setting">
            <div class="target-progress">
              <div class="target-label">收入目标</div>
              <el-progress :percentage="incomePercent" :status="incomePercent >= 100 ? 'success' : undefined" />
              <div class="target-label" style="margin-top:4px;font-size:12px;color:#999">
                {{ stats.monthIncome || 0 }} / {{ setting.monthlyTarget || 0 }} 元
              </div>
            </div>
            <div class="target-progress" style="padding-top:4px">
              <div class="target-label">支出预算</div>
              <el-progress :percentage="expensePercent" :status="expensePercent <= 0 ? 'exception' : undefined" />
              <div class="target-label" style="margin-top:4px;font-size:12px;color:#999">
                剩余 {{ expenseRemaining }} / {{ setting.monthlyExpenseTarget || 0 }} 元
              </div>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="trend-card">
          <template #header>
            <div style="display:flex;align-items:center;justify-content:space-between">
              <div style="display:flex;align-items:center;gap:12px">
                <span style="font-size:14px">收支趋势</span>
                <div style="display:flex;align-items:center;gap:8px;font-size:12px;color:#666">
                  <span><span style="display:inline-block;width:12px;height:12px;border-radius:2px;background:#E8789A;vertical-align:middle;margin-right:3px"></span>收入</span>
                  <span><span style="display:inline-block;width:12px;height:12px;border-radius:2px;background:#F0B070;vertical-align:middle;margin-right:3px"></span>支出</span>
                </div>
              </div>
              <el-radio-group v-model="trendMode" size="small">
                <el-radio-button value="week">周</el-radio-button>
                <el-radio-button value="month">月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="trend-chart-wrapper">
            <VChart :key="trendKey" :option="trendOption" autoresize />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showCreate" title="记账" width="450px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="类型">
          <el-radio-group v-model="createForm.recordType">
            <el-radio :value="1">收入</el-radio>
            <el-radio :value="2">支出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="createForm.category" placeholder="如: 陪玩收入/外设/推广" />
        </el-form-item>
        <el-form-item label="金额(元)">
          <el-input-number v-model="createForm.amount" :precision="2" :min="0" />
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="createForm.paymentMethodId" clearable>
            <el-option
              v-for="m in paymentMethods"
              :key="m.id"
              :value="m.id"
              :label="m.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="createForm.recordDate" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 设定月度目标 -->
    <el-dialog v-model="showTargetEdit" title="设定月度目标" width="400px">
      <el-form label-width="120px">
        <el-form-item label="月收入目标(元)">
          <el-input-number v-model="targetEditIncome" :precision="2" :min="0" style="width:100%" />
        </el-form-item>
        <el-form-item label="月支出预算(元)">
          <el-input-number v-model="targetEditExpense" :precision="2" :min="0" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTargetEdit = false">取消</el-button>
        <el-button type="primary" @click="handleSaveTarget">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { LineChart, BarChart } from 'echarts/charts';
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
import { getFinanceRecords, createFinanceRecord, deleteFinanceRecord, getFinanceStats, getFinanceSetting, updateFinanceSetting, getFinanceTrend } from '@/api/finance';
import { getEnabledPaymentMethods } from '@/api/paymentMethod';
import PixelSticker from '@/components/PixelSticker.vue';

use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer]);

const stats = ref<any>({});
const records = ref<any[]>([]);
const recordTotal = ref(0);
const setting = ref<any>(null);
const showCreate = ref(false);
const showTargetEdit = ref(false);
const targetEditIncome = ref(0);
const targetEditExpense = ref(0);

const paymentMethods = ref<any[]>([]);
const paymentMethodMap = ref<Record<number, string>>({});
const query = reactive({ type: undefined as number | undefined, current: 1, size: 10 });
const createForm = reactive({ recordType: 1, category: '', amount: 0, paymentMethodId: undefined as number | undefined, recordDate: '' });

const trendMode = ref('week');
const trendData = ref<any[]>([]);
const trendKey = ref(0);

const incomePercent = computed(() => {
  if (!setting.value?.monthlyTarget || setting.value.monthlyTarget === 0) return 0;
  return Number((((stats.value.monthIncome || 0) / setting.value.monthlyTarget) * 100).toFixed(1));
});

const expensePercent = computed(() => {
  if (!setting.value?.monthlyExpenseTarget || setting.value.monthlyExpenseTarget === 0) return 0;
  const used = stats.value.monthExpense || 0;
  const remaining = Math.max(0, setting.value.monthlyExpenseTarget - used);
  return Number(((remaining / setting.value.monthlyExpenseTarget) * 100).toFixed(1));
});

const expenseRemaining = computed(() => {
  if (!setting.value?.monthlyExpenseTarget) return '0.00';
  const remaining = (setting.value.monthlyExpenseTarget - (stats.value.monthExpense || 0));
  return remaining > 0 ? remaining.toFixed(2) : '0.00';
});

onMounted(() => {
  loadStats();
  loadRecords();
  loadSetting();
  loadTrend();
  loadPaymentMethods();
});

watch(trendMode, () => loadTrend());

async function loadTrend() {
  const res: any = await getFinanceTrend(trendMode.value);
  trendData.value = res.data || [];
  trendKey.value++;
}

async function loadStats() {
  const res: any = await getFinanceStats();
  stats.value = res.data || {};
}
async function loadRecords() {
  const res: any = await getFinanceRecords(query);
  records.value = res.data?.records || [];
  recordTotal.value = res.data?.total || 0;
}
async function loadSetting() {
  const res: any = await getFinanceSetting();
  setting.value = res.data;
}

function openTargetEdit() {
  targetEditIncome.value = setting.value?.monthlyTarget || 0;
  targetEditExpense.value = setting.value?.monthlyExpenseTarget || 0;
  showTargetEdit.value = true;
}

async function handleSaveTarget() {
  await updateFinanceSetting({
    id: setting.value?.id,
    monthlyTarget: targetEditIncome.value,
    monthlyExpenseTarget: targetEditExpense.value,
  });
  if (setting.value) {
    setting.value.monthlyTarget = targetEditIncome.value;
    setting.value.monthlyExpenseTarget = targetEditExpense.value;
  }
  ElMessage.success('目标已更新');
  showTargetEdit.value = false;
  loadSetting();
}

async function loadPaymentMethods() {
  try {
    const res: any = await getEnabledPaymentMethods();
    paymentMethods.value = res.data || [];
    paymentMethods.value.forEach(m => { paymentMethodMap.value[m.id] = m.name; });
  } catch { /* ignore */ }
}

async function handleCreate() {
  const pm = createForm.paymentMethodId ? paymentMethodMap.value[createForm.paymentMethodId] : '';
  await createFinanceRecord({
    recordType: createForm.recordType,
    category: createForm.category,
    amount: createForm.amount,
    paymentMethod: pm,
    recordDate: createForm.recordDate || new Date().toISOString().split('T')[0],
  });
  ElMessage.success('记账成功');
  showCreate.value = false;
  createForm.paymentMethodId = undefined;
  loadRecords();
  loadStats();
  loadTrend();
}

const trendOption = computed(() => {
  const dates = trendData.value.map((d: any) => d.date);
  const income = trendData.value.map((d: any) => Number(d.income || 0));
  const expense = trendData.value.map((d: any) => Number(d.expense || 0));
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 55, right: 20, bottom: 36, top: 10 },
    xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', splitNumber: 4, axisLabel: { fontSize: 11, formatter: '¥{value}' }, splitLine: { lineStyle: { type: 'dashed', color: '#eee' } } },
    series: [
      { name: '收入', type: 'bar', data: income, barWidth: 12, itemStyle: { color: '#E8789A', borderRadius: [3, 3, 0, 0] } },
      { name: '支出', type: 'bar', data: expense, barWidth: 12, itemStyle: { color: '#F0B070', borderRadius: [3, 3, 0, 0] } },
    ],
  };
});

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除该流水记录？', '确认删除', { type: 'warning' });
    await deleteFinanceRecord(id);
    ElMessage.success('已删除');
    loadRecords();
    loadStats();
    loadTrend();
  } catch {
    // 取消删除
  }
}

</script>

<style scoped>
.mb-16 { margin-bottom: 16px; }
.mt-16 { margin-top: 16px; }
.stat-item { text-align: center; padding: 8px 0; }
.stat-value { font-size: 24px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 13px; color: #999; margin-top: 4px; }
.target-progress { padding: 16px 0; }
.target-label { font-size: 14px; color: #666; margin-bottom: 8px; }
.left-col, .right-col { display: flex; }
.left-col { flex-direction: column; }
.left-col > .el-card { flex: 1; }
.right-col { flex-direction: column; gap: 12px; }
.right-col > .el-card { margin-bottom: 0; }
.trend-card { flex: 1; display: flex; flex-direction: column; }
.trend-card :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; }
.trend-chart-wrapper { flex: 1; }
.trend-chart-wrapper > * { width: 100%; height: 100%; }
</style>
