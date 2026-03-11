<template>
  <div class="product-view">
    
    <div class="page-header-small">
      <div class="header-overlay"></div>
      <div class="container header-content">
        <h1>CỬA HÀNG</h1>
        <div class="breadcrumb">
          <span @click="$router.push('/')">Trang chủ</span> / <span>Sản phẩm</span>
        </div>
      </div>
    </div>

    <div class="container page-content" id="product-section">
      
      <aside class="sidebar-filter">
        <div class="filter-block">
          <h3>Khoảng giá</h3>
          <div class="price-range">
            <input 
                type="range" 
                v-model.number="filter.maxPrice" 
                min="0" 
                max="5000000" 
                step="50000" 
                class="range-input"
            >
            <div class="range-labels">
                <span>0đ</span>
                <span>{{ formatMoney(filter.maxPrice) }}</span>
            </div>
          </div>
        </div>

        <div class="filter-block">
          <h3>Thương hiệu</h3>
          <div class="checkbox-list">
            <label class="custom-checkbox" v-for="th in attributes.thuongHieu" :key="th.id">
                <input type="checkbox" :value="th.id" v-model="filter.idThuongHieu">
                <span class="checkmark"></span>
                {{ th.tenThuongHieu }}
            </label>
          </div>
        </div>

        <div class="filter-block">
          <h3>Chất liệu</h3>
          <div class="checkbox-list">
            <label class="custom-checkbox" v-for="cl in attributes.chatLieu" :key="cl.id">
              <input type="checkbox" :value="cl.id" v-model="filter.idChatLieu">
              <span class="checkmark"></span>
              {{ cl.tenChatLieu }}
            </label>
          </div>
        </div>

        <div class="filter-block">
          <h3>Màu sắc</h3>
          <div class="color-options">
             <div 
               v-for="ms in attributes.mauSac" 
               :key="ms.id" 
               class="color-circle" 
               :class="{ active: filter.idMauSac === ms.id }"
               :style="{ backgroundColor: getColorCode(ms.tenMauSac) }"
               @click="toggleColor(ms.id)"
               :title="ms.tenMauSac"
             ></div>
          </div>
        </div>

        <button class="btn-filter-apply" @click="fetchProducts">Lọc sản phẩm</button>
      </aside>

      <div class="product-area">
        <div class="product-top-bar">
          <span class="result-count">Hiển thị <strong>{{ products.length }}</strong> sản phẩm</span>
          <div class="sort-select">
             <select v-model="sortBy" @change="fetchProducts">
                <option value="newest">Mới nhất</option>
                <option value="price_asc">Giá: Thấp đến cao</option>
                <option value="price_desc">Giá: Cao đến thấp</option>
             </select>
          </div>
        </div>

        <div v-if="loading" class="loading-grid">
           <div class="spinner"></div>
        </div>

        <div v-else-if="products.length > 0" class="product-grid">
           <div 
              v-for="prod in products" 
              :key="prod.id" 
              class="product-card"
              @click="$router.push(`/product/${prod.id}`)"
           >
              <div class="product-image">
                 <img :src="getProductImage(prod)" alt="Product">
                 <div class="overlay-actions">
                    <button class="btn-quick-view">Xem nhanh</button>
                 </div>
                 <div v-if="prod.phanTramGiam > 0" class="sale-tag">-{{ prod.phanTramGiam }}%</div>
                 <div v-if="prod.soLuong <= 0" class="sold-out-overlay">
                    <span class="sold-out-text">Đã hết hàng</span>
                 </div>
              </div>
              
              <div class="product-info">
                 <div class="brand-name">{{ prod.tenThuongHieu }}</div>
                 <h3 class="product-name">{{ prod.tenSanPham }}</h3>
                 <div class="product-price">
                    <span class="current-price">{{ formatMoney(calculateMinPrice(prod)) }}</span>
                    <span v-if="calculateMinPrice(prod) < prod.giaGoc" class="old-price">{{ formatMoney(prod.giaGoc) }}</span>
                 </div>
                 
                 <div class="preview-colors">
                    <span v-for="(color, idx) in getPreviewColors(prod)" :key="idx" :style="{background: getColorCode(color)}"></span>
                 </div>
              </div>
           </div>
        </div>

        <div v-else class="empty-state">
           <i class="far fa-sad-tear"></i>
           <p>Không tìm thấy sản phẩm nào phù hợp.</p>
        </div>

        <div class="pagination" v-if="totalPages > 1">
           <button :disabled="page === 0" @click="changePage(page - 1)">«</button>
           <span>Trang {{ page + 1 }} / {{ totalPages }}</span>
           <button :disabled="page === totalPages - 1" @click="changePage(page + 1)">»</button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue';
import axios from 'axios';

// --- CẤU HÌNH ---
const API_URL = 'http://localhost:8080/api/v1';
// SỬA LẠI ĐƯỜNG DẪN NÀY CHO KHỚP VỚI API ẢNH CỦA BẠN
const IMAGE_BASE_URL = 'http://localhost:8080/images/';
// --- STATE ---
const loading = ref(false);
const products = ref([]);
const attributes = reactive({ thuongHieu: [], chatLieu: [], mauSac: [] });
const page = ref(0);
const totalPages = ref(1);
const sortBy = ref('newest');

const filter = reactive({
  keyword: '',
  maxPrice: 5000000,
  idThuongHieu: [],
  idChatLieu: [],
  idMauSac: null
});

// --- API ACTIONS ---
const fetchAttributes = async () => {
  try {
    const [th, cl, ms] = await Promise.all([
      axios.get(`${API_URL}/thuong-hieu`, { params: { status: 1, size: 100 } }),
      axios.get(`${API_URL}/chat-lieu`, { params: { status: 1, size: 100 } }),
      axios.get(`${API_URL}/mau-sac`, { params: { status: 1, size: 100 } })
    ]);
    attributes.thuongHieu = th.data.content || [];
    attributes.chatLieu = cl.data.content || [];
    attributes.mauSac = ms.data.content || [];
  } catch(e) { console.error("Lỗi tải thuộc tính:", e); }
};

const fetchProducts = async () => {
  loading.value = true;
  try {
    const params = {
      page: page.value,
      size: 12,
      keyword: filter.keyword,
      status: 1,
      idThuongHieu: filter.idThuongHieu.length > 0 ? filter.idThuongHieu[0] : null,
      idChatLieu: filter.idChatLieu.length > 0 ? filter.idChatLieu[0] : null,
      idMauSac: filter.idMauSac,
      maxPrice: filter.maxPrice,
      sort: sortBy.value 
    };
    
    const res = await axios.get(`${API_URL}/products`, { params });
    // Xử lý an toàn nếu API trả về cấu trúc khác
    products.value = res.data.content || [];
    totalPages.value = res.data.totalPages || 1;
    
    // Debug dữ liệu để kiểm tra giá
    console.log("Dữ liệu sản phẩm trả về:", products.value);
  } catch (e) {
    console.error("Lỗi tải sản phẩm:", e);
    products.value = [];
  } finally {
    loading.value = false;
  }
};

// --- HELPER FUNCTIONS ---
const formatMoney = (val) => {
    if (!val) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
};

const getColorCode = (name) => {
    const map = { 'Đỏ': '#ef4444', 'Xanh': '#3b82f6', 'Vàng': '#eab308', 'Đen': '#000', 'Trắng': '#eee', 'Tím': '#a855f7', 'Xám': '#6b7280', 'Cam': '#f97316', 'Hồng': '#ec4899', 'Nâu': '#78350f' };
    for(let k in map) if(name && name.includes(k)) return map[k];
    return '#ccc';
};

const toggleColor = (id) => {
  filter.idMauSac = filter.idMauSac === id ? null : id;
  // Watcher sẽ tự gọi API
};

// Hàm Debounce tự viết (Thay thế lodash)
const debounceFn = (fn, delay) => {
  let timeoutId;
  return (...args) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => fn(...args), delay);
  };
};

const debouncedFetch = debounceFn(() => {
    page.value = 0; 
    fetchProducts();
}, 500);

// --- WATCHERS ---
watch(filter, () => { debouncedFetch(); }, { deep: true });
watch(sortBy, () => { fetchProducts(); });

// --- LOGIC GIÁ & ẢNH ---
const calculateMinPrice = (prod) => {
    const giaBanMin = Number(prod.giaBanMin);
    if (giaBanMin > 0) return giaBanMin;
    return 0; 
};

const getProductImage = (prod) => {
    // Dùng ảnh chính từ ProductResponse
    if (prod.anhChinh) {
        if (prod.anhChinh.startsWith('http')) return prod.anhChinh;
        return `${IMAGE_BASE_URL}${prod.anhChinh}`;
    }
    return 'https://placehold.co/300x400?text=No+Image';
};

const getPreviewColors = (prod) => {
    return prod.dsMauSac ? prod.dsMauSac.split(', ') : []; 
};

const changePage = (p) => {
  if (p >= 0 && p < totalPages.value) {
    page.value = p;
    fetchProducts();
    document.getElementById('product-section').scrollIntoView({ behavior: 'smooth' });
  }
};

let stockInterval;
const onVisibilityChange = () => {
    if (document.visibilityState === 'visible') fetchProducts();
};

onMounted(() => {
  fetchAttributes();
  fetchProducts();
  document.addEventListener('visibilitychange', onVisibilityChange);
  stockInterval = setInterval(fetchProducts, 30000);
});

onUnmounted(() => {
  document.removeEventListener('visibilitychange', onVisibilityChange);
  if (stockInterval) clearInterval(stockInterval);
});
</script>

<style scoped>
/* (Giữ nguyên phần Style cũ của bạn) */
/* GLOBAL FONT */
.product-view { font-family: Arial, sans-serif; font-size: 16px; color: #333; }
.container { max-width: 1280px; margin: 0 auto; padding: 0 20px; width: 100%; box-sizing: border-box; }

/* PAGE HEADER SMALL */
.page-header-small {
  background-image: url('https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=1600&q=80');
  background-size: cover; background-position: center; height: 200px; position: relative;
  display: flex; align-items: center; justify-content: center; margin-bottom: 40px;
}
.header-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.5); }
.header-content { position: relative; z-index: 1; text-align: center; color: #fff; }
.header-content h1 { font-size: 36px; font-weight: 700; margin-bottom: 10px; letter-spacing: 1px; }
.breadcrumb { font-size: 15px; color: #e2e8f0; }
.breadcrumb span { cursor: pointer; transition: 0.2s; }
.breadcrumb span:first-child:hover { color: #fff; text-decoration: underline; }

/* LAYOUT */
.page-content { display: grid; grid-template-columns: 280px 1fr; gap: 40px; margin-bottom: 60px; }

/* SIDEBAR */
.sidebar-filter { padding-right: 25px; border-right: 1px solid #f1f5f9; }
.filter-block { margin-bottom: 35px; }
.filter-block h3 { font-size: 18px; margin-bottom: 18px; font-weight: 700; text-transform: uppercase; color: #0f172a; }

.checkbox-list { display: flex; flex-direction: column; gap: 12px; }
.custom-checkbox { display: flex; align-items: center; gap: 12px; cursor: pointer; font-size: 16px; color: #475569; transition: 0.2s; }
.custom-checkbox:hover { color: #1e3a8a; }
.custom-checkbox input { display: none; }
.checkmark { width: 20px; height: 20px; border: 1px solid #cbd5e1; border-radius: 4px; position: relative; transition: 0.2s; }
.custom-checkbox input:checked + .checkmark { background: #1e3a8a; border-color: #1e3a8a; }
.custom-checkbox input:checked + .checkmark::after { content: '✓'; color: white; position: absolute; top: -1px; left: 4px; font-size: 14px; }

.color-options { display: flex; flex-wrap: wrap; gap: 12px; }
.color-circle { width: 28px; height: 28px; border-radius: 50%; cursor: pointer; border: 1px solid #e2e8f0; position: relative; }
.color-circle.active { border: 2px solid #1e3a8a; transform: scale(1.1); box-shadow: 0 0 0 2px rgba(30, 58, 138, 0.1); }

.range-input { width: 100%; accent-color: #1e3a8a; }
.range-labels { display: flex; justify-content: space-between; font-size: 15px; color: #64748b; margin-top: 8px; font-weight: 600; }

.btn-filter-apply { 
  width: 100%; padding: 12px; border: none; cursor: pointer; 
  font-weight: 700; font-size: 16px; margin-top: 15px; transition: 0.2s; border-radius: 6px;
  background: #0f172a; color: #fff; 
}
.btn-filter-apply:hover { background: #1e3a8a; }

/* PRODUCT AREA */
.product-top-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.result-count { font-size: 16px; color: #64748b; }
.sort-select select { padding: 10px 15px; border: 1px solid #cbd5e1; outline: none; font-family: Arial, sans-serif; font-size: 15px; border-radius: 6px; color: #334155; }

.product-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 30px; }

.product-card { cursor: pointer; transition: 0.3s; }
.product-card:hover .product-image img { transform: scale(1.05); }
.product-card:hover .btn-quick-view { opacity: 1; transform: translateY(0); }

.product-image { position: relative; overflow: hidden; aspect-ratio: 3/4; background: #f8fafc; margin-bottom: 18px; border-radius: 8px; }
.product-image img { width: 100%; height: 100%; object-fit: cover; transition: 0.5s ease; }

.overlay-actions { position: absolute; bottom: 20px; left: 0; right: 0; display: flex; justify-content: center; }
.btn-quick-view {
  background: white; color: #0f172a; border: none; padding: 12px 24px; border-radius: 4px;
  font-weight: 700; font-size: 14px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); 
  opacity: 0; transform: translateY(20px); transition: 0.3s; cursor: pointer;
}
.btn-quick-view:hover { background: #0f172a; color: white; }

.sale-tag { position: absolute; top: 10px; left: 10px; background: #ef4444; color: white; padding: 5px 10px; font-size: 13px; font-weight: 700; border-radius: 4px; }
.sold-out-overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; z-index: 5; }
.sold-out-text { color: #fff; background: #dc2626; font-size: 14px; font-weight: 700; padding: 6px 18px; border-radius: 4px; letter-spacing: 0.5px; text-transform: uppercase; }

.product-info { text-align: center; }
.brand-name { font-size: 13px; text-transform: uppercase; color: #94a3b8; margin-bottom: 6px; font-weight: 600; }
.product-name { font-size: 17px; font-weight: 600; margin: 0 0 10px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #334155; }
.product-name:hover { color: #1e3a8a; }
.product-price { font-weight: 700; color: #0f172a; font-size: 17px; }
.old-price { text-decoration: line-through; color: #94a3b8; font-weight: 400; margin-left: 10px; font-size: 14px; }

.preview-colors { display: flex; justify-content: center; gap: 6px; margin-top: 12px; }
.preview-colors span { width: 12px; height: 12px; border-radius: 50%; border: 1px solid #cbd5e1; }

.loading-grid { min-height: 400px; display: flex; justify-content: center; align-items: center; }
.spinner { border: 4px solid #f1f5f9; border-top: 4px solid #1e3a8a; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

.empty-state { text-align: center; padding: 50px; color: #64748b; width: 100%; grid-column: span 3; font-size: 16px; }
.empty-state i { font-size: 45px; margin-bottom: 15px; color: #cbd5e1; }

.pagination { display: flex; justify-content: center; gap: 10px; margin-top: 60px; }
.pagination button { width: 40px; height: 40px; border: 1px solid #cbd5e1; background: white; cursor: pointer; transition: 0.2s; border-radius: 6px; color: #64748b; font-size: 16px; }
.pagination button:hover:not(:disabled) { background: #eff6ff; border-color: #1e3a8a; color: #1e3a8a; }
.pagination button:disabled { opacity: 0.5; cursor: not-allowed; }
.pagination span { display: flex; align-items: center; font-size: 16px; font-weight: 600; color: #334155; }

@media (max-width: 992px) {
  .page-content { grid-template-columns: 1fr; }
  .sidebar-filter { display: none; }
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); gap: 15px; }
}
</style>