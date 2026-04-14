<template>
  <div class="page-container">
    <div class="header-section">
      <h1 class="page-title">Quản lý sản phẩm / Toàn bộ biến thể</h1>
      <div class="total-count"><h5>Tổng biến thể tìm thấy: <b>{{ totalElements }}</b></h5></div>
    </div>

    <!-- BỘ LỌC -->
    <div class="card control-panel">
      <div class="filter-header">
        <i class="fas fa-filter"></i> Bộ lọc tìm kiếm
      </div>
      <div class="filter-body">
        <!-- Hàng 1: Keyword + Trạng thái -->
        <div class="row-top">
          <div class="input-group search-group">
            <label>Tìm kiếm</label>
            <div class="search-box">
              <i class="fas fa-search search-icon"></i>
              <input type="text" class="form-control" v-model="filter.keyword"
                placeholder="Tìm theo mã SKU / tên sản phẩm..." @keyup.enter="doSearch" />
            </div>
          </div>
          <div class="status-group">
            <label>Trạng thái</label>
            <div class="radio-list">
              <label class="radio-item">
                <input type="radio" :value="null" v-model="filter.trangThai" @change="doSearch"><span>Tất cả</span>
              </label>
              <label class="radio-item">
                <input type="radio" :value="1" v-model="filter.trangThai" @change="doSearch"><span>Kinh doanh</span>
              </label>
              <label class="radio-item">
                <input type="radio" :value="0" v-model="filter.trangThai" @change="doSearch"><span>Ngừng KD</span>
              </label>
            </div>
          </div>
        </div>

        <!-- Hàng 2: Các combobox lọc theo thuộc tính -->
        <div class="filter-grid">
          <div class="filter-item">
            <label>Màu sắc</label>
            <select v-model="filter.idMauSac" @change="doSearch" class="form-select">
              <option :value="null">Tất cả</option>
              <option v-for="item in options.mauSac" :key="item.id" :value="item.id">{{ item.tenMauSac }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>Kích thước</label>
            <select v-model="filter.idKichThuoc" @change="doSearch" class="form-select">
              <option :value="null">Tất cả</option>
              <option v-for="item in options.kichThuoc" :key="item.id" :value="item.id">{{ item.tenKichThuoc }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>Thương hiệu</label>
            <select v-model="filter.idThuongHieu" @change="doSearch" class="form-select">
              <option :value="null">Tất cả</option>
              <option v-for="item in options.thuongHieu" :key="item.id" :value="item.id">{{ item.tenThuongHieu }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>Chất liệu</label>
            <select v-model="filter.idChatLieu" @change="doSearch" class="form-select">
              <option :value="null">Tất cả</option>
              <option v-for="item in options.chatLieu" :key="item.id" :value="item.id">{{ item.tenChatLieu }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>Cổ áo</label>
            <select v-model="filter.idCoAo" @change="doSearch" class="form-select">
              <option :value="null">Tất cả</option>
              <option v-for="item in options.coAo" :key="item.id" :value="item.id">{{ item.tenCoAo }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>Tay áo</label>
            <select v-model="filter.idTayAo" @change="doSearch" class="form-select">
              <option :value="null">Tất cả</option>
              <option v-for="item in options.tayAo" :key="item.id" :value="item.id">{{ item.tenTayAo }}</option>
            </select>
          </div>
          <div class="filter-item">
            <label>Xuất xứ</label>
            <select v-model="filter.idXuatXu" @change="doSearch" class="form-select">
              <option :value="null">Tất cả</option>
              <option v-for="item in options.xuatXu" :key="item.id" :value="item.id">{{ item.tenXuatXu }}</option>
            </select>
          </div>
        </div>

        <!-- Hàng 3: Tickbox "Chỉ còn hàng" + Buttons -->
        <div class="row-bottom">
          <label class="checkbox-item">
            <input type="checkbox" v-model="filter.onlyInStock" @change="doSearch">
            <span>Chỉ hiển thị còn hàng (tồn kho > 0)</span>
          </label>
          <div class="action-buttons">
            <button class="btn btn-outline" @click="$router.push('/admin/products')">
              <i class="fas fa-arrow-left"></i> Quay lại
            </button>
            <button class="btn btn-dark" @click="resetFilter">
              <i class="fas fa-sync-alt"></i> Đặt lại bộ lọc
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- BẢNG KẾT QUẢ -->
    <div class="card table-container">
      <table class="custom-table">
        <thead>
          <tr>
            <th width="4%">STT</th>
            <th width="6%">Ảnh</th>
            <th width="10%">Mã SKU</th>
            <th width="14%">Tên sản phẩm</th>
            <th width="8%">Màu sắc</th>
            <th width="8%">Kích thước</th>
            <th width="9%">Thương hiệu</th>
            <th width="8%">Chất liệu</th>
            <th width="10%">Giá bán</th>
            <th width="7%">Tồn kho</th>
            <th width="8%">Trạng thái</th>
            <th width="8%">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="12" class="text-center py-5">Đang tải dữ liệu...</td>
          </tr>
          <tr v-else-if="items.length === 0">
            <td colspan="12" class="text-center py-5 empty-state">Không tìm thấy biến thể nào.</td>
          </tr>
          <tr v-else v-for="(item, index) in items" :key="item.id">
            <td class="text-center">{{ (page - 1) * pageSize + index + 1 }}</td>
            <td class="text-center">
              <img v-if="getImage(item)" :src="getImage(item)" class="variant-img" />
              <span v-else class="no-img"><i class="far fa-image"></i></span>
            </td>
            <td class="font-bold">{{ item.maCtsp }}</td>
            <td class="text-primary font-bold">{{ item.sanPham?.tenSanPham || '-' }}</td>
            <td>
              <span v-if="item.mauSac" class="color-cell">
                <span class="color-dot" :style="{ background: item.mauSac.maMau || '#ccc' }"></span>
                {{ item.mauSac.tenMauSac }}
              </span>
              <span v-else>-</span>
            </td>
            <td>{{ item.kichThuoc?.tenKichThuoc || '-' }}</td>
            <td>{{ item.thuongHieu?.tenThuongHieu || '-' }}</td>
            <td>{{ item.chatLieu?.tenChatLieu || '-' }}</td>
            <td class="font-bold">{{ formatCurrency(item.giaBan) }}</td>
            <td class="text-center font-bold" :class="{ 'text-danger': item.soLuong === 0 }">{{ item.soLuong }}</td>
            <td class="text-center">
              <span class="badge" :class="item.trangThai === 1 ? 'badge-success' : 'badge-danger'">
                {{ item.trangThai === 1 ? 'Kinh doanh' : 'Ngừng KD' }}
              </span>
            </td>
            <td class="text-center">
              <button class="btn-icon-eye" @click="goToProductDetail(item)" title="Xem sản phẩm cha">
                <i class="far fa-eye"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- PHÂN TRANG -->
      <div class="pagination-footer">
        <div class="page-info">
          Hiển thị
          <select v-model="pageSize" @change="handlePageSizeChange">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          biến thể / trang
        </div>
        <div class="page-controls">
          <button class="page-btn" :disabled="page === 1" @click="changePage(page - 1)">‹</button>
          <button v-for="p in visiblePages" :key="p" :class="['page-btn', { active: p === page }]" @click="changePage(p)">{{ p }}</button>
          <button class="page-btn" :disabled="page === totalPages" @click="changePage(page + 1)">›</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const API_URL = 'http://localhost:8080/api/v1'
const router = useRouter()

const items = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const totalPages = ref(1)
const totalElements = ref(0)

const filter = reactive({
  keyword: '',
  trangThai: null,
  idMauSac: null,
  idKichThuoc: null,
  idThuongHieu: null,
  idChatLieu: null,
  idCoAo: null,
  idTayAo: null,
  idXuatXu: null,
  onlyInStock: false,
})

const options = reactive({
  mauSac: [], kichThuoc: [], thuongHieu: [], chatLieu: [],
  coAo: [], tayAo: [], xuatXu: [],
})

const fetchOptions = async () => {
  try {
    const [ms, kt, th, cl, ca, ta, xx] = await Promise.all([
      axios.get(`${API_URL}/mau-sac?size=100&status=1`),
      axios.get(`${API_URL}/kich-thuoc?size=100&status=1`),
      axios.get(`${API_URL}/thuong-hieu?size=100&status=1`),
      axios.get(`${API_URL}/chat-lieu?size=100&status=1`),
      axios.get(`${API_URL}/co-ao?size=100&status=1`),
      axios.get(`${API_URL}/tay-ao?size=100&status=1`),
      axios.get(`${API_URL}/xuat-xu?size=100&status=1`),
    ])
    options.mauSac = ms.data.content || ms.data || []
    options.kichThuoc = kt.data.content || kt.data || []
    options.thuongHieu = th.data.content || th.data || []
    options.chatLieu = cl.data.content || cl.data || []
    options.coAo = ca.data.content || ca.data || []
    options.tayAo = ta.data.content || ta.data || []
    options.xuatXu = xx.data.content || xx.data || []
  } catch (e) {
    console.error('Lỗi tải bộ lọc:', e)
  }
}

const fetchVariants = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value - 1,
      size: pageSize.value,
      keyword: filter.keyword || undefined,
      idMauSac: filter.idMauSac || undefined,
      idKichThuoc: filter.idKichThuoc || undefined,
      idThuongHieu: filter.idThuongHieu || undefined,
      idChatLieu: filter.idChatLieu || undefined,
      idCoAo: filter.idCoAo || undefined,
      idTayAo: filter.idTayAo || undefined,
      idXuatXu: filter.idXuatXu || undefined,
      trangThai: filter.trangThai !== null ? filter.trangThai : undefined,
    }
    const res = await axios.get(`${API_URL}/chi-tiet-san-pham`, { params })
    let data = res.data.content || []
    if (filter.onlyInStock) {
      data = data.filter(v => v.soLuong > 0)
    }
    items.value = data
    totalElements.value = res.data.totalElements || 0
    totalPages.value = res.data.totalPages || 1
  } catch (e) {
    console.error('Lỗi tải biến thể:', e)
    items.value = []
  } finally {
    loading.value = false
  }
}

const doSearch = () => {
  page.value = 1
  fetchVariants()
}

const resetFilter = () => {
  filter.keyword = ''
  filter.trangThai = null
  filter.idMauSac = null
  filter.idKichThuoc = null
  filter.idThuongHieu = null
  filter.idChatLieu = null
  filter.idCoAo = null
  filter.idTayAo = null
  filter.idXuatXu = null
  filter.onlyInStock = false
  page.value = 1
  fetchVariants()
}

const getImage = (item) => {
  if (item.hinhAnhs && item.hinhAnhs.length > 0) {
    const url = item.hinhAnhs[0].duongDan || item.hinhAnhs[0].url
    if (url) return url.startsWith('http') ? url : `${API_URL.replace('/api/v1', '')}${url}`
  }
  return null
}

const goToProductDetail = (item) => {
  if (item.sanPham?.id) {
    router.push(`/admin/products/${item.sanPham.id}`)
  }
}

const formatCurrency = (val) => {
  if (!val && val !== 0) return '-'
  return Number(val).toLocaleString('vi-VN') + ' đ'
}

const changePage = (p) => {
  if (p >= 1 && p <= totalPages.value) { page.value = p; fetchVariants() }
}
const handlePageSizeChange = () => { page.value = 1; fetchVariants() }
const visiblePages = computed(() => {
  const p = []
  for (let i = 1; i <= totalPages.value; i++) {
    if (i === 1 || i === totalPages.value || (i >= page.value - 1 && i <= page.value + 1)) p.push(i)
  }
  return p
})

onMounted(() => {
  fetchOptions()
  fetchVariants()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  font-family: 'Segoe UI', sans-serif;
  background-color: #ebecee;
  min-height: 100vh;
  color: #333;
  font-size: 14px;
}
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 24px; font-weight: 700; color: #1e293b; }
.total-count { font-size: 13px; color: black; }

.card { background: white; border-radius: 12px; border: 1px solid #bfdbfe; box-shadow: 0 4px 12px rgba(0,0,0,0.05); padding: 24px; margin-bottom: 20px; }

.filter-header { font-size: 15px; font-weight: 700; color: #334155; margin-bottom: 15px; display: flex; align-items: center; gap: 8px; }
.filter-body { display: flex; flex-direction: column; gap: 18px; }

.row-top { display: flex; gap: 20px; align-items: flex-end; }
.input-group { display: flex; flex-direction: column; gap: 6px; }
.input-group label { font-size: 13px; font-weight: 600; color: #64748b; }
.search-group { flex: 2; }
.search-box { position: relative; width: 100%; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; }
.form-control { width: 100%; padding: 10px 10px 10px 36px; border: 1px solid #e2e8f0; border-radius: 8px; outline: none; transition: 0.2s; height: 42px; font-size: 14px; box-sizing: border-box; }
.form-control:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }

.filter-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; }
.filter-item { display: flex; flex-direction: column; gap: 5px; }
.filter-item label { font-size: 13px; font-weight: 600; color: #64748b; }
.form-select { width: 100%; padding: 0 10px; border: 1px solid #e2e8f0; border-radius: 8px; outline: none; height: 40px; font-size: 14px; cursor: pointer; background-color: #fff; box-sizing: border-box; }
.form-select:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }

.status-group { display: flex; flex-direction: column; gap: 6px; }
.status-group label { font-size: 13px; font-weight: 600; color: #64748b; }
.radio-list { display: flex; gap: 15px; align-items: center; }
.radio-item { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 14px; color: #334155; }
.radio-item input { width: 16px; height: 16px; accent-color: #ef4444; cursor: pointer; }

.row-bottom { display: flex; justify-content: space-between; align-items: center; padding-top: 5px; }
.checkbox-item { display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 14px; color: #334155; }
.checkbox-item input { width: 17px; height: 17px; accent-color: #3b82f6; cursor: pointer; }

.action-buttons { display: flex; gap: 10px; }
.btn { height: 40px; padding: 0 18px; border-radius: 8px; font-weight: 600; cursor: pointer; border: 1px solid transparent; font-size: 13px; display: inline-flex; align-items: center; gap: 8px; transition: 0.2s; }
.btn-outline { background: #fff; border-color: #e2e8f0; color: #334155; }
.btn-outline:hover { background: #f8fafc; border-color: #cbd5e1; }
.btn-dark { background: #334155; color: white; }
.btn-dark:hover { background: #1e293b; }

/* Table */
.table-container { padding: 0; overflow: hidden; }
.custom-table { width: 100%; border-collapse: collapse; }
.custom-table th {
  background: #f5f5f5;
  color: #000;
  padding: 14px;
  font-weight: 700;
  text-transform: uppercase;
  font-size: 12px;
  border-bottom: none;
  text-align: center;
}
.custom-table td { padding: 12px 10px; border-bottom: 1px solid #f1f5f9; font-size: 14px; vertical-align: middle; color: #334155; text-align: center; }

.font-bold { font-weight: 600; }
.text-primary { color: #3b82f6; }
.text-danger { color: #dc2626; }
.text-center { text-align: center; }
.py-5 { padding: 40px 0; }
.empty-state { color: #94a3b8; font-style: italic; }

.variant-img { width: 44px; height: 44px; object-fit: cover; border-radius: 6px; border: 1px solid #e2e8f0; }
.no-img { color: #cbd5e1; font-size: 22px; }

.color-cell { display: inline-flex; align-items: center; gap: 6px; }
.color-dot { width: 14px; height: 14px; border-radius: 50%; border: 1px solid #ccc; flex-shrink: 0; }

.badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; border: 1px solid transparent; }
.badge-success { background: #dbeafe; color: #201dd8; border-color: #93c5fd; }
.badge-danger { background: #fee2e2; color: #991b1b; border-color: #fecaca; }

.btn-icon-eye { width: 32px; height: 32px; border-radius: 6px; background: #fff; border: 1px solid #e2e8f0; color: #64748b; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: 0.2s; margin: 0 auto; }
.btn-icon-eye:hover { border-color: #3b82f6; color: #3b82f6; background: #eff6ff; }

.pagination-footer { display: flex; justify-content: space-between; align-items: center; padding: 15px 24px; border-top: 1px solid #f1f5f9; }
.page-info { font-size: 13px; color: #64748b; display: flex; align-items: center; gap: 8px; }
.page-info select { height: 32px; border: 1px solid #e2e8f0; border-radius: 6px; padding: 0 8px; font-size: 13px; }
.page-controls { display: flex; gap: 5px; }
.page-btn { width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 6px; cursor: pointer; font-size: 13px; display: flex; align-items: center; justify-content: center; }
.page-btn.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
</style>
