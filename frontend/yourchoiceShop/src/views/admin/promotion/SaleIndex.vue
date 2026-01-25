<template>
<<<<<<< HEAD
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
          <button @click="exportExcel" class="btn btn-outline">
            <i class="fas fa-file-excel"></i> Xuất Excel
          </button>
          <router-link to="/admin/sales/create" class="btn btn-primary">
            <i class="fas fa-plus"></i> Tạo mới
          </router-link>
        </div>
      </div>
      
      <div class="filter-row">
        <div class="date-group">
            <div class="date-input-wrapper">
                <i class="far fa-calendar date-icon"></i>
                <input 
                    type="text" 
                    onfocus="(this.type='datetime-local')" 
                    onblur="(this.type='text')" 
                    placeholder="Ngày bắt đầu" 
                    v-model="filter.startDate"
                >
            </div>
            <span class="divider">-</span>
            <div class="date-input-wrapper">
                <i class="far fa-calendar date-icon"></i>
                <input 
                    type="text" 
                    onfocus="(this.type='datetime-local')" 
                    onblur="(this.type='text')" 
                    placeholder="Ngày kết thúc" 
                    v-model="filter.endDate"
                >
            </div>
        </div>
         <select v-model="filter.status" @change="fetchData" class="form-select">
            <option value="">-- Trạng thái --</option>
            <option value="1">Đang diễn ra</option>
            <option value="0">Đã kết thúc</option>
         </select>

         <select v-model="filter.valueType" @change="fetchData" class="form-select">
            <option value="">-- Giá trị --</option>
            <option value="%">Phần trăm (%)</option>
            <option value="VND">Tiền mặt (VND)</option>
         </select>
      </div>
    </div>

    <div class="table-container">
      <table class="custom-table">
        <thead>
          <tr>
            <th>STT</th>
            <th>Tên Đợt giảm giá</th>
            <th>Giá trị</th>
            <th>Trạng thái</th>
            <th>Thời gian áp dụng</th>
            <th class="text-center">Hoạt động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in list" :key="item.id" :class="{'row-disabled': item.trangThai === 0}">
            <td>{{ (currentPage * pageSize) + index + 1 }}</td>
            <td style="font-weight: bold; color: #2b4360;">{{ item.tenDotGiamGia }}</td>
            <td style="color: #ef4444; font-weight: bold;">
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
                <button class="icon-btn" title="Xem sản phẩm" @click="openModal(item)">
                    <i class="fas fa-eye"></i>
                </button>
                
                <button 
                    v-if="item.trangThai === 1" 
                    class="icon-btn" 
                    @click="stopSale(item)" 
                    title="Kết thúc đợt giảm giá"
                >
                    <i class="fas fa-ban" style="color: #d9534f;"></i>
                </button>
                
                <button v-else class="icon-btn" disabled>
                     <i class="fas fa-ban"></i>
                </button>
            </td>
          </tr>
          <tr v-if="list.length === 0">
             <td colspan="6" class="text-center empty-state">Không có dữ liệu</td>
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

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
        <div class="modal-content">
            <div class="modal-header">
                <h3>Sản phẩm trong đợt: <span style="color: #2b4360;">{{ selectedItem?.tenDotGiamGia }}</span></h3>
                <button @click="showModal = false" class="close-btn"><i class="fas fa-xmark"></i></button>
            </div>
            <div class="modal-body">
                <table class="custom-table">
                    <thead>
                        <tr>
                            <th>Ảnh</th>
                            <th>Tên sản phẩm</th>
                            <th>Chi tiết</th>
                            <th>Giá gốc</th>
                            <th>Giá sau giảm</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="p in modalProducts" :key="p.id">
                            <td>
                                <div class="img-placeholder"><i class="fas fa-image"></i></div>
                            </td>
                            <td>{{ p.chiTietSanPham.sanPham.tenSanPham }}</td>
                            <td>
                                <span class="variant-badge">
                                    {{ p.chiTietSanPham.mauSac.tenMauSac }} - {{ p.chiTietSanPham.kichThuoc.tenKichThuoc }}
                                </span>
                            </td>
                            <td style="text-decoration: line-through; color: #94a3b8;">
                                {{ formatCurrency(p.chiTietSanPham.giaBan) }}
                            </td>
                            <td style="color: #ef4444; font-weight: bold;">
                                {{ calculateNewPrice(p.chiTietSanPham.giaBan, selectedItem) }}
                            </td>
                        </tr>
                        <tr v-if="modalProducts.length === 0">
                            <td colspan="5" class="text-center empty-state">Chưa có sản phẩm nào trong đợt này</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
=======
  <div style="padding: 20px;">
    <h2>Trang này đang được xây dựng...</h2>
>>>>>>> upstream/main
  </div>
</template>

<script setup>
<<<<<<< HEAD
import { ref, onMounted } from 'vue';
import request from '@/services/request';

const list = ref([]);
const filter = ref({ keyword: '', startDate: '', endDate: '', status: '', valueType: '' });
const currentPage = ref(0);
const pageSize = ref(5);
const totalPages = ref(0);

// Modal State
const showModal = ref(false);
const selectedItem = ref(null);
const modalProducts = ref([]);

// --- API Calls ---
const fetchData = async () => {
    try {
        const params = { ...filter.value, page: currentPage.value, size: pageSize.value };
        const res = await request.get('/dot-giam-gia', { params });
        list.value = res.data.content;
        totalPages.value = res.data.totalPages;
    } catch (e) { console.error(e); }
};

const openModal = async (item) => {
    selectedItem.value = item;
    try {
        const res = await request.get(`/dot-giam-gia/${item.id}/products`);
        modalProducts.value = res.data;
        showModal.value = true;
    } catch (e) { alert('Lỗi tải danh sách sản phẩm'); }
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
        link.setAttribute('download', 'Sales.xlsx'); 
        document.body.appendChild(link); 
        link.click();
    } catch (e) { alert('Lỗi tải file'); }
};

// --- Helpers ---
const isExpired = (date) => new Date(date) < new Date();
const getStatusLabel = (item) => item.trangThai === 0 ? 'Đã kết thúc' : (isExpired(item.ngayKetThuc) ? 'Hết hạn' : 'Đang diễn ra');
const getStatusClass = (item) => item.trangThai === 0 ? 'badge-stopped' : (isExpired(item.ngayKetThuc) ? 'badge-expired' : 'badge-active');

const calculateNewPrice = (price, sale) => {
    let final = sale.loaiGiamGia === 'VND' ? price - sale.giaTriGiam : price * (100 - sale.giaTriGiam) / 100;
    return formatCurrency(final);
};
const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
const formatDate = (val) => val ? new Date(val).toLocaleString('vi-VN', {year:'numeric', month:'2-digit', day:'2-digit', hour:'2-digit', minute:'2-digit'}) : '';
const changePage = (page) => { currentPage.value = page; fetchData(); };
const handlePageSizeChange = () => { currentPage.value = 0; fetchData(); };

onMounted(fetchData);
</script>

<style scoped>
/* Reuse Styles to match VoucherIndex */
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; }
.page-title { font-size: 24px; font-weight: 700; color: #2b4360; margin-bottom: 20px; }

/* Control Panel & Search */
.control-panel { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); margin-bottom: 20px; }
.action-row { display: flex; justify-content: space-between; margin-bottom: 15px; }
.search-box { position: relative; width: 400px; }
.search-icon { position: absolute; left: 12px; top: 11px; color: #2b4360; }
.search-box input { padding-left: 38px; width: 100%; height: 38px; border: 1px solid #cbd5e1; border-radius: 6px; }

/* Filter Row */
.filter-row { display: flex; gap: 15px; flex-wrap: wrap; }
.date-group { display: flex; align-items: center; gap: 8px; }
.date-input-wrapper { position: relative; }
.date-icon { position: absolute; left: 10px; top: 10px; color: #2b4360; pointer-events: none; }
.date-input-wrapper input { padding-left: 35px; width: 160px; height: 38px; border: 1px solid #cbd5e1; border-radius: 6px; }
.divider { font-weight: bold; color: #2b4360; }
.form-select { height: 38px; min-width: 150px; border: 1px solid #cbd5e1; border-radius: 6px; }

/* Table & Status */
.table-container { background: white; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); overflow: hidden; border: 1px solid #e2e8f0; }
.custom-table { width: 100%; border-collapse: collapse; }
.custom-table th { background: #f8fafc; color: #475569; padding: 15px; text-align: left; font-weight: 700; border-bottom: 1px solid #e2e8f0; }
.custom-table td { padding: 15px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.row-disabled { opacity: 0.6; background: #fafafa; }
.small-text { font-size: 13px; color: #64748b; }
.badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; white-space: nowrap; }
.badge-active { background: #dcfce7; color: #166534; }
.badge-stopped { background: #fee2e2; color: #991b1b; }
.badge-expired { background: #f1f5f9; color: #94a3b8; }

/* Actions */
.action-col { display: flex; justify-content: center; gap: 8px; }
.icon-btn { background: none; border: 1px solid #e2e8f0; border-radius: 4px; cursor: pointer; font-size: 14px; padding: 6px; color: #2b4360; transition: 0.2s; }
.icon-btn:hover:not(:disabled) { background: #ffffff; color: #eddcc3; }
.icon-btn:disabled { color: #cbd5e1; border-color: #f1f5f9; cursor: not-allowed; }

/* Pagination */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; padding: 15px 20px; background: white; border-top: 1px solid #f1f5f9; }
.page-size-selector select { border: 1px solid #cbd5e1; border-radius: 4px; padding: 4px; margin: 0 5px; color: #2b4360; font-weight: bold; }
.pagination-controls { display: flex; gap: 8px; }
.page-btn { width: 32px; height: 32px; border-radius: 50%; border: 1px solid #e2e8f0; background: white; color: #64748b; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: 0.2s; }
.page-btn:hover:not(:disabled) { border-color: #2b4360; color: #2b4360; }
.page-btn.active { background: #dbeafe; color: #1e40af; border-color: #dbeafe; font-weight: bold; }
.page-btn:disabled { color: #cbd5e1; cursor: not-allowed; }

/* Modal */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(43, 67, 96, 0.6); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: white; width: 800px; max-height: 85vh; overflow-y: auto; border-radius: 8px; padding: 20px; box-shadow: 0 10px 25px rgba(0,0,0,0.2); }
.modal-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; padding-bottom: 15px; margin-bottom: 15px; }
.close-btn { background: none; border: none; font-size: 20px; cursor: pointer; color: #94a3b8; transition: 0.2s; }
.close-btn:hover { color: #ef4444; }
.img-placeholder { width: 40px; height: 40px; background: #f1f5f9; display: flex; align-items: center; justify-content: center; color: #cbd5e1; border-radius: 4px; }
.variant-badge { background: #f1f5f9; padding: 2px 8px; border-radius: 4px; font-size: 12px; color: #64748b; }
.text-center { text-align: center; }
.empty-state { font-style: italic; color: #94a3b8; }
</style>
=======
</script>
>>>>>>> upstream/main
