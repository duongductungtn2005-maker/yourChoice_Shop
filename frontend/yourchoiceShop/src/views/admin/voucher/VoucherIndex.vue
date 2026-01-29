<template>
  <div class="page-container">
    <h1 class="page-title">Quản lý Phiếu giảm giá</h1>

    <div class="control-panel">
      <div class="action-row">
        <div class="search-box">
          <i class="fas fa-magnifying-glass search-icon"></i>
          <input v-model="filter.keyword" placeholder="Tìm kiếm theo mã, tên phiếu..." @keyup.enter="fetchData" />
        </div>
        <div class="buttons">
          <button @click="exportExcel" class="btn btn-outline"><i class="fas fa-file-excel"></i> Xuất Excel</button>
          
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
            <th>Giá trị</th>
            <th>Thời gian</th>
            <th>Trạng thái</th>
            <th class="text-center">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in list" :key="item.id" :class="{'row-disabled': item.trangThai === 0}">
            <td>{{ (currentPage * pageSize) + index + 1 }}</td>
            <td class="code-text">{{ item.maPhieuGiamGia }}</td>
            <td style="font-weight: 600;">{{ item.tenPhieuGiamGia }}</td>
            <td>
  <span class="badge" :class="item.kieu === 'CaNhan' ? 'badge-private' : 'badge-public'">
      {{ item.kieu === 'CaNhan' ? 'Cá nhân' : 'Công khai' }}
  </span>
</td>
            <td>{{ formatValue(item) }}</td>
            <td class="time-col">
              <div>{{ formatDate(item.ngayBatDau) }}</div>
              <div>{{ formatDate(item.ngayKetThuc) }}</div>
            </td>
            <td><span class="badge" :class="getStatusClass(item)">{{ getStatusLabel(item) }}</span></td>
            <td class="text-center action-col">
              <button 
  v-if="item.kieu === 'CaNhan' && !isExpired(item.ngayKetThuc)" 
  class="icon-btn" title="Gửi mail" @click="openSendMailModal(item)"
>
  <i class="far fa-envelope"></i>
</button>
              <button v-if="item.trangThai === 1" class="icon-btn" title="Ngưng hoạt động" @click="toggleStatus(item)">
                <i class="fas fa-ban" style="color: #ef4444;"></i>
              </button>
              <button v-else class="icon-btn" title="Kích hoạt lại" :disabled="!canReactivate(item)" @click="toggleStatus(item)">
                <i class="fas fa-arrow-rotate-right" :style="{color: canReactivate(item) ? '#22c55e' : '#ccc'}"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <div class="pagination-footer">
        <div class="page-size-selector">
            Xem <select v-model="pageSize" @change="handlePageSizeChange"><option :value="5">5</option><option :value="10">10</option></select> sản phẩm
        </div>
        <div class="pagination-controls">
            <button class="page-btn" :disabled="currentPage === 0" @click="changePage(currentPage - 1)"><i class="fas fa-chevron-left"></i></button>
            <button class="page-btn active">{{ currentPage + 1 }}</button>
            <button class="page-btn" :disabled="currentPage >= totalPages - 1" @click="changePage(currentPage + 1)"><i class="fas fa-chevron-right"></i></button>
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

const list = ref([]);
const filter = ref({ keyword: '', startDate: '', endDate: '', scope: '', status: '' });
const currentPage = ref(0);
const pageSize = ref(5);
const totalPages = ref(0);

// Modal Mail State
const showMailModal = ref(false);
const selectedVoucher = ref(null);
const customerList = ref([
    { id: 1, hoTen: 'Nguyễn Văn A', email: 'khacha@gmail.com' },
    { id: 2, hoTen: 'Trần Thị B', email: 'khachb@gmail.com' },
    { id: 3, hoTen: 'Test Email', email: 'your_email@gmail.com' } // Thay bằng mail bạn
]);
const customerKeyword = ref('');
const selectedEmails = ref([]);

const isExpired = (date) => new Date(date) < new Date();
const canReactivate = (item) => item.kieu === 'CongKhai' ? true : !isExpired(item.ngayKetThuc);
const getStatusLabel = (item) => item.trangThai === 0 ? 'Ngưng' : (isExpired(item.ngayKetThuc) ? 'Hết hạn' : 'Đang diễn ra');
const getStatusClass = (item) => item.trangThai === 0 ? 'badge-stopped' : (isExpired(item.ngayKetThuc) ? 'badge-expired' : 'badge-active');

const fetchData = async () => {
    try {
        const params = { ...filter.value, page: currentPage.value, size: pageSize.value };
        const res = await request.get('/phieu-giam-gia', { params });
        list.value = res.data.content;
        totalPages.value = res.data.totalPages;
    } catch (e) { console.error(e); }
};

const toggleStatus = async (item) => {
    if (item.trangThai === 1) {
        if (!confirm('Ngưng hoạt động?')) return;
        await callApi(item.id, null);
    } else {
        let payload = {};
        if (item.kieu === 'CongKhai' && isExpired(item.ngayKetThuc)) {
            const date = prompt("Nhập ngày kết thúc mới (YYYY-MM-DDTHH:mm):", "2026-12-31T23:59");
            if (!date) return;
            payload.newEndDate = date;
        } else {
            if (!confirm('Kích hoạt lại?')) return;
        }
        await callApi(item.id, payload);
    }
};

const callApi = async (id, payload) => {
    try { await request.put(`/phieu-giam-gia/${id}/toggle`, payload); fetchData(); } catch (e) { alert(e.response?.data?.message); }
};

// Mail Logic
const openSendMailModal = (item) => {
    selectedVoucher.value = item;
    selectedEmails.value = [];
    showMailModal.value = true;
};
const filteredCustomers = computed(() => customerList.value.filter(c => c.email.includes(customerKeyword.value) || c.hoTen.includes(customerKeyword.value)));
const toggleAll = (e) => selectedEmails.value = e.target.checked ? filteredCustomers.value.map(c => c.email) : [];

const confirmSendMail = async () => {
    if(!confirm('Gửi mail?')) return;
    try {
        await request.post(`/phieu-giam-gia/${selectedVoucher.value.id}/send-mail`, { emails: selectedEmails.value });
        alert('Đã gửi mail!'); showMailModal.value = false;
    } catch(e) { alert('Lỗi gửi mail'); }
};

const changePage = (p) => { currentPage.value = p; fetchData(); };
const handlePageSizeChange = () => { currentPage.value = 0; fetchData(); };
const formatValue = (item) => item.loaiPhieu === 'PhanTram' ? item.giaTriGiam + '%' : item.giaTriGiam + ' đ';
const formatDate = (val) => val ? new Date(val).toLocaleString('vi-VN') : '';
const exportExcel = () => alert('Đang tải file...');

onMounted(fetchData);
</script>

<style scoped>
/* Style cũ giữ nguyên */
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background: #f8f9fa; min-height: 100vh; }
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }
.control-panel { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
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
.custom-table th { background: #f8fafc; padding: 15px; text-align: left; color: #475569; }
.custom-table td { padding: 15px; border-bottom: 1px solid #f1f5f9; }
.badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.badge-active { background: #dcfce7; color: #166534; }
.badge-stopped { background: #fee2e2; color: #991b1b; }
.badge-expired { background: #f1f5f9; color: #94a3b8; }
.badge-public { background: #e0f2fe; color: #0369a1; }
.badge-private { background: #f3e8ff; color: #7e22ce; }
.icon-btn { border: none; background: transparent; cursor: pointer; font-size: 16px; margin: 0 5px; }
.btn { padding: 8px 16px; border-radius: 6px; font-weight: 600; cursor: pointer; text-decoration: none; display: inline-flex; align-items: center; gap: 8px; }
.btn-primary { background: #2b4360; color: white; border: none; }
.btn-outline { background: white; border: 1px solid #2b4360; color: #2b4360; }
.pagination-footer { padding: 15px; display: flex; justify-content: space-between; border-top: 1px solid #f1f5f9; }
.page-btn { width: 30px; height: 30px; border-radius: 50%; border: 1px solid #e2e8f0; background: white; cursor: pointer; }
.page-btn.active { background: #dbeafe; color: #1e40af; border-color: #dbeafe; }

/* Modal Styles */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: white; width: 600px; padding: 20px; border-radius: 8px; }
.modal-header { display: flex; justify-content: space-between; margin-bottom: 15px; }
.customer-list-box { max-height: 300px; overflow-y: auto; border: 1px solid #eee; margin-bottom: 15px; }
.modal-actions { display: flex; justify-content: space-between; align-items: center; }
</style>