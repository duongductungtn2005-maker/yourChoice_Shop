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
      </nav>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <div class="header-actions">
          <button class="icon-btn" title="Lịch">
            <i class="fa-regular fa-calendar"></i>
          </button>
          <button class="icon-btn" title="Thông báo">
            <i class="fa-regular fa-bell"></i>
            <span class="badge-count">3</span>
          </button>
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
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { toastSuccess } from '@/utils/toast'; 
import { logout as authLogout, getRole, getCurrentUser, getCurrentUserName } from '@/services/auth';
import { useShiftStore } from '@/stores/shiftStore'

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
  if (isAdmin.value && currentUserName.value) {
    return `Admin: ${currentUserName.value}`;
  }
  if (isAdmin.value) return 'Admin';
  if (isStaff.value && currentUserName.value) {
    return `Nhân viên: ${currentUserName.value}`;
  }
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

const handleLogout = () => {
  shiftStore.clearShift();
  
  authLogout();
  isUserDropdownOpen.value = false;

  try {
    toastSuccess('Đăng xuất thành công!');
  } catch {
    // Nếu chưa cấu hình toast thì bỏ qua
  }

  router.push('/login');
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
    p.includes('/admin/products') ||
    p.includes('/admin/co-ao') ||
    p.includes('/admin/tay-ao') ||
    p.includes('/admin/xuat-xu') ||
    p.includes('/admin/chat-lieu') ||
    p.includes('/admin/thuong-hieu') ||
    p.includes('/admin/mau-sac') ||
    p.includes('/admin/kich-thuoc')
  );
});

const isDiscountRoute = computed(() => {
  const p = route.path;
  return p.includes('/admin/vouchers') || p.includes('/admin/sales');
});

const isWorkScheduleRoute = computed(() => {
  const p = route.path;
  return p.includes('/admin/shifts') || p.includes('/admin/schedules');
});

const isAccountRoute = computed(() => {
  const p = route.path;
  return (
    p.includes('/admin/customers') ||
    p.includes('/staff/customers') ||
    p.includes('/admin/employees')
  );
});

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