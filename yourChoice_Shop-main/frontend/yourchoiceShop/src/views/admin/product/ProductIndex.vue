<template>
  <div class="product-page">
    <div class="breadcrumb">
      <span class="text-gray">Quản lý sản phẩm</span>
      <span class="separator">/</span>
      <span class="text-bold">Sản phẩm</span>
    </div>

    <div class="card">
      <div class="card-header">
        <h3>Quản lý sản phẩm / Sản phẩm</h3>
      </div>

      <div class="toolbar">
        <div class="toolbar-left">
          <div class="search-box">
            <span class="search-icon">🔍</span>
            <input 
              v-model="filter.keyword" 
              type="text" 
              placeholder="Tìm sản phẩm..." 
              @keyup.enter="handleSearch"
            />
          </div>
          
          <div class="status-filter">
            <span class="label">Trạng thái: </span>
            <label class="radio-item">
              <input type="radio" :value="null" v-model="filter.status" @change="fetchProducts"> Tất cả
            </label>
            <label class="radio-item">
              <input type="radio" :value="1" v-model="filter.status" @change="fetchProducts"> Đang bán
            </label>
            <label class="radio-item">
              <input type="radio" :value="0" v-model="filter.status" @change="fetchProducts"> Ngừng bán
            </label>
          </div>
        </div>

        <div class="toolbar-right">
          <button class="btn btn-outline">
            📥 Xuất Excel
          </button>
          <button class="btn btn-primary" @click="$router.push('/admin/products/create')">
            + Tạo mới
          </button>
        </div>
      </div>

      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th style="width: 50px; text-align: center;">STT</th>
              <th style="text-align: left;">Tên</th>
              <th style="text-align: center;">Ngày thêm</th>
              <th style="text-align: center;">Số lượng</th>
              <th style="text-align: center;">Trạng thái</th>
              <th style="text-align: center;">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
                <td colspan="6" class="text-center py-4">
                  <div class="loading-spinner">⏳ Đang tải dữ liệu từ Server...</div>
                </td>
            </tr>
            
            <tr v-else-if="products.length === 0">
                <td colspan="6" class="text-center py-4">Không tìm thấy sản phẩm nào.</td>
            </tr>

            <tr v-else v-for="(product, index) in products" :key="product.id">
              <td class="text-center">{{ (page - 1) * pageSize + index + 1 }}</td>
              
              <td class="text-left font-medium">
                <div class="product-name-col">
                  <span>{{ product.tenSanPham }}</span>
                  <span class="sub-text">{{ product.maSanPham }}</span>
                </div>
              </td>
              
              <td class="text-center text-gray">{{ formatDate(product.ngayTao) }}</td>
              
              <td class="text-center font-bold">{{ product.tongSoLuongTon }}</td>
              
              <td class="text-center">
                <span :class="['badge', getStatusClass(product.trangThai)]">
                  {{ product.trangThai === 1 ? 'Đang bán' : 'Ngừng bán' }}
                </span>
              </td>
              
              <td class="text-center action-col">
                <button class="btn-icon" title="Xem chi tiết" @click="goToDetail(product.id)">
                  👁️
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-container" v-if="products.length > 0">
        <div class="page-info">
          Hiển thị <select v-model="pageSize" @change="handlePageSizeChange">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
          </select> sản phẩm / trang
        </div>

        <div class="pagination-controls">
          <button :disabled="page === 1" @click="changePage(page - 1)">‹</button>
          
          <button 
            v-for="p in visiblePages" 
            :key="p"
            :class="{ active: p === page }"
            @click="changePage(p)"
          >
            {{ p }}
          </button>
          
          <button :disabled="page === totalPages" @click="changePage(page + 1)">›</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios'; // IMPORT AXIOS

const router = useRouter();

// --- STATE ---
const products = ref([]);
const loading = ref(false);
const page = ref(1);        // Trang hiện tại (Frontend đếm từ 1)
const pageSize = ref(10);   // Kích thước trang
const totalPages = ref(1);  // Tổng số trang từ Server trả về

const filter = reactive({
  keyword: '',
  status: null 
});

// --- API CONFIG ---
const API_URL = 'http://localhost:8080/api/v1/products'; 

// --- COMPUTED ---
// Logic để hiển thị số trang (Ví dụ: 1, 2, 3... thay vì in hết 100 trang)
const visiblePages = computed(() => {
  let pages = [];
  for (let i = 1; i <= totalPages.value; i++) {
    // Chỉ hiện trang đầu, trang cuối, và trang xung quanh trang hiện tại
    if (i === 1 || i === totalPages.value || (i >= page.value - 2 && i <= page.value + 2)) {
      pages.push(i);
    }
  }
  return pages;
});

// --- HELPER FUNCTIONS ---
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  // Format dd/mm/yyyy HH:mm
  return new Intl.DateTimeFormat('vi-VN', { 
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  }).format(date);
};

const getStatusClass = (status) => {
  return status === 1 ? 'badge-success' : 'badge-danger';
};

// --- ACTION HANDLERS ---
const handleSearch = () => {
  page.value = 1; // Reset về trang 1 khi tìm kiếm
  fetchProducts();
};

const handlePageSizeChange = () => {
  page.value = 1; // Reset về trang 1 khi đổi kích thước
  fetchProducts();
};

const changePage = (newPage) => {
    if(newPage >= 1 && newPage <= totalPages.value) {
        page.value = newPage;
        fetchProducts();
    }
}

const goToDetail = (id) => {
    router.push(`/admin/products/${id}`); // Chuyển sang trang chi tiết (Sẽ làm sau)
}

// --- MAIN FETCH FUNCTION ---
const fetchProducts = async () => {
  loading.value = true;
  try {
    // Gọi API Spring Boot
    const response = await axios.get(API_URL, {
      params: {
        page: page.value - 1, // Quan trọng: Spring Boot trang bắt đầu từ 0, Vue từ 1
        size: pageSize.value,
        keyword: filter.keyword
      }
    });

    // Binding dữ liệu từ Spring Page<ProductResponse>
    const data = response.data;
    products.value = data.content;         // List sản phẩm nằm trong 'content'
    totalPages.value = data.totalPages;    // Tổng số trang
    
    // Nếu trang hiện tại lớn hơn tổng trang (do xóa bớt data), lùi về trang 1
    if (page.value > totalPages.value && totalPages.value > 0) {
      page.value = 1;
      fetchProducts();
    }

  } catch (error) {
    console.error("Lỗi khi tải danh sách sản phẩm:", error);
    alert("Không thể kết nối đến Server!");
  } finally {
    loading.value = false;
  }
};

// --- LIFECYCLE ---
onMounted(() => {
  fetchProducts();
});
</script>

<style scoped>
/* --- GIỮ NGUYÊN CSS CŨ VÀ THÊM MỘT CHÚT CHO ĐẸP HƠN --- */
.product-page {
  font-family: 'Segoe UI', sans-serif;
  color: #333;
}

.breadcrumb {
  margin-bottom: 16px;
  font-size: 14px;
}
.breadcrumb .text-gray { color: #64748b; }
.breadcrumb .separator { margin: 0 8px; color: #cbd5e1; }
.breadcrumb .text-bold { font-weight: 600; color: #0f172a; }

.card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  padding: 24px;
}
.card-header h3 { display: none; }

/* TOOLBAR */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.toolbar-left { display: flex; flex-direction: column; gap: 12px; }

.search-box { position: relative; width: 300px; }
.search-box input {
  width: 100%;
  padding: 8px 12px 8px 36px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  outline: none;
  font-size: 14px;
}
.search-box input:focus { border-color: #3b82f6; }
.search-box .search-icon {
  position: absolute; left: 10px; top: 50%; transform: translateY(-50%);
  color: #94a3b8; font-size: 12px;
}

.status-filter { display: flex; align-items: center; gap: 16px; font-size: 14px; }
.status-filter .label { font-weight: 600; }
.radio-item { display: flex; align-items: center; gap: 6px; cursor: pointer; }
.radio-item input[type="radio"] { accent-color: #0f172a; }

.toolbar-right { display: flex; gap: 10px; }
.btn {
    padding: 8px 16px; border-radius: 4px; font-size: 14px; font-weight: 500;
    cursor: pointer; border: 1px solid transparent; transition: all 0.2s;
}
.btn-outline { background: #fff; border-color: #cbd5e1; color: #475569; }
.btn-outline:hover { background: #f8fafc; }
.btn-primary { background: #fff; border: 1px solid #0f172a; color: #0f172a; font-weight: 600; }
.btn-primary:hover { background: #0f172a; color: #fff; }

/* TABLE */
.table-container { overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 4px; }
table { width: 100%; border-collapse: collapse; }
th {
    background: #f8fafc; padding: 12px 16px; font-weight: 600; color: #475569;
    font-size: 13px; text-transform: uppercase; border-bottom: 1px solid #e2e8f0;
}
td { padding: 12px 16px; border-bottom: 1px solid #f1f5f9; font-size: 14px; vertical-align: middle; }
tr:last-child td { border-bottom: none; }
tr:hover { background-color: #f8fafc; }

/* Product Name Styling */
.product-name-col { display: flex; flex-direction: column; }
.sub-text { font-size: 12px; color: #94a3b8; margin-top: 2px; }

/* UTILS */
.text-center { text-align: center; }
.text-left { text-align: left; }
.font-medium { font-weight: 500; color: #1e293b; }
.font-bold { font-weight: 700; color: #334155; }
.text-gray { color: #64748b; }
.py-4 { padding-top: 1rem; padding-bottom: 1rem; }

/* BADGES */
.badge {
    padding: 4px 12px; border-radius: 9999px; font-size: 12px; font-weight: 600; display: inline-block;
}
.badge-success { background-color: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.badge-danger { background-color: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }

/* ACTION ICON */
.btn-icon {
    background: none; border: none; cursor: pointer; font-size: 16px; color: #64748b;
    padding: 4px; border-radius: 4px;
}
.btn-icon:hover { background-color: #f1f5f9; color: #0f172a; }

/* PAGINATION */
.pagination-container {
    display: flex; justify-content: space-between; align-items: center;
    margin-top: 20px; padding-top: 20px; border-top: 1px solid #f1f5f9;
}
.page-info { color: #64748b; font-size: 14px; }
.page-info select { border: 1px solid #e2e8f0; border-radius: 4px; padding: 2px 4px; outline: none; }

.pagination-controls { display: flex; gap: 4px; }
.pagination-controls button {
    width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;
    border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; cursor: pointer;
    color: #64748b; transition: all 0.2s;
}
.pagination-controls button:hover:not(:disabled) { border-color: #cbd5e1; background-color: #f8fafc; }
.pagination-controls button.active {
    background-color: #eff6ff; color: #3b82f6; border-color: #3b82f6; font-weight: 600;
}
.pagination-controls button:disabled { opacity: 0.5; cursor: not-allowed; }
</style>