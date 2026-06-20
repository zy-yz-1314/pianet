<template>
  <div class="login-page">
    <div class="login-page__bg" aria-hidden="true">
      <div class="blob blob--1" />
      <div class="blob blob--2" />
      <div class="blob blob--3" />
      <div class="grid" />
    </div>

    <div class="login-page__content">
      <div class="login-hero">
        <div class="login-hero__badge">
          <el-icon><TrendCharts /></el-icon>
          数字化看诊工作台
        </div>
        <h2 class="login-hero__title">更清晰的患者档案<br />更高效的随访记录</h2>
        <p class="login-hero__desc">
          统一管理与检索病人基础信息与历次看诊，界面为演示环境，登录后数据与本地后端同步。
        </p>
        <ul class="login-hero__list">
          <li><el-icon><CircleCheck /></el-icon> 档案与看诊一站式维护</li>
          <li><el-icon><CircleCheck /></el-icon> 图形验证码降低机器尝试</li>
          <li><el-icon><CircleCheck /></el-icon> 适配桌面端宽屏布局</li>
        </ul>
      </div>

      <div class="login-card">
        <div class="login-card__head">
          <h1 class="login-card__title">欢迎回来</h1>
          <p class="login-card__subtitle">医生 / 管理员 / 病人账号登录（PCMS）</p>
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form" @submit.prevent>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" clearable autocomplete="username" size="large">
              <template #prefix>
                <el-icon class="input-icon"><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              show-password
              clearable
              autocomplete="current-password"
              size="large"
            >
              <template #prefix>
                <el-icon class="input-icon"><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="验证码" prop="captchaInput">
            <div class="captcha-row">
              <el-input
                v-model="form.captchaInput"
                placeholder="输入右侧字符"
                clearable
                maxlength="6"
                class="captcha-input"
                size="large"
              />
              <div class="captcha-box" title="点击刷新">
                <canvas ref="canvasRef" width="128" height="44" class="captcha-canvas" @click="refreshCaptcha" />
              </div>
              <el-button type="primary" link @click="refreshCaptcha">
                <el-icon><Refresh /></el-icon>
                换一张
              </el-button>
            </div>
          </el-form-item>
          <el-form-item class="login-form__submit-wrap">
            <el-button type="primary" class="submit-btn" size="large" :loading="loading" @click="onSubmit">
              登录系统
            </el-button>
          </el-form-item>
        </el-form>
        <p class="login-extra">
          <router-link class="link-light" to="/register">病人注册</router-link>
          <span class="sep"> · </span>
          <el-button link type="primary" class="forgot-btn" @click="forgotTip">忘记密码</el-button>
        </p>
        <p class="copyright">© 病人看诊管理系统（PCMS）· 纯文本设计方案对照实现</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { User, Lock, Refresh, TrendCharts, CircleCheck } from "@element-plus/icons-vue";
import { useAuthStore } from "../stores/auth";
import { generateCaptchaCode, drawCaptchaCanvas } from "../utils/captcha";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const formRef = ref();
const canvasRef = ref();
const loading = ref(false);
const captchaCode = ref("");

const form = reactive({
  username: "",
  password: "",
  captchaInput: "",
});

const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  captchaInput: [{ required: true, message: "请输入验证码", trigger: "blur" }],
};

function refreshCaptcha() {
  captchaCode.value = generateCaptchaCode(4);
  form.captchaInput = "";
  nextTick(() => drawCaptchaCanvas(canvasRef.value, captchaCode.value));
}

onMounted(() => {
  refreshCaptcha();
});

async function onSubmit() {
  await formRef.value.validate();
  const input = form.captchaInput.trim().toUpperCase();
  const expected = captchaCode.value.toUpperCase();
  if (input !== expected) {
    ElMessage.error("验证码错误，请重试");
    refreshCaptcha();
    return;
  }
  loading.value = true;
  try {
    await auth.login(form.username, form.password);
    ElMessage.success("登录成功");
    const redirect = route.query.redirect;
    router.replace(typeof redirect === "string" && redirect.startsWith("/") ? redirect : "/dashboard");
  } catch {
    refreshCaptcha();
  } finally {
    loading.value = false;
  }
}

function forgotTip() {
  ElMessage.info("演示环境请联系管理员重置密码；默认 admin / 123456，doctor / 123456");
}
</script>

<style scoped>
.login-page {
  min-height: 100%;
  position: relative;
  overflow-x: hidden;
  background: #070b14;
}

.login-page__bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.55;
}
.blob--1 {
  width: 420px;
  height: 420px;
  background: #6366f1;
  top: -120px;
  right: 10%;
}
.blob--2 {
  width: 360px;
  height: 360px;
  background: #22d3ee;
  bottom: -80px;
  left: -80px;
}
.blob--3 {
  width: 280px;
  height: 280px;
  background: #a78bfa;
  top: 40%;
  left: 35%;
  opacity: 0.35;
}

.grid {
  position: absolute;
  inset: 0;
  background-image: linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(ellipse 70% 60% at 50% 40%, black 20%, transparent 70%);
}

.login-page__content {
  position: relative;
  z-index: 1;
  min-height: 100%;
  display: flex;
  align-items: stretch;
  justify-content: center;
  gap: clamp(24px, 5vw, 72px);
  padding: clamp(24px, 4vw, 48px);
  box-sizing: border-box;
  flex-wrap: wrap;
}

.login-hero {
  flex: 1 1 320px;
  max-width: 440px;
  padding: 32px 8px 32px 16px;
  color: #e2e8f0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-hero__badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  width: fit-content;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  font-size: 13px;
  color: #cbd5e1;
  margin-bottom: 24px;
}

.login-hero__title {
  margin: 0 0 16px;
  font-size: clamp(1.75rem, 3vw, 2.25rem);
  font-weight: 700;
  line-height: 1.25;
  letter-spacing: -0.03em;
}

.login-hero__desc {
  margin: 0 0 28px;
  font-size: 15px;
  line-height: 1.65;
  color: rgba(148, 163, 184, 0.95);
}

.login-hero__list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 14px;
  color: rgba(226, 232, 240, 0.88);
}

.login-hero__list li {
  display: flex;
  align-items: center;
  gap: 10px;
}

.login-hero__list .el-icon {
  color: #22d3ee;
  font-size: 18px;
}

.login-card {
  flex: 0 1 420px;
  width: 100%;
  max-width: 420px;
  padding: 40px 36px 36px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.06);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.45);
  align-self: center;
}

.login-card__head {
  margin-bottom: 8px;
}

.login-card__title {
  margin: 0 0 8px;
  font-size: 1.6rem;
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: -0.02em;
}

.login-card__subtitle {
  margin: 0 0 28px;
  font-size: 14px;
  color: rgba(148, 163, 184, 0.95);
}

.login-form :deep(.el-form-item__label) {
  color: rgba(226, 232, 240, 0.85);
  font-weight: 500;
}

.login-form :deep(.el-input__wrapper) {
  background: rgba(15, 23, 42, 0.5);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
}

.login-form :deep(.el-input__inner) {
  color: #f1f5f9;
}

.input-icon {
  color: rgba(148, 163, 184, 0.9);
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
}

.captcha-input {
  flex: 1;
  min-width: 140px;
}

.captcha-box {
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

.captcha-canvas {
  display: block;
  vertical-align: middle;
}

.login-form__submit-wrap {
  margin-bottom: 0;
  margin-top: 8px;
}

.submit-btn {
  width: 100%;
  font-weight: 600;
  height: 46px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 55%, #22d3ee 140%) !important;
  border: none !important;
  box-shadow: 0 12px 32px rgba(99, 102, 241, 0.35);
}

.submit-btn:hover {
  filter: brightness(1.06);
}

.login-extra {
  margin-top: 12px;
  text-align: center;
  font-size: 13px;
  color: rgba(226, 232, 240, 0.75);
}

.link-light {
  color: #a5b4fc;
  text-decoration: none;
}

.link-light:hover {
  text-decoration: underline;
}

.forgot-btn {
  padding: 0;
  vertical-align: baseline;
}

.copyright {
  margin: 14px 0 0;
  text-align: center;
  font-size: 11px;
  color: rgba(148, 163, 184, 0.55);
}

@media (max-width: 900px) {
  .login-hero {
    max-width: 100%;
    padding-bottom: 0;
  }
  .login-page__content {
    flex-direction: column;
  }
}
</style>
