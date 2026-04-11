/**
 * Module quản lý phiên đăng nhập (auth session).
 *
 * - Lưu token + role + thời gian đăng nhập vào localStorage (chia sẻ giữa các tab)
 * - Tự động kiểm tra hết hạn (mặc định 8 giờ)
 * - Cung cấp helper isAuthenticated / getRole / login / logout
 * - Tự động đồng bộ trạng thái đăng xuất/đăng nhập giữa các tab
 */

const TOKEN_KEY = 'token'
const ROLE_KEY = 'userRole'
const USER_KEY = 'user'
const LOGIN_TIME_KEY = 'loginTime'

/** Thời gian hết hạn phiên: 8 giờ (ms) */
const SESSION_TTL = 8 * 60 * 60 * 1000

/* ============================
   CORE HELPERS
============================ */

/** Lưu thông tin đăng nhập */
export function login({ token, role, user = null } = {}) {
  if (!token) throw new Error('Token is required for login')
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(ROLE_KEY, role)
  localStorage.setItem(LOGIN_TIME_KEY, String(Date.now()))
  if (user) localStorage.setItem(USER_KEY, JSON.stringify(user))
}

/** Xoá toàn bộ thông tin đăng nhập (đồng bộ cả token, role, user, loginTime) */
export function logout() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ROLE_KEY)
  localStorage.removeItem(USER_KEY)
  localStorage.removeItem(LOGIN_TIME_KEY)
}

/** Kiểm tra phiên còn hiệu lực không */
export function isAuthenticated() {
  const token = localStorage.getItem(TOKEN_KEY)
  const role = localStorage.getItem(ROLE_KEY)
  if (!token || !role) return false

  const loginTime = Number(localStorage.getItem(LOGIN_TIME_KEY) || 0)
  if (!loginTime) return false

  // Hết hạn → xoá luôn
  if (Date.now() - loginTime > SESSION_TTL) {
    logout()
    return false
  }

  return true
}

/** Lấy role đã chuẩn hoá */
export function getRole() {
  if (!isAuthenticated()) return null
  return localStorage.getItem(ROLE_KEY)
}

/** Lấy token */
export function getToken() {
  if (!isAuthenticated()) return null
  return localStorage.getItem(TOKEN_KEY)
}

/** "Gia hạn" phiên — gọi mỗi khi có tương tác API thành công */
export function touchSession() {
  if (isAuthenticated()) {
    localStorage.setItem(LOGIN_TIME_KEY, String(Date.now()))
  }
}

/** Lấy thông tin user hiện tại */
export function getCurrentUser() {
  if (!isAuthenticated()) return null
  const userStr = localStorage.getItem(USER_KEY)
  if (!userStr) return null
  try {
    return JSON.parse(userStr)
  } catch {
    return null
  }
}

/** Lấy tên nhân viên/khách hàng hiện tại */
export function getCurrentUserName() {
  const user = getCurrentUser()
  if (!user) return null
  return user.tenNhanVien || user.tenKhachHang || user.username || null
}

/* ============================
   CROSS-TAB SYNC
============================ */

/**
 * Lắng nghe storage event từ tab khác.
 * Khi tab khác logout (xoá token), tab hiện tại cũng redirect về login.
 * Khi tab khác login (thêm token), tab hiện tại nếu đang ở /login sẽ redirect.
 *
 * @param {object} router – Vue Router instance
 */
export function initCrossTabSync(router) {
  window.addEventListener('storage', (e) => {
    // Tab khác đã logout → redirect về login
    if (e.key === TOKEN_KEY && !e.newValue) {
      if (router.currentRoute.value.meta?.requiresAuth ||
          router.currentRoute.value.matched?.some(r => r.meta?.requiresAuth)) {
        router.push('/login')
      }
    }

    // Tab khác đã login → nếu đang ở /login thì redirect
    if (e.key === TOKEN_KEY && e.newValue && router.currentRoute.value.path === '/login') {
      const role = localStorage.getItem(ROLE_KEY)
      if (role) {
        const normalized = normalizeRoleValue(role)
        if (normalized === 'CUSTOMER') router.push('/')
        else if (normalized === 'STAFF') router.push('/staff/pos')
        else router.push('/admin/dashboard')
      }
    }
  })
}

/* ============================
   INTERNAL
============================ */
function normalizeRoleValue(role) {
  const v = String(role || '').toUpperCase()
  if (v === 'EMPLOYEE' || v === 'NHANVIEN' || v === 'NHAN_VIEN') return 'STAFF'
  return v
}