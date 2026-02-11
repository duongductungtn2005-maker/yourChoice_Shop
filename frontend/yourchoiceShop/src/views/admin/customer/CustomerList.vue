<template>
  <div class="customer-page">
    
    <div class="header-section">
      <h1 class="page-title">Quản lý tài khoản / Quản lý khách hàng</h1>
    </div>

    <div class="card filter-card">
      <div class="filter-wrapper">
        <div class="search-area">
           <div class="input-modern">
              <i class="fas fa-search icon"></i>
              <input 
                type="text" 
                v-model="filter.keyword" 
                placeholder="Tìm kiếm theo tên, SĐT, email..." 
                @keyup.enter="fetchData"
              >
           </div>
        </div>

        <div class="controls-row">
            <div class="filter-group">
               <div class="select-modern">
                  <span class="label-text">Giới tính:</span>
                  <select v-model="filter.gioiTinh" @change="fetchData">
                     <option :value="null">Tất cả</option>
                     <option :value="true">Nam</option>
                     <option :value="false">Nữ</option>
                  </select>
               </div>
               
               <div class="select-modern">
                  <span class="label-text">Trạng thái:</span>
                  <select v-model="filter.trangThai" @change="fetchData">
                     <option :value="null">Tất cả</option>
                     <option :value="1">Hoạt động</option>
                     <option :value="0">Ngừng hoạt động</option>
                  </select>
               </div>
            </div>

            <div class="action-buttons">
               <button class="btn btn-secondary" @click="resetFilter">
                  <i class="fas fa-sync-alt"></i> Đặt lại
               </button>
               <button class="btn btn-outline" @click="exportExcel">
                  <i class="fas fa-file-excel"></i> Xuất Excel
               </button>
               <button class="btn btn-gradient" @click="$router.push({ name: 'admin-customer-create' })">
                  <i class="fas fa-plus"></i> Thêm khách hàng
               </button>
            </div>
        </div>
      </div>
    </div>

    <div class="card table-card">
      <div class="table-responsive">
        <table>
          <thead>
            <tr>
              <th class="text-center" width="5%">STT</th>
              <th width="10%">Mã KH</th>
              <th width="15%">Họ tên</th>
              <th width="10%">SĐT</th>
              <th width="15%">Email</th>
              <th width="25%">Địa chỉ</th>
              <th class="text-center" width="8%">Giới tính</th>
              <th class="text-center" width="12%">Trạng thái</th>
              <th class="text-center" width="10%">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading"><td colspan="9" class="text-center py-5">Đang tải dữ liệu...</td></tr>
            <tr v-else-if="items.length === 0"><td colspan="9" class="text-center py-5 empty-text">Không tìm thấy dữ liệu.</td></tr>
            
            <tr v-else v-for="(item, index) in items" :key="item.id">
              <td class="text-center">{{ (page - 1) * pageSize + index + 1 }}</td>
              <td class="text-code">{{ item.maKhachHang }}</td>
              <td class="font-bold text-dark">{{ item.tenKhachHang }}</td>
              <td>{{ item.soDienThoai }}</td>
              <td class="text-gray">{{ item.email }}</td>
              
              <td class="text-address">
                 <span class="truncate-text" :title="getAddressString(item)">
                    {{ getAddressString(item) }}
                 </span>
              </td>

              <td class="text-center">{{ item.gioiTinh === true ? 'Nam' : 'Nữ' }}</td>
              
              <td class="text-center">
                 <span :class="['status-badge', item.trangThai === 1 ? 'active' : 'inactive']">
                    {{ item.trangThai === 1 ? 'Hoạt động' : 'Ngừng' }}
                 </span>
              </td>

              <td class="text-center">
                 <div class="action-cell">
                    <label class="switch" title="Bật/Tắt">
                        <input type="checkbox" :checked="item.trangThai === 1" @click="toggleStatus(item, $event)">
                        <span class="slider round"></span>
                    </label>
                    <button class="btn-icon-eye" @click="viewDetail(item)" title="Chi tiết">
                        <i class="far fa-eye"></i>
                    </button>
                 </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-footer">
         <div class="page-info">
            Hiển thị 
            <select v-model="pageSize" @change="handlePageSizeChange">
               <option :value="5">5</option>
               <option :value="10">10</option>
               <option :value="20">20</option>
            </select> 
            bản ghi
         </div>
         
         <div class="page-nav">
            <button class="nav-btn" :disabled="page === 1" @click="changePage(page - 1)">
               <i class="fas fa-chevron-left"></i>
            </button>
            <div class="page-numbers">
               <button 
                  v-for="p in visiblePages" 
                  :key="p" 
                  :class="['nav-number', { active: p === page }]" 
                  @click="changePage(p)"
               >
                  {{ p }}
               </button>
            </div>
            <button class="nav-btn" :disabled="page === totalPages" @click="changePage(page + 1)">
               <i class="fas fa-chevron-right"></i>
            </button>
         </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue';
import request from '@/services/request'; 
import Swal from 'sweetalert2';
import { useRouter } from 'vue-router';
import { toastSuccess, toastError } from '@/utils/toast';

const router = useRouter();
const items = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);
const filter = reactive({ keyword: '', gioiTinh: null, trangThai: null });
const API_URL = '/khach-hang'; 

// FETCH DATA
const fetchData = async () => {
    loading.value = true;
    try {
        const res = await request.get(API_URL, {
            params: {
                page: page.value - 1, size: pageSize.value,
                keyword: filter.keyword, gioiTinh: filter.gioiTinh, trangThai: filter.trangThai
            }
        });
        items.value = res.data.content;
        totalPages.value = res.data.totalPages;
    } catch (e) { console.error(e); } finally { loading.value = false; }
};

const getAddressString = (item) => {
    let list = item.listDiaChi || item.addresses || item.diaChiKhachHangList;
    if (typeof list === 'string') {
        try { list = JSON.parse(list); } catch (e) { return '-'; }
    }
    if (Array.isArray(list) && list.length > 0) {
        let addr = list.find(a => a.macDinh === true || a.macDinh === 1 || a.macDinh === 'true');
        if (!addr) addr = list.find(a => a.trangThai === 1) || list[0];
        if (!addr) return 'Chưa có địa chỉ';

        const street = addr.diaChiCuThe || addr.diaChiNhanHang || '';
        const ward = addr.phuong || addr.tenXa || '';
        const district = addr.quan || addr.tenHuyen || '';
        const city = addr.thanhPho || addr.tenTinh || '';

        const parts = [street, ward, district, city].filter(p => p && String(p).trim() !== '');
        return parts.length > 0 ? parts.join(', ') : 'Địa chỉ trống';
    }
    return '-'; 
};

const resetFilter = () => {
    filter.keyword = ''; filter.gioiTinh = null; filter.trangThai = null;
    page.value = 1; fetchData();
};

const toggleStatus = async (item, event) => {
    event.preventDefault(); 
    const newStatus = item.trangThai === 1 ? 0 : 1;
    const result = await Swal.fire({
        title: 'Xác nhận thay đổi?',
        text: `Bạn có muốn đổi trạng thái khách hàng "${item.tenKhachHang}"?`,
        icon: 'question', showCancelButton: true,
        confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy', confirmButtonColor: '#0f172a'
    });

    if (result.isConfirmed) {
        try {
            await request.put(`${API_URL}/${item.id}/trang-thai`, null, { params: { trangThai: newStatus } });
            item.trangThai = newStatus;
            toastSuccess('Cập nhật trạng thái thành công!');
        } catch (e) { toastError('Lỗi cập nhật trạng thái'); }
    }
};

const exportExcel = async () => {
    const result = await Swal.fire({
        title: 'Xác nhận', text: 'Tải xuống danh sách khách hàng?',
        icon: 'question', showCancelButton: true, confirmButtonText: 'Có', cancelButtonText: 'Hủy'
    });
    if (!result.isConfirmed) return;

    try {
        const response = await request.get(`${API_URL}/export`, {
            params: { keyword: filter.keyword, gioiTinh: filter.gioiTinh, trangThai: filter.trangThai },
            responseType: 'blob'
        });
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a'); link.href = url;
        const dateStr = new Date().toISOString().slice(0,10);
        link.setAttribute('download', `DS_KhachHang_${dateStr}.xlsx`);
        document.body.appendChild(link); link.click(); document.body.removeChild(link);
        toastSuccess('Xuất Excel thành công!');
    } catch (e) { toastError('Lỗi xuất file Excel'); }
};

const changePage = (p) => { if (p >= 1 && p <= totalPages.value) { page.value = p; fetchData(); } };
const handlePageSizeChange = () => { page.value = 1; fetchData(); };
const viewDetail = (item) => { router.push({ name: 'admin-customer-detail', params: { id: item.id } }); };

let searchTimer = null;
watch(() => filter.keyword, () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => { page.value = 1; fetchData(); }, 400);
});

const visiblePages = computed(() => {
    let p = [];
    for (let i = 1; i <= totalPages.value; i++) {
        if (i === 1 || i === totalPages.value || (i >= page.value - 1 && i <= page.value + 1)) p.push(i);
    }
    return p;
});

onMounted(() => { fetchData(); });
</script>

<style scoped>
.customer-page { font-family: 'Segoe UI', sans-serif; background-color: #f3f4f6; min-height: 100vh; padding: 20px; }
.header-section { margin-bottom: 20px; }
.page-title { font-size: 22px; font-weight: 700; color: #1e293b; }

/* === [MODIFIED] CARD STYLING === */
.card { 
    background: #fff; 
    border-radius: 16px; /* Bo tròn hơn */
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); 
    border: 1px solid #bfdbfe !important; /* Viền xanh dương nhạt (Quan trọng) */
    margin-bottom: 24px; 
}

.filter-card { padding: 24px; }
.filter-wrapper { display: flex; flex-direction: column; gap: 20px; }
.search-area { width: 100%; }
.input-modern, .select-modern { display: flex; align-items: center; border: 1px solid #e2e8f0; border-radius: 6px; padding: 0 12px; background: #fff; transition: 0.2s; height: 40px; }
.input-modern:focus-within, .select-modern:focus-within { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.input-modern input { border: none; outline: none; width: 100%; height: 100%; font-size: 14px; padding-left: 10px; color: #334155; }
.input-modern .icon { color: #94a3b8; font-size: 14px; }
.controls-row { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.filter-group { display: flex; gap: 15px; }
.select-modern { min-width: 200px; background: #f8fafc; }
.select-modern .label-text { font-size: 13px; font-weight: 600; color: #64748b; margin-right: 8px; white-space: nowrap; }
.select-modern select { border: none; outline: none; background: transparent; font-size: 14px; font-weight: 500; color: #334155; cursor: pointer; width: 100%; }

/* BUTTONS */
.action-buttons { display: flex; gap: 10px; }
.btn { padding: 0 20px; height: 40px; border-radius: 6px; font-weight: 600; font-size: 13px; cursor: pointer; display: inline-flex; align-items: center; gap: 8px; transition: 0.2s; border: none; }
.btn-secondary { background: #334155; color: #fff; } .btn-secondary:hover { background: #1e293b; }
.btn-outline { background: #fff; border: 1px solid #e2e8f0; color: #475569; } .btn-outline:hover { background: #f8fafc; border-color: #cbd5e1; }
.btn-gradient { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; box-shadow: 0 4px 10px rgba(15, 23, 42, 0.3); }
.btn-gradient:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.4); }

/* TABLE */
.table-card { padding: 0; overflow: hidden; }
.table-responsive { width: 100%; overflow-x: auto; }
table { width: 100%; border-collapse: collapse; table-layout: fixed; }

/* === [MODIFIED] TABLE HEADER === */
th { 
    background: #eff6ff; /* Nền xanh nhạt */
    padding: 16px; 
    text-align: left; 
    font-size: 12px; 
    font-weight: 700; 
    color: #1e40af; /* Chữ xanh đậm */
    text-transform: uppercase; 
    border-bottom: none !important; /* XÓA DÒNG KẺ NGANG */
    white-space: nowrap; 
}

td { padding: 14px 16px; border-bottom: 1px solid #f1f5f9; font-size: 14px; color: #334155; vertical-align: middle; height: 60px; }
.text-center { text-align: center; }
.text-code { font-family: monospace; font-weight: 700; color: #3b82f6; }
.font-bold { font-weight: 600; }
.text-gray { color: #64748b; font-size: 13px; }

/* Address Cell Style */
.text-address { max-width: 250px; font-size: 13px; color: #475569; line-height: 1.4; }
.truncate-text { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; text-overflow: ellipsis; white-space: normal; }

/* Status & Actions */
.status-badge { padding: 4px 12px; border-radius: 20px; font-size: 11px; font-weight: 600; white-space: nowrap; }
.active { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.inactive { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
.action-cell { display: flex; justify-content: center; align-items: center; gap: 12px; }
.btn-icon-eye { width: 32px; height: 32px; border-radius: 6px; background: #fff; border: 1px solid #e2e8f0; color: #334155; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; }
.btn-icon-eye:hover { background: #f1f5f9; border-color: #cbd5e1; }
.switch { position: relative; display: inline-block; width: 36px; height: 20px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #cbd5e1; transition: .4s; border-radius: 34px; }
.slider:before { position: absolute; content: ""; height: 14px; width: 14px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; box-shadow: 0 1px 3px rgba(0,0,0,0.3); }
input:checked + .slider { background-color: #10b981; }
input:checked + .slider:before { transform: translateX(16px); }

/* Pagination */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; padding: 15px 24px; border-top: 1px solid #e2e8f0; background: #fff; }
.page-info { font-size: 13px; color: #64748b; font-weight: 500; }
.page-info select { border: 1px solid #e2e8f0; border-radius: 4px; padding: 2px 5px; margin: 0 5px; outline: none; cursor: pointer; }
.page-nav { display: flex; align-items: center; gap: 8px; }
.page-numbers { display: flex; gap: 5px; }
.nav-btn, .nav-number { width: 32px; height: 32px; border: 1px solid #e2e8f0; border-radius: 6px; background: #fff; color: #64748b; font-size: 13px; font-weight: 500; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; }
.nav-btn:hover:not(:disabled), .nav-number:hover { background: #f8fafc; border-color: #cbd5e1; color: #0f172a; }
.nav-number.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.nav-btn:disabled { opacity: 0.5; cursor: not-allowed; background: #f1f5f9; }
</style>