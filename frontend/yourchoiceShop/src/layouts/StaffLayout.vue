<template>
  <div class="staff-layout">
    <aside class="sidebar">
      <div class="logo">
        <h2>YourChoice<span style="color: #42b983;">Shop</span></h2>
        <p class="role-badge">Dành cho Nhân viên</p>
      </div>

      <nav class="menu">
        <router-link to="/staff/giao-ca" class="menu-item" active-class="active">
          <i class="fas fa-clock"></i> Trực ca làm việc
        </router-link>

        <template v-if="hasActiveShift">
          <router-link to="/staff/pos" class="menu-item" active-class="active">
            <i class="fas fa-desktop"></i> Bán hàng tại quầy
          </router-link>
          
          <router-link to="/staff/hoa-don" class="menu-item" active-class="active">
            <i class="fas fa-file-invoice-dollar"></i> Quản lý hóa đơn
          </router-link>
          
          <router-link to="/staff/khach-hang" class="menu-item" active-class="active">
            <i class="fas fa-users"></i> Quản lý khách hàng
          </router-link>

          <router-link to="/staff/chat" class="menu-item" active-class="active">
            <i class="fa-regular fa-comment-dots"></i> Quản lý Chat
          </router-link>
        </template>

        <div v-if="!hasActiveShift" class="shift-notice">
          <i class="fa-solid fa-circle-exclamation icon"></i>
          <span>Bạn chưa mở ca làm việc</span>
          <router-link to="/staff/giao-ca" class="shift-link">
            Mở ca ngay
          </router-link>
        </div>
      </nav>

      <div class="bottom-menu">
        <button @click="logout" class="btn-logout">
          <i class="fas fa-sign-out-alt"></i> Đăng xuất
        </button>
      </div>
    </aside>

    <main class="main-content">
      <header class="top-header">
        <div class="header-title">Hệ thống quản lý cửa hàng</div>
        <div class="user-info">
          <i class="fas fa-user-circle"></i> Xin chào, <strong>{{ userName }}</strong>
        </div>
      </header>

      <div class="content-wrapper">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
// Import thêm auth để lấy tên user thật
import { getCurrentUser } from '@/services/auth'; 

const router = useRouter();

// 1. Xử lý hiển thị tên nhân viên thật
const user = getCurrentUser() || {};
const userName = ref(user.tenNhanVien || user.tenTaiKhoan || 'Nhân viên');

// 2. Logic kiểm tra trạng thái ca làm việc
const hasActiveShift = ref(sessionStorage.getItem('hasActiveShift') === 'true');

// Lắng nghe event 'shift-changed' (bạn đã tạo ở màn hình Giao Ca trước đó)
// Khi nhân viên bấm "Xác nhận vào ca", menu sẽ tự động mở khóa các chức năng
const updateShiftStatus = () => {
  hasActiveShift.value = sessionStorage.getItem('hasActiveShift') === 'true';
};

onMounted(() => {
  window.addEventListener('shift-changed', updateShiftStatus);
});

onUnmounted(() => {
  window.removeEventListener('shift-changed', updateShiftStatus);
});

// 3. Đăng xuất
const logout = () => {
  sessionStorage.clear(); // Nên clear session để xóa trạng thái ca làm việc & token
  router.push('/login');
};
</script>

<style scoped>
/* CSS Reset cơ bản cho layout */
.staff-layout {
  display: flex;
  height: 100vh;
  background-color: #f4f6f9;
  font-family: Arial, sans-serif;
}

/* --- SIDEBAR --- */
.sidebar {
  width: 250px;
  background-color: #2c3e50;
  color: #fff;
  display: flex;
  flex-direction: column;
}
.logo { padding: 20px; text-align: center; border-bottom: 1px solid #34495e; }
.logo h2 { margin: 0; font-size: 22px; }
.role-badge { font-size: 12px; color: #aaa; margin-top: 5px; }

.menu { flex: 1; padding-top: 20px; }
.menu-item {
  display: block;
  padding: 15px 20px;
  color: #ecf0f1;
  text-decoration: none;
  font-size: 15px;
  transition: 0.3s;
}
.menu-item i { margin-right: 10px; width: 20px; text-align: center; }
.menu-item:hover, .menu-item.active {
  background-color: #42b983;
  color: #fff;
  font-weight: bold;
}

.bottom-menu { padding: 20px; border-top: 1px solid #34495e; }
.btn-logout {
  width: 100%; padding: 10px; background: #e74c3c; color: white; border: none; border-radius: 4px; cursor: pointer;
}

/* --- MAIN CONTENT --- */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.top-header {
  height: 60px; background: #fff; display: flex; align-items: center; justify-content: space-between; padding: 0 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.content-wrapper {
  flex: 1; padding: 20px; overflow-y: auto;
}
</style>