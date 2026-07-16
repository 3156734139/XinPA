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
      <el-col :span="14" class="mb-16">
        <el-card shadow="never">
          <template #header><span>收支流水</span></template>
          <el-form :inline="true">
            <el-form-item label="类型">
              <el-select v-model="query.type" placeholder="全部" clearable @change="loadRecords">
                <el-option :value="1" label="收入" />
                <el-option :value="2" label="支出" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="showCreate = true">记账</el-button>
            </el-form-item>
          </el-form>
          <el-table :data="records" stripe>
            <el-table-column label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="row.recordType === 1 ? 'success' : 'danger'" size="small">
                  {{ row.recordType === 1 ? '收入' : '支出' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="category" label="分类" />
            <el-table-column prop="amount" label="金额(元)" width="120" />
            <el-table-column prop="paymentMethod" label="支付方式" width="100" />
            <el-table-column prop="recordDate" label="日期" width="120" />
            <el-table-column label="操作" width="80" fixed="right">
              <template #default="{ row }">
                <el-button size="small" link type="danger" @click="handleDelete(row.id)">删除</el-button>
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

      <el-col :span="10" class="mb-16">
        <el-card shadow="never">
          <template #header>
            <span style="display:flex;align-items:center;justify-content:space-between">
              月度目标
              <el-button size="small" link @click="openTargetEdit">编辑</el-button>
            </span>
          </template>
          <div v-if="setting">
            <div class="target-progress">
              <div class="target-label">目标: {{ setting.monthlyTarget }} 元</div>
              <el-progress :percentage="targetPercent" :status="targetPercent >= 100 ? 'success' : undefined" />
              <div class="target-label" style="margin-top:8px">
                已完成: {{ stats.monthIncome || 0 }} / {{ setting.monthlyTarget || 0 }} 元
              </div>
            </div>
          </div>
        </el-card>
        <el-card shadow="never" class="mt-16">
          <template #header><span>提现计算器</span></template>
          <el-input-number v-model="withdrawAmount" :precision="2" :min="0" placeholder="输入提现金额" />
          <el-button type="primary" size="small" @click="calcWithdraw" class="ml-12">计算</el-button>
          <div v-if="withdrawResult" class="withdraw-result">
            <div>手续费: {{ withdrawResult.fee }} 元</div>
            <div>实际到账: {{ withdrawResult.actualAmount }} 元</div>
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
          <el-select v-model="createForm.paymentMethod">
            <el-option label="平台" value="平台" />
            <el-option label="微信" value="微信" />
            <el-option label="支付宝" value="支付宝" />
            <el-option label="现金" value="现金" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="createForm.recordDate" type="date" />
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
        <el-form-item label="月目标(元)">
          <el-input-number v-model="targetEditValue" :precision="2" :min="0" style="width:100%" />
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
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getFinanceRecords, createFinanceRecord, deleteFinanceRecord, getFinanceStats, getFinanceSetting, updateFinanceSetting, withdrawCalc } from '@/api/finance';

const stats = ref<any>({});
const records = ref<any[]>([]);
const recordTotal = ref(0);
const setting = ref<any>(null);
const showCreate = ref(false);
const showTargetEdit = ref(false);
const targetEditValue = ref(0);
const withdrawAmount = ref(0);
const withdrawResult = ref<any>(null);
const query = reactive({ type: undefined as number | undefined, current: 1, size: 20 });
const createForm = reactive({ recordType: 1, category: '', amount: 0, paymentMethod: '微信', recordDate: '' });

const targetPercent = computed(() => {
  if (!setting.value?.monthlyTarget || setting.value.monthlyTarget === 0) return 0;
  return Number((((stats.value.monthIncome || 0) / setting.value.monthlyTarget) * 100).toFixed(1));
});

onMounted(() => {
  loadStats();
  loadRecords();
  loadSetting();
});

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
  targetEditValue.value = setting.value?.monthlyTarget || 0;
  showTargetEdit.value = true;
}

async function handleSaveTarget() {
  await updateFinanceSetting({ ...setting.value, monthlyTarget: targetEditValue.value });
  ElMessage.success('目标已更新');
  showTargetEdit.value = false;
  loadSetting();
}

async function handleCreate() {
  await createFinanceRecord({ ...createForm, recordDate: createForm.recordDate || new Date().toISOString().split('T')[0] });
  ElMessage.success('记账成功');
  showCreate.value = false;
  loadRecords();
  loadStats();
}

async function handleDelete(id: number) {
  await deleteFinanceRecord(id);
  ElMessage.success('已删除');
  loadRecords();
  loadStats();
}

async function calcWithdraw() {
  const res: any = await withdrawCalc(withdrawAmount.value);
  withdrawResult.value = res.data;
}
</script>

<style scoped>
.mb-16 { margin-bottom: 16px; }
.mt-16 { margin-top: 16px; }
.ml-12 { margin-left: 12px; }
.stat-item { text-align: center; padding: 8px 0; }
.stat-value { font-size: 24px; font-weight: bold; color: #409eff; }
.stat-label { font-size: 13px; color: #999; margin-top: 4px; }
.target-progress { padding: 16px 0; }
.target-label { font-size: 14px; color: #666; margin-bottom: 8px; }
.withdraw-result { margin-top: 12px; font-size: 14px; color: #666; }
.withdraw-result div { margin-bottom: 4px; }
</style>
