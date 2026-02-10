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
          <span class="note" v-if="form.kieu === 'CongKhai'">
             <i class="fas fa-info-circle"></i> Chọn "Cá nhân" để chọn khách hàng cụ thể.
          </span>
          <span class="note text-primary" v-else>
             <i class="fas fa-check-circle"></i> Đã chọn: <b>{{ selectedCustomerIds.length }}</b> khách hàng
          </span>
        </div>
        
        <div class="search-box">
          <input 
            v-model="custFilter.keyword" 
            @keyup.enter="fetchCustomers"
            placeholder="Tìm kiếm theo tên, email, sđt..." 
            class="form-control" 
            :disabled="form.kieu === 'CongKhai'"
          />
        </div>
        
        <div class="customer-list" :class="{ 'disabled-list': form.kieu === 'CongKhai' }">
          <table class="mini-table">
            <thead>
              <tr>
                <th width="40" class="text-center">
                    <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" :disabled="form.kieu === 'CongKhai'">
                </th>
                <th>Thông tin khách hàng</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loadingCust"><td colspan="2" class="text-center">Đang tải...</td></tr>
              <tr v-else-if="customers.length === 0"><td colspan="2" class="text-center">Không tìm thấy khách hàng</td></tr>
              
              <tr v-else v-for="cust in customers" :key="cust.id" :class="{ 'active-row': selectedCustomerIds.includes(cust.id) }">
                <td class="text-center">
                    <input 
                        type="checkbox" 
                        :value="cust.id" 
                        v-model="selectedCustomerIds"
                        :disabled="form.kieu === 'CongKhai'"
                    >
                </td>
                <td>
                    <div class="cust-name">{{ cust.hoTen }}</div>
                    <div class="cust-email">{{ cust.email }} - {{ cust.soDienThoai }}</div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="mini-pagination">
            <button class="mini-btn" :disabled="custPage === 0" @click="changeCustPage(custPage - 1)">‹</button>
            <span>Trang {{ custPage + 1 }}</span>
            <button class="mini-btn" :disabled="custPage >= custTotalPages - 1" @click="changeCustPage(custPage + 1)">›</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import request from '@/services/request';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';

const router = useRouter();

// --- STATE VOUCHER ---
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
  kieu: 'CongKhai'
});

// --- STATE KHÁCH HÀNG ---
const customers = ref([]);
const selectedCustomerIds = ref([]);
const loadingCust = ref(false);
const custPage = ref(0);
const custPageSize = ref(10); // Lấy 10 khách mỗi trang cho gọn
const custTotalPages = ref(0);
const custFilter = reactive({ keyword: '' });

// API: Lấy danh sách khách hàng
const fetchCustomers = async () => {
    loadingCust.value = true;
    try {
        const res = await request.get('/khach-hang', {
            params: {
                page: custPage.value,
                size: custPageSize.value,
                keyword: custFilter.keyword,
                
                // --- THÊM DÒNG NÀY ---
                trangThai: 1  // Chỉ lấy khách hàng đang Hoạt động
                // (Nếu API bên Khách hàng dùng tên biến là 'status' thì đổi thành status: 1)
                // ---------------------
            }
        });
        customers.value = res.data.content;
        custTotalPages.value = res.data.totalPages;
    } catch (e) {
        console.error(e);
    } finally {
        loadingCust.value = false;
    }
};

const changeCustPage = (p) => {
    if (p >= 0 && p < custTotalPages.value) {
        custPage.value = p;
        fetchCustomers();
    }
};

// Logic: Chọn tất cả (Bulk Select) trên trang hiện tại
const isAllSelected = computed(() => {
    if (customers.value.length === 0) return false;
    return customers.value.every(c => selectedCustomerIds.value.includes(c.id));
});

const toggleSelectAll = (e) => {
    if (e.target.checked) {
        // Thêm tất cả ID của trang hiện tại vào mảng selected
        const currentIds = customers.value.map(c => c.id);
        // Dùng Set để tránh trùng lặp
        selectedCustomerIds.value = [...new Set([...selectedCustomerIds.value, ...currentIds])];
    } else {
        // Bỏ chọn tất cả ID của trang hiện tại
        const currentIds = customers.value.map(c => c.id);
        selectedCustomerIds.value = selectedCustomerIds.value.filter(id => !currentIds.includes(id));
    }
};

// Nếu chuyển về Công khai -> Reset chọn khách hàng
watch(() => form.value.kieu, (newVal) => {
    if (newVal === 'CongKhai') {
        selectedCustomerIds.value = [];
    }
});

// --- SUBMIT ---
const submitForm = async () => {
  try {
    // Validate cơ bản
    if (!form.value.tenPhieuGiamGia || form.value.tenPhieuGiamGia.trim() === '') {
      return Swal.fire({ icon: 'warning', title: 'Thiếu tên phiếu', text: 'Vui lòng nhập tên phiếu giảm giá.' });
    }
    if (!form.value.giaTriGiam || Number(form.value.giaTriGiam) <= 0) {
      return Swal.fire({ icon: 'warning', title: 'Giá trị giảm không hợp lệ', text: 'Vui lòng nhập giá trị giảm lớn hơn 0.' });
    }
    if (!form.value.ngayBatDau || !form.value.ngayKetThuc) {
      return Swal.fire({ icon: 'warning', title: 'Thiếu thời gian', text: 'Vui lòng chọn thời gian bắt đầu và kết thúc.' });
    }
    if (new Date(form.value.ngayKetThuc) <= new Date(form.value.ngayBatDau)) {
      return Swal.fire({ icon: 'warning', title: 'Thời gian không hợp lệ', text: 'Thời gian kết thúc phải sau thời gian bắt đầu.' });
    }
    // Validate Khách hàng
    if (form.value.kieu === 'CaNhan' && selectedCustomerIds.value.length === 0) {
        return Swal.fire({ icon: 'warning', title: 'Chưa chọn khách hàng', text: 'Vui lòng chọn ít nhất 1 khách hàng cho phiếu cá nhân.' });
    }

    const payload = { ...form.value };
    
    if (!payload.maPhieuGiamGia || payload.maPhieuGiamGia.trim() === '') {
        payload.maPhieuGiamGia = null; 
    }

    // Nếu là Cá nhân, gửi kèm danh sách ID khách hàng
    if (payload.kieu === 'CaNhan') {
        payload.customerIds = selectedCustomerIds.value;
    }

    await request.post('/phieu-giam-gia', payload);
    await Swal.fire({ icon: 'success', title: 'Thành công', text: 'Đã tạo phiếu giảm giá!' });
    router.push('/admin/vouchers');
  } catch (error) {
    const msg = error.response?.data?.message || error.message || 'Có lỗi xảy ra';
    alert('Lỗi: ' + msg);
  }
};

onMounted(() => {
    fetchCustomers();
});
</script>

<style scoped>
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background-color: #f8f9fa; min-height: 100vh; }
.header { margin-bottom: 20px; }
.header h3 { font-weight: 700; color: #2b4360; font-size: 24px; }
.grid-layout { display: grid; grid-template-columns: 3fr 2fr; gap: 20px; }
.card { background: #fff; padding: 24px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }

/* Left Panel Form */
.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 6px; font-weight: 500; color: #334155; font-size: 14px; }
.required { color: #ef4444; }
.form-control { width: 100%; padding: 10px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 14px; outline: none; }
.form-control:focus { border-color: #2b4360; }
.form-control:disabled { background: #f1f5f9; cursor: not-allowed; }
.row-2 { display: flex; gap: 15px; }
.row-2 > * { flex: 1; }
.input-group { display: flex; }
.input-group input { border-top-right-radius: 0; border-bottom-right-radius: 0; }
.unit-select { border: 1px solid #cbd5e1; border-left: none; background: #f8fafc; padding: 0 10px; border-top-right-radius: 6px; border-bottom-right-radius: 6px; font-weight: 500; }
.radio-group { display: flex; gap: 20px; margin-top: 8px; }
.radio-item { display: flex; align-items: center; cursor: pointer; font-size: 14px; }
.radio-item input { margin-right: 8px; accent-color: #2b4360; }
.btn-submit { width: 100%; background: #2b4360; color: #fff; padding: 12px; border: none; border-radius: 6px; font-weight: 500; cursor: pointer; margin-top: 15px; font-size: 15px; }
.btn-submit:hover { background: #1e2f45; }

/* Right Panel Customer List */
.panel-header { margin-bottom: 15px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
.panel-header h4 { margin: 0; color: #2b4360; font-size: 16px; }
.note { font-size: 12px; color: #64748b; margin-top: 5px; display: block; }
.text-primary { color: #2563eb; }
.search-box { margin-bottom: 10px; }

.customer-list { max-height: 500px; overflow-y: auto; border: 1px solid #eee; border-radius: 4px; }
/* Làm mờ khi không chọn cá nhân */
.disabled-list { opacity: 0.5; pointer-events: none; background: #f9fafb; }

.mini-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.mini-table th { text-align: left; padding: 10px; background: #E9F1FB; color: #1E3A8A; border-bottom: 1px solid #e2e8f0; position: sticky; top: 0; font-weight: 700; }
.mini-table td { padding: 10px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.active-row { background-color: #eff6ff; }

.cust-name { font-weight: 400; color: #334155; }
.cust-email { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.text-center { text-align: center; }

/* Mini Pagination */
.mini-pagination { display: flex; justify-content: center; align-items: center; gap: 10px; margin-top: 15px; font-size: 13px; color: #64748b; }
.mini-btn { width: 28px; height: 28px; border: 1px solid #ddd; background: #fff; border-radius: 4px; cursor: pointer; }
.mini-btn:disabled { color: #ccc; cursor: not-allowed; }
.mini-btn:hover:not(:disabled) { border-color: #2b4360; color: #2b4360; }
</style>