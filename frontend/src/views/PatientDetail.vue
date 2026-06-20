<template>
  <div class="page">
    <PageHeader :title="detail?.name || '病人详情'" subtitle="查看基础资料、健康史与历次看诊时间线。">
      <template #actions>
        <el-button round @click="$router.push('/patients')">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <el-button v-if="canStaff" type="primary" round @click="$router.push(`/patients/${id}/edit`)">
          <el-icon><EditPen /></el-icon>
          编辑档案
        </el-button>
        <el-button v-if="canStaff" type="success" round plain @click="addVisit">
          <el-icon><DocumentAdd /></el-icon>
          新增看诊
        </el-button>
        <el-button v-if="isPatientUser" type="info" round plain @click="$router.push('/ai-chat')">
          <el-icon><ChatDotRound /></el-icon>
          AI 咨询
        </el-button>
      </template>
    </PageHeader>

    <div v-loading="loading" class="detail-layout">
      <div class="pn-card profile-card" v-if="detail">
        <div class="profile-card__hero">
          <div class="profile-card__avatar">{{ (detail.name || "?").charAt(0) }}</div>
          <div>
            <h2 class="profile-card__name">{{ detail.name }}</h2>
            <div class="profile-card__chips">
              <el-tag v-if="detail.archived" type="warning" round effect="dark">已归档</el-tag>
              <el-tag v-if="detail.gender" round effect="dark" type="primary">{{ detail.gender }}</el-tag>
              <el-tag v-if="detail.birthDate" round effect="plain">{{ detail.birthDate }}</el-tag>
              <el-tag v-if="detail.age != null" round effect="plain">年龄 {{ detail.age }}</el-tag>
              <el-tag v-if="detail.phone" round effect="plain" type="info">{{ detail.phone }}</el-tag>
              <el-tag v-if="detail.departmentName" round effect="plain" type="success">{{ detail.departmentName }}</el-tag>
            </div>
          </div>
        </div>
        <el-descriptions :column="2" class="profile-desc" border>
          <el-descriptions-item label="住址" :span="2">{{ detail.address || "—" }}</el-descriptions-item>
          <el-descriptions-item label="身份证" :span="2">
            {{ detail.idCard || "—" }}
          </el-descriptions-item>
          <el-descriptions-item label="过敏史" :span="2">{{ detail.allergyHistory || "—" }}</el-descriptions-item>
          <el-descriptions-item label="既往史" :span="2">{{ detail.pastMedicalHistory || "—" }}</el-descriptions-item>
          <el-descriptions-item label="家族史" :span="2">{{ detail.familyMedicalHistory || "—" }}</el-descriptions-item>
          <el-descriptions-item label="用药史" :span="2">{{ detail.medicationHistory || "—" }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="pn-card pn-table visit-block" v-if="detail">
        <div class="pn-card__header visit-block__head">
          <div>
            <span class="visit-block__title">看诊记录</span>
            <span class="visit-block__count">共 {{ (detail.visits || []).length }} 条</span>
          </div>
        </div>
        <div class="pn-card__body">
          <el-table :data="detail.visits || []" stripe empty-text="暂无看诊记录，可点击右上角新增">
            <el-table-column prop="visitDate" label="就诊日" width="118" />
            <el-table-column prop="department" label="科室" width="104" />
            <el-table-column prop="doctorName" label="医生" width="100" />
            <el-table-column label="症状/主诉" min-width="140" show-overflow-tooltip>
              <template #default="{ row }">{{ symptomLine(row) }}</template>
            </el-table-column>
            <el-table-column prop="visitStatus" label="状态" width="88">
              <template #default="{ row }">
                <el-tag v-if="row.visitStatus" size="small" round effect="light">{{ row.visitStatus }}</el-tag>
                <span v-else class="muted">—</span>
              </template>
            </el-table-column>
            <el-table-column prop="diagnosis" label="诊断" min-width="140" show-overflow-tooltip />
            <el-table-column v-if="canStaff" label="操作" width="148" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="$router.push(`/visits/${row.id}/edit`)">
                  <el-icon><EditPen /></el-icon>
                  编辑
                </el-button>
                <el-button link type="danger" @click="removeVisit(row)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessageBox, ElMessage } from "element-plus";
import { ArrowLeft, EditPen, DocumentAdd, Delete, ChatDotRound } from "@element-plus/icons-vue";
import PageHeader from "../components/PageHeader.vue";
import { usePatientStore } from "../stores/patient";
import { useVisitStore } from "../stores/visit";
import { useAuthStore } from "../stores/auth";

const props = defineProps({
  id: { type: String, required: true },
});

const router = useRouter();
const patientStore = usePatientStore();
const visitStore = useVisitStore();
const auth = useAuthStore();

const loading = ref(false);
const detail = ref(null);

const canStaff = computed(() => auth.role === "ADMIN" || auth.role === "DOCTOR");
const isPatientUser = computed(() => auth.role === "PATIENT");

function symptomLine(row) {
  return row.symptoms || row.chiefComplaint || "—";
}

async function load() {
  loading.value = true;
  try {
    detail.value = await patientStore.fetchDetail(props.id);
  } finally {
    loading.value = false;
  }
}

onMounted(load);

function addVisit() {
  router.push({ path: "/visits/new", query: { patientId: props.id } });
}

async function removeVisit(row) {
  try {
    await ElMessageBox.confirm("确定删除该条看诊记录？", "确认", { type: "warning" });
    await visitStore.remove(row.id);
    ElMessage.success("已删除");
    load();
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

.detail-layout {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card__hero {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px 24px 8px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.08) 0%, rgba(34, 211, 238, 0.06) 100%);
  border-radius: var(--pn-radius) var(--pn-radius) 0 0;
  margin: -1px -1px 0;
  border-bottom: 1px solid var(--pn-border);
}

.profile-card__avatar {
  width: 72px;
  height: 72px;
  border-radius: 20px;
  background: linear-gradient(145deg, #6366f1, #22d3ee);
  color: #fff;
  font-size: 28px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12px 28px rgba(99, 102, 241, 0.35);
}

.profile-card__name {
  margin: 0 0 10px;
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.profile-card__chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.profile-desc {
  padding: 16px 24px 20px;
}

.profile-desc :deep(.el-descriptions__label) {
  width: 88px;
  color: var(--pn-text-muted);
  font-weight: 500;
}

.visit-block__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.visit-block__title {
  font-weight: 600;
  font-size: 15px;
  margin-right: 10px;
}

.visit-block__count {
  font-size: 13px;
  color: var(--pn-text-muted);
}

.muted {
  color: var(--pn-text-muted);
}
</style>
