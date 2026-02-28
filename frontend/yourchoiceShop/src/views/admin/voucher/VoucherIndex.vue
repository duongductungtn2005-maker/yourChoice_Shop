<template>
  <div class="page-container">
    <div class="header-section">
      <h1 class="page-title">Quản lý phiếu giảm giá</h1>
    </div>

    <div class="control-panel">
      <div class="filter-row">
            <div class="search-box">
              <i class="fas fa-magnifying-glass search-icon"></i>
              <input 
                class="input-den form-control"
                v-model="filter.keyword" 
                placeholder="Tìm theo tên hoặc mã..." 
                @keyup.enter="fetchData" 
              />
            </div>

            <div class="date-input-wrapper">
              <input 
                class="input-den form-control" 
                type="text" 
                onfocus="(this.type='datetime-local')" 
                onblur="(this.type='text')" 
                placeholder="Ngày bắt đầu" 
                v-model="filter.startDate"
              />
              <i class="far fa-calendar-alt date-icon"></i>
            </div>
            
            <div class="date-input-wrapper">
              <input 
                class="input-den form-control" 
                type="text" 
                onfocus="(this.type='datetime-local')" 
                onblur="(this.type='text')" 
                placeholder="Ngày kết thúc" 
                v-model="filter.endDate"
              />
              <i class="far fa-calendar-alt date-icon"></i>
            </div>
            
            <select v-model="filter.scope" @change="fetchData" class="form-select form-control">
              <option value="">-- Kiểu --</option>
              <option value="CongKhai">Công khai</option>
              <option value="CaNhan">Cá nhân</option>
            </select>

            <select v-model="filter.status" @change="fetchData" class="form-select form-control">
              <option value="">-- Trạng thái --</option>
              <option value="1">Đang hoạt động</option>
              <option value="0">Ngưng hoạt động</option>
            </select>
      </div>

      <div class="action-row">
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

    <div class="table-container">
      <table class="custom-table">
        <thead>
          <tr>
            <th width="50">STT</th>
            <th>Mã</th>
            <th>Tên phiếu</th>
            <th>Kiểu</th>
            <th>Số lượng</th>
            <th>Giá trị</th>
            <th>Ngày Bắt đầu</th>
            <th>Ngày Kết thúc</th>
            <th>Trạng thái</th>
            <th>Thao tác</th> 
          </tr>
        </thead>
        <tbody>
          <tr v-if="list.length === 0">
             <td colspan="10" class="empty-state">Không có dữ liệu</td>
          </tr>
          <tr v-for="(item, index) in list" :key="item.id" :class="{'row-disabled': item.trangThai === 0}">
            
            <td>{{ (currentPage * pageSize) + index + 1 }}</td>
            <td class="code-text">{{ item.maPhieuGiamGia }}</td>
            <td class="name-text">{{ item.tenPhieuGiamGia }}</td>
            
            <td>
              <span class="badge" :class="getScope(item).class">
                  {{ getScope(item).label }}
              </span>
            </td>

            <td>
                <span v-if="item.soLuong === null" class="infinity-text">Vô hạn</span>
                <span v-else>{{ item.soLuong }}</span>
            </td>
            
            <td class="font-bold" style="color: rgb(43, 67, 96);">
                {{ item.loaiPhieu === 'PhanTram' ? item.giaTriGiam + '%' : formatCurrency(item.giaTriGiam) }}
            </td>

            <td>{{ formatDate(item.ngayBatDau) }}</td>
            <td>{{ formatDate(item.ngayKetThuc) }}</td>

            <td>
                <span class="badge" :class="getStatusClass(item)">{{ getStatusLabel(item) }}</span>
            </td>
            
            <td class="action-col">
              <div class="action-wrapper"> 
                  <button 
                    class="icon-btn" 
                    title="Xem chi tiết" 
                    @click="editVoucher(item)"
                  >
                    <i class="fas fa-eye"></i>
                  </button>

                  <button 
                    v-if="getScope(item).isPrivate" 
                    class="icon-btn" 
                    title="Gửi Email" 
                    @click="openSendMailModal(item)"
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
          <button @click="showMailModal = false" class="close-btn"><i class="fas fa-times"></i></button>
        </div>
        <div class="modal-body">
           <div class="search-box" style="width:100%; margin-bottom:15px;">
              <i class="fas fa-magnifying-glass search-icon"></i>
              <input v-model="customerKeyword" placeholder="Tìm email khách hàng..." style="width:100%" />
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
// Bỏ loaiGiamGia khỏi filter
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

const getStatusLabel = (item) => {
    if (item.trangThai === 0 || isExpired(item.ngayKetThuc)) return 'Đã kết thúc'; // Gộp trạng thái dừng và hết hạn
    return 'Đang hoạt động';
};

const getStatusClass = (item) => {
    if (item.trangThai === 0 || isExpired(item.ngayKetThuc)) return 'badge-stopped'; // Màu xám/đỏ nhạt cho trạng thái dừng
    return 'badge-active';
};

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
    // Nhớ check lại tên route 'admin-voucher-edit' xem có đúng với file router của mày chưa nhé
    router.push({ name: 'admin-voucher-edit', params: { id: item.id } });
};

const handleToggleStatus = async (item, event) => {
    event.preventDefault();
    const currentStatus = item.trangThai;
    const newStatus = currentStatus === 1 ? 0 : 1;
    const scope = getScope(item);
    
    let payload = {};
    let sendEmailFlag = false;

    // TRƯỜNG HỢP 1: TẮT PHIẾU CÁ NHÂN (Hiện 3 nút: Có gửi mail, Không gửi, Hủy)
    if (newStatus === 0 && scope.isPrivate) {
        const result = await Swal.fire({
            title: `Ngừng hoạt động phiếu?`,
            text: `Bạn có muốn thông báo qua email cho khách hàng rằng phiếu "${item.tenPhieuGiamGia}" đã ngừng hoạt động không?`,
            icon: 'warning',
            showDenyButton: true,
            showCancelButton: true,
            confirmButtonText: '<i class="fas fa-paper-plane"></i> Tắt & Gửi Mail',
            denyButtonText: '<i class="fas fa-power-off"></i> Tắt (Không gửi)',
            cancelButtonText: 'Hủy bỏ',
            confirmButtonColor: '#2563eb',
            denyButtonColor: '#64748b'
        });

        if (result.isDismissed) return; // Nếu bấm Hủy hoặc click ra ngoài -> Dừng lại
        if (result.isConfirmed) sendEmailFlag = true; // Bấm Tắt & Gửi mail
        // Nếu result.isDenied (Bấm Tắt không gửi) thì sendEmailFlag vẫn = false
    } 
    // TRƯỜNG HỢP 2: BẬT PHIẾU HOẶC TẮT PHIẾU CÔNG KHAI (Chỉ hiện 2 nút mặc định)
    else {
        const actionText = newStatus === 1 ? 'Kích hoạt' : 'Ngừng hoạt động';
        const confirmBtnColor = newStatus === 1 ? '#10b981' : '#ef4444';
        
        const result = await Swal.fire({
            title: `Xác nhận ${actionText}?`,
            text: `Bạn có muốn ${actionText.toLowerCase()} phiếu "${item.tenPhieuGiamGia}"?`,
            icon: 'question', 
            showCancelButton: true, 
            confirmButtonText: 'Đồng ý', 
            cancelButtonText: 'Hủy', 
            confirmButtonColor: confirmBtnColor
        });

        if (!result.isConfirmed) return;

        // Logic gia hạn nếu bật lại phiếu công khai đã hết hạn
        if (newStatus === 1 && isExpired(item.ngayKetThuc)) {
             if (!scope.isPrivate) {
                const { value: dateStr } = await Swal.fire({
                    title: 'Gia hạn phiếu', 
                    text: 'Phiếu đã hết hạn. Chọn ngày kết thúc mới:',
                    input: 'datetime-local', 
                    inputValue: '2026-12-31T23:59',
                    showCancelButton: true, 
                    confirmButtonText: 'Lưu & Kích hoạt'
                });
                if (!dateStr) return;
                payload.newEndDate = dateStr;
             } else {
                return Swal.fire('Lỗi', 'Phiếu cá nhân hết hạn không thể kích hoạt lại.', 'error');
             }
        }
    }

    // Gắn cờ gửi mail vào payload để gửi xuống Backend
    payload.sendEmail = sendEmailFlag;

    try {
        await request.put(`/phieu-giam-gia/${item.id}/toggle`, payload);
        item.trangThai = newStatus;
        if(payload.newEndDate) item.ngayKetThuc = payload.newEndDate;
        
        const successMsg = sendEmailFlag ? 'Đã ngừng hoạt động và gửi mail thành công' : 'Đã cập nhật trạng thái thành công';
        Swal.fire({ icon: 'success', title: successMsg, toast: true, position: 'top-end', showConfirmButton: false, timer: 2000 });
    } catch (e) { 
        Swal.fire({ icon: 'error', title: 'Lỗi', text: e.response?.data?.message || 'Lỗi hệ thống' }); 
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
// Chỉ hiển thị ngày tháng năm
const formatDate = (val) => val ? new Date(val).toLocaleDateString('vi-VN') : ''; 
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
    link.setAttribute('download', `DS_PhieuGiamGia_${new Date().toISOString().slice(0,10)}.xlsx`);
    document.body.appendChild(link); link.click(); document.body.removeChild(link); window.URL.revokeObjectURL(url);

    const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 1500 });
    Toast.fire({ icon: 'success', title: 'Xuất Excel thành công' });
  } catch (e) {
    console.error(e);
    Swal.fire('Lỗi', 'Không thể tải file', 'error');
  }
};

onMounted(() => {
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
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background:#ebecee;min-height: 100vh; color: #333; font-size: 14px; }
.page-title { margin: 0; font-size: 24px; font-weight: 700; color: #1e293b; }
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
/* === CARD STYLING === */
.control-panel, .table-container { 
    background: white; border-radius: 16px; border: 1px solid #bfdbfe !important; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); margin-bottom: 20px; padding: 24px; 
}
.table-container { padding: 0; overflow: hidden; }

/* === GRID LAYOUT FOR FILTERS === */
/* Tạo Grid 5 cột bằng nhau cho các ô input (Tìm kiếm, Ngày BĐ, Ngày KT, Kiểu, Trạng thái) */
.filter-row { 
    display: grid;
    grid-template-columns: repeat(5, 1fr); 
    gap: 15px;
    width: 100%;
}

/* === FLEX LAYOUT FOR ACTIONS === */
/* Tạo dòng mới cho nút bấm, căn phải */
.action-row {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 15px;
}

/* CSS chung cho input và select để chúng đồng bộ chiều cao */
.form-control {
    width: 100%;
    height: 40px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    outline: none;
    padding: 0 10px;
    box-sizing: border-box; 
}

/* INPUTS & SELECTS */
.search-box { position: relative; width: 100%; } 
.search-icon { position: absolute; left: 12px; top: 12px; color: #94a3b8; }
.search-box input { padding-left: 36px; }

.search-box input:focus, .form-control:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }

/* DATE INPUT WITH ICON */
.date-input-wrapper { position: relative; width: 100%; }
.date-icon { 
    position: absolute; 
    right: 12px; 
    top: 50%;
    transform: translateY(-50%);
    color: #94a3b8; 
    pointer-events: none;
}
.date-input-wrapper input { padding-right: 35px; }

/* === BUTTONS === */
.btn { 
    height: 40px; 
    border-radius: 6px; 
    font-weight: 600; 
    cursor: pointer; 
    font-size: 13px; 
    border: 1px solid transparent; 
    transition: 0.2s; 
    display: inline-flex; 
    align-items: center; 
    justify-content: center; 
    gap: 8px; 
    text-decoration: none;
    min-width: 130px; 
}

.btn-navy {
    background-color: #334155; 
    color: #ffffff;
    box-shadow: 0 2px 4px rgba(51, 65, 85, 0.2);
}
.btn-navy:hover { background-color: #1e293b; transform: translateY(-1px); }

.btn-outline { background: #fff; border: 1px solid #cbd5e1; color: #475569; }
.btn-outline:hover { background: #f8fafc; border-color: #94a3b8; color: #0f172a; }

.btn-gradient { 
    background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
    color: #fff; 
    
}
.btn-gradient:hover { transform: translateY(-1px); }

/* === TABLE STYLES === */
.custom-table { width: 100%; border-collapse: separate; border-spacing: 0; }
.custom-table th {
  background:#f5f5f5;
  color: #333;
  padding: 12px;
  text-align: center; /* Căn giữa tiêu đề */
  font-weight: 700;
  font-size: 13px;
  border-bottom: 1px solid #f1f5f9;
  white-space: nowrap;
}

.custom-table td {
  padding: 16px 12px;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
  font-size: 14px;
  color: #334155;
  text-align: center; /* Căn giữa nội dung */
}

/* BADGES - MÀU SẮC YÊU CẦU */
.badge { padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 600; white-space: nowrap; display: inline-block;}
.badge-public, .badge-active { background: #ecfdf5; color: #059669; }
.badge-private, .badge-stopped, .badge-expired { background: #eff6ff; color: #2563eb; }
.badge-light { background: #f3f4f6; color: #4b5563; }

.code-text { font-family: monospace; font-weight: 600; color: #475569; }
.name-text { font-weight: 500; }
.font-bold { font-weight: 700; }
.infinity-text { font-style: italic; color: #64748b; font-weight: 500; }

/* ACTIONS */
/* Căn chỉnh 2 nút gần nhau và căn giữa */
.action-wrapper { display: flex; align-items: center; justify-content: center; gap: 8px; }

/* ĐÃ SỬA: Icon Button - Bo góc và có viền */
.icon-btn { 
    width: 32px; 
    height: 32px; 
    border: 1px solid #cbd5e1; /* Thêm viền */
    border-radius: 8px; /* Bo góc */
    background: white; 
    cursor: pointer; 
    color: #64748b; 
    font-size: 14px; 
    transition: 0.2s; 
    display: flex; 
    align-items: center; 
    justify-content: center;
}
.icon-btn:hover { 
    border-color: #3b82f6; 
    color: #3b82f6; 
}

/* Switch Toggle */
.switch { position: relative; display: inline-block; width: 40px; height: 22px; margin: 0; flex-shrink: 0; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #cbd5e1; transition: .4s; border-radius: 34px; }
.slider:before { position: absolute; content: ""; height: 16px; width: 16px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; box-shadow: 0 2px 4px 0 rgba(0,0,0,0.2); }
input:checked + .slider { background-color: #10b981; }
input:checked + .slider:before { transform: translateX(18px); }
input:disabled + .slider { background-color: #e2e8f0; cursor: not-allowed; }

/* PAGINATION */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; padding: 15px 24px; border-top: 1px solid #f1f5f9; }
.page-info select { border: 1px solid #e2e8f0; border-radius: 4px; padding: 2px 5px; margin: 0 5px; }
.page-controls button { width: 30px; height: 30px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; margin-left: 5px; cursor: pointer; color: #64748b; font-size: 12px; }
.page-controls button.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.page-controls button:disabled { opacity: 0.5; cursor: not-allowed; }

/* Placeholder */
.input-den::placeholder { color: #94a3b8 !important; opacity: 1 !important; font-weight: normal; }

/* Modal Styles */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: white; width: 600px; padding: 20px; border-radius: 8px; }
.modal-header { display: flex; justify-content: space-between; margin-bottom: 15px; }
.customer-list-box { max-height: 300px; overflow-y: auto; border: 1px solid #eee; margin-bottom: 15px; }
.modal-actions { display: flex; justify-content: space-between; align-items: center; }
.close-btn { border: none; background: none; font-size: 18px; cursor: pointer; color: #64748b; }
</style>
