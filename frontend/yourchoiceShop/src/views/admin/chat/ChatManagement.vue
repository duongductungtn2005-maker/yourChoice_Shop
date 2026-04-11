<template>
  <div class="chat-management">
    <div class="page-header">
      <h2><i class="fa-regular fa-comment-dots"></i> Quản lý Chat</h2>
    </div>

    <!-- Tabs: Khách hàng / Nội bộ -->
    <div class="chat-tabs">
      <button
        class="tab-btn"
        :class="{ active: activeTab === 'KHACH_HANG' }"
        @click="activeTab = 'KHACH_HANG'; loadSessions()"
      >
        <i class="fa-solid fa-users"></i> Khách hàng
      </button>
      <button
        class="tab-btn tab-secondary"
        :class="{ active: activeTab === 'NOI_BO' }"
        @click="activeTab = 'NOI_BO'; loadSessions()"
      >
        <i class="fa-solid fa-building"></i> Nội bộ
      </button>
      <button
        v-if="activeTab === 'NOI_BO'"
        class="btn-new-chat"
        @click="openNewInternalChat"
      >
        <i class="fa-solid fa-plus"></i> Tạo cuộc trò chuyện
      </button>
    </div>

    <!-- Sub-tabs: Trạng thái -->
    <div class="status-tabs">
      <button
        v-for="st in statusTabs"
        :key="st.value"
        class="status-btn"
        :class="{ active: activeStatus === st.value }"
        @click="activeStatus = st.value; loadSessions()"
      >
        {{ st.label }}
      </button>
    </div>

    <div class="chat-container">
      <!-- LEFT: Danh sách session -->
      <div class="session-list">
        <div v-if="sessions.length === 0" class="empty-sessions">
          Không có phiên chat nào
        </div>
        <div
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: selectedSession?.id === session.id }"
          @click="selectSession(session)"
        >
          <div class="session-info">
            <div class="session-name">
              {{ session.tenHienThi || 'Khách vãng lai' }}
              <span class="handler-badge" v-if="session.nguoiXuLy">
                <i class="fa-solid fa-robot" v-if="session.nguoiXuLy === 'AI'"></i>
                <i class="fa-solid fa-user" v-else></i>
                {{ session.nguoiXuLy === 'AI' ? 'Ai đang xử lý' : session.nguoiXuLy }}
              </span>
            </div>
            <div class="session-preview">{{ truncate(session.lastMessage, 50) }}</div>
            <div class="session-time">{{ formatTime(session.ngayCapNhat) }}</div>
          </div>
        </div>
      </div>

      <!-- RIGHT: Chat window -->
      <div class="chat-window">
        <template v-if="selectedSession">
          <!-- Header -->
          <div class="chat-header">
            <div class="chat-header-info">
              <strong>{{ selectedSession.tenHienThi }}</strong>
              <span class="status-label" :class="'status-' + selectedSession.trangThai">
                {{ getStatusLabel(selectedSession.trangThai) }}
              </span>
            </div>
            <div class="chat-header-actions">
              <button
                v-if="selectedSession.trangThai !== 3 && selectedSession.nguoiXuLy === 'AI'"
                class="btn-assign"
                @click="handleAssign"
              >
                <i class="fa-solid fa-hand"></i> Nhận xử lý
              </button>
              <button
                v-if="selectedSession.trangThai !== 3"
                class="btn-close-session"
                @click="handleCloseSession"
              >
                <i class="fa-solid fa-xmark"></i> Đóng phiên
              </button>
            </div>
          </div>

          <!-- Messages -->
          <div class="messages-container" ref="messagesContainer">
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="message-row"
              :class="getMessageClass(msg)"
            >
              <div class="message-bubble">
                <div class="message-sender">{{ msg.senderName || msg.senderRole }}</div>
                <div class="message-content" v-html="formatMessage(msg.noiDung)"></div>
                <div class="message-time">{{ formatTime(msg.ngayGui) }}</div>
              </div>
            </div>
          </div>

          <!-- Input (nhân viên gửi tin) -->
          <div class="chat-input" v-if="selectedSession.trangThai !== 3">
            <input
              v-model="staffMessage"
              type="text"
              placeholder="Nhập tin nhắn..."
              @keyup.enter="handleStaffSend"
            />
            <button class="btn-send" @click="handleStaffSend" :disabled="!staffMessage.trim()">
              <i class="fa-solid fa-paper-plane"></i>
            </button>
          </div>
        </template>

        <template v-else>
          <div class="empty-chat">
            <i class="fa-regular fa-comment-dots empty-icon"></i>
            <p>Chọn một phiên chat để bắt đầu</p>
          </div>
        </template>
      </div>
    </div>

    <!-- MODAL: Tạo cuộc trò chuyện nội bộ -->
    <div v-if="showNewChatModal" class="modal-overlay" @click.self="showNewChatModal = false">
      <div class="modal-box">
        <div class="modal-header">
          <h3><i class="fa-solid fa-comments"></i> Tạo cuộc trò chuyện nội bộ</h3>
          <button class="modal-close" @click="showNewChatModal = false"><i class="fa-solid fa-xmark"></i></button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Tiêu đề cuộc trò chuyện</label>
            <input
              type="text"
              v-model="newChatTitle"
              class="form-input"
              placeholder="VD: Trao đổi đơn hàng HD123..."
            />
          </div>
          <div class="form-group">
            <label>Tìm nhân viên</label>
            <input
              type="text"
              v-model="staffSearch"
              class="form-input"
              placeholder="Tìm theo tên hoặc mã nhân viên..."
            />
          </div>
          <div class="staff-list">
            <div v-if="filteredStaff.length === 0" class="staff-empty">Không tìm thấy nhân viên</div>
            <div
              v-for="nv in filteredStaff"
              :key="nv.id"
              class="staff-item"
              :class="{ disabled: nv.id === currentUserId }"
            >
              <div class="staff-info">
                <div class="staff-avatar"><i class="fa-solid fa-user"></i></div>
                <div>
                  <div class="staff-name">{{ nv.tenNhanVien }}</div>
                  <div class="staff-code">{{ nv.maNhanVien }} {{ nv.chucVu ? '· ' + nv.chucVu : '' }}</div>
                </div>
              </div>
              <button
                class="btn-start-chat"
                :disabled="nv.id === currentUserId"
                @click="createInternalChat(nv)"
              >
                <i class="fa-solid fa-message"></i> Chat
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast thông báo tin nhắn mới -->
    <Transition name="toast-slide">
      <div v-if="chatToast" class="chat-toast" @click="handleToastClick">
        <div class="chat-toast-icon"><i class="fa-solid fa-comment-dots"></i></div>
        <div class="chat-toast-body">
          <strong>{{ chatToast.senderName }}</strong>
          <p>{{ chatToast.noiDung }}</p>
        </div>
        <button class="chat-toast-close" @click.stop="chatToast = null"><i class="fa-solid fa-xmark"></i></button>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { getChatSessions, getAdminChatMessages, staffSendMessage, assignStaffToSession, closeChatSession, createInternalSession, getStaffListForChat } from '@/api/chatApi'
import { getCurrentUser } from '@/services/auth'
import SockJS from 'sockjs-client/dist/sockjs'
import { Client } from '@stomp/stompjs'

const activeTab = ref('KHACH_HANG')
const activeStatus = ref(1)
const sessions = ref([])
const selectedSession = ref(null)
const messages = ref([])
const staffMessage = ref('')
const messagesContainer = ref(null)
let pollInterval = null

const statusTabs = [
  { value: 1, label: 'Đang hoạt động' },
  { value: 2, label: 'Chờ nhận' },
  { value: 3, label: 'Đã đóng' }
]

onMounted(() => {
  loadSessions()
  connectWebSocket()
  // Polling mỗi 5 giây (fallback / cập nhật session list)
  pollInterval = setInterval(() => {
    loadSessions()
  }, 5000)
})

// Cleanup
import { onUnmounted } from 'vue'
onUnmounted(() => {
  if (pollInterval) clearInterval(pollInterval)
  disconnectWebSocket()
})

async function loadSessions() {
  try {
    const res = await getChatSessions({ loaiChat: activeTab.value, trangThai: activeStatus.value })
    sessions.value = res.data
  } catch (e) {
    console.error('Load sessions failed:', e)
  }
}

async function selectSession(session) {
  selectedSession.value = session
  await loadMessages(session.id)
}

async function loadMessages(sessionId) {
  try {
    const res = await getAdminChatMessages(sessionId)
    messages.value = res.data
    await nextTick()
    scrollToBottom()
  } catch (e) {
    console.error('Load messages failed:', e)
  }
}

async function handleStaffSend() {
  if (!staffMessage.value.trim() || !selectedSession.value) return
  const user = getCurrentUser()
  try {
    await staffSendMessage({
      sessionId: selectedSession.value.id,
      noiDung: staffMessage.value,
      senderRole: 'STAFF',
      senderName: user?.tenNhanVien || 'Nhân viên',
      nhanVienId: user?.id
    })
    staffMessage.value = ''
    await loadMessages(selectedSession.value.id)
  } catch (e) {
    console.error('Send message failed:', e)
  }
}

async function handleAssign() {
  if (!selectedSession.value) return
  const user = getCurrentUser()
  try {
    await assignStaffToSession(selectedSession.value.id, user?.id)
    await loadSessions()
    await loadMessages(selectedSession.value.id)
    // Update selected session
    const updated = sessions.value.find(s => s.id === selectedSession.value.id)
    if (updated) selectedSession.value = updated
  } catch (e) {
    console.error('Assign failed:', e)
  }
}

async function handleCloseSession() {
  if (!selectedSession.value) return
  if (!confirm('Bạn có chắc muốn đóng phiên chat này?')) return
  try {
    await closeChatSession(selectedSession.value.id)
    selectedSession.value = null
    messages.value = []
    await loadSessions()
  } catch (e) {
    console.error('Close session failed:', e)
  }
}

function scrollToBottom() {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

function getMessageClass(msg) {
  if (msg.senderRole === 'SYSTEM') return 'msg-system'
  if (msg.senderRole === 'STAFF') {
    // Nội bộ: so sánh nhanVienId để phân biệt tin nhắn của mình / người khác
    if (msg.nhanVienId && msg.nhanVienId !== currentUserId.value) {
      return 'msg-other-staff'
    }
    return 'msg-staff'
  }
  if (msg.senderRole === 'AI') return 'msg-ai'
  return 'msg-customer'
}

function getStatusLabel(status) {
  if (status === 1) return 'Đang hoạt động'
  if (status === 2) return 'Chờ nhận'
  if (status === 3) return 'Đã đóng'
  return ''
}

function truncate(text, max) {
  if (!text) return ''
  return text.length > max ? text.substring(0, max) + '...' : text
}

function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const mo = String(d.getMonth() + 1).padStart(2, '0')
  return `${hh}:${mm} ${dd}-${mo}`
}

function formatMessage(text) {
  if (!text) return ''
  return text.replace(/\n/g, '<br>')
}

/* ======================== CHAT NỘI BỘ ======================== */
const showNewChatModal = ref(false)
const newChatTitle = ref('')
const staffSearch = ref('')
const staffList = ref([])
const currentUserId = computed(() => getCurrentUser()?.id)

const filteredStaff = computed(() => {
  const q = staffSearch.value.toLowerCase().trim()
  if (!q) return staffList.value
  return staffList.value.filter(nv =>
    (nv.tenNhanVien || '').toLowerCase().includes(q) ||
    (nv.maNhanVien || '').toLowerCase().includes(q)
  )
})

async function openNewInternalChat() {
  showNewChatModal.value = true
  newChatTitle.value = ''
  staffSearch.value = ''
  try {
    const res = await getStaffListForChat()
    staffList.value = res.data || []
  } catch (e) {
    console.error('Load staff list failed:', e)
  }
}

async function createInternalChat(nv) {
  try {
    const res = await createInternalSession({
      nhanVienId: nv.id,
      tieuDe: newChatTitle.value.trim() || null
    })
    showNewChatModal.value = false
    activeTab.value = 'NOI_BO'
    activeStatus.value = 1
    await loadSessions()
    // Tự động chọn session vừa tạo
    const newSession = sessions.value.find(s => s.id === res.data.id)
    if (newSession) selectSession(newSession)
  } catch (e) {
    console.error('Create internal chat failed:', e)
    alert('Tạo cuộc trò chuyện thất bại: ' + (e.response?.data || e.message))
  }
}

/* ======================== WEBSOCKET REALTIME ======================== */
let stompClient = null
let currentSubscription = null
const chatToast = ref(null)   // { senderName, noiDung, sessionId }
let toastTimer = null

function connectWebSocket() {
  stompClient = new Client({
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    reconnectDelay: 5000,
    onConnect: () => {
      // Subscribe tất cả sessions đang mở để nhận thông báo
      subscribeToAllSessions()
    },
    onStompError: (frame) => {
      console.error('Chat WebSocket error:', frame.headers?.message)
    }
  })
  stompClient.activate()
}

function disconnectWebSocket() {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
}

// Map lưu subscriptions theo sessionId
const sessionSubscriptions = new Map()

function subscribeToAllSessions() {
  // Unsubscribe cũ
  sessionSubscriptions.forEach(sub => sub.unsubscribe())
  sessionSubscriptions.clear()

  // Subscribe mỗi session đang hiển thị
  sessions.value.forEach(s => {
    subscribeToSession(s.id)
  })
}

function subscribeToSession(sessionId) {
  if (!stompClient || !stompClient.connected) return
  if (sessionSubscriptions.has(sessionId)) return

  const sub = stompClient.subscribe(`/topic/chat/${sessionId}`, (message) => {
    const newMsg = JSON.parse(message.body)
    handleIncomingMessage(newMsg, sessionId)
  })
  sessionSubscriptions.set(sessionId, sub)
}

function handleIncomingMessage(newMsg, sessionId) {
  const user = getCurrentUser()
  const isMyMessage = newMsg.senderName === (user?.tenNhanVien || '')

  // Nếu đang xem session này → thêm tin nhắn trực tiếp
  if (selectedSession.value && selectedSession.value.id === sessionId) {
    // Tránh duplicate (nếu tự gửi, đã load lại rồi)
    const exists = messages.value.some(m => m.id === newMsg.id)
    if (!exists) {
      messages.value.push(newMsg)
      nextTick(() => scrollToBottom())
    }
  }

  // Nếu tin nhắn từ người khác → hiện thông báo toast
  if (!isMyMessage && newMsg.senderRole !== 'SYSTEM') {
    showChatToast(newMsg, sessionId)
    // Refresh session list để cập nhật preview
    loadSessions()
  }
}

function showChatToast(msg, sessionId) {
  chatToast.value = {
    senderName: msg.senderName || 'Nhân viên',
    noiDung: msg.noiDung?.length > 80 ? msg.noiDung.substring(0, 80) + '...' : msg.noiDung,
    sessionId
  }
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { chatToast.value = null }, 5000)
}

function handleToastClick() {
  if (!chatToast.value) return
  const sid = chatToast.value.sessionId
  chatToast.value = null
  // Tìm và chọn session
  const target = sessions.value.find(s => s.id === sid)
  if (target) {
    selectSession(target)
  } else {
    // Có thể ở tab khác → reload
    loadSessions().then(() => {
      const t = sessions.value.find(s => s.id === sid)
      if (t) selectSession(t)
    })
  }
}

// Khi chọn session mới → subscribe nếu chưa
watch(selectedSession, (newVal) => {
  if (newVal && stompClient?.connected) {
    subscribeToSession(newVal.id)
  }
})

// Khi load xong sessions → subscribe tất cả
watch(sessions, () => {
  if (stompClient?.connected) {
    sessions.value.forEach(s => subscribeToSession(s.id))
  }
})
</script>

<style scoped>
.chat-management {
  padding: 20px;
}

.page-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 20px;
}

/* Tabs */
.chat-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 0;
}

.tab-btn {
  padding: 10px 24px;
  border: none;
  background: #f1f5f9;
  color: #64748b;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  border-radius: 8px 8px 0 0;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.tab-btn.active {
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%);
  color: #fff;
}

.tab-btn.tab-secondary.active {
  background: #6c757d;
  color: #fff;
}

/* Status tabs */
.status-tabs {
  display: flex;
  gap: 0;
  background: #f8f9fa;
  border: 1px solid #e2e8f0;
  border-bottom: none;
  padding: 0;
}

.status-btn {
  padding: 10px 24px;
  border: none;
  background: transparent;
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.2s;
}

.status-btn.active {
  color: #1e293b;
  font-weight: 700;
  border-bottom-color: #1e3a8a;
  background: #fff;
}

/* Container */
.chat-container {
  display: flex;
  border: 1px solid #e2e8f0;
  border-radius: 0 0 12px 12px;
  overflow: hidden;
  height: calc(100vh - 280px);
  min-height: 500px;
  background: #fff;
}

/* Session list */
.session-list {
  width: 340px;
  min-width: 340px;
  border-right: 1px solid #e2e8f0;
  overflow-y: auto;
  background: #fafbfc;
}

.empty-sessions {
  padding: 40px 20px;
  text-align: center;
  color: #94a3b8;
  font-size: 14px;
}

.session-item {
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: background 0.15s;
}

.session-item:hover {
  background: #f1f5f9;
}

.session-item.active {
  background: #e8f0fe;
  border-left: 3px solid #1e3a8a;
}

.session-name {
  font-weight: 600;
  font-size: 14px;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.handler-badge {
  font-size: 11px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 10px;
  background: #dbeafe;
  color: #1d4ed8;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.session-preview {
  font-size: 13px;
  color: #64748b;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 4px;
}

/* Chat window */
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.empty-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
  opacity: 0.4;
}

/* Chat header */
.chat-header {
  padding: 12px 20px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fafbfc;
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-label {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 10px;
  font-weight: 500;
}

.status-1 { background: #d1fae5; color: #065f46; }
.status-2 { background: #fef3c7; color: #92400e; }
.status-3 { background: #e2e8f0; color: #475569; }

.chat-header-actions {
  display: flex;
  gap: 8px;
}

.btn-assign {
  padding: 6px 14px;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.btn-assign:hover { background: #1d4ed8; }

.btn-close-session {
  padding: 6px 14px;
  background: #1e3a8a;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}

.btn-close-session:hover { background: #0f172a; }

/* Messages container */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #f8f9fa;
}

.message-row {
  display: flex;
}

.message-row.msg-customer,
.message-row.msg-other-staff {
  justify-content: flex-start;
}

.message-row.msg-staff,
.message-row.msg-ai {
  justify-content: flex-end;
}

.message-row.msg-system {
  justify-content: center;
}

.message-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
}

.msg-customer .message-bubble {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-bottom-left-radius: 4px;
}

.msg-other-staff .message-bubble {
  background: #e0f2fe;
  color: #0c4a6e;
  border-bottom-left-radius: 4px;
}

.msg-staff .message-bubble {
  background: #2563eb;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-ai .message-bubble {
  background: #f3e8ff;
  color: #581c87;
  border-bottom-right-radius: 4px;
}

.msg-system .message-bubble {
  background: #fef3c7;
  color: #92400e;
  font-size: 12px;
  padding: 6px 16px;
  border-radius: 20px;
}

.message-sender {
  font-size: 11px;
  font-weight: 600;
  opacity: 0.7;
  margin-bottom: 2px;
}

.msg-staff .message-sender,
.msg-ai .message-sender {
  text-align: right;
}

.message-time {
  font-size: 10px;
  opacity: 0.5;
  margin-top: 4px;
}

.msg-staff .message-time,
.msg-ai .message-time {
  text-align: right;
}

/* Input */
.chat-input {
  padding: 12px 16px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 8px;
  background: #fff;
}

.chat-input input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
}

.chat-input input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.1);
}

.btn-send {
  padding: 10px 18px;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}

.btn-send:hover { background: #1d4ed8; }
.btn-send:disabled { background: #94a3b8; cursor: not-allowed; }

/* Button tạo cuộc trò chuyện nội bộ */
.btn-new-chat {
  margin-left: auto;
  padding: 8px 18px;
  background: #059669;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: background 0.2s;
}
.btn-new-chat:hover { background: #047857; }

/* Modal overlay */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-box {
  background: #fff;
  border-radius: 12px;
  width: 500px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  overflow: hidden;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}
.modal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 8px;
}
.modal-close {
  background: none;
  border: none;
  font-size: 18px;
  color: #64748b;
  cursor: pointer;
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-close:hover { background: #f1f5f9; color: #1e293b; }
.modal-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}
.form-group {
  margin-bottom: 16px;
}
.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
}
.form-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
  transition: 0.2s;
}
.form-input:focus { border-color: #2563eb; box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.1); }

/* Staff list in modal */
.staff-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 350px;
  overflow-y: auto;
}
.staff-empty {
  text-align: center;
  color: #94a3b8;
  padding: 20px;
  font-size: 13px;
}
.staff-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  transition: 0.15s;
}
.staff-item:hover { background: #f8fafc; }
.staff-item.disabled { opacity: 0.5; }
.staff-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.staff-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e0f2fe;
  color: #0284c7;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}
.staff-name {
  font-weight: 600;
  font-size: 14px;
  color: #1e293b;
}
.staff-code {
  font-size: 12px;
  color: #94a3b8;
}
.btn-start-chat {
  padding: 6px 14px;
  background: #2563eb;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
}
.btn-start-chat:hover { background: #1d4ed8; }
.btn-start-chat:disabled { background: #94a3b8; cursor: not-allowed; }

/* ===== Chat Toast Notification ===== */
.chat-toast {
  position: fixed;
  bottom: 24px;
  right: 24px;
  background: #1e293b;
  color: #f1f5f9;
  border-radius: 12px;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.25);
  cursor: pointer;
  z-index: 9999;
  max-width: 380px;
  transition: transform 0.2s;
}
.chat-toast:hover { transform: translateY(-2px); }
.chat-toast-icon {
  font-size: 22px;
  color: #38bdf8;
  flex-shrink: 0;
}
.chat-toast-body {
  flex: 1;
  min-width: 0;
}
.chat-toast-body strong {
  display: block;
  font-size: 13px;
  color: #38bdf8;
  margin-bottom: 2px;
}
.chat-toast-body p {
  margin: 0;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.chat-toast-close {
  background: none;
  border: none;
  color: #94a3b8;
  font-size: 14px;
  cursor: pointer;
  padding: 2px 4px;
  flex-shrink: 0;
}
.chat-toast-close:hover { color: #93c5fd; }

/* Toast transition */
.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: all 0.35s ease;
}
.toast-slide-enter-from,
.toast-slide-leave-to {
  opacity: 0;
  transform: translateX(80px);
}
</style>
