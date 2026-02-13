<template>
  <div class="page-container">
    <div class="header-row">
       <div class="header-title">
          <h3>Thêm đợt giảm giá</h3>
       </div>
       <button type="button" @click="$router.go(-1)" class="btn btn-back">
          <i class="fas fa-arrow-left"></i> Quay lại
       </button>
    </div>

    <div class="split-layout">
      
      <div class="card left-panel">
        <h4 class="panel-title">Thông tin đợt giảm giá</h4>
        
        <div class="form-container">
            <div class="form-group">
                <label>Mã đợt (Tự động sinh)</label>
                <input value="Mã tự sinh (DGG-...)" class="form-control input-den" disabled style="background-color: #f8fafc; color: #64748b;" />
            </div>

            <div class="form-group">
                <label>Tên đợt giảm giá <span class="required">*</span></label>
                <input v-model="form.tenDotGiamGia" placeholder="Nhập tên đợt giảm giá..." class="form-control input-den" />
            </div>

            <div class="form-group">
                <label>Loại giảm giá</label>
                <div class="radio-group">
                    <label class="radio-item">
                        <input type="radio" value="%" v-model="form.loaiGiamGia" />
                        <span>% Phần trăm</span>
                    </label>
                </div>
            </div>

            <div class="form-group">
                <label>Giá trị giảm (%) <span class="required">*</span></label>
                <input v-model="form.giaTriGiam" type="number" class="form-control input-den" placeholder="Nhập % giảm..." />
            </div>

            <div class="form-group">
                <label>Ngày bắt đầu <span class="required">*</span></label>
                <input v-model="form.ngayBatDau" type="datetime-local" class="form-control input-den" />
            </div>

            <div class="form-group">
                <label>Ngày kết thúc <span class="required">*</span></label>
                <input v-model="form.ngayKetThuc" type="datetime-local" class="form-control input-den" />
            </div>
        </div>
        
        <div class="form-footer">
            <button type="button" @click="createSale" class="btn btn-create">
                <i class="fas fa-plus"></i> Tạo đợt giảm giá
            </button>
        </div>
      </div>

      <div class="card right-panel">
        <div class="panel-header">
            <h4>Chọn sản phẩm áp dụng</h4>
            <div class="selected-count">
                Đã chọn: <b>{{ selectedParentIds.length }}</b> sản phẩm
            </div>
        </div>

        <div class="filter-toolbar">
            <div class="search-box">
                <i class="fas fa-magnifying-glass search-icon"></i>
                <input 
                    class="input-den" 
                    v-model="filter.keyword" 
                    @keyup.enter="loadParentProducts" 
                    placeholder="Tìm tên hoặc mã sản phẩm..." 
                />
            </div>
            
            <div class="filter-group">
                 <select v-model="filter.mauSacId" @change="loadParentProducts" class="filter-select">
                    <option value="">-- Màu sắc --</option>
                    <option v-for="ms in options.mauSac" :key="ms.id" :value="ms.id">{{ ms.tenMauSac }}</option>
                </select>

                <select v-model="filter.kichThuocId" @change="loadParentProducts" class="filter-select">
                    <option value="">-- Kích thước --</option>
                    <option v-for="kt in options.kichThuoc" :key="kt.id" :value="kt.id">{{ kt.tenKichThuoc }}</option>
                </select>

                 <button type="button" class="filter-btn" @click="resetFilter" title="Làm mới bộ lọc">
                    <i class="fas fa-filter"></i>
                </button>
            </div>
        </div>

        <div class="table-responsive">
            <table class="custom-table">
                <thead>
                    <tr>
                        <th width="40px" class="text-center">
                            <input type="checkbox" @change="toggleAllParent" :checked="isAllParentSelected" />
                        </th>
                        <th width="60px" class="text-center">Ảnh</th>
                        <th width="100px" class="text-center">Mã SP</th>
                        <th>Tên sản phẩm</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="loadingProd">
                        <td colspan="4" class="text-center py-4">Đang tải dữ liệu...</td>
                    </tr>
                    <tr v-else-if="parentProducts.length === 0">
                         <td colspan="4" class="text-center empty-state">Không tìm thấy sản phẩm nào.</td>
                    </tr>
                    <tr v-else v-for="sp in parentProducts" :key="sp.id" :class="{ 'selected-row': selectedParentIds.includes(sp.id) }">
                        <td class="text-center">
                            <input type="checkbox" :value="sp.id" v-model="selectedParentIds" @change="handleSelectParent(sp.id)" />
                        </td>
                        <td class="text-center">
                            <div class="img-wrapper">
                                <img v-if="sp.hinhAnh" :src="'http://localhost:8080/api/v1/product-images/' + sp.hinhAnh" class="thumb-img" @error="handleImgError" />
                                <div v-else class="img-placeholder"><i class="far fa-image"></i></div>
                            </div>
                        </td>
                        <td class="text-center code-text">{{ sp.maSanPham }}</td>
                        <td>
                            <div class="prod-name" :title="sp.tenSanPham">{{ sp.tenSanPham }}</div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="pagination-centered">
            <button class="page-btn" :disabled="page === 0" @click="changePage(page - 1)">‹</button>
            <span class="page-counter">Trang {{ page + 1 }} / {{ totalPages }}</span>
            <button class="page-btn" :disabled="page >= totalPages - 1" @click="changePage(page + 1)">›</button>
        </div>
      </div>
    </div>

    <div class="card bottom-panel mt-3" v-if="selectedVariants.length > 0">
        <div class="panel-header">
            <h4>Danh sách chi tiết sản phẩm được chọn ({{ selectedVariants.length }})</h4>
            <div class="action-right">
                <button v-if="selectedDetailIds.length > 0" class="btn-sm btn-outline-danger mr-2" @click="removeSelectedDetails">
                    <i class="fas fa-trash-alt"></i> Xóa đã chọn ({{ selectedDetailIds.length }})
                </button>
            </div>
        </div>

        <div class="detail-filter-bar">
            <select v-model="detailFilter.thuongHieu" class="detail-select">
                <option value="">-- Thương hiệu --</option>
                <option v-for="th in options.thuongHieu" :key="th.id" :value="th.tenThuongHieu">{{ th.tenThuongHieu }}</option>
            </select>
            <select v-model="detailFilter.chatLieu" class="detail-select">
                <option value="">-- Chất liệu --</option>
                <option v-for="cl in options.chatLieu" :key="cl.id" :value="cl.tenChatLieu">{{ cl.tenChatLieu }}</option>
            </select>
            <select v-model="detailFilter.kichThuoc" class="detail-select">
                <option value="">-- Kích cỡ --</option>
                <option v-for="kt in options.kichThuoc" :key="kt.id" :value="kt.tenKichThuoc">{{ kt.tenKichThuoc }}</option>
            </select>
            <select v-model="detailFilter.mauSac" class="detail-select">
                <option value="">-- Màu sắc --</option>
                <option v-for="ms in options.mauSac" :key="ms.id" :value="ms.tenMauSac">{{ ms.tenMauSac }}</option>
            </select>
            <button class="btn-icon-filter" @click="resetDetailFilter" title="Đặt lại bộ lọc">
                <i class="fas fa-filter"></i>
            </button>
        </div>

        <div class="table-responsive">
             <table class="custom-table">
                <thead>
                    <tr>
                        <th width="40px" class="text-center">
                            <input type="checkbox" @change="toggleAllDetail" :checked="isAllDetailSelected" />
                        </th>
                        <th width="50px" class="text-center">STT</th>
                        <th width="60px" class="text-center">Ảnh</th>
                        <th class="text-center">Mã sản phẩm</th>
                        <th class="text-center">Tên sản phẩm</th>
                        <th class="text-center">Giá bán</th>
                        <th class="text-center">Thương hiệu</th>
                        <th class="text-center">Chất liệu</th>
                        <th class="text-center">Kích cỡ</th>
                        <th class="text-center">Màu sắc</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="paginatedDetails.length === 0">
                        <td colspan="10" class="text-center empty-state">Không tìm thấy kết quả lọc.</td>
                    </tr>
                    <tr v-for="(v, index) in paginatedDetails" :key="v.id">
                        <td class="text-center">
                            <input type="checkbox" :value="v.id" v-model="selectedDetailIds">
                        </td>
                        <td class="text-center">{{ (detailPage - 1) * detailPageSize + index + 1 }}</td>
                        <td class="text-center">
                            <div class="img-wrapper-sm">
                                <img v-if="v.hinhAnh" :src="'http://localhost:8080/api/v1/product-images/' + v.hinhAnh" class="thumb-img" @error="handleImgError" />
                                <div v-else class="img-placeholder"><i class="far fa-image"></i></div>
                            </div>
                        </td>
                        <td class="code-text text-center">{{ v.maCtsp }}</td>
                        <td class="text-center">{{ v.tenSanPham }}</td>
                        <td class="text-center font-bold">{{ formatCurrency(v.giaBan) }}</td>
                        
                        <td class="text-center">{{ v.tenThuongHieu }}</td>
                        <td class="text-center">{{ v.tenChatLieu }}</td>
                        
                        <td class="text-center">
                            <span class="badge-size">{{ v.tenKichThuoc }}</span>
                        </td>
                        <td class="text-center">
                             <div class="color-dot-wrapper justify-center">
                                <span class="color-dot" :style="{ backgroundColor: getColorCode(v.tenMauSac) }"></span> 
                                <span class="ml-1">{{ v.tenMauSac }}</span>
                            </div>
                        </td>
                    </tr>
                </tbody>
             </table>
        </div>

        <div class="pagination-centered">
            <button class="page-btn" :disabled="detailPage === 1" @click="changeDetailPage(detailPage - 1)">‹</button>
            <span class="page-counter">Trang {{ detailPage }} / {{ detailTotalPages }}</span>
            <button class="page-btn" :disabled="detailPage === detailTotalPages" @click="changeDetailPage(detailPage + 1)">›</button>
        </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue';
import request from '@/services/request';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';

const Toast = Swal.mixin({
    toast: true, position: 'top-end', showConfirmButton: false, timer: 3000, timerProgressBar: true
});
const router = useRouter();

// === FORM STATE ===
const form = reactive({
    tenDotGiamGia: '', giaTriGiam: 0, loaiGiamGia: '%', 
    ngayBatDau: '', ngayKetThuc: '', trangThai: 1, idChiTietSanPhams: [] 
});

// === DATA STATE ===
const parentProducts = ref([]);
const selectedParentIds = ref([]); 
const selectedVariants = ref([]); 
const selectedDetailIds = ref([]); 

// Loading & Pagination Parent
const loadingProd = ref(false);
const page = ref(0);
const pageSize = ref(10); 
const totalPages = ref(0);

// Filters
const filter = reactive({ keyword: '', mauSacId: '', kichThuocId: '' });
const options = reactive({ mauSac: [], kichThuoc: [], thuongHieu: [], chatLieu: [] });

// Detail List State
const detailFilter = reactive({ thuongHieu: '', chatLieu: '', kichThuoc: '', mauSac: '' });
const detailPage = ref(1);
const detailPageSize = 5;

// --- API LOAD OPTIONS ---
const fetchFilterOptions = async () => {
    try {
        const [ms, kt, th, cl] = await Promise.all([
            request.get('/mau-sac?size=100&status=1'),
            request.get('/kich-thuoc?size=100&status=1'),
            request.get('/thuong-hieu?size=100&status=1'),
            request.get('/chat-lieu?size=100&status=1')
        ]);
        options.mauSac = ms.data.content || ms.data || [];
        options.kichThuoc = kt.data.content || kt.data || [];
        options.thuongHieu = th.data.content || th.data || [];
        options.chatLieu = cl.data.content || cl.data || [];
    } catch (e) { console.error("Lỗi tải bộ lọc:", e); }
};

// --- API LOAD PARENT PRODUCTS ---
const loadParentProducts = async () => {
    loadingProd.value = true;
    try {
        const res = await request.get('/products', { 
            params: {
                page: page.value, size: pageSize.value, keyword: filter.keyword, status: 1 
            }
        });
        parentProducts.value = res.data.content;
        totalPages.value = res.data.totalPages;
    } catch (e) { console.error(e); } finally { loadingProd.value = false; }
};

const changePage = (p) => { if (p >= 0 && p < totalPages.value) { page.value = p; loadParentProducts(); } };
const resetFilter = () => { filter.keyword = ''; filter.mauSacId = ''; filter.kichThuocId = ''; page.value = 0; loadParentProducts(); };

// --- LOGIC FETCH VARIANTS & MAPPING DATA (ĐÃ SỬA) ---
const fetchVariantsByProductId = async (parentId) => {
    try {
        const res = await request.get(`/products/${parentId}/variants`);
        const variants = Array.isArray(res.data) ? res.data : (res.data.content || []);
        
        // Tìm thông tin cha để fill nếu thiếu
        const parent = parentProducts.value.find(p => p.id === parentId);

        return variants.map(v => ({
            id: v.id,
            maCtsp: v.maCtsp,
            tenSanPham: v.sanPham?.tenSanPham || parent?.tenSanPham || 'Sản phẩm',
            
            // --- FIX: Ưu tiên lấy trường phẳng (Flat) từ API ---
            tenThuongHieu: v.tenThuongHieu || parent?.tenThuongHieu || v.sanPham?.tenThuongHieu || '-',
            tenChatLieu: v.tenChatLieu || parent?.tenChatLieu || v.sanPham?.tenChatLieu || '-',
            
            // Mấy cái này thường nằm trong object con thì giữ nguyên hoặc thêm fallback
            tenKichThuoc: v.kichThuoc?.tenKichThuoc || v.tenKichThuoc || '-',
            tenMauSac: v.mauSac?.tenMauSac || v.tenMauSac || '-',
            
            // Map Ảnh
            hinhAnh: v.listAnh && v.listAnh.length > 0 ? v.listAnh[0] : (v.hinhAnh || parent?.hinhAnh || ''),
            
            giaBan: v.giaBan,
            parentId: parentId 
        }));
    } catch (e) { console.error(e); return []; }
};

const handleSelectParent = async (parentId) => {
    if (selectedParentIds.value.includes(parentId)) {
        const variants = await fetchVariantsByProductId(parentId);
        if (variants.length === 0) return Toast.fire({ icon: 'warning', title: 'Sản phẩm này không có biến thể!' });

        variants.forEach(v => {
            if (!selectedVariants.value.some(ex => ex.id === v.id)) selectedVariants.value.push(v);
        });
    } else {
        selectedVariants.value = selectedVariants.value.filter(v => v.parentId !== parentId);
    }
};

const toggleAllParent = async (e) => {
    const currentIds = parentProducts.value.map(p => p.id);
    if (e.target.checked) {
        for (const id of currentIds) {
            if (!selectedParentIds.value.includes(id)) {
                selectedParentIds.value.push(id);
                await handleSelectParent(id);
            }
        }
    } else {
        for (const id of currentIds) {
            const idx = selectedParentIds.value.indexOf(id);
            if (idx > -1) {
                selectedParentIds.value.splice(idx, 1);
                handleSelectParent(id);
            }
        }
    }
};

const isAllParentSelected = computed(() => parentProducts.value.length > 0 && parentProducts.value.every(p => selectedParentIds.value.includes(p.id)));

// --- LOGIC DETAIL LIST ---
const filteredDetails = computed(() => {
    return selectedVariants.value.filter(v => {
        const matchTH = !detailFilter.thuongHieu || v.tenThuongHieu === detailFilter.thuongHieu;
        const matchCL = !detailFilter.chatLieu || v.tenChatLieu === detailFilter.chatLieu;
        const matchKC = !detailFilter.kichThuoc || v.tenKichThuoc === detailFilter.kichThuoc;
        const matchMS = !detailFilter.mauSac || v.tenMauSac === detailFilter.mauSac;
        return matchTH && matchCL && matchKC && matchMS;
    });
});

const detailTotalPages = computed(() => Math.ceil(filteredDetails.value.length / detailPageSize) || 1);

const paginatedDetails = computed(() => {
    const start = (detailPage.value - 1) * detailPageSize;
    return filteredDetails.value.slice(start, start + detailPageSize);
});

const changeDetailPage = (p) => { if(p >= 1 && p <= detailTotalPages.value) detailPage.value = p; };
const resetDetailFilter = () => { 
    detailFilter.thuongHieu = ''; detailFilter.chatLieu = ''; 
    detailFilter.kichThuoc = ''; detailFilter.mauSac = ''; 
    detailPage.value = 1; 
};

const isAllDetailSelected = computed(() => {
    if (paginatedDetails.value.length === 0) return false;
    return paginatedDetails.value.every(v => selectedDetailIds.value.includes(v.id));
});

const toggleAllDetail = (e) => {
    const currentIds = paginatedDetails.value.map(v => v.id);
    if (e.target.checked) {
        selectedDetailIds.value = [...new Set([...selectedDetailIds.value, ...currentIds])];
    } else {
        selectedDetailIds.value = selectedDetailIds.value.filter(id => !currentIds.includes(id));
    }
};

const removeSelectedDetails = () => {
    selectedVariants.value = selectedVariants.value.filter(v => !selectedDetailIds.value.includes(v.id));
    selectedDetailIds.value = [];
};

const clearAllSelection = () => {
    selectedVariants.value = [];
    selectedParentIds.value = [];
    selectedDetailIds.value = [];
};

// --- SUBMIT ---
const createSale = async () => {
    if(!form.tenDotGiamGia.trim()) return Toast.fire({ icon: 'warning', title: 'Thiếu tên đợt giảm giá' });
    if(form.giaTriGiam <= 0) return Toast.fire({ icon: 'warning', title: 'Mức giảm phải > 0' });
    if(!form.ngayBatDau || !form.ngayKetThuc) return Toast.fire({ icon: 'warning', title: 'Chọn thời gian' });
    form.idChiTietSanPhams = selectedVariants.value.map(v => v.id);
    if(form.idChiTietSanPhams.length === 0) return Toast.fire({ icon: 'warning', title: 'Chưa chọn sản phẩm nào' });

    try {
        await request.post('/dot-giam-gia', form);
        localStorage.setItem('saleSuccessMessage', 'Tạo đợt giảm giá thành công!');
        router.push({ name: 'admin-sale-list' });
    } catch (e) {
        console.error(e);
        Toast.fire({ icon: 'error', title: e.response?.data?.message || 'Có lỗi xảy ra' });
    }
};

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
const handleImgError = (e) => { e.target.src = "https://via.placeholder.com/50?text=IMG"; };
const getColorCode = (name) => {
    const map = { 'Đen': '#000', 'Trắng': '#fff', 'Xanh': '#3b82f6', 'Đỏ': '#ef4444', 'Vàng': '#eab308', 'Hồng': '#ec4899', 'Xám': '#6b7280', 'Cam': '#f97316', 'Tím': '#a855f7' };
    return map[name] || '#e5e7eb';
};

onMounted(() => { fetchFilterOptions(); loadParentProducts(); });
</script>

<style scoped>
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background-color: #f8fafc; min-height: 100vh; }
.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.header-title h3 { font-weight: 700; color: #2b4360; font-size: 24px; margin: 0; }
.btn-back { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); border: 1px solid #cbd5e1; color: #ffffff; padding: 8px 16px; border-radius: 6px; cursor: pointer; font-weight: 600; font-size: 14px; transition: 0.2s; }
.btn-back:hover {background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #ffffff; }

/* LAYOUT: Chỉnh kích thước card trái to hơn */
.split-layout { 
    display: grid; 
    grid-template-columns: 500px 1fr; /* Trái 500px */
    gap: 20px; 
    height: 700px; 
}
.card { background: #fff; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); padding: 20px; border: 1px solid #bfdbfe; display: flex; flex-direction: column; height: 100%; overflow: hidden; position: relative; }

/* LEFT PANEL */
.panel-title { font-size: 16px; font-weight: 700; color: #334155; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px solid #f1f5f9; flex-shrink: 0; }
.form-container { flex: 1; overflow-y: auto; padding-right: 5px; }
.form-group { margin-bottom: 15px; }
.form-group label {  margin-bottom: 6px; font-weight: 600; font-size: 13px; color: #475569; }
.required { color: #ef4444; }
.form-control { width: 100%; padding: 10px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; transition: 0.2s; font-size: 14px; }
.form-control:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
/* Container chứa cả cụm radio */
.radio-group {
    display: flex;
    align-items: center;
    padding: 5px 0; /* Tạo chút khoảng trống trên dưới cho thoáng */
}

/* Từng item radio */
.radio-item {
    display: grid;
    grid-template-columns: auto 1fr; /* Cột 1 là nút, cột 2 là chữ */
    align-items: center;             /* Căn giữa theo trục dọc của Grid */
    gap: 8px;
    cursor: pointer;
}

.radio-item input[type="radio"] {
    /* Ép kích thước chuẩn để không bị trình duyệt nhảy size */
    width: 16px;
    height: 16px;
    margin: 0;
    padding: 0;
    
    /* Chiêu cuối: Dùng transform để nhích cái nút lên 1-2px nếu font chữ của mày 
       có "chân" làm cảm giác nút bị thấp hơn */
    transform: translateY(-1px); 
    
    cursor: pointer;
    accent-color: #0f172a;
}

.radio-item span {
    font-size: 14px;
    line-height: 1; /* Rất quan trọng để triệt tiêu khoảng cách thừa trên dưới chữ */
    display: inline-block;
    vertical-align: middle;
}
.form-footer { margin-top: 15px; padding-top: 15px; border-top: 1px dashed #e2e8f0; flex-shrink: 0; }
.btn-create { width: 100%; background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; padding: 12px; border: none; border-radius: 6px; font-weight: 600; cursor: pointer; font-size: 15px; box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); transition: all 0.2s; }
.btn-create:hover { transform: translateY(-2px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.3); }

/* RIGHT PANEL */
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; flex-shrink: 0; }
.panel-header h4 { margin: 0; font-size: 16px; font-weight: 700; color: #334155; }
.selected-count b { color: #2563eb; }
.filter-toolbar { display: flex; justify-content: space-between; gap: 10px; margin-bottom: 15px; flex-shrink: 0; }
.search-box { position: relative; flex: 1; }
.search-icon { position: absolute; left: 12px; top: 11px; color: #94a3b8; }
.search-box input { width: 100%; padding: 8px 10px 8px 36px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; height: 38px; font-weight: 600; color: #334155; }
.filter-group { display: flex; gap: 8px; }
.filter-select { padding: 0 10px; border: 1px solid #e2e8f0; border-radius: 6px; min-width: 110px; height: 38px; cursor: pointer; color: #475569; outline: none; }
.filter-btn { width: 38px; height: 38px; border: 1px solid #e2e8f0; background: #fff; border-radius: 6px; cursor: pointer; color: #64748b; display: flex; align-items: center; justify-content: center; }
.table-responsive { flex: 1; overflow-y: auto; border: 1px solid #f1f5f9; border-radius: 8px; margin-bottom: 10px; min-height: 0; }
.custom-table { width: 100%; border-collapse: separate; border-spacing: 0; font-size: 13px; }
.custom-table th { background: #ffffff; color: #000000d9; padding: 12px; font-weight: 700; border-bottom: 1px solid #e2e8f0; position: sticky; top: 0; z-index: 10; text-align: center; }
.custom-table td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; text-align: center; }
.selected-row { background-color: #f0f9ff; }
.img-wrapper { width: 40px; height: 40px; border-radius: 4px; overflow: hidden; border: 1px solid #e2e8f0; margin: 0 auto; display: flex; align-items: center; justify-content: center; }
.img-wrapper-sm { width: 35px; height: 35px; border-radius: 4px; overflow: hidden; border: 1px solid #e2e8f0; margin: 0 auto; display: flex; align-items: center; justify-content: center; }
.thumb-img { width: 100%; height: 100%; object-fit: cover; }
.prod-code { font-family: monospace; color: #2563eb; font-weight: 600; }
.prod-name { font-weight: 600; color: #1e293b; }

/* BOTTOM PANEL */
.bottom-panel { height: auto; min-height: 200px; margin-top: 30px; overflow: visible; }
.detail-filter-bar { display: flex; gap: 10px; margin-bottom: 15px; align-items: center; }
.detail-select { padding: 0 10px; border: 1px solid #e2e8f0; border-radius: 6px; height: 36px; min-width: 130px; cursor: pointer; color: #475569; outline: none; }
.btn-icon-filter { width: 36px; height: 36px; border: 1px solid #e2e8f0; background: #f8fafc; border-radius: 6px; cursor: pointer; color: #64748b; }
.btn-icon-filter:hover { background: #e2e8f0; }
.pagination-centered { display: flex; justify-content: center; align-items: center; margin-top: 15px; gap: 10px; flex-shrink: 0;}
.page-counter { font-size: 13px; color: #64748b; font-weight: 500; }
.badge-size { background: #f1f5f9; padding: 2px 8px; border-radius: 4px; color: #475569; font-weight: 600; }
.color-dot-wrapper { display: flex; align-items: center; gap: 5px; }
.color-dot { display: block; width: 12px; height: 12px; border-radius: 50%; border: 1px solid #e2e8f0; }
.btn-sm { padding: 5px 12px; font-size: 12px; border-radius: 4px; cursor: pointer; font-weight: 600; }
.btn-outline-danger { background: white; border: 1px solid #ef4444; color: #ef4444; }
.btn-outline-danger:hover { background: #fee2e2; }
.btn-danger { background: #ef4444; border: 1px solid #ef4444; color: white; }
.btn-danger:hover { background: #dc2626; }
.input-den::placeholder { color: #000 !important; opacity: 0.6 !important; font-weight: 500; }
.text-right { text-align: right !important; }
.font-bold { font-weight: 700; }
.page-btn { min-width: 30px; height: 30px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; cursor: pointer; color: #64748b; margin-left: 5px; }
.mr-2 { margin-right: 10px; }
</style>
