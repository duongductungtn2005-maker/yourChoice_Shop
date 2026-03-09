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
        <!-- Tìm kiếm -->
        <div class="filter-block">
          <h3>Tìm kiếm</h3>
          <input type="text" v-model="filter.keyword" placeholder="Tên sản phẩm..." class="search-filter-input" />
        </div>

        <!-- Khoảng giá -->
        <div class="filter-block">
          <h3>Khoảng giá</h3>
          <div class="price-inputs">
            <input type="number" v-model.number="filter.minPrice" placeholder="Từ" min="0" class="price-input" />
            <span class="price-sep">—</span>
            <input type="number" v-model.number="filter.maxPrice" placeholder="Đến" min="0" class="price-input" />
          </div>
        </div>

        <!-- Thương hiệu -->
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

        <!-- Chất liệu -->
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

        <!-- Màu sắc -->
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

        <!-- Kích thước -->
        <div class="filter-block">
          <h3>Kích thước</h3>
          <div class="size-options">
            <div 
              v-for="kt in attributes.kichThuoc" 
              :key="kt.id"
              class="size-tag"
              :class="{ active: filter.idKichThuoc === kt.id }"
              @click="toggleSize(kt.id)"
            >
              {{ kt.tenKichThuoc }}
            </div>
          </div>
        </div>

        <button class="btn-filter-apply" @click="applyFilter">Lọc sản phẩm</button>
        <button class="btn-filter-reset" @click="resetFilter">Xóa bộ lọc</button>
      </aside>

      <div class="product-area">
        <div class="product-top-bar">
          <span class="result-count">Hiển thị <strong>{{ products.length }}</strong> / {{ totalElements }} sản phẩm</span>
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
                 <img :src="getProductImage(prod)" :alt="prod.tenSanPham" @error="handleImageError">
                 <div class="overlay-actions">
                    <button class="btn-quick-view" @click.stop="$router.push(`/product/${prod.id}`)">Xem chi tiết</button>
                 </div>
                 <div v-if="prod.phanTramGiam > 0" class="sale-tag">-{{ prod.phanTramGiam }}%</div>
              </div>
              
              <div class="product-info">
                 <div class="brand-name">{{ prod.tenThuongHieu || 'YourChoice' }}</div>
                 <h3 class="product-name">{{ prod.tenSanPham }}</h3>
                 <div class="product-price">
                    <span class="current-price">{{ formatMoney(getDisplayPrice(prod)) }}</span>
                    <span v-if="prod.giaGoc && getDisplayPrice(prod) < prod.giaGoc" class="old-price">{{ formatMoney(prod.giaGoc) }}</span>
                 </div>
                 
                 <div class="preview-colors" v-if="getPreviewColors(prod).length > 0">
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
           <button :disabled="page === 0" @click="changePage(0)" title="Trang đầu">«</button>
           <button :disabled="page === 0" @click="changePage(page - 1)" title="Trang trước">‹</button>
           <button 
             v-for="p in visiblePages" :key="p"
             :class="{ active: p === page }"
             @click="changePage(p)"
           >{{ p + 1 }}</button>
           <button :disabled="page === totalPages - 1" @click="changePage(page + 1)" title="Trang sau">›</button>
           <button :disabled="page === totalPages - 1" @click="changePage(totalPages - 1)" title="Trang cuối">»</button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { getProducts, getThuongHieu, getChatLieu, getMauSac, getKichThuoc } from '@/api/clientApi'

const loading = ref(false)
const products = ref([])
const attributes = reactive({ thuongHieu: [], chatLieu: [], mauSac: [], kichThuoc: [] })
const page = ref(0)
const totalPages = ref(1)
const totalElements = ref(0)
const sortBy = ref('newest')

const filter = reactive({
  keyword: '',
  minPrice: null,
  maxPrice: null,
  idThuongHieu: [],
  idChatLieu: [],
  idMauSac: null,
  idKichThuoc: null,
})

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(0, page.value - 2)
  const end = Math.min(totalPages.value - 1, page.value + 2)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

const fetchAttributes = async () => {
  try {
    const [th, cl, ms, kt] = await Promise.all([
      getThuongHieu({ status: 1, size: 100 }),
      getChatLieu({ status: 1, size: 100 }),
      getMauSac({ status: 1, size: 100 }),
      getKichThuoc({ status: 1, size: 100 }),
    ])
    attributes.thuongHieu = th.data.content || []
    attributes.chatLieu = cl.data.content || []
    attributes.mauSac = ms.data.content || []
    attributes.kichThuoc = kt.data.content || []
  } catch(e) { console.error('Lỗi tải thuộc tính:', e) }
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: 12,
      keyword: filter.keyword || undefined,
      status: 1,
      idThuongHieu: filter.idThuongHieu.length > 0 ? filter.idThuongHieu[0] : undefined,
      idChatLieu: filter.idChatLieu.length > 0 ? filter.idChatLieu[0] : undefined,
      idMauSac: filter.idMauSac || undefined,
      idKichThuoc: filter.idKichThuoc || undefined,
      minPrice: filter.minPrice || undefined,
      maxPrice: filter.maxPrice || undefined,
      sort: sortBy.value,
    }
    const res = await getProducts(params)
    products.value = res.data.content || []
    totalPages.value = res.data.totalPages || 1
    totalElements.value = res.data.totalElements || 0
  } catch (e) {
    console.error('Lỗi tải sản phẩm:', e)
    products.value = []
  } finally {
    loading.value = false
  }
}

const formatMoney = (val) => {
  if (!val) return '0 ₫'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

const getColorCode = (name) => {
  const map = { 'Đỏ': '#ef4444', 'Xanh': '#3b82f6', 'Vàng': '#eab308', 'Đen': '#000', 'Trắng': '#eee', 'Tím': '#a855f7', 'Xám': '#6b7280', 'Cam': '#f97316', 'Hồng': '#ec4899', 'Nâu': '#78350f', 'Be': '#d4a574', 'Xanh lá': '#22c55e' }
  for (let k in map) if (name && name.includes(k)) return map[k]
  return '#ccc'
}

const toggleColor = (id) => { filter.idMauSac = filter.idMauSac === id ? null : id }
const toggleSize = (id) => { filter.idKichThuoc = filter.idKichThuoc === id ? null : id }

const applyFilter = () => { page.value = 0; fetchProducts() }
const resetFilter = () => {
  filter.keyword = ''; filter.minPrice = null; filter.maxPrice = null
  filter.idThuongHieu = []; filter.idChatLieu = []; filter.idMauSac = null; filter.idKichThuoc = null
  page.value = 0; fetchProducts()
}

let debounceTimer = null
watch(() => filter.keyword, () => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => { page.value = 0; fetchProducts() }, 500)
})

const getDisplayPrice = (prod) => {
  const giaBanMin = Number(prod.giaBanMin || 0)
  const giaBanMax = Number(prod.giaBanMax || 0)
  return giaBanMin > 0 ? giaBanMin : giaBanMax > 0 ? giaBanMax : 0
}

const getProductImage = (prod) => {
  if (prod.anhChinh) {
    if (prod.anhChinh.startsWith('http')) return prod.anhChinh
    return `http://localhost:8080/images/${prod.anhChinh}`
  }
  if (prod.listAnh && prod.listAnh.length > 0) {
    const img = prod.listAnh[0]
    if (img.startsWith('http')) return img
    return `http://localhost:8080/images/${img}`
  }
  return 'https://placehold.co/300x400?text=No+Image'
}

const handleImageError = (e) => { e.target.src = 'https://placehold.co/300x400?text=No+Image' }

const getPreviewColors = (prod) => {
  if (prod.dsMauSac) return prod.dsMauSac.split(', ').filter(Boolean)
  if (prod.mauSac) return prod.mauSac.split(', ').filter(Boolean)
  return []
}

const changePage = (p) => {
  if (p >= 0 && p < totalPages.value) {
    page.value = p
    fetchProducts()
    document.getElementById('product-section')?.scrollIntoView({ behavior: 'smooth' })
  }
}

onMounted(() => {
  fetchAttributes()
  fetchProducts()
})
</script>

<style scoped>
.product-view { font-family: Arial, sans-serif; font-size: 16px; color: #333; }
.container { max-width: 1280px; margin: 0 auto; padding: 0 20px; width: 100%; box-sizing: border-box; }

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

.page-content { display: grid; grid-template-columns: 280px 1fr; gap: 40px; margin-bottom: 60px; }

.sidebar-filter { padding-right: 25px; border-right: 1px solid #f1f5f9; }
.filter-block { margin-bottom: 28px; }
.filter-block h3 { font-size: 15px; margin-bottom: 12px; font-weight: 700; text-transform: uppercase; color: #0f172a; }

.search-filter-input { width: 100%; padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 14px; outline: none; box-sizing: border-box; }
.search-filter-input:focus { border-color: #1e3a8a; }

.price-inputs { display: flex; align-items: center; gap: 8px; }
.price-input { flex: 1; padding: 8px 10px; border: 1px solid #cbd5e1; border-radius: 6px; font-size: 13px; outline: none; min-width: 0; }
.price-input:focus { border-color: #1e3a8a; }
.price-sep { color: #94a3b8; font-weight: 600; }

.checkbox-list { display: flex; flex-direction: column; gap: 10px; max-height: 200px; overflow-y: auto; }
.custom-checkbox { display: flex; align-items: center; gap: 10px; cursor: pointer; font-size: 14px; color: #475569; transition: 0.2s; }
.custom-checkbox:hover { color: #1e3a8a; }
.custom-checkbox input { display: none; }
.checkmark { width: 18px; height: 18px; border: 1px solid #cbd5e1; border-radius: 4px; position: relative; transition: 0.2s; flex-shrink: 0; }
.custom-checkbox input:checked + .checkmark { background: #1e3a8a; border-color: #1e3a8a; }
.custom-checkbox input:checked + .checkmark::after { content: '✓'; color: white; position: absolute; top: -1px; left: 3px; font-size: 13px; }

.color-options { display: flex; flex-wrap: wrap; gap: 10px; }
.color-circle { width: 28px; height: 28px; border-radius: 50%; cursor: pointer; border: 1px solid #e2e8f0; transition: 0.2s; }
.color-circle.active { border: 2px solid #1e3a8a; transform: scale(1.15); box-shadow: 0 0 0 2px rgba(30, 58, 138, 0.15); }

.size-options { display: flex; flex-wrap: wrap; gap: 8px; }
.size-tag { padding: 6px 14px; border: 1px solid #cbd5e1; border-radius: 4px; cursor: pointer; font-size: 13px; font-weight: 600; transition: 0.2s; color: #475569; }
.size-tag:hover { border-color: #1e3a8a; color: #1e3a8a; }
.size-tag.active { background: #1e3a8a; color: #fff; border-color: #1e3a8a; }

.btn-filter-apply { 
  width: 100%; padding: 11px; border: none; cursor: pointer; 
  font-weight: 700; font-size: 15px; margin-top: 12px; transition: 0.2s; border-radius: 6px;
  background: #0f172a; color: #fff; 
}
.btn-filter-apply:hover { background: #1e3a8a; }
.btn-filter-reset {
  width: 100%; padding: 10px; border: 1px solid #cbd5e1; cursor: pointer;
  font-weight: 600; font-size: 14px; margin-top: 8px; transition: 0.2s; border-radius: 6px;
  background: #fff; color: #64748b;
}
.btn-filter-reset:hover { border-color: #ef4444; color: #ef4444; }

.product-top-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }
.result-count { font-size: 15px; color: #64748b; }
.sort-select select { padding: 10px 15px; border: 1px solid #cbd5e1; outline: none; font-size: 14px; border-radius: 6px; color: #334155; }

.product-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 30px; }
.product-card { cursor: pointer; transition: 0.3s; }
.product-card:hover .product-image img { transform: scale(1.05); }
.product-card:hover .btn-quick-view { opacity: 1; transform: translateY(0); }

.product-image { position: relative; overflow: hidden; aspect-ratio: 3/4; background: #f8fafc; margin-bottom: 15px; border-radius: 8px; }
.product-image img { width: 100%; height: 100%; object-fit: cover; transition: 0.5s ease; }
.overlay-actions { position: absolute; bottom: 20px; left: 0; right: 0; display: flex; justify-content: center; }
.btn-quick-view {
  background: white; color: #0f172a; border: none; padding: 10px 20px; border-radius: 4px;
  font-weight: 700; font-size: 13px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); 
  opacity: 0; transform: translateY(20px); transition: 0.3s; cursor: pointer;
}
.btn-quick-view:hover { background: #0f172a; color: white; }
.sale-tag { position: absolute; top: 10px; left: 10px; background: #ef4444; color: white; padding: 5px 10px; font-size: 13px; font-weight: 700; border-radius: 4px; }

.product-info { text-align: center; }
.brand-name { font-size: 12px; text-transform: uppercase; color: #94a3b8; margin-bottom: 5px; font-weight: 600; }
.product-name { font-size: 16px; font-weight: 600; margin: 0 0 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #334155; }
.product-name:hover { color: #1e3a8a; }
.product-price { font-weight: 700; color: #0f172a; font-size: 16px; }
.old-price { text-decoration: line-through; color: #94a3b8; font-weight: 400; margin-left: 8px; font-size: 13px; }
.preview-colors { display: flex; justify-content: center; gap: 5px; margin-top: 10px; }
.preview-colors span { width: 12px; height: 12px; border-radius: 50%; border: 1px solid #cbd5e1; }

.loading-grid { min-height: 400px; display: flex; justify-content: center; align-items: center; }
.spinner { border: 4px solid #f1f5f9; border-top: 4px solid #1e3a8a; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

.empty-state { text-align: center; padding: 50px; color: #64748b; width: 100%; grid-column: span 3; font-size: 16px; }
.empty-state i { font-size: 45px; margin-bottom: 15px; color: #cbd5e1; }

.pagination { display: flex; justify-content: center; gap: 6px; margin-top: 50px; }
.pagination button { min-width: 38px; height: 38px; border: 1px solid #cbd5e1; background: white; cursor: pointer; transition: 0.2s; border-radius: 6px; color: #64748b; font-size: 15px; font-weight: 600; }
.pagination button:hover:not(:disabled) { background: #eff6ff; border-color: #1e3a8a; color: #1e3a8a; }
.pagination button.active { background: #1e3a8a; color: #fff; border-color: #1e3a8a; }
.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }

@media (max-width: 992px) {
  .page-content { grid-template-columns: 1fr; }
  .sidebar-filter { display: none; }
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 768px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); gap: 15px; }
}
</style>