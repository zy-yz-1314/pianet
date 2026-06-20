<template>
  <div class="page">
    <PageHeader title="科室管理" subtitle="管理员维护科室信息（设计方案·系统管理模块）。">
      <template #actions>
        <el-button type="primary" round @click="openNew">新增科室</el-button>
      </template>
    </PageHeader>
    <div class="pn-card pn-table">
      <div class="pn-card__body">
        <el-table :data="rows" v-loading="loading" stripe>
          <el-table-column prop="id" width="72" label="ID" />
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="introduction" label="简介" show-overflow-tooltip />
          <el-table-column width="160" label="操作">
            <template #default="{ row }">
              <el-button link type="primary" @click="edit(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="dlg" :title="editingId ? '编辑科室' : '新增科室'" width="480px">
      <el-form label-width="72px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.introduction" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import PageHeader from "../components/PageHeader.vue";
import http from "../api/http";

const rows = ref([]);
const loading = ref(false);
const dlg = ref(false);
const editingId = ref(null);
const form = ref({ name: "", introduction: "" });

async function load() {
  loading.value = true;
  try {
    const { data } = await http.get("/departments");
    rows.value = data;
  } finally {
    loading.value = false;
  }
}

function openNew() {
  editingId.value = null;
  form.value = { name: "", introduction: "" };
  dlg.value = true;
}

function edit(row) {
  editingId.value = row.id;
  form.value = { name: row.name, introduction: row.introduction || "" };
  dlg.value = true;
}

async function save() {
  if (editingId.value) {
    await http.put(`/departments/${editingId.value}`, form.value);
  } else {
    await http.post("/departments", form.value);
  }
  ElMessage.success("已保存");
  dlg.value = false;
  load();
}

async function remove(row) {
  await ElMessageBox.confirm(`删除科室「${row.name}」？`, "确认", { type: "warning" });
  await http.delete(`/departments/${row.id}`);
  ElMessage.success("已删除");
  load();
}

onMounted(load);
</script>

<style scoped>
.page {
  animation: fade-in 0.35s ease;
}
</style>
