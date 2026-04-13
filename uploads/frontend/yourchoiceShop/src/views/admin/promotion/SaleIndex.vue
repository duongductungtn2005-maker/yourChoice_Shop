<template>
  <div class="page-container">
    <div class="header-section">
      <h1 class="page-title">Quản lý đợt giảm giá</h1>
    </div>

    <div class="control-panel">
      <div class="filter-row">
           <div class="search-box">
              <i class="fas fa-magnifying-glass search-icon"></i>
              <input 
                class="input-den form-control"
                v-model="filter.keyword" 
                placeholder="Tìm tên, mã đợt..." 
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
           
           <select v-model="filter.status" @change="fetchData" class="form-select form-control">
              <option value="">-- Trạng thái --</option>
              <option value="1">Đang diễn ra</option>
              <option value="0">Đã kết thúc</option>
           </select>
      </div>

      <div class="action-row">
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

    <div class="table-container">
      <table class="custom-table">
        <thead>
          <tr>
            <th>STT</th>
            <th>Mã đợt</th> 
            <th>Tên đợt</th>
            <th>Giá trị</th> <th>Loại giảm</th> 
            <th>Ngày bắt đầu</th> 
            <th>Ngày kết thúc</th> 
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="list.length === 0">
             <td colspan="9" class="empty-state">Không có dữ liệu</td>
          </tr>
          <tr v-for="(item, index) in list" :key="item.id" :class="{'row-disabled': item.trangThai === 0}">
            
            <td>{{ (currentPage * pageSize) + index + 1 }}</td>
            
            <td class="code-text">{{ item.maDotGiamGia || '---' }}</td>
            
            <td style="color: #2b4360; font-weight: 500;">{{ item.tenDotGiamGia }}</td>
            
            <td class="font-bold" style="color: rgb(43, 67, 96);">
                {{ item.loaiGiamGia === '%' ? item.giaTriGiam + '%' : formatCurrency(item.giaTriGiam) }}
            </td>

            <td>
                <span class="badge badge-light">{{ item.loaiGiamGia === '%' ? '%' : 'VND' }}</span>
            </td>

            <td>{{ formatDate(item.ngayBatDau) }}</td>
            
            <td>{{ formatDate(item.ngayKetThuc) }}</td>

            <td>
                <span class="badge" :class="getStatusClass(item)">{{ getStatusLabel(item) }}</span>
            </td>

            <td class="action-col">
              <div class="action-wrapper">
                  <button @click="goToEdit(item.id)" class="icon-btn" title="Xem sản phẩm"">
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
                            <td colspan="5" class="empty-state">Chưa có sản phẩm nào trong đợt này</td>
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
import { useRouter } from 'vue-router';

const router = useRouter();

const goToEdit = (id) => {
    // Tên name 'admin-sale-edit' phải khớp với name mày đặt ở Bước 1
    router.push({ name: 'admin-sale-edit', params: { id: id } });
};

const list = ref([]);
const filter = ref({ keyword: '', startDate: '', endDate: '', status: '' });
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

const resetFilter = () => {
    filter.value = { keyword: '', startDate: '', endDate: '', status: '' };
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
            await request.put(`/dot-giam-gia/${item.id}`, { ...item, trangThai: newStatus, idChiTietSanPhams: null });
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
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background: #ebecee;; min-height: 100vh; color: #333; font-size: 14px; }
.page-title { margin: 0; font-size: 24px; font-weight: 700; color: #1e293b; }
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
/* === CARD STYLING === */
.control-panel, .table-container { 
    background: white; border-radius: 16px; border: 1px solid #bfdbfe !important; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); margin-bottom: 20px; padding: 24px; 
}
.table-container { padding: 0; overflow: hidden; }

/* === GRID LAYOUT FOR FILTERS === */
.filter-row { 
    display: grid;
    grid-template-columns: repeat(4, 1fr); /* 4 cột bằng nhau */
    gap: 15px;
    width: 100%;
}

/* === FLEX LAYOUT FOR ACTIONS === */
.action-row {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 15px;
}

/* INPUTS & SELECTS */
.form-control {
    width: 100%;
    height: 40px;
    border: 1px solid #e2e8f0;
    border-radius: 6px;
    outline: none;
    padding: 0 10px;
    box-sizing: border-box; 
}

.search-box { position: relative; width: 100%; } 
.search-icon { position: absolute; left: 12px; top: 12px; color: #94a3b8; }
.search-box input { padding-left: 36px; }

.search-box input:focus, .form-control:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }

/* === DATE INPUT WITH ICON === */
.date-input-wrapper { position: relative; width: 100%; }
.date-icon { 
    position: absolute; 
    right: 12px; /* Icon bên phải */
    top: 50%;
    transform: translateY(-50%);
    color: #94a3b8; 
    pointer-events: none; /* Để click xuyên qua vào input */
}
.date-input-wrapper input { padding-right: 35px; } /* Tránh text đè icon */

.input-den::placeholder { color: #94a3b8 !important; opacity: 1 !important; font-weight: normal; }

/* BUTTONS */
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

.btn-navy { background-color: #334155; color: #ffffff; box-shadow: 0 2px 4px rgba(51, 65, 85, 0.2); }
.btn-navy:hover { background-color: #1e293b; transform: translateY(-1px); }

.btn-outline { background: #fff; border: 1px solid #cbd5e1; color: #475569; }
.btn-outline:hover { background: #f8fafc; border-color: #94a3b8; color: #0f172a; }

.btn-gradient { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%);  color: #fff; box-shadow: 0 4px 10px rgba(255, 255, 255, 0.2); }
.btn-gradient:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(255, 252, 252, 0.3); }

/* === TABLE STYLES === */
.custom-table { width: 100%; border-collapse: separate; border-spacing: 0; }
.custom-table th {
  background: #f5f5f5; 
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

.code-text { font-family: monospace; font-weight: 600; color: #475569; }
.font-bold { font-weight: 700; }

/* BADGES */
.badge { padding: 6px 12px; border-radius: 6px; font-size: 12px; font-weight: 600; white-space: nowrap; display: inline-block;}
.badge-active { background: #ecfdf5; color: #059669; }
.badge-stopped { background: #eff6ff; color: #2563eb; }
.badge-expired { background: #fee2e2; color: #ef4444; }
.badge-light { background: #f3f4f6; color: #4b5563; }

/* ACTIONS */
.action-wrapper { display: flex; align-items: center; justify-content: center; gap: 8px; }

/* ĐÃ SỬA: Icon Button - Bo góc và có viền (Giống file Voucher) */
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

/* Switch */
.switch { position: relative; display: inline-block; width: 40px; height: 22px; margin: 0; flex-shrink: 0; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #cbd5e1; transition: .4s; border-radius: 34px; }
.slider:before { position: absolute; content: ""; height: 16px; width: 16px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; box-shadow: 0 2px 4px 0 rgba(0,0,0,0.2); }
input:checked + .slider { background-color: #10b981; } /* Màu đỏ khi active */
input:checked + .slider:before { transform: translateX(18px); }
input:disabled + .slider { background-color: #e2e8f0; cursor: not-allowed; }

/* PAGINATION */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; padding: 15px 24px; border-top: 1px solid #f1f5f9; }
.page-info select { border: 1px solid #e2e8f0; border-radius: 4px; padding: 2px 5px; margin: 0 5px; }
.page-controls button { width: 30px; height: 30px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; margin-left: 5px; cursor: pointer; color: #64748b; font-size: 12px; }
.page-controls button.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.page-controls button:disabled { opacity: 0.5; cursor: not-allowed; }

/* MODAL */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: white; width: 800px; max-height: 85vh; overflow-y: auto; border-radius: 12px; padding: 20px; }
.modal-header { display: flex; justify-content: space-between; margin-bottom: 15px; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px; }
.close-btn { border: none; background: none; font-size: 20px; cursor: pointer; color: #64748b; }
.img-placeholder { width: 40px; height: 40px; background: #f1f5f9; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #cbd5e1; }
.variant-badge { background: #f1f5f9; padding: 2px 8px; border-radius: 4px; font-size: 12px; color: #64748b; font-weight: 500; }
</style>
