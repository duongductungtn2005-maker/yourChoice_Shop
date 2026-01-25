<template>
  <div class="page-container">
    <div class="header">
      <h3>Phiếu giảm giá / Tạo phiếu giảm giá</h3>
    </div>

    <div class="grid-layout">
      <div class="card left-panel">
        <div class="form-group">
          <label>Mã phiếu giảm giá</label>
          <input v-model="form.maPhieuGiamGia" placeholder="Để trống mã sẽ tự sinh (VD: PGG...)" class="form-control" />
        </div>
        
        <div class="form-group">
          <label>Tên phiếu giảm giá <span class="required">*</span></label>
          <input v-model="form.tenPhieuGiamGia" placeholder="Ví dụ: Siêu sale 2025" class="form-control" />
        </div>

        <div class="row-2">
          <div class="form-group">
            <label>Giá trị giảm</label>
            <div class="input-group">
              <input v-model.number="form.giaTriGiam" type="number" class="form-control" />
              <select v-model="form.loaiPhieu" class="unit-select">
                <option value="PhanTram">%</option>
                <option value="TienMat">VNĐ</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label>Giảm tối đa (VNĐ)</label>
            <input v-model.number="form.giaTriGiamToiDa" type="number" class="form-control" :disabled="form.loaiPhieu === 'TienMat'" />
          </div>
        </div>

        <div class="row-2">
          <div class="form-group">
            <label>Số lượng</label>
            <input v-model.number="form.soLuong" type="number" class="form-control" />
          </div>
          <div class="form-group">
            <label>Đơn tối thiểu (VNĐ)</label>
            <input v-model.number="form.donHangToiThieu" type="number" class="form-control" />
          </div>
        </div>

        <div class="form-group">
          <label>Thời gian áp dụng <span class="required">*</span></label>
          <div class="row-2">
            <input v-model="form.ngayBatDau" type="datetime-local" class="form-control" />
            <input v-model="form.ngayKetThuc" type="datetime-local" class="form-control" />
          </div>
        </div>

        <div class="form-group">
          <label>Kiểu phát hành</label>
          <div class="radio-group">
            <label class="radio-item">
              <input type="radio" value="CongKhai" v-model="form.kieu" /> Công khai
            </label>
            <label class="radio-item">
              <input type="radio" value="CaNhan" v-model="form.kieu" /> Cá nhân (Gửi riêng)
            </label>
          </div>
        </div>

        <button @click="submitForm" class="btn-submit">Hoàn tất & Thêm mới</button>
      </div>

      <div class="card right-panel">
        <div class="panel-header">
          <h4>Danh sách khách hàng</h4>
          <span class="note" v-if="form.kieu === 'CongKhai'">Tất cả khách hàng sẽ sử dụng được mã này</span>
          <span class="note" v-else>Cần gửi mã qua Email cho khách hàng sau khi tạo</span>
        </div>
        
        <div class="search-box">
          <input placeholder="Tìm khách hàng..." class="form-control" />
        </div>
        
        <div class="customer-list">
          <table class="mini-table">
            <thead>
              <tr>
                <th><input type="checkbox"></th>
                <th>Tên</th>
                <th>Email</th>
              </tr>
            </thead>
            <tbody>
              <tr><td colspan="3" style="text-align:center; color:#999; padding:20px;">Tính năng chọn khách hàng đang phát triển</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import request from '@/services/request';
import { useRouter } from 'vue-router';

const router = useRouter();

// FIX LỖI: Đưa kieu vào thẳng form
const form = ref({
  maPhieuGiamGia: '',
  tenPhieuGiamGia: '',
  loaiPhieu: 'PhanTram',
  giaTriGiam: 0,
  giaTriGiamToiDa: 0,
  donHangToiThieu: 0,
  soLuong: 100,
  ngayBatDau: '',
  ngayKetThuc: '',
  trangThai: 1,
  kieu: 'CongKhai' // Mặc định là Công khai
});

const submitForm = async () => {
  try {
    // Validate
    if (!form.value.tenPhieuGiamGia) return alert('Vui lòng nhập tên phiếu');
    if (!form.value.ngayBatDau || !form.value.ngayKetThuc) return alert('Vui lòng chọn thời gian');

    // Clone payload để xử lý
    const payload = { ...form.value };
    
    // Xử lý mã rỗng -> null để Backend tự sinh
    if (!payload.maPhieuGiamGia || payload.maPhieuGiamGia.trim() === '') {
        payload.maPhieuGiamGia = null; 
    }

    // Call API
    await request.post('/phieu-giam-gia', payload);
    
    alert('Tạo phiếu giảm giá thành công!');
    router.push('/admin/vouchers');
  } catch (error) {
    const msg = error.response?.data?.message || error.message || 'Có lỗi xảy ra';
    alert('Lỗi: ' + msg);
  }
};
</script>

<style scoped>
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; }
.header { margin-bottom: 20px; }
.header h3 { font-weight: 700; color: #2b4360; }
.grid-layout { display: grid; grid-template-columns: 3fr 2fr; gap: 20px; }
.card { background: #fff; padding: 24px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }

.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 6px; font-weight: 600; color: #334155; font-size: 14px; }
.required { color: #ef4444; }

.form-control { width: 100%; padding: 10px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 14px; }
.form-control:focus { border-color: #2b4360; outline: none; }
.form-control:disabled { background: #f1f5f9; cursor: not-allowed; }

.row-2 { display: flex; gap: 15px; }
.row-2 > * { flex: 1; }

.input-group { display: flex; }
.input-group input { border-top-right-radius: 0; border-bottom-right-radius: 0; }
.unit-select { border: 1px solid #cbd5e1; border-left: none; background: #f8fafc; padding: 0 10px; border-top-right-radius: 6px; border-bottom-right-radius: 6px; font-weight: bold; }

.radio-group { display: flex; gap: 20px; margin-top: 8px; }
.radio-item { display: flex; align-items: center; cursor: pointer; font-size: 14px; }
.radio-item input { margin-right: 8px; accent-color: #2b4360; }

.btn-submit { width: 100%; background: #2b4360; color: #fff; padding: 12px; border: none; border-radius: 6px; font-weight: 600; cursor: pointer; margin-top: 10px; }
.btn-submit:hover { background: #1e2f45; }

/* Right Panel */
.panel-header { margin-bottom: 15px; }
.note { font-size: 12px; color: #64748b; font-style: italic; display: block; margin-top: 5px;}
.mini-table { width: 100%; margin-top: 15px; border-collapse: collapse; font-size: 13px; }
.mini-table th { text-align: left; padding: 8px; color: #64748b; border-bottom: 1px solid #e2e8f0; }
.mini-table td { padding: 8px; border-bottom: 1px solid #f1f5f9; }
</style>