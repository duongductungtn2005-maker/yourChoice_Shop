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
        <div class="action-group">
  <button class="btn btn-outline" @click="exportExcel">
    <font-awesome-icon :icon="['fas','file-excel']" /> Xuất Excel
  </button>
  
  <router-link :to="{ name: 'admin-sale-create' }" class="btn btn-primary">
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
            <td style="color: #2b4360;">{{ item.tenDotGiamGia }}</td>
            <td style="color: #ef4444;">
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
    <div class="action-wrapper">
        
        <button class="icon-btn" title="Xem sản phẩm" @click="openModal(item)">
            <i class="fas fa-eye"></i>
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
          <tr v-if="list.length === 0">
             <td colspan="6" class="text-center empty-state">Không có dữ liệu</td>
          </tr>
        </tbody>
      </table>

      <div class="pagination-footer">
        <div class="page-info">
            Hiển thị 
            <select v-model="pageSize" @change="handlePageSizeChange">
                <option :value="5">5</option>
                <option :value="10">10</option>
                <option :value="20">20</option>
            </select> 
            đợt giảm giá / trang
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
                            <td style="color: #ef4444;">
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
const list = ref([]);
const filter = ref({ keyword: '', startDate: '', endDate: '', status: '', valueType: '' });
const currentPage = ref(0);
const pageSize = ref(5);
const totalPages = ref(0);
const handleToggleStatus = async (item, event) => {
    // Ngăn checkbox nhảy trạng thái ngay lập tức
    event.preventDefault();

    const currentStatus = item.trangThai;
    const newStatus = currentStatus === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? 'Kích hoạt' : 'Ngừng hoạt động';
    const confirmBtnColor = newStatus === 1 ? '#10b981' : '#ef4444';

    // Check hết hạn nếu định kích hoạt lại
    if (newStatus === 1 && isExpired(item.ngayKetThuc)) {
        Swal.fire('Lỗi', 'Đợt giảm giá này đã hết hạn, không thể kích hoạt lại.', 'error');
        return;
    }

    const result = await Swal.fire({
        title: `<h3 style="color:#1e293b; font-size:18px;">Xác nhận ${actionText}?</h3>`,
        text: `Bạn có muốn ${actionText.toLowerCase()} đợt "${item.tenDotGiamGia}"?`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy',
        confirmButtonColor: confirmBtnColor
    });

    if (result.isConfirmed) {
        try {
            if (newStatus === 0) {
                // Tắt: Gọi API Delete (như cũ)
                await request.delete(`/dot-giam-gia/${item.id}`);
            } else {
                // Bật: Gọi API Update (Gửi lại thông tin item với trạng thái mới)
                // Lưu ý: Backend cần hỗ trợ update qua PUT
                await request.put(`/dot-giam-gia/${item.id}`, { ...item, trangThai: 1, idChiTietSanPhams: null });
            }

            // Cập nhật giao diện
            item.trangThai = newStatus;
            
            const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 1500 });
            Toast.fire({ icon: 'success', title: `Đã ${actionText.toLowerCase()} thành công!` });

        } catch (e) {
            console.error(e);
            Swal.fire('Lỗi', e.response?.data?.message || 'Có lỗi xảy ra', 'error');
        }
    }
};
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

onMounted(fetchData);
</script>

<style scoped>
/* --- BUTTON STYLES (Đồng bộ màn Cổ áo) --- */

.action-group { 
  display: flex; 
  gap: 10px; /* Khoảng cách giữa các nút */
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
    text-decoration: none; /* Bỏ gạch chân cho router-link */
}

/* Nút Tạo mới: Màu xanh đen đậm (#0f172a) */
.btn-primary { 
  background: #0f172a; 
  color: #fff; 
  border-color: #0f172a;
}
.btn-primary:hover {
  background: #1e293b;
  border-color: #1e293b;
}

/* Nút Xuất Excel: Nền trắng, viền xám (#cbd5e1) */
.btn-outline { 
  background: #fff; 
  border-color: #cbd5e1; 
  color: #475569; 
}
.btn-outline:hover {
  background: #f8fafc;
  color: #0f172a;
  border-color: #94a3b8;
}
/* Reuse Styles to match VoucherIndex */
/* --- THÊM ĐOẠN NÀY VÀO CUỐI THẺ STYLE --- */
.switch {
  position: relative;
  display: inline-block;
  width: 36px;
  height: 20px;
  margin: 0; /* Xóa margin thừa */
  flex-shrink: 0;
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
  background-color: #10b981; /* Màu xanh khi bật */
}

input:checked + .slider:before {
  transform: translateX(16px);
}

input:disabled + .slider {
  background-color: #e2e8f0;
  cursor: not-allowed;
}
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background: #f8f9fa; min-height: 100vh; color: #333; font-size: 14px; }
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }

/* Table typography (match CoAoIndex) */
.custom-table th {
  background: #E9F1FB;
  color: #1E3A8A;
  padding: 15px;
  text-align: center;
  font-size: 13px;
  font-weight: 700;
}
.custom-table td {
  padding: 15px;
  border-bottom: 1px solid #f1f5f9;
  text-align: center;
  vertical-align: middle;
  font-size: 14px;
  color: #333;
} 
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
.divider { font-weight: 500; color: #2b4360; }
.form-select { height: 38px; min-width: 150px; border: 1px solid #cbd5e1; border-radius: 6px; }

/* Table & Status */
.table-container { background: white; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); overflow: hidden; border: 1px solid #e2e8f0; }
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
}
.row-disabled { opacity: 0.6; background: #fafafa; }
.small-text { font-size: 13px; color: #64748b; }
.badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; white-space: nowrap; }
.badge-active { background: #dcfce7; color: #166534; }
.badge-stopped { background: #fee2e2; color: #991b1b; }
.badge-expired { background: #f1f5f9; color: #94a3b8; }

/* Actions */
.action-col {
  vertical-align: middle !important; /* Căn giữa theo chiều dọc của dòng */
  padding: 8px !important;           /* Padding đều 4 phía */
  width: 150px;                      /* Cố định chiều rộng cột để không bị co kéo */
}
.action-wrapper {
  display: flex !important;
  align-items: center !important;     /* Thẳng hàng dọc */
  justify-content: center !important; /* Thẳng hàng ngang */
  gap: 12px !important;               /* Khoảng cách giữa 2 nút */
  width: 100%;
}
.icon-btn {
  width: 34px;
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
  margin: 0; /* Xóa margin thừa */
}

.icon-btn:hover:not(:disabled) {
  background: #f1f5f9;
  color: #0f172a;
  border-color: #cbd5e1;
}

.icon-btn:disabled {
  background: #f8fafc;
  color: #cbd5e1;
  cursor: not-allowed;
}

/* Pagination (đồng bộ màn Sản phẩm) */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding-top: 15px; border-top: 1px solid #f1f5f9; }
.page-info { font-size: 14px; color: #64748b; }
.page-info select { border: 1px solid #cbd5e1; border-radius: 4px; padding: 4px 8px; margin: 0 5px; outline: none; }
.page-controls button { width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; margin-left: 5px; cursor: pointer; color: #64748b; }
.page-controls button.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.page-controls button:disabled { opacity: 0.5; cursor: not-allowed; }

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