<template>
  <div class="page-container">
    <h1 class="page-title">Quản lý Đợt giảm giá</h1>

    <div class="control-panel">
      <div class="action-row">
        <div class="search-box">
          <i class="fas fa-magnifying-glass search-icon"></i>
          <input 
            v-model="filter.keyword" 
            placeholder="Tìm kiếm tên đợt giảm giá..." 
            @keyup.enter="fetchData" 
          />
        </div>
        <div class="buttons">
          <button class="btn btn-outline" @click="exportExcel">
            <i class="fas fa-file-excel"></i> Xuất Excel
          </button>
          <router-link :to="{ name: 'admin-sale-create' }" class="btn btn-primary">
            <i class="fas fa-plus"></i> Tạo mới
          </router-link>
        </div>
      </div>
      
      <div class="filter-row">
        <div class="date-group">
            <div class="date-input-wrapper">
                <input type="text" onfocus="(this.type='datetime-local')" onblur="(this.type='text')" placeholder="Ngày bắt đầu" v-model="filter.startDate">
            </div>
            <span class="divider">-</span>
            <div class="date-input-wrapper">
                <input type="text" onfocus="(this.type='datetime-local')" onblur="(this.type='text')" placeholder="Ngày kết thúc" v-model="filter.endDate">
            </div>
        </div>
         <select v-model="filter.status" @change="fetchData" class="form-select">
            <option value="">-- Trạng thái --</option>
            <option value="1">Đang diễn ra</option>
            <option value="0">Đã kết thúc</option>
         </select>
      </div>
    </div>

    <div class="table-container">
      <table class="custom-table">
        <thead>
          <tr>
            <th>STT</th>
            <th>Tên Đợt giảm giá</th>
            <th>Mức giảm</th>
            <th>Trạng thái</th>
            <th>Thời gian</th>
            <th class="text-center">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in list" :key="item.id" :class="{'row-disabled': item.trangThai === 0}">
            <td>{{ (currentPage * pageSize) + index + 1 }}</td>
            <td style="font-weight: 600; color: #1e293b;">{{ item.tenDotGiamGia }}</td>
            <td style="color: #dc2626; font-weight: 700;">
                {{ item.loaiGiamGia === '%' ? item.giaTriGiam + '%' : formatCurrency(item.giaTriGiam) }}
            </td>
            <td>
                <span class="badge" :class="getStatusClass(item)">{{ getStatusLabel(item) }}</span>
            </td>
            <td class="small-text">
                <div>{{ formatDate(item.ngayBatDau) }}</div>
                <div>{{ formatDate(item.ngayKetThuc) }}</div>
            </td>
            <td class="text-center action-col">
                <button class="icon-btn" title="Cập nhật chi tiết" @click="goToUpdate(item.id)">
                    <i class="fas fa-pen-to-square"></i>
                </button>
                
                <button v-if="item.trangThai === 1" class="icon-btn delete" @click="stopSale(item)" title="Kết thúc ngay">
                    <i class="fas fa-ban"></i>
                </button>
            </td>
          </tr>
          <tr v-if="list.length === 0">
             <td colspan="6" class="text-center empty-state">Không tìm thấy dữ liệu</td>
          </tr>
        </tbody>
      </table>

      <div class="pagination-footer">
        <div class="page-size-selector">
            Xem 
            <select v-model="pageSize" @change="handlePageSizeChange">
                <option :value="5">5</option>
                <option :value="10">10</option>
                <option :value="20">20</option>
            </select> 
            sản phẩm
        </div>
        <div class="pagination-controls">
            <button class="page-btn" :disabled="currentPage === 0" @click="changePage(currentPage - 1)">
                <i class="fas fa-chevron-left"></i>
            </button>
            <button class="page-btn active">{{ currentPage + 1 }}</button>
            <button class="page-btn" :disabled="currentPage >= totalPages - 1" @click="changePage(currentPage + 1)">
                <i class="fas fa-chevron-right"></i>
            </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import request from '@/services/request';
import { useRouter } from 'vue-router';

const router = useRouter();
const list = ref([]);
const filter = ref({ keyword: '', startDate: '', endDate: '', status: '', valueType: '' });
const currentPage = ref(0);
const pageSize = ref(10);
const totalPages = ref(0);

// --- API ---
const fetchData = async () => {
    try {
        const params = { ...filter.value, page: currentPage.value, size: pageSize.value };
        const res = await request.get('/dot-giam-gia', { params });
        list.value = res.data.content;
        totalPages.value = res.data.totalPages;
    } catch (e) { console.error(e); }
};

watch(() => [filter.value.status, filter.value.valueType], () => {
    currentPage.value = 0;
    fetchData();
});

// --- CHUYỂN HƯỚNG ---
const goToUpdate = (id) => {
    // Chuyển sang trang SaleUpdate với tham số ID
    router.push({ name: 'admin-sale-update', params: { id: id } });
};

const stopSale = async (item) => {
    if(!confirm('Bạn có chắc chắn muốn KẾT THÚC đợt giảm giá này ngay lập tức?')) return;
    try { 
        await request.delete(`/dot-giam-gia/${item.id}`); 
        alert('Đã kết thúc đợt giảm giá.');
        fetchData(); 
    } catch (e) { alert('Lỗi hệ thống'); }
};

const exportExcel = async () => {
    if(!confirm('Tải xuống danh sách đợt giảm giá?')) return;
    try {
        const res = await request.get('/dot-giam-gia/export', { responseType: 'blob' });
        const url = window.URL.createObjectURL(new Blob([res.data]));
        const link = document.createElement('a'); 
        link.href = url; 
        link.setAttribute('download', 'DanhSachDotGiamGia.xlsx'); 
        document.body.appendChild(link); 
        link.click();
    } catch (e) { alert('Lỗi tải file'); }
};

// Helpers
const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
const formatDate = (val) => val ? new Date(val).toLocaleString('vi-VN') : '';
const getStatusLabel = (item) => item.trangThai === 1 ? 'Đang diễn ra' : 'Đã kết thúc';
const getStatusClass = (item) => item.trangThai === 1 ? 'badge-active' : 'badge-stopped';
const changePage = (page) => { currentPage.value = page; fetchData(); };
const handlePageSizeChange = () => { currentPage.value = 0; fetchData(); };

onMounted(fetchData);
</script>

<style scoped>
/* Reuse Styles */
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background: #f8f9fa; min-height: 100vh; }
.page-title { font-size: 24px; font-weight: 700; color: #2b4360; margin-bottom: 20px; }
.control-panel { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); margin-bottom: 20px; }
.action-row { display: flex; justify-content: space-between; margin-bottom: 15px; }
.search-box { position: relative; width: 400px; }
.search-icon { position: absolute; left: 12px; top: 11px; color: #64748b; }
.search-box input { width: 100%; padding: 10px 10px 10px 35px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; }
.buttons { display: flex; gap: 10px; }
.btn { padding: 8px 16px; border-radius: 6px; cursor: pointer; border: none; font-weight: 600; display: flex; align-items: center; gap: 5px; text-decoration: none; font-size: 14px; }
.btn-primary { background: #1e293b; color: white; }
.btn-outline { background: white; border: 1px solid #cbd5e1; color: #334155; }

.filter-row { display: flex; gap: 15px; flex-wrap: wrap; }
.date-group { display: flex; align-items: center; gap: 8px; }
.date-input-wrapper input { padding-left: 10px; width: 160px; height: 38px; border: 1px solid #cbd5e1; border-radius: 6px; outline: none; }
.divider { font-weight: bold; color: #2b4360; }
.form-select { height: 38px; min-width: 150px; border: 1px solid #cbd5e1; border-radius: 6px; outline: none; }

.table-container { background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); overflow: hidden; }
.custom-table { width: 100%; border-collapse: collapse; }
.custom-table th { background: #f8fafc; padding: 15px; text-align: left; color: #475569; border-bottom: 1px solid #e2e8f0; }
.custom-table td { padding: 15px; border-bottom: 1px solid #f1f5f9; color: #334155; vertical-align: middle; }
.row-disabled { opacity: 0.6; background: #fafafa; }
.small-text { font-size: 13px; color: #64748b; }

.badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.badge-active { background: #ecfdf5; color: #059669; }
.badge-stopped { background: #fef2f2; color: #dc2626; }

.action-col { display: flex; justify-content: center; gap: 8px; }
.icon-btn { background: none; border: 1px solid #e2e8f0; padding: 6px; border-radius: 4px; cursor: pointer; color: #64748b; }
.icon-btn:hover { background: #1e293b; color: white; }
.icon-btn.delete:hover { background: #dc2626; color: white; }

.pagination-footer { display: flex; justify-content: space-between; align-items: center; padding: 15px 20px; border-top: 1px solid #f1f5f9; }
.page-size-selector select { border: 1px solid #cbd5e1; border-radius: 4px; padding: 4px; margin: 0 5px; }
.pagination-controls { display: flex; gap: 5px; }
.page-btn { width: 32px; height: 32px; border-radius: 50%; border: 1px solid #e2e8f0; background: white; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.page-btn.active { background: #1e293b; color: white; border-color: #1e293b; }
.text-center { text-align: center; }
.empty-state { padding: 30px; color: #94a3b8; font-style: italic; }
</style>