<template>
  <div class="page">
    <PageHeader
      title="控制台"
      subtitle="病人看诊管理系统（PCMS）— 关键指标与数据可视化"
    />
    <el-row v-if="stats" :gutter="16" class="tiles">
      <el-col v-for="item in tileList" :key="item.k" :xs="24" :sm="12" :md="8">
        <div class="tile">
          <p class="tile__label">{{ item.label }}</p>
          <p class="tile__value">{{ item.value }}</p>
          <p v-if="item.hint" class="tile__hint">{{ item.hint }}</p>
        </div>
      </el-col>
    </el-row>
    <el-skeleton v-else :rows="4" animated />

    <el-row v-if="chartData" :gutter="16" class="charts">
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <h3 class="chart-card__title">近 7 天就诊趋势</h3>
          <div class="mini-chart">
            <div v-for="t in chartData.visitTrend" :key="t.date" class="mini-bar-wrap">
              <span class="mini-label">{{ t.date.slice(5) }}</span>
              <div class="mini-bar-track">
                <div class="mini-bar" :style="{ width: barWidth(t.count) }">{{ t.count }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div class="chart-card">
          <h3 class="chart-card__title">挂号状态分布</h3>
          <div class="status-grid">
            <div v-for="s in chartData.registrationStatus" :key="s.name" class="status-item">
              <span class="status-dot" :style="{ background: statusColor(s.name) }"></span>
              <span class="status-name">{{ s.name }}</span>
              <span class="status-val">{{ s.value }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import PageHeader from "../components/PageHeader.vue";
import http from "../api/http";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const stats = ref(null);
const chartData = ref(null);

const tileList = computed(() => {
  const s = stats.value;
  if (!s) return [];
  const r = s.role;
  const out = [];
  if (r === "ADMIN") {
    out.push({ k: "p", label: "病人档案数", value: s.patients });
    out.push({ k: "v", label: "看诊记录数", value: s.visitRecords });
    out.push({ k: "g", label: "挂号总数", value: s.registrations });
    out.push({ k: "d", label: "科室数", value: s.departments });
    out.push({ k: "m", label: "药品条目", value: s.medicines });
    out.push({ k: "t", label: "今日看诊", value: s.todayVisits });
    out.push({ k: "q", label: "待接诊挂号", value: s.pendingRegistrationsTotal, hint: "全院待就诊状态" });
  } else if (r === "DOCTOR") {
    out.push({ k: "dp", label: "职称", value: s.doctorTitle || "—" });
    out.push({ k: "dept", label: "科室", value: s.department || "—" });
    out.push({ k: "pen", label: "待接诊", value: s.pendingRegistrations });
    out.push({ k: "tv", label: "今日全院看诊(参考)", value: s.todayVisits });
    out.push({ k: "mv", label: "本人看诊记录数", value: s.myVisitRecordsTotal, hint: "historical linkage" });
  } else {
    out.push({ k: "pid", label: "档案编号", value: s.patientId || "暂无绑定" });
    out.push({ k: "pr", label: "我的待就诊挂号", value: s.myPendingRegistrations ?? 0 });
    out.push({ k: "ai", label: "AI 咨询条目", value: s.myAiConsults ?? 0, hint: "含演示模式占位回复" });
  }
  return out;
});

const maxTrend = computed(() => {
  if (!chartData.value) return 1;
  return Math.max(1, ...chartData.value.visitTrend.map(t => t.count));
});

function barWidth(count) {
  return Math.max(2, (count / maxTrend.value) * 100) + "%";
}

function statusColor(name) {
  const map = { "待就诊": "#f59e0b", "就诊中": "#3b82f6", "已完成": "#10b981", "已取消": "#ef4444" };
  return map[name] || "#64748b";
}

onMounted(async () => {
  await auth.restoreFromServer();
  const [statsRes, chartsRes] = await Promise.all([
    http.get("/dashboard"),
    http.get("/dashboard/charts"),
  ]);
  stats.value = statsRes.data;
  chartData.value = chartsRes.data;
});
</script>

<style scoped>
.page { animation: fade-in 0.35s ease; }
@keyframes fade-in { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: none; } }
.tiles { margin-top: 8px; }
.tile {
  background: var(--pn-bg-elevated);
  border: 1px solid var(--pn-border);
  border-radius: var(--pn-radius);
  padding: 20px; margin-bottom: 16px;
  box-shadow: var(--pn-shadow);
}
.tile__label { margin: 0; font-size: 13px; color: var(--pn-text-muted); }
.tile__value { margin: 8px 0 0; font-size: 28px; font-weight: 700; letter-spacing: -0.02em; color: var(--pn-text); }
.tile__hint { margin: 8px 0 0; font-size: 12px; color: var(--pn-text-muted); }
.charts { margin-top: 16px; }
.chart-card {
  background: var(--pn-bg-elevated);
  border: 1px solid var(--pn-border);
  border-radius: var(--pn-radius);
  padding: 20px; margin-bottom: 16px;
  box-shadow: var(--pn-shadow);
}
.chart-card__title { margin: 0 0 16px; font-size: 15px; font-weight: 600; color: var(--pn-text); }
.mini-chart { display: flex; flex-direction: column; gap: 8px; }
.mini-bar-wrap { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.mini-label { width: 48px; color: var(--pn-text-muted); flex-shrink: 0; }
.mini-bar-track { flex: 1; background: #f1f5f9; border-radius: 6px; overflow: hidden; height: 24px; }
.mini-bar {
  height: 100%; background: var(--pn-primary); border-radius: 6px;
  color: #fff; font-size: 12px; font-weight: 600;
  display: flex; align-items: center; padding-left: 8px;
  min-width: 24px; transition: width 0.5s ease;
}
.status-grid { display: flex; flex-direction: column; gap: 12px; }
.status-item { display: flex; align-items: center; gap: 10px; font-size: 14px; }
.status-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
.status-name { flex: 1; color: var(--pn-text-muted); }
.status-val { font-weight: 700; font-size: 18px; color: var(--pn-text); }
</style>
