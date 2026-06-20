<template>
  <div class="page">
    <PageHeader title="药品管理" subtitle="维护药品库，供处方选择与合规提示（设计方案·药品模块）。">
      <template #actions>
        <el-button type="primary" round @click="openNew">新增药品</el-button>
      </template>
    </PageHeader>
    <div class="pn-card pn-table">
      <div class="pn-card__body">
        <el-table :data="rows" v-loading="loading" stripe>
          <el-table-column prop="id" width="72" label="ID" />
          <el-table-column prop="name" label="名称" min-width="140" />
          <el-table-column prop="category" label="类别" width="100" />
          <el-table-column prop="specification" label="规格" width="120" />
          <el-table-column prop="stock" label="库存" width="80" />
          <el-table-column width="160" label="操作">
            <template #default="{ row }">
              <el-button link type="primary" @click="edit(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <el-dialog v-model="dlg" :title="editingId ? '编辑药品' : '新增药品'" width="520px">
      <el-form label-width="88px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类别"><el-input v-model="form.category" /></el-form-item>
        <el-form-item label="规格"><el-input v-model="form.specification" /></el-form-item>
        <el-form-item label="用法用量"><el-input v-model="form.usageText" /></el-form-item>
        <el-form-item label="禁忌"><el-input v-model="form.taboo" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="form.stock" :min="0" /></el-form-item>
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
const form = ref({
  name: "",
  category: "",
  specification: "",
  usageText: "",
  taboo: "",
  stock: 0,
});

async function load() {
  loading.value = true;
  try {
    const { data } = await http.get("/medicines");
    rows.value = data;
  } finally {
    loading.value = false;
  }
}

function openNew() {
  editingId.value = null;
  form.value = { name: "", category: "", specification: "", usageText: "", taboo: "", stock: 0 };
  dlg.value = true;
}

function edit(row) {
  editingId.value = row.id;
  form.value = {
    name: row.name,
    category: row.category,
    specification: row.specification,
    usageText: row.usageText,
    taboo: row.taboo,
    stock: row.stock ?? 0,
  };
  dlg.value = true;
}

async function save() {
  if (editingId.value) {
    await http.put(`/medicines/${editingId.value}`, form.value);
  } else {
    await http.post("/medicines", form.value);
  }
  ElMessage.success("已保存");
  dlg.value = false;
  load();
}

async function remove(row) {
  await ElMessageBox.confirm(`删除药品「${row.name}」？`, "确认", { type: "warning" });
  await http.delete(`/medicines/${row.id}`);
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
