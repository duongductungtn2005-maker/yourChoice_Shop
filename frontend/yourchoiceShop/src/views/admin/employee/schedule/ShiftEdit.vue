<template>
  <div class="schedule-container">
    <h1 class="page-title">Cập nhật ca làm việc</h1>

    <div class="card">
      <form @submit.prevent="updateShift">
        <h2 class="section-subtitle">THÔNG TIN CA LÀM VIỆC</h2>
        
        <div class="filter-row">
          <div class="filter-group col-full">
            <label>Tên ca làm việc <span class="text-red">*</span></label>
            <input 
              type="text" 
              v-model="shiftData.tenCa" 
              placeholder="Nhập tên ca..." 
              required 
            />
          </div>

          <div class="filter-group">
            <label>Giờ bắt đầu <span class="text-red">*</span></label>
            <input type="time" v-model="shiftData.thoiGianBatDau" required />
          </div>

          <div class="filter-group">
            <label>Giờ kết thúc <span class="text-red">*</span></label>
            <input type="time" v-model="shiftData.thoiGianKetThuc" required />
          </div>

          <div class="filter-group">
            <label>Trạng thái</label>
            <select v-model="shiftData.trangThai">
              <option :value="1">Hoạt động</option>
              <option :value="0">Ngừng hoạt động</option>
            </select>
          </div>
        </div>

        <div class="action-footer">
          <button type="button" class="btn btn-navy" @click="router.back()">
            <i class="fas fa-arrow-left"></i> Quay lại
          </button>
          <button type="submit" class="btn btn-add">
            <i class="fas fa-save"></i> Lưu thay đổi
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '@/services/request';
import Swal from 'sweetalert2';

const route = useRoute();
const router = useRouter();
const shiftId = route.params.id;

const shiftData = ref({
  tenCa: '',
  thoiGianBatDau: '',
  thoiGianKetThuc: '',
  trangThai: 1
});

// Lưu bản gốc để khôi phục
const originalData = ref({});

const fetchShiftDetail = async () => {
  try {
    // Đảm bảo shiftId đã lấy được từ route.params.id
    if (!shiftId) return;

    // Kiểm tra URL API: '/ca-lam-viec/' + id
    const response = await request.get(`/ca-lam-viec/${shiftId}`);
    
    if (response.data) {
      shiftData.value = { ...response.data };
      originalData.value = { ...response.data };
    }
  } catch (error) {
    console.error("Lỗi 404 hoặc lỗi lấy dữ liệu:", error);
    // Nếu API trả về 404, có thể do ID không tồn tại trong DB
  }
};

const updateShift = async () => {
  try {
    await request.put(`/ca-lam-viec/${shiftId}`, shiftData.value);
    Swal.fire({
      icon: 'success',
      title: 'Thành công',
      text: 'Đã cập nhật thông tin ca làm việc!',
      timer: 1500,
      showConfirmButton: false
    });
    router.push({ name: 'admin-shift-list' });
  } catch (error) {
    Swal.fire('Thất bại', 'Vui lòng kiểm tra lại thông tin', 'error');
  }
};

const resetForm = () => {
  shiftData.value = { ...originalData.value };
};

onMounted(fetchShiftDetail);
</script>

<style scoped>
/* Đồng bộ CSS từ ảnh chụp màn hình */
.schedule-container { padding: 20px; background: #ebecee; min-height: 100vh; font-family: 'Segoe UI', sans-serif; }
.page-title { font-size: 24px; font-weight: bold; color: #1e293b; margin-bottom: 24px; }
.card { background: #fff; border-radius: 12px; padding: 30px; border: 1px solid #f1f5f9; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }

.section-subtitle { font-size: 14px; font-weight: 700; color: #334155; margin-bottom: 25px; text-transform: uppercase; }
.filter-row { display: grid; grid-template-columns: repeat(2, 1fr); gap: 24px; }
.col-full { grid-column: span 2; }

.filter-group { display: flex; flex-direction: column; gap: 8px; }
.filter-group label { font-size: 13px; font-weight: 600; color: #475569; }
.filter-group input, .filter-group select { 
  padding: 10px 14px; border: 1px solid #e2e8f0; border-radius: 8px; outline: none; font-size: 14px; 
}
.filter-group input:focus { border-color: #3b82f6; }

.text-red { color: #ef4444; }
.action-footer { margin-top: 30px; display: flex; justify-content: flex-end; gap: 12px; }

.btn { padding: 10px 24px; border-radius: 8px; font-weight: 600; cursor: pointer; border: none; display: flex; align-items: center; gap: 8px; }
.btn-navy { background: #0f172a; color: white; }
.btn-add { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: white; }
</style>