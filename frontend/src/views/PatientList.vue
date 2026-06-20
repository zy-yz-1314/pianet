<template>
  <div class="page">
    <PageHeader title="病人档案" subtitle="维护患者基础信息与健康史，支持跳转详情与关联看诊记录。">
      <template #actions>
        <el-button
          v-if="canCreatePatient"
          type="primary"
          size="large"
          round
          @click="$router.push('/patients/new')"
        >
          <el-icon class="btn-icon"><Plus /></el-icon>
          新建档案
        </el-button>
      </template>
    </PageHeader>

    <el-row :gutter="20" class="stat-row">
      <el-col :xs="24" :sm="12" :md="8">
        <div class="pn-stat">
          <div class="pn-stat__icon pn-stat__icon--indigo">
            <el-icon><User /></el-icon>
          </div>
          <div>
            <p class="pn-stat__label">档案总数</p>
            <p class="pn-stat__value">{{ store.total }}</p>
            <p class="pn-stat__hint">{{ includeArchived ? "含已归档" : "未归档档案" }}</p>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <div class="pn-stat">
          <div class="pn-stat__icon pn-stat__icon--cyan">
            <el-icon><Document /></el-icon>
          </div>
          <div>
            <p class="pn-stat__label">快捷操作</p>
            <p class="pn-stat__value hint-line">详情 · 编辑</p>
            <p class="pn-stat__hint">删除仅管理员可用</p>
          </div>
        </div>
      </el-col>
    </el-row>

    <div class="pn-card pn-table">
      <div class="pn-card__header table-toolbar">
        <span class="table-toolbar__title">全部档案</span>
        <div class="table-toolbar__filters">
          <el-switch
            v-if="canToggleArchive"
            v-model="includeArchived"
            active-text="含归档"
            inactive-text="仅未归档"
            style="--el-switch-on-color: #6366f1"
            @change="reload"
          />
          <el-input
            v-model="keyword"
            clearable
            placeholder="按姓名 / 手机筛选"
            class="table-toolbar__search"
            :prefix-icon="Search"
          />
        </div>
      </div>
      <div class="pn-card__body table-body">
        <el-table v-loading="store.loading" :data="store.list" stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="76" />
          <el-table-column label="姓名" min-width="120">
            <template #default="{ row }">
              <div class="name-cell">
                <span class="name-cell__avatar">{{ (row.name || "?").charAt(0) }}</span>
                <span>{{ row.name }}</span>
                <el-tag v-if="row.archived" size="small" type="info" round effect="plain">归档</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="科室" min-width="100" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.department?.name || "—" }}
            </template>
          </el-table-column>
          <el-table-column prop="gender" label="性别" width="76">
            <template #default="{ row }">
              <el-tag v-if="row.gender" size="small" effect="light" round>{{ row.gender }}</el-tag>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
          <el-table-column prop="birthDate" label="出生日期" width="118" />
          <el-table-column prop="phone" label="手机" min-width="128" />
          <el-table-column prop="idCard" label="身份证" min-width="168" show-overflow-tooltip />
          <el-table-column label="操作" width="268" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="$router.push(`/patients/${row.id}`)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button v-if="canEditPatient" link type="primary" @click="$router.push(`/patients/${row.id}/edit`)">
                <el-icon><EditPen /></el-icon>
                编辑
              </el-button>
              <el-button v-if="isAdmin" link type="danger" @click="onDelete(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="store.total > pageSize" class="table-pager">
          <el-pagination
            v-model:current-page="page"
            layout="total, prev, pager, next"
            :total="store.total"
            :page-size="pageSize"
            background
            @current-change="onPageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import { Plus, User, Document, View, EditPen, Delete } from "@element-plus/icons-vue";
import PageHeader from "../components/PageHeader.vue";
import { usePatientStore } from "../stores/patient";
import { useAuthStore } from "../stores/auth";

const store = usePatientStore();
const auth = useAuthStore();
const keyword = ref("");
const page = ref(1);
const pageSize = 10;
const includeArchived = ref(false);

const isAdmin = computed(() => auth.role === "ADMIN");
const canEditPatient = computed(() => auth.role === "ADMIN" || auth.role === "DOCTOR");
const canCreatePatient = computed(() => canEditPatient.value);
const canToggleArchive = computed(() => auth.role === "ADMIN" || auth.role === "DOCTOR");

async function reload() {
  await store.fetchList(includeArchived.value, page.value - 1, pageSize, keyword.value.trim());
}

function onPageChange(p) {
  page.value = p;
  reload();
}

watch(keyword, () => {
  page.value = 1;
  reload();
});

watch(includeArchived, () => {
  page.value = 1;
  reload();
});

onMounted(reload);

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除病人「${row.name}」？关联看诊记录将一并删除。`, "确认删除", {
      type: "warning",
      confirmButtonText: "删除",
      cancelButtonText: "取消",
    });
    await store.remove(row.id);
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

.table-toolbar__filters {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.table-toolbar__search {
  width: 260px;
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

.btn-icon {
  margin-right: 6px;
}

.name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.name-cell__avatar {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.muted {
  color: var(--pn-text-muted);
}
</style>
