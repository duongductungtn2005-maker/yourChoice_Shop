<template>
  <div class="page-container">
    <div class="header">
        <h3>Đợt giảm giá / Thêm đợt giảm giá</h3>
    </div>

    <div class="card section-info">
        <h4 class="section-title">Thông tin chương trình</h4>
        <div class="row-3">
             <div class="form-group">
                <label>Tên chương trình</label>
                <input v-model="form.tenDotGiamGia" placeholder="VD: Sale Black Friday" class="form-control" />
            </div>
             <div class="form-group">
                <label>Mức giảm giá</label>
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
                <label>Thời gian bắt đầu</label>
                <input v-model="form.ngayBatDau" type="datetime-local" class="form-control" />
            </div>
             <div class="form-group">
                <label>Thời gian kết thúc</label>
                <input v-model="form.ngayKetThuc" type="datetime-local" class="form-control" />
            </div>
        </div>
        <div class="action-bar">
            <button @click="createSale" class="btn-primary">+ Xác nhận & Tạo mới</button>
        </div>
    </div>

    <div class="card section-product">
        <div class="panel-header">
            <h4>Sản phẩm áp dụng</h4>
            <div class="search-box">
                <input v-model="productKeyword" @keyup.enter="loadProducts" placeholder="Tìm sản phẩm..." class="form-control" style="width: 300px;" />
            </div>
        </div>

        <div class="table-responsive">
            <table class="custom-table">
                <thead>
                    <tr>
                        <th style="width: 50px;"><input type="checkbox" @change="toggleAll" :checked="isAllSelected" /></th>
                        <th>Hình ảnh</th>
                        <th>Tên sản phẩm</th>
                        <th>Phân loại</th>
                        <th>Giá gốc</th>
                        <th>Giá sau giảm</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="sp in products" :key="sp.id">
                        <td>
                            <input type="checkbox" :value="sp.id" v-model="form.idChiTietSanPhams" />
                        </td>
                        <td>
                            <img src="https://via.placeholder.com/40" class="thumb-img" />
                        </td>
                        <td>
                            <div class="prod-name">{{ sp.tenSanPham }}</div>
                            <div class="prod-code">{{ sp.maCtsp }}</div>
                        </td>
                        <td>
                            <span class="variant-tag">{{ sp.tenMauSac }} - {{ sp.tenKichThuoc }}</span>
                        </td>
                        <td>{{ formatCurrency(sp.giaBan) }}</td>
                        <td class="new-price">
                            {{ calculateNewPrice(sp.giaBan) }}
                        </td>
                    </tr>
                    <tr v-if="products.length === 0">
                         <td colspan="6" class="text-center">Đang tải hoặc không có sản phẩm...</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import request from '@/services/request';
import { useRouter } from 'vue-router';
import Swal from 'sweetalert2';

const router = useRouter();
const productKeyword = ref('');
const products = ref([]); // Danh sách biến thể sản phẩm

const form = ref({
    tenDotGiamGia: '',
    giaTriGiam: 0,
    loaiGiamGia: 'VND',
    ngayBatDau: '',
    ngayKetThuc: '',
    trangThai: 1,
    idChiTietSanPhams: [] // Array chứa ID các biến thể (ChiTietSanPham) được chọn
});

// Computed để hiển thị giá sau giảm demo
const calculateNewPrice = (oldPrice) => {
    let price = Number(oldPrice);
    let giam = Number(form.value.giaTriGiam);
    if(form.value.loaiGiamGia === 'VND') {
        return formatCurrency(Math.max(0, price - giam));
    } else {
        return formatCurrency(price * (100 - giam) / 100);
    }
}

// Logic load sản phẩm (Giả lập gọi API lấy danh sách biến thể)
// [Quan trọng] Backend cần có API trả về list ChiTietSanPham đầy đủ
// Ở đây mình gọi tạm API sản phẩm và map dữ liệu để bạn test giao diện
// Thay thế hoàn toàn hàm loadProducts cũ bằng hàm này:
const loadProducts = async () => {
    try {
        // 1. Gọi API lấy danh sách Chi Tiết Sản Phẩm (ID thật)
        // Lưu ý: Đảm bảo bạn đã có Controller cho /chi-tiet-san-pham như hướng dẫn trước
        const res = await request.get('/chi-tiet-san-pham', { params: { size: 100, keyword: productKeyword.value } });
        
        // 2. Map dữ liệu trả về từ DB vào biến products
        products.value = res.data.content.map(ctsp => ({
            id: ctsp.id, // <--- QUAN TRỌNG: Đây là ID thật từ Database
            maCtsp: ctsp.maCtsp || 'Chưa có mã',
            // Kiểm tra null an toàn cho các trường con
            tenSanPham: ctsp.sanPham ? ctsp.sanPham.tenSanPham : 'Sản phẩm', 
            tenMauSac: ctsp.mauSac ? ctsp.mauSac.tenMauSac : '',
            tenKichThuoc: ctsp.kichThuoc ? ctsp.kichThuoc.tenKichThuoc : '',
            giaBan: ctsp.giaBan
        }));

    } catch (e) { 
        console.error("Lỗi tải sản phẩm:", e); 
    }
};
const createSale = async () => {
    try {
        if(!form.value.tenDotGiamGia || form.value.tenDotGiamGia.trim() === '') {
            return Swal.fire({ icon: 'warning', title: 'Thiếu tên chương trình', text: 'Vui lòng nhập tên chương trình.' });
        }
        if (!form.value.giaTriGiam || Number(form.value.giaTriGiam) <= 0) {
            return Swal.fire({ icon: 'warning', title: 'Giá giảm không hợp lệ', text: 'Vui lòng nhập mức giảm lớn hơn 0.' });
        }
        if (form.value.loaiGiamGia === '%' && (Number(form.value.giaTriGiam) <=0 || Number(form.value.giaTriGiam) > 100)) {
            return Swal.fire({ icon: 'warning', title: 'Phần trăm không hợp lệ', text: 'Vui lòng nhập phần trăm trong khoảng 1 - 100.' });
        }
        if (!form.value.ngayBatDau || !form.value.ngayKetThuc) {
            return Swal.fire({ icon: 'warning', title: 'Thiếu thời gian', text: 'Vui lòng chọn thời gian bắt đầu và kết thúc.' });
        }
        if (new Date(form.value.ngayKetThuc) <= new Date(form.value.ngayBatDau)) {
            return Swal.fire({ icon: 'warning', title: 'Thời gian không hợp lệ', text: 'Thời gian kết thúc phải sau thời gian bắt đầu.' });
        }
        if(form.value.idChiTietSanPhams.length === 0) return Swal.fire({ icon: 'warning', title: 'Chưa chọn sản phẩm', text: 'Vui lòng chọn ít nhất 1 sản phẩm.' });

        await request.post('/dot-giam-gia', form.value);
        await Swal.fire({ icon: 'success', title: 'Thành công', text: 'Đã tạo đợt giảm giá.' });
        router.push('/admin/sales');
    } catch (e) {
        Swal.fire({ icon: 'error', title: 'Lỗi', text: e.response?.data?.message || 'Không thể tạo' });
    }
};

// Checkbox logic
const isAllSelected = computed(() => {
    return products.value.length > 0 && form.value.idChiTietSanPhams.length === products.value.length;
});

const toggleAll = (e) => {
    if (e.target.checked) {
        form.value.idChiTietSanPhams = products.value.map(p => p.id);
    } else {
        form.value.idChiTietSanPhams = [];
    }
};

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

onMounted(loadProducts);
</script>

<style scoped>
.page-container { padding: 20px; max-width: 1200px; margin: 0 auto; }
.card { background: white; padding: 24px; border-radius: 8px; margin-bottom: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.section-title { margin-bottom: 20px; font-size: 16px; font-weight: 700; color: #0f172a; border-bottom: 1px solid #f1f5f9; padding-bottom: 10px; }

.row-3 { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 6px; font-weight: 500; }
.form-control { width: 100%; padding: 10px; border: 1px solid #cbd5e1; border-radius: 6px; }
.input-group { display: flex; }
.input-group input { border-top-right-radius: 0; border-bottom-right-radius: 0; }
.unit-select { background: #f8fafc; border: 1px solid #cbd5e1; border-left: none; padding: 0 10px; border-top-right-radius: 6px; border-bottom-right-radius: 6px; }

.btn-primary { background: #0f172a; color: white; padding: 12px 24px; border: none; border-radius: 6px; cursor: pointer; font-weight: 500; }

/* Table Section */
.panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.custom-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.custom-table th { background: #E9F1FB; padding: 12px; text-align: left; color: #1E3A8A; font-weight: 700; }
.custom-table td { padding: 12px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.thumb-img { width: 40px; height: 40px; border-radius: 4px; object-fit: cover; border: 1px solid #e2e8f0; }
.prod-name { font-weight: 500; color: #0f172a; }
.prod-code { font-size: 12px; color: #64748b; }
.variant-tag { background: #f1f5f9; padding: 2px 8px; border-radius: 4px; font-size: 12px; color: #475569; }
.new-price { color: #dc2626; font-weight: 400; }
.text-center { text-align: center; color: #94a3b8; }
</style>