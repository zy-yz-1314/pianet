<template>
  <div class="page-header">
    <div class="page-header__text">
      <h1 class="page-header__title">{{ title }}</h1>
      <p v-if="subtitle" class="page-header__subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.actions" class="page-header__actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: "" },
});
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 24px;
}
.page-header__title {
  margin: 0;
  font-size: 1.65rem;
  font-weight: 700;
  letter-spacing: -0.03em;
  color: var(--pn-text);
  line-height: 1.2;
}
.page-header__subtitle {
  margin: 8px 0 0;
  font-size: 14px;
  color: var(--pn-text-muted);
  line-height: 1.5;
  max-width: 520px;
}
.page-header__actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
</style>
