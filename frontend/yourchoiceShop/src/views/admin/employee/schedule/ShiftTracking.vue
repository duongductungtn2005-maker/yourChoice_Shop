<template>
  <h1 style="color: red; background: yellow; padding: 20px;">ĐANG NẰM Ở TRANG SHIFT TRACKING</h1>
  <div class="shift-tracking-container">
    <div class="user-header">
      <i class="fas fa-user-circle"></i> Xin chào, <strong>{{ employeeName }}</strong>
    </div>

    <div v-if="isLoading" class="alert-card status-no-shift">
      <p>Đang tải dữ liệu ca làm việc...</p>
    </div>

    <div v-else>
      <div v-if="activeShift" class="alert-card status-active">
        <h2 class="text-success"><i class="fas fa-check-circle"></i> Ca làm việc đang mở</h2>
        
        <div class="shift-details">
          <p><strong>Giờ vào ca:</strong> {{ formatTime(activeShift.thoiGianBatDau) || 'Đang cập nhật' }}</p>
          <p><strong>Dự kiến kết thúc:</strong> {{ formatTime(activeShift.thoiGianKetThucDuKien) }}</p>
        </div>

        <div v-if="isTimeToEndShift" class="wait-message">
          <i class="fas fa-exclamation-triangle"></i> Đã đến giờ kết thúc ca. Vui lòng hoàn tất đơn hàng và đóng ca!
        </div>

        <div class="action-buttons">
          <router-link :to="{ name: 'staff-pos' }" class="btn btn-pos">
            ĐI ĐẾN MÀN HÌNH BÁN HÀNG
          </router-link>
          <button @click="handleCloseShift" class="btn btn-close">
            KẾT THÚC CA
          </button>
        </div>
      </div>

      <div v-else-if="todaySchedule" class="alert-card status-empty">
        <h2><i class="fas fa-clock"></i> Chưa mở ca làm việc</h2>
        
        <div class="shift-details">
          <p>Bạn có lịch làm việc hôm nay:</p>
          <p><strong>Ca:</strong> {{ todaySchedule.caLamViec?.tenCa || 'Đang cập nhật' }}</p>
          <p><strong>Thời gian:</strong> 
            {{ formatTime(todaySchedule.caLamViec?.thoiGianBatDau || todaySchedule.thoiGianBatDau) }} - 
            {{ formatTime(todaySchedule.caLamViec?.thoiGianKetThuc || todaySchedule.thoiGianKetThuc) }}
          </p>
        </div>

        <button @click="handleOpenShift" class="btn btn-open">
          BẮT ĐẦU CA LÀM VIỆC
        </button>
      </div>

      <div v-else class="alert-card status-no-shift">
        <h2><i class="fas fa-calendar-times"></i> Không có lịch làm việc</h2>
        <p>Hôm nay bạn không có lịch được phân công hoặc ca làm việc đã kết thúc.</p>
      </div>
    </div>
  </div>
</template>
<script setup>

import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2'; 
import request from '@/services/request'; 
import { getCurrentUser } from '@/services/auth'; 

const router = useRouter(); 
// ĐÃ FIX: Thêm || {} để nếu user null thì gán bằng object rỗng, code không bị sập ngang
const user = getCurrentUser() || {}; 
const employeeName = user.tenNhanVien || user.username || 'Nhân viên';

const activeShift = ref(null);    
const todaySchedule = ref(null);  
const isLoading = ref(true);
const isTimeToEndShift = ref(false); 
let timeChecker = null; 

// ==========================================
// 1. LẤY DỮ LIỆU LẦN ĐẦU (CÓ LOADING)
// ==========================================
const fetchCurrentShift = async () => {
  // ĐÃ FIX: Chặn gọi API nếu không có username, đồng thời đẩy về login
  if (!user || !user.username) {
    router.push('/login');
    return;
  }

  isLoading.value = true;
  try {
    const scheduleRes = await request.get(`/lich-lam-viec/hom-nay?username=${user.username}`); 
    if (scheduleRes.data) {
      todaySchedule.value = scheduleRes.data;
    }

    const res = await request.get(`/giao-ca/hien-tai?username=${user.username}`);
    
    if (res.data && res.data.id) {
      activeShift.value = res.data;
      
      const ca = todaySchedule.value?.caLamViec || {};
      activeShift.value.thoiGianBatDauDuKien = ca.thoiGianBatDau || todaySchedule.value?.thoiGianBatDau;
      activeShift.value.thoiGianKetThucDuKien = ca.thoiGianKetThuc || todaySchedule.value?.thoiGianKetThuc;

      checkTimeToEndShift(); 
    }
  } catch (error) { 
    console.error("Lỗi:", error);
  } finally { 
    isLoading.value = false; 
  }
};

// ==========================================
// 2. ĐỒNG BỘ NGẦM (KHÔNG LOADING)
// ==========================================
const backgroundSync = async () => {
  // ĐÃ FIX: Chặn đồng bộ ngầm nếu tự nhiên mất thông tin user
  if (!user || !user.username) {
    if (timeChecker) clearInterval(timeChecker);
    return;
  }

  try {
    const res = await request.get(`/giao-ca/hien-tai?username=${user.username}`);
    
    if (res.data && res.data.id) {
      activeShift.value = res.data;
      checkTimeToEndShift();
    } else {
      if (activeShift.value) {
        Swal.fire({
          icon: 'warning',
          title: 'Ca làm việc đã kết thúc!',
          text: 'Quản lý đã xóa hoặc kết thúc ca làm việc của bạn.',
          confirmButtonColor: '#3085d6'
        });
        activeShift.value = null; 
      }

      const scheduleRes = await request.get(`/lich-lam-viec/hom-nay?username=${user.username}`);
      if (scheduleRes.data) {
        todaySchedule.value = scheduleRes.data;
      } else {
        if (todaySchedule.value) {
          Swal.fire({
            icon: 'info',
            title: 'Lịch làm việc thay đổi',
            text: 'Lịch làm việc hôm nay của bạn đã bị thay đổi hoặc hủy bỏ.',
            confirmButtonColor: '#3085d6'
          });
          todaySchedule.value = null; 
        }
      }
    }
  } catch (error) {
    console.log("Đồng bộ ngầm thất bại", error.message);
  }
};

const checkTimeToEndShift = () => {
  if (!activeShift.value || !activeShift.value.thoiGianKetThucDuKien) return;

  const now = new Date();
  
  let endTime;
  const thoiGianKetThucStr = activeShift.value.thoiGianKetThucDuKien;
  
  if (typeof thoiGianKetThucStr === 'string' && thoiGianKetThucStr.includes(':') && !thoiGianKetThucStr.includes('T')) {
    const [hours, minutes] = thoiGianKetThucStr.split(':');
    endTime = new Date();
    endTime.setHours(parseInt(hours), parseInt(minutes), 0, 0);
  } else {
    endTime = new Date(thoiGianKetThucStr);
  }
  
  activeShift.value.thoiGianKetThucDuKien = endTime;
  isTimeToEndShift.value = now >= endTime;
};

const handleOpenShift = async () => {
  try {
    const idLich = todaySchedule.value.id; 
    
    // ✅ ĐÃ FIX: Truyền thêm param username để khớp với Controller Backend
    const res = await request.post(`/giao-ca/mo-ca?username=${user.username}`, { 
        idLichLamViec: idLich 
    }); 
    
    Swal.fire({
      icon: 'success',
      title: 'Vào ca thành công!',
      text: 'Chúc bạn một ca làm việc hiệu quả.',
      timer: 2000,
      showConfirmButton: false
    });
    
    activeShift.value = res.data;
    checkTimeToEndShift(); 
    
  } catch (e) {
    Swal.fire({
      icon: 'error',
      title: 'Không thể mở ca',
      text: e.response?.data?.message || e.response?.data || "Vui lòng kiểm tra lại lịch làm việc."
    });
  }
};

const handleCloseShift = async () => {
  const result = await Swal.fire({
    title: 'Xác nhận kết thúc ca?',
    text: "Hệ thống sẽ chốt doanh thu và bạn không thể tạo đơn hàng cho ca này nữa!",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    cancelButtonColor: '#3085d6',
    confirmButtonText: 'Đóng ca ngay',
    cancelButtonText: 'Hủy'
  });

  if (result.isConfirmed) {
    try {
      await request.put(`/giao-ca/dong-ca/${activeShift.value.id}`);
      
      Swal.fire({
        icon: 'success',
        title: 'Đã đóng ca!',
        text: 'Cảm ơn bạn đã hoàn thành ca làm việc.',
        timer: 2000,
        showConfirmButton: false
      });
      
      activeShift.value = null;
      todaySchedule.value = null; 
    } catch (e) { 
      Swal.fire({
        icon: 'error',
        title: 'Lỗi đóng ca',
        text: 'Có lỗi xảy ra, vui lòng thử lại sau.'
      });
    }
  }
};

const formatTime = (dateString) => {
  if (!dateString) return '';
  const d = new Date(dateString);
  return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
};
const handleRequestCloseShift = async (shift) => {
  const now = new Date();
  const endTime = new Date(shift.thoiGianKetThucDuKien);

  // 1. Kiểm tra thời gian: Chưa tới giờ không cho đóng
  if (now < endTime) {
    Swal.fire({
      icon: 'error',
      title: 'Chưa thể đóng ca!',
      text: `Ca làm việc của bạn dự kiến kết thúc lúc ${endTime.toLocaleTimeString('vi-VN')}. Bạn phải làm đủ thời gian quy định mới được đóng ca.`,
      confirmButtonColor: '#3085d6'
    });
    return;
  }

  // 2. Nếu đã đủ giờ, yêu cầu xác nhận
  const confirm = await Swal.fire({
    title: 'Xác nhận đóng ca?',
    text: "Mọi giao dịch sau khi đóng ca sẽ không được tính vào phiên này!",
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Đóng ca ngay',
    cancelButtonText: 'Tiếp tục làm việc'
  });

  if (confirm.isConfirmed) {
    try {
      await request.post(`/giao-ca/ket-thuc/${shift.id}`);
      sessionStorage.setItem('hasActiveShift', 'false'); // Cập nhật trạng thái ngay
      Swal.fire('Thành công!', 'Ca làm việc đã được đóng.', 'success');
      window.location.reload(); // Refresh để router guard chặn truy cập lại
    } catch (error) {
      Swal.fire('Lỗi', 'Không thể đóng ca, vui lòng liên hệ Admin.', 'error');
    }
  }
};

onMounted(() => {
  fetchCurrentShift();
  
  timeChecker = setInterval(() => {
    checkTimeToEndShift();
    backgroundSync(); 
  }, 10000); 
});

onUnmounted(() => {
  if (timeChecker) clearInterval(timeChecker);
});
</script>

<style scoped>
.shift-tracking-container { max-width: 600px; margin: 0 auto; padding: 20px; }
.user-header { background: #e9ecef; padding: 15px; border-radius: 8px; margin-bottom: 20px; font-size: 16px; border-left: 5px solid #007bff;}
.alert-card { padding: 30px; border-radius: 12px; text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.05); margin-bottom: 20px;}
.status-active { border: 2px solid #28a745; background: #f8fff9; }
.status-empty { border: 2px solid #ffc107; background: #fffdf5; }
.status-no-shift { border: 2px solid #6c757d; background: #f8f9fa; color: #6c757d;}
.btn { padding: 12px 25px; border: none; border-radius: 6px; cursor: pointer; font-weight: bold; font-size: 15px; margin: 10px; transition: all 0.3s ease; }
.btn-pos { background: #007bff; color: white; text-decoration: none; display: inline-block; box-shadow: 0 4px 6px rgba(0,123,255,0.2);}
.btn-close { background: #dc3545; color: white; box-shadow: 0 4px 6px rgba(220,53,69,0.2);}
.btn-open { background: #28a745; color: white; box-shadow: 0 4px 6px rgba(40,167,69,0.2);}
.btn:hover { opacity: 0.9; transform: translateY(-2px); }
.shift-details { margin: 20px 0; padding: 15px; background: rgba(0,0,0,0.03); border-radius: 8px; font-size: 15px;}
.wait-message { margin-top: 15px; color: #856404; background-color: #fff3cd; padding: 10px; border-radius: 6px; font-size: 14px;}
.text-success { color: #28a745; font-weight: bold; }
</style>