<template>
    <div class="breadcrumb">
      <span class="link" @click="router.back()">Ca làm việc</span> 
      <span class="separator">/</span>
      <span class="current">Thêm ca làm việc</span>
    </div>

    <div class="form-card">
      <form @submit.prevent="handleSubmit">
        
        <div class="form-group">
          <label class="form-label">Tên ca <span class="required">*</span></label>
          <input type="text" v-model="form.name" placeholder="Nhập tên ca" class="form-input" required />
        </div>

        <div class="form-row">
          <div class="form-group half-width">
            <label class="form-label">Thời gian bắt đầu <span class="required">*</span></label>
            <input type="time" v-model="form.startTime" class="form-input" required />
          </div>
          <div class="form-group half-width">
            <label class="form-label">Thời gian kết thúc <span class="required">*</span></label>
            <input type="time" v-model="form.endTime" class="form-input" required />
          </div>
        </div>

        <div class="form-group">
          <label class="form-label">Mô tả</label>
          <input type="text" v-model="form.description" placeholder="Nhập mô tả" class="form-input" />
        </div>

        <div class="btn-container">
          <button type="submit" class="btn-submit">
            Thêm ca làm việc
          </button>
        </div>

      </form>
    </div>
</template>

<script setup>
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import Swal from 'sweetalert2';

const router = useRouter();
const form = reactive({ name: '', startTime: '', endTime: '', description: '' });

const handleSubmit = async () => {
  try {
    const payload = {
      maCa: 'CA' + new Date().getTime().toString().slice(-5), 
      tenCa: form.name,
      thoiGianBatDau: form.startTime + ':00', 
      thoiGianKetThuc: form.endTime + ':00',
      ghiChu: form.description,
      trangThai: 1 
    };

    const response = await axios.post('http://localhost:8080/api/v1/ca-lam-viec', payload);

    if (response.status === 200 || response.status === 201) {
      Swal.fire({
        icon: 'success',
        title: 'Thành công!',
        text: 'Thêm ca làm việc thành công',
        showConfirmButton: false,
        timer: 1500
      });
      router.back(); 
    }
  } catch (error) {
    console.error("Lỗi khi thêm ca làm việc:", error);
    Swal.fire({
      icon: 'error',
      title: 'Thất bại!',
      text: error.response?.data?.message || 'Có lỗi xảy ra khi thêm ca làm việc.',
    });
  }
};
</script>

<style scoped>

/* Tổng quan trang */
.shift-container {
  padding: 24px;
  background-color: #f9fafb;
  min-height: 100vh;
  box-sizing: border-box;
}

/* Breadcrumb (Đường dẫn phía trên) */
.breadcrumb {
  margin-bottom: 16px;
  font-size: 18px;
  font-weight: bold;
}
.breadcrumb .link {
  color: #6b7280;
  cursor: pointer;
  transition: color 0.2s;
}
.breadcrumb .link:hover {
  color: #1f2937;
}
.breadcrumb .separator {
  color: #6b7280;
  margin: 0 4px;
}
.breadcrumb .current {
  color: #1f2937;
}

/* Khung Form trắng */
.form-card {
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 24px;
  max-width: 800px;
}

/* Layout của Form */
.form-group {
  margin-bottom: 16px;
}
.form-row {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
}
.half-width {
  flex: 1;
  margin-bottom: 0; /* Ghi đè margin-bottom khi ở trong row */
}

/* Nhãn (Label) */
.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}
.required {
  color: #ef4444;
}

/* Ô nhập liệu (Input) */
.form-input {
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 10px 16px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.form-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
}

/* Khu vực nút bấm */
.btn-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

/* Nút Submit */
.btn-submit {
  padding: 8px 24px;
  border: 2px solid #334155;
  color: #334155;
  background-color: transparent;
  font-weight: 500;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s, color 0.2s;
}
.btn-submit:hover {
  background-color: #f8fafc;
}
</style>