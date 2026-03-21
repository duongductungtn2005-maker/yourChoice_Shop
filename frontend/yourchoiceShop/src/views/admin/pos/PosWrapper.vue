<template>
  
  <div class="pos-wrapper">
    
    <div v-if="isLoading" class="loading-box">
      <h3><i class="fas fa-spinner fa-spin"></i> Đang kiểm tra trạng thái ca làm việc...</h3>
    </div>

    <div v-else-if="!hasActiveShift" class="lock-overlay">
      <div class="lock-box">
        
        <div class="icon-wrapper">
          <i class="fas fa-lock"></i>
        </div>
        
        <template v-if="todaySchedule">
          <h2>Chưa mở ca làm việc!</h2>
          <p>Hôm nay bạn có lịch làm việc nhưng chưa mở ca. Vui lòng quay lại trang Trực ca để mở ca và bắt đầu bán hàng.</p>
          <router-link :to="{ name: 'staff-shift-tracking' }" class="btn-unlock">
            <i class="fas fa-key"></i> ĐI ĐẾN TRANG MỞ CA
          </router-link>
        </template>

        <template v-else>
          <h2>Không có quyền truy cập!</h2>
          <p class="error-text">Hôm nay bạn không có lịch làm việc được phân công.</p>
          <p>Bạn không thể sử dụng chức năng bán hàng hoặc quản lý trong lúc hệ thống đang đóng ca của bạn.</p>
          <router-link :to="{ name: 'staff-shift-tracking' }" class="btn-unlock btn-disabled">
            <i class="fas fa-info-circle"></i> XEM LỊCH CỦA TÔI
          </router-link>
        </template>
      </div>
    </div>

    <BanHangTaiQuay v-else />

  </div>
</template>
<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2'; 
import request from '@/services/request'; 
import { isAuthenticated, getCurrentUser, getCurrentUserName } from '@/services/auth';
import BanHangTaiQuay from './BanHangTaiQuay.vue'; 

const router = useRouter(); 
const isLoading = ref(true);
const hasActiveShift = ref(false); 
const todaySchedule = ref(null); 
let posInterval = null; 

const checkShiftStatus = async (isBackground = false) => {
  const user = getCurrentUser(); 

  // 1. Kiểm tra đăng nhập trước tiên
  if (!isAuthenticated() || !user) {
    if (!isBackground) router.push('/login');
    return;
  }

  // 2. Bật Loading
  if (!isBackground) isLoading.value = true;

  try {
    // 3. ÁP DỤNG PRO TIP Ở ĐÂY LÀ CHUẨN BÀI NHẤT:
    // Ưu tiên lấy id (86) hoặc mã nhân viên/tài khoản viết liền, TUYỆT ĐỐI không lấy tên có dấu!
const usernameQuery = user.tenTaiKhoan || user.maNhanVien || user.username || user.id;
    
    // 4. Gọi API
    const response = await request.get(`/giao-ca/hien-tai?username=${usernameQuery}`);
    
    if (response.data && response.data.id) {
      hasActiveShift.value = true; 
      sessionStorage.setItem('hasActiveShift', 'true');
    } else {
      hasActiveShift.value = false;
      sessionStorage.setItem('hasActiveShift', 'false');

      const scheduleRes = await request.get(`/lich-lam-viec/hom-nay?username=${usernameQuery}`);
      if (scheduleRes.data) {
        todaySchedule.value = scheduleRes.data; 
      } else {
        todaySchedule.value = null; 
      }

      if (isBackground && !hasActiveShift.value) { 
        Swal.fire({
          icon: 'error',
          title: 'Ca làm việc đã bị đóng!',
          text: 'Quản lý đã kết thúc ca này. Màn hình bán hàng sẽ bị khóa.',
          confirmButtonColor: '#d33',
          allowOutsideClick: false 
        }).then(() => {
          router.push('/staff/giao-ca'); 
        });
      }
    }
  } catch (error) {
    console.error("Lỗi khi gọi API kiểm tra ca:", error);
    hasActiveShift.value = false;
  } finally {
    // 5. Luôn luôn tắt Loading
    if (!isBackground) isLoading.value = false;
  }
};
const deleteStaff = async (staffId) => {
  const result = await Swal.fire({
    title: 'Bạn có chắc chắn?',
    text: "Nhân viên này sẽ bị xóa. Nếu đang trong ca, ca làm việc sẽ tự động bị đóng!",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    confirmButtonText: 'Xóa ngay',
    cancelButtonText: 'Hủy'
  });

  if (result.isConfirmed) {
    try {
      // Gọi API xóa (Backend xử lý: nếu có ca active -> close ca -> delete staff)
      await request.delete(`/nhan-vien/${staffId}`);
      
      Swal.fire(
        'Đã xóa!',
        'Nhân viên đã được xóa và ca làm việc (nếu có) đã được đóng.',
        'success'
      );
      // Refresh danh sách nhân viên
      loadStaffList();
    } catch (error) {
      Swal.fire('Lỗi!', 'Có lỗi xảy ra khi xóa nhân viên.', 'error');
    }
  }
};

const handleCloseShift = async (shiftData) => {
  const now = new Date();
  const endTime = new Date(shiftData.thoiGianKetThucDuKien);

  // VALIDATE: Nếu chưa đến giờ kết ca
  if (now < endTime) {
    Swal.fire({
      icon: 'error',
      title: 'Chưa được đóng ca!',
      text: `Ca làm việc của bạn kết thúc vào lúc ${endTime.toLocaleTimeString()}. Vui lòng quay lại sau!`,
    });
    return;
  }

  // Nếu hợp lệ, tiến hành đóng ca
  const { isConfirmed } = await Swal.fire({
    title: 'Xác nhận đóng ca?',
    text: "Bạn sẽ không thể bán hàng sau khi đóng ca!",
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Đồng ý',
    cancelButtonText: 'Hủy'
  });

  if (isConfirmed) {
    try {
      await request.post('/giao-ca/ket-thuc', { id: shiftData.id });
      sessionStorage.setItem('hasActiveShift', 'false');
      Swal.fire('Thành công!', 'Ca làm việc đã được đóng.', 'success');
      router.push('/staff/giao-ca');
    } catch (error) {
      Swal.fire('Lỗi!', 'Không thể đóng ca, vui lòng thử lại.', 'error');
    }
  }
};

onMounted(() => {
  checkShiftStatus(false); 
  // Lặp lại việc check mỗi 10 giây chạy ngầm
  posInterval = setInterval(() => {
    checkShiftStatus(true); 
  }, 10000);
});

onUnmounted(() => {
  if (posInterval) clearInterval(posInterval);
});
</script>
<style scoped>
/* Định dạng khung bao ngoài */
.pos-wrapper {
  position: relative; 
  min-height: 80vh; 
  background-color: #f4f6f8;
}

/* Định dạng màn hình Loading */
.loading-box {
  padding: 50px; 
  text-align: center;
  color: #666;
}

/* --- ĐỊNH DẠNG MÀN HÌNH KHÓA --- */
.lock-overlay {
  position: absolute; 
  top: 0; 
  left: 0; 
  width: 100%; 
  height: 100%; 
  background: rgba(0, 0, 0, 0.6); 
  display: flex; 
  justify-content: center; 
  align-items: center; 
  z-index: 9999;
}

.lock-box {
  background: white; 
  padding: 40px; 
  border-radius: 12px; 
  text-align: center; 
  max-width: 500px; 
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
}

.icon-wrapper {
  font-size: 50px; 
  color: #dc3545; 
  margin-bottom: 20px;
}

.lock-box h2 {
  margin-bottom: 15px; 
  color: #333;
}

.lock-box p {
  color: #666; 
  margin-bottom: 25px; 
  line-height: 1.5;
}

.lock-box p.error-text {
  color: #dc3545; 
  font-weight: bold; 
  margin-bottom: 10px;
}

/* Nút bấm */
.btn-unlock {
  display: inline-block; 
  padding: 12px 24px; 
  background-color: #0d6efd; 
  color: white; 
  text-decoration: none; 
  border-radius: 6px; 
  font-weight: bold; 
  transition: background-color 0.3s;
}

.btn-unlock:hover {
  background-color: #0b5ed7;
  color: white;
}

.btn-disabled {
  background-color: #6c757d;
}

.btn-disabled:hover {
  background-color: #5c636a;
}
</style>