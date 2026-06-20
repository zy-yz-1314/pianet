import { defineStore } from "pinia";
import { ref } from "vue";
import http from "../api/http";

export const useVisitStore = defineStore("visit", () => {
  const list = ref([]);
  const loading = ref(false);
  const total = ref(0);
  const currentPage = ref(0);
  const pageSize = ref(10);

  async function fetchList(patientId, page = 0, size = 10) {
    loading.value = true;
    try {
      const params = patientId != null ? { patientId, page, size } : { page, size };
      const { data } = await http.get("/visits", { params });
      list.value = data.content;
      total.value = data.totalElements;
      currentPage.value = data.page;
      pageSize.value = data.size;
    } finally {
      loading.value = false;
    }
  }

  async function getOne(id) {
    const { data } = await http.get(`/visits/${id}`);
    return data;
  }

  async function create(payload) {
    const { data } = await http.post("/visits", payload);
    return data;
  }

  async function update(id, payload) {
    const { data } = await http.put(`/visits/${id}`, payload);
    return data;
  }

  async function remove(id) {
    await http.delete(`/visits/${id}`);
  }

  return { list, loading, total, currentPage, pageSize, fetchList, getOne, create, update, remove };
});
