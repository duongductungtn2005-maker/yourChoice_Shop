<template>
  <div class="page-container">
    <h1 class="page-title">Quản lý Phiếu giảm giá</h1>

    <div class="control-panel">
      <div class="controls-row">
        
        <div class="filter-group">
           <div class="search-box">
              <i class="fas fa-magnifying-glass search-icon"></i>
              <input class="input-den" v-model="filter.keyword" placeholder="Tìm kiếm theo mã, tên..." @keyup.enter="fetchData" />
           </div>

           <div class="date-group">
              <div class="date-input-wrapper">
                 <input class="input-den" type="text" onfocus="(this.type='datetime-local')" onblur="(this.type='text')" placeholder="Ngày bắt đầu" v-model="filter.startDate">
              </div>
              <span class="divider">-</span>
              <div class="date-input-wrapper">
                 <input class="input-den" type="text" onfocus="(this.type='datetime-local')" onblur="(this.type='text')" placeholder="Ngày kết thúc" v-model="filter.endDate">
              </div>
           </div>
           
           <select v-model="filter.scope" @change="fetchData" class="form-select">
              <option value="">-- Kiểu --</option>
              <option value="CongKhai">Công khai</option>
              <option value="CaNhan">Cá nhân</option>
           </select>

           <select v-model="filter.status" @change="fetchData" class="form-select">
              <option value="">-- Trạng thái --</option>
              <option value="1">Đang hoạt động</option>
              <option value="0">Ngưng hoạt động</option>
           </select>
        </div>

        <div class="action-group">
           <button class="btn btn-navy" @click="resetFilter">
              <i class="fas fa-sync-alt"></i> Đặt lại
           </button>

           <button class="btn btn-outline" @click="exportExcel">
              <font-awesome-icon :icon="['fas','file-excel']" /> Xuất Excel
           </button>
           
           <router-link :to="{ name: 'admin-voucher-create' }" class="btn btn-gradient">
              <i class="fas fa-plus"></i> Tạo mới
           </router-link>
        </div>

      </div>
    </div>

    <div class="table-container">
      <table class="custom-table">
        <thead>
          <tr>
            <th>STT</th>
            <th>Mã</th>
            <th>Tên phiếu</th>
            <th>Kiểu</th>
            <th class="text-center">Số lượng</th>
            <th>Giá trị</th>
            <th>Thời gian</th>
            <th>Trạng thái</th>
            <th class="text-center">Hành động</th> 
          </tr>
        </thead>
        <tbody>
          <tr v-if="list.length === 0">
             <td colspan="9" class="text-center empty-state">Không có dữ liệu</td>
          </tr>
          <tr v-for="(item, index) in list" :key="item.id" :class="{'row-disabled': item.trangThai === 0}">
            
            <td>{{ (currentPage * pageSize) + index + 1 }}</td>
            <td class="code-text">{{ item.maPhieuGiamGia }}</td>
            <td class="code-text">{{ item.tenPhieuGiamGia }}</td>
            
            <td>
              <span class="badge" :class="getScope(item).class">
                  {{ getScope(item).label }}
              </span>
            </td>

            <td class="text-center">{{ item.soLuong }}</td>
            
            <td>
                {{ item.loaiPhieu === 'PhanTram' ? item.giaTriGiam + '%' : formatCurrency(item.giaTriGiam) }}
            </td>

            <td class="time-col">
              <div>{{ formatDate(item.ngayBatDau) }}</div>
              <div>{{ formatDate(item.ngayKetThuc) }}</div>
            </td>

            <td>
                <span class="badge" :class="getStatusClass(item)">{{ getStatusLabel(item) }}</span>
            </td>
            
            <td class="text-center action-col">
              <div class="action-wrapper"> 
                  <button 
                    v-if="getScope(item).isPrivate && !isExpired(item.ngayKetThuc)" 
                    class="icon-btn" title="Gửi mail" @click="openSendMailModal(item)"
                  >
                    <i class="far fa-envelope"></i>
                  </button>

                  <label class="switch" title="Bật/Tắt trạng thái">
                      <input 
                        type="checkbox" 
                        :checked="item.trangThai === 1" 
                        :disabled="!canReactivate(item) && item.trangThai === 0"
                        @click="handleToggleStatus(item, $event)"
                      >
                      <span class="slider round"></span>
                  </label>

                  <button class="icon-btn" title="Chi tiết/Sửa" @click="editVoucher(item)">
                    <i class="far fa-eye"></i>
                  </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div class="pagination-footer">
        <div class="page-info">
            Hiển thị 
            <select v-model="pageSize" @change="handlePageSizeChange">
               <option :value="5">5</option>
               <option :value="10">10</option>
            </select> 
            phiếu / trang
        </div>
        <div class="page-controls">
            <button :disabled="currentPage === 0" @click="changePage(currentPage - 1)">‹</button>
            <button 
              v-for="p in visiblePages" 
              :key="p" 
              :class="{ active: p === currentPage + 1 }" 
              @click="changePage(p - 1)"
            >
              {{ p }}
            </button>
            <button :disabled="currentPage >= totalPages - 1" @click="changePage(currentPage + 1)">›</button>
        </div>
      </div>
    </div>

    <div v-if="showMailModal" class="modal-overlay" @click.self="showMailModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Gửi mã: <span style="color:#2b4360">{{ selectedVoucher?.maPhieuGiamGia }}</span></h3>
          <button @click="showMailModal = false" class="close-btn"><i class="fas fa-times"></i></button>
        </div>
        <div class="modal-body">
           <div class="search-box" style="width:100%; margin-bottom:15px;">
              <i class="fas fa-magnifying-glass search-icon"></i>
              <input v-model="customerKeyword" placeholder="Tìm email khách hàng..." />
           </div>
           <div class="customer-list-box">
              <table class="custom-table">
                  <thead>
                      <tr><th><input type="checkbox" @change="toggleAll"></th><th>Tên</th><th>Email</th></tr>
                  </thead>
                  <tbody>
                      <tr v-for="c in filteredCustomers" :key="c.id">
                          <td><input type="checkbox" :value="c.email" v-model="selectedEmails"></td>
                          <td>{{ c.hoTen }}</td><td>{{ c.email }}</td>
                      </tr>
                  </tbody>
              </table>
           </div>
           <div class="modal-actions">
              <span>Đã chọn: <b>{{ selectedEmails.length }}</b></span>
              <button class="btn btn-gradient" @click="confirmSendMail" :disabled="selectedEmails.length===0"><i class="fas fa-paper-plane"></i> Gửi ngay</button>
           </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue';
import request from '@/services/request';
import Swal from 'sweetalert2';
import { useRouter } from 'vue-router'; 

const router = useRouter(); 
const list = ref([]);
const filter = ref({ keyword: '', startDate: '', endDate: '', scope: '', status: '' });
const currentPage = ref(0);
const pageSize = ref(5);
const totalPages = ref(0);

const showMailModal = ref(false);
const selectedVoucher = ref(null);
const customerList = ref([]);
const customerKeyword = ref('');
const selectedEmails = ref([]);

const isExpired = (date) => new Date(date) < new Date();

const getScope = (item) => {
    const isPrivate = item.kieu === 'CaNhan' || item.kieu === 1 || item.kieu === '1';
    return {
        isPrivate: isPrivate,
        label: isPrivate ? 'Cá nhân' : 'Công khai',
        class: isPrivate ? 'badge-private' : 'badge-public' 
    };
};

const getStatusLabel = (item) => item.trangThai === 0 ? 'Ngưng' : (isExpired(item.ngayKetThuc) ? 'Hết hạn' : 'Đang diễn ra');
const getStatusClass = (item) => item.trangThai === 0 ? 'badge-stopped' : (isExpired(item.ngayKetThuc) ? 'badge-expired' : 'badge-active');

const canReactivate = (item) => getScope(item).isPrivate ? !isExpired(item.ngayKetThuc) : true;

const fetchData = async () => {
    try {
        const params = { ...filter.value, page: currentPage.value, size: pageSize.value };
        const res = await request.get('/phieu-giam-gia', { params });
        list.value = res.data.content;
        totalPages.value = res.data.totalPages;
    } catch (e) { console.error(e); }
};

const resetFilter = () => {
    filter.value = { keyword: '', startDate: '', endDate: '', scope: '', status: '' };
    currentPage.value = 0;
    fetchData();
};

const editVoucher = (item) => {
    // console.log("Edit voucher:", item.id);
    // Swal.fire("Tính năng", "Chức năng sửa phiếu giảm giá ID: " + item.id, "info");
    // Nếu đã có route sửa thì uncomment dòng dưới:
    // router.push({ name: 'admin-voucher-edit', params: { id: item.id } });
};

const handleToggleStatus = async (item, event) => {
    event.preventDefault();
    const currentStatus = item.trangThai;
    const newStatus = currentStatus === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? 'Kích hoạt' : 'Ngừng hoạt động';
    const confirmBtnColor = newStatus === 1 ? '#10b981' : '#ef4444';

    const result = await Swal.fire({
        title: `Xác nhận ${actionText}?`,
        text: `Bạn có muốn ${actionText.toLowerCase()} phiếu "${item.tenPhieuGiamGia}"?`,
        icon: 'question', showCancelButton: true, confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy', confirmButtonColor: confirmBtnColor
    });

    if (result.isConfirmed) {
        try {
            let payload = {};
            if (newStatus === 1 && isExpired(item.ngayKetThuc)) {
                 const scope = getScope(item);
                 if (!scope.isPrivate) {
                    const { value: dateStr } = await Swal.fire({
                        title: 'Gia hạn phiếu', text: 'Phiếu đã hết hạn. Chọn ngày kết thúc mới:',
                        input: 'datetime-local', inputValue: '2026-12-31T23:59',
                        showCancelButton: true, confirmButtonText: 'Lưu & Kích hoạt'
                    });
                    if (!dateStr) return;
                    payload.newEndDate = dateStr;
                 } else {
                    return Swal.fire('Lỗi', 'Phiếu cá nhân hết hạn không thể kích hoạt lại.', 'error');
                 }
            }
            await request.put(`/phieu-giam-gia/${item.id}/toggle`, payload);
            item.trangThai = newStatus;
            if(payload.newEndDate) item.ngayKetThuc = payload.newEndDate;
            Swal.fire({ icon: 'success', title: 'Thành công', toast: true, position: 'top-end', showConfirmButton: false, timer: 1500 });
        } catch (e) { Swal.fire({ icon: 'error', title: 'Lỗi', text: e.response?.data?.message || 'Lỗi hệ thống' }); }
    }
};

const openSendMailModal = async (item) => {
    selectedVoucher.value = item;
    selectedEmails.value = [];
    customerList.value = [];
    try {
        const res = await request.get(`/phieu-giam-gia/${item.id}/customers`);
        customerList.value = res.data;
        selectedEmails.value = customerList.value.map(c => c.email);
        showMailModal.value = true;
    } catch (e) { alert("Không thể tải danh sách khách hàng."); }
};

const filteredCustomers = computed(() => customerList.value.filter(c => c.email.includes(customerKeyword.value) || c.hoTen.includes(customerKeyword.value)));
const toggleAll = (e) => selectedEmails.value = e.target.checked ? filteredCustomers.value.map(c => c.email) : [];

const visiblePages = computed(() => {
    const pages = [];
    const current = currentPage.value + 1;
    for (let i = 1; i <= totalPages.value; i++) {
        if (i === 1 || i === totalPages.value || (i >= current - 1 && i <= current + 1)) pages.push(i);
    }
    return pages;
});

const confirmSendMail = async () => {
    if(!confirm('Gửi mail?')) return;
    try {
        await request.post(`/phieu-giam-gia/${selectedVoucher.value.id}/send-mail`, { emails: selectedEmails.value });
        alert('Đã gửi mail!'); showMailModal.value = false;
    } catch(e) { alert('Lỗi gửi mail'); }
};

const changePage = (p) => { currentPage.value = p; fetchData(); };
const handlePageSizeChange = () => { currentPage.value = 0; fetchData(); };
const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
const formatDate = (val) => val ? new Date(val).toLocaleString('vi-VN') : '';
const exportExcel = async () => {
  const result = await Swal.fire({
    title: 'Xác nhận',
    text: 'Bạn có muốn tải xuống danh sách phiếu giảm giá không?',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Có',
    cancelButtonText: 'Hủy'
  });
  if (!result.isConfirmed) return;

  try {
    const params = { keyword: filter.value.keyword, startDate: filter.value.startDate || null, endDate: filter.value.endDate || null, scope: filter.value.scope || null, status: filter.value.status || null };
    const res = await request.get('/phieu-giam-gia/export', { params, responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `DS_PhongPhieu_${new Date().toISOString().slice(0,10)}.xlsx`);
    document.body.appendChild(link); link.click(); document.body.removeChild(link); window.URL.revokeObjectURL(url);

    const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 1500 });
    Toast.fire({ icon: 'success', title: 'Xuất Excel thành công' });
  } catch (e) {
    console.error(e);
    Swal.fire('Lỗi', 'Không thể tải file', 'error');
  }
};

onMounted(() => {
    // Kiểm tra và hiển thị thông báo nếu có từ localStorage (dùng cho Create xong redirect về)
    const successMsg = localStorage.getItem('voucherSuccessMessage');
    if (successMsg) {
        const Toast = Swal.mixin({
            toast: true,
            position: 'top-end',
            showConfirmButton: false,
            timer: 3000,
            timerProgressBar: true
        });
        Toast.fire({ icon: 'success', title: successMsg });
        localStorage.removeItem('voucherSuccessMessage');
    }
    fetchData();
});
</script>

<style scoped>
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background: #f8f9fa; min-height: 100vh; color: #333; font-size: 14px; }
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }

/* === CARD STYLING === */
.control-panel, .table-container { 
    background: white; 
    border-radius: 16px; 
    border: 1px solid #bfdbfe !important; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    margin-bottom: 20px;
    padding: 24px; /* Tăng padding để thoáng hơn */
}
.table-container { padding: 0; overflow: hidden; }

/* === FLEX LAYOUT CHO CONTROLS === */
.controls-row { 
    display: flex; 
    justify-content: space-between; /* Đẩy 2 nhóm sang 2 bên */
    align-items: center; 
    flex-wrap: wrap; 
    gap: 15px; 
}

/* Nhóm Bộ lọc (Bên trái) */
.filter-group { 
    display: flex; 
    gap: 12px; 
    align-items: center; 
    flex-wrap: wrap; 
}

/* Nhóm Nút bấm (Bên phải) */
.action-group { 
    display: flex; 
    gap: 10px; 
}

/* INPUTS & SELECTS */
.search-box { position: relative; width: 250px; } /* Thu nhỏ search box một chút */
.search-icon { position: absolute; left: 12px; top: 11px; color: #94a3b8; }
.search-box input { width: 100%; padding: 8px 10px 8px 36px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; height: 40px; }
.search-box input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }

.date-group { display: flex; gap: 8px; align-items: center; }
.date-input-wrapper input { padding: 8px 10px; width: 140px; height: 40px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; }
.form-select { height: 40px; border-radius: 6px; border: 1px solid #e2e8f0; outline: none; padding: 0 10px; color: #334155; min-width: 150px; cursor: pointer; }

/* === BUTTONS === */
.btn { 
    height: 40px; 
    padding: 0 20px; 
    border-radius: 6px; 
    font-weight: 600; 
    cursor: pointer; 
    font-size: 13px; 
    border: 1px solid transparent; 
    transition: 0.2s; 
    display: inline-flex; 
    align-items: center; 
    gap: 8px; 
    text-decoration: none;
}

.btn-secondary { background: #334155; color: #fff; } 
.btn-secondary:hover { background: #1e293b; }

.btn-outline { background: #fff; border: 1px solid #e2e8f0; color: #475569; }
.btn-outline:hover { background: #f8fafc; border-color: #cbd5e1; }

.btn-gradient { 
    background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
    color: #fff; 
    box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); 
}
.btn-gradient:hover { 
    transform: translateY(-1px); 
    box-shadow: 0 6px 15px rgba(15, 23, 42, 0.3); 
}

/* === TABLE STYLES === */
.custom-table { width: 100%; border-collapse: collapse; }
.custom-table th {
  background: #eff6ff !important; /* Xanh nhạt */
  color: #1e40af;
  padding: 16px;
  text-align: center;
  
  font-weight: 700;
  text-transform: uppercase;
  border-bottom: none !important; /* Xóa dòng kẻ */
  white-space: nowrap;
}

.custom-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
  text-align: center;
  vertical-align: middle;
  font-size: 14px;
  font-weight: 400;
  color: #333;
}

/* BADGES */
.badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; white-space: nowrap; }
.badge-public, .badge-active { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.badge-stopped { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
.badge-expired { background: #f1f5f9; color: #94a3b8; border: 1px solid #e2e8f0; }
.badge-private, .badge-stopped, .badge-expired { background: #f3f4f6; color: #4b5563; border: 1px solid #e5e7eb; }

/* ACTIONS */
.action-wrapper { display: flex; align-items: center; justify-content: center; gap: 10px; }
.icon-btn { width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; background: white; border: 1px solid #e2e8f0; border-radius: 6px; cursor: pointer; color: #64748b; transition: all 0.2s; }
.icon-btn:hover { background: #f1f5f9; color: #0f172a; border-color: #cbd5e1; }

.switch { position: relative; display: inline-block; width: 36px; height: 20px; margin: 0; flex-shrink: 0; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #cbd5e1; transition: .4s; border-radius: 34px; }
.slider:before { position: absolute; content: ""; height: 14px; width: 14px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; box-shadow: 0 2px 4px 0 rgba(0,0,0,0.2); }
input:checked + .slider { background-color: #10b981; }
input:checked + .slider:before { transform: translateX(16px); }
input:disabled + .slider { background-color: #e2e8f0; cursor: not-allowed; }

/* PAGINATION */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding: 15px 24px; border-top: 1px solid #f1f5f9; }
.page-info { font-size: 13px; color: #64748b; font-weight: 500; }
.page-info select { border: 1px solid #e2e8f0; border-radius: 4px; padding: 2px 5px; margin: 0 5px; outline: none; cursor: pointer; }
.page-controls button { width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; margin-left: 5px; cursor: pointer; color: #64748b; }
.page-controls button.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.page-controls button:disabled { opacity: 0.5; cursor: not-allowed; }
.empty-state { padding: 40px; font-size: 14px; color: #64748b; font-style: italic; }

/* MODAL */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: white; width: 600px; padding: 20px; border-radius: 8px; }
.modal-header { display: flex; justify-content: space-between; margin-bottom: 15px; }
.customer-list-box { max-height: 300px; overflow-y: auto; border: 1px solid #eee; margin-bottom: 15px; }
.modal-actions { display: flex; justify-content: space-between; align-items: center; }
.close-btn { border: none; background: none; font-size: 18px; cursor: pointer; color: #64748b; }
.btn-navy {
    background-color: #0f172a; /* Xanh than đậm */
    color: #ffffff;
    box-shadow: 0 4px 6px rgba(15, 23, 42, 0.2);
}
.btn-navy:hover {
    background-color: #1e293b;
    transform: translateY(-1px);
}
/* Màu chữ placeholder đen xì, rõ nét */
.input-den::placeholder {
    color: #000000 !important;  /* Màu đen */
    opacity: 1 !important;      /* Chống mờ */
    font-weight: 500;           /* Đậm lên tí cho dễ đọc (tùy chọn) */
}
</style>