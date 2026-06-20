import { defineStore } from "pinia";
import { ref } from "vue";
import http from "../api/http";

export const usePatientStore = defineStore("patient", () => {
  const list = ref([]);
  const current = ref(null);
  const loading = ref(false);
  const total = ref(0);
  const currentPage = ref(0);
  const pageSize = ref(10);

  async function fetchList(includeArchived = false, page = 0, size = 10, keyword = "") {
    loading.value = true;
    try {
      const params = { includeArchived, page, size };
      if (keyword) params.keyword = keyword;
      const { data } = await http.get("/patients", { params });
      list.value = data.content;
      total.value = data.totalElements;
      currentPage.value = data.page;
      pageSize.value = data.size;
    } finally {
      loading.value = false;
    }
  }

  async function fetchDetail(id) {
    loading.value = true;
    try {
      const { data } = await http.get(`/patients/${id}`);
      current.value = data;
      return data;
    } finally {
      loading.value = false;
    }
  }

  async function create(payload) {
    const { data } = await http.post("/patients", payload);
    return data;
  }

  async function update(id, payload) {
    const { data } = await http.put(`/patients/${id}`, payload);
    return data;
  }

  async function remove(id) {
    await http.delete(`/patients/${id}`);
  }

  return { list, current, loading, total, currentPage, pageSize, fetchList, fetchDetail, create, update, remove };
});
