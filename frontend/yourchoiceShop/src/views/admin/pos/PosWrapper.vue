<template>
  <div class="pos-wrapper" style="position: relative; min-height: 80vh;">
    
    <div v-if="!isLoading && !hasActiveShift" class="lock-overlay">
      <div class="lock-box">
        <div class="icon-wrapper"><i class="fas fa-lock"></i></div>
        
        <template v-if="todaySchedule">
          <h2>Chưa mở ca làm việc!</h2>
          <p>Hôm nay bạn có lịch làm việc nhưng chưa mở ca. Vui lòng quay lại trang Trực ca để mở ca và bắt đầu bán hàng.</p>
          <router-link :to="{ name: 'staff-shift-tracking' }" class="btn-unlock">
            <i class="fas fa-key"></i> ĐI ĐẾN TRANG MỞ CA
          </router-link>
        </template>

        <template v-else>
          <h2>Không có quyền truy cập!</h2>
          <p style="color: #dc3545; font-weight: bold;">Hôm nay bạn không có lịch làm việc được phân công.</p>
          <p>Bạn không thể sử dụng chức năng bán hàng hoặc quản lý trong lúc hệ thống đang đóng ca của bạn.</p>
          <router-link :to="{ name: 'staff-shift-tracking' }" class="btn-unlock btn-disabled">
            <i class="fas fa-info-circle"></i> XEM LỊCH CỦA TÔI
          </router-link>
        </template>
      </div>
    </div>

    <BanHangTaiQuay v-if="hasActiveShift" />

  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2'; 

import request from '@/services/request'; 
import { isAuthenticated, getCurrentUser } from '@/services/auth'; 
import BanHangTaiQuay from './BanHangTaiQuay.vue'; 

const router = useRouter(); 
const user = getCurrentUser(); 

const isLoading = ref(true);
const hasActiveShift = ref(false); 
const todaySchedule = ref(null); 

let posInterval = null; 

const checkShiftStatus = async (isBackground = false) => {
  if (!isAuthenticated()) {
    if (!isBackground) router.push('/login');
    return;
  }

  if (!isBackground) isLoading.value = true;

  try {
    // ĐÃ FIX: Truyền username vào để Backend biết là ai đang hỏi
    const response = await request.get(`/giao-ca/hien-tai?username=${user.username}`);
    
    if (response.data && response.data.id) {
      hasActiveShift.value = true; 
    } else {
      const scheduleRes = await request.get(`/lich-lam-viec/hom-nay?username=${user.username}`);
      if (scheduleRes.data) {
        todaySchedule.value = scheduleRes.data; 
      } else {
        todaySchedule.value = null; 
      }

      if (hasActiveShift.value && isBackground) {
        Swal.fire({
          icon: 'error',
          title: 'Ca làm việc đã bị đóng!',
          text: 'Quản lý đã kết thúc ca này. Màn hình bán hàng sẽ bị khóa.',
          confirmButtonColor: '#d33',
          allowOutsideClick: false 
        }).then(() => {
          router.push({ name: 'staff-shift-tracking' }); 
        });
      }
      hasActiveShift.value = false; 
    }
  } catch (error) {
    if (error.response?.status === 400 && !isBackground) {
      Swal.fire({ icon: 'error', title: 'Lỗi phiên', text: 'Vui lòng đăng nhập lại!'});
      router.push('/login');
    }
    hasActiveShift.value = false;
  } finally {
    if (!isBackground) isLoading.value = false;
  }
};

onMounted(() => {
  checkShiftStatus(false); 
  
  posInterval = setInterval(() => {
    checkShiftStatus(true); 
  }, 10000);
});

onUnmounted(() => {
  if (posInterval) clearInterval(posInterval);
});
</script>

<style scoped>
/* CSS giữ nguyên, thêm style cho nút bị vô hiệu hóa */
.lock-overlay {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(235, 240, 245, 0.85); backdrop-filter: blur(4px);
  display: flex; justify-content: center; align-items: center; z-index: 1000; border-radius: 8px;
}
.lock-box { background: white; padding: 40px; border-radius: 16px; text-align: center; max-width: 450px; box-shadow: 0 10px 30px rgba(0,0,0,0.1); border: 1px solid #e0e0e0; }
.icon-wrapper { background-color: #f8d7da; color: #dc3545; width: 80px; height: 80px; border-radius: 50%; display: flex; justify-content: center; align-items: center; font-size: 32px; margin: 0 auto 20px auto; }
.lock-box h2 { color: #2c3e50; margin-bottom: 12px; font-weight: 700; }
.lock-box p { color: #6c757d; margin-bottom: 30px; line-height: 1.5; }
.btn-unlock { display: inline-block; background-color: #1e3a5f; color: white; padding: 12px 24px; border-radius: 8px; text-decoration: none; font-weight: 600; transition: all 0.3s ease; }
.btn-unlock:hover { background-color: #152b47; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(30,58,95,0.3); }

/* Thêm nút màu xám cho trường hợp không có lịch */
.btn-disabled { background-color: #6c757d; }
.btn-disabled:hover { background-color: #5a6268; box-shadow: none; }
</style>