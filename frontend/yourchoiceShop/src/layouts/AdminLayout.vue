<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="logo-circle">
          <img src="@/img/logo1.png" alt="Logo" @error="handleImageError" />
        </div>
        <span class="brand-name">YourChoice</span>
      </div>

      <nav class="menu">
        <!-- Trang chủ -->
        <router-link
          v-if="isAdmin || (isStaff && hasActiveShift)"
          to="/admin/home" 
          class="menu-item"
          active-class="active-link"
        >
          <i class="fa-solid fa-house icon"></i> Trang chủ
        </router-link>

        <!-- Admin only -->
        <router-link
          v-if="isAdmin"
          to="/admin/dashboard"
          class="menu-item"
          active-class="active-link"
        >
          <i class="fa-solid fa-gauge icon"></i> Thống kê
        </router-link>

        <router-link 
          v-if="isStaff" 
          to="/staff/giao-ca" 
          class="menu-item" 
          active-class="active-link"
        >
          <i class="fas fa-clock icon"></i> Trực ca làm việc
        </router-link>

        <template v-if="isAdmin || (isStaff && hasActiveShift)">
          <router-link
            :to="`${basePath}/pos`"
            class="menu-item"
            active-class="active-link"
          >
            <i class="fa-solid fa-shop icon"></i> Bán hàng tại quầy
          </router-link>

          <router-link
            :to="`${basePath}/orders`"
            class="menu-item"
            active-class="active-link"
          >
            <i class="fa-solid fa-file-lines icon"></i> Quản lý hóa đơn
          </router-link>

          <router-link
            v-if="isStaff"
            :to="`${basePath}/customers`"
            class="menu-item"
            active-class="active-link"
          >
            <i class="fa-solid fa-users icon"></i> Khách hàng
          </router-link>
        </template>

        <!-- Thông báo cho nhân viên khi chưa có ca -->
        <div v-if="isStaff && !hasActiveShift" class="shift-notice">
          <i class="fa-solid fa-clock icon"></i>
          <span>Chưa mở ca làm việc</span>
          <router-link to="/staff/giao-ca" class="shift-link">
            Mở ca ngay
          </router-link>
        </div>


        <div v-if="isAdmin" class="menu-group">
          <div
            class="menu-item parent"
            :class="{ 'active-parent': openMenus.products || isProductRoute }"
            @click="toggleMenu('products')"
          >
            <span>
              <i class="fa-solid fa-shirt icon"></i> Quản lý sản phẩm
            </span>
            <span class="arrow">{{ openMenus.products ? '▲' : '▼' }}</span>
          </div>

          <div class="submenu" v-show="openMenus.products">
            <router-link to="/admin/products" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Sản phẩm
            </router-link>
            <router-link to="/admin/co-ao" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Cổ áo
            </router-link>
            <router-link to="/admin/tay-ao" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Tay áo
            </router-link>
            <router-link to="/admin/xuat-xu" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Xuất xứ
            </router-link>
            <router-link to="/admin/chat-lieu" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Chất liệu
            </router-link>
            <router-link to="/admin/thuong-hieu" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Thương hiệu
            </router-link>
            <router-link to="/admin/mau-sac" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Màu sắc
            </router-link>
            <router-link to="/admin/kich-thuoc" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Kích thước
            </router-link>
          </div>
        </div>

        <div v-if="isAdmin" class="menu-group">
          <div
            class="menu-item parent"
            :class="{ 'active-parent': openMenus.discounts || isDiscountRoute }"
            @click="toggleMenu('discounts')"
          >
            <span>
              <i class="fa-solid fa-ticket icon"></i> Giảm giá
            </span>
            <span class="arrow">{{ openMenus.discounts ? '▲' : '▼' }}</span>
          </div>

          <div class="submenu" v-show="openMenus.discounts">
            <router-link to="/admin/vouchers" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Phiếu giảm giá
            </router-link>
            <router-link to="/admin/sales" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Đợt giảm giá
            </router-link>
          </div>
        </div>

        <div v-if="isAdmin" class="menu-group">
          <div
            class="menu-item parent"
            :class="{ 'active-parent': openMenus.workSchedules || isWorkScheduleRoute }"
            @click="toggleMenu('workSchedules')"
          >
            <span>
              <i class="fa-solid fa-calendar-days icon"></i> Quản lý lịch làm việc
            </span>
            <span class="arrow">{{ openMenus.workSchedules ? '▲' : '▼' }}</span>
          </div>

          <div class="submenu" v-show="openMenus.workSchedules">
            <router-link to="/admin/shifts" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Quản lý ca làm việc
            </router-link>
            <router-link to="/admin/schedules" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Xếp lịch nhân viên
            </router-link>
            <router-link to="/admin/history-activity" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Lịch sử hoạt động
            </router-link>
          </div>
        </div>

        <div v-if="isAdmin" class="menu-group">
          <div
            class="menu-item parent"
            :class="{ 'active-parent': openMenus.accounts || isAccountRoute }"
            @click="toggleMenu('accounts')"
          >
            <span>
              <i class="fa-solid fa-user-shield icon"></i> Quản lý tài khoản
            </span>
            <span class="arrow">{{ openMenus.accounts ? '▲' : '▼' }}</span>
          </div>

          <div class="submenu" v-show="openMenus.accounts">
            <router-link to="/admin/customers" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Khách hàng
            </router-link>
            <router-link to="/admin/employees" class="submenu-item" active-class="active-sub">
              <span class="dot">•</span> Nhân viên
            </router-link>
          </div>
        </div>

        <!-- Chat Management -->
        <router-link
          v-if="isAdmin || (isStaff && hasActiveShift)"
          :to="`${basePath}/chat`"
          class="menu-item"
          active-class="active-link"
        >
          <i class="fa-regular fa-comment-dots icon"></i> Quản lý Chat
        </router-link>
      </nav>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <div class="header-actions">
          <button class="icon-btn" title="Lịch">
            <i class="fa-regular fa-calendar"></i>
          </button>
          <div class="notification-wrapper" ref="notifWrapperRef">
            <button class="icon-btn" :class="{ 'bell-ring': bellRinging }" title="Thông báo" @click="toggleNotifDropdown">
              <i class="fa-regular fa-bell"></i>
              <span class="badge-count" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
            </button>
            <div v-if="isNotifOpen" class="notif-dropdown">
              <div class="notif-header">
                <span class="notif-title">Thông báo</span>
                <button v-if="unreadCount > 0" class="mark-all-btn" @click.stop="markAllAsRead">Đánh dấu đã đọc tất cả</button>
              </div>
              <div class="notif-body" v-if="notifications.length > 0">
                <div
                  v-for="item in notifications"
                  :key="item.id"
                  class="notif-item"
                  :class="{ unread: !item.daDoc }"
                  @click="handleNotifClick(item)"
                >
                  <div class="notif-icon-box">
                    <i class="fa-solid fa-cart-shopping"></i>
                  </div>
                  <div class="notif-content">
                    <div class="notif-item-title">{{ item.tieuDe }}</div>
                    <div class="notif-item-desc">{{ item.noiDung }}</div>
                    <div class="notif-item-time">{{ timeAgo(item.ngayTao) }}</div>
                  </div>
                  <div v-if="!item.daDoc" class="notif-dot"></div>
                </div>
              </div>
              <div v-else class="notif-empty">Không có thông báo</div>
            </div>
          </div>
        </div>

        <div
          class="user-info"
          @click="toggleUserDropdown"
          :class="{ 'dropdown-open': isUserDropdownOpen }"
        >
          <div class="avatar">
            <i class="fa-solid fa-user"></i>
          </div>
          <i class="fa-solid fa-chevron-down dropdown-arrow"></i>

          <div v-if="isUserDropdownOpen" class="user-dropdown">
            <div class="dropdown-item admin-label">{{ userRoleLabel }}</div>
            <button class="dropdown-item profile-btn" @click.stop="goToProfile">
              <i class="fa-solid fa-user-pen"></i> Thông tin cá nhân
            </button>
            <button class="dropdown-item logout-btn" @click.stop="handleLogout">
              <i class="fa-solid fa-sign-out-alt"></i> Đăng xuất
            </button>
          </div>
        </div>
      </header>

      <div class="content-body">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { toastSuccess } from '@/utils/toast';
import { logout as authLogout, getRole, getCurrentUser, getCurrentUserName } from '@/services/auth'; // Xóa getCurrentUserName vì bạn tự viết logic tính toán rồi
import axios from 'axios';
import SockJS from 'sockjs-client/dist/sockjs';
import { Client } from '@stomp/stompjs';
import { useShiftStore } from '@/stores/shiftStore';

const API_URL = 'http://localhost:8080/api/v1';

const shiftStore = useShiftStore()
const route = useRoute();
const router = useRouter();

/* =========================
   PHÂN QUYỀN VÀ CA LÀM VIỆC
========================= */
const normalizeRole = (value) => {
  const role = String(value || '').toUpperCase();
  if (role === 'EMPLOYEE' || role === 'NHANVIEN' || role === 'NHAN_VIEN') return 'STAFF';
  return role;
};

const userRole = computed(() => {
  const role = getRole();
  return normalizeRole(role);
});

const isAdmin = computed(() => userRole.value === 'ADMIN');
const isStaff = computed(() => userRole.value === 'STAFF');
const basePath = computed(() => (isAdmin.value ? '/admin' : '/staff'));

const currentUser = computed(() => getCurrentUser());
const currentUserName = computed(() => {
  const user = currentUser.value;
  if (!user) return null;
  return user.tenNhanVien || user.tenKhachHang || user.username || null;
});

const userRoleLabel = computed(() => {
  if (isAdmin.value && currentUserName.value) return `Admin: ${currentUserName.value}`;
  if (isAdmin.value) return 'Admin';
  if (isStaff.value && currentUserName.value) return `Nhân viên: ${currentUserName.value}`;
  return 'Nhân viên';
});

const hasActiveShift = computed(() => shiftStore.hasActiveShift);

onMounted(() => {
  shiftStore.fetchShift();
});
/* =========================
   USER DROPDOWN
========================= */
const isUserDropdownOpen = ref(false);

const toggleUserDropdown = () => {
  isUserDropdownOpen.value = !isUserDropdownOpen.value;
};

const handleClickOutside = (e) => {
  const userInfoEl = document.querySelector('.user-info');
  if (userInfoEl && !userInfoEl.contains(e.target)) {
    isUserDropdownOpen.value = false;
  }
  if (notifWrapperRef.value && !notifWrapperRef.value.contains(e.target)) {
    isNotifOpen.value = false;
  }
};

watch(() => route.path, () => {
  isUserDropdownOpen.value = false;
});

onMounted(() => {
  document.addEventListener('click', handleClickOutside);
  fetchNotifications();
  connectWebSocket();
});
onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside);
  disconnectWebSocket();
});

const goToProfile = () => {
  isUserDropdownOpen.value = false;
  const profilePath = isAdmin.value ? '/admin/thong-tin-ca-nhan' : '/staff/thong-tin-ca-nhan';
  router.push(profilePath);
};

const handleLogout = () => {
  shiftStore.clearShift();
  authLogout();
  isUserDropdownOpen.value = false;

  try {
    toastSuccess('Đăng xuất thành công!');
  } catch {
    // Nếu chưa cấu hình toast thì bỏ qua
  }

  router.push('/admin/login');
};

/* =========================
   THÔNG BÁO REALTIME
========================= */
const isNotifOpen = ref(false);
const notifications = ref([]);
const unreadCount = ref(0);
const bellRinging = ref(false);
const notifWrapperRef = ref(null);
let stompClient = null;

const toggleNotifDropdown = () => {
  isNotifOpen.value = !isNotifOpen.value;
  if (isNotifOpen.value) fetchNotifications();
};

const fetchNotifications = async () => {
  try {
    const res = await axios.get(`${API_URL}/thong-bao`);
    notifications.value = res.data.items || [];
    unreadCount.value = res.data.unreadCount || 0;
  } catch (e) {
    console.error('Lỗi tải thông báo:', e);
  }
};

const markAsRead = async (id) => {
  try {
    await axios.put(`${API_URL}/thong-bao/${id}/read`);
    const item = notifications.value.find((n) => n.id === id);
    if (item && !item.daDoc) {
      item.daDoc = true;
      unreadCount.value = Math.max(0, unreadCount.value - 1);
    }
  } catch (e) {
    console.error('Lỗi đánh dấu đã đọc:', e);
  }
};

const markAllAsRead = async () => {
  try {
    await axios.put(`${API_URL}/thong-bao/read-all`);
    notifications.value.forEach((n) => (n.daDoc = true));
    unreadCount.value = 0;
  } catch (e) {
    console.error('Lỗi đánh dấu tất cả:', e);
  }
};

const handleNotifClick = (item) => {
  if (!item.daDoc) markAsRead(item.id);
  if (item.maHoaDon) {
    isNotifOpen.value = false;
    router.push(`${basePath.value}/orders`);
  }
};

const triggerBellAnimation = () => {
  bellRinging.value = true;
  setTimeout(() => { bellRinging.value = false; }, 1500);
};

const connectWebSocket = () => {
  stompClient = new Client({
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    reconnectDelay: 5000,
    onConnect: () => {
      stompClient.subscribe('/topic/notifications', (message) => {
        const newNotif = JSON.parse(message.body);
        notifications.value.unshift(newNotif);
        unreadCount.value++;
        triggerBellAnimation();
      });
    },
    onStompError: (frame) => {
      console.error('WebSocket STOMP error:', frame.headers?.message);
    },
  });
  stompClient.activate();
};

const disconnectWebSocket = () => {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
  }
};

const timeAgo = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = Math.floor((now - date) / 1000);
  if (diff < 60) return 'Vừa xong';
  if (diff < 3600) return Math.floor(diff / 60) + ' phút trước';
  if (diff < 86400) return Math.floor(diff / 3600) + ' giờ trước';
  if (diff < 604800) return Math.floor(diff / 86400) + ' ngày trước';
  return date.toLocaleDateString('vi-VN');
};

/* =========================
   MENU TOGGLE
========================= */
const openMenus = ref({
  products: false,
  discounts: false,
  workSchedules: false,
  accounts: false
});

const toggleMenu = (key) => {
  openMenus.value[key] = !openMenus.value[key];
};

/* =========================
   ROUTE ACTIVE
========================= */
const isProductRoute = computed(() => {
  const p = route.path;
  return (
    p.includes('/admin/products') || p.includes('/admin/co-ao') ||
    p.includes('/admin/tay-ao') || p.includes('/admin/xuat-xu') ||
    p.includes('/admin/chat-lieu') || p.includes('/admin/thuong-hieu') ||
    p.includes('/admin/mau-sac') || p.includes('/admin/kich-thuoc')
  );
});

const isDiscountRoute = computed(() => route.path.includes('/admin/vouchers') || route.path.includes('/admin/sales'));
const isWorkScheduleRoute = computed(() => route.path.includes('/admin/shifts') || route.path.includes('/admin/schedules'));
const isAccountRoute = computed(() => route.path.includes('/admin/customers') || route.path.includes('/staff/customers') || route.path.includes('/admin/employees'));

/* =========================
   LOGO FALLBACK
========================= */
const handleImageError = (e) => {
  e.target.style.display = 'none';
}; 
</script>

<style scoped>
/* --- 1. BIẾN MÀU SẮC --- */
:root {
  --primary-color: #2b4360;
  --muted-color: #64748b;
  --bg-light: #f8fafc;
  --bg-surface: #f1f5f9;
  --border-color: #e2e8f0;
  --card-bg: #ffffff;
  --danger-color: #ef4444;
}

/* --- 2. LAYOUT CHUNG --- */
.admin-layout {
  display: flex;
  min-height: 100vh;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}
.sidebar {
  width: 260px;
  background: white;
  border-right: 1px solid #e2e8f0;
  position: fixed;
  height: 100vh;
  overflow-y: auto;
  z-index: 10;
  display: flex;
  flex-direction: column;
}
.main-content {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

/* --- 3. BRAND / LOGO --- */
.brand {
  margin-top: 10px;
  height: 80px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #f1f5f9;
}
.logo-circle {
  width: 45px;
  height: 45px;
  border-radius: 12px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  background: #f8fafc;
}
.logo-circle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.brand-name {
  font-weight: 700;
  font-size: 20px;
  color: #2b4360;
}

/* --- 4. MENU STYLES --- */
.menu {
  padding: 15px 12px;
}

.menu-item {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  color: #64748b;
  cursor: pointer;
  text-decoration: none;
  font-weight: 500;
  font-size: 14px;
  transition: all 0.2s ease;
  border-radius: 8px;
  margin-bottom: 4px;
}

.menu-item .icon {
  width: 24px;
  text-align: center;
  margin-right: 12px;
  font-size: 18px;
  transition: color 0.2s;
}

.menu-item:hover {
  background-color: #f1f5f9;
  color: #2b4360;
}

.active-link {
  background-color: #e0f2fe !important;
  color: #0284c7 !important;
  font-weight: 700;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}
.active-link .icon {
  color: #0284c7 !important;
}

/* --- 5. PARENT MENU STYLES --- */
.menu-item.parent {
  justify-content: space-between;
}
.arrow {
  font-size: 10px;
  color: var(--muted-color);
}

.active-parent {
  color: #2b4360 !important;
  font-weight: 700;
  background-color: #f8fafc;
}

/* --- 6. SUBMENU STYLES --- */
.submenu {
  margin-left: 12px;
  padding-left: 12px;
  border-left: 1px solid #e2e8f0;
  margin-bottom: 10px;
}

.submenu-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  text-decoration: none;
  color: #64748b;
  font-size: 14px;
  transition: 0.2s ease;
  border-radius: 6px;
  margin-top: 2px;
}

.submenu-item:hover {
  color: var(--primary-color);
}

.submenu-item .dot {
  margin-right: 10px;
  font-size: 6px;
  color: #cbd5e1;
  transform: translateY(-1px);
}

.active-sub {
  background-color: #e0f2fe !important;
  color: #0284c7 !important;
  font-weight: 600;
}

.active-sub .dot {
  color: #0284c7;
  transform: scale(1.5);
}

/* --- 7. HEADER --- */
.top-header {
  height: 64px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 24px;
  gap: 20px;
  position: sticky;
  top: 0;
  z-index: 5;
}
.header-actions {
  display: flex;
  gap: 15px;
}
.icon-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #64748b;
  position: relative;
  cursor: pointer;
}
.badge-count {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #ef4444;
  color: white;
  font-size: 10px;
  padding: 2px 5px;
  border-radius: 10px;
}

/* --- NOTIFICATION DROPDOWN --- */
.notification-wrapper {
  position: relative;
}
.bell-ring i {
  animation: bellShake 0.6s ease-in-out 2;
  color: #f59e0b;
}
@keyframes bellShake {
  0%, 100% { transform: rotate(0); }
  20% { transform: rotate(15deg); }
  40% { transform: rotate(-15deg); }
  60% { transform: rotate(10deg); }
  80% { transform: rotate(-10deg); }
}
.notif-dropdown {
  position: absolute;
  top: calc(100% + 10px);
  right: -40px;
  width: 380px;
  max-height: 480px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.notif-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
}
.notif-title {
  font-weight: 700;
  font-size: 15px;
  color: #1e293b;
}
.mark-all-btn {
  background: none;
  border: none;
  color: #3b82f6;
  font-size: 12px;
  cursor: pointer;
  font-weight: 600;
}
.mark-all-btn:hover {
  color: #2563eb;
  text-decoration: underline;
}
.notif-body {
  overflow-y: auto;
  flex: 1;
  max-height: 400px;
}
.notif-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.15s;
  border-bottom: 1px solid #f8fafc;
  position: relative;
}
.notif-item:hover {
  background: #f8fafc;
}
.notif-item.unread {
  background: #eff6ff;
}
.notif-item.unread:hover {
  background: #dbeafe;
}
.notif-icon-box {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
  margin-top: 2px;
}
.notif-content {
  flex: 1;
  min-width: 0;
}
.notif-item-title {
  font-weight: 600;
  font-size: 13px;
  color: #1e293b;
  margin-bottom: 2px;
}
.notif-item-desc {
  font-size: 12px;
  color: #64748b;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.notif-item-time {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 3px;
}
.notif-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #3b82f6;
  flex-shrink: 0;
  margin-top: 6px;
}
.notif-empty {
  padding: 40px 16px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
}

.user-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: 0.2s;
  position: relative;
}
.user-info:hover {
  opacity: 0.8;
}
.user-info:hover .avatar {
  background: #1e293b;
}
.user-info:hover .dropdown-arrow {
  color: #334155;
}
.user-info .avatar {
  width: 36px;
  height: 36px;
  background: #2b4360;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  transition: 0.2s;
}
.dropdown-arrow {
  font-size: 14px;
  color: #64748b;
  transition: 0.2s;
}

/* --- 8. USER DROPDOWN --- */
.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  min-width: 150px;
  margin-top: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  overflow: hidden;
}
.dropdown-item {
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: 0.2s;
  border: none;
  background: none;
  font-size: 14px;
  width: 100%;
  text-align: left;
  color: #334155;
}
.dropdown-item:hover:not(.admin-label) {
  background-color: #f1f5f9;
  color: #2b4360;
}
.admin-label {
  background-color: #f8fafc;
  color: #64748b;
  cursor: default;
  font-weight: 500;
  border-bottom: 1px solid #e2e8f0;
}
.profile-btn {
  color: #334155;
}
.profile-btn:hover {
  background-color: #eff6ff !important;
  color: #1e3a8a !important;
}
.logout-btn {
  color: #ef4444;
}
.logout-btn:hover {
  background-color: #fef2f2 !important;
  color: #dc2626 !important;
}

.content-body {
  padding: 24px;
  flex: 1;
  background-color: #ebecee;
}

/* --- 9. SHIFT NOTICE STYLES --- */
.shift-notice {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background-color: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 8px;
  margin-top: 10px;
  color: #856404;
  font-size: 14px;
  font-weight: 500;
}

.shift-notice .icon {
  font-size: 16px;
  margin-right: 8px;
}

.shift-link {
  color: #0284c7;
  text-decoration: none;
  font-weight: 700;
  padding: 4px 8px;
  border-radius: 4px;
  transition: 0.2s;
}

.shift-link:hover {
  background-color: #e0f2fe;
  color: #0284c7;
}
</style>