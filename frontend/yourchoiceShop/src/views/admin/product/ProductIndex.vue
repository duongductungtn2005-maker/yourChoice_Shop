<template>
  <div class="product-page">
    <div class="header-section">
      <h1 class="page-title">Quản lý sản phẩm / Sản phẩm</h1>
    </div>

    <div class="card">
      <div class="card-header">
         <div class="filter-section">
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
            <button class="btn btn-outline" @click="exportExcel">
                <font-awesome-icon :icon="['fas', 'file-excel']" /> Xuất Excel
            </button>
            
            <button class="btn btn-primary" @click="$router.push('/admin/products/create')">
                <font-awesome-icon :icon="['fas', 'plus']" /> Tạo mới
            </button>
         </div>
      </div>

      <div class="table-responsive">
        <table>
          <thead>
            <tr>
              <th class="text-center">STT</th>
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
            <tr v-else-if="items.length === 0"><td colspan="7" class="text-center py-4">Không tìm thấy sản phẩm nào.</td></tr>
            
            <tr v-else v-for="(item, index) in items" :key="item.id">
              <td class="text-center">{{ (page - 1) * pageSize + index + 1 }}</td>
              <td class="text-gray">{{ item.maSanPham }}</td>
              <td class="font-medium text-primary">{{ item.tenSanPham }}</td>
              
              <td class="text-center text-gray">{{ item.ngayTao }}</td>
              
              <td class="text-center font-bold">{{ item.soLuong }}</td>
              <td class="text-center">
                 <span :class="['badge', item.trangThai === 1 ? 'badge-success' : 'badge-danger']">
                    {{ item.trangThai === 1 ? 'Đang bán' : 'Ngừng bán' }}
                 </span>
              </td>
              <td class="text-center">
                 <button class="action-btn" @click="$router.push(`/admin/products/${item.id}`)">
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
            <button :disabled="page === 1" @click="changePage(page - 1)">‹</button>
            <button v-for="p in visiblePages" :key="p" :class="{ active: p === page }" @click="changePage(p)">{{ p }}</button>
            <button :disabled="page === totalPages" @click="changePage(page + 1)">›</button>
         </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
// UPDATE: Sử dụng request chung thay vì axios trực tiếp
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
        // UPDATE: Dùng request.get, bỏ bớt phần 'http://localhost...' vì đã có baseURL
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
        // Có thể thêm thông báo lỗi nhẹ nếu muốn
    } finally {
        loading.value = false;
    }
};

// EXPORT EXCEL
const exportExcel = async () => {
    try {
        const response = await request.get('/products/export', {
            responseType: 'blob' 
        });

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
        Toast.fire({ icon: 'success', title: 'Đã tải xuống file Excel!' });

    } catch (e) {
        console.error("Lỗi xuất Excel:", e);
        Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Có lỗi xảy ra khi xuất file!' });
    }
};

// UTILS
const changePage = (p) => { if (p >= 1 && p <= totalPages.value) { page.value = p; fetchProducts(); } };
const handlePageSizeChange = () => { page.value = 1; fetchProducts(); };

// REMOVE: Đã xóa hàm formatDate gây lỗi "Invalid Date"

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
/* GENERAL STYLES */
.product-page { font-family: 'Segoe UI', sans-serif; color: #333; background-color: #f8fafc; min-height: 100vh; padding: 20px; }
.header-section { margin-bottom: 20px; }
.breadcrumb { font-size: 14px; color: #64748b; } .breadcrumb .active { font-weight: 600; color: #0f172a; }
.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 20px; }

/* HEADER & FILTER STYLES */
.card-header { margin-bottom: 20px; }
.filter-section { margin-bottom: 15px; }

.search-wrap { position: relative; width: 100%; max-width: 400px; margin-bottom: 15px; }
.search-wrap input { width: 100%; padding: 10px 12px 10px 36px; border: 1px solid #e2e8f0; border-radius: 4px; outline: none; font-size: 14px; }
.search-wrap input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.search-wrap .search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: #94a3b8; }

/* Radio Button Status */
.status-filter { display: flex; align-items: center; gap: 15px; font-size: 14px; }
.status-filter .label { font-weight: 600; color: #334155; }
.radio-item { cursor: pointer; display: flex; align-items: center; gap: 6px; color: #475569; }
.radio-item input { accent-color: #0f172a; width: 16px; height: 16px; cursor: pointer; }

/* Action Buttons */
.action-group { display: flex; justify-content: flex-end; gap: 10px; margin-top: -40px; } /* Đẩy lên ngang hàng với search nếu màn hình rộng */

/* BUTTON STYLES (Cập nhật để đồng bộ với các màn khác) */
.btn { 
    height: 38px; /* Chiều cao cố định cho các nút bằng nhau */
    padding: 0 16px; 
    border-radius: 4px; 
    font-weight: 600; 
    cursor: pointer; 
    font-size: 14px; 
    border: 1px solid transparent; 
    transition: 0.2s;
    display: flex; /* Flex để căn giữa icon và chữ */
    align-items: center;
    gap: 8px; /* Khoảng cách giữa icon và chữ */
}

.btn-primary { 
    background: #0f172a; 
    color: #fff; 
} 
.btn-primary:hover { 
    background: #1e293b; 
}

.btn-outline { 
    background: #fff; 
    border-color: #cbd5e1; 
    color: #475569; 
} 
.btn-outline:hover { 
    background: #f1f5f9; 
    border-color: #94a3b8;
}

/* TABLE */
.table-responsive { overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 4px; }
table { width: 100%; border-collapse: collapse; }
th { background: #f8fafc; padding: 12px; font-weight: 600; color: #475569; border-bottom: 1px solid #e2e8f0; font-size: 13px; text-transform: uppercase; text-align: left; }
td { padding: 12px; border-bottom: 1px solid #f1f5f9; font-size: 14px; vertical-align: middle; }
.text-center { text-align: center; } .font-medium { font-weight: 500; } .font-bold { font-weight: 700; }
.text-primary { color: #0f172a; } .text-gray { color: #64748b; }

.badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.badge-success { background: #dcfce7; color: #166534; } 
.badge-danger { background: #fee2e2; color: #991b1b; }

.action-btn { background: none; border: none; cursor: pointer; font-size: 18px; color: #475569; transition: 0.2s; }
.action-btn:hover { color: #0f172a; transform: scale(1.1); }
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }

/* PAGINATION */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding-top: 15px; border-top: 1px solid #f1f5f9; }
.page-info { font-size: 14px; color: #64748b; }
.page-info select { padding: 4px 8px; border: 1px solid #cbd5e1; border-radius: 4px; margin: 0 5px; outline: none; }
.page-controls button { width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; margin-left: 5px; cursor: pointer; color: #64748b; }
.page-controls button.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.page-controls button:disabled { opacity: 0.5; cursor: not-allowed; }

/* Responsive adjustments */
@media (max-width: 768px) {
    .action-group { margin-top: 10px; justify-content: flex-start; }
    .status-filter { flex-wrap: wrap; }
}
</style>