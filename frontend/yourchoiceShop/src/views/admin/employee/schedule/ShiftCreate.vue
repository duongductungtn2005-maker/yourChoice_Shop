<template>
    <div>
      <h1><span class="link" @click="router.back()">Ca làm việc</span><span class="separator">/</span>
      <span class="current">Thêm ca làm việc</span> </h1>
      
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

/* ===== PAGE WRAPPER ===== */
:host, .shift-container, body {
  margin: 0;
}

/* Tạo layout full màn hình + căn giữa */
.breadcrumb,
.form-card {
  max-width: 750px;
  margin-left: auto;
  margin-right: auto;
}

body {
  background: #f1f5f9;
}

/* ===== BREADCRUMB ===== */
.breadcrumb {
  margin-top: 40px;
  margin-bottom: 20px;
  font-size: 14px;
  font-weight: 500;
}

link {
  margin: 0; font-size: 24px; font-weight: bold; color: #1e293b; 
}

link:hover {
  color: #2563eb;
}

.separator {
  margin: 0 6px;
  color: #94a3b8;
}

.current {
  color: #1e293b;
  font-weight: 600;
}

/* ===== CARD ===== */
.form-card {
  background: #ffffff;
  border-radius: 14px;
  padding: 35px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.06);
  transition: 0.3s;
}

.form-card:hover {
  transform: translateY(-2px);
}

/* ===== FORM LAYOUT ===== */
.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 20px;
}

.half-width {
  flex: 1;
  margin-bottom: 0;
}

/* ===== LABEL ===== */
.form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 6px;
}

.required {
  color: #ef4444;
}

/* ===== INPUT ===== */
.form-input {
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 14px;
  transition: 0.2s;
  outline: none;
}

.form-input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.2);
}

/* ===== BUTTON AREA ===== */
.btn-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 25px;
}

/* ===== BUTTON ===== */
.btn-submit {
  padding: 10px 24px;
  border: none;
  background: #2563eb;
  color: white;
  font-weight: 500;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: 0.2s;
}

.btn-submit:hover {
  background: #1d4ed8;
}

/* ===== RESPONSIVE ===== */
@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
  }

  .breadcrumb,
  .form-card {
    padding-left: 15px;
    padding-right: 15px;
  }
}

</style>