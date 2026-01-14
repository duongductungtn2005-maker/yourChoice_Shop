<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="logo-circle">
            <img src="https://via.placeholder.com/40" alt="Logo" />
        </div>
        <span class="brand-name">YourChoice</span>
      </div>

      <nav class="menu">
        <div class="menu-item">
          <span class="icon">📊</span> Thống kê
        </div>
        <div class="menu-item">
            <span class="icon">🏪</span> Bán hàng tại quầy
        </div>
        <div class="menu-item">
            <span class="icon">📄</span> Quản lý đơn hàng
        </div>

        <div class="menu-group expanded">
          <div class="menu-item parent">
            <span class="icon">👕</span> Quản lý sản phẩm <span class="arrow">▼</span>
          </div>
          <div class="submenu">
            <router-link to="/admin/products" class="submenu-item active">
                <span class="dot">•</span> Sản phẩm
            </router-link>
            <router-link to="/admin/co-ao" class="submenu-item">
                <span class="dot">•</span> Cổ áo
            </router-link>
            <router-link to="/admin/tay-ao" class="submenu-item">
                <span class="dot">•</span> Tay áo
            </router-link>
            <router-link to="/admin/xuat-xu" class="submenu-item">
                <span class="dot">•</span> Xuất xứ
            </router-link>
            <router-link to="/admin/chat-lieu" class="submenu-item">
                <span class="dot">•</span> Chất liệu
            </router-link>
            <router-link to="/admin/thuong-hieu" class="submenu-item">
                <span class="dot">•</span> Thương hiệu
            </router-link>
          </div>
        </div>

        <div class="menu-item">
            <span class="icon">🎟️</span> Giảm giá
        </div>
        <div class="menu-item">
            <span class="icon">👤</span> Tài khoản
        </div>
      </nav>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <div class="user-info">
            <div class="avatar">Admin</div>
        </div>
      </header>

      <div class="content-body">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
// Logic toggle menu có thể thêm sau
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}

/* SIDEBAR STYLES */
.sidebar {
  width: 260px;
  background-color: #fff;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #f1f5f9;
  font-weight: bold;
  font-size: 18px;
  color: #0f172a;
}
.logo-circle img {
    border-radius: 50%;
    margin-right: 10px;
}

.menu {
  padding: 20px 0;
}

.menu-item {
  padding: 12px 20px;
  display: flex;
  align-items: center;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}

.menu-item:hover {
  background-color: #f8fafc;
  color: #0f172a;
}

.menu-item .icon {
  margin-right: 12px;
  width: 20px;
  text-align: center;
}

/* Active Menu Group Logic */
.menu-group.expanded .submenu {
  display: block;
}

.menu-item.parent {
    justify-content: space-between;
    color: #0f172a; /* Darker for expanded */
}
.menu-item.parent .arrow {
    font-size: 10px;
    color: #94a3b8;
}

.submenu {
  background-color: #f8fafc; /* Lighter bg for submenu */
}

.submenu-item {
  display: block;
  padding: 10px 20px 10px 52px; /* Indent */
  text-decoration: none;
  color: #64748b;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.submenu-item:hover {
    color: #0f172a;
}

.submenu-item .dot {
    margin-right: 8px;
    font-size: 20px;
    line-height: 0;
    color: #cbd5e1;
}

/* ACTIVE STATE (Giống ảnh: Nền xanh than, chữ trắng) */
.submenu-item.active {
  background-color: #1e293b;
  color: #fff;
}
.submenu-item.active .dot {
    color: #fff;
}

/* MAIN CONTENT STYLES */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f1f5f9;
}

.top-header {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 24px;
}

.content-body {
  padding: 24px;
  flex: 1;
}
</style>