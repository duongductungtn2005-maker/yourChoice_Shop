<template>
  <div class="page-container">
    <div class="breadcrumb">
      <span @click="router.back()">Lịch làm việc</span> / Thêm lịch làm việc
    </div>

    <div class="card form-card">
      <form @submit.prevent="handleSubmit">
        
        <div class="form-group">
          <label>Nhân viên <span class="required">*</span></label>
          <select v-model="form.employeeId" required>
            <option value="">Chọn nhân viên</option>
            <option value="1">Vũ Thành Hải Phong</option>
          </select>
        </div>

        <div class="form-group">
          <label>Ca làm việc <span class="required">*</span></label>
          <select v-model="form.shiftId" required>
            <option value="">Chọn ca làm việc</option>
            <option value="1">Ca Sáng (08:00 - 12:00)</option>
          </select>
        </div>

        <div class="form-group">
          <label>Ngày làm việc <span class="required">*</span></label>
          <input type="date" v-model="form.workDate" required />
        </div>

        <div class="form-group">
          <label>Ghi chú</label>
          <input type="text" v-model="form.note" placeholder="Nhập ghi chú nếu có" />
        </div>

        <div class="form-actions">
          <button type="submit" class="btn-submit">
            Thêm lịch làm việc
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const form = reactive({ employeeId: '', shiftId: '', workDate: '', note: '' });

const handleSubmit = () => {
    console.log("Dữ liệu lịch làm việc:", form);
};
</script>

<style scoped>
/* Tái sử dụng CSS từ ShiftCreate.vue */
* { box-sizing: border-box; }
.page-container { padding: 24px; background-color: #f8f9fa; min-height: 100vh; font-family: Arial, sans-serif; }
.breadcrumb { font-size: 18px; font-weight: bold; color: #1e293b; margin-bottom: 24px; }
.breadcrumb span { color: #94a3b8; cursor: pointer; transition: color 0.2s; }
.breadcrumb span:hover { color: #1e293b; }
.card { background: #ffffff; border-radius: 12px; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1); padding: 32px; border: 1px solid #f1f5f9; }
.form-card { max-width: 800px; }
.form-group { margin-bottom: 24px; }
label { display: block; font-size: 14px; font-weight: 500; color: #475569; margin-bottom: 8px; }
.required { color: #ef4444; }
input, select { width: 100%; padding: 10px 14px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; outline: none; transition: border-color 0.2s; background-color: #fff;}
input:focus, select:focus { border-color: #3b82f6; }
.form-actions { display: flex; justify-content: flex-end; margin-top: 32px; }
.btn-submit { padding: 10px 24px; border: 2px solid #1e293b; background: #ffffff; color: #1e293b; border-radius: 8px; cursor: pointer; font-weight: 600; font-size: 14px; transition: background 0.2s; }
.btn-submit:hover { background: #f8fafc; }
</style>