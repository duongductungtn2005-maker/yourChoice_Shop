<template>
  <div class="page-container">
    <h1 class="page-title">Quản lý sản phẩm / Tay áo</h1>

    <div class="control-panel">
      <div class="controls-row">
        <div class="filter-group">
          <div class="search-box">
            <i class="fas fa-magnifying-glass search-icon"></i>
            <input 
            class="input-den"
              type="text" 
              v-model="filter.keyword" 
              placeholder="Tìm kiếm tay áo..." 
              @keyup.enter="fetchData"
            >
          </div>
        </div>

        <div class="action-group">
          <button class="btn btn-navy" @click="resetFilter">
            <font-awesome-icon :icon="['fas', 'sync-alt']" /> Đặt lại
          </button>
          <button class="btn btn-outline" @click="exportExcel">
            <font-awesome-icon :icon="['fas', 'file-excel']" /> Xuất Excel
          </button>
          <button class="btn btn-gradient" @click="openModal()">
            <i class="fas fa-plus"></i> Tạo mới
          </button>
        </div>
      </div>
    </div>

    <div class="table-container">
      <table class="custom-table">
        <thead>
          <tr>
            <th class="text-center">STT</th>
            <th>Tên Tay Áo</th>
            <th class="text-center">Trạng thái</th>
            <th class="text-center">Ngày tạo</th>
            <th class="text-center">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="5" class="text-center empty-state">Đang tải dữ liệu...</td>
          </tr>
          <tr v-else-if="items.length === 0">
            <td colspan="5" class="text-center empty-state">Không có dữ liệu</td>
          </tr>

          <tr v-else v-for="(item, index) in items" :key="item.id">
            <td class="text-center">{{ (page - 1) * pageSize + index + 1 }}</td>
            
            <td class="code-text">
              {{ item.tenTayAo }}
            </td>
            
            <td class="text-center">
              <span class="badge" :class="item.trangThai === 1 ? 'badge-active' : 'badge-stopped'">
                {{ item.trangThai === 1 ? 'Hoạt động' : 'Ngừng' }}
              </span>
            </td>
            
            <td class="text-center time-col">{{ formatDate(item.ngayTao) }}</td>
            
            <td class="text-center action-col">
              <div class="action-wrapper">
                <button class="icon-btn" @click="openModal(item)" title="Sửa">
                  <i class="far fa-eye"></i>
                </button>
                
                <label class="switch" title="Bật/Tắt trạng thái">
                  <input 
                    type="checkbox" 
                    :checked="item.trangThai === 1" 
                    @click="handleToggleStatus(item, $event)"
                  >
                  <span class="slider round"></span>
                </label>
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
          </select> 
          kết quả / trang
        </div>
        <div class="page-controls">
          <button :disabled="page === 1" @click="changePage(page - 1)">‹</button>
          <button 
            v-for="p in visiblePages" 
            :key="p" 
            :class="{ active: p === page }" 
            @click="changePage(p)"
          >
            {{ p }}
          </button>
          <button :disabled="page === totalPages" @click="changePage(page + 1)">›</button>
        </div>
      </div>
    </div>

    <div class="modal-overlay" v-if="showModal" @click.self="showModal = false">
      <div class="modal-content" style="width: 450px;">
        <div class="modal-header">
          <h3 style="margin:0">{{ isEdit ? 'Cập nhật tay áo' : 'Thêm mới tay áo' }}</h3>
          <button @click="showModal = false" class="close-btn"><i class="fas fa-times"></i></button>
        </div>
        
        <div class="modal-body">
          <form @submit.prevent="saveData">
            <div class="form-group text-left">
              <label class="form-label">Tên tay áo <span class="required">*</span></label>
              <input 
                type="text" 
                v-model="form.ten" 
                class="form-control-custom" 
                :class="{ 'is-invalid': errors.ten }"
                placeholder="Nhập tên tay áo (VD: Tay ngắn...)" 
                ref="nameInput"
                @blur="validateField('ten')" 
                @input="validateField('ten')"
              >
              <span v-if="errors.ten" class="error-msg">{{ errors.ten }}</span>
            </div>

            <div class="form-group text-left" v-if="isEdit" style="margin-top: 15px;">
              <label class="form-label">Trạng thái:</label>
              <div class="radio-group" style="display: flex; gap: 20px;">
                <label><input type="radio" :value="1" v-model="form.trangThai"> Hoạt động</label>
                <label><input type="radio" :value="0" v-model="form.trangThai"> Ngừng</label>
              </div>
            </div>
          </form>

          <div class="modal-actions" style="margin-top: 25px; justify-content: flex-end; gap: 10px; display: flex;">
            <button class="btn btn-outline" @click="showModal = false">Đóng</button>
            <button class="btn btn-gradient" @click="saveData">
              {{ isEdit ? 'Lưu thay đổi' : 'Thêm mới' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue';
import axios from 'axios';
import Swal from 'sweetalert2';
import { validate } from '@/utils/validate'; 
import { toastSuccess, toastError } from '@/utils/toast'; 

// --- STATE ---
const items = ref([]);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(5);
const totalPages = ref(1);
const filter = reactive({ keyword: '' });

const showModal = ref(false);
const isEdit = ref(false);
const currentId = ref(null);
const form = reactive({ ten: '', trangThai: 1 });
const errors = reactive({ ten: null }); 
const nameInput = ref(null);

const API_URL = 'http://localhost:8080/api/v1/tay-ao';

// --- VALIDATE LOGIC ---
const validateField = (field) => {
    if (field === 'ten') {
        errors.ten = validate.isRequired(form.ten) || validate.minLength(form.ten, 2);
    }
};

const isFormValid = () => {
    validateField('ten');
    return !errors.ten;
};

// --- API FETCH ---
const fetchData = async () => {
    loading.value = true;
    try {
        const res = await axios.get(API_URL, {
            params: { page: page.value - 1, size: pageSize.value, keyword: filter.keyword }
        });
        items.value = res.data.content;
        totalPages.value = res.data.totalPages;
    } catch (e) { console.error(e); } finally { loading.value = false; }
};

// --- EXPORT EXCEL ---
const exportExcel = async () => {
    const confirmRes = await Swal.fire({
        title: 'Xác nhận', text: 'Bạn có muốn tải xuống danh sách tay áo không?', icon: 'question',
        showCancelButton: true, confirmButtonText: 'Có', cancelButtonText: 'Hủy'
    });
    if (!confirmRes.isConfirmed) return;

    try {
        const response = await axios.get(`${API_URL}/export`, { responseType: 'blob' });
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a'); link.href = url;
        const dateStr = new Date().toISOString().slice(0,10);
        link.setAttribute('download', `DS_TayAo_${dateStr}.xlsx`);
        document.body.appendChild(link); link.click(); document.body.removeChild(link); window.URL.revokeObjectURL(url);
        
        const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 2000 });
        Toast.fire({ icon: 'success', title: 'Xuất Excel thành công' });
    } catch (e) {
        console.error("Lỗi xuất Excel:", e);
        Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Không thể xuất file Excel. Vui lòng thử lại sau.' });
    }
};

// --- MODAL HANDLERS ---
const openModal = (item = null) => {
    errors.ten = null; 
    if (item) {
        isEdit.value = true; currentId.value = item.id;
        form.ten = item.tenTayAo; 
        form.trangThai = item.trangThai;
    } else {
        isEdit.value = false; currentId.value = null;
        form.ten = ''; form.trangThai = 1;
    }
    showModal.value = true;
    nextTick(() => nameInput.value?.focus());
};

const saveData = async () => {
    if (!isFormValid()) return;
    try {
        const checkRes = await axios.get(API_URL, { params: { keyword: form.ten.trim(), page: 0, size: 100 } });
        const listCheck = checkRes.data.content || [];
        const isDuplicate = listCheck.some(item => 
            item.tenTayAo.toLowerCase() === form.ten.trim().toLowerCase() && item.id !== currentId.value
        );
        if (isDuplicate) { errors.ten = `Tên "${form.ten}" đã tồn tại!`; return; }

        const result = await Swal.fire({
            title: `Xác nhận ${isEdit.value ? 'cập nhật' : 'thêm mới'}?`,
            icon: 'question', showCancelButton: true, confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy'
        });

        if (result.isConfirmed) {
            const payload = { ten: form.ten, trangThai: form.trangThai };
            if (isEdit.value) await axios.put(`${API_URL}/${currentId.value}`, payload);
            else await axios.post(API_URL, payload);
            showModal.value = false; 
            fetchData();
            toastSuccess(isEdit.value ? 'Cập nhật thành công!' : 'Thêm mới thành công!');
        }
    } catch (e) { toastError(e.response?.data?.message || 'Có lỗi xảy ra!'); }
};

const handleToggleStatus = async (item, event) => {
    event.preventDefault();
    const newStatus = item.trangThai === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? 'Kích hoạt' : 'Ngừng hoạt động';
    const result = await Swal.fire({
        title: `Xác nhận ${actionText}?`,
        text: `Bạn có chắc muốn ${actionText.toLowerCase()} "${item.tenTayAo}"?`,
        icon: 'question', showCancelButton: true, confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        try {
            if (newStatus === 0) await axios.delete(`${API_URL}/${item.id}`);
            else await axios.put(`${API_URL}/${item.id}`, { ten: item.tenTayAo, trangThai: 1 });
            item.trangThai = newStatus;
            toastSuccess(`Đã ${actionText.toLowerCase()} thành công!`);
        } catch (e) { toastError('Không thể thay đổi trạng thái.'); }
    }
};
const resetFilter = () => {
    filter.keyword = '';
    page.value = 1;
    fetchData();
};
// --- UTILS ---
const changePage = (p) => { if (p >= 1 && p <= totalPages.value) { page.value = p; fetchData(); } };
const handlePageSizeChange = () => { page.value = 1; fetchData(); };
const visiblePages = computed(() => {
    let p = []; for (let i = 1; i <= totalPages.value; i++) if (i===1 || i===totalPages.value || (i>=page.value-1 && i<=page.value+1)) p.push(i); return p;
});
const formatDate = (d) => d ? new Date(d).toLocaleDateString('vi-VN') : '';

onMounted(() => { fetchData(); });
</script>

<style scoped>
/* === CSS CHUẨN ĐỒNG BỘ === */
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background: #f8f9fa; min-height: 100vh; color: #333; font-size: 14px; }
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }

/* CARD STYLING */
.control-panel, .table-container { 
    background: white; border-radius: 16px; border: 1px solid #bfdbfe !important; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); margin-bottom: 20px; padding: 24px; 
}
.table-container { padding: 0; overflow: hidden; }

/* FLEX LAYOUT */
.controls-row { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.filter-group { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.action-group { display: flex; gap: 10px; }

/* INPUTS & SEARCH */
.search-box { position: relative; width: 250px; }
.search-icon { position: absolute; left: 12px; top: 11px; color: #94a3b8; }
.search-box input { 
    width: 100%; 
    padding: 8px 10px 8px 36px; 
    border: 1px solid #e2e8f0; 
    border-radius: 6px; 
    outline: none; 
    height: 40px; 
    /* ĐÃ CHỈNH SỬA: Thêm font-weight để chữ đậm hơn */
    font-weight: 700;
}
.search-box input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.btn-navy {
    background-color: #0f172a; /* Xanh than đậm */
    color: #ffffff;
    box-shadow: 0 4px 6px rgba(15, 23, 42, 0.2);
}
.btn-navy:hover {
    background-color: #1e293b;
    transform: translateY(-1px);
}
/* BUTTONS */
.btn { 
    height: 40px; padding: 0 20px; border-radius: 6px; font-weight: 600; cursor: pointer; 
    font-size: 13px; border: 1px solid transparent; transition: 0.2s; display: inline-flex; 
    align-items: center; gap: 8px; text-decoration: none;
}
.btn-outline { background: #fff; border: 1px solid #e2e8f0; color: #475569; }
.btn-outline:hover { background: #f8fafc; border-color: #cbd5e1; }
.btn-gradient { 
    background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; 
    box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); 
}
.btn-gradient:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.3); }

/* TABLE STYLES */
.custom-table { width: 100%; border-collapse: collapse; }
.custom-table th {
    background: #eff6ff !important; color: #1e40af; padding: 16px; text-align: center;
     font-weight: 700; text-transform: uppercase; border-bottom: none !important;
}
.custom-table td { padding: 14px 16px; border-bottom: 1px solid #f1f5f9; text-align: center; vertical-align: middle; }

/* BADGES */
.badge { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; white-space: nowrap; }
.badge-active { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.badge-stopped { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
.badge-expired { background: #f1f5f9; color: #94a3b8; border: 1px solid #e2e8f0; }

/* ACTIONS */
.action-wrapper { display: flex; align-items: center; justify-content: center; gap: 10px; }
.icon-btn { 
    width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; 
    background: white; border: 1px solid #e2e8f0; border-radius: 6px; cursor: pointer; color: #64748b; 
}
.icon-btn:hover { background: #f1f5f9; color: #0f172a; border-color: #cbd5e1; }

/* TOGGLE SWITCH */
.switch { position: relative; display: inline-block; width: 36px; height: 20px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #cbd5e1; transition: .4s; border-radius: 34px; }
.slider:before { position: absolute; content: ""; height: 14px; width: 14px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; }
input:checked + .slider { background-color: #10b981; }
input:checked + .slider:before { transform: translateX(16px); }

/* PAGINATION */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; padding: 15px 24px; border-top: 1px solid #f1f5f9; }
.page-info select { border: 1px solid #e2e8f0; border-radius: 4px; padding: 2px 5px; margin: 0 5px; }
.page-controls button { width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; margin-left: 5px; cursor: pointer; }
.page-controls button.active { background: #0f172a; color: #fff; border-color: #0f172a; }

/* MODAL & OTHERS */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: white; padding: 24px; border-radius: 12px; }
.modal-header { display: flex; justify-content: space-between; margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 10px;}
.close-btn { border: none; background: none; font-size: 18px; cursor: pointer; color: #64748b; }
.empty-state { padding: 40px; color: #64748b; font-style: italic; }
.form-control-custom { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; margin-top: 5px; outline: none; }
.form-control-custom:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.error-msg { color: #ef4444; font-size: 12px; margin-top: 5px; display: block; }
.is-invalid { border-color: #ef4444; }
/* Màu chữ placeholder đen xì, rõ nét */
.input-den::placeholder {
    color: #000000 !important;  /* Màu đen */
    opacity: 1 !important;      /* Chống mờ */
    font-weight: 500;           /* Đậm lên tí cho dễ đọc (tùy chọn) */
}
</style>