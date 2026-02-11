<template>
  <div class="product-detail-page">
    
    <div class="header-section">
      <div class="breadcrumb">
        <span class="root-link" @click="$router.push('/admin/products')">Quản lý sản phẩm</span>
        <span class="divider">/</span>
        <span class="sub-link">Chi tiết sản phẩm</span>
        <span class="divider">/</span>
        <span class="current-item">{{ productInfo.tenSanPham || 'Đang tải...' }}</span>
      </div>
      
      <div class="header-actions">
         <button class="btn btn-outline" @click="$router.push('/admin/products')">
            <font-awesome-icon :icon="['fas', 'arrow-left']" /> Quay lại danh sách
         </button>
      </div>
    </div>

    <div class="card filter-card">
      <div class="filter-row-top">
        <div class="search-wrapper">
            <i class="fas fa-search search-icon"></i>
            <input 
                type="text" 
                class="form-control pl-35" 
                placeholder="Nhập mã SKU biến thể để tìm..." 
                v-model="filter.keyword" 
            />
        </div>

        <div class="price-wrapper">
            <div class="price-info">
                <span>0 đ</span>
                <span>-</span>
                <span class="fw-bold">{{ formatCurrency(filter.maxPrice) }}</span>
            </div>
            <input 
              type="range" 
              v-model.number="filter.maxPrice" 
              :min="0" 
              :max="sliderMax" 
              class="range-slider"
           >
        </div>

        <div class="actions-wrapper">
            <button class="btn btn-orange" @click="openQrScanner">
                <i class="fas fa-qrcode"></i> Quét QR
            </button>
            <button class="btn btn-dark" @click="resetFilter">
                <i class="fas fa-sync-alt"></i> Đặt lại
            </button>
            <button class="btn btn-gradient" @click="showAllVariants">
                <i class="fas fa-list"></i> Hiển thị tất cả
            </button>
        </div>
      </div>

      <div class="filter-grid">
         <div class="filter-item">
            <label>Mã SP Cha:</label>
            <input type="text" :value="productInfo.maSanPham" readonly class="form-control bg-gray">
         </div>
         <div class="filter-item"><label>Cổ áo:</label><select v-model="filter.coAo" class="form-select"><option value="">Tất cả</option><option v-for="item in options.coAo" :key="item.id" :value="item.tenCoAo">{{ item.tenCoAo }}</option></select></div>
         <div class="filter-item"><label>Tay áo:</label><select v-model="filter.tayAo" class="form-select"><option value="">Tất cả</option><option v-for="item in options.tayAo" :key="item.id" :value="item.tenTayAo">{{ item.tenTayAo }}</option></select></div>
         <div class="filter-item"><label>Xuất xứ:</label><select v-model="filter.xuatXu" class="form-select"><option value="">Tất cả</option><option v-for="item in options.xuatXu" :key="item.id" :value="item.tenXuatXu">{{ item.tenXuatXu }}</option></select></div>
         <div class="filter-item"><label>Kích thước:</label><select v-model="filter.idKichThuoc" class="form-select"><option :value="null">Tất cả</option><option v-for="item in options.kichThuoc" :key="item.id" :value="item.id">{{ item.tenKichThuoc }}</option></select></div>
         <div class="filter-item"><label>Thương hiệu:</label><select v-model="filter.thuongHieu" class="form-select"><option value="">Tất cả</option><option v-for="item in options.thuongHieu" :key="item.id" :value="item.tenThuongHieu">{{ item.tenThuongHieu }}</option></select></div>
         <div class="filter-item"><label>Màu sắc:</label><select v-model="filter.idMauSac" class="form-select"><option :value="null">Tất cả</option><option v-for="item in options.mauSac" :key="item.id" :value="item.id">{{ item.tenMauSac }}</option></select></div>
         <div class="filter-item"><label>Chất liệu:</label><select v-model="filter.chatLieu" class="form-select"><option value="">Tất cả</option><option v-for="item in options.chatLieu" :key="item.id" :value="item.tenChatLieu">{{ item.tenChatLieu }}</option></select></div>
      </div>
    </div>

    <div class="card result-card">
      <div class="table-header-title">
         <h3 class="text-black">DANH SÁCH BIẾN THỂ ({{ filteredVariants.length }})</h3>
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
              <th width="140">Giá bán</th>
              <th width="100">Tồn kho</th>
              <th>Trạng thái</th>
              <th width="120">
                  <button v-if="selectedIds.length > 0" class="btn-save-mini" @click="handleBulkUpdate" title="Lưu tất cả thay đổi">
                      <font-awesome-icon :icon="['fas', 'save']" /> Lưu ({{ selectedIds.length }})
                  </button>
                  <span v-else>Thao tác</span>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading"><td colspan="12" class="text-center py-5">Đang tải dữ liệu...</td></tr>
            <tr v-else-if="filteredVariants.length === 0"><td colspan="12" class="text-center py-5 text-gray">Không tìm thấy biến thể nào phù hợp.</td></tr>
            
            <tr v-else v-for="variant in paginatedVariants" :key="variant.id" :class="{ 'active-row': selectedIds.includes(variant.id) }">
              <td class="text-center"><input type="checkbox" :value="variant.id" v-model="selectedIds"></td>
              <td>
                <div class="img-thumb">
                    <img v-if="variant.listAnh && variant.listAnh.length > 0" :src="variant.listAnh[0]" class="custom-thumb-img" @error="$event.target.style.display='none'">
                    <font-awesome-icon v-else :icon="['far', 'image']" class="icon-placeholder" />
                </div>
              </td>
              <td class="text-gray-500">{{ productInfo.maSanPham }}</td>
              <td class="font-mono font-bold text-primary">{{ variant.maCtsp }}</td>
              <td>{{ productInfo.tenThuongHieu || '-' }}</td>
              <td><span class="tag-color">{{ variant.mauSac?.tenMauSac }}</span></td>
              <td><span class="tag-size">{{ variant.kichThuoc?.tenKichThuoc }}</span></td>
              
              <td>
                  <div v-if="selectedIds.includes(variant.id)"><input type="number" v-model="variant.giaBan" class="form-input-sm text-center"></div>
                  <div v-else class="text-price">{{ formatCurrency(variant.giaBan) }}</div>
              </td>
              <td>
                  <div v-if="selectedIds.includes(variant.id)"><input type="number" v-model="variant.soLuong" class="form-input-sm text-center"></div>
                  <div v-else>{{ variant.soLuong }}</div>
              </td>
              <td>
                 <span :class="['status-badge', variant.trangThai === 1 ? 'status-active' : 'status-inactive']">{{ variant.trangThai === 1 ? 'Đang bán' : 'Ngừng HĐ' }}</span>
              </td>
              <td>
                 <button class="btn-icon-eye" title="Xem chi tiết" @click="openEditModal(variant)"><font-awesome-icon :icon="['far', 'eye']" /></button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-bar">
         <div class="page-size">Xem <select v-model="pageSize" class="size-select"><option :value="5">5</option><option :value="10">10</option><option :value="20">20</option></select> biến thể / trang</div>
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

    <div v-if="showScanModal" class="modal-overlay" @click.self="closeScanModal">
      <div class="modal-content scan-modal">
        <div class="modal-header">
          <h3>Quét mã QR Biến thể</h3>
          <button @click="closeScanModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div id="qr-reader" style="width: 100%;"></div>
          <p class="scan-hint">Di chuyển camera vào mã QR/Mã vạch của biến thể</p>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import Swal from 'sweetalert2';
import { toastSuccess, toastError } from '@/utils/toast';
import ProductUpdateModal from './ProductUpdateModal.vue';
import { Html5QrcodeScanner } from "html5-qrcode"; // IMPORT THƯ VIỆN QR

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

// QR Code State
const showScanModal = ref(false);
let html5QrcodeScanner = null;

// Filter & Options
const options = reactive({ thuongHieu: [], chatLieu: [], xuatXu: [], coAo: [], tayAo: [], mauSac: [], kichThuoc: [] });
const filter = reactive({ keyword: '', minPrice: 0, maxPrice: 10000000, thuongHieu: '', chatLieu: '', xuatXu: '', coAo: '', tayAo: '', idMauSac: null, idKichThuoc: null });

// Slider
const sliderMax = ref(10000000); 

// Modal State
const isModalOpen = ref(false);
const selectedVariant = ref({});

// --- QR CODE LOGIC ---
const openQrScanner = () => {
    showScanModal.value = true;
    // Chờ DOM render xong mới khởi tạo camera
    nextTick(() => { startScanner(); });
}

const closeScanModal = () => {
    if (html5QrcodeScanner) {
        html5QrcodeScanner.clear().catch(error => console.error("Failed to clear html5QrcodeScanner. ", error));
    }
    showScanModal.value = false;
}

const startScanner = () => {
    // Cấu hình scanner
    html5QrcodeScanner = new Html5QrcodeScanner( 
        "qr-reader", 
        { fps: 10, qrbox: { width: 250, height: 250 } }, 
        false 
    );
    html5QrcodeScanner.render(onScanSuccess, (error) => {
        // Handle scan error (thường không cần làm gì để tránh spam log)
    });
}

const onScanSuccess = (decodedText, decodedResult) => {
    // 1. Dừng camera và đóng modal
    closeScanModal();
    
    // 2. Điền mã vừa quét vào ô tìm kiếm
    filter.keyword = decodedText;
    
    // 3. Thông báo
    toastSuccess(`Đã tìm thấy mã: ${decodedText}`);
    
    // 4. Reset phân trang về 1 để thấy kết quả
    currentPage.value = 1;
}
// ---------------------

// API FETCHING
const fetchAllAttributes = async () => {
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
        
        if (allVariants.value.length > 0) {
            const maxPriceInList = Math.max(...allVariants.value.map(v => v.giaBan || 0));
            sliderMax.value = maxPriceInList > 0 ? maxPriceInList : 10000000;
            filter.maxPrice = sliderMax.value;
        }

        selectedIds.value = []; 
    } catch (e) { console.error(e); allVariants.value = []; } finally { loading.value = false; }
};

// FILTER LOGIC
const filteredVariants = computed(() => {
    if (!Array.isArray(allVariants.value)) return [];
    return allVariants.value.filter(v => {
        // Tìm theo Mã SKU
        const matchKey = filter.keyword ? v.maCtsp.toLowerCase().includes(filter.keyword.toLowerCase()) : true;
        const matchColor = filter.idMauSac ? v.mauSac?.id === filter.idMauSac : true;
        const matchSize = filter.idKichThuoc ? v.kichThuoc?.id === filter.idKichThuoc : true;
        const matchTH = filter.thuongHieu ? productInfo.value.tenThuongHieu === filter.thuongHieu : true;
        const matchCL = filter.chatLieu ? productInfo.value.tenChatLieu === filter.chatLieu : true;
        const matchXX = filter.xuatXu ? productInfo.value.tenXuatXu === filter.xuatXu : true;
        const matchCA = filter.coAo ? productInfo.value.tenCoAo === filter.coAo : true;
        const matchTA = filter.tayAo ? productInfo.value.tenTayAo === filter.tayAo : true;

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
    const result = await Swal.fire({
        title: 'Xác nhận cập nhật?',
        text: `Bạn có chắc chắn muốn cập nhật ${selectedIds.value.length} dòng đã chọn?`,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy',
        confirmButtonColor: '#0f172a'
    });

    if (!result.isConfirmed) return;

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
        
        toastSuccess("Cập nhật nhanh thành công!");
        selectedIds.value = [];
        fetchData();
    } catch (e) {
        console.error(e);
        toastError("Lỗi cập nhật nhanh! Vui lòng kiểm tra lại dữ liệu.");
    }
};

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);
const openEditModal = (variant) => { selectedVariant.value = variant; isModalOpen.value = true; };

const handleSaveVariant = () => {
    isModalOpen.value = false;
    fetchData();
};

const resetFilter = () => {
    filter.keyword = '';
    filter.minPrice = 0;
    filter.maxPrice = sliderMax.value;
    filter.thuongHieu = '';
    filter.chatLieu = '';
    filter.xuatXu = '';
    filter.coAo = '';
    filter.tayAo = '';
    filter.idMauSac = null;
    filter.idKichThuoc = null;
}

const showAllVariants = () => {
    resetFilter();
    pageSize.value = 20; // Hiện nhiều hơn
}

onMounted(() => { fetchAllAttributes(); fetchData(); });
</script>

<style scoped>
/* GLOBAL & FONTS */
.product-detail-page { font-family: 'Segoe UI', sans-serif; color: #334155; padding-bottom: 40px; background-color: #f8fafc; min-height: 100vh; padding: 20px;}

/* HEADER SECTION (Breadcrumb) */
.header-section { margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center; }
.breadcrumb { font-size: 16px; display: flex; align-items: center; }
.root-link { font-weight: 700; color: #2b4360; cursor: pointer; }
.sub-link { color: #64748b; }
.current-item { color: #2563eb; font-weight: 700; }
.divider { margin: 0 10px; color: #cbd5e1; font-size: 14px; }

/* CARD STYLING */
.card { 
    background: #fff; 
    border: 1px solid #bfdbfe !important; 
    border-radius: 16px; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); 
    margin-bottom: 20px; 
    padding: 24px; 
}

/* === FILTER ROW 1 === */
.filter-row-top { display: flex; align-items: center; gap: 30px; margin-bottom: 25px; }
.search-wrapper { flex: 2; position: relative; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; }
.form-control.pl-35 { padding-left: 35px; }

.price-wrapper { flex: 1.5; display: flex; flex-direction: column; gap: 6px; }
.price-info { display: flex; justify-content: space-between; font-size: 13px; color: #334155; }
.range-slider { width: 100%; cursor: pointer; accent-color: #0f172a; }

.actions-wrapper { display: flex; gap: 10px; }

/* === FILTER ROW 2 === */
.filter-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px 30px; }
.filter-item { display: flex; align-items: center; gap: 10px; }
.filter-item label { font-weight: 600; font-size: 13px; color: #0f172a; white-space: nowrap; min-width: 70px; text-align: right; }
.form-control, .form-select { width: 100%; padding: 8px 12px; border: 1px solid #e2e8f0; border-radius: 6px; font-size: 13px; outline: none; background-color: #fff; height: 38px; color: #334155; }
.form-control:focus, .form-select:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.bg-gray { background-color: #f8fafc; color: #64748b; }

/* BUTTONS */
.btn { height: 38px; padding: 0 16px; border-radius: 6px; font-weight: 600; cursor: pointer; font-size: 13px; border: 1px solid transparent; transition: 0.2s; display: inline-flex; align-items: center; gap: 8px; }
.btn-outline { background: #fff; border: 1px solid #e2e8f0; color: #475569; }
.btn-outline:hover { background: #f8fafc; border-color: #cbd5e1; }
.btn-orange { background-color: #f97316; color: #fff; }
.btn-orange:hover { background-color: #ea580c; box-shadow: 0 2px 5px rgba(234, 88, 12, 0.3); }
.btn-navy { background-color: #0f172a; color: #fff; }
.btn-navy:hover { background-color: #1e293b; }
.btn-dark { background-color: #1e293b; color: #fff; }

.btn-gradient { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); }
.btn-gradient:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.3); }

/* TABLE */
.result-card { padding: 0; overflow: hidden; }
.table-header-title { padding: 20px; text-align: center; border-bottom: 1px solid #f1f5f9; }
.text-black { color: #0f172a !important; font-size: 18px; font-weight: 700; margin: 0; text-transform: uppercase; }

.table-responsive { width: 100%; overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th { background: #E9F1FB; padding: 15px 10px; font-weight: 700; font-size: 13px; color: #1E3A8A; border-bottom: none !important; text-align: center; }
td { padding: 15px 10px; border-bottom: 1px solid #f1f5f9; font-size: 14px; vertical-align: middle; color: #334155; text-align: center; }

.fw-bold { font-weight: 700; }
.font-mono { font-family: monospace; } 
.text-primary { color: #2563eb; } 
.text-price { color: #ef4444; font-weight: 600; }
.text-gray-500 { color: #6b7280; font-size: 13px; }

/* STATUS BADGE */
.status-badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.status-active { background-color: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.status-inactive { background-color: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }

/* THUMBNAIL */
.img-thumb { width: 40px; height: 40px; background: #f1f5f9; border-radius: 4px; display: flex; align-items: center; justify-content: center; overflow: hidden; border: 1px solid #e2e8f0; margin: 0 auto; }
.custom-thumb-img { width: 100%; height: 100%; object-fit: cover; display: block; }
.icon-placeholder { color: #cbd5e1; font-size: 18px; }

.tag-color { background: #eff6ff; color: #1e40af; border: 1px solid #dbeafe; padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.tag-size { background: #f0fdf4; color: #166534; border: 1px solid #dcfce7; padding: 2px 8px; border-radius: 4px; font-size: 12px; font-weight: 700; }
.btn-icon { background: transparent; border: none; font-size: 16px; color: #1e293b; cursor: pointer; }

/* BTN EYE STYLE */
.btn-icon-eye { width: 32px; height: 32px; border-radius: 6px; background: #fff; border: 1px solid #e2e8f0; color: #64748b; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: 0.2s; margin: 0 auto; }
.btn-icon-eye:hover { border-color: #3b82f6; color: #3b82f6; background: #eff6ff; }

/* PAGINATION */
.pagination-bar { padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; border-top: 1px solid #f1f5f9; }
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

/* QR Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content.scan-modal { background: white; width: 500px; padding: 20px; border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
.modal-header { display: flex; justify-content: space-between; margin-bottom: 15px; }
.modal-header h3 { margin: 0; font-size: 18px; color: #0f172a; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #94a3b8; }
.close-btn:hover { color: #ef4444; }
.scan-hint { text-align: center; margin-top: 10px; color: #64748b; font-size: 14px; }
</style>