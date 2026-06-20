<template>
  <router-view v-if="isLoginPage" />
  <el-container v-else class="app-shell">
    <el-aside :width="sidebarWidth" class="sidebar" :class="{ 'sidebar--collapsed': sidebarCollapsed }">
      <div class="sidebar__brand">
        <div class="sidebar__logo">
          <span class="sidebar__logo-mark">P</span>
        </div>
        <div v-show="!sidebarCollapsed" class="sidebar__brand-text">
          <span class="sidebar__app">PCMS</span>
          <span class="sidebar__tag">病人看诊管理系统</span>
        </div>
        <el-button
          class="sidebar__collapse"
          :aria-label="sidebarCollapsed ? '展开菜单' : '收起菜单'"
          text
          circle
          @click="sidebarCollapsed = !sidebarCollapsed"
        >
          <el-icon :size="18">
            <Fold v-if="!sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
        </el-button>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        class="sidebar__menu"
        router
        background-color="transparent"
        text-color="rgba(226,232,240,0.72)"
        active-text-color="#fff"
      >
        <el-menu-item v-for="item in navItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.label }}</template>
        </el-menu-item>
      </el-menu>
      <div class="sidebar__footer">
        <p v-show="!sidebarCollapsed" class="sidebar__foot-note">院内使用 · 数据请妥善保管</p>
      </div>
    </el-aside>

    <el-container class="main-column">
      <el-header class="topbar">
        <div class="topbar__left">
          <span class="topbar__crumb">{{ pageLabel }}</span>
        </div>
        <div class="topbar__right">
          <el-badge is-dot class="topbar__notify">
            <el-button circle plain class="topbar__notify-btn">
              <el-icon><Bell /></el-icon>
            </el-button>
          </el-badge>
          <div class="topbar__pill">
            <el-icon class="topbar__pill-icon"><CircleCheck /></el-icon>
            <span class="topbar__name">{{ auth.displayName || auth.username }}</span>
            <span class="topbar__dot" />
            <span class="topbar__role">{{ roleLabel }}</span>
          </div>
          <el-button class="topbar__logout" round @click="onLogout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </el-header>
      <el-main class="main-area">
        <div class="main-area__inner">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  User,
  Document,
  CircleCheck,
  SwitchButton,
  Fold,
  Expand,
  Odometer,
  Calendar,
  ChatDotRound,
  OfficeBuilding,
  FirstAidKit,
  Bell,
} from "@element-plus/icons-vue";
import { useAuthStore } from "./stores/auth";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const sidebarCollapsed = ref(false);
const sidebarWidth = computed(() => (sidebarCollapsed.value ? "76px" : "252px"));

const isLoginPage = computed(() => route.path === "/login" || route.path === "/register");

const navItems = computed(() => {
  const r = auth.role;
  const entries = [
    { path: "/dashboard", label: "统计控制台", icon: Odometer, roles: null },
    { path: "/patients", label: "病人档案", icon: User, roles: null },
    { path: "/visits", label: "看诊记录", icon: Document, roles: null },
    { path: "/registrations", label: "挂号预约", icon: Calendar, roles: null },
    { path: "/ai-chat", label: "AI 咨询", icon: ChatDotRound, roles: null },
    { path: "/admin/departments", label: "科室管理", icon: OfficeBuilding, roles: ["ADMIN"] },
    { path: "/admin/medicines", label: "药品字典", icon: FirstAidKit, roles: ["ADMIN"] },
  ];
  return entries.filter((i) => !i.roles || i.roles.includes(r));
});

const activeMenu = computed(() => {
  const p = route.path;
  let best = "";
  for (const i of navItems.value) {
    if (p === i.path || p.startsWith(i.path + "/")) {
      if (i.path.length > best.length) {
        best = i.path;
      }
    }
  }
  return best || "/dashboard";
});

const roleLabel = computed(() => {
  switch (auth.role) {
    case "ADMIN":
      return "管理员";
    case "DOCTOR":
      return "医生";
    case "PATIENT":
      return "病人";
    default:
      return auth.role || "—";
  }
});

const pageLabel = computed(() => {
  const p = route.path;
  if (p === "/dashboard" || p.startsWith("/dashboard")) return "统计控制台";
  if (p === "/patients" || p === "/patients/") return "病人档案";
  if (p.startsWith("/patients/new")) return "新建病人档案";
  if (/\/patients\/\d+\/edit$/.test(p)) return "编辑病人档案";
  if (/\/patients\/\d+$/.test(p)) return "病人详情";
  if (p === "/visits" || p === "/visits/") return "看诊记录";
  if (p.startsWith("/visits/new")) return "新建看诊记录";
  if (/\/visits\/\d+\/edit$/.test(p)) return "编辑看诊记录";
  if (p.startsWith("/registrations")) return "挂号预约";
  if (p.startsWith("/ai-chat")) return "AI 咨询";
  if (p.startsWith("/admin/departments")) return "科室管理";
  if (p.startsWith("/admin/medicines")) return "药品字典";
  return "工作台";
});

async function onLogout() {
  await auth.logout();
  router.replace("/login");
}
</script>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
}

.app-shell {
  min-height: 100%;
  background: var(--pn-bg);
}

.sidebar {
  background: linear-gradient(180deg, #0c1222 0%, #151b2e 48%, #0f172a 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  transition: width 0.22s ease;
}

.sidebar--collapsed .sidebar__brand {
  padding-left: 16px;
  padding-right: 12px;
}

.sidebar__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 18px 20px 22px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  position: relative;
}

.sidebar__collapse {
  margin-left: auto;
  color: rgba(226, 232, 240, 0.65) !important;
  flex-shrink: 0;
}

.sidebar__collapse:hover {
  color: #fff !important;
}

.sidebar__logo {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #6366f1 0%, #22d3ee 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(99, 102, 241, 0.35);
  flex-shrink: 0;
}

.sidebar__logo-mark {
  font-weight: 800;
  font-size: 20px;
  color: #fff;
  font-family: var(--pn-font);
}

.sidebar__brand-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.sidebar__app {
  font-size: 17px;
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: -0.02em;
}

.sidebar__tag {
  font-size: 12px;
  color: rgba(148, 163, 184, 0.9);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar__menu {
  flex: 1;
  border-right: none !important;
  padding: 16px 8px;
}

.sidebar__menu.el-menu--collapse {
  padding-left: 4px;
  padding-right: 4px;
}

.sidebar__menu .el-menu-item {
  height: 48px;
  margin-bottom: 6px;
  border-radius: 10px;
  color: rgba(226, 232, 240, 0.75) !important;
}

.sidebar__menu .el-menu-item:hover {
  background: var(--pn-sidebar-hover) !important;
  color: #fff !important;
}

.sidebar__menu .el-menu-item.is-active {
  background: var(--pn-sidebar-active) !important;
  color: #fff !important;
  box-shadow: inset 0 0 0 1px rgba(99, 102, 241, 0.35);
}

.sidebar__menu .el-icon {
  font-size: 18px;
}

.sidebar__footer {
  padding: 16px 20px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

.sidebar__foot-note {
  margin: 0;
  font-size: 11px;
  line-height: 1.45;
  color: rgba(148, 163, 184, 0.55);
}

.main-column {
  flex-direction: column;
  min-width: 0;
}

.topbar {
  height: 64px !important;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px !important;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--pn-border);
  box-sizing: border-box;
}

.topbar__crumb {
  font-size: 15px;
  font-weight: 600;
  color: var(--pn-text);
  letter-spacing: -0.02em;
}

.topbar__right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.topbar__notify .el-badge__content.is-dot {
  top: 6px;
  right: 10px;
}

.topbar__notify-btn {
  border-color: var(--pn-border);
  color: var(--pn-text-muted);
}

.topbar__pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: #f1f5f9;
  border-radius: 999px;
  font-size: 13px;
  color: var(--pn-text-muted);
}

.topbar__pill-icon {
  color: var(--pn-success);
  font-size: 16px;
}

.topbar__name {
  font-weight: 600;
  color: var(--pn-text);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topbar__dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: var(--pn-border);
}

.topbar__role {
  font-weight: 600;
  color: var(--pn-text);
}

.topbar__logout {
  border-color: var(--pn-border);
  color: var(--pn-text-muted);
}

.topbar__logout:hover {
  color: var(--pn-primary);
  border-color: var(--pn-primary-light-5);
  background: var(--el-color-primary-light-9);
}

.main-area {
  padding: 28px !important;
  box-sizing: border-box;
  overflow: auto;
}

.main-area__inner {
  max-width: 1280px;
  margin: 0 auto;
  width: 100%;
}
</style>
