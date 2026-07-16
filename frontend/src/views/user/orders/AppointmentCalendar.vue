<template>
  <div class="appointment-calendar">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>♡ 预约日历</span>
          <el-button type="primary" size="small" @click="showCreate = true">新建预约</el-button>
        </div>
      </template>
      <el-calendar v-model="currentDate">
        <template #date-cell="{ data }">
          <div class="calendar-cell">
            <div class="date-num">{{ data.day.split('-')[2] }}</div>
            <div v-for="item in getDayAppointments(data.day)" :key="item.id" class="appointment-tag" :style="{ background: item.color || '#E8789A' }">
              {{ item.title }}
            </div>
          </div>
        </template>
      </el-calendar>
    </el-card>

    <el-dialog v-model="showCreate" title="♡ 新建预约" width="500px" class="appointment-dialog">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="createForm.title" placeholder="预约名称" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="createForm.startTime" type="datetime" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="createForm.endTime" type="datetime" placeholder="选择结束时间" />
        </el-form-item>
        <el-form-item label="通宵">
          <el-switch v-model="createForm.isOvernight" />
        </el-form-item>
        <el-form-item label="线下">
          <el-switch v-model="createForm.isOffline" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { getAppointments, createAppointment } from '@/api/orders';

const currentDate = ref(new Date());
const appointments = ref<any[]>([]);
const showCreate = ref(false);
const createForm = ref<any>({ title: '', startTime: '', endTime: '', isOvernight: false, isOffline: false });

onMounted(() => { loadAppointments(); });

async function loadAppointments() {
  const res: any = await getAppointments({});
  appointments.value = res.data || [];
}

function getDayAppointments(day: string) {
  return appointments.value.filter((a: any) => {
    const start = a.startTime?.substring(0, 10);
    return start === day;
  });
}

async function handleCreate() {
  await createAppointment(createForm.value);
  ElMessage.success('预约创建成功');
  showCreate.value = false;
  loadAppointments();
}
</script>

<style scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.calendar-cell {
  min-height: 60px;
}

.date-num {
  font-weight: 600;
  margin-bottom: 4px;
  color: #5D4E6D;
  font-size: 13px;
}

.appointment-tag {
  font-size: 11px;
  color: #fff;
  padding: 2px 6px;
  border-radius: 6px;
  margin-bottom: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
  transition: opacity 0.2s;
}

.appointment-tag:hover {
  opacity: 0.8;
}

/* 适配粉色主题日历 */
:deep(.el-calendar) {
  --el-calendar-border: rgba(232, 130, 154, 0.1);
  border-radius: 12px;
}

:deep(.el-calendar__header) {
  padding: 12px 16px;
}

:deep(.el-calendar__title) {
  color: #5D4E6D;
  font-weight: 600;
}

:deep(.el-calendar-table td) {
  border-color: rgba(232, 130, 154, 0.06);
}

:deep(.el-calendar-table td.is-today) {
  color: #E8789A;
  font-weight: 700;
}

:deep(.el-calendar-table td.is-selected) {
  background: rgba(232, 130, 154, 0.06);
}

:deep(.el-calendar-table thead th) {
  color: #A890B0;
  font-weight: 500;
  padding: 8px 0;
}
</style>
