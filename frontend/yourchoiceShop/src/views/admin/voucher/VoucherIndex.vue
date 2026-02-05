<template>
  <div class="page-container">
    <h1 class="page-title">Quản lý Phiếu giảm giá</h1>

    <div class="control-panel">
      <div class="action-row">
        <div class="search-box">
          <i class="fas fa-magnifying-glass search-icon"></i>
          <input v-model="filter.keyword" placeholder="Tìm kiếm theo mã, tên phiếu..." @keyup.enter="fetchData" />
        </div>
        <div class="action-group">
  <button @click="exportExcel" class="btn btn-outline">
    <font-awesome-icon :icon="['fas','file-excel']" /> Xuất Excel
  </button>
  
  <router-link :to="{ name: 'admin-voucher-create' }" class="btn btn-primary">
     <i class="fas fa-plus"></i> Tạo mới
  </router-link>
</div>
      </div>

      <div class="filter-row">
        <div class="date-group">
          <div class="date-input-wrapper">
             <i class="far fa-calendar date-icon"></i>
             <input type="text" onfocus="(this.type='datetime-local')" onblur="(this.type='text')" placeholder="Ngày bắt đầu" v-model="filter.startDate">
          </div>
          <span class="divider">-</span>
          <div class="date-input-wrapper">
             <i class="far fa-calendar date-icon"></i>
             <input type="text" onfocus="(this.type='datetime-local')" onblur="(this.type='text')" placeholder="Ngày kết thúc" v-model="filter.endDate">
          </div>
        </div>
        
        <select v-model="filter.scope" @change="fetchData" class="form-select">
          <option value="">-- Tất cả kiểu --</option>
          <option value="CongKhai">Công khai</option>
          <option value="CaNhan">Cá nhân</option>
        </select>

        <select v-model="filter.status" @change="fetchData" class="form-select">
          <option value="">-- Tất cả trạng thái --</option>
          <option value="1">Đang hoạt động</option>
          <option value="0">Ngưng hoạt động</option>
        </select>
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
            <th class="text-center">Hành động</th> </tr>
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
            <td><span class="badge" :class="getStatusClass(item)">{{ getStatusLabel(item) }}</span></td>
            
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
          <button @click="showMailModal = false" class="close-btn"><i class="fas fa-xmark"></i></button>
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
              <button class="btn btn-primary" @click="confirmSendMail" :disabled="selectedEmails.length===0"><i class="fas fa-paper-plane"></i> Gửi ngay</button>
           </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import request from '@/services/request';
import Swal from 'sweetalert2'; // Import Swal

const list = ref([]);
const filter = ref({ keyword: '', startDate: '', endDate: '', scope: '', status: '' });
const currentPage = ref(0);
const pageSize = ref(5);
const totalPages = ref(0);
// ĐÃ XÓA: selectedIds

// Modal Mail State
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

const canReactivate = (item) => getScope(item).isPrivate ? !isExpired(item.ngayKetThuc) : true;
const getStatusLabel = (item) => item.trangThai === 0 ? 'Ngưng' : (isExpired(item.ngayKetThuc) ? 'Hết hạn' : 'Đang diễn ra');
const getStatusClass = (item) => item.trangThai === 0 ? 'badge-stopped' : (isExpired(item.ngayKetThuc) ? 'badge-expired' : 'badge-active');

const fetchData = async () => {
    try {
        const params = { ...filter.value, page: currentPage.value, size: pageSize.value };
        const res = await request.get('/phieu-giam-gia', { params });
        list.value = res.data.content;
        totalPages.value = res.data.totalPages;
        // ĐÃ XÓA: Reset selectedIds
    } catch (e) { console.error(e); }
};

// ĐÃ XÓA: isAllSelected, toggleSelectAll, handleBulkDelete

const handleToggleStatus = async (item, event) => {
    event.preventDefault();

    const currentStatus = item.trangThai;
    const newStatus = currentStatus === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? 'Kích hoạt' : 'Ngừng hoạt động';
    const confirmBtnColor = newStatus === 1 ? '#10b981' : '#ef4444';

    const result = await Swal.fire({
        title: `<h3 style="color:#1e293b; font-size:18px;">Xác nhận ${actionText}?</h3>`,
        text: `Bạn có muốn ${actionText.toLowerCase()} phiếu "${item.tenPhieuGiamGia}"?`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy',
        confirmButtonColor: confirmBtnColor
    });

    if (result.isConfirmed) {
        try {
            let payload = {};
            if (newStatus === 1) {
                const scope = getScope(item);
                if (isExpired(item.ngayKetThuc)) {
                    if (!scope.isPrivate) {
                        const { value: dateStr } = await Swal.fire({
                            title: 'Gia hạn phiếu',
                            text: 'Phiếu đã hết hạn. Vui lòng chọn ngày kết thúc mới:',
                            input: 'datetime-local',
                            inputValue: '2026-12-31T23:59',
                            showCancelButton: true,
                            confirmButtonText: 'Lưu & Kích hoạt'
                        });
                        if (!dateStr) return;
                        payload.newEndDate = dateStr;
                    } else {
                        Swal.fire('Lỗi', 'Phiếu cá nhân đã hết hạn không thể kích hoạt lại.', 'error');
                        return;
                    }
                }
            }

            await request.put(`/phieu-giam-gia/${item.id}/toggle`, payload);

            item.trangThai = newStatus;
            if(payload.newEndDate) {
                item.ngayKetThuc = payload.newEndDate;
            }
            const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 1500 });
            Toast.fire({ icon: 'success', title: `Đã ${actionText.toLowerCase()} thành công!` });

        } catch (e) {
            console.error(e);
            Swal.fire({ icon: 'error', title: 'Lỗi', text: e.response?.data?.message || 'Lỗi hệ thống' });
        }
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
    } catch (e) {
        console.error(e);
        alert("Không thể tải danh sách khách hàng.");
    }
};

const filteredCustomers = computed(() => customerList.value.filter(c => c.email.includes(customerKeyword.value) || c.hoTen.includes(customerKeyword.value)));
const toggleAll = (e) => selectedEmails.value = e.target.checked ? filteredCustomers.value.map(c => c.email) : [];

// Trang hiển thị giống màn Sản phẩm
const visiblePages = computed(() => {
    const pages = [];
    const current = currentPage.value + 1; // currentPage là 0-based
    for (let i = 1; i <= totalPages.value; i++) {
        if (i === 1 || i === totalPages.value || (i >= current - 1 && i <= current + 1)) {
            pages.push(i);
        }
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

onMounted(fetchData);
</script>

<style scoped>
.action-group {
  display: flex;
  gap: 10px; /* Căn chỉnh khoảng cách nút */
}

.btn {
  padding: 8px 16px;
  border-radius: 4px; /* Bo góc nhẹ 4px giống mẫu */
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid transparent; /* Viền mặc định trong suốt */
  font-size: 14px;
  transition: all 0.2s;
}

/* Nút Tạo mới: Màu xanh đậm #0f172a */
.btn-primary {
  background: #0f172a; 
  color: #fff;
  border-color: #0f172a;
}
.btn-primary:hover {
  background: #333333; /* Xám đậm khi di chuột */
  border-color: #333333;
}
/* Nút Xuất Excel: Nền trắng, viền xám #cbd5e1 */
.btn-outline {
  background: #fff;
  border: 1px solid #cbd5e1;
  color: #475569;
}
.btn-outline:hover {
  background: #000000; /* Hover chuyển sang nền đen */
  color: #fff;          /* Chữ trắng */
  border-color: #000000;
}
/* Giữ nguyên CSS cũ */
.action-col {
  vertical-align: middle !important;
  padding: 8px !important;
  width: 150px; /* Cố định độ rộng để cột không bị co giãn */
}
.action-wrapper {
  display: flex !important;
  align-items: center !important;     /* Căn giữa dọc */
  justify-content: center !important; /* Căn giữa ngang */
  gap: 12px !important;               /* Khoảng cách giữa nút Mail và Switch */
  width: 100%;
}
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background: #f8f9fa; min-height: 100vh; color: #333; font-size: 14px; }
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }
.control-panel { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; }

/* Table typography (match CoAoIndex) */
.custom-table th {
  background: #E9F1FB;
  color: #1E3A8A;
  padding: 15px;
  text-align: center;
  color: #475569;
  font-size: 13px;
  font-weight: 700;
}
.custom-table td {
  padding: 15px;
  border-bottom: 1px solid #f1f5f9;
  text-align: center;
  vertical-align: middle;
  font-size: 14px;
  font-weight: 400;
  color: #333;
}
.code-text { color: #0f172a; font-weight: 400; }
.action-row { display: flex; justify-content: space-between; margin-bottom: 15px; }
.search-box { position: relative; width: 400px; }
.search-icon { position: absolute; left: 12px; top: 11px; color: #2b4360; }
.search-box input { width: 100%; padding: 10px 10px 10px 38px; border: 1px solid #cbd5e1; border-radius: 6px; }
.filter-row { display: flex; gap: 15px; }
.date-group { display: flex; gap: 8px; align-items: center; }
.date-input-wrapper { position: relative; }
.date-icon { position: absolute; left: 10px; top: 10px; color: #2b4360; pointer-events: none; }
.date-input-wrapper input { padding-left: 35px; width: 160px; height: 38px; border: 1px solid #cbd5e1; border-radius: 6px; }
.form-select { height: 38px; border-radius: 6px; border: 1px solid #cbd5e1; }
.table-container { background: white; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0; }
.custom-table { width: 100%; border-collapse: collapse; }
.custom-table th { 
  background: #E9F1FB; 
  color: #1E3A8A;
  padding: 15px; 
  text-align: center; /* SỬA: Đổi từ left sang center */
  font-weight: 700;
}

.custom-table td { 
  padding: 15px; 
  border-bottom: 1px solid #f1f5f9; 
  text-align: center; /* THÊM: Căn giữa nội dung các cột */
  vertical-align: middle;
  font-size: 14px;
  font-weight: 400;
}
.badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.badge-active { background: #dcfce7; color: #166534; }
.badge-stopped { background: #fee2e2; color: #991b1b; }
.badge-expired { background: #f1f5f9; color: #94a3b8; }
.badge-public { background: #e0f2fe; color: #0369a1; }
.badge-private { background: #f3e8ff; color: #7e22ce; }
.icon-btn {
  width: 34px;  /* Cố định kích thước để vuông vắn */
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  color: #2b4360;
  transition: all 0.2s;
  margin: 0 !important; /* Xóa margin cũ gây lệch */
}
.icon-btn:hover:not(:disabled) {
  background: #f1f5f9;
  color: #0f172a;
  border-color: #cbd5e1;
}
.btn { 
    height: 38px; /* Chiều cao cố định cho các nút bằng nhau */
    padding: 0 16px; 
    border-radius: 4px; 
    font-weight: 500; 
    cursor: pointer; 
    font-size: 14px; 
    border: 1px solid transparent; 
    transition: 0.2s;
    display: inline-flex; /* Flex để căn giữa icon và chữ */
    align-items: center;
    gap: 8px; /* Khoảng cách giữa icon và chữ */
    text-decoration: none;
}
.btn-primary {
  background: #000000; /* Đen tuyệt đối */
  color: #fff;
  border-color: #000000;
}
.btn-primary:hover { background: #333333; border-color: #333333; }
.btn-outline { 
    background: #fff; 
    border-color: #cbd5e1; 
    color: #475569; 
}
.btn-outline:hover { background: #f1f5f9; border-color: #94a3b8; }
.pagination-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding-top: 15px; border-top: 1px solid #f1f5f9; }
.page-info { font-size: 14px; color: #64748b; }
.page-info select { padding: 4px 8px; border: 1px solid #cbd5e1; border-radius: 4px; margin: 0 5px; outline: none; }
.page-controls button { width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; margin-left: 5px; cursor: pointer; color: #64748b; }
.page-controls button.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.page-controls button:disabled { opacity: 0.5; cursor: not-allowed; }
.font-bold { font-weight: 400; }
.text-center { text-align: center; }
.empty-state { padding: 40px; font-size: 14px; color: #64748b; font-style: italic; }

/* CSS Switch */
.switch {
  position: relative;
  display: inline-block;
  width: 36px;
  height: 20px;
  margin: 0 !important; /* Quan trọng: Xóa margin-left cũ */
  flex-shrink: 0;
  vertical-align: middle;
}

.switch input { 
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: #cbd5e1;
  transition: .4s;
  border-radius: 34px;
}
.slider:before {
  position: absolute;
  content: "";
  height: 14px;
  width: 14px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
  box-shadow: 0 2px 4px 0 rgba(0,0,0,0.2);
}

input:checked + .slider {
  background-color: #10b981;
}

input:checked + .slider:before {
  transform: translateX(16px);
}

input:disabled + .slider {
  background-color: #e2e8f0;
  cursor: not-allowed;
}

/* Modal Styles */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: white; width: 600px; padding: 20px; border-radius: 8px; }
.modal-header { display: flex; justify-content: space-between; margin-bottom: 15px; }
.customer-list-box { max-height: 300px; overflow-y: auto; border: 1px solid #eee; margin-bottom: 15px; }
.modal-actions { display: flex; justify-content: space-between; align-items: center; }
</style>