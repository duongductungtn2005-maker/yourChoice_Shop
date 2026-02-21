<template>
  <div class="page-container">
    <div class="breadcrumb">
      <span @click="router.back()">Ca làm việc</span> / Thêm ca làm việc
    </div>

    <div class="card form-card">
      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>Tên ca <span class="required">*</span></label>
          <input type="text" v-model="form.tenCa" placeholder="VD: Ca Sáng" required />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>Thời gian bắt đầu <span class="required">*</span></label>
            <input type="time" v-model="form.thoiGianBatDau" required />
          </div>
          <div class="form-group">
            <label>Thời gian kết thúc <span class="required">*</span></label>
            <input type="time" v-model="form.thoiGianKetThuc" required />
          </div>
        </div>
        <div class="form-group">
          <label>Mô tả</label>
          <input type="text" v-model="form.moTa" placeholder="Nhập mô tả" />
        </div>
        <div class="form-actions">
          <button type="submit" class="btn-submit">Thêm ca làm việc</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/services/request';
import Swal from 'sweetalert2';

const router = useRouter();
const form = reactive({ tenCa: '', thoiGianBatDau: '', thoiGianKetThuc: '', moTa: '' });

const handleSubmit = async () => {
    Swal.fire({ title: 'Đang xử lý...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    try {
        await request.post('/ca-lam-viec', form); // Sửa URL cho khớp API Backend
        
        Swal.close();
        await Swal.fire({ icon: 'success', title: 'Thành công!', text: 'Đã thêm ca làm việc.', timer: 2000, showConfirmButton: false });
        router.back(); // Quay lại trang danh sách
    } catch (error) {
        Swal.close();
        Swal.fire({ icon: 'error', title: 'Thất bại', text: error.response?.data?.message || 'Có lỗi xảy ra!' });
    }
};
</script>

<style scoped>
* { box-sizing: border-box; }
.page-container { padding: 24px; background-color: #f8f9fa; min-height: 100vh; font-family: Arial, sans-serif; }
.breadcrumb { font-size: 18px; font-weight: bold; color: #1e293b; margin-bottom: 24px; }
.breadcrumb span { color: #94a3b8; cursor: pointer; transition: color 0.2s; }
.breadcrumb span:hover { color: #1e293b; }
.card { background: #ffffff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1); padding: 32px; border: 1px solid #f1f5f9; }
.form-card { max-width: 800px; }
.form-group { margin-bottom: 24px; }
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
label { display: block; font-size: 14px; font-weight: 500; color: #475569; margin-bottom: 8px; }
.required { color: #ef4444; }
input, select { width: 100%; padding: 10px 14px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; outline: none; transition: border-color 0.2s; }
input:focus, select:focus { border-color: #3b82f6; }
.form-actions { display: flex; justify-content: flex-end; margin-top: 32px; }
.btn-submit { padding: 10px 24px; border: 2px solid #1e293b; background: #ffffff; color: #1e293b; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 14px; transition: background 0.2s; }
.btn-submit:hover { background: #f8fafc; }
</style>