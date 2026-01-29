<template>
  <div class="product-detail-page">
    
    <div class="header-section">
      <div class="breadcrumb">
        <span class="text-bold text-lg">Quản lý sản phẩm</span> 
        <span class="divider">/</span> 
        <span class="text-gray text-lg">Chi tiết sản phẩm</span>
        <span class="divider">/</span> 
        <span class="text-primary text-lg font-bold">{{ productInfo.tenSanPham || 'Đang tải...' }}</span>
      </div>
      <div class="header-actions">
         <button class="btn btn-outline" @click="$router.push('/admin/products')">
            <font-awesome-icon :icon="['fas', 'arrow-left']" /> Quay lại danh sách
         </button>
      </div>
    </div>

    <div class="card filter-card">
      <div class="filter-top-row">
        <div class="search-box">
          <div class="input-wrapper">
            <font-awesome-icon :icon="['fas', 'magnifying-glass']" class="search-icon" />
            <input type="text" placeholder="Nhập mã SKU biến thể để tìm..." v-model="filter.keyword" />
          </div>
        </div>

        <div class="price-slider-box">
           <div class="price-labels">
              <span class="currency-label">0 đ</span>
              <span class="separator">-</span>
              <span class="currency-label font-bold">{{ formatCurrency(filter.maxPrice) }}</span>
           </div>
           <input 
              type="range" 
              v-model.number="filter.maxPrice" 
              :min="0" 
              :max="sliderMax" 
              class="range-slider"
           >
           
        </div>
      </div>

      <div class="filter-grid">
         <div class="filter-item">
            <label class="highlight-label">Mã SP Cha:</label>
            <input type="text" :value="productInfo.maSanPham" readonly class="simple-input readonly-text active-filter">
         </div>
         <div class="filter-item"><label class="highlight-label">Cổ áo:</label><select v-model="filter.coAo" class="simple-select active-filter"><option value="">Tất cả</option><option v-for="item in options.coAo" :key="item.id" :value="item.tenCoAo">{{ item.tenCoAo }}</option></select></div>
         <div class="filter-item"><label class="highlight-label">Tay áo:</label><select v-model="filter.tayAo" class="simple-select active-filter"><option value="">Tất cả</option><option v-for="item in options.tayAo" :key="item.id" :value="item.tenTayAo">{{ item.tenTayAo }}</option></select></div>
         <div class="filter-item"><label class="highlight-label">Xuất xứ:</label><select v-model="filter.xuatXu" class="simple-select active-filter"><option value="">Tất cả</option><option v-for="item in options.xuatXu" :key="item.id" :value="item.tenXuatXu">{{ item.tenXuatXu }}</option></select></div>
         <div class="filter-item"><label class="highlight-label">Kích thước:</label><select v-model="filter.idKichThuoc" class="simple-select active-filter"><option :value="null">Tất cả</option><option v-for="item in options.kichThuoc" :key="item.id" :value="item.id">{{ item.tenKichThuoc }}</option></select></div>
         <div class="filter-item"><label class="highlight-label">Thương hiệu:</label><select v-model="filter.thuongHieu" class="simple-select active-filter"><option value="">Tất cả</option><option v-for="item in options.thuongHieu" :key="item.id" :value="item.tenThuongHieu">{{ item.tenThuongHieu }}</option></select></div>
         <div class="filter-item"><label class="highlight-label">Màu sắc:</label><select v-model="filter.idMauSac" class="simple-select active-filter"><option :value="null">Tất cả</option><option v-for="item in options.mauSac" :key="item.id" :value="item.id">{{ item.tenMauSac }}</option></select></div>
         <div class="filter-item"><label class="highlight-label">Chất liệu:</label><select v-model="filter.chatLieu" class="simple-select active-filter"><option value="">Tất cả</option><option v-for="item in options.chatLieu" :key="item.id" :value="item.tenChatLieu">{{ item.tenChatLieu }}</option></select></div>
      </div>
    </div>

    <div class="card result-card">
      <div class="table-header-title">
         <h3>Danh sách biến thể ({{ filteredVariants.length }})</h3>
      </div>

      <div class="table-responsive">
        <table>
          <thead>
            <tr>
              <th width="40" class="text-center">
                  <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll">
              </th>
              <th width="60">Ảnh</th>
              <th>Mã SP</th>
              <th>Mã SKU</th>
              
              <th>Thương hiệu</th>
              <th>Màu sắc</th>
              <th>Kích thước</th>
              <th class="text-right" width="140">Giá bán</th>
              <th class="text-center" width="100">Tồn kho</th>
              <th class="text-center">Trạng thái</th>
              
              <th class="text-center" width="120">
                  <button 
                      v-if="selectedIds.length > 0" 
                      class="btn-save-mini" 
                      @click="handleBulkUpdate"
                      title="Lưu tất cả thay đổi"
                  >
                      <font-awesome-icon :icon="['fas', 'save']" /> Lưu ({{ selectedIds.length }})
                  </button>
                  <span v-else>Thao tác</span>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading"><td colspan="12" class="text-center py-5">Đang tải dữ liệu...</td></tr>
            <tr v-else-if="filteredVariants.length === 0"><td colspan="12" class="text-center py-5 text-gray">Không tìm thấy biến thể nào phù hợp.</td></tr>
            
            <tr 
               v-else 
               v-for="variant in paginatedVariants" 
               :key="variant.id"
               :class="{ 'active-row': selectedIds.includes(variant.id) }" 
            >
              <td class="text-center">
                  <input type="checkbox" :value="variant.id" v-model="selectedIds">
              </td>

              <td>
                <div class="img-thumb">
                    <img 
                        v-if="variant.listAnh && variant.listAnh.length > 0" 
                        :src="variant.listAnh[0]" 
                        alt="Img" 
                        class="custom-thumb-img"
                        @error="$event.target.style.display='none'" 
                    >
                    <font-awesome-icon v-else :icon="['far', 'image']" class="icon-placeholder" />
                </div>
              </td>

              <td class="text-gray-500">{{ productInfo.maSanPham }}</td>
              
              <td class="font-mono font-bold text-primary">{{ variant.maCtsp }}</td>
              <td>{{ productInfo.tenThuongHieu || '-' }}</td>
              <td><span class="tag-color">{{ variant.mauSac?.tenMauSac }}</span></td>
              <td><span class="tag-size">{{ variant.kichThuoc?.tenKichThuoc }}</span></td>
              
              <td class="text-right">
                  <div v-if="selectedIds.includes(variant.id)">
                      <input type="number" v-model="variant.giaBan" class="form-input-sm text-right">
                  </div>
                  <div v-else class="text-price">{{ formatCurrency(variant.giaBan) }}</div>
              </td>

              <td class="text-center">
                  <div v-if="selectedIds.includes(variant.id)">
                      <input type="number" v-model="variant.soLuong" class="form-input-sm text-center">
                  </div>
                  <div v-else class="font-bold">{{ variant.soLuong }}</div>
              </td>

              <td class="text-center">
                 <span :class="['status-badge', variant.trangThai === 1 ? 'status-active' : 'status-inactive']">
                    {{ variant.trangThai === 1 ? 'Đang bán' : 'Ngừng HĐ' }}
                 </span>
              </td>

              <td class="text-center">
                 <button class="btn-icon" title="Cập nhật chi tiết" @click="openEditModal(variant)">
                    <font-awesome-icon :icon="['far', 'pen-to-square']" />
                 </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-bar">
         <div class="page-size">
            Xem <select v-model="pageSize" class="size-select"><option :value="5">5</option><option :value="10">10</option><option :value="20">20</option></select> biến thể / trang
         </div>
         <div class="page-nav">
            <button class="nav-btn" :disabled="currentPage === 1" @click="currentPage--">‹</button>
            <span class="page-count">Trang {{ currentPage }} / {{ totalPages }}</span>
            <button class="nav-btn" :disabled="currentPage === totalPages" @click="currentPage++">›</button>
         </div>
      </div>
    </div>

    <ProductUpdateModal 
       :is-open="isModalOpen"
       :variant-data="selectedVariant"
       :parent-data="productInfo"
       :options="options"
       @close="isModalOpen = false"
       @save="handleSaveVariant"
    />

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import ProductUpdateModal from './ProductUpdateModal.vue';

const route = useRoute();
const productId = route.params.id;
const API_URL = 'http://localhost:8080/api/v1';

// STATE
const loading = ref(false);
const productInfo = ref({});
const allVariants = ref([]);
const currentPage = ref(1);
const pageSize = ref(5);
const selectedIds = ref([]); 

// Filter & Options
const options = reactive({ thuongHieu: [], chatLieu: [], xuatXu: [], coAo: [], tayAo: [], mauSac: [], kichThuoc: [] });
const filter = reactive({ keyword: '', minPrice: 0, maxPrice: 10000000, thuongHieu: '', chatLieu: '', xuatXu: '', coAo: '', tayAo: '', idMauSac: null, idKichThuoc: null });

// SỬA 3: BIẾN LƯU GIÁ MAX CỦA SP
const sliderMax = ref(10000000); 

// Modal State
const isModalOpen = ref(false);
const selectedVariant = ref({});

// API FETCHING
const fetchAllAttributes = async () => {
    // ... (Giữ nguyên logic fetch attribute)
    try {
        const [th, cl, xx, ca, ta, ms, kt] = await Promise.all([
            axios.get(`${API_URL}/thuong-hieu?size=100&status=1`),
            axios.get(`${API_URL}/chat-lieu?size=100&status=1`),
            axios.get(`${API_URL}/xuat-xu?size=100&status=1`),
            axios.get(`${API_URL}/co-ao?size=100&status=1`),
            axios.get(`${API_URL}/tay-ao?size=100&status=1`),
            axios.get(`${API_URL}/mau-sac?size=100&status=1`),
            axios.get(`${API_URL}/kich-thuoc?size=100&status=1`)
        ]);
        options.thuongHieu = th.data.content || th.data;
        options.chatLieu = cl.data.content || cl.data;
        options.xuatXu = xx.data.content || xx.data;
        options.coAo = ca.data.content || ca.data;
        options.tayAo = ta.data.content || ta.data;
        options.mauSac = ms.data.content || ms.data;
        options.kichThuoc = kt.data.content || kt.data;
    } catch (e) { console.error(e); }
};

const fetchData = async () => {
    loading.value = true;
    try {
        const pRes = await axios.get(`${API_URL}/products/${productId}`);
        productInfo.value = pRes.data || {};
        
        const vRes = await axios.get(`${API_URL}/products/${productId}/variants`);
        allVariants.value = Array.isArray(vRes.data) ? vRes.data : [];
        
        // SỬA 3: TÍNH TOÁN GIÁ MAX
        if (allVariants.value.length > 0) {
            // Tìm giá bán cao nhất trong các biến thể
            const maxPriceInList = Math.max(...allVariants.value.map(v => v.giaBan || 0));
            // Set Slider Max = Giá cao nhất (làm tròn lên chút cho đẹp nếu muốn)
            sliderMax.value = maxPriceInList > 0 ? maxPriceInList : 10000000;
            // Set Filter Max mặc định = Slider Max
            filter.maxPrice = sliderMax.value;
        }

        selectedIds.value = []; 
    } catch (e) { console.error(e); allVariants.value = []; } finally { loading.value = false; }
};

// FILTER LOGIC
const filteredVariants = computed(() => {
    if (!Array.isArray(allVariants.value)) return [];
    return allVariants.value.filter(v => {
        // Logic filter cũ
        const matchKey = filter.keyword ? v.maCtsp.toLowerCase().includes(filter.keyword.toLowerCase()) : true;
        const matchColor = filter.idMauSac ? v.mauSac?.id === filter.idMauSac : true;
        const matchSize = filter.idKichThuoc ? v.kichThuoc?.id === filter.idKichThuoc : true;
        const matchTH = filter.thuongHieu ? productInfo.value.tenThuongHieu === filter.thuongHieu : true;
        const matchCL = filter.chatLieu ? productInfo.value.tenChatLieu === filter.chatLieu : true;
        const matchXX = filter.xuatXu ? productInfo.value.tenXuatXu === filter.xuatXu : true;
        const matchCA = filter.coAo ? productInfo.value.tenCoAo === filter.coAo : true;
        const matchTA = filter.tayAo ? productInfo.value.tenTayAo === filter.tayAo : true;

        // Logic filter giá
        const price = v.giaBan || 0;
        const matchPrice = price >= filter.minPrice && price <= filter.maxPrice;

        return matchKey && matchColor && matchSize && matchPrice && 
               matchTH && matchCL && matchXX && matchCA && matchTA;
    });
});

const totalPages = computed(() => Math.ceil(filteredVariants.value.length / pageSize.value) || 1);
const paginatedVariants = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value;
    return filteredVariants.value.slice(start, start + pageSize.value);
});

// BULK UPDATE Logic
const isAllSelected = computed(() => {
    if (paginatedVariants.value.length === 0) return false;
    return paginatedVariants.value.every(v => selectedIds.value.includes(v.id));
});

const toggleSelectAll = () => {
    if (isAllSelected.value) {
        const idsToRemove = paginatedVariants.value.map(v => v.id);
        selectedIds.value = selectedIds.value.filter(id => !idsToRemove.includes(id));
    } else {
        const idsToAdd = paginatedVariants.value.map(v => v.id);
        selectedIds.value = [...new Set([...selectedIds.value, ...idsToAdd])];
    }
};

const handleBulkUpdate = async () => {
    if (!confirm(`Bạn có chắc chắn muốn cập nhật ${selectedIds.value.length} dòng đã chọn?`)) return;
    try {
        const variantsToUpdate = allVariants.value
            .filter(v => selectedIds.value.includes(v.id))
            .map(v => ({
                id: v.id,
                giaBan: v.giaBan ? Number(v.giaBan) : 0, 
                soLuong: v.soLuong ? Number(v.soLuong) : 0 
            }));

        await axios.put(`${API_URL}/products/variants/bulk-update`, { 
            variants: variantsToUpdate 
        });
        
        alert("Cập nhật nhanh thành công!");
        selectedIds.value = [];
        fetchData();
    } catch (e) {
        console.error(e);
        alert("Lỗi cập nhật nhanh! Vui lòng kiểm tra lại dữ liệu.");
    }
};

// HELPERS
const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);
const openEditModal = (variant) => { selectedVariant.value = variant; isModalOpen.value = true; };

const handleSaveVariant = async (formData) => {
    try {
        await axios.put(`${API_URL}/products/variants/${formData.id}`, {
            ...formData,
            listAnh: formData.listAnh 
        });
        alert("Cập nhật thành công!");
        isModalOpen.value = false;
        fetchData();
    } catch (e) { console.error(e); alert("Có lỗi xảy ra khi cập nhật!"); }
};

onMounted(() => { fetchAllAttributes(); fetchData(); });
</script>

<style scoped>
/* GLOBAL */
.product-detail-page { font-family: 'Segoe UI', sans-serif; color: #334155; padding-bottom: 40px; }
.header-section { margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
.text-bold { font-weight: 700; color: #0f172a; } .text-gray { color: #64748b; }
.text-primary { color: #2563eb; } .text-lg { font-size: 18px; }
.divider { margin: 0 10px; color: #cbd5e1; }

/* CARD & FILTER */
.card { background: #fff; border: 1px solid #e2e8f0; border-radius: 4px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); margin-bottom: 20px; padding: 24px; }
.filter-top-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; gap: 40px; }
.search-box { flex: 1; max-width: 50%; }
.input-wrapper { position: relative; }
.input-wrapper input { width: 100%; padding: 10px 10px 10px 36px; border: 1px solid #94a3b8; border-radius: 4px; outline: none; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #64748b; }

/* SLIDER */
.price-slider-box { flex: 1; max-width: 40%; display: flex; flex-direction: column; gap: 5px; }
.price-labels { display: flex; justify-content: space-between; align-items: center; font-size: 13px; color: #0f172a; }
.range-slider { width: 100%; cursor: pointer; accent-color: #2563eb; }
.range-info { text-align: center; color: #64748b; font-size: 12px; }

/* FILTER GRID */
.filter-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px 30px; }
.filter-item { display: flex; align-items: center; gap: 8px; justify-content: flex-end; }
.filter-item label { font-weight: 700; font-size: 13px; color: #0f172a; white-space: nowrap; }
.simple-input, .simple-select { border: none; background: transparent; color: #334155; font-weight: 600; font-size: 13px; outline: none; text-align: left; width: 100%; cursor: pointer; }
.simple-select:focus { border-bottom: 1px solid #2563eb; }
.readonly-text { color: #64748b; cursor: default; border-bottom: 1px dashed #e2e8f0; }
.highlight-label { color: #2563eb; }
.active-filter { border: 1px solid #cbd5e1; border-radius: 4px; padding: 4px; color: #0f172a; }

/* TABLE */
.result-card { padding: 0; }
.table-header-title { padding: 20px; text-align: center; border-bottom: 1px solid #f1f5f9; }
.table-header-title h3 { margin: 0; font-size: 18px; font-weight: 700; color: #94a3b8; text-transform: uppercase; }
.table-responsive { width: 100%; overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th { background: #fff; padding: 15px 10px; font-weight: 700; font-size: 13px; color: #0f172a; border-bottom: 1px solid #e2e8f0; text-align: left; }
td { padding: 15px 10px; border-bottom: 1px solid #f1f5f9; font-size: 14px; vertical-align: middle; color: #334155; }
.text-center { text-align: center; } .text-right { text-align: right; }
.font-mono { font-family: monospace; } .font-bold { font-weight: 600; } .text-price { color: #ef4444; font-weight: 600; }
.text-gray-500 { color: #6b7280; font-size: 13px; }

/* STATUS BADGE */
.status-badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.status-active { background-color: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.status-inactive { background-color: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }

/* THUMBNAIL */
.img-thumb { width: 40px; height: 40px; background: #f1f5f9; border-radius: 4px; display: flex; align-items: center; justify-content: center; overflow: hidden; border: 1px solid #e2e8f0; }
.custom-thumb-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.icon-placeholder { color: #cbd5e1; font-size: 18px; }

.tag-color { background: #eff6ff; color: #1e40af; border: 1px solid #dbeafe; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.tag-size { background: #f0fdf4; color: #166534; border: 1px solid #dcfce7; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 700; }
.btn-icon { background: transparent; border: none; font-size: 16px; color: #1e293b; cursor: pointer; }
.btn-outline { background: #fff; border: 1px solid #cbd5e1; padding: 8px 16px; border-radius: 4px; cursor: pointer; display: flex; align-items: center; gap: 8px; }
.pagination-bar { padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
.page-size { font-size: 13px; color: #94a3b8; }
.size-select { border: 1px solid #cbd5e1; border-radius: 4px; padding: 2px 5px; margin: 0 5px; outline: none; }
.page-nav { display: flex; gap: 10px; align-items: center; }
.nav-btn { width: 30px; height: 30px; border: 1px solid #cbd5e1; border-radius: 50%; background: #fff; color: #64748b; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.page-count { font-size: 13px; color: #64748b; }

/* INLINE EDIT */
.active-row { background-color: #e0f2fe !important; }
.active-row td { border-bottom: 1px solid #bae6fd !important; }
.form-input-sm { width: 100%; padding: 6px; border: 1px solid #2563eb; border-radius: 4px; outline: none; font-weight: 600; font-size: 13px; }
.btn-save-mini { background: #2563eb; color: white; border: none; padding: 5px 10px; border-radius: 4px; cursor: pointer; font-size: 12px; font-weight: 700; box-shadow: 0 2px 4px rgba(37, 99, 235, 0.3); transition: all 0.2s; white-space: nowrap; animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275); }
.btn-save-mini:hover { background: #1d4ed8; transform: translateY(-1px); box-shadow: 0 4px 6px rgba(37, 99, 235, 0.4); }
@keyframes popIn { from { opacity: 0; transform: scale(0.8); } to { opacity: 1; transform: scale(1); } }
</style>