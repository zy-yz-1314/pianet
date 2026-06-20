import { defineStore } from "pinia";
import { ref, computed } from "vue";
import http, { TOKEN_KEY } from "../api/http";

const USER_KEY = "PCMS_USER";

export const useAuthStore = defineStore("auth", () => {
  const token = ref(sessionStorage.getItem(TOKEN_KEY) || "");
  const username = ref("");
  const displayName = ref("");
  const role = ref("");
  const patientId = ref(null);
  const doctorProfileId = ref(null);

  const isLoggedIn = computed(() => !!token.value);

  function hydrateFromStorage() {
    const raw = sessionStorage.getItem(USER_KEY);
    if (raw) {
      try {
        const u = JSON.parse(raw);
        username.value = u.username || "";
        displayName.value = u.name || "";
        role.value = u.role || "";
        patientId.value = u.patientId ?? null;
        doctorProfileId.value = u.doctorProfileId ?? null;
      } catch (_) {
        /* ignore */
      }
    }
  }

  function persistUser(profile) {
    sessionStorage.setItem(
      USER_KEY,
      JSON.stringify({
        username: profile.username,
        name: profile.name,
        role: profile.role,
        patientId: profile.patientId,
        doctorProfileId: profile.doctorProfileId,
      })
    );
  }

  function applyLoginResponse(data) {
    token.value = data.token;
    username.value = data.username;
    displayName.value = data.name;
    role.value = data.role;
    patientId.value = data.patientId ?? null;
    doctorProfileId.value = data.doctorProfileId ?? null;
    sessionStorage.setItem(TOKEN_KEY, data.token);
    persistUser({
      username: data.username,
      name: data.name,
      role: data.role,
      patientId: data.patientId,
      doctorProfileId: data.doctorProfileId,
    });
  }

  hydrateFromStorage();

  async function login(usernameVal, password) {
    const { data } = await http.post("/auth/login", {
      username: usernameVal.trim(),
      password,
    });
    applyLoginResponse(data);
    return data;
  }

  async function register(payload) {
    const { data } = await http.post("/auth/register", payload);
    applyLoginResponse(data);
    return data;
  }

  async function logout() {
    try {
      await http.post("/auth/logout");
    } catch (_) {
      /* ignore */
    }
    token.value = "";
    username.value = "";
    displayName.value = "";
    role.value = "";
    patientId.value = null;
    doctorProfileId.value = null;
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_KEY);
  }

  /** 刷新后补全用户信息（令牌仍有效） */
  async function restoreFromServer() {
    if (!token.value) return;
    try {
      const { data } = await http.get("/me");
      username.value = data.username;
      displayName.value = data.name;
      role.value = data.role;
      patientId.value = data.patientId ?? null;
      doctorProfileId.value = data.doctorProfileId ?? null;
      persistUser({
        username: data.username,
        name: data.name,
        role: data.role,
        patientId: data.patientId,
        doctorProfileId: data.doctorProfileId,
      });
    } catch (_) {
      await logout();
    }
  }

  return {
    token,
    username,
    displayName,
    role,
    patientId,
    doctorProfileId,
    isLoggedIn,
    login,
    register,
    logout,
    restoreFromServer,
  };
});
