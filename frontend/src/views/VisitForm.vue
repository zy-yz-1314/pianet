<template>
  <div class="page">
    <PageHeader
      :title="isEdit ? '编辑看诊记录' : '新建看诊记录'"
      :subtitle="isEdit ? '修改症状、体征、诊断与医嘱等信息。' : '为选定病人登记一次新的就诊内容。'"
    >
      <template #actions>
        <el-button round @click="$router.back()">
          <el-icon><Close /></el-icon>
          取消
        </el-button>
        <el-button type="primary" round :loading="saving" @click="submit">
          <el-icon><CircleCheck /></el-icon>
          保存
        </el-button>
      </template>
    </PageHeader>

    <div class="pn-card form-card">
      <div class="pn-card__header">
        <span class="form-card__title">就诊信息</span>
        <el-tag v-if="lockedPatient" type="info" effect="plain" round>已从病人详情带入病人，不可更改</el-tag>
      </div>
      <div class="pn-card__body">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="visit-form">
          <el-row :gutter="24">
            <el-col :xs="24" :md="12">
              <el-form-item label="病人" prop="patientId">
                <el-select
                  v-model="form.patientId"
                  filterable
                  placeholder="请选择病人"
                  style="width: 100%"
                  size="large"
                  :disabled="lockedPatient"
                >
                  <el-option v-for="p in patients" :key="p.id" :label="`${p.name}（#${p.id}）`" :value="p.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="就诊日" prop="visitDate">
                <el-date-picker
                  v-model="form.visitDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="默认今天"
                  style="width: 100%"
                  size="large"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="关联挂号 ID">
                <el-input v-model="form.registrationInput" clearable placeholder="可选，整数" size="large" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="医生档案 ID">
                <el-input v-model="form.doctorProfileInput" clearable placeholder="可选，自动带出医生名与科室" size="large" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="科室" prop="department">
                <el-select
                  v-model="form.department"
                  filterable
                  allow-create
                  clearable
                  placeholder="选择或输入科室"
                  style="width: 100%"
                  size="large"
                >
                  <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.name" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="医生" prop="doctorName">
                <el-select
                  v-model="form.doctorName"
                  filterable
                  allow-create
                  clearable
                  placeholder="选择或输入医生"
                  style="width: 100%"
                  size="large"
                >
                  <el-option v-for="doc in doctors" :key="doc.id" :label="`${doc.user?.name || doc.name}（${doc.title || ''}）`" :value="doc.user?.name || doc.name" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="就诊状态">
                <el-select v-model="form.visitStatus" placeholder="默认已完成" style="width: 100%" size="large">
                  <el-option label="就诊中" value="就诊中" />
                  <el-option label="已完成" value="已完成" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="症状/主诉" prop="symptoms">
                <el-input v-model="form.symptoms" type="textarea" :rows="2" placeholder="症状描述" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="体征" prop="physicalSigns">
                <el-input v-model="form.physicalSigns" type="textarea" :rows="2" placeholder="体格检查要点" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="诊断" prop="diagnosis">
                <el-input v-model="form.diagnosis" type="textarea" :rows="2" placeholder="临床诊断" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="处方" prop="prescription">
                <el-input v-model="form.prescription" type="textarea" :rows="3" placeholder="用药与处置" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="医嘱/建议" prop="advice">
                <el-input v-model="form.advice" type="textarea" :rows="2" placeholder="医嘱、复诊提醒等" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="备注" prop="notes">
                <el-input v-model="form.notes" type="textarea" :rows="2" placeholder="其他说明" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Close, CircleCheck } from "@element-plus/icons-vue";
import PageHeader from "../components/PageHeader.vue";
import http from "../api/http";
import { usePatientStore } from "../stores/patient";
import { useVisitStore } from "../stores/visit";

const props = defineProps({
  id: { type: String, default: undefined },
});

const route = useRoute();
const router = useRouter();
const patientStore = usePatientStore();
const visitStore = useVisitStore();

const isEdit = computed(() => !!props.id);
const formRef = ref();
const saving = ref(false);
const patients = ref([]);
const lockedPatient = ref(false);
const departments = ref([]);
const doctors = ref([]);

const form = reactive({
  patientId: undefined,
  visitDate: "",
  registrationInput: "",
  doctorProfileInput: "",
  department: "",
  doctorName: "",
  visitStatus: "已完成",
  symptoms: "",
  physicalSigns: "",
  diagnosis: "",
  prescription: "",
  advice: "",
  notes: "",
});

const rules = {
  patientId: [{ required: true, message: "请选择病人", trigger: "change" }],
};

function todayStr() {
  const d = new Date();
  const p = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

function numOrNull(raw) {
  if (raw === "" || raw === undefined || raw === null) return null;
  const n = Number(String(raw).trim());
  return Number.isFinite(n) ? n : null;
}

onMounted(async () => {
  await patientStore.fetchList(true, 0, 1000);
  patients.value = patientStore.list;
  const deptRes = await http.get("/departments");
  departments.value = deptRes.data;
  const schedRes = await http.get("/schedules", { params: { from: new Date().toISOString().slice(0, 10) } });
  const scheds = schedRes.data.content || schedRes.data || [];
  const seen = new Set();
  doctors.value = [];
  for (const s of scheds) {
    const dp = s.doctorProfile;
    if (dp && !seen.has(dp.id)) {
      seen.add(dp.id);
      doctors.value.push(dp);
    }
  }

  const qPid = route.query.patientId;
  if (!isEdit.value && qPid) {
    form.patientId = Number(qPid);
    lockedPatient.value = true;
  }

  if (isEdit.value) {
    const v = await visitStore.getOne(props.id);
    form.patientId = v.patient?.id;
    form.visitDate = v.visitDate || "";
    form.department = v.department || "";
    form.doctorName = v.doctorName || "";
    form.visitStatus = v.visitStatus || "已完成";
    form.symptoms = v.symptoms || v.chiefComplaint || "";
    form.physicalSigns = v.physicalSigns || "";
    form.diagnosis = v.diagnosis || "";
    form.prescription = v.prescription || "";
    form.advice = v.advice || "";
    form.notes = v.notes || "";
    form.registrationInput =
      v.registration?.id !== undefined && v.registration?.id !== null ? String(v.registration.id) : "";
    form.doctorProfileInput =
      v.doctorProfile?.id !== undefined && v.doctorProfile?.id !== null ? String(v.doctorProfile.id) : "";
  } else if (!form.visitDate) {
    form.visitDate = todayStr();
  }
});

function buildPayload() {
  const sym = form.symptoms?.trim() || null;
  return {
    patientId: form.patientId,
    visitDate: form.visitDate || null,
    registrationId: numOrNull(form.registrationInput),
    doctorProfileId: numOrNull(form.doctorProfileInput),
    department: form.department || null,
    doctorName: form.doctorName || null,
    visitStatus: form.visitStatus || null,
    symptoms: sym,
    chiefComplaint: sym,
    physicalSigns: form.physicalSigns || null,
    diagnosis: form.diagnosis || null,
    prescription: form.prescription || null,
    advice: form.advice || null,
    notes: form.notes || null,
  };
}

async function submit() {
  await formRef.value.validate();
  saving.value = true;
  try {
    const payload = buildPayload();
    if (isEdit.value) {
      await visitStore.update(props.id, payload);
      ElMessage.success("已保存");
    } else {
      await visitStore.create(payload);
      ElMessage.success("已创建");
    }
    const pid = form.patientId;
    router.push(`/patients/${pid}`);
  } finally {
    saving.value = false;
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

.form-card__title {
  font-weight: 600;
  font-size: 15px;
}

.pn-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.visit-form {
  max-width: 920px;
}
</style>
