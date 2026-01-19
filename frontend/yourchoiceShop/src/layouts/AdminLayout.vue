<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="brand">
        <div class="logo-circle">
            <img src="C:\Users\quanm\OneDrive\Tài liệu\GitHub\yourChoice_Shop\frontend\yourchoiceShop\src\img\logo1.png" alt="Logo" />
        </div>
        <span class="brand-name">YourChoice</span>
      </div>

      <nav class="menu">
        <router-link to="/admin/dashboard" class="menu-item">
          <i class="fa-solid fa-gauge icon"></i> Thống kê
        </router-link>

        <router-link to="/admin/pos" class="menu-item">
            <i class="fa-solid fa-shop icon"></i> Bán hàng tại quầy
        </router-link>

        <router-link to="/admin/orders" class="menu-item">
            <i class="fa-solid fa-file-lines icon"></i> Quản lý đơn hàng
        </router-link>

        <div class="menu-group" :class="{ expanded: openMenus.products }">
          <div class="menu-item parent" @click="toggleMenu('products')">
            <span>
                <i class="fa-solid fa-shirt icon"></i> Quản lý sản phẩm
            </span>
            <span class="arrow">{{ openMenus.products ? '▲' : '▼' }}</span>
          </div>
          
          <div class="submenu" v-show="openMenus.products">
            <router-link to="/admin/products" class="submenu-item" active-class="active">
                <span class="dot">•</span> Sản phẩm
            </router-link>
            <router-link to="/admin/co-ao" class="submenu-item" active-class="active">
                <span class="dot">•</span> Cổ áo
            </router-link>
            <router-link to="/admin/tay-ao" class="submenu-item" active-class="active">
                <span class="dot">•</span> Tay áo
            </router-link>
            <router-link to="/admin/xuat-xu" class="submenu-item" active-class="active">
                <span class="dot">•</span> Xuất xứ
            </router-link>
            <router-link to="/admin/chat-lieu" class="submenu-item" active-class="active">
                <span class="dot">•</span> Chất liệu
            </router-link>
            <router-link to="/admin/thuong-hieu" class="submenu-item" active-class="active">
                <span class="dot">•</span> Thương hiệu
            </router-link>
             <router-link to="/admin/mau-sac" class="submenu-item" active-class="active">
                <span class="dot">•</span> Màu sắc
            </router-link>
             <router-link to="/admin/kich-thuoc" class="submenu-item" active-class="active">
                <span class="dot">•</span> Kích thước
            </router-link>
          </div>
        </div>

        <div class="menu-group" :class="{ expanded: openMenus.discounts }">
            <div class="menu-item parent" @click="toggleMenu('discounts')">
                <span>
                    <i class="fa-solid fa-ticket icon"></i> Giảm giá
                </span>
                <span class="arrow">{{ openMenus.discounts ? '▲' : '▼' }}</span>
</div>
            
            <div class="submenu" v-show="openMenus.discounts">
                <router-link to="/admin/vouchers" class="submenu-item" active-class="active">
                    <span class="dot">•</span> Phiếu giảm giá
                </router-link>
                <router-link to="/admin/sales" class="submenu-item" active-class="active">
                    <span class="dot">•</span> Đợt giảm giá
                </router-link>
            </div>
        </div>

        <router-link to="/admin/customers" class="menu-item">
            <i class="fa-solid fa-user icon"></i> Khách hàng
        </router-link>

        <router-link to="/admin/employees" class="menu-item">
            <i class="fa-solid fa-user-tie icon"></i> Nhân viên
        </router-link>
      </nav>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <div class="header-actions">
            <button class="icon-btn">
                <i class="fa-regular fa-calendar"></i>
            </button>
            <button class="icon-btn">
                <i class="fa-regular fa-bell"></i>
                <span class="badge">3</span>
            </button>
        </div>

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
import { ref } from 'vue';

// State quản lý việc đóng mở các menu cha
const openMenus = ref({
    products: true, 
    discounts: false 
});

const toggleMenu = (key) => {
    openMenus.value[key] = !openMenus.value[key];
};
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}
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
  z-index: 10;
}

.brand {
  /* Tăng chiều cao header để chứa logo lớn hơn */
  height: 100px; 
  display: flex;
  align-items: center;
  padding: 0 20px;
  border-bottom: 1px solid #f1f5f9;
  font-weight: bold;
  font-size: 20px; /* Tăng nhẹ cỡ chữ tên thương hiệu */
  color: #0f172a;
}

/* CẬP NHẬT: Style mới cho Logo to hơn và bo góc nhẹ */
.logo-circle { /* Bạn có thể đổi tên class này thành .logo-container cho hợp lý hơn */
    /* Tăng kích thước khung chứa lên đáng kể */
    width: 60px;
    height: 60px;
    margin-right: 15px; /* Tăng khoảng cách với tên thương hiệu */
    
    /* Thay đổi bo góc: 12px để tạo hình vuông bo góc nhẹ như mẫu */
    border-radius: 50px; 
    
    overflow: hidden;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: #f8fafc;
    /* border: 1px solid #e2e8f0; Bỏ viền nếu muốn giống hệt mẫu */
}

.logo-circle img {
    /* width: 100%; */
    height: 100%;
object-fit: cover; /* Ảnh sẽ lấp đầy khung mà không bị méo */
}

.brand-name {
    /* Thêm style cho tên thương hiệu nếu cần */
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; /* Ví dụ font chữ */
}

.menu {
  padding: 20px 0;
}

/* Menu Item Styles */
.menu-item {
  padding: 12px 20px;
  display: flex;
  align-items: center;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
  text-decoration: none;
}

.menu-item:hover, .menu-item.router-link-active {
  background-color: #f8fafc;
  color: #0f172a;
}

/* Style cho Icon FontAwesome */
.menu-item .icon {
  margin-right: 12px;
  width: 24px; /* Tăng nhẹ width để icon cân đối hơn */
  text-align: center;
  font-size: 18px; /* Kích thước icon */
}

/* Parent Menu Styles */
.menu-item.parent {
    justify-content: space-between;
    color: #0f172a;
}
.menu-item.parent .arrow {
    font-size: 10px;
    color: #94a3b8;
}

/* Submenu Styles */
.submenu {
  background-color: #f8fafc;
  overflow: hidden;
  transition: max-height 0.3s ease-out;
}

.submenu-item {
  display: block;
  padding: 10px 20px 10px 52px;
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

/* ACTIVE STATE cho Submenu */
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
  margin-left: 260px;
}

.top-header {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: flex-end; /* Đẩy nội dung sang phải */
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 5;
  gap: 20px; /* Khoảng cách giữa các phần tử header */
}

/* Style mới cho các nút Header Action (Chuông, Lịch) */
.header-actions {
    display: flex;
    align-items: center;
    gap: 15px;
}

.icon-btn {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 18px;
    color: #64748b;
    position: relative;
    padding: 5px;
}

.icon-btn:hover {
    color: #0f172a;
}

.badge {
    position: absolute;
    top: -2px;
    right: -2px;
    background-color: #ef4444;
    color: white;
    font-size: 10px;
    padding: 2px 5px;
    border-radius: 10px;
    font-weight: bold;
}

.content-body {
  padding: 24px;
  flex: 1;
}
</style>