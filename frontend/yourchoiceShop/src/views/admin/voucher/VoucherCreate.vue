<template>
  <div class="page-container">
    <div class="header">
      <h3>Phiếu giảm giá / Tạo phiếu giảm giá</h3>
    </div>

    <div class="grid-layout">
      
      <div class="card left-panel">
        <h4 class="section-title">Thông tin phiếu</h4>
        
        <div class="form-group">
          <label>Mã phiếu giảm giá</label>
          <input v-model="form.maPhieuGiamGia" placeholder="Để trống mã sẽ tự sinh (VD: PGG...)" class="form-control" />
        </div>
        
        <div class="form-group">
          <label>Tên phiếu giảm giá <span class="required">*</span></label>
          <input v-model="form.tenPhieuGiamGia" placeholder="Ví dụ: Voucher tri ân khách hàng" class="form-control" />
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
            <label>Số lượng phát hành</label>
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
            <div class="date-wrap">
                <span class="sub-label">Bắt đầu:</span>
                <input v-model="form.ngayBatDau" type="datetime-local" class="form-control" />
            </div>
            <div class="date-wrap">
                <span class="sub-label">Kết thúc:</span>
                <input v-model="form.ngayKetThuc" type="datetime-local" class="form-control" />
            </div>
          </div>
        </div>

        <div class="form-group highlight-box">
          <label>Kiểu phát hành</label>
          <div class="radio-group">
            <label class="radio-item">
              <input type="radio" value="CongKhai" v-model="form.kieu" /> 
              <span><b>Công khai</b> (Mọi người đều thấy)</span>
            </label>
            <label class="radio-item">
              <input type="radio" value="CaNhan" v-model="form.kieu" /> 
              <span><b>Cá nhân</b> (Gửi riêng qua Email)</span>
            </label>
          </div>
        </div>

        <button @click="submitForm" class="btn-submit">
            <i class="fas fa-save"></i> Hoàn tất & Thêm mới
        </button>
      </div>

      <div class="card right-panel">
        <div class="panel-header">
          <h4>Danh sách khách hàng</h4>
          <span class="note" v-if="form.kieu === 'CongKhai'">Tất cả khách hàng sẽ sử dụng được mã này</span>
          <span class="note text-blue" v-else>Chọn khách hàng để gửi mã qua Email</span>
        </div>
        
        <div v-if="form.kieu === 'CaNhan'" class="customer-section">
            <div class="search-box">
              <i class="fas fa-search search-icon"></i>
              <input v-model="searchQuery" placeholder="Tìm tên hoặc email..." class="form-control pl-30" />
            </div>
            
            <div class="customer-list-container">
              <table class="mini-table">
                <thead>
                  <tr>
                    <th width="10%">
                        <input type="checkbox" @change="toggleAll($event)" :checked="isAllSelected">
                    </th>
                    <th>Tên khách hàng</th>
                    <th>Email</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="filteredCustomers.length === 0">
                      <td colspan="3" class="text-center">Không tìm thấy khách hàng</td>
                  </tr>
                  <tr v-for="kh in filteredCustomers" :key="kh.id" :class="{ 'selected-row': form.customerIds.includes(kh.id) }">
                    <td>
                        <input type="checkbox" :value="kh.id" v-model="form.customerIds">
                    </td>
                    <td>{{ kh.tenKhachHang }}</td>
                    <td class="text-email">{{ kh.email }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="selected-count" v-if="form.customerIds.length > 0">
                Đã chọn: <b>{{ form.customerIds.length }}</b> khách hàng
            </div>
        </div>
        
        <div v-else class="empty-state">
            <div class="icon-circle">
                <i class="fas fa-users-viewfinder"></i>
            </div>
            <p>Voucher công khai áp dụng cho tất cả mọi người.</p>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import request from '@/services/request'; 
import { useRouter } from 'vue-router';

const router = useRouter();

// 1. STATE
const customers = ref([]); // Data khách hàng từ API
const searchQuery = ref('');

const form = ref({
  maPhieuGiamGia: '',
  tenPhieuGiamGia: '',
  loaiPhieu: 'PhanTram', // Hoặc TienMat
  giaTriGiam: 0,
  giaTriGiamToiDa: 0,
  donHangToiThieu: 0,
  soLuong: 100,
  ngayBatDau: '',
  ngayKetThuc: '',
  trangThai: 1,
  kieu: 'CongKhai', // Mặc định là Công khai
  customerIds: []   // Mảng chứa ID khách hàng được chọn
});

// 2. LOAD DATA KHÁCH HÀNG
onMounted(async () => {
    try {
        // Giả sử API lấy khách hàng của bạn là /khach-hang (Method GET)
        const res = await request.get('/khach-hang'); 
        
        // Backend có thể trả về Page (res.data.content) hoặc List (res.data)
        // Bạn check lại API của mình nhé. Ở đây mình handle cả 2.
        if (res.data && Array.isArray(res.data.content)) {
            customers.value = res.data.content;
        } else if (Array.isArray(res.data)) {
            customers.value = res.data;
        }
    } catch (e) {
        console.error("Lỗi tải khách hàng:", e);
    }
});

// 3. LOGIC LỌC KHÁCH HÀNG
const filteredCustomers = computed(() => {
    if (!searchQuery.value) return customers.value;
    const lower = searchQuery.value.toLowerCase();
    return customers.value.filter(c => 
        (c.tenKhachHang && c.tenKhachHang.toLowerCase().includes(lower)) || 
        (c.email && c.email.toLowerCase().includes(lower))
    );
});

// 4. LOGIC CHỌN TẤT CẢ (CHECK ALL)
const isAllSelected = computed(() => {
    if (filteredCustomers.value.length === 0) return false;
    // Kiểm tra xem tất cả KH đang hiển thị có nằm trong mảng selected không
    return filteredCustomers.value.every(c => form.value.customerIds.includes(c.id));
});

const toggleAll = (event) => {
    const visibleIds = filteredCustomers.value.map(c => c.id);
    if (event.target.checked) {
        // Thêm những ID chưa có vào mảng selected
        const newIds = visibleIds.filter(id => !form.value.customerIds.includes(id));
        form.value.customerIds.push(...newIds);
    } else {
        // Bỏ chọn những ID đang hiển thị
        form.value.customerIds = form.value.customerIds.filter(id => !visibleIds.includes(id));
    }
};

// 5. SUBMIT FORM
const submitForm = async () => {
  try {
    // Validate cơ bản
    if (!form.value.tenPhieuGiamGia) return alert('Vui lòng nhập tên phiếu');
    if (!form.value.ngayBatDau || !form.value.ngayKetThuc) return alert('Vui lòng chọn thời gian');
    
    // Validate Cá nhân
    if (form.value.kieu === 'CaNhan' && form.value.customerIds.length === 0) {
        return alert('Vui lòng chọn ít nhất 1 khách hàng để gửi mã!');
    }

    const payload = { ...form.value };
    // Nếu mã rỗng -> để null cho BE tự sinh
    if (!payload.maPhieuGiamGia || payload.maPhieuGiamGia.trim() === '') {
        payload.maPhieuGiamGia = null; 
    }

    // Call API tạo voucher
    await request.post('/phieu-giam-gia', payload);
    
    alert('Tạo phiếu giảm giá thành công! ' + (form.value.kieu === 'CaNhan' ? 'Email đang được gửi.' : ''));
    router.push('/admin/vouchers');
  } catch (error) {
    const msg = error.response?.data?.message || error.message || 'Có lỗi xảy ra';
    alert('Lỗi: ' + msg);
  }
};
</script>

<style scoped>
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; color: #334155; }
.header { margin-bottom: 20px; font-weight: 700; color: #2b4360; }
.grid-layout { display: grid; grid-template-columns: 3fr 2fr; gap: 20px; }

/* PANEL CHUNG */
.card { background: #fff; padding: 24px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); height: fit-content; }
.section-title { margin-bottom: 20px; font-size: 16px; border-bottom: 2px solid #f1f5f9; padding-bottom: 10px; color: #2b4360; }

/* FORM STYLES */
.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 6px; font-weight: 600; font-size: 14px; }
.required { color: #ef4444; }
.form-control { width: 100%; padding: 10px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 14px; outline: none; transition: 0.2s; }
.form-control:focus { border-color: #2b4360; box-shadow: 0 0 0 2px rgba(43, 67, 96, 0.1); }
.form-control:disabled { background: #f1f5f9; cursor: not-allowed; }

.row-2 { display: flex; gap: 15px; }
.row-2 > * { flex: 1; }
.input-group { display: flex; }
.input-group input { border-top-right-radius: 0; border-bottom-right-radius: 0; }
.unit-select { border: 1px solid #cbd5e1; border-left: none; background: #f8fafc; padding: 0 15px; border-top-right-radius: 6px; border-bottom-right-radius: 6px; font-weight: bold; cursor: pointer; }
.sub-label { display: block; font-size: 12px; color: #64748b; margin-bottom: 4px; }

/* RADIO GROUP */
.highlight-box { background: #f8fafc; padding: 15px; border-radius: 8px; border: 1px dashed #cbd5e1; }
.radio-group { display: flex; flex-direction: column; gap: 10px; margin-top: 8px; }
.radio-item { display: flex; align-items: center; cursor: pointer; font-size: 14px; }
.radio-item input { margin-right: 10px; width: 18px; height: 18px; accent-color: #2b4360; }
.radio-item span b { color: #2b4360; }

.btn-submit { width: 100%; background: #2b4360; color: #fff; padding: 12px; border: none; border-radius: 6px; font-weight: 600; cursor: pointer; margin-top: 20px; font-size: 15px; display: flex; justify-content: center; gap: 10px; align-items: center; transition: 0.2s; }
.btn-submit:hover { background: #1e2f45; transform: translateY(-1px); }

/* RIGHT PANEL STYLES */
.panel-header h4 { margin: 0 0 5px 0; font-size: 16px; color: #2b4360; }
.note { font-size: 13px; color: #64748b; font-style: italic; }
.text-blue { color: #2563eb; }

/* Customer Table */
.customer-section { margin-top: 15px; }
.search-box { position: relative; margin-bottom: 10px; }
.search-icon { position: absolute; left: 10px; top: 12px; color: #94a3b8; }
.pl-30 { padding-left: 32px; }

.customer-list-container { max-height: 400px; overflow-y: auto; border: 1px solid #e2e8f0; border-radius: 6px; }
.mini-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.mini-table th { position: sticky; top: 0; background: #f1f5f9; padding: 10px; text-align: left; color: #475569; font-weight: 600; border-bottom: 1px solid #e2e8f0; }
.mini-table td { padding: 8px 10px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.selected-row { background-color: #eff6ff; }
.text-email { color: #64748b; }
.selected-count { margin-top: 10px; text-align: right; font-size: 13px; color: #2b4360; background: #e0f2fe; display: inline-block; padding: 4px 10px; border-radius: 4px; float: right; }

/* Empty State */
.empty-state { text-align: center; padding: 40px 20px; color: #94a3b8; background: #f8fafc; border-radius: 6px; margin-top: 15px; border: 1px dashed #cbd5e1; }
.icon-circle { width: 60px; height: 60px; background: #e2e8f0; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 15px; }
.icon-circle i { font-size: 24px; color: #64748b; }
</style>