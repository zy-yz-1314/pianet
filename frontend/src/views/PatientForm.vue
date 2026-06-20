<template>
  <div class="page">
    <PageHeader
      :title="isEdit ? '编辑病人档案' : '新建病人档案'"
      :subtitle="isEdit ? '更新联系方式、科室归属与健康史。' : '录入新患者，保存后可追加看诊与挂号。'"
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
        <span class="form-card__title">基本信息</span>
      </div>
      <div class="pn-card__body">
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="patient-form">
          <el-row :gutter="24">
            <el-col :xs="24" :md="12">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="form.name" placeholder="必填" size="large" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="性别" prop="gender">
                <el-select v-model="form.gender" placeholder="可选" clearable style="width: 100%" size="large">
                  <el-option label="男" value="男" />
                  <el-option label="女" value="女" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="出生日期" prop="birthDate">
                <el-date-picker
                  v-model="form.birthDate"
                  type="date"
                  value-format="YYYY-MM-DD"
                  placeholder="可选"
                  style="width: 100%"
                  size="large"
                />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="年龄" prop="age">
                <el-input-number v-model="form.age" :min="0" :max="130" placeholder="可留空自动算" controls-position="right" style="width: 100%" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="手机" prop="phone">
                <el-input v-model="form.phone" placeholder="可选" size="large" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :md="12">
              <el-form-item label="身份证" prop="idCard">
                <el-input v-model="form.idCard" placeholder="可选" size="large" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="住址" prop="address">
                <el-input v-model="form.address" placeholder="可选" size="large" clearable />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
    </div>

    <div class="pn-card form-card form-card--second">
      <div class="pn-card__header">
        <span class="form-card__title">科室与健康史</span>
      </div>
      <div class="pn-card__body">
        <el-form :model="form" label-width="100px" class="patient-form">
          <el-row :gutter="24">
            <el-col :xs="24" :md="12">
              <el-form-item label="科室">
                <el-select v-model="form.departmentId" placeholder="请选择科室" filterable clearable style="width: 100%" size="large">
                  <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col v-if="showArchived" :xs="24" :md="12">
              <el-form-item label="归档">
                <el-switch v-model="form.archived" active-text="已归档（长期未到诊）" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="过敏史">
                <el-input v-model="form.allergyHistory" type="textarea" :rows="2" placeholder="药物或食物过敏等" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="既往史">
                <el-input v-model="form.pastMedicalHistory" type="textarea" :rows="2" placeholder="重大疾病与手术史" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="家族史">
                <el-input v-model="form.familyMedicalHistory" type="textarea" :rows="2" />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="用药史">
                <el-input v-model="form.medicationHistory" type="textarea" :rows="2" placeholder="长期在服药物" />
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
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Close, CircleCheck } from "@element-plus/icons-vue";
import PageHeader from "../components/PageHeader.vue";
import http from "../api/http";
import { usePatientStore } from "../stores/patient";

const props = defineProps({
  id: { type: String, default: undefined },
});

const router = useRouter();
const store = usePatientStore();

const isEdit = computed(() => !!props.id);
const formRef = ref();
const saving = ref(false);
const departments = ref([]);

const showArchived = computed(() => isEdit.value);

const form = reactive({
  name: "",
  gender: "",
  birthDate: "",
  age: null,
  phone: "",
  idCard: "",
  address: "",
  departmentId: undefined,
  allergyHistory: "",
  pastMedicalHistory: "",
  familyMedicalHistory: "",
  medicationHistory: "",
  archived: false,
});

const rules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
};

onMounted(async () => {
  const { data } = await http.get("/departments");
  departments.value = data;
  if (isEdit.value) {
    const d = await store.fetchDetail(props.id);
    form.name = d.name || "";
    form.gender = d.gender || "";
    form.birthDate = d.birthDate || "";
    form.age = d.age ?? null;
    form.phone = d.phone || "";
    form.idCard = d.idCard || "";
    form.address = d.address || "";
    form.departmentId = d.departmentId ?? undefined;
    form.allergyHistory = d.allergyHistory || "";
    form.pastMedicalHistory = d.pastMedicalHistory || "";
    form.familyMedicalHistory = d.familyMedicalHistory || "";
    form.medicationHistory = d.medicationHistory || "";
    form.archived = !!d.archived;
  }
});

function buildPayload() {
  return {
    name: form.name,
    gender: form.gender || null,
    birthDate: form.birthDate || null,
    age: form.age != null ? form.age : null,
    phone: form.phone || null,
    idCard: form.idCard || null,
    address: form.address || null,
    departmentId: form.departmentId ?? null,
    allergyHistory: form.allergyHistory || null,
    pastMedicalHistory: form.pastMedicalHistory || null,
    familyMedicalHistory: form.familyMedicalHistory || null,
    medicationHistory: form.medicationHistory || null,
    archived: showArchived.value ? form.archived : undefined,
  };
}

async function submit() {
  await formRef.value.validate();
  saving.value = true;
  try {
    const payload = buildPayload();
    if (isEdit.value) {
      await store.update(props.id, payload);
      ElMessage.success("已保存");
      router.push(`/patients/${props.id}`);
    } else {
      const created = await store.create(payload);
      ElMessage.success("已创建");
      router.push(`/patients/${created.id}`);
    }
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

.form-card--second {
  margin-top: 20px;
}

.patient-form {
  max-width: 920px;
}
</style>
