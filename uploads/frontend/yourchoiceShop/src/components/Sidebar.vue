<!-- <template>
  <aside class="sidebar">
    <div class="logo-box">
      <div class="logo-img">
        <img src="https://cdn-icons-png.flaticon.com/512/3135/3135715.png" alt="Logo" />
      </div>
      <h2 class="brand-name">YourChoice</h2>
    </div>

    <nav class="menu-list">
      
      <router-link to="/admin/dashboard" class="menu-item">
        <span class="icon">📊</span>
        <span class="text">Thống kê</span>
      </router-link>

      <router-link to="/admin/pos" class="menu-item">
        <span class="icon">🏪</span>
        <span class="text">Bán hàng tại quầy</span>
      </router-link>

      <router-link to="/admin/orders" class="menu-item">
        <span class="icon">📄</span>
        <span class="text">Quản lý đơn hàng</span>
      </router-link>

      <div class="menu-group">
        <div 
          class="menu-item parent-item" 
          @click="toggleMenu('product')" 
          :class="{ 'active-parent': isProductMenuOpen }"
        >
          <span class="icon">👕</span>
          <span class="text">Quản lý sản phẩm</span>
          <span class="arrow" :class="{ rotated: isProductMenuOpen }">▼</span>
        </div>

        <div class="submenu" v-show="isProductMenuOpen">
          <router-link to="/admin/products" class="submenu-item">
            <span class="dot">•</span> Sản phẩm
          </router-link>
          <router-link to="/admin/products/variants" class="submenu-item">
            <span class="dot">•</span> Toàn bộ biến thể
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

      <router-link to="/admin/vouchers" class="menu-item">
        <span class="icon">🎟️</span>
        <span class="text">Giảm giá</span>
      </router-link>

      <router-link to="/admin/accounts" class="menu-item">
        <span class="icon">👤</span>
        <span class="text">Tài khoản</span>
      </router-link>

    </nav>
  </aside>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const isProductMenuOpen = ref(false);

const toggleMenu = (menu) => {
  if (menu === 'product') isProductMenuOpen.value = !isProductMenuOpen.value;
};

// Hàm kiểm tra đường dẫn hiện tại để tự động mở menu cha
const checkActiveRoute = () => {
  const path = route.path;
  // Danh sách các link con thuộc nhóm Sản phẩm
  const productLinks = [
    '/admin/products', 
    '/admin/co-ao', 
    '/admin/tay-ao', 
    '/admin/xuat-xu', 
    '/admin/chat-lieu', 
    '/admin/thuong-hieu'
  ];

  // Nếu đường dẫn hiện tại khớp với bất kỳ link con nào -> Mở menu cha
  if (productLinks.some(link => path.startsWith(link))) {
    isProductMenuOpen.value = true;
  }
};

// Theo dõi sự thay đổi URL để cập nhật trạng thái menu
watch(route, () => {
  checkActiveRoute();
});

onMounted(() => {
  checkActiveRoute();
});
</script>

<style scoped>
/* SIDEBAR CONTAINER */
.sidebar {
  width: 250px;
  background-color: #fff;
  height: 100vh;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  position: fixed; /* Cố định bên trái */
  left: 0; top: 0;
  z-index: 1000;
}

/* LOGO */
.logo-box {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #f1f5f9;
}
.logo-img img { width: 40px; height: 40px; }
.brand-name { font-size: 20px; font-weight: 700; color: #0f172a; margin: 0; }

/* MENU LIST */
.menu-list { padding: 15px 10px; overflow-y: auto; }

/* MENU ITEM (Chung) */
.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 15px;
  border-radius: 8px;
  color: #475569;
  text-decoration: none;
  transition: all 0.2s;
  cursor: pointer;
  margin-bottom: 5px;
  font-weight: 500;
}
.menu-item:hover { background-color: #f1f5f9; color: #0f172a; }

.icon { margin-right: 12px; font-size: 18px; width: 24px; text-align: center; }
.arrow { margin-left: auto; font-size: 12px; transition: transform 0.3s; }
.arrow.rotated { transform: rotate(180deg); }

/* --- ACTIVE STATE (Khi router trùng khớp link) --- */
/* Vue Router tự động thêm class 'router-link-active' vào thẻ active */
.menu-item.router-link-active {
  background-color: #0f172a; /* Màu nền xanh đậm như ảnh mẫu */
  color: #fff; /* Chữ trắng */
}

/* SUBMENU */
.submenu {
  padding-left: 10px;
  overflow: hidden;
  animation: slideDown 0.3s ease-in-out;
}
@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.submenu-item {
  display: flex;
  align-items: center;
  padding: 10px 15px 10px 45px; /* Thụt đầu dòng */
  color: #64748b;
  text-decoration: none;
  font-size: 14px;
  border-radius: 8px;
  margin-bottom: 2px;
  transition: 0.2s;
}
.submenu-item:hover { color: #0f172a; background-color: #f8fafc; }
.submenu-item .dot { margin-right: 8px; font-size: 20px; line-height: 0; }

/* ACTIVE SUBMENU ITEM */
.submenu-item.router-link-active {
  color: #0f172a;
  font-weight: 700;
  background-color: #e2e8f0; /* Màu nền nhạt cho submenu active */
}
</style> -->