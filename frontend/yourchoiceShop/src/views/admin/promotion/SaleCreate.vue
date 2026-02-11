<template>
  <div class="page-container">
    <div class="header">
        <h3>Đợt giảm giá / Thêm đợt giảm giá</h3>
    </div>

    <div class="card section-info">
        <h4 class="section-title">Thông tin chương trình</h4>
        <div class="row-3">
             <div class="form-group">
                <label>Tên chương trình <span class="required">*</span></label>
                <input  v-model="form.tenDotGiamGia" placeholder="VD: Sale Black Friday" class="form-control" />
            </div>
             <div class="form-group">
                <label>Mức giảm giá <span class="required">*</span></label>
                <div class="input-group">
                    <input v-model="form.giaTriGiam" type="number" class="form-control" />
                    <select v-model="form.loaiGiamGia" class="unit-select">
                        <option value="VND">VND</option>
                        <option value="%">%</option>
                    </select>
                </div>
            </div>
        </div>
        <div class="row-3">
             <div class="form-group">
                <label>Thời gian bắt đầu <span class="required">*</span></label>
                <input v-model="form.ngayBatDau" type="datetime-local" class="form-control" />
            </div>
             <div class="form-group">
                <label>Thời gian kết thúc <span class="required">*</span></label>
                <input v-model="form.ngayKetThuc" type="datetime-local" class="form-control" />
            </div>
        </div>
        
        <div class="action-bar">
            <button type="button" @click="$router.go(-1)" class="btn btn-outline">Hủy</button>
            <button type="button" @click="createSale" class="btn btn-gradient">Xác nhận & Tạo mới</button>
        </div>
    </div>

    <div class="card section-product">
        <div class="panel-header">
            <h4>Sản phẩm áp dụng</h4>
            <div class="selected-count">
                Đã chọn: <b>{{ form.idChiTietSanPhams.length }}</b> sản phẩm
            </div>
        </div>

        <div class="filter-toolbar">
            <div class="search-box">
                <i class="fas fa-search icon"></i>
                <input class="input-den" v-model="filter.keyword" @keyup.enter="loadProducts" placeholder="Tìm tên/mã sản phẩm..." />
            </div>
            
            <select v-model="filter.mauSacId" @change="loadProducts" class="filter-select">
                <option value="">-- Màu sắc --</option>
                <option v-for="ms in colors" :key="ms.id" :value="ms.id">{{ ms.tenMauSac || ms.ten }}</option>
            </select>

            <select v-model="filter.kichThuocId" @change="loadProducts" class="filter-select">
                <option value="">-- Kích thước --</option>
                <option v-for="kt in sizes" :key="kt.id" :value="kt.id">{{ kt.tenKichThuoc || kt.ten }}</option>
            </select>

            <button type="button" class="btn btn-black btn-sm" @click="resetFilter">
                <i class="fas fa-sync-alt"></i> Đặt lại
            </button>
        </div>

        <div class="table-responsive">
            <table class="custom-table">
                <thead>
                    <tr>
                        <th width="40px" class="text-center">
                            <input type="checkbox" @change="toggleAll" :checked="isAllSelected" />
                        </th>
                        <th width="60px" class="text-center">Ảnh</th>
                        <th>Mã SP</th>
                        <th>Tên sản phẩm</th>
                        <th class="text-center">Giá bán</th> 
                        <th class="text-center">Thương hiệu</th>
                        <th class="text-center">SL</th>
                        <th class="text-center">Chất liệu</th>
                        <th class="text-center">Kích cỡ</th>
                        <th class="text-center">Màu sắc</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="loadingProd">
                        <td colspan="10" class="text-center py-4">Đang tải dữ liệu...</td>
                    </tr>
                    <tr v-else-if="products.length === 0">
                         <td colspan="10" class="text-center empty-state">Không tìm thấy sản phẩm nào.</td>
                    </tr>
                    <tr v-else v-for="sp in products" :key="sp.id" :class="{ 'selected-row': form.idChiTietSanPhams.includes(sp.id) }">
                        <td class="text-center">
                            <input type="checkbox" :value="sp.id" v-model="form.idChiTietSanPhams" />
                        </td>
                        <td class="text-center">
                            <div class="img-wrapper">
                                <img v-if="sp.hinhAnh" :src="'http://localhost:8080/api/v1/product-images/' + sp.hinhAnh" class="thumb-img" @error="handleImgError" />
                                <div v-else class="img-placeholder"><i class="fas fa-image"></i></div>
                            </div>
                        </td>
                        <td class="prod-code">{{ sp.maCtsp }}</td>
                        <td>
                            <div class="prod-name" :title="sp.sanPham?.tenSanPham">{{ sp.sanPham?.tenSanPham }}</div>
                        </td>
                        
                        <td class="text-center">
                            <div class="price-box">
                                <div class="old-price">{{ formatCurrency(sp.giaBan) }}</div>
                                <div class="new-price">{{ calculateNewPrice(sp.giaBan) }}</div>
                                <div v-if="form.giaTriGiam > 0" class="discount-badge">
                                    -{{ calculateDiscountPercent(sp.giaBan) }}%
                                </div>
                            </div>
                        </td>

                        <td class="text-center">
                            {{ sp.thuongHieu?.tenThuongHieu || sp.sanPham?.thuongHieu?.tenThuongHieu || '-' }}
                        </td>
                        <td class="text-center font-weight-bold">{{ sp.soLuong }}</td>
                        <td class="text-center">
                            {{ sp.chatLieu?.tenChatLieu || sp.sanPham?.chatLieu?.tenChatLieu || '-' }}
                        </td>
                        <td class="text-center">
                            <span class="badge-size">{{ sp.kichThuoc?.tenKichThuoc }}</span>
                        </td>
                        
                        <td class="text-center">
                            <div class="color-wrapper justify-center">
                                <span class="color-dot" :style="{ backgroundColor: getColorCode(sp.mauSac?.tenMauSac) }"></span>
                                {{ sp.mauSac?.tenMauSac }}
                            </div>
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
                bản ghi
            </div>
            
            <div class="page-controls">
                <button class="page-btn" :disabled="page === 0" @click="changePage(page - 1)">‹</button>
                
                <button 
                    v-for="p in visiblePages" 
                    :key="p" 
                    class="page-btn" 
                    :class="{ 'active': p === page + 1 }"
                    @click="changePage(p - 1)"
                >
                    {{ p }}
                </button>

                <button class="page-btn" :disabled="page >= totalPages - 1" @click="changePage(page + 1)">›</button>
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue';
import request from '@/services/request';
import { useRouter } from 'vue-router';
import { toastSuccess, toastError, Toast } from '@/utils/toast';

const router = useRouter();

const form = reactive({
    tenDotGiamGia: '', giaTriGiam: 0, loaiGiamGia: 'VND',
    ngayBatDau: '', ngayKetThuc: '', trangThai: 1,
    idChiTietSanPhams: [] 
});

const products = ref([]);
const loadingProd = ref(false);
const page = ref(0);
const pageSize = ref(10);
const totalPages = ref(0);

const filter = reactive({ keyword: '', mauSacId: '', kichThuocId: '' });
const colors = ref([]);
const sizes = ref([]);

const loadFilterOptions = async () => {
    try {
        const [resMS, resKT] = await Promise.all([
            request.get('/mau-sac/list', { params: { trangThai: 1 } }),
            request.get('/kich-thuoc/list', { params: { trangThai: 1 } })
        ]);
        colors.value = resMS.data || [];
        sizes.value = resKT.data || [];
    } catch (e) { console.error("Lỗi load bộ lọc", e); }
};

const loadProducts = async () => {
    loadingProd.value = true;
    try {
        const params = {
            page: page.value, size: pageSize.value, keyword: filter.keyword,
            idMauSac: filter.mauSacId || null, idKichThuoc: filter.kichThuocId || null,
            trangThai: 1 
        };
        const res = await request.get('/chi-tiet-san-pham', { params });
        products.value = res.data.content;
        totalPages.value = res.data.totalPages;
    } catch (e) { console.error(e); } finally { loadingProd.value = false; }
};

// Pagination Logic
const changePage = (p) => { if (p >= 0 && p < totalPages.value) { page.value = p; loadProducts(); } };
const handlePageSizeChange = () => { page.value = 0; loadProducts(); };

const visiblePages = computed(() => {
    let pages = [];
    // Logic hiển thị các trang xung quanh trang hiện tại
    for (let i = 1; i <= totalPages.value; i++) {
        if (i === 1 || i === totalPages.value || (i >= page.value && i <= page.value + 2)) {
            pages.push(i);
        }
    }
    // Lọc trùng (nếu có)
    return [...new Set(pages)].sort((a, b) => a - b);
});

const resetFilter = () => { filter.keyword = ''; filter.mauSacId = ''; filter.kichThuocId = ''; page.value = 0; loadProducts(); };

const isAllSelected = computed(() => products.value.length > 0 && products.value.every(p => form.idChiTietSanPhams.includes(p.id)));
const toggleAll = (e) => {
    const currentIds = products.value.map(p => p.id);
    if (e.target.checked) {
        const newIds = currentIds.filter(id => !form.idChiTietSanPhams.includes(id));
        form.idChiTietSanPhams.push(...newIds);
    } else {
        form.idChiTietSanPhams = form.idChiTietSanPhams.filter(id => !currentIds.includes(id));
    }
};

const calculateNewPrice = (oldPrice) => {
    if (!form.giaTriGiam) return formatCurrency(oldPrice);
    let price = Number(oldPrice); let giam = Number(form.giaTriGiam);
    let final = form.loaiGiamGia === 'VND' ? (price - giam) : (price * (100 - giam) / 100);
    return formatCurrency(Math.max(0, final));
};

const calculateDiscountPercent = (oldPrice) => {
    if(form.loaiGiamGia === '%') return form.giaTriGiam;
    let percent = Math.round((Number(form.giaTriGiam) / Number(oldPrice)) * 100);
    return percent > 100 ? 100 : (percent < 0 ? 0 : percent);
}

const createSale = async () => {
    if(!form.tenDotGiamGia.trim()) return Toast.fire({ icon: 'warning', title: 'Thiếu tên chương trình' });
    if(form.giaTriGiam <= 0) return Toast.fire({ icon: 'warning', title: 'Mức giảm phải > 0' });
    if(!form.ngayBatDau || !form.ngayKetThuc) return Toast.fire({ icon: 'warning', title: 'Chọn đầy đủ thời gian' });
    if(form.idChiTietSanPhams.length === 0) return Toast.fire({ icon: 'warning', title: 'Chưa chọn sản phẩm nào' });

    try {
        await request.post('/dot-giam-gia', form);
        localStorage.setItem('saleSuccessMessage', 'Tạo đợt giảm giá thành công!');
        router.push({ name: 'admin-sale-list' });
    } catch (e) {
        console.error(e);
        toastError(e.response?.data?.message || 'Có lỗi xảy ra');
    }
};

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
const handleImgError = (e) => { e.target.src = "https://via.placeholder.com/50?text=No+Img"; };

const getColorCode = (name) => {
    const map = { 'Đen': '#000', 'Trắng': '#fff', 'Xanh': '#3b82f6', 'Đỏ': '#ef4444', 'Vàng': '#eab308', 'Hồng': '#ec4899', 'Xám': '#6b7280' };
    return map[name] || '#e5e7eb';
};

onMounted(() => { loadFilterOptions(); loadProducts(); });
</script>

<style scoped>
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background-color: #f8f9fa; min-height: 100vh; }
.header { margin-bottom: 20px; }
.header h3 { font-weight: 700; color: #2b4360; font-size: 24px; }

/* === CARD STYLE === */
.card { 
    background: #fff; border-radius: 16px; border: 1px solid #bfdbfe !important; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); padding: 24px; margin-bottom: 24px;
}
.section-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 20px; text-transform: uppercase; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px; }

/* Form */
.row-3 { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 6px; font-weight: 600; font-size: 13px; color: #334155; }
.required { color: #ef4444; }
.form-control { width: 100%; padding: 10px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; }
.form-control::placeholder {

    color: #000000 !important;  /* Màu đen */
    opacity: 0.5 !important;      /* Chống mờ */
    font-weight: 500;           /* Đậm lên tí cho dễ đọc (tùy chọn) */

}
.input-group { display: flex; }
.input-group input { border-top-right-radius: 0; border-bottom-right-radius: 0; }
.unit-select { border: 1px solid #e2e8f0; border-left: none; background: #f8fafc; padding: 0 15px; border-top-right-radius: 6px; border-bottom-right-radius: 6px; font-weight: 600; cursor: pointer; }

/* Action Bar */
.action-bar { display: flex; justify-content: flex-end; gap: 12px; margin-top: 20px; padding-top: 15px; border-top: 1px dashed #e2e8f0; }
.btn { padding: 10px 24px; border-radius: 6px; font-weight: 600; cursor: pointer; border: 1px solid transparent; font-size: 14px; }
.btn-outline { background: #fff; border-color: #cbd5e1; color: #475569; }
.btn-gradient { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); }

/* === UPDATE: Nút Đặt lại màu đen === */
.btn-black { background: #1e293b; color: #fff; border-color: #1e293b; }
.btn-black:hover { background: #0f172a; }

/* Filter Toolbar */
.filter-toolbar { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 15px; align-items: center; }
.search-box { position: relative; width: 250px; }
.search-box .icon { position: absolute; left: 10px; top: 11px; color: #94a3b8; }
.search-box input { width: 100%; padding: 8px 10px 8px 36px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; }
.filter-select { padding: 0 10px; border: 1px solid #e2e8f0; border-radius: 6px; min-width: 140px; height: 38px; cursor: pointer; }
.btn-sm { padding: 8px 16px; font-size: 13px; height: 38px; }

/* === TABLE STYLING === */
.table-responsive { overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 8px; }
.custom-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.custom-table th { background: #eff6ff; color: #1e40af; padding: 12px; font-weight: 700; white-space: nowrap; border-bottom: none; }
.custom-table td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; color: #334155; }
.selected-row { background-color: #f0f9ff; }

/* Utilities */
.text-center { text-align: center !important; }
.justify-center { justify-content: center; }
.font-weight-bold { font-weight: 600; }

/* Product Columns */
.img-wrapper { width: 40px; height: 40px; border-radius: 4px; overflow: hidden; border: 1px solid #e2e8f0; margin: 0 auto; }
.thumb-img { width: 100%; height: 100%; object-fit: cover; }
.prod-name { font-weight: 600; color: #334155; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 180px; }
.prod-code { font-family: monospace; color: #2563eb; }

/* === UPDATE: Price Column Box === */
.price-box { display: flex; flex-direction: column; align-items: center; gap: 2px; }
.old-price { text-decoration: line-through; color: #94a3b8; font-size: 11px; }
.new-price { color: #dc2626; font-weight: 700; font-size: 13px; }
.discount-badge { 
    background-color: #ef4444; color: white; 
    border-radius: 10px; padding: 2px 6px; 
    font-size: 10px; font-weight: 700; 
    display: inline-block; margin-top: 2px;
}

/* Attributes */
.badge-size { background: #f1f5f9; padding: 2px 8px; border-radius: 4px; color: #475569; font-weight: 600; }
.color-wrapper { display: flex; align-items: center; gap: 6px; }
.color-dot { display: inline-block; width: 12px; height: 12px; border-radius: 50%; border: 1px solid #e2e8f0; }

/* === UPDATE: Pagination Style (Giống quản lý) === */
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.pagination-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 15px; font-size: 13px; color: #64748b; }
.page-info select { border: 1px solid #e2e8f0; border-radius: 4px; padding: 2px 5px; margin: 0 5px; outline: none; cursor: pointer; }

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
/* Màu chữ placeholder đen xì, rõ nét */
.input-den::placeholder {
    color: #000000 !important;  /* Màu đen */
    opacity: 1 !important;      /* Chống mờ */
    font-weight: 500;           /* Đậm lên tí cho dễ đọc (tùy chọn) */
}
</style>