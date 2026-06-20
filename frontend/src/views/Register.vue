<template>
  <div class="reg-page">
    <div class="reg-card">
      <h1>病人账号注册（PCMS）</h1>
      <p class="sub">设计方案：手机号/用户名建档，绑定病人角色</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="登录用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" clearable placeholder="可选" style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="w100" :loading="loading" @click="submit">注册并登录</el-button>
        </el-form-item>
        <el-link type="primary" @click="$router.push('/login')">返回登录</el-link>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const auth = useAuthStore();
const formRef = ref();
const loading = ref(false);

const form = reactive({
  username: "",
  password: "",
  name: "",
  phone: "",
  idCard: "",
  gender: "",
});

const rules = {
  username: [{ required: true, message: "必填", trigger: "blur" }],
  password: [{ required: true, message: "必填", trigger: "blur" }],
  name: [{ required: true, message: "必填", trigger: "blur" }],
  phone: [{ required: true, message: "必填", trigger: "blur" }],
  idCard: [{ required: true, message: "必填", trigger: "blur" }],
};

async function submit() {
  await formRef.value.validate();
  loading.value = true;
  try {
    await auth.register(form);
    ElMessage.success("注册成功");
    router.replace("/dashboard");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.reg-page {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #0f172a, #312e81);
  padding: 24px;
}
.reg-card {
  width: 100%;
  max-width: 440px;
  background: #fff;
  padding: 32px;
  border-radius: 16px;
}
h1 {
  margin: 0 0 8px;
}
.sub {
  margin: 0 0 20px;
  color: var(--pn-text-muted);
  font-size: 14px;
}
.w100 {
  width: 100%;
}
</style>
