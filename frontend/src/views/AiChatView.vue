<template>
  <div class="page ai-layout">
    <PageHeader
      title="DeepSeek 健康咨询"
      subtitle="仅健康科普与用药指导，不替代面诊。"
    >
      <template #actions>
        <el-select
          v-if="auth.role === 'ADMIN' || auth.role === 'DOCTOR'"
          v-model="currentPatientId"
          filterable
          placeholder="选择病人档案"
          style="width: 220px"
          @change="loadHistory"
        >
          <el-option v-for="p in patientOptions" :key="p.id" :label="`${p.name} #${p.id}`" :value="p.id" />
        </el-select>
      </template>
    </PageHeader>

    <div class="ai-grid">
      <div class="pn-card history">
        <div class="pn-card__header">历史记录</div>
        <div class="pn-card__body history-list">
          <div
            v-for="h in history"
            :key="h.id"
            class="hist-item"
            :class="{ 'hist-item--active': selected?.id === h.id }"
            @click="selectHistory(h)"
          >
            <div class="hist-q">{{ h.question?.slice(0, 40) }}{{ h.question?.length > 40 ? '…' : '' }}</div>
            <div class="hist-t">{{ formatTime(h.chatTime) }}</div>
          </div>
          <el-empty v-if="!history.length" description="暂无记录" />
        </div>
      </div>
      <div class="pn-card chat">
        <div class="pn-card__header">
          <span>对话</span>
          <el-button text size="small" @click="startNewChat">新对话</el-button>
        </div>
        <div class="pn-card__body chat-body" ref="chatBodyRef">
          <div v-if="messages.length === 0 && !selected" class="chat-empty">
            <el-empty description="选择一个历史记录或开始新对话" />
          </div>
          <div v-for="(msg, idx) in messages" :key="idx" class="msg-row" :class="msg.role">
            <div class="msg-bubble">
              <div class="msg-text">{{ msg.content }}</div>
              <div class="msg-time">{{ msg.time }}</div>
            </div>
          </div>
          <div v-if="sending" class="msg-row ai">
            <div class="msg-bubble typing">
              <span class="dot-flash">AI 正在回复...</span>
            </div>
          </div>
        </div>
        <div class="chat-input-area">
          <div class="quick">
            <span class="quick-label">快捷提问：</span>
            <el-tag
              v-for="q in quicks"
              :key="q"
              class="qtag"
              @click="input = q"
            >{{ q }}</el-tag>
          </div>
          <div class="chat-send-row">
            <el-input
              v-model="input"
              type="textarea"
              :rows="2"
              placeholder="输入您的问题…"
              @keydown.enter.exact.prevent="send"
            />
            <el-button type="primary" class="send-btn" :loading="sending" :disabled="!currentPatientId || !input.trim()" @click="send">
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import PageHeader from "../components/PageHeader.vue";
import http from "../api/http";
import { useAuthStore } from "../stores/auth";

const auth = useAuthStore();
const route = useRoute();

const patientOptions = ref([]);
const currentPatientId = ref(null);
const history = ref([]);
const selected = ref(null);
const messages = ref([]);
const input = ref("");
const sending = ref(false);
const chatBodyRef = ref(null);

const quicks = [
  "感冒发烧在家如何处理？",
  "高血压患者饮食注意什么？",
  "糖尿病人运动建议？",
];

function formatTime(t) {
  if (!t) return "";
  return new Date(t).toLocaleString("zh-CN", {
    month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit",
  });
}

function scrollToBottom() {
  nextTick(() => {
    const el = chatBodyRef.value;
    if (el) el.scrollTop = el.scrollHeight;
  });
}

async function loadHistory() {
  if (!currentPatientId.value) return;
  const { data } = await http.get(`/ai/history/${currentPatientId.value}`);
  history.value = data;
}

function selectHistory(h) {
  selected.value = h;
  messages.value = [
    { role: "user", content: h.question, time: formatTime(h.chatTime) },
    { role: "ai", content: h.answer, time: formatTime(h.chatTime) },
  ];
  scrollToBottom();
}

function startNewChat() {
  selected.value = null;
  messages.value = [];
  input.value = "";
}

async function send() {
  if (!currentPatientId.value) {
    ElMessage.warning("请选择病人");
    return;
  }
  const q = input.value.trim();
  if (!q) return;
  const now = formatTime(new Date().toISOString());
  messages.value.push({ role: "user", content: q, time: now });
  input.value = "";
  scrollToBottom();
  sending.value = true;
  try {
    await http.post("/ai/ask", { patientId: currentPatientId.value, question: q });
    await loadHistory();
    const latest = history.value[0];
    if (latest) {
      messages.value.push({ role: "ai", content: latest.answer, time: formatTime(latest.chatTime) });
    }
    scrollToBottom();
  } catch {
    messages.value.push({ role: "ai", content: "AI 服务暂时不可用，请稍后再试。", time: now });
  } finally {
    sending.value = false;
  }
}

onMounted(async () => {
  const q = route.query.patientId;
  if (auth.role === "ADMIN" || auth.role === "DOCTOR") {
    const { data } = await http.get("/patients", { params: { includeArchived: true, size: 1000 } });
    patientOptions.value = data.content || data;
    currentPatientId.value = q ? Number(q) : (Array.isArray(data) ? data[0]?.id : data.content?.[0]?.id) ?? null;
  } else {
    currentPatientId.value = auth.patientId;
    if (!currentPatientId.value) {
      ElMessage.warning("当前账号未绑定病人档案");
    }
  }
  await loadHistory();
});
</script>

<style scoped>
.page { animation: fade-in 0.35s ease; }
.ai-grid {
  display: grid;
  grid-template-columns: minmax(200px, 280px) 1fr;
  gap: 20px;
}
@media (max-width: 900px) { .ai-grid { grid-template-columns: 1fr; } }
.history-list { max-height: 420px; overflow: auto; }
.hist-item {
  padding: 10px; border-radius: 8px; margin-bottom: 8px; cursor: pointer;
  border: 1px solid var(--pn-border);
}
.hist-item:hover { background: rgba(99,102,241,0.06); }
.hist-item--active { background: rgba(99,102,241,0.1); border-color: var(--pn-primary); }
.hist-q { font-size: 13px; }
.hist-t { font-size: 12px; color: var(--pn-text-muted); margin-top: 4px; }
.chat-body {
  flex: 1; overflow-y: auto; max-height: 420px;
  display: flex; flex-direction: column; gap: 12px; padding: 8px 0;
}
.chat-empty { flex: 1; display: flex; align-items: center; justify-content: center; }
.msg-row { display: flex; }
.msg-row.user { justify-content: flex-end; }
.msg-bubble {
  max-width: 80%; padding: 10px 14px; border-radius: 14px; font-size: 14px; line-height: 1.55;
}
.msg-row.user .msg-bubble { background: var(--pn-primary); color: #fff; border-bottom-right-radius: 4px; }
.msg-row.ai .msg-bubble { background: #f1f5f9; color: var(--pn-text); border-bottom-left-radius: 4px; }
.msg-time { font-size: 11px; margin-top: 4px; opacity: 0.65; }
.typing .dot-flash { color: var(--pn-text-muted); font-style: italic; }
.chat-input-area { padding-top: 12px; border-top: 1px solid var(--pn-border); }
.quick-label { font-size: 13px; color: var(--pn-text-muted); }
.qtag { margin: 4px 6px 0 0; cursor: pointer; }
.chat-send-row { display: flex; gap: 8px; margin-top: 10px; align-items: flex-end; }
.send-btn { flex-shrink: 0; }
@keyframes fade-in { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: none; } }
</style>
