<template>
  <div class="page visit-page">
    <PageHeader title="看诊记录" subtitle="按时间查看就诊流水，可按病人筛选（症状字段与后端 symptoms 对齐）。" />

    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :md="8">
        <div class="pn-stat">
          <div class="pn-stat__icon pn-stat__icon--cyan">
            <el-icon><Calendar /></el-icon>
          </div>
          <div>
            <p class="pn-stat__label">记录条数</p>
            <p class="pn-stat__value">{{ visitStore.total }}</p>
            <p class="pn-stat__hint">当前筛选条件下</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <div class="pn-stat">
          <div class="pn-stat__icon pn-stat__icon--indigo">
            <el-icon><Filter /></el-icon>
          </div>
          <div>
            <p class="pn-stat__label">病人筛选</p>
            <p class="pn-stat__value hint-line">{{ filterPatientId ? "已启用" : "全部" }}</p>
            <p class="pn-stat__hint">下拉可快速定位某位患者</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="pn-card pn-table">
      <div class="pn-card__header table-toolbar">
        <span class="table-toolbar__title">就诊列表</span>
        <el-select
          v-model="filterPatientId"
          clearable
          placeholder="按病人筛选"
          filterable
          class="table-toolbar__select"
          size="large"
          @change="reload"
        >
          <el-option v-for="p in patientOptions" :key="p.id" :label="`${p.name}（#${p.id}）`" :value="p.id" />
        </el-select>
      </div>
      <div class="pn-card__body table-body">
        <el-table v-loading="visitStore.loading" :data="visitStore.list" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="76" />
          <el-table-column label="病人" min-width="140">
            <template #default="{ row }">
              <div class="patient-cell">
                <span class="patient-cell__dot" />
                <span>{{ row.patient?.name || "—" }}</span>
                <el-button link type="primary" @click="$router.push(`/patients/${row.patient?.id}`)">
                  档案
                </el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="visitDate" label="就诊日" width="118" />
          <el-table-column prop="department" label="科室" width="104">
            <template #default="{ row }">
              <el-tag v-if="row.department" size="small" effect="light" round>{{ row.department }}</el-tag>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="doctorName" label="医生" width="100" />
          <el-table-column label="症状" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ symptomLine(row) }}</template>
          </el-table-column>
          <el-table-column prop="visitStatus" label="状态" width="92">
            <template #default="{ row }">
              <el-tag v-if="row.visitStatus" size="small" round effect="plain">{{ row.visitStatus }}</el-tag>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="diagnosis" label="诊断" min-width="168" show-overflow-tooltip />
          <el-table-column v-if="canStaff" label="操作" width="168" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="$router.push(`/visits/${row.id}/edit`)">
                <el-icon><EditPen /></el-icon>
                编辑
              </el-button>
              <el-button link type="danger" @click="onDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
          <el-table-column v-else label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="$router.push(`/patients/${row.patient?.id}`)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="visitStore.total > pageSize" class="table-pager">
          <el-pagination
            v-model:current-page="page"
            layout="total, prev, pager, next"
            :total="visitStore.total"
            :page-size="pageSize"
            background
            @current-change="onPageChange"
          />
        </div>
      </div>
    </div>

    <el-button v-if="canStaff" class="visit-fab" type="primary" circle size="large" @click="goNew">
      <el-icon><Plus /></el-icon>
    </el-button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessageBox, ElMessage } from "element-plus";
import { Plus, Calendar, Filter, EditPen, Delete } from "@element-plus/icons-vue";
import PageHeader from "../components/PageHeader.vue";
import { useVisitStore } from "../stores/visit";
import { usePatientStore } from "../stores/patient";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const visitStore = useVisitStore();
const patientStore = usePatientStore();
const auth = useAuthStore();

const filterPatientId = ref(undefined);
const patientOptions = ref([]);
const page = ref(1);
const pageSize = 10;

const canStaff = computed(() => auth.role === "ADMIN" || auth.role === "DOCTOR");

function symptomLine(row) {
  return row.symptoms || row.chiefComplaint || "—";
}

async function reload() {
  await visitStore.fetchList(filterPatientId.value, page.value - 1, pageSize);
}

function onPageChange(p) {
  page.value = p;
  reload();
}

watch(filterPatientId, () => {
  page.value = 1;
  reload();
});

onMounted(async () => {
  await patientStore.fetchList(true, 0, 1000);
  patientOptions.value = patientStore.list;
  await reload();
});

function goNew() {
  const q = filterPatientId.value ? { patientId: String(filterPatientId.value) } : {};
  router.push({ path: "/visits/new", query: q });
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm("确定删除该条看诊记录？", "确认删除", {
      type: "warning",
      confirmButtonText: "删除",
      cancelButtonText: "取消",
    });
    await visitStore.remove(row.id);
    ElMessage.success("已删除");
    reload();
  } catch (e) {
    if (e !== "cancel") throw e;
  }
}
</script>

<style scoped>
.page {
  animation: fade-in 0.35s ease;
}
@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

.visit-page {
  position: relative;
  padding-bottom: 72px;
}

.stat-row {
  margin-bottom: 24px;
}

.hint-line {
  font-size: 15px !important;
  font-weight: 600 !important;
  color: var(--pn-text-muted) !important;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.table-toolbar__title {
  font-weight: 600;
  font-size: 15px;
  color: var(--pn-text);
}

.table-toolbar__select {
  width: 280px;
  max-width: 100%;
}

.table-body {
  padding-top: 8px;
}

.table-pager {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 8px;
}

.patient-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.patient-cell__dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: linear-gradient(135deg, #22d3ee, #6366f1);
  flex-shrink: 0;
}

.muted {
  color: var(--pn-text-muted);
}

.visit-fab {
  position: fixed;
  right: 28px;
  bottom: 28px;
  z-index: 20;
  box-shadow: 0 12px 32px rgba(99, 102, 241, 0.4);
}
</style>
