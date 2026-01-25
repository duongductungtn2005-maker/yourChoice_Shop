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
        <router-link to="/admin/dashboard" class="menu-item" active-class="active-link">
          <i class="fa-solid fa-gauge icon"></i> Thống kê
        </router-link>

        <router-link to="/admin/pos" class="menu-item" active-class="active-link">
            <i class="fa-solid fa-shop icon"></i> Bán hàng tại quầy
        </router-link>

        <router-link to="/admin/orders" class="menu-item" active-class="active-link">
            <i class="fa-solid fa-file-lines icon"></i> Quản lý đơn hàng
        </router-link>

        <div class="menu-group">
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

        <div class="menu-group">
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

        <router-link to="/admin/customers" class="menu-item" active-class="active-link">
            <i class="fa-solid fa-user icon"></i> Khách hàng
        </router-link>
        
        <router-link to="/admin/employees" class="menu-item" active-class="active-link">
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
                <span class="badge-count">3</span>
            </button>
        </div>

        <div class="user-info">
            <div class="avatar">
                 <i class="fa-solid fa-user"></i>
            </div>
            <span class="username" style="margin-left: 8px; font-weight: 600;">Admin</span>
        </div>
      </header>

      <div class="content-body">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

// 1. Trạng thái đóng/mở menu
const openMenus = ref({
    products: false, 
    discounts: false 
});

const toggleMenu = (key) => {
    openMenus.value[key] = !openMenus.value[key];
};

// 2. Computed Properties: Tự động active menu cha khi vào trang con
const isProductRoute = computed(() => {
    const p = route.path;
    return p.includes('/admin/products') || 
           p.includes('/admin/co-ao') || 
           p.includes('/admin/tay-ao') ||
           p.includes('/admin/xuat-xu') ||
           p.includes('/admin/chat-lieu') ||
           p.includes('/admin/thuong-hieu') ||
           p.includes('/admin/mau-sac') ||
           p.includes('/admin/kich-thuoc');
});

const isDiscountRoute = computed(() => {
    const p = route.path;
    return p.includes('/admin/vouchers') || 
           p.includes('/admin/sales');
});

// Xử lý ảnh lỗi nếu logo không load được
const handleImageError = (e) => {
    // e.target.style.display = 'none'; // Có thể ẩn hoặc thay bằng ảnh default
    e.target.src = 'https://via.placeholder.com/40';
};
</script>

<style scoped>
/* --- 1. BIẾN MÀU SẮC --- */
:root {
  --primary-color: #2b4360; /* Xanh than đậm */
  --text-gray: #64748b;     /* Màu chữ thường */
  --bg-hover: #f1f5f9;      /* Màu nền khi hover */
  --active-bg: #e2e8f0;     /* Màu nền active menu con */
}

/* --- 2. LAYOUT CHUNG --- */
.admin-layout { display: flex; min-height: 100vh; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
.sidebar { width: 260px; background: white; border-right: 1px solid #e2e8f0; position: fixed; height: 100vh; overflow-y: auto; z-index: 10; display: flex; flex-direction: column; }
.main-content { flex: 1; margin-left: 260px; display: flex; flex-direction: column; background: #f8f9fa; }

/* --- 3. BRAND / LOGO --- */
.brand { height: 80px; display: flex; align-items: center; padding: 0 20px; border-bottom: 1px solid #f1f5f9; }
.logo-circle { width: 45px; height: 45px; border-radius: 12px; overflow: hidden; display: flex; align-items: center; justify-content: center; margin-right: 12px; background: #f8fafc; }
.logo-circle img { width: 100%; height: 100%; object-fit: cover; }
.brand-name { font-weight: 700; font-size: 20px; color: #2b4360; }

/* --- 4. MENU STYLES --- */
.menu { padding: 15px 0; }

/* MENU ITEM CHUNG */
.menu-item {
  padding: 12px 24px;
  display: flex;
  align-items: center;
  color: #64748b; /* Màu nhạt mặc định */
  cursor: pointer;
  text-decoration: none;
  font-weight: 500;
  font-size: 15px;
  transition: all 0.2s;
  border-right: 3px solid transparent; /* Viền ẩn bên phải */
}

.menu-item .icon {
  width: 24px;
  text-align: center;
  margin-right: 12px;
  font-size: 18px;
  transition: color 0.2s;
}

.menu-item:hover {
  background-color: #f8fafc;
  color: #2b4360;
}

/* Trạng thái Active cho Menu Đơn (Dashboard, POS...) */
.active-link {
  background-color: #f1f5f9;
  color: #2b4360 !important; /* Xanh than đậm */
  font-weight: 700;
  border-right-color: #2b4360; /* Hiện viền phải */
}

/* --- 5. PARENT MENU STYLES --- */
.menu-item.parent {
  justify-content: space-between;
}
.arrow { font-size: 10px; color: #94a3b8; }

/* Trạng thái Active cho Menu Cha (Khi mở hoặc khi con active) */
.active-parent {
  color: #2b4360 !important; /* Đậm màu chữ */
  font-weight: 700;
  background-color: #f8fafc; /* Nền sáng nhẹ */
}
.active-parent .icon {
  color: #2b4360; /* Đậm màu icon */
}

/* --- 6. SUBMENU STYLES --- */
.submenu {
  background-color: #f8fafc;
  overflow: hidden;
}

.submenu-item {
  display: flex;
  align-items: center;
  padding: 10px 20px 10px 56px; /* Thụt đầu dòng */
  text-decoration: none;
  color: #64748b;
  font-size: 14px;
  transition: 0.2s;
}

.submenu-item:hover {
  color: #2b4360;
}

.submenu-item .dot {
  margin-right: 8px;
  font-size: 18px;
  line-height: 0;
  color: #cbd5e1;
}

/* Active Submenu */
.active-sub {
  color: #2b4360;
  font-weight: 600;
  background-color: #e2e8f0;
}
.active-sub .dot {
  color: #2b4360;
}

/* --- 7. HEADER --- */
.top-header { height: 64px; background: white; border-bottom: 1px solid #e2e8f0; display: flex; align-items: center; justify-content: flex-end; padding: 0 24px; gap: 20px; position: sticky; top: 0; z-index: 5; }
.header-actions { display: flex; gap: 15px; }
.icon-btn { background: none; border: none; font-size: 20px; color: #64748b; position: relative; cursor: pointer; }
.badge-count { position: absolute; top: -5px; right: -5px; background: #ef4444; color: white; font-size: 10px; padding: 2px 5px; border-radius: 10px; }
.user-info .avatar { width: 36px; height: 36px; background: #2b4360; color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold; }

.content-body { padding: 24px; flex: 1; }
</style>