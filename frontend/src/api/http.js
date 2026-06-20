import axios from "axios";
import { ElMessage } from "element-plus";

const TOKEN_KEY = "PCMS_TOKEN";

const http = axios.create({
  baseURL: "/api",
  timeout: 60000,
});

http.interceptors.request.use((config) => {
  const t = sessionStorage.getItem(TOKEN_KEY);
  if (t) {
    config.headers.Authorization = `Bearer ${t}`;
  }
  return config;
});

http.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      sessionStorage.removeItem(TOKEN_KEY);
      sessionStorage.removeItem("PCMS_USER");
      if (typeof window !== "undefined" && !window.location.pathname.endsWith("/login")) {
        window.location.assign("/login");
      }
      return Promise.reject(err);
    }
    const msg = err.response?.data?.message || err.message || "请求失败";
    ElMessage.error(msg);
    return Promise.reject(err);
  }
);

export default http;
export { TOKEN_KEY };
