<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="logo-circle">
          <img src="./logo.png" alt="Logo" />
        </div>
        <span class="brand-name">YourChoice</span>
      </div>

      <nav class="menu">
        <router-link to="/admin/dashboard" class="menu-item" active-class="active-link">
          <div class="icon-wrap">
            <font-awesome-icon :icon="['fas', 'gauge']" />
          </div>
          <span class="menu-text">Thống kê</span>
        </router-link>

        <router-link to="/admin/pos" class="menu-item" active-class="active-link">
          <div class="icon-wrap">
            <font-awesome-icon :icon="['fas', 'shop']" />
          </div>
          <span class="menu-text">Bán hàng tại quầy</span>
        </router-link>

        <router-link to="/admin/orders" class="menu-item" active-class="active-link">
          <div class="icon-wrap">
            <font-awesome-icon :icon="['fas', 'file-lines']" />
          </div>
          <span class="menu-text">Quản lý đơn hàng</span>
        </router-link>

        <div class="menu-group" :class="{ expanded: openMenus.products }">
          <div class="menu-item parent" @click="toggleMenu('products')">
            <div class="menu-label">
              <div class="icon-wrap">
                <font-awesome-icon :icon="['fas', 'shirt']" />
              </div>
              <span class="menu-text">Quản lý sản phẩm</span>
            </div>
            <span class="arrow">
               <font-awesome-icon :icon="['fas', openMenus.products ? 'chevron-up' : 'chevron-down']" size="xs"/>
            </span>
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

        <div class="menu-group" :class="{ expanded: openMenus.discounts }">
            <div class="menu-item parent" @click="toggleMenu('discounts')">
                <div class="menu-label">
                    <div class="icon-wrap">
                      <font-awesome-icon :icon="['fas', 'ticket']" />
                    </div>
                    <span class="menu-text">Giảm giá</span>
                </div>
                <span class="arrow">
                  <font-awesome-icon :icon="['fas', openMenus.discounts ? 'chevron-up' : 'chevron-down']" size="xs"/>
                </span>
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

        <router-link to="/admin/customers" class="menu-item" active-class="active-link">
            <div class="icon-wrap">
              <font-awesome-icon :icon="['fas', 'user']" />
            </div>
            <span class="menu-text">Khách hàng</span>
        </router-link>

        <router-link to="/admin/employees" class="menu-item" active-class="active-link">
            <div class="icon-wrap">
              <font-awesome-icon :icon="['fas', 'user-tie']" />
            </div>
            <span class="menu-text">Nhân viên</span>
        </router-link>
      </nav>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <div class="header-actions">
            <button class="icon-btn" title="Lịch">
                <font-awesome-icon :icon="['far', 'calendar']" />
            </button>
            <button class="icon-btn" title="Thông báo">
                <font-awesome-icon :icon="['far', 'bell']" />
                <span class="badge">3</span>
            </button>
        </div>

        <div class="user-info">
            <div class="avatar-circle">
              <font-awesome-icon :icon="['fas', 'user']" />
            </div>
            <span class="username">Admin</span>
        </div>
      </header>

      <div class="content-body">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
// Import FontAwesome (Đảm bảo bạn đã cài đặt và cấu hình trong main.js)
// npm install --save @fortawesome/fontawesome-svg-core @fortawesome/free-solid-svg-icons @fortawesome/free-regular-svg-icons @fortawesome/vue-fontawesome

// State đóng mở menu
const openMenus = ref({
    products: true, 
    discounts: false 
});

const toggleMenu = (key) => {
    openMenus.value[key] = !openMenus.value[key];
};
</script>

<style scoped>
/* --- FONT & GLOBAL --- */
.admin-layout {
  display: flex;
  min-height: 100vh;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f1f5f9;
}

/* --- SIDEBAR --- */
.sidebar {
  width: 260px;
  background-color: #fff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  position: fixed;
  height: 100vh;
  overflow-y: auto;
  z-index: 20;
}

/* LOGO SECTION */
.brand {
  height: 80px; /* Chiều cao header logo */
  display: flex;
  align-items: center;
  padding: 0 24px;
  border-bottom: 1px solid #f1f5f9;
}

.logo-circle {
    width: 48px;
    height: 48px;
    margin-right: 12px;
    border-radius: 12px; /* Bo góc nhẹ */
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f8fafc;
}

.logo-circle img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.brand-name {
    font-size: 24px; /* H2 Size */
    font-weight: 700; /* In đậm */
    color: #0f172a;
    letter-spacing: -0.5px;
}

/* MENU LIST */
.menu {
  padding: 20px 10px;
}

/* MENU ITEM CHUNG */
.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 4px;
  border-radius: 8px;
  color: #64748b; /* Màu chữ thường */
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.menu-item:hover {
  background-color: #f1f5f9;
  color: #0f172a;
}

/* ACTIVE STATE (Menu cha) */
.active-link {
  background-color: #0f172a !important; /* Xanh đen đậm */
  color: #ffffff !important;
  font-weight: 600;
}

/* ICON */
.icon-wrap {
  width: 24px;
  display: flex;
  justify-content: center;
  margin-right: 12px;
  font-size: 18px; /* Kích thước icon */
}

.menu-text {
  font-size: 16px; /* Body text size */
  font-weight: 500;
}

/* PARENT MENU (Có mũi tên) */
.menu-item.parent {
    justify-content: space-between;
}
.menu-label {
    display: flex;
    align-items: center;
}
.arrow {
    font-size: 12px;
    color: #94a3b8;
    transition: transform 0.3s;
}

/* SUBMENU */
.submenu {
  margin-top: 2px;
  padding-bottom: 5px;
}

.submenu-item {
  display: flex;
  align-items: center;
  padding: 10px 16px 10px 52px; /* Thụt đầu dòng */
  text-decoration: none;
  color: #64748b;
  font-size: 14px; /* Small text size */
  border-radius: 8px;
  margin-bottom: 2px;
  transition: 0.2s;
}

.submenu-item:hover {
    color: #0f172a;
    background-color: #f8fafc;
}

.submenu-item .dot {
    margin-right: 8px;
    font-size: 18px;
    line-height: 0;
    color: #cbd5e1;
}

/* ACTIVE STATE (Submenu con) */
.active-sub {
  background-color: #e2e8f0;
  color: #0f172a;
  font-weight: 600;
}
.active-sub .dot {
    color: #0f172a;
}

/* --- MAIN CONTENT --- */
.main-content {
  flex: 1;
  margin-left: 260px; /* Bằng width sidebar */
  display: flex;
  flex-direction: column;
}

/* HEADER */
.top-header {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 32px;
  position: sticky;
  top: 0;
  z-index: 10;
  gap: 24px;
}

.header-actions {
    display: flex;
    gap: 16px;
}

.icon-btn {
    background: transparent;
    border: none;
    font-size: 20px;
    color: #64748b;
    cursor: pointer;
    position: relative;
    padding: 4px;
    transition: 0.2s;
}
.icon-btn:hover { color: #0f172a; }

.badge {
    position: absolute;
    top: -2px;
    right: -4px;
    background-color: #ef4444;
    color: white;
    font-size: 10px;
    font-weight: bold;
    padding: 2px 5px;
    border-radius: 99px;
    border: 2px solid #fff;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
}

.avatar-circle {
    width: 36px;
    height: 36px;
    background-color: #e2e8f0;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #64748b;
}

.username {
    font-weight: 600;
    color: #0f172a;
    font-size: 14px;
}

.content-body {
  padding: 24px;
  flex: 1;
}
</style>