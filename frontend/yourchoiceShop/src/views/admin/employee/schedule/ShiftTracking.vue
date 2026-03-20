<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2'; 
import request from '@/services/request'; 
import { getCurrentUser } from '@/services/auth'; 

const router = useRouter(); 
const user = getCurrentUser();
const employeeName = user?.tenNhanVien || user?.username || 'Nhân viên';

const activeShift = ref(null);    
const todaySchedule = ref(null);  
const isLoading = ref(true);
const isTimeToEndShift = ref(false); 
let timeChecker = null; 

// ==========================================
// 1. LẤY DỮ LIỆU LẦN ĐẦU (CÓ LOADING)
// ==========================================
const fetchCurrentShift = async () => {
  isLoading.value = true;
  try {
    const scheduleRes = await request.get(`/lich-lam-viec/hom-nay?username=${user.username}`); 
    if (scheduleRes.data) {
      todaySchedule.value = scheduleRes.data;
    }

    // ĐÃ FIX: Truyền username vào API hiện tại
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
  try {
    // ĐÃ FIX: Truyền username vào API hiện tại
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
    const res = await request.post('/giao-ca/mo-ca', { idLichLamViec: idLich }); 
    
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
/* CSS giữ nguyên, thêm 1 chút cho đẹp */
.user-header { background: #e9ecef; padding: 15px; border-radius: 8px; margin-bottom: 20px; font-size: 16px; border-left: 5px solid #007bff;}
.alert-card { padding: 30px; border-radius: 12px; text-align: center; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }
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