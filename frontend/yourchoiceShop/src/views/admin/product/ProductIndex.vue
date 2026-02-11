<template>
  <div class="page-container">
    <div class="header-section">
      <h1 class="page-title">Quản lý sản phẩm / Sản phẩm</h1>
    </div>

    <div class="control-panel">
      <div class="controls-row">
        
        <div class="filter-group">
            <div class="search-wrap">
               <span class="search-icon">🔍</span>
               <input 
                 type="text" 
                 v-model="filter.keyword" 
                 placeholder="Tìm sản phẩm (Tên, Mã)..." 
                 @keyup.enter="fetchProducts"
               >
            </div>

            <div class="status-filter">
               <span class="label">Trạng thái: </span>
               <label class="radio-item">
                  <input type="radio" :value="null" v-model="filter.status" @change="fetchProducts"> 
                  Tất cả
               </label>
               <label class="radio-item">
                  <input type="radio" :value="1" v-model="filter.status" @change="fetchProducts"> 
                  Đang bán
               </label>
               <label class="radio-item">
                  <input type="radio" :value="0" v-model="filter.status" @change="fetchProducts"> 
                  Ngừng bán
               </label>
            </div>
        </div>

        <div class="action-group">
            <button class="btn btn-secondary" @click="resetFilter">
                <font-awesome-icon :icon="['fas', 'sync-alt']" /> Đặt lại
            </button>

            <button class="btn btn-outline" @click="exportExcel">
                <font-awesome-icon :icon="['fas', 'file-excel']" /> Xuất Excel
            </button>
            
            <button class="btn btn-gradient" @click="$router.push('/admin/products/create')">
                <font-awesome-icon :icon="['fas', 'plus']" /> Tạo mới
            </button>
        </div>
      </div>
    </div>

    <div class="table-container">
      <div class="table-responsive">
        <table>
          <thead>
            <tr>
              <th class="text-center" width="50">STT</th>
              <th>Mã</th>
              <th>Tên Sản Phẩm</th>
              <th class="text-center">Ngày thêm</th>
              <th class="text-center">Số lượng tồn</th>
              <th class="text-center">Trạng thái</th>
              <th class="text-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading"><td colspan="7" class="text-center py-4">Đang tải dữ liệu...</td></tr>
            <tr v-else-if="items.length === 0"><td colspan="7" class="text-center py-4 empty-state">Không tìm thấy sản phẩm nào.</td></tr>
            
            <tr v-else v-for="(item, index) in items" :key="item.id">
              <td class="text-center">{{ (page - 1) * pageSize + index + 1 }}</td>
              <td class="text-gray">{{ item.maSanPham }}</td>
              <td class="font-medium text-primary">{{ item.tenSanPham }}</td>
              
              <td class="text-center col-ngay-tao">{{ item.ngayTao }}</td>
              
              <td class="text-center font-bold">{{ item.soLuong }}</td>
              <td class="text-center">
                 <span :class="['badge', item.trangThai === 1 ? 'badge-success' : 'badge-danger']">
                    {{ item.trangThai === 1 ? 'Đang bán' : 'Ngừng bán' }}
                 </span>
              </td>
              <td class="text-center">
                 <button class="action-btn" @click="$router.push(`/admin/products/${item.id}`)" title="Chỉnh sửa">
                    <font-awesome-icon :icon="['far', 'pen-to-square']" />
                 </button>
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
            sản phẩm / trang
         </div>
         <div class="page-controls">
            <button class="page-btn" :disabled="page === 1" @click="changePage(page - 1)">‹</button>
            <button 
                v-for="p in visiblePages" 
                :key="p" 
                class="page-btn" 
                :class="{ active: p === page }" 
                @click="changePage(p)"
            >
                {{ p }}
            </button>
            <button class="page-btn" :disabled="page === totalPages" @click="changePage(page + 1)">›</button>
         </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import request from '@/services/request'; 
import Swal from 'sweetalert2';

// STATE
const items = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);

// Filter object
const filter = reactive({ 
    keyword: '', 
    status: null 
});

// FETCH DATA
const fetchProducts = async () => {
    loading.value = true;
    try {
        const res = await request.get('/products', {
            params: {
                page: page.value - 1,
                size: pageSize.value,
                keyword: filter.keyword,
                status: filter.status
            }
        });
        items.value = res.data.content;
        totalPages.value = res.data.totalPages;
    } catch (e) {
        console.error(e);
    } finally {
        loading.value = false;
    }
};

// RESET FILTER (Mới)
const resetFilter = () => {
    filter.keyword = '';
    filter.status = null;
    page.value = 1;
    fetchProducts();
};

// EXPORT EXCEL
const exportExcel = async () => {
    const confirmRes = await Swal.fire({
        title: 'Xác nhận', 
        text: 'Bạn có muốn tải xuống danh sách sản phẩm không?',
        icon: 'question',
        showCancelButton: true, 
        confirmButtonText: 'Có', 
        cancelButtonText: 'Hủy'
    });

    if (!confirmRes.isConfirmed) return;

    try {
        const response = await request.get('/products/export', { responseType: 'blob' });
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a'); 
        link.href = url;
        const dateStr = new Date().toISOString().slice(0,10);
        link.setAttribute('download', `DS_SanPham_${dateStr}.xlsx`);
        document.body.appendChild(link); 
        link.click(); 
        document.body.removeChild(link); 
        window.URL.revokeObjectURL(url);
        
        const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 2000 });
        Toast.fire({ icon: 'success', title: 'Xuất Excel thành công' });
    } catch (e) {
        console.error("Lỗi xuất Excel:", e);
        Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Không thể xuất file Excel.' });
    }
};

// UTILS
const changePage = (p) => { if (p >= 1 && p <= totalPages.value) { page.value = p; fetchProducts(); } };
const handlePageSizeChange = () => { page.value = 1; fetchProducts(); };

const visiblePages = computed(() => {
    let p = [];
    for (let i = 1; i <= totalPages.value; i++) {
        if (i === 1 || i === totalPages.value || (i >= page.value - 1 && i <= page.value + 1)) p.push(i);
    }
    return p;
});

onMounted(() => {
    fetchProducts();
});
</script>

<style scoped>
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background-color: #f8fafc; min-height: 100vh; }
.header-section { margin-bottom: 20px; }
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; }

/* === UPDATE: CARD STYLING (Viền xanh + Bo góc) === */
.control-panel, .table-container { 
    background: #fff; 
    border-radius: 16px; /* Bo góc 16px */
    border: 1px solid #bfdbfe !important; /* Viền xanh nhạt */
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); 
    padding: 24px; 
    margin-bottom: 20px;
}

/* HEADER & FILTER LAYOUT */
.controls-row { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.filter-group { display: flex; gap: 20px; align-items: center; flex-wrap: wrap; }

.search-wrap { position: relative; width: 300px; }
.search-wrap input { width: 100%; padding: 10px 12px 10px 36px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; font-size: 14px; }
.search-wrap input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.search-wrap .search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; }

/* Radio Button Status */
.status-filter { display: flex; align-items: center; gap: 15px; font-size: 14px; }
.status-filter .label { font-weight: 600; color: #334155; }
.radio-item { cursor: pointer; display: flex; align-items: center; gap: 6px; color: #475569; }
.radio-item input { accent-color: #0f172a; width: 16px; height: 16px; cursor: pointer; }

/* Action Buttons */
.action-group { display: flex; gap: 10px; }

/* === BUTTON STYLES === */
.btn { 
    height: 38px; 
    padding: 0 16px; 
    border-radius: 6px; 
    font-weight: 600; 
    cursor: pointer; 
    font-size: 13px; 
    border: 1px solid transparent; 
    transition: 0.2s;
    display: flex; align-items: center; gap: 8px;
}

/* Button Gradient (Tạo mới) */
.btn-gradient { 
    background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
    color: #fff; 
    box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); 
}
.btn-gradient:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.3); }

/* Button Secondary (Đặt lại) */
.btn-secondary { background: #334155; color: #fff; }
.btn-secondary:hover { background: #1e293b; }

/* Button Outline (Xuất Excel) */
.btn-outline { background: #fff; border-color: #cbd5e1; color: #475569; }
.btn-outline:hover { background: #f8fafc; border-color: #94a3b8; color: #0f172a; }

/* TABLE STYLES */
.table-responsive { overflow-x: auto; border-radius: 8px; border: 1px solid #e2e8f0; }
table { width: 100%; border-collapse: collapse; }

/* Header Bảng (Xanh nhạt) */
th { 
    background: #eff6ff; /* Màu nền xanh nhạt */
    padding: 14px; 
    font-weight: 700; 
    color: #1e40af; 
    font-size: 13px; 
    text-transform: uppercase; 
    text-align: left; 
    border-bottom: none; 
}

td { padding: 12px 14px; border-bottom: 1px solid #f1f5f9; font-size: 14px; vertical-align: middle; color: #334155; }
.text-center { text-align: center; }
.font-medium { font-weight: 600; }
.font-bold { font-weight: 700; }
.text-primary { color: #0f172a; }
.text-gray { color: #64748b; font-family: monospace; }
.col-ngay-tao { color: #334155; }
.empty-state { font-style: italic; color: #94a3b8; }

/* Badges */
.badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; border: 1px solid transparent; }
.badge-success { background: #dcfce7; color: #166534; border-color: #bbf7d0; }
.badge-danger { background: #fee2e2; color: #991b1b; border-color: #fecaca; }

.action-btn { width: 32px; height: 32px; border-radius: 4px; border: 1px solid #e2e8f0; background: #fff; cursor: pointer; color: #475569; transition: 0.2s; display: inline-flex; align-items: center; justify-content: center; }
.action-btn:hover { background: #f1f5f9; color: #0f172a; border-color: #cbd5e1; }

/* PAGINATION */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding-top: 15px; border-top: 1px solid #f1f5f9; }
.page-info { font-size: 13px; color: #64748b; font-weight: 500; }
.page-info select { padding: 4px 8px; border: 1px solid #cbd5e1; border-radius: 4px; margin: 0 5px; outline: none; cursor: pointer; }

.page-controls { display: flex; gap: 5px; }
.page-btn { 
    min-width: 32px; height: 32px; 
    border: 1px solid #e2e8f0; background: #fff; 
    border-radius: 4px; cursor: pointer; 
    color: #64748b; font-weight: 500;
    display: flex; align-items: center; justify-content: center;
}
.page-btn:hover:not(:disabled) { border-color: #0f172a; color: #0f172a; }
.page-btn.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; background: #f8fafc; }

/* Responsive */
@media (max-width: 992px) {
    .controls-row { flex-direction: column; align-items: flex-start; }
    .action-group { width: 100%; justify-content: flex-start; margin-top: 10px; }
    .filter-group { width: 100%; justify-content: space-between; }
    .search-wrap { width: 100%; max-width: none; }
}
</style>