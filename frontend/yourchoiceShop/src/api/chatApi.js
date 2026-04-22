/**
 * Chat API service
 * API calls cho tính năng Chat (AI + Nhân viên)
 */
import axios from 'axios'
import request from '@/services/request'

const API_URL = 'http://localhost:8080/api/v1/chat'

// ===================== SESSION =====================

/** Tạo hoặc lấy session chat */
export function getOrCreateSession({ khachHangId, sessionKey, tenHienThi }) {
  return axios.post(`${API_URL}/session`, null, {
    params: { khachHangId, sessionKey, tenHienThi }
  })
}

/** Lấy danh sách sessions (admin) */
export function getChatSessions(params) {
  return request.get('/chat/sessions', { params })
}

/** Lấy lịch sử tin nhắn của session */
export function getChatMessages(sessionId) {
  return axios.get(`${API_URL}/sessions/${sessionId}/messages`)
}

/** Lấy thông tin session (polling, không cần auth) */
export function getSessionInfo(sessionId) {
  return axios.get(`${API_URL}/sessions/${sessionId}`)
}

/** Lấy lịch sử tin nhắn của session (admin, có auth) */
export function getAdminChatMessages(sessionId) {
  return request.get(`/chat/sessions/${sessionId}/messages`)
}

// ===================== MESSAGE =====================

/** Gửi tin nhắn */
export function sendChatMessage(data) {
  return axios.post(`${API_URL}/send`, data)
}

/** Khách hàng yêu cầu nhân viên hỗ trợ */
export function requestStaffSupport(sessionId) {
  return axios.post(`${API_URL}/sessions/${sessionId}/request-staff`)
}

/** Nhân viên gửi tin nhắn (có auth) */
export function staffSendMessage(data) {
  return request.post('/chat/send', data)
}

// ===================== ADMIN =====================

/** Nhân viên nhận xử lý session */
export function assignStaffToSession(sessionId, nhanVienId) {
  return request.post(`/chat/sessions/${sessionId}/assign`, null, {
    params: { nhanVienId }
  })
}

/** Đóng session */
export function closeChatSession(sessionId) {
  return request.post(`/chat/sessions/${sessionId}/close`)
}

// ===================== CHAT NỘI BỘ =====================

/** Tạo session chat nội bộ */
export function createInternalSession({ nhanVienId, tieuDe }) {
  return request.post('/chat/internal/session', null, {
    params: { nhanVienId, tieuDe }
  })
}

/** Lấy danh sách nhân viên */
export function getStaffListForChat() {
  return request.get('/chat/staff-list')
}
