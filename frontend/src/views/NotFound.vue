<template>
  <div class="not-found">
    <el-result icon="warning" title="404" sub-title="页面未找到">
      <template #extra>
        <el-button type="primary" @click="$router.push('/dashboard')">返回工作台</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
// 页面未找到 - 404
</script>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}
</style>
