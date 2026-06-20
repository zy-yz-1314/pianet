<template>
  <div class="page">
    <PageHeader title="挂号 / 预约" subtitle="选择医生排班提交挂号；医生可更新接诊状态（待就诊/就诊中/已完成）。">
      <template #actions>
        <el-button v-if="canBook" type="primary" round @click="openBook">我要挂号</el-button>
      </template>
    </PageHeader>

    <div class="pn-card">
      <div class="pn-card__header">
        <span>我的挂号记录</span>
      </div>
      <div class="pn-card__body">
        <el-table v-loading="loading" :data="regs" stripe>
          <el-table-column prop="registrationNo" label="单号" min-width="160" />
          <el-table-column label="医生" width="100">
            <template #default="{ row }">{{ row.doctorProfile?.user?.name }}</template>
          </el-table-column>
          <el-table-column label="日期" width="110">
            <template #default="{ row }">{{ row.schedule?.workDate }}</template>
          </el-table-column>
          <el-table-column label="时段" width="140">
            <template #default="{ row }">
              {{ row.schedule?.startTime }} - {{ row.schedule?.endTime }}
            </template>
          </el-table-column>
          <el-table-column prop="queueNo" label="排队号" width="88" />
          <el-table-column prop="status" label="状态" width="100" />
          <el-table-column v-if="auth.role === 'DOCTOR' || auth.role === 'ADMIN'" label="操作" width="220">
            <template #default="{ row }">
              <el-button link type="primary" @click="setSt(row, '就诊中')">就诊中</el-button>
              <el-button link type="success" @click="setSt(row, '已完成')">完成</el-button>
            </template>
          </el-table-column>
          <el-table-column v-if="auth.role === 'PATIENT'" label="操作" width="100">
            <template #default="{ row }">
              <el-button v-if="row.status === '待就诊'" link type="danger" @click="cancel(row)">取消</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="dlg" title="选择排班挂号" width="480px" @open="loadSchedules">
      <el-form label-width="88px">
        <el-form-item label="病人">
          <el-select
            v-if="auth.role === 'ADMIN' || auth.role === 'DOCTOR'"
            v-model="book.patientId"
            filterable
            style="width: 100%"
          >
            <el-option v-for="p in patients" :key="p.id" :label="`${p.name} #${p.id}`" :value="p.id" />
          </el-select>
          <span v-else>{{ auth.displayName }}（档案 #{{ auth.patientId }}）</span>
        </el-form-item>
        <el-form-item label="排班">
          <el-select v-model="book.scheduleId" filterable style="width: 100%">
            <el-option
              v-for="s in schedules"
              :key="s.id"
              :label="`${s.doctorProfile?.user?.name} · ${s.workDate} ${s.startTime}-${s.endTime} 余${s.maxNum - s.currentNum}`"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" :loading="booking" @click="doBook">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import PageHeader from "../components/PageHeader.vue";
import http from "../api/http";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const loading = ref(false);
const regs = ref([]);
const schedules = ref([]);
const patients = ref([]);
const dlg = ref(false);
const booking = ref(false);
const book = ref({ patientId: undefined, scheduleId: undefined });

const canBook = computed(
  () => auth.role === "PATIENT" || auth.role === "DOCTOR" || auth.role === "ADMIN"
);

async function load() {
  loading.value = true;
  try {
    const { data } = await http.get("/registrations", { params: { page: 0, size: 500 } });
    regs.value = data.content || [];
  } finally {
    loading.value = false;
  }
}

async function loadSchedules() {
  const { data } = await http.get("/schedules", { params: { from: new Date().toISOString().slice(0, 10) } });
  schedules.value = data.content || data || [];
  if (auth.role === "PATIENT") {
    book.value.patientId = auth.patientId;
  }
  if (auth.role === "ADMIN" || auth.role === "DOCTOR") {
    const { data: plist } = await http.get("/patients", { params: { includeArchived: true, page: 0, size: 1000 } });
    patients.value = plist.content || [];
  }
}

function openBook() {
  book.value = { patientId: auth.role === "PATIENT" ? auth.patientId : undefined, scheduleId: undefined };
  dlg.value = true;
}

async function doBook() {
  if (!book.value.scheduleId || !book.value.patientId) {
    ElMessage.warning("请选择排班与病人");
    return;
  }
  booking.value = true;
  try {
    await http.post("/registrations", {
      scheduleId: book.value.scheduleId,
      patientId: book.value.patientId,
    });
    ElMessage.success("挂号成功");
    dlg.value = false;
    load();
  } finally {
    booking.value = false;
  }
}

async function setSt(row, st) {
  await http.put(`/registrations/${row.id}/status`, { status: st });
  ElMessage.success("已更新");
  load();
}

async function cancel(row) {
  await ElMessageBox.confirm("确定取消该挂号？", "确认", { type: "warning" });
  await http.delete(`/registrations/${row.id}`);
  ElMessage.success("已取消");
  load();
}

onMounted(load);
</script>

<style scoped>
.page {
  animation: fade-in 0.35s ease;
}
@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}
</style>
