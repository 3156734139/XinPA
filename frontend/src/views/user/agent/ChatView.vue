<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- 头部 -->
      <div class="chat-header">
        <div class="header-info">
          <el-icon :size="22" color="#E8789A"><MagicStick /></el-icon>
          <span class="header-title">AI 助理</span>
          <el-tag size="small" type="danger" effect="plain" class="beta-tag">Beta</el-tag>
        </div>
        <el-button text size="small" @click="handleClear">
          <el-icon><Delete /></el-icon>
          清空对话
        </el-button>
      </div>

      <!-- 消息列表 -->
      <div class="message-list" ref="messageListRef">
        <div v-if="messages.length === 0" class="welcome">
          <div class="welcome-icon">
            <el-icon :size="48" color="#E8789A"><MagicStick /></el-icon>
          </div>
          <h3 class="welcome-title">你好，我是你的 AI 助理</h3>
          <p class="welcome-desc">我可以帮你查询和管理业务数据，也可以帮你创建订单</p>
          <div class="suggestions">
            <el-tag
              v-for="q in suggestions"
              :key="q"
              class="suggestion-tag"
              effect="plain"
              @click="sendMessage(q)"
            >
              {{ q }}
            </el-tag>
          </div>
        </div>

        <template v-for="(msg, idx) in messages" :key="idx">
          <!-- 订单预览卡片 -->
          <div v-if="msg.type === 'order_preview'" class="message-item assistant-msg card-msg">
            <div class="message-avatar">
              <el-avatar :size="36" class="assistant-avatar">
                <el-icon :size="20"><MagicStick /></el-icon>
              </el-avatar>
            </div>
            <div class="order-preview-card">
              <div class="card-header">
                <span v-if="!msg.cardState || msg.cardState === 'preview'">📋 订单预览 — 请确认信息</span>
                <span v-else-if="msg.cardState === 'loading'">⏳ 正在创建订单...</span>
                <span v-else-if="msg.cardState === 'success'">✅ 订单已创建成功</span>
                <span v-else-if="msg.cardState === 'error'">❌ 创建失败</span>
              </div>

              <!-- 预览/编辑模式 -->
              <template v-if="!msg.cardState || msg.cardState === 'preview'">
                <div v-if="msg.preview" class="card-body">
                  <el-form label-width="90px" size="small">
                    <el-form-item label="客户">
                      <span class="readonly-value">{{ msg.preview.customerName }}</span>
                    </el-form-item>
                    <el-form-item label="开始时间">
                      <el-date-picker
                        v-model="orderForm.startTime"
                        type="datetime"
                        format="YYYY-MM-DD HH:mm"
                        value-format="YYYY-MM-DD HH:mm"
                        placeholder="开始时间"
                        style="width: 100%"
                      />
                    </el-form-item>
                    <el-form-item label="结束时间">
                      <el-date-picker
                        v-model="orderForm.endTime"
                        type="datetime"
                        format="YYYY-MM-DD HH:mm"
                        value-format="YYYY-MM-DD HH:mm"
                        placeholder="结束时间"
                        style="width: 100%"
                      />
                    </el-form-item>
                    <el-form-item label="单价">
                      <el-input-number v-model="orderForm.unitPrice" :min="0" :precision="2" style="width: 100%" />
                    </el-form-item>
                    <el-form-item label="支付方式">
                      <el-select v-model="orderForm.paymentMethod" placeholder="选择支付方式" style="width: 100%" clearable>
                        <el-option label="微信" value="微信" />
                        <el-option label="支付宝" value="支付宝" />
                        <el-option label="现金" value="现金" />
                        <el-option label="银行转账" value="银行转账" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="到手比例">
                      <el-input-number v-model="orderForm.settleRatio" :min="0" :max="100" :precision="0" style="width: 100%" />
                      <span class="ratio-suffix">%</span>
                    </el-form-item>
                    <el-form-item label="VIP优惠">
                      <el-switch v-model="orderForm.applyVipDiscount" />
                      <span class="vip-hint">开启后自动根据客户等级计算折扣</span>
                    </el-form-item>
                    <el-divider />
                    <el-form-item label="时长">
                      <span class="readonly-value">{{ msg.preview.actualMinutes }} 分钟</span>
                    </el-form-item>
                    <el-form-item label="总金额">
                      <span class="readonly-value">¥{{ formatMoney(msg.preview.totalAmount) }}</span>
                    </el-form-item>
                    <el-form-item label="优惠金额">
                      <span class="readonly-value">-¥{{ formatMoney(msg.preview.discountAmount) }}</span>
                    </el-form-item>
                    <el-form-item label="最终金额">
                      <span class="final-amount">¥{{ formatMoney(msg.preview.finalAmount) }}</span>
                    </el-form-item>
                  </el-form>
                  <div class="card-note">确认后系统将重新核算，金额以实际计算结果为准</div>
                </div>
                <div class="card-actions">
                  <el-button size="small" @click="cancelOrder(idx)">取消</el-button>
                  <el-button size="small" type="primary" @click="confirmOrder(idx)" :loading="confirmLoading">
                    确认创建
                  </el-button>
                </div>
              </template>

              <!-- 成功状态 -->
              <template v-else-if="msg.cardState === 'success'">
                <div class="card-body success-body">
                  <div class="success-icon">
                    <el-icon :size="40" color="#67C23A"><CircleCheck /></el-icon>
                  </div>
                  <div class="success-details">
                    <div class="success-row">
                      <span class="success-label">订单号</span>
                      <span class="success-value">{{ msg.orderResult?.orderNo }}</span>
                    </div>
                    <div class="success-row">
                      <span class="success-label">客户</span>
                      <span class="success-value">{{ msg.orderResult?.customerName }}</span>
                    </div>
                    <div class="success-row">
                      <span class="success-label">最终金额</span>
                      <span class="success-value price">¥{{ formatMoney(msg.orderResult?.finalAmount) }}</span>
                    </div>
                  </div>
                </div>
              </template>

              <!-- 错误状态 -->
              <template v-else-if="msg.cardState === 'error'">
                <div class="card-body error-body">
                  <p>{{ msg.errorMsg || '创建订单时发生错误，请重试' }}</p>
                </div>
                <div class="card-actions">
                  <el-button size="small" @click="cancelOrder(idx)">关闭</el-button>
                  <el-button size="small" type="primary" @click="retryConfirm(idx)">重试</el-button>
                </div>
              </template>
            </div>
          </div>

          <!-- 普通文字消息 -->
          <div v-else class="message-item" :class="msg.role === 'user' ? 'user-msg' : 'assistant-msg'">
            <div class="message-avatar">
              <el-avatar v-if="msg.role === 'user'" :size="36" class="user-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <el-avatar v-else :size="36" class="assistant-avatar">
                <el-icon :size="20"><MagicStick /></el-icon>
              </el-avatar>
            </div>
            <div class="message-bubble">
              <div class="message-text">{{ msg.content }}</div>
            </div>
          </div>
        </template>

        <!-- 加载中 -->
        <div v-if="loading" class="message-item assistant-msg">
          <div class="message-avatar">
            <el-avatar :size="36" class="assistant-avatar">
              <el-icon :size="20"><MagicStick /></el-icon>
            </el-avatar>
          </div>
          <div class="message-bubble">
            <div class="typing-indicator">
              <span class="typing-dot"></span>
              <span class="typing-dot"></span>
              <span class="typing-dot"></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="chat-input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          placeholder="输入你的问题..."
          :disabled="loading"
          @keydown.enter.exact.prevent="handleSend"
          resize="none"
          class="chat-input"
        />
        <el-button
          type="primary"
          :loading="loading"
          :disabled="!inputMessage.trim()"
          @click="handleSend"
          class="send-btn"
        >
          <el-icon><Promotion /></el-icon>
          <span>发送</span>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, nextTick, onMounted } from 'vue';
import { sendChatMessage, getChatHistory, clearChatHistory, confirmOrder } from '@/api/agent';

/** 后端返回的预览数据 */
interface PreviewData {
  confirmToken: string;
  customerName: string;
  customerId: number;
  packageName: string | null;
  startTime: string;
  endTime: string;
  unitPrice: number;
  actualMinutes: number;
  extraMinutes: number;
  totalAmount: number;
  discountAmount: number;
  finalAmount: number;
  paymentMethod: string | null;
  settleRatio: number;
  applyVipDiscount: boolean;
  title: string | null;
}

/** 订单创建结果 */
interface OrderResult {
  success: boolean;
  orderNo: string;
  finalAmount: number;
  customerName: string;
}

/** 消息扩展 */
interface ChatMsg {
  role: string;
  content: string;
  type?: string;          // "order_preview"
  preview?: PreviewData;   // 预览数据
  cardState?: 'preview' | 'loading' | 'success' | 'error';
  orderResult?: OrderResult;
  errorMsg?: string;
}

/** 后端 AgentResponse */
interface AgentResponse {
  type: string;
  role: string;
  content: string;
  preview: PreviewData | null;
  confirmToken: string | null;
}

const messages = ref<ChatMsg[]>([]);
const inputMessage = ref('');
const loading = ref(false);
const confirmLoading = ref(false);
const messageListRef = ref<HTMLElement | null>(null);

/** 当前编辑的表单数据（由预览卡片填充） */
const orderForm = reactive({
  startTime: '',
  endTime: '',
  unitPrice: 0,
  paymentMethod: '',
  settleRatio: 100,
  applyVipDiscount: true,
});

const suggestions = [
  '我今天接了哪些单子',
  '本月收入有多少？',
  '帮我查一下客户信息',
  '帮我开个单，今天下午2点到4点，单价150，微信支付',
];

async function loadHistory() {
  try {
    const res: any = await getChatHistory();
    messages.value = res.data || [];
  } catch {
    // ignore
  }
}

async function handleSend() {
  const text = inputMessage.value.trim();
  if (!text || loading.value) return;
  sendMessage(text);
}

async function sendMessage(text: string) {
  inputMessage.value = '';
  messages.value.push({ role: 'user', content: text });
  loading.value = true;
  scrollToBottom();

  try {
    const res: any = await sendChatMessage(text);
    const reply: AgentResponse | undefined = res.data;
    if (reply) {
      if (reply.type === 'order_preview' && reply.preview) {
        // 填充表单初始值
        orderForm.startTime = reply.preview.startTime || '';
        orderForm.endTime = reply.preview.endTime || '';
        orderForm.unitPrice = reply.preview.unitPrice || 0;
        orderForm.paymentMethod = reply.preview.paymentMethod || '';
        orderForm.settleRatio = reply.preview.settleRatio ?? 100;
        orderForm.applyVipDiscount = reply.preview.applyVipDiscount ?? true;

        messages.value.push({
          role: 'assistant',
          content: reply.content || '',
          type: 'order_preview',
          preview: reply.preview,
          cardState: 'preview',
        });
      } else {
        messages.value.push({ role: 'assistant', content: reply.content || '...' });
      }
    }
  } catch {
    messages.value.push({ role: 'assistant', content: '抱歉，请求失败，请检查网络或 LLM 配置。' });
  } finally {
    loading.value = false;
    scrollToBottom();
  }
}

async function confirmOrder(msgIdx: number) {
  const msg = messages.value[msgIdx];
  if (!msg?.preview) return;

  confirmLoading.value = true;
  msg.cardState = 'loading';
  scrollToBottom();

  try {
    const edits = {
      startTime: orderForm.startTime,
      endTime: orderForm.endTime,
      unitPrice: orderForm.unitPrice,
      paymentMethod: orderForm.paymentMethod,
      settleRatio: orderForm.settleRatio,
      applyVipDiscount: orderForm.applyVipDiscount,
    };

    const res: any = await confirmOrder(msg.preview.confirmToken, edits);
    const result = res.data;
    if (result && result.success) {
      msg.cardState = 'success';
      msg.orderResult = result;
    } else {
      msg.cardState = 'error';
      msg.errorMsg = result?.error || '未知错误';
    }
  } catch (e: any) {
    msg.cardState = 'error';
    msg.errorMsg = e?.response?.data?.message || e?.message || '网络错误';
  } finally {
    confirmLoading.value = false;
    scrollToBottom();
    // 回复一个提示消息
    setTimeout(() => {
      messages.value.push({
        role: 'assistant',
        content: msg.cardState === 'success'
          ? `订单已创建成功，订单号：${msg.orderResult?.orderNo}，最终金额：¥${formatMoney(msg.orderResult?.finalAmount)}`
          : '订单创建失败，请重试或手动创建。',
      });
      scrollToBottom();
    }, 500);
  }
}

function cancelOrder(msgIdx: number) {
  const msg = messages.value[msgIdx];
  if (msg) {
    msg.cardState = 'preview';
  }
}

function retryConfirm(msgIdx: number) {
  confirmOrder(msgIdx);
}

async function handleClear() {
  try {
    await clearChatHistory();
  } catch {
    // ignore
  }
  messages.value = [];
}

function formatMoney(val: number | string | undefined | null): string {
  if (val == null) return '0.00';
  return Number(val).toFixed(2);
}

function scrollToBottom() {
  nextTick(() => {
    const el = messageListRef.value;
    if (el) el.scrollTop = el.scrollHeight;
  });
}

onMounted(() => {
  loadHistory();
});
</script>

<style scoped>
.chat-page {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.chat-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(232, 130, 154, 0.06);
  overflow: hidden;
}

/* 头部 */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(232, 130, 154, 0.08);
  flex-shrink: 0;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-title {
  font-size: 16px;
  font-weight: 600;
  color: #5D4E6D;
}

.beta-tag {
  border-radius: 8px !important;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 20px;
  text-align: center;
}

.welcome-icon {
  margin-bottom: 8px;
}

.welcome-title {
  font-size: 18px;
  font-weight: 600;
  color: #5D4E6D;
  margin: 0;
}

.welcome-desc {
  font-size: 14px;
  color: #A890B0;
  margin: 0 0 16px;
}

.suggestions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  max-width: 420px;
}

.suggestion-tag {
  cursor: pointer;
  border-radius: 16px !important;
  padding: 4px 12px;
  border-color: rgba(232, 130, 154, 0.2) !important;
  color: #A890B0 !important;
  transition: all 0.2s;
}

.suggestion-tag:hover {
  background: rgba(232, 130, 154, 0.08) !important;
  color: #E8789A !important;
  border-color: rgba(232, 130, 154, 0.3) !important;
}

/* 消息气泡 */
.message-item {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.card-msg {
  max-width: 90%;
}

.user-msg {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.assistant-msg {
  align-self: flex-start;
}

.message-avatar {
  flex-shrink: 0;
}

.user-avatar {
  background: #E8789A !important;
}

.assistant-avatar {
  background: #FFF5F7 !important;
  color: #E8789A !important;
  border: 1px solid rgba(232, 130, 154, 0.15);
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.user-msg .message-bubble {
  background: #E8789A;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.assistant-msg .message-bubble {
  background: #FFF5F7;
  color: #5D4E6D;
  border-bottom-left-radius: 4px;
}

.message-text {
  white-space: pre-wrap;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 0;
}

.typing-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #E8789A;
  animation: typing-bounce 1.4s ease-in-out infinite;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing-bounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-6px);
    opacity: 1;
  }
}

/* ===== 订单预览卡片 ===== */
.order-preview-card {
  background: #fff;
  border: 1px solid rgba(232, 130, 154, 0.15);
  border-radius: 16px;
  overflow: hidden;
  min-width: 380px;
  box-shadow: 0 2px 8px rgba(232, 130, 154, 0.06);
}

.card-header {
  padding: 12px 16px;
  background: linear-gradient(135deg, #FFF5F7, #FFF9FB);
  font-size: 14px;
  font-weight: 600;
  color: #5D4E6D;
  border-bottom: 1px solid rgba(232, 130, 154, 0.08);
}

.card-body {
  padding: 16px;
}

.card-body :deep(.el-form-item) {
  margin-bottom: 12px;
}

.card-body :deep(.el-form-item__label) {
  color: #A890B0;
  font-size: 13px;
}

.readonly-value {
  color: #5D4E6D;
  font-size: 14px;
  line-height: 32px;
}

.final-amount {
  color: #E8789A;
  font-size: 18px;
  font-weight: 700;
  line-height: 32px;
}

.ratio-suffix {
  margin-left: 4px;
  color: #A890B0;
  font-size: 13px;
}

.vip-hint {
  margin-left: 8px;
  color: #A890B0;
  font-size: 12px;
}

.card-note {
  text-align: center;
  color: #C4B0CC;
  font-size: 11px;
  margin-top: -4px;
  padding-bottom: 4px;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid rgba(232, 130, 154, 0.08);
  background: #FFF9FB;
}

.card-actions .el-button--primary {
  background: #E8789A;
  border-color: #E8789A;
}

.card-actions .el-button--primary:hover {
  background: #E06080;
  border-color: #E06080;
}

/* 成功状态 */
.success-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 24px 16px;
}

.success-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-details {
  width: 100%;
}

.success-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.success-label {
  color: #A890B0;
  font-size: 13px;
}

.success-value {
  color: #5D4E6D;
  font-size: 14px;
  font-weight: 500;
}

.success-value.price {
  color: #E8789A;
  font-weight: 700;
}

/* 错误状态 */
.error-body {
  padding: 20px 16px;
  text-align: center;
  color: #F56C6C;
  font-size: 14px;
}

/* 输入区 */
.chat-input-area {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid rgba(232, 130, 154, 0.08);
  flex-shrink: 0;
}

.chat-input {
  flex: 1;
}

:deep(.chat-input .el-textarea__inner) {
  border-radius: 12px;
  border-color: rgba(232, 130, 154, 0.12);
  background: #FFF9FB;
  color: #5D4E6D;
  resize: none;
}

:deep(.chat-input .el-textarea__inner:focus) {
  border-color: #E8789A;
  box-shadow: 0 0 0 2px rgba(232, 130, 154, 0.08);
}

.send-btn {
  height: auto;
  border-radius: 12px;
  padding: 0 20px;
  background: #E8789A;
  border-color: #E8789A;
  display: flex;
  align-items: center;
  gap: 4px;
}

.send-btn:hover {
  background: #E06080;
  border-color: #E06080;
}
</style>
