<template>
  <div class="page-container">
    <div class="header-section">
      <h1 class="page-title">Quản lý sản phẩm / Danh sách sản phẩm</h1>
<div class="total-count"><h5>Tổng số sản phẩm: <b>{{ items.length }}</b></h5></div>
    </div>

    <div class="card control-panel">
      <div class="filter-header">
        <i class="fas fa-filter"></i> Bộ lọc tìm kiếm
      </div>

      <div class="filter-body">
        <div class="row-top">
          <div class="input-group search-group">
            <label>Tìm kiếm</label>
            <div class="search-box">
              <i class="fas fa-search search-icon"></i>
              <input
                type="text"
                class="form-control"
                v-model="filter.keyword"
                placeholder="Tìm theo mã / tên sản phẩm..."
                @keyup.enter="fetchProducts"
              />
            </div>
          </div>

          <div class="input-group select-group">
            <label>Chất liệu</label>
            <select v-model="filter.chatLieuId" @change="fetchProducts" class="form-select">
              <option value="">Tất cả chất liệu</option>
              <option v-for="m in options.materials" :key="m.id" :value="m.id">
                {{ m.tenChatLieu }}
              </option>
            </select>
          </div>

          <div class="input-group select-group">
            <label>Thương hiệu</label>
            <select v-model="filter.thuongHieuId" @change="fetchProducts" class="form-select">
              <option value="">Tất cả thương hiệu</option>
              <option v-for="b in options.brands" :key="b.id" :value="b.id">
                {{ b.tenThuongHieu }}
              </option>
            </select>
          </div>
        </div>

        <div class="row-bottom">
          <div class="status-group">
            <label>Trạng thái</label>
            <div class="radio-list">
              <label class="radio-item">
                <input type="radio" :value="null" v-model="filter.status" @change="fetchProducts">
                <span>Tất cả</span>
              </label>
              <label class="radio-item">
                <input type="radio" :value="1" v-model="filter.status" @change="fetchProducts">
                <span>Kinh doanh</span>
              </label>
              <label class="radio-item">
                <input type="radio" :value="0" v-model="filter.status" @change="fetchProducts">
                <span>Ngừng kinh doanh</span>
              </label>
            </div>
          </div>

          <div class="action-buttons">
            <button class="btn btn-outline" @click="exportExcel">
              <i class="fas fa-file-excel btn-icon-gray"></i> Tải Excel
            </button>

            <button class="btn btn-gradient" @click="$router.push('/admin/products/create')">
              <i class="fas fa-plus"></i> Thêm chi tiết sản phẩm
            </button>

            <button class="btn btn-teal" @click="$router.push('/admin/products/variants')">
              <i class="fas fa-th-list"></i> Xem toàn bộ biến thể
            </button>

            

            <button class="btn btn-dark" @click="resetFilter">
              <i class="fas fa-sync-alt"></i> Đặt lại bộ lọc
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="card table-container">
      <table class="custom-table">
        <thead>
          <tr>
            <th width="5%" class="text-center">STT</th>
            <th width="10%">Mã sản phẩm</th>
            <th width="25%">Tên sản phẩm</th>
            <th width="12%">Thương hiệu</th>
            <th width="12%">Chất liệu</th>
            <th width="10%" class="text-center">Số lượng tồn</th>
            <th width="10%" class="text-center">Trạng thái</th>
            <th width="10%" class="text-center">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="text-center py-5">Đang tải dữ liệu...</td>
          </tr>
          <tr v-else-if="items.length === 0">
            <td colspan="8" class="text-center py-5 empty-state">Không tìm thấy sản phẩm nào.</td>
          </tr>

          <tr v-else v-for="(item, index) in items" :key="item.id">
            <td class="text-center">{{ (page - 1) * pageSize + index + 1 }}</td>
            <td class="font-bold">{{ item.maSanPham }}</td>
            <td class="text-primary font-bold">{{ item.tenSanPham }}</td>
            
            <td>{{ item.thuongHieu?.tenThuongHieu || item.tenThuongHieu || '-' }}</td>
            <td>{{ item.chatLieu?.tenChatLieu || item.tenChatLieu || '-' }}</td>
            
            <td class="text-center font-bold">{{ item.soLuong }}</td>

            <td class="text-center">
              <span class="badge" :class="item.trangThai === 1 ? 'badge-success' : 'badge-danger'">
                {{ item.trangThai === 1 ? 'Kinh doanh' : 'Ngừng KD' }}
              </span>
            </td>

            <td class="text-center">
              <div class="action-cell">
                <label 
                  class="switch-wrapper" 
                  title="Đổi trạng thái nhanh" 
                  @click.prevent="toggleStatus(item)"
                >
                  <input
                    type="checkbox"
                    :checked="item.trangThai === 1"
                    class="switch-input"
                    readonly
                  >
                  <span class="switch-slider"></span>
                </label>

                <button class="btn-icon-eye" @click="$router.push(`/admin/products/${item.id}`)" title="Xem chi tiết">
                  <i class="far fa-eye"></i>
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="pagination-footer">
        <div class="page-info">
          Hiển thị
          <select v-model="pageSize" @change="handlePageSizeChange">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
          </select>
          sản phẩm / trang 
        </div>
        <div class="page-controls">
          <button class="page-btn" :disabled="page === 1" @click="changePage(page - 1)">‹</button>
          <button
            v-for="p in visiblePages"
            :key="p"
            :class="['page-btn', { active: p === page }]"
            @click="changePage(p)"
          >
            {{ p }}
          </button>
          <button class="page-btn" :disabled="page === totalPages" @click="changePage(page + 1)">›</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import request from '@/services/request';
import Swal from 'sweetalert2';

// CẤU HÌNH TOAST (Góc phải trên cùng)
const Toast = Swal.mixin({
  toast: true,
  position: 'top-end',
  showConfirmButton: false,
  timer: 3000,
  timerProgressBar: true,
  didOpen: (toast) => {
    toast.addEventListener('mouseenter', Swal.stopTimer)
    toast.addEventListener('mouseleave', Swal.resumeTimer)
  }
});

const items = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const totalPages = ref(1);

const options = reactive({ brands: [], materials: [] });
const filter = reactive({ keyword: '', status: null, thuongHieuId: '', chatLieuId: '' });

// 1. Load Options (Thương hiệu, Chất liệu)
const fetchFilterOptions = async () => {
  try {
    // Đảm bảo endpoint API đúng với backend của bạn
    const [resBrand, resMat] = await Promise.all([
      request.get('/thuong-hieu?status=1&size=100'),
      request.get('/chat-lieu?status=1&size=100')
    ]);
    options.brands = resBrand.data.content || resBrand.data || [];
    options.materials = resMat.data.content || resMat.data || [];
  } catch (e) {
    console.error("Lỗi tải bộ lọc:", e);
  }
}

// 2. Load Products
const fetchProducts = async () => {
  loading.value = true;
  try {
    const params = {
      page: page.value - 1,
      size: pageSize.value,
      keyword: filter.keyword,
      status: filter.status,
      idThuongHieu: filter.thuongHieuId || null,
      idChatLieu: filter.chatLieuId || null
    };
    const res = await request.get('/products', { params });
    items.value = res.data.content || [];
    totalPages.value = res.data.totalPages || 1;
  } catch (e) {
    console.error(e);
    items.value = [];
  } finally {
    loading.value = false;
  }
};

// 3. LOGIC BẬT/TẮT TRẠNG THÁI (Đã sửa & test kỹ)
// LOGIC BẬT/TẮT TRẠNG THÁI (Đã sửa lỗi 404)
const toggleStatus = async (item) => {
    const originalStatus = item.trangThai;
    const newStatus = originalStatus === 1 ? 0 : 1;
    
    // 1. Cập nhật UI ngay lập tức (Optimistic Update)
    item.trangThai = newStatus;

    try {
        // --- SỬA LỖI TẠI ĐÂY ---
        // Thay vì gọi /status (bị 404), ta gọi vào đường dẫn gốc /products/{id}
        // Và gửi toàn bộ object item (đã bao gồm trạng thái mới) lên server
        await request.put(`/products/${item.id}`, item);

        // Thông báo thành công
        Toast.fire({
            icon: 'success',
            title: `Đã chuyển sang ${newStatus === 1 ? 'Đang bán' : 'Ngừng bán'}`
        });

    } catch (e) {
        // 2. Nếu lỗi, hoàn tác lại trạng thái cũ trên giao diện
        item.trangThai = originalStatus;
        
        console.error("Lỗi cập nhật trạng thái:", e);
        Toast.fire({
            icon: 'error',
            title: 'Lỗi: ' + (e.response?.data?.message || 'Không thể cập nhật trạng thái')
        });
    }
}

const resetFilter = () => {
  filter.keyword = '';
  filter.status = null;
  filter.thuongHieuId = '';
  filter.chatLieuId = '';
  page.value = 1;
  fetchProducts();
};

const openQrScanner = () => { Toast.fire({ icon: 'info', title: 'Chức năng đang phát triển' }); };

const exportExcel = async () => {
  const confirmRes = await Swal.fire({ title: 'Xác nhận', text: 'Tải xuống Excel?', icon: 'question', showCancelButton: true });
  if (!confirmRes.isConfirmed) return;
  try {
    const response = await request.get('/products/export', { responseType: 'blob' });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a'); link.href = url;
    link.setAttribute('download', `DS_SanPham_${new Date().toISOString().slice(0, 10)}.xlsx`);
    document.body.appendChild(link); link.click();
    window.URL.revokeObjectURL(url);
    Toast.fire({ icon: 'success', title: 'Xuất file thành công' });
  } catch (e) { Toast.fire({ icon: 'error', title: 'Không thể xuất file' }); }
};

const changePage = (p) => { if (p >= 1 && p <= totalPages.value) { page.value = p; fetchProducts(); } };
const handlePageSizeChange = () => { page.value = 1; fetchProducts(); };
const visiblePages = computed(() => {
  let p = [];
  for (let i = 1; i <= totalPages.value; i++) {
    if (i === 1 || i === totalPages.value || (i >= page.value - 1 && i <= page.value + 1)) p.push(i);
  }
  return p;
});

onMounted(() => {
  fetchFilterOptions();
  fetchProducts();
});
</script>

<style scoped>
.page-container { 
    padding: 20px; 
    font-family: 'Segoe UI', sans-serif; 
    /* ĐỔI THÀNH MÀU XÁM TRUNG TÍNH (Không ám xanh) */
    background-color: #ebecee;
    min-height: 100vh; 
    color: #333; 
    font-size: 14px; 
}
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-title { margin: 0; font-size: 24px; font-weight: 700; color: #1e293b; }
.total-count { font-size: 13px; color: black; }

.card { background: white; border-radius: 12px; border: 1px solid #bfdbfe; box-shadow: 0 4px 12px rgba(0,0,0,0.05); padding: 24px; margin-bottom: 20px; }

/* Filter Styles */
.filter-header { font-size: 15px; font-weight: 700; color: #334155; margin-bottom: 15px; display: flex; align-items: center; gap: 8px; }
.filter-body { display: flex; flex-direction: column; gap: 20px; }
.row-top { display: flex; gap: 20px; align-items: flex-end; }
.input-group { display: flex; flex-direction: column; gap: 6px; }
.input-group label { font-size: 13px; font-weight: 600; color: #64748b; }
.search-group { flex: 2; }
.select-group { flex: 1; }
.search-box { position: relative; width: 100%; }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; }
.form-control { width: 100%; padding: 10px 10px 10px 36px; border: 1px solid #e2e8f0; border-radius: 8px; outline: none; transition: 0.2s; height: 42px; font-size: 14px; }
.form-select { width: 100%; padding: 0 10px; border: 1px solid #e2e8f0; border-radius: 8px; outline: none; height: 42px; font-size: 14px; cursor: pointer; background-color: #fff; }
.form-control:focus, .form-select:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }

/* Row 2 */
.row-bottom { display: flex; justify-content: space-between; align-items: center; padding-top: 5px; }
.status-group { display: flex; flex-direction: column; gap: 6px; }
.status-group label { font-size: 13px; font-weight: 600; color: #64748b; }
.radio-list { display: flex; gap: 15px; align-items: center; }
.radio-item { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 14px; color: #334155; }
.radio-item input { width: 16px; height: 16px; accent-color: #ef4444; cursor: pointer; }

/* Buttons */
.action-buttons { display: flex; gap: 10px; }
.btn { height: 40px; padding: 0 18px; border-radius: 8px; font-weight: 600; cursor: pointer; border: 1px solid transparent; font-size: 13px; display: inline-flex; align-items: center; gap: 8px; transition: 0.2s; }
.btn-outline { background: #fff; border-color: #e2e8f0; color: #334155; }
.btn-outline:hover { background: #f8fafc; border-color: #cbd5e1; }

/* Gradient Xanh Đậm Cũ (Khôi phục) */
.btn-gradient { 
    background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
    color: white; 
    box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); 
}
.btn-gradient:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.3); }

.btn-orange { background: #161af9; color: white; }
.btn-orange:hover { background: #220cea; }
.btn-teal { background: linear-gradient(135deg, #0d9488 0%, #065f46 100%); color: white; box-shadow: 0 4px 10px rgba(6,95,70,0.2); }
.btn-teal:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(6,95,70,0.3); }
.btn-dark { background: #334155; color: white; }
.btn-dark:hover { background: #1e293b; }
.btn-icon-gray { color: #64748b !important; margin-right: 6px; font-size: 16px; }

/* Table Header (Màu Xanh Nhạt Cũ) */
.table-container { padding: 0; overflow: hidden; }
.custom-table { width: 100%; border-collapse: collapse; }
.custom-table th { 
    background: #f5f5f5; 
    color: #000000;
    padding: 14px; 
    font-weight: 700; 
    text-transform: uppercase; 
    font-size: 12px; 
    border-bottom: none; 
    text-align: center;
}
.custom-table td { padding: 14px; border-bottom: 1px solid #f1f5f9; font-size: 14px; vertical-align: middle; color: #334155; text-align: center; }

.font-bold { font-weight: 600; }
.text-primary { color: #3b82f6; }

.badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; border: 1px solid transparent; }
.badge-success { background: #dbeafe; color: #201dd8; border-color: #93c5fd; }
.badge-danger { background: #fee2e2; color: #991b1b; border-color: #fecaca; }

.action-cell { display: flex; align-items: center; justify-content: center; gap: 10px; }
.btn-icon-eye { width: 32px; height: 32px; border-radius: 6px; background: #fff; border: 1px solid #e2e8f0; color: #64748b; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: 0.2s; }
.btn-icon-eye:hover { border-color: #3b82f6; color: #3b82f6; background: #eff6ff; }

/* Switch Toggle (Màu Xanh Lá + Con trỏ) */
.switch-wrapper { 
    position: relative; 
    display: inline-block; 
    width: 36px; 
    height: 20px; 
    cursor: pointer; /* Quan trọng: hiển thị bàn tay khi hover */
}
.switch-input { opacity: 0; width: 0; height: 0; }
.switch-slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #ccc; transition: .4s; border-radius: 34px; }
.switch-slider:before { position: absolute; content: ""; height: 14px; width: 14px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; }

/* Màu xanh lá khi Checked */
.switch-input:checked + .switch-slider { background-color: #10b981; } 
.switch-input:checked + .switch-slider:before { transform: translateX(16px); }

.pagination-footer { display: flex; justify-content: space-between; align-items: center; padding: 15px 24px; border-top: 1px solid #f1f5f9; }
.page-controls button { width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 6px; margin-left: 5px; cursor: pointer; }
.page-controls button.active { background: #0f172a; color: #fff; border-color: #0f172a; }
</style>