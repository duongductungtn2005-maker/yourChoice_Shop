<template>
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
          <p><strong>Giờ vào ca:</strong> {{ formatTime(activeShift.thoiGianNhanCa) || 'Đang cập nhật' }}</p>
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

      <div v-else-if="todaySchedule && !isSchedulePast" class="alert-card status-empty">
        <h2><i class="fas fa-clock"></i> Chưa mở ca làm việc</h2>
        
        <div class="shift-details">
          <p>Bạn có lịch làm việc hôm nay:</p>
          <p><strong>Ca:</strong> {{ todaySchedule.caLamViec?.tenCa || 'Đang cập nhật' }}</p>
          <p><strong>Thời gian:</strong> 
            {{ formatTime(todaySchedule.caLamViec?.thoiGianBatDau || todaySchedule.thoiGianBatDau) }} - 
            {{ formatTime(endTimeRawGlobal) }} </p>
        </div>

        <button @click="handleOpenShift" class="btn btn-open">
          BẮT ĐẦU CA LÀM VIỆC
        </button>
      </div>

      <div v-else class="alert-card status-no-shift">
          <h2><i class="fas fa-calendar-times"></i> Không có lịch làm việc</h2>
          <p v-if="isSchedulePast">Ca làm việc của bạn hôm nay đã kết thúc lúc {{ endTimeRawGlobal }}.</p>
          <p v-else>Hôm nay bạn không có lịch được phân công.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2'; 
import request from '@/services/request'; 
import { getCurrentUser } from '@/services/auth'; 

const router = useRouter(); 
const user = getCurrentUser() || {}; 
const employeeName = user.tenNhanVien || user.tenTaiKhoan || 'Nhân viên';

const activeShift = ref(null);    
const todaySchedule = ref(null);  
const isLoading = ref(true);
const isTimeToEndShift = ref(false); 
const endTimeRawGlobal = ref(''); // 🔥 ĐÃ THÊM: Biến lưu riêng giờ kết thúc để không ảnh hưởng activeShift
let timeChecker = null; 

// 2. Thêm đoạn này vào bên dưới các khai báo ref
const isSchedulePast = computed(() => {
  if (!endTimeRawGlobal.value) return false;
  
  const endTime = parseTime(endTimeRawGlobal.value);
  if (!endTime) return false;

  const now = new Date();
  // Nếu bây giờ đã muộn hơn giờ kết thúc ca thì coi như ca đã qua
  return now > endTime;
});
const handleOpenShift = async () => {
  if (!todaySchedule.value) return;

  // HIỆN THÔNG BÁO XÁC NHẬN TRƯỚC KHI VÀO CA
  const confirm = await Swal.fire({
    title: 'Xác nhận vào ca?',
    text: `Bạn chuẩn bị bắt đầu ca làm việc: ${todaySchedule.value.caLamViec?.tenCa}`,
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Xác nhận vào làm',
    cancelButtonText: 'Để sau'
  });

  if (!confirm.isConfirmed) return;

  try {
    const res = await request.post(`/giao-ca/mo-ca?username=${user.tenTaiKhoan}`, {
      idLichLamViec: todaySchedule.value.id
    });

    if (res.data) {
      activeShift.value = {
        ...res.data,
        thoiGianKetThucDuKien: endTimeRawGlobal.value
      };
      sessionStorage.setItem('hasActiveShift', 'true');
      window.dispatchEvent(new Event('shift-changed'));
      
      Swal.fire({
        icon: 'success',
        title: 'Bắt đầu ca thành công!',
        timer: 1500,
        showConfirmButton: false
      });
    }
  } catch (error) {
    Swal.fire('Lỗi', error.response?.data || 'Không thể mở ca', 'error');
  }
};

const fetchCurrentShift = async () => {
  if (!user || !user.tenTaiKhoan) {
    router.push('/login');
    return;
  }

  isLoading.value = true;

  try {
    // 1. Lấy lịch làm việc
    const scheduleRes = await request.get(`/lich-lam-viec/hom-nay?username=${user.tenTaiKhoan}`);
    todaySchedule.value = scheduleRes.data || null;

    if (!todaySchedule.value) {
      activeShift.value = null;
      sessionStorage.setItem('hasActiveShift', 'false');
      return;
    }

    const ca = todaySchedule.value?.caLamViec || {};

    const endTimeRaw =
      ca?.gioKetThuc ||
      ca?.thoiGianKetThuc ||
      todaySchedule.value?.thoiGianKetThuc ||
      todaySchedule.value?.gioKetThuc;

    endTimeRawGlobal.value = endTimeRaw; // 🔥 ĐÃ THÊM: Lưu vào biến ngoài để dùng cho Template

    // 2. Lấy ca hiện tại
    const res = await request.get(`/giao-ca/hien-tai?username=${user.tenTaiKhoan}`);

    if (res.data && res.data.trangThai === 1) {
      // 👉 ĐANG TRONG CA
      activeShift.value = {
        ...res.data,
        thoiGianKetThucDuKien: endTimeRaw
      };
      sessionStorage.setItem('hasActiveShift', 'true');

    } else {
      // 👉 CHƯA MỞ CA: Gán null chuẩn chỉ để không bị lỗi màn hình xanh
      activeShift.value = null; 
      sessionStorage.setItem('hasActiveShift', 'false');
    }

    checkTimeToEndShift();

  } catch (error) {
    console.error("Lỗi:", error);
  } finally {
    isLoading.value = false;
  }
};
const backgroundSync = async () => {
  if (!user?.tenTaiKhoan) return;

  try {
    const res = await request.get(`/giao-ca/hien-tai?username=${user.tenTaiKhoan}`);

    if (res.data && res.data.trangThai === 1) {
      activeShift.value = {
        ...res.data,
        thoiGianKetThucDuKien: endTimeRawGlobal.value
      };
      checkTimeToEndShift();
      sessionStorage.setItem('hasActiveShift', 'true');
    } else {
      console.warn("Không tìm thấy ca hiện tại (có thể delay backend)");
    }
  } catch (error) {
    console.log("Sync lỗi:", error.message);
  }
};

const checkTimeToEndShift = () => {
  if (!activeShift.value) return;

  const endTime = parseTime(activeShift.value.thoiGianKetThucDuKien);
  if (!endTime) return;

  const now = new Date();
  isTimeToEndShift.value = now >= endTime;
};

const parseTime = (timeStr) => {
  if (!timeStr) return null;
  if (typeof timeStr === 'string' && timeStr.includes(':') && !timeStr.includes('T')) {
    const parts = timeStr.split(':');
    const h = Number(parts[0]);
    const m = Number(parts[1]);
    const d = new Date();
    d.setHours(h, m, 0, 0);
    return d;
  }
  const d = new Date(timeStr);
  return isNaN(d.getTime()) ? null : d;
};

const handleCloseShift = async () => {
  if (isTimeToEndShift.value) {
    const confirm = await Swal.fire({
      icon: 'info',
      title: 'Đã đến giờ kết ca',
      text: 'Bạn có muốn đóng ca không?',
      showCancelButton: true,
      confirmButtonText: 'Đóng ca',
      cancelButtonText: 'Hủy'
    });
    if (!confirm.isConfirmed) return;
  }
  
  if (!isTimeToEndShift.value) {
    const confirm = await Swal.fire({
      icon: 'warning',
      title: 'Chưa đến giờ kết ca',
      text: 'Bạn có chắc muốn đóng ca sớm không?',
      showCancelButton: true,
      confirmButtonText: 'Đóng ca',
      cancelButtonText: 'Hủy'
    });
    if (!confirm.isConfirmed) return;
  }

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
      sessionStorage.setItem('hasActiveShift', 'false'); 
      window.dispatchEvent(new Event('shift-changed'));
      router.push('/staff/giao-ca'); 
    } catch (e) { 
      Swal.fire({
        icon: 'error',
        title: 'Lỗi đóng ca',
        text: 'Có lỗi xảy ra, vui lòng thử lại sau.'
      });
    }
  }
};

const formatTime = (value) => {
  if (!value) return '--:--';
  if (typeof value === 'string' && value.includes(':') && !value.includes('T')) {
    return value.slice(0, 5); 
  }
  const d = new Date(value);
  if (isNaN(d.getTime())) return '--:--';
  return d.toLocaleTimeString('vi-VN', {
    hour: '2-digit',
    minute: '2-digit'
  });
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