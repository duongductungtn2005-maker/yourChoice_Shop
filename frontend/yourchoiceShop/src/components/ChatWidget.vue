<template>
  <!-- Floating chat button -->
  <div class="chat-widget">
    <button class="chat-toggle" @click="toggleChat" :class="{ active: isOpen }">
      <i class="fa-solid fa-comments" v-if="!isOpen"></i>
      <i class="fa-solid fa-xmark" v-else></i>
    </button>

    <!-- Chat panel -->
    <div class="chat-panel" v-show="isOpen">
      <div class="chat-panel-header">
        <div class="header-info">
          <i class="fa-solid fa-headset"></i>
          <div>
            <div class="header-title">Hỗ trợ trực tuyến</div>
            <div class="header-subtitle">{{ handlerName }}</div>
          </div>
        </div>
        <button class="btn-minimize" @click="isOpen = false">
          <i class="fa-solid fa-minus"></i>
        </button>
      </div>

      <!-- Messages -->
      <div class="chat-messages" ref="chatMessages">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="msg-row"
          :class="getMsgClass(msg)"
        >
          <div class="msg-bubble">
            <div class="msg-sender" v-if="msg.senderRole !== 'CUSTOMER'">
              {{ msg.senderName }}
            </div>
            <div class="msg-text" v-html="formatText(msg.noiDung)"></div>
            <div class="msg-time">{{ formatTime(msg.ngayGui) }}</div>
          </div>
        </div>

        <!-- Product suggestions -->
        <div v-if="suggestedProducts.length > 0" class="product-suggestions">
          <div class="product-card" v-for="p in suggestedProducts" :key="p.id" @click="goToProduct(p.id)">
            <img
              v-if="p.hinhAnh"
              :src="'http://localhost:8080/images/' + p.hinhAnh"
              :alt="p.tenSanPham"
              class="product-img"
            />
            <div class="product-info">
              <div class="product-name">{{ p.tenSanPham }}</div>
              <div class="product-brand" v-if="p.thuongHieu">{{ p.thuongHieu }}</div>
              <div class="product-price">
                <template v-if="p.giaSauGiam">
                  <span class="price-old">{{ formatMoney(p.giaMin) }}</span>
                  <span class="price-sale">{{ formatMoney(p.giaSauGiam) }}</span>
                  <span v-if="p.giamGia" class="badge-discount">-{{ p.giamGia }}%</span>
                </template>
                <template v-else>
                  {{ formatMoney(p.giaMin) }}
                  <span v-if="p.giaMin !== p.giaMax"> ~ {{ formatMoney(p.giaMax) }}</span>
                </template>
              </div>
              <div class="product-attrs" v-if="p.mauSacs?.length || p.kichThuocs?.length">
                <span v-if="p.mauSacs?.length" class="attr-tag">🎨 {{ p.mauSacs.join(', ') }}</span>
                <span v-if="p.kichThuocs?.length" class="attr-tag">📏 {{ p.kichThuocs.join(', ') }}</span>
              </div>
              <div class="product-stock" v-if="p.tonKho != null">
                <span :class="p.tonKho > 0 ? 'stock-ok' : 'stock-out'">
                  {{ p.tonKho > 0 ? `Còn ${p.tonKho} sp` : 'Hết hàng' }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Typing indicator -->
        <div v-if="isTyping" class="msg-row msg-other">
          <div class="msg-bubble typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>

      <!-- Input -->
      <div class="chat-input-area">
        <div class="chat-actions">
          <button
            class="btn-request-staff"
            @click="handleRequestStaff"
            :disabled="requestingStaff || !sessionId || staffRequested || sessionClosed"
          >
            <i class="fa-solid fa-user-tie"></i>
            {{ staffRequested ? 'Đã yêu cầu nhân viên' : 'Chat với Nhân viên' }}
          </button>
        </div>
        <div class="chat-input-row">
          <input
            v-model="inputMessage"
            type="text"
            placeholder="Nhập tin nhắn..."
            @keyup.enter="handleSend"
            :disabled="sending"
          />
          <button class="btn-send" @click="handleSend" :disabled="!inputMessage.trim() || sending">
            <i class="fa-solid fa-paper-plane"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrCreateSession, sendChatMessage, getChatMessages, getSessionInfo, requestStaffSupport } from '@/api/chatApi'
import { getCurrentUser, isAuthenticated } from '@/services/auth'

const router = useRouter()

const isOpen = ref(false)
const messages = ref([])
const inputMessage = ref('')
const sending = ref(false)
const isTyping = ref(false)
const sessionId = ref(null)
const sessionKey = ref(null)
const sessionStatus = ref(1)
const handlerName = ref('Trợ lý AI')
const staffRequested = ref(false)
const requestingStaff = ref(false)
const suggestedProducts = ref([])
const chatMessages = ref(null)
const sessionClosed = computed(() => sessionStatus.value === 3)
let pollInterval = null

onMounted(() => {
  // Tạo session key cho khách vãng lai
  let stored = sessionStorage.getItem('chatSessionKey')
  if (!stored) {
    stored = 'anon_' + Date.now() + '_' + Math.random().toString(36).substring(2, 9)
    sessionStorage.setItem('chatSessionKey', stored)
  }
  sessionKey.value = stored
})

onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
})

function toggleChat() {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    initChat()
  }
}

async function initChat() {
  try {
    const user = isAuthenticated() ? getCurrentUser() : null
    const res = await getOrCreateSession({
      khachHangId: user?.id || null,
      sessionKey: sessionKey.value,
      tenHienThi: user?.tenKhachHang || 'Khách vãng lai'
    })
    sessionId.value = res.data.id
    applySessionState(res.data)

    await loadMessages()

    // Nếu chưa có tin nhắn, hiển thị lời chào
    if (messages.value.length === 0) {
      messages.value.push({
        id: 'welcome',
        senderRole: 'AI',
        senderName: 'Trợ lý AI',
        noiDung: 'Xin chào! 👋 Mình là trợ lý ảo của YourChoice Shop. Mình có thể giúp bạn tìm sản phẩm, kiểm tra voucher, hoặc kết nối với nhân viên tư vấn. Bạn cần hỗ trợ gì ạ?',
        loaiTinNhan: 'TEXT',
        ngayGui: new Date().toISOString()
      })
    }

    // Start polling (messages + session state)
    if (pollInterval) clearInterval(pollInterval)
    pollInterval = setInterval(() => {
      loadMessages()
      refreshSessionState()
    }, 3000)
  } catch (e) {
    console.error('Init chat failed:', e)
  }
}

async function loadMessages() {
  if (!sessionId.value) return
  try {
    const res = await getChatMessages(sessionId.value)
    if (res.data && res.data.length > 0) {
      messages.value = res.data
      suggestedProducts.value = [] // Clear product suggestions when messages refresh
    }
    await nextTick()
    scrollToBottom()
  } catch (e) {
    console.error('Load messages error:', e)
  }
}

async function refreshSessionState() {
  if (!sessionId.value) return
  try {
    const res = await getSessionInfo(sessionId.value)
    const session = res.data
    if (session) {
      applySessionState(session)
    }
  } catch (e) {
    // silent
  }
}

async function handleSend() {
  if (!inputMessage.value.trim() || sending.value) return

  const text = inputMessage.value.trim()
  inputMessage.value = ''
  sending.value = true
  isTyping.value = true
  suggestedProducts.value = []

  // Add user message immediately
  messages.value.push({
    id: 'temp_' + Date.now(),
    senderRole: 'CUSTOMER',
    senderName: 'Bạn',
    noiDung: text,
    loaiTinNhan: 'TEXT',
    ngayGui: new Date().toISOString()
  })

  await nextTick()
  scrollToBottom()

  try {
    const user = isAuthenticated() ? getCurrentUser() : null
    const res = await sendChatMessage({
      sessionId: sessionId.value,
      noiDung: text,
      senderRole: 'CUSTOMER',
      senderName: user?.tenKhachHang || 'Khách vãng lai',
      khachHangId: user?.id || null,
      sessionKey: sessionKey.value
    })

    // Update sessionId if new
    if (res.data.sessionId) sessionId.value = res.data.sessionId

    // Add AI reply if exists
    if (res.data.aiReply) {
      messages.value.push(res.data.aiReply)
    }

    // Product suggestions
    if (res.data.products && res.data.products.length > 0) {
      suggestedProducts.value = res.data.products
    }

    // Transfer to staff notification
    if (res.data.transferToStaff) {
      sessionStatus.value = 2
      staffRequested.value = true
      handlerName.value = 'Đang chờ nhân viên...'
    }
  } catch (e) {
    console.error('Send failed:', e)
    messages.value.push({
      id: 'err_' + Date.now(),
      senderRole: 'SYSTEM',
      senderName: 'Hệ thống',
      noiDung: 'Không thể gửi tin nhắn. Vui lòng thử lại.',
      loaiTinNhan: 'TEXT',
      ngayGui: new Date().toISOString()
    })
  } finally {
    sending.value = false
    isTyping.value = false
    await nextTick()
    scrollToBottom()
  }
}

async function handleRequestStaff() {
  if (!sessionId.value || requestingStaff.value || staffRequested.value || sessionClosed.value) return

  requestingStaff.value = true
  try {
    const res = await requestStaffSupport(sessionId.value)
    applySessionState(res.data)
    await loadMessages()
  } catch (e) {
    console.error('Request staff support failed:', e)
    messages.value.push({
      id: 'err_staff_' + Date.now(),
      senderRole: 'SYSTEM',
      senderName: 'Hệ thống',
      noiDung: 'Không thể yêu cầu nhân viên lúc này. Vui lòng thử lại.',
      loaiTinNhan: 'TEXT',
      ngayGui: new Date().toISOString()
    })
  } finally {
    requestingStaff.value = false
  }
}

function applySessionState(session) {
  sessionStatus.value = session?.trangThai ?? 1

  const isWaiting = session?.trangThai === 2 || session?.nguoiXuLy === 'Chờ nhân viên'
  const hasStaffHandler = session?.nguoiXuLy && session.nguoiXuLy !== 'AI' && session.nguoiXuLy !== 'Chờ nhân viên'
  staffRequested.value = !!(isWaiting || hasStaffHandler)

  if (sessionStatus.value === 3) {
    handlerName.value = 'Phiên đã kết thúc'
    return
  }

  if (isWaiting) {
    handlerName.value = 'Đang chờ nhân viên...'
    return
  }

  handlerName.value = session?.nguoiXuLy === 'AI' ? 'Trợ lý AI' : session?.nguoiXuLy || 'Hỗ trợ'
}

function goToProduct(id) {
  router.push(`/product/${id}`)
  isOpen.value = false
}

function scrollToBottom() {
  if (chatMessages.value) {
    chatMessages.value.scrollTop = chatMessages.value.scrollHeight
  }
}

function getMsgClass(msg) {
  if (msg.senderRole === 'CUSTOMER') return 'msg-mine'
  if (msg.senderRole === 'SYSTEM') return 'msg-system'
  return 'msg-other'
}

function formatText(text) {
  if (!text) return ''
  return text.replace(/\n/g, '<br>')
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${hh}:${mm}`
}

function formatMoney(amount) {
  if (!amount) return '0đ'
  return new Intl.NumberFormat('vi-VN').format(amount) + 'đ'
}
</script>

<style scoped>
.chat-widget {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
}

.chat-toggle {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%);
  color: #fff;
  border: none;
  font-size: 24px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.chat-toggle:hover { transform: scale(1.1); }
.chat-toggle.active { background: #0f172a; }

/* Panel */
.chat-panel {
  position: absolute;
  bottom: 70px;
  right: 0;
  width: 380px;
  height: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-panel-header {
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%);
  color: #fff;
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-info i {
  font-size: 24px;
}

.header-title {
  font-weight: 700;
  font-size: 15px;
}

.header-subtitle {
  font-size: 12px;
  opacity: 0.85;
}

.btn-minimize {
  background: rgba(255,255,255,0.2);
  border: none;
  color: #fff;
  width: 28px;
  height: 28px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

/* Messages */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #f8f9fa;
}

.msg-row { display: flex; }
.msg-row.msg-mine { justify-content: flex-end; }
.msg-row.msg-other { justify-content: flex-start; }
.msg-row.msg-system { justify-content: center; }

.msg-bubble {
  max-width: 80%;
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.5;
}

.msg-mine .msg-bubble {
  background: #1e3a8a;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-other .msg-bubble {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-bottom-left-radius: 4px;
}

.msg-system .msg-bubble {
  background: #fef3c7;
  color: #92400e;
  font-size: 11px;
  padding: 4px 14px;
  border-radius: 20px;
}

.msg-sender {
  font-size: 11px;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 2px;
}

.msg-time {
  font-size: 10px;
  opacity: 0.5;
  margin-top: 3px;
}

.msg-mine .msg-time { text-align: right; }

/* Typing indicator */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px !important;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #94a3b8;
  animation: typingBounce 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typingBounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-6px); }
}

/* Product suggestions */
.product-suggestions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px 0;
}

.product-card {
  display: flex;
  gap: 10px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.product-card:hover {
  border-color: #1e3a8a;
  box-shadow: 0 2px 8px rgba(30, 58, 138, 0.12);
}

.product-img {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border-radius: 6px;
  background: #f3f4f6;
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 13px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-brand {
  font-size: 11px;
  color: #94a3b8;
}

.product-price {
  font-size: 13px;
  font-weight: 700;
  color: #1e3a8a;
  margin-top: 2px;
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.price-old {
  text-decoration: line-through;
  color: #94a3b8;
  font-weight: 400;
  font-size: 11px;
}

.price-sale {
  color: #1e3a8a;
  font-weight: 700;
}

.badge-discount {
  background: #1e3a8a;
  color: #fff;
  font-size: 10px;
  padding: 1px 5px;
  border-radius: 4px;
  font-weight: 600;
}

.product-attrs {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  margin-top: 3px;
}

.attr-tag {
  font-size: 10px;
  color: #64748b;
  background: #f1f5f9;
  padding: 1px 5px;
  border-radius: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 140px;
}

.product-stock {
  margin-top: 2px;
  font-size: 10px;
}

.stock-ok { color: #16a34a; font-weight: 600; }
.stock-out { color: #475569; font-weight: 600; }

/* Input */
.chat-input-area {
  padding: 10px 12px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: #fff;
}

.chat-actions {
  display: flex;
}

.btn-request-staff {
  border: 1px solid #bfdbfe;
  background: #eff6ff;
  color: #1e3a8a;
  border-radius: 999px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-request-staff:hover {
  background: #dbeafe;
}

.btn-request-staff:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.chat-input-row {
  display: flex;
  gap: 8px;
}

.chat-input-row input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 20px;
  font-size: 13px;
  outline: none;
}

.chat-input-row input:focus {
  border-color: #1e3a8a;
}

.btn-send {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #1e3a8a;
  color: #fff;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.btn-send:hover {
  background: #0f172a;
}

.btn-send:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 480px) {
  .chat-panel {
    width: calc(100vw - 24px);
    right: -12px;
    height: 70vh;
  }
}
</style>
