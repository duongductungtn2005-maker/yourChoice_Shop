<template>
  <div class="page-container">
    <div class="header-row">
       <div class="title-group">
           <h2 class="page-title">Cập nhật đợt giảm giá</h2> 
           <p class="sub-title">Chỉnh sửa thông tin và danh sách sản phẩm áp dụng</p>
       </div>
       <router-link to="/admin/sales" class="btn-back">
           <i class="fas fa-arrow-left"></i> Quay lại
       </router-link>
    </div>

    <div class="main-layout">
      
      <div class="card left-panel">
        <h4 class="card-title">Thông tin chương trình</h4>
        <div class="form-body">
            <div class="form-group">
              <label>Tên chương trình <span class="required">*</span></label>
              <input v-model="form.tenDotGiamGia" class="custom-input" placeholder="VD: Sale Tết 2026..." />
            </div>
            <div class="form-row">
                <div class="form-group flex-grow">
                    <label>Mức giảm <span class="required">*</span></label>
                    <input v-model.number="form.giaTriGiam" type="number" class="custom-input" placeholder="0" />
                </div>
                <div class="form-group" style="width: 120px;">
                    <label>Đơn vị</label>
                    <select v-model="form.loaiGiamGia" class="custom-input select-input">
                        <option value="%">%</option>
                        <option value="VND">VND</option>
                    </select>
                </div>
            </div>
            <div class="form-group">
              <label>Thời gian áp dụng <span class="required">*</span></label>
              <div class="date-group-row">
                  <div class="date-item">
                      <span class="tiny-label">Từ ngày</span>
                      <input type="datetime-local" v-model="form.ngayBatDau" class="custom-input date-input" />
                  </div>
                  <div class="date-separator">-</div>
                  <div class="date-item">
                      <span class="tiny-label">Đến ngày</span>
                      <input type="datetime-local" v-model="form.ngayKetThuc" class="custom-input date-input" />
                  </div>
              </div>
            </div>
            <div class="summary-box" :class="{ 'active': selectedIds.length > 0 }">
                <div class="s-icon"><i class="fas fa-check-circle"></i></div>
                <div class="s-content">
                    <span>Đã chọn áp dụng:</span>
                    <strong>{{ selectedIds.length }} sản phẩm chi tiết</strong>
                </div>
            </div>
            <button @click="submitForm" class="btn-submit">
                <i class="fas fa-save"></i> Lưu thay đổi
            </button>
        </div>
      </div>

      <div class="card right-panel">
        <div class="panel-header">
            <h4>Danh sách sản phẩm</h4>
            <div class="search-wrap">
                <i class="fas fa-search"></i>
                <input v-model="searchParent" placeholder="Tìm tên/mã SP..." />
            </div>
        </div>

        <div class="product-list-wrapper custom-scrollbar">
            <table class="simple-table">
                <thead>
                    <tr>
                        <th width="40px" class="text-center">
                            <input type="checkbox" class="custom-checkbox" 
                                   :checked="isAllParentsViewed" 
                                   @change="toggleAllParentsView">
                        </th>
                        <th width="50px" class="text-center">STT</th>
                        <th>Thông tin sản phẩm</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="paginatedParents.length === 0">
                        <td colspan="3" class="text-center empty-text">
                            Không tìm thấy sản phẩm nào
                        </td>
                    </tr>
                    <tr 
                        v-for="(sp, index) in paginatedParents" 
                        :key="sp.id" 
                        @click="toggleParentView(sp)"
                        :class="{ 'row-active': viewedParentIds.includes(sp.id) }"
                    >
                        <td class="text-center" @click.stop>
                            <input type="checkbox" class="custom-checkbox"
                                   :checked="viewedParentIds.includes(sp.id)"
                                   @change="toggleParentView(sp)">
                        </td>
                        <td class="text-center text-muted">
                            {{ (productPage - 1) * productPageSize + index + 1 }}
                        </td>
                        <td>
                            <div class="p-name">{{ sp.tenSanPham }}</div>
                            <div class="p-code">{{ sp.maSanPham }}</div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="panel-footer-pagination" v-if="filteredParents.length > 0">
            <div class="page-info">
                {{ (productPage - 1) * productPageSize + 1 }} - 
                {{ Math.min(productPage * productPageSize, filteredParents.length) }} 
                trên {{ filteredParents.length }}
            </div>
            <div class="page-controls">
                <button 
                    class="page-btn" 
                    :disabled="productPage === 1" 
                    @click="productPage--"
                >
                    <i class="fas fa-chevron-left"></i>
                </button>
                <button 
                    class="page-btn" 
                    :disabled="productPage >= totalProductPages" 
                    @click="productPage++"
                >
                    <i class="fas fa-chevron-right"></i>
                </button>
            </div>
        </div>
      </div>
    </div>

    <transition name="fade">
        <div class="card bottom-panel" v-if="viewedParentIds.length > 0">
            <div class="panel-header-bottom">
                <div class="left-group">
                    <h4>Chi tiết biến thể sản phẩm</h4>
                    <span class="sub-text">Danh sách các biến thể thuộc các sản phẩm đã tích chọn ở trên</span>
                </div>
            </div>

            <div class="table-responsive custom-scrollbar">
                <table class="custom-table">
                    <thead>
                        <tr>
                            <th width="40px" class="text-center">
                                <input type="checkbox" class="custom-checkbox"
                                       :checked="isAllDisplayedVariantsSelected"
                                       @change="toggleSelectAllDisplayed">
                            </th>
                            <th width="50px" class="text-center">STT</th>
                            <th>Mã Biến thể</th>
                            <th>Sản phẩm gốc</th>
                            <th>Thuộc tính</th>
                            <th>Giá bán</th>
                            <th class="text-center">Kho</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(d, index) in displayedVariants" :key="d.id" :class="{ 'selected-row': selectedIds.includes(d.id) }">
                            <td class="text-center">
                                <input type="checkbox" :value="d.id" v-model="selectedIds" class="custom-checkbox">
                            </td>
                            <td class="text-center text-muted">{{ index + 1 }}</td>
                            <td class="text-code">{{ d.maSanPham }}</td>
                            <td style="font-weight: 500;">{{ d.tenSanPham }}</td>
                            <td>
                                <span class="variant-tag">{{ d.mauSac }}</span>
                                <span class="variant-tag">{{ d.kichThuoc }}</span>
                            </td>
                            <td class="text-price">{{ formatCurrency(d.giaBan) }}</td>
                            <td class="text-center">{{ d.soLuong }}</td>
                        </tr>
                        <tr v-if="displayedVariants.length === 0">
                            <td colspan="7" class="text-center empty-state">Đang tải dữ liệu...</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import request from '@/services/request';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const id = route.params.id; // Lấy ID từ URL

// --- STATE ---
const form = ref({
    maDotGiamGia: '', tenDotGiamGia: '', giaTriGiam: 0, loaiGiamGia: '%',
    ngayBatDau: '', ngayKetThuc: '', trangThai: 1, idChiTietSanPhams: [] 
});

const parentProducts = ref([]);
const viewedParentIds = ref([]); // Danh sách ID cha đang mở xem
const variantCache = ref({});
const selectedIds = ref([]); // Danh sách ID biến thể đã chọn
const searchParent = ref('');

// --- PAGINATION STATE ---
const productPage = ref(1);
const productPageSize = ref(7); 

// --- LOAD DATA ---
onMounted(async () => {
    try {
        // 1. Lấy danh sách sản phẩm cha
        const resProd = await request.get('/san-pham/active'); 
        parentProducts.value = resProd.data;

        // 2. Lấy thông tin đợt giảm giá
        const resDetail = await request.get(`/dot-giam-gia/${id}`);
        form.value = resDetail.data;

        // 3. Lấy danh sách các sản phẩm ĐANG CÓ trong đợt
        const resSelected = await request.get(`/dot-giam-gia/${id}/products`);
        const selectedVariants = resSelected.data;

        // 3.1. Đổ ID vào mảng selectedIds để checkbox tự tích
        selectedIds.value = selectedVariants.map(p => p.id); 

        // 3.2. LOGIC TỰ ĐỘNG HIỂN THỊ BẢNG DƯỚI (Fix lỗi chỉ hiện số lượng)
        const parentIdsToShow = new Set();
        
        selectedVariants.forEach(variant => {
            // Tìm cha của biến thể này trong danh sách cha (dựa vào tên)
            const parent = parentProducts.value.find(p => p.tenSanPham === variant.tenSanPham);
            if (parent) {
                parentIdsToShow.add(parent.id);
            }
        });

        // Cập nhật danh sách xem -> Bảng dưới sẽ hiện ra
        viewedParentIds.value = Array.from(parentIdsToShow);

        // Tải dữ liệu chi tiết cho các cha này
        for (const parentId of viewedParentIds.value) {
            await fetchVariants(parentId);
        }

    } catch (e) { console.error(e); }
});

// --- FILTER & PAGINATION LOGIC ---
const filteredParents = computed(() => {
    if (!searchParent.value) return parentProducts.value;
    const lower = searchParent.value.toLowerCase();
    return parentProducts.value.filter(p => 
        p.tenSanPham.toLowerCase().includes(lower) || 
        p.maSanPham.toLowerCase().includes(lower)
    );
});

watch(searchParent, () => { productPage.value = 1; });

const paginatedParents = computed(() => {
    const start = (productPage.value - 1) * productPageSize.value;
    const end = start + productPageSize.value;
    return filteredParents.value.slice(start, end);
});

const totalProductPages = computed(() => {
    return Math.ceil(filteredParents.value.length / productPageSize.value);
});

// --- HELPER FETCH VARIANTS ---
const fetchVariants = async (parentId) => {
    if (variantCache.value[parentId]) return variantCache.value[parentId];
    try {
        const res = await request.get(`/chi-tiet-san-pham/by-product/${parentId}`);
        // Gán tên cha để hiển thị đẹp
        const parent = parentProducts.value.find(p => p.id === parentId);
        const variants = res.data.map(v => ({ ...v, tenSanPham: parent?.tenSanPham || '' }));
        variantCache.value[parentId] = variants;
        return variants;
    } catch (e) { console.error(e); return []; }
};

// --- LOGIC TOGGLE PARENT ---
const toggleParentView = async (sp) => {
    const idx = viewedParentIds.value.indexOf(sp.id);
    if (idx > -1) viewedParentIds.value.splice(idx, 1);
    else {
        viewedParentIds.value.push(sp.id);
        if (!variantCache.value[sp.id]) await fetchVariants(sp.id);
    }
};

const isAllParentsViewed = computed(() => {
    if (filteredParents.value.length === 0) return false;
    // Kiểm tra xem tất cả SP trong danh sách tìm kiếm/hiện tại có đang được xem không
    // Chỉ check trên filteredParents để UX tốt hơn
    return filteredParents.value.every(p => viewedParentIds.value.includes(p.id));
});

const toggleAllParentsView = async (e) => {
    const checked = e.target.checked;
    if (checked) {
        const allIds = filteredParents.value.map(p => p.id);
        viewedParentIds.value = [...new Set([...viewedParentIds.value, ...allIds])];
        for (const id of allIds) { if (!variantCache.value[id]) await fetchVariants(id); }
    } else {
        const allIds = filteredParents.value.map(p => p.id);
        viewedParentIds.value = viewedParentIds.value.filter(id => !allIds.includes(id));
    }
};

// --- DISPLAY & SELECT VARIANTS ---
const displayedVariants = computed(() => {
    let list = [];
    viewedParentIds.value.forEach(parentId => {
        if (variantCache.value[parentId]) list = list.concat(variantCache.value[parentId]);
    });
    return list;
});

const isAllDisplayedVariantsSelected = computed(() => {
    if (displayedVariants.value.length === 0) return false;
    const ids = displayedVariants.value.map(v => v.id);
    return ids.every(id => selectedIds.value.includes(id));
});

const toggleSelectAllDisplayed = (e) => {
    const checked = e.target.checked;
    const ids = displayedVariants.value.map(v => v.id);
    if (checked) selectedIds.value = [...new Set([...selectedIds.value, ...ids])];
    else selectedIds.value = selectedIds.value.filter(id => !ids.includes(id));
};

// --- SUBMIT ---
const submitForm = async () => {
    if (!form.value.tenDotGiamGia) return alert("Vui lòng nhập tên");
    if (new Date(form.value.ngayBatDau) >= new Date(form.value.ngayKetThuc)) return alert("Ngày kết thúc lỗi!");
    form.value.idChiTietSanPhams = selectedIds.value;

    try {
        await request.put(`/dot-giam-gia/${id}`, form.value);
        alert("Cập nhật thành công!");
        router.push('/admin/sales');
    } catch (e) { alert("Lỗi: " + (e.response?.data?.message || e.message)); }
};

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
</script>

<style scoped>
/* GLOBAL & LAYOUT */
.page-container { padding: 24px; background: #f8f9fa; min-height: 100vh; font-family: 'Inter', sans-serif; color: #334155; }
.header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; }
.sub-title { margin: 4px 0 0; font-size: 13px; color: #64748b; }
.btn-back { color: #64748b; text-decoration: none; font-weight: 600; display: flex; align-items: center; gap: 6px; font-size: 14px; transition: 0.2s; }
.btn-back:hover { color: #2b4360; }

.main-layout { display: grid; grid-template-columns: 550px 1fr; gap: 24px; align-items: start; }

/* CARD */
.card { background: white; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.03); border: 1px solid #f1f5f9; overflow: hidden; }
.left-panel { height: fit-content; padding: 24px; } 
.right-panel { height: 600px; display: flex; flex-direction: column; } 

/* FORM */
.card-title { margin: 0 0 20px 0; font-size: 16px; font-weight: 700; color: #1e293b; border-bottom: 2px solid #f1f5f9; padding-bottom: 12px; }
.form-group { margin-bottom: 18px; }
.form-group label { display: block; margin-bottom: 8px; font-size: 13px; font-weight: 600; color: #475569; }
.required { color: #ef4444; margin-left: 3px; }
.custom-input { width: 100%; padding: 10px 12px; border: 1px solid #e2e8f0; border-radius: 6px; font-size: 14px; outline: none; transition: 0.2s; color: #1e293b; background: #fff; height: 42px; box-sizing: border-box; }
.custom-input:focus { border-color: #2b4360; box-shadow: 0 0 0 3px rgba(43, 67, 96, 0.1); }
.form-row { display: flex; gap: 12px; }
.flex-grow { flex: 1; }
.select-input { background-color: #f8fafc; font-weight: 600; cursor: pointer; }

.date-group-row { display: flex; align-items: flex-end; gap: 10px; width: 100%; }
.date-item { flex: 1; width: 100%; }
.date-input { width: 100% !important; min-width: 0; }
.date-separator { padding-bottom: 10px; color: #cbd5e1; font-weight: bold; }
.tiny-label { display: block; font-size: 11px; color: #94a3b8; margin-bottom: 4px; }

.summary-box { background: #f8fafc; border: 1px dashed #cbd5e1; padding: 12px; border-radius: 8px; display: flex; align-items: center; gap: 12px; margin: 24px 0; transition: 0.3s; }
.summary-box.active { background: #f0fdf4; border-color: #86efac; }
.s-icon { width: 32px; height: 32px; background: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #cbd5e1; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.active .s-icon { color: #16a34a; }
.s-content span { display: block; font-size: 12px; color: #64748b; }
.s-content strong { font-size: 14px; color: #1e293b; }
.btn-submit { width: 100%; padding: 12px; border: none; border-radius: 6px; background: #2b4360; color: white; font-weight: 600; font-size: 14px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; transition: 0.2s; }
.btn-submit:hover { background: #1e2f45; transform: translateY(-1px); }

/* RIGHT PANEL & PAGINATION */
.panel-header { padding: 16px 20px; border-bottom: 1px solid #f1f5f9; background: #fff; display: flex; justify-content: space-between; align-items: center; }
.search-wrap input { width: 100%; padding: 8px 12px 8px 36px; border: 1px solid #e2e8f0; border-radius: 20px; outline: none; font-size: 13px; transition: 0.2s; }
.search-wrap { position: relative; width: 250px; }
.search-wrap i { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; font-size: 14px; }

.product-list-wrapper { flex: 1; overflow-y: auto; background: #fff; }

/* === STYLES CHO PAGINATION FOOTER === */
.panel-footer-pagination {
    border-top: 1px solid #f1f5f9;
    padding: 12px 20px;
    background: #fff;
    display: flex;
    justify-content: flex-end; /* Căn phải */
    align-items: center;
    gap: 15px;
}
.page-info { font-size: 13px; color: #64748b; }
.page-controls { display: flex; gap: 8px; }
.page-btn {
    width: 30px; height: 30px;
    border: 1px solid #e2e8f0;
    background: white;
    border-radius: 6px;
    cursor: pointer;
    color: #64748b;
    display: flex; align-items: center; justify-content: center;
    transition: 0.2s;
}
.page-btn:hover:not(:disabled) { background: #f1f5f9; color: #2b4360; }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* TABLE */
.simple-table, .custom-table { width: 100%; border-collapse: collapse; }
.simple-table th, .custom-table th { position: sticky; top: 0; background: #f8fafc; color: #64748b; font-weight: 600; font-size: 12px; text-transform: uppercase; padding: 12px 16px; border-bottom: 1px solid #e2e8f0; z-index: 5; text-align: left; }
.simple-table td, .custom-table td { padding: 12px 16px; border-bottom: 1px solid #f1f5f9; font-size: 14px; vertical-align: middle; }
.simple-table tr:hover { background: #f8fafc; cursor: pointer; }
.simple-table tr.row-active { background: #eff6ff; }
.simple-table tr.row-active td:first-child { border-left: 3px solid #2b4360; } 

.p-name { font-weight: 600; color: #334155; margin-bottom: 2px; }
.p-code { font-size: 12px; color: #94a3b8; background: #f1f5f9; display: inline-block; padding: 2px 6px; border-radius: 4px; }
.empty-text { padding: 40px; color: #94a3b8; font-style: italic; }

/* BOTTOM PANEL */
.bottom-panel { margin-top: 24px; border-top: 4px solid #2b4360; grid-column: 1 / -1; }
.panel-header-bottom { padding: 16px 20px; border-bottom: 1px solid #f1f5f9; }
.text-price { font-weight: 600; color: #dc2626; }
.variant-tag { background: #f1f5f9; border: 1px solid #e2e8f0; padding: 2px 8px; border-radius: 4px; font-size: 12px; margin-right: 6px; color: #475569; }
.custom-checkbox { width: 18px; height: 18px; cursor: pointer; accent-color: #2b4360; }
.custom-scrollbar::-webkit-scrollbar { width: 6px; height: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: #f1f5f9; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 3px; }

/* Animation */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease, transform 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; transform: translateY(10px); }
</style>