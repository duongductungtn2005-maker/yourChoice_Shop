<template>
  <div class="page-container">
    <h1 class="page-title">Quản lý Đợt giảm giá</h1>

    <div class="control-panel">
      <div class="controls-row">
        
        <div class="filter-group">
           <div class="search-box">
              <i class="fas fa-magnifying-glass search-icon"></i>
              <input 
              class="input-den"
                v-model="filter.keyword" 
                placeholder="Tìm tên, mã đợt..." 
                @keyup.enter="fetchData" 
              />
           </div>

           <div class="date-group">
              <div class="date-input-wrapper">
                 <input class="input-den" type="text" onfocus="(this.type='datetime-local')" onblur="(this.type='text')" placeholder="Bắt đầu" v-model="filter.startDate">
              </div>
              <span class="divider">-</span>
              <div class="date-input-wrapper">
                 <input class="input-den" type="text" onfocus="(this.type='datetime-local')" onblur="(this.type='text')" placeholder="Kết thúc" v-model="filter.endDate">
              </div>
           </div>
           
           <select v-model="filter.status" @change="fetchData" class="form-select">
              <option value="">-- Trạng thái --</option>
              <option value="1">Đang diễn ra</option>
              <option value="0">Đã kết thúc</option>
           </select>

           <select v-model="filter.loaiGiamGia" @change="fetchData" class="form-select">
              <option value="">-- Loại giảm --</option>
              <option value="%">%</option>
              <option value="VND">VND</option>
           </select>
        </div>

        <div class="action-group">
           <button class="btn btn-navy" @click="resetFilter">
              <i class="fas fa-sync-alt"></i> Đặt lại
           </button>

           <button class="btn btn-outline" @click="exportExcel">
              <font-awesome-icon :icon="['fas','file-excel']" /> Xuất Excel
           </button>
           
           <router-link :to="{ name: 'admin-sale-create' }" class="btn btn-gradient">
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
            <th>Mã đợt</th> <th>Tên đợt</th>
            <th class="text-center">Giá trị</th>
            <th class="text-center">Loại giảm</th> <th>Ngày bắt đầu</th> <th>Ngày kết thúc</th> <th class="text-center">Trạng thái</th>
            <th class="text-center">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="list.length === 0">
             <td colspan="9" class="text-center empty-state">Không có dữ liệu</td>
          </tr>
          <tr v-for="(item, index) in list" :key="item.id" :class="{'row-disabled': item.trangThai === 0}">
            
            <td class="text-center">{{ (currentPage * pageSize) + index + 1 }}</td>
            
            <td class="code-text">{{ item.maDotGiamGia || '---' }}</td>
            
            <td style="color: #2b4360; font-weight: 500;">{{ item.tenDotGiamGia }}</td>
            
            <td class="text-center font-bold" style="color: #ef4444;">
                {{ item.loaiGiamGia === '%' ? item.giaTriGiam : formatCurrency(item.giaTriGiam) }}
            </td>

            <td class="text-center">
                <span class="badge badge-light">{{ item.loaiGiamGia === '%' ? '%' : 'VND' }}</span>
            </td>

            <td>{{ formatDate(item.ngayBatDau) }}</td>
            
            <td>{{ formatDate(item.ngayKetThuc) }}</td>

            <td class="text-center">
                <span class="badge" :class="getStatusClass(item)">{{ getStatusLabel(item) }}</span>
            </td>

            <td class="text-center action-col">
              <div class="action-wrapper">
                  
                  <button class="icon-btn" title="Xem sản phẩm" @click="openModal(item)">
                      <i class="far fa-eye"></i>
                  </button>

                  <label class="switch" title="Bật/Tắt trạng thái">
                      <input 
                          type="checkbox" 
                          :checked="item.trangThai === 1" 
                          :disabled="item.trangThai === 0 && isExpired(item.ngayKetThuc)"
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
            bản ghi
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
                            <td><div class="img-placeholder"><i class="fas fa-image"></i></div></td>
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
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import request from '@/services/request';
import Swal from 'sweetalert2';
import { toastSuccess, toastError } from '@/utils/toast';

const list = ref([]);
// [FIXED] Đổi tên biến valueType -> loaiGiamGia để khớp với DB
const filter = ref({ keyword: '', startDate: '', endDate: '', status: '', loaiGiamGia: '' });
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

// [NEW] Reset Filter
const resetFilter = () => {
    filter.value = { keyword: '', startDate: '', endDate: '', status: '', loaiGiamGia: '' };
    currentPage.value = 0;
    fetchData();
};

const openModal = async (item) => {
    selectedItem.value = item;
    try {
        const res = await request.get(`/dot-giam-gia/${item.id}/products`);
        modalProducts.value = res.data;
        showModal.value = true;
    } catch (e) { toastError('Lỗi tải danh sách sản phẩm'); }
};

const handleToggleStatus = async (item, event) => {
    event.preventDefault();
    const currentStatus = item.trangThai;
    const newStatus = currentStatus === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? 'Kích hoạt' : 'Ngừng hoạt động';
    const confirmBtnColor = newStatus === 1 ? '#10b981' : '#ef4444';

    if (newStatus === 1 && isExpired(item.ngayKetThuc)) {
        Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Đợt giảm giá này đã hết hạn, không thể kích hoạt lại.', customClass: { popup: 'swal-rounded' } });
        return;
    }

    const result = await Swal.fire({
        title: `Xác nhận ${actionText}?`,
        text: `Bạn có muốn ${actionText.toLowerCase()} đợt "${item.tenDotGiamGia}"?`,
        icon: 'question', showCancelButton: true, confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy', confirmButtonColor: confirmBtnColor, customClass: { popup: 'swal-rounded' }
    });

    if (result.isConfirmed) {
        try {
            if (newStatus === 0) {
                await request.delete(`/dot-giam-gia/${item.id}`);
            } else {
                await request.put(`/dot-giam-gia/${item.id}`, { ...item, trangThai: 1, idChiTietSanPhams: null });
            }
            item.trangThai = newStatus;
            toastSuccess(`Đã ${actionText.toLowerCase()} thành công!`);
        } catch (e) { console.error(e); toastError(e.response?.data?.message || 'Có lỗi xảy ra'); }
    }
};

const exportExcel = async () => {
    const confirmRes = await Swal.fire({ title: 'Xác nhận', text: 'Tải xuống danh sách đợt giảm giá?', icon: 'question', showCancelButton: true, confirmButtonText: 'Có', cancelButtonText: 'Hủy' });
    if (!confirmRes.isConfirmed) return;
    try {
        const res = await request.get('/dot-giam-gia/export', { responseType: 'blob' });
        const url = window.URL.createObjectURL(new Blob([res.data]));
        const link = document.createElement('a'); link.href = url; 
        link.setAttribute('download', `DS_DotGiamGia_${new Date().toISOString().slice(0,10)}.xlsx`); 
        document.body.appendChild(link); link.click();
        toastSuccess('Xuất Excel thành công!');
    } catch (e) { toastError('Lỗi tải file'); }
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
const formatDate = (val) => val ? new Date(val).toLocaleString('vi-VN', {day:'2-digit', month:'2-digit', year:'numeric', hour:'2-digit', minute:'2-digit'}) : '';
const changePage = (page) => { currentPage.value = page; fetchData(); };
const handlePageSizeChange = () => { currentPage.value = 0; fetchData(); };

const visiblePages = computed(() => {
    let p = [];
    for (let i = 1; i <= totalPages.value; i++) {
        if (i === 1 || i === totalPages.value || (i >= currentPage.value && i <= currentPage.value + 2)) p.push(i);
    }
    return p;
});

onMounted(() => {
    const successMsg = localStorage.getItem('saleSuccessMessage');
    if (successMsg) { toastSuccess(successMsg); localStorage.removeItem('saleSuccessMessage'); }
    fetchData();
});
</script>

<style scoped>
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background: #f8f9fa; min-height: 100vh; color: #333; font-size: 14px; }
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }

/* === UPDATE: CARD STYLING & BORDER === */
.control-panel, .table-container { 
    background: white; 
    border-radius: 16px; 
    border: 1px solid #bfdbfe !important; /* Viền xanh */
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    margin-bottom: 20px;
    padding: 24px; 
}
.table-container { padding: 0; overflow: hidden; }

/* === UPDATE: FLEX LAYOUT === */
.controls-row { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.filter-group { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.action-group { display: flex; gap: 10px; }

/* INPUTS */
.search-box { position: relative; width: 250px; } 
.search-icon { position: absolute; left: 12px; top: 11px; color: #94a3b8; }
.search-box input { width: 100%; padding: 8px 10px 8px 36px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; height: 40px; }
.search-box input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.search-box input::placeholder {
    color: #000000; /* Màu đen tuyệt đối */
    opacity: 1;     /* Đảm bảo hiển thị rõ 100% trên mọi trình duyệt */
}

.date-group { display: flex; gap: 8px; align-items: center; }
.date-input-wrapper { position: relative; }
.date-icon { position: absolute; left: 10px; top: 10px; color: #94a3b8; pointer-events: none; }
.date-input-wrapper input { padding-left: 35px; width: 160px; height: 40px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; }
.form-select { height: 40px; border-radius: 6px; border: 1px solid #e2e8f0; outline: none; padding: 0 10px; color: #334155; min-width: 150px; cursor: pointer; }

/* BUTTONS */
.btn { height: 40px; padding: 0 20px; border-radius: 6px; font-weight: 600; cursor: pointer; font-size: 13px; border: 1px solid transparent; transition: 0.2s; display: inline-flex; align-items: center; gap: 8px; text-decoration: none; }
.btn-secondary { background: #334155; color: #fff; } .btn-secondary:hover { background: #1e293b; }
.btn-outline { background: #fff; border: 1px solid #e2e8f0; color: #475569; } .btn-outline:hover { background: #f8fafc; border-color: #cbd5e1; }
.btn-gradient { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); }
.btn-gradient:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.3); }

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
.custom-table td { padding: 14px 16px; border-bottom: 1px solid #f1f5f9; text-align: center; vertical-align: middle; font-size: 14px; color: #333; }

/* BADGES */
.badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; white-space: nowrap; }
.badge-active { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.badge-stopped { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
.badge-expired { background: #f1f5f9; color: #94a3b8; border: 1px solid #e2e8f0; }
.badge-light { background: #f8fafc; color: #64748b; border: 1px solid #e2e8f0; }

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
.modal-content { background: white; width: 800px; max-height: 85vh; overflow-y: auto; border-radius: 8px; padding: 20px; box-shadow: 0 10px 25px rgba(0,0,0,0.2); }
.modal-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #e2e8f0; padding-bottom: 15px; margin-bottom: 15px; }
.close-btn { background: none; border: none; font-size: 20px; cursor: pointer; color: #94a3b8; transition: 0.2s; }
.close-btn:hover { color: #ef4444; }
.img-placeholder { width: 40px; height: 40px; background: #f1f5f9; display: flex; align-items: center; justify-content: center; color: #cbd5e1; border-radius: 4px; }
.variant-badge { background: #f1f5f9; padding: 2px 8px; border-radius: 4px; font-size: 12px; color: #64748b; }
.font-bold { font-weight: 600; }
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