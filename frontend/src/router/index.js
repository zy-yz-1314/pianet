import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";
import Login from "../views/Login.vue";
import Register from "../views/Register.vue";
import Dashboard from "../views/Dashboard.vue";
import PatientList from "../views/PatientList.vue";
import PatientDetail from "../views/PatientDetail.vue";
import PatientForm from "../views/PatientForm.vue";
import VisitList from "../views/VisitList.vue";
import VisitForm from "../views/VisitForm.vue";
import RegistrationView from "../views/RegistrationView.vue";
import AiChatView from "../views/AiChatView.vue";
import DepartmentsView from "../views/DepartmentsView.vue";
import MedicinesView from "../views/MedicinesView.vue";
import NotFound from "../views/NotFound.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/login", name: "login", component: Login, meta: { public: true } },
    { path: "/register", name: "register", component: Register, meta: { public: true } },
    { path: "/", redirect: "/dashboard" },
    {
      path: "/dashboard",
      name: "dashboard",
      component: Dashboard,
      meta: { requiresAuth: true },
    },
    {
      path: "/patients",
      name: "patients",
      component: PatientList,
      meta: { requiresAuth: true },
    },
    {
      path: "/patients/new",
      name: "patient-new",
      component: PatientForm,
      meta: { requiresAuth: true, roles: ["ADMIN", "DOCTOR"] },
    },
    {
      path: "/patients/:id",
      name: "patient-detail",
      component: PatientDetail,
      props: true,
      meta: { requiresAuth: true },
    },
    {
      path: "/patients/:id/edit",
      name: "patient-edit",
      component: PatientForm,
      props: true,
      meta: { requiresAuth: true, roles: ["ADMIN", "DOCTOR"] },
    },
    {
      path: "/visits",
      name: "visits",
      component: VisitList,
      meta: { requiresAuth: true },
    },
    {
      path: "/visits/new",
      name: "visit-new",
      component: VisitForm,
      meta: { requiresAuth: true, roles: ["ADMIN", "DOCTOR"] },
    },
    {
      path: "/visits/:id/edit",
      name: "visit-edit",
      component: VisitForm,
      props: true,
      meta: { requiresAuth: true, roles: ["ADMIN", "DOCTOR"] },
    },
    {
      path: "/registrations",
      name: "registrations",
      component: RegistrationView,
      meta: { requiresAuth: true },
    },
    {
      path: "/ai-chat",
      name: "ai-chat",
      component: AiChatView,
      meta: { requiresAuth: true },
    },
    {
      path: "/admin/departments",
      name: "admin-departments",
      component: DepartmentsView,
      meta: { requiresAuth: true, roles: ["ADMIN"] },
    },
    {
      path: "/admin/medicines",
      name: "admin-medicines",
      component: MedicinesView,
      meta: { requiresAuth: true, roles: ["ADMIN"] },
    },
    { path: "/:pathMatch(.*)*", name: "not-found", component: NotFound },
  ],
});

router.beforeEach((to) => {
  const auth = useAuthStore();
  if (to.meta.public) {
    if (auth.isLoggedIn && (to.name === "login" || to.name === "register")) {
      return { path: "/dashboard" };
    }
    return true;
  }
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { path: "/login", query: { redirect: to.fullPath } };
  }
  if (to.meta.roles && auth.role && !to.meta.roles.includes(auth.role)) {
    return { path: "/dashboard" };
  }
  return true;
});

export default router;
