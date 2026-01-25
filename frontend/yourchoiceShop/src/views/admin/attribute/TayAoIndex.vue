<template>
  <div class="attribute-page">
    <div class="header-section">
      <div class="breadcrumb">
        <span>Quản lý sản phẩm</span> 
        <span class="divider">/</span> 
        <span class="active">Tay áo</span>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
         <div class="toolbar">
            <div class="search-wrap">
               <span class="search-icon">🔍</span>
               <input 
                 type="text" 
                 v-model="filter.keyword" 
                 placeholder="Tìm kiếm tay áo..." 
                 @keyup.enter="fetchData"
               >
            </div>
            <div class="action-group">
               <button class="btn btn-outline" @click="exportExcel">📥 Xuất Excel</button>
               <button class="btn btn-primary" @click="openModal()">+ Tạo mới</button>
            </div>
         </div>
      </div>

      <div class="table-responsive">
        <table>
          <thead>
            <tr>
              <th class="text-center" width="10%">STT</th>
              <th width="40%">Tên Tay Áo</th>
              <th class="text-center" width="20%">Trạng thái</th>
              <th class="text-center" width="20%">Ngày tạo</th>
              <th class="text-center" width="10%">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading"><td colspan="5" class="text-center py-4">Đang tải dữ liệu...</td></tr>
            <tr v-else-if="items.length === 0"><td colspan="5" class="text-center py-4">Không có dữ liệu.</td></tr>
            <tr v-else v-for="(item, index) in items" :key="item.id">
              <td class="text-center">{{ (page - 1) * pageSize + index + 1 }}</td>
              <td class="font-medium text-primary">{{ item.tenTayAo }}</td>
              <td class="text-center">
                 <span :class="['badge', item.trangThai === 1 ? 'badge-success' : 'badge-danger']">
                    {{ item.trangThai === 1 ? 'Hoạt động' : 'Ngừng' }}
                 </span>
              </td>
              <td class="text-center text-gray">{{ formatDate(item.ngayTao) }}</td>
              <td class="text-center">
                <button class="btn-icon" @click="openModal(item)" title="Sửa">
                    <font-awesome-icon :icon="['far', 'pen-to-square']" />
                </button>
                <button class="btn-icon delete" @click="confirmChangeStatus(item)" title="Đổi trạng thái">
                    <font-awesome-icon :icon="['far', 'trash-can']" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-footer">
         <div class="page-info">
            Hiển thị <select v-model="pageSize" @change="handlePageSizeChange"><option :value="5">5</option><option :value="10">10</option></select> kết quả
         </div>
         <div class="page-controls">
            <button :disabled="page === 1" @click="changePage(page - 1)">‹</button>
            <button v-for="p in visiblePages" :key="p" :class="{ active: p === page }" @click="changePage(p)">{{ p }}</button>
            <button :disabled="page === totalPages" @click="changePage(page + 1)">›</button>
         </div>
      </div>
    </div>

    <div class="modal-backdrop" v-if="showModal">
       <div class="modal-dialog-custom">
          <div class="modal-content-custom">
             <div class="modal-header-custom">
                <h3>{{ isEdit ? 'Cập nhật tay áo' : 'Thêm mới tay áo' }}</h3>
             </div>
             <div class="modal-body-custom">
                <form @submit.prevent="saveData">
                   <div class="form-group">
                      <input 
                        type="text" 
                        v-model="form.ten" 
                        class="form-control-custom" 
                        placeholder="Nhập tên tay áo (VD: Tay ngắn, Tay dài...)" 
                        ref="nameInput"
                      >
                   </div>
                   <div class="form-group" v-if="isEdit" style="margin-top: 15px;">
                      <label style="font-size: 13px; font-weight: 600;">Trạng thái:</label>
                      <div class="radio-group">
                         <label class="radio-item"><input type="radio" :value="1" v-model="form.trangThai"> Hoạt động</label>
                         <label class="radio-item"><input type="radio" :value="0" v-model="form.trangThai"> Ngừng</label>
                      </div>
                   </div>
                </form>
             </div>
             <div class="modal-footer-custom">
                <button type="button" class="btn-custom btn-dark-blue" @click="showModal = false">Đóng</button>
                <button type="button" class="btn-custom btn-white-outline" @click="saveData">
                    {{ isEdit ? 'Lưu' : 'Thêm' }}
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
const nameInput = ref(null);

// ĐỔI API URL SANG TAY ÁO
const API_URL = 'http://localhost:8080/api/v1/tay-ao';

// --- FETCH DATA ---
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

// --- CHỨC NĂNG XUẤT EXCEL (TƯƠNG TỰ CỔ ÁO) ---
const exportExcel = async () => {
    try {
        const response = await axios.get(`${API_URL}/export`, {
            responseType: 'blob' 
        });

        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        
        // Đặt tên file DS_TayAo_...
        const dateStr = new Date().toISOString().slice(0,10);
        link.setAttribute('download', `DS_TayAo_${dateStr}.xlsx`);
        
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        
        const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 2000 });
        Toast.fire({ icon: 'success', title: 'Đã tải xuống file Excel!' });

    } catch (e) {
        console.error("Lỗi xuất Excel:", e);
        Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Không thể xuất file Excel.' });
    }
};

const openModal = (item = null) => {
    if (item) {
        isEdit.value = true; currentId.value = item.id;
        form.ten = item.tenTayAo; // Đổi thành tenTayAo
        form.trangThai = item.trangThai;
    } else {
        isEdit.value = false; currentId.value = null;
        form.ten = ''; form.trangThai = 1;
    }
    showModal.value = true;
    nextTick(() => nameInput.value?.focus());
};

const saveData = async () => {
    if (!form.ten.trim()) {
        const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 3000 });
        Toast.fire({ icon: 'warning', title: 'Vui lòng nhập tên tay áo!' });
        return;
    }

    try {
        const checkRes = await axios.get(API_URL, {
            params: { keyword: form.ten.trim(), page: 0, size: 100 }
        });
        const listCheck = checkRes.data.content || [];
        
        // Check trùng với tenTayAo
        const isDuplicate = listCheck.some(item => 
            item.tenTayAo.toLowerCase() === form.ten.trim().toLowerCase() && 
            item.id !== currentId.value
        );

        if (isDuplicate) {
            Swal.fire({
                icon: 'error', title: 'Tên đã tồn tại!', 
                text: `Tay áo "${form.ten}" đã có trong hệ thống.`,
                confirmButtonColor: '#ef4444'
            });
            return;
        }
    } catch (e) { console.error(e); }

    const result = await Swal.fire({
        title: `<h3 style="color:#1e293b; margin:0; font-size:20px;">Xác nhận ${isEdit.value ? 'Cập nhật' : 'Thêm mới'}?</h3>`,
        icon: 'warning', iconColor: '#facc15', showCancelButton: true, confirmButtonText: 'Có', cancelButtonText: 'Không',
        customClass: { confirmButton: 'swal-btn-outline', cancelButton: 'swal-btn-solid', popup: 'swal-rounded' }, 
        buttonsStyling: false, reverseButtons: true
    });

    if (result.isConfirmed) {
        try {
            const payload = { ten: form.ten, trangThai: form.trangThai };
            
            if (isEdit.value) {
                await axios.put(`${API_URL}/${currentId.value}`, payload);
            } else {
                await axios.post(API_URL, payload);
            }
            showModal.value = false; fetchData();
            Swal.fire({ icon: 'success', title: 'Thành công', showConfirmButton: false, timer: 1500, customClass: { popup: 'swal-rounded' } });
        } catch (e) {
            Swal.fire({ icon: 'error', title: 'Thất bại', text: e.response?.data?.message || 'Lỗi hệ thống', customClass: { popup: 'swal-rounded' } });
        }
    }
};

const confirmChangeStatus = async (item) => {
    const result = await Swal.fire({
        title: `<h3 style="color:#1e293b; font-size:18px;">Ngừng hoạt động?</h3>`,
        icon: 'warning', iconColor: '#ef4444', showCancelButton: true, confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy',
        customClass: { confirmButton: 'swal-btn-danger', cancelButton: 'swal-btn-solid' }, buttonsStyling: false
    });

    if (result.isConfirmed) {
        try {
            await axios.delete(`${API_URL}/${item.id}`);
            fetchData();
            Swal.fire({ icon: 'success', title: 'Đã chuyển trạng thái', timer: 1500, showConfirmButton: false });
        } catch (e) { Swal.fire('Lỗi', '', 'error'); }
    }
}

// --- UTILS ---
const changePage = (p) => { if (p >= 1 && p <= totalPages.value) { page.value = p; fetchData(); } };
const handlePageSizeChange = () => { page.value = 1; fetchData(); };
const visiblePages = computed(() => {
    let p = []; for (let i = 1; i <= totalPages.value; i++) if (i===1 || i===totalPages.value || (i>=page.value-1 && i<=page.value+1)) p.push(i); return p;
});
const formatDate = (d) => d ? new Date(d).toLocaleDateString('vi-VN', {day:'2-digit',month:'2-digit',year:'numeric'}) : '';

onMounted(() => { fetchData(); });
</script>

<style>
/* CSS SWEETALERT */
.swal-rounded { border-radius: 12px !important; }
.swal-btn-outline { background-color: #fff !important; color: #1e293b !important; border: 1px solid #1e293b !important; padding: 8px 24px; border-radius: 6px; font-weight: 600; cursor: pointer; margin-left: 10px; }
.swal-btn-outline:hover { background-color: #f1f5f9 !important; }
.swal-btn-solid { background-color: #1e293b !important; color: #fff !important; border: none !important; padding: 8px 24px; border-radius: 6px; font-weight: 600; cursor: pointer; }
.swal-btn-solid:hover { background-color: #334155 !important; }
.swal-btn-danger { background-color: #ef4444 !important; color: #fff !important; padding: 8px 24px; border-radius: 6px; font-weight: 600; cursor: pointer; margin-left: 10px; }
</style>

<style scoped>
.attribute-page { font-family: 'Segoe UI', sans-serif; color: #333; background-color: #f8fafc; min-height: 100vh; padding: 20px; }
.header-section { margin-bottom: 20px; }
.breadcrumb { font-size: 14px; color: #64748b; } .breadcrumb .active { font-weight: 600; color: #0f172a; }
.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 20px; }
.toolbar { display: flex; justify-content: space-between; margin-bottom: 20px; }
.search-wrap { position: relative; width: 300px; }
.search-wrap input { width: 100%; padding: 8px 12px 8px 36px; border: 1px solid #e2e8f0; border-radius: 4px; outline: none; }
.search-wrap .search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: #94a3b8; }
.btn { padding: 8px 16px; border-radius: 4px; font-weight: 600; cursor: pointer; border: 1px solid transparent; }
.btn-primary { background: #0f172a; color: #fff; }
.btn-outline { background: #fff; border-color: #cbd5e1; color: #475569; }
.action-group { display: flex; gap: 10px; }
.table-responsive { overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 4px; }
table { width: 100%; border-collapse: collapse; }
th { background: #f8fafc; padding: 12px; font-weight: 600; color: #475569; border-bottom: 1px solid #e2e8f0; font-size: 13px; text-transform: uppercase; }
td { padding: 12px; border-bottom: 1px solid #f1f5f9; font-size: 14px; vertical-align: middle; }
.text-center { text-align: center; } .font-medium { font-weight: 500; } .text-primary { color: #0f172a; } .text-gray { color: #64748b; }
.badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.badge-success { background: #dcfce7; color: #166534; } .badge-danger { background: #fee2e2; color: #991b1b; }
.pagination-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding-top: 15px; border-top: 1px solid #f1f5f9; }
.page-controls button { width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; margin-left: 5px; cursor: pointer; }
.page-controls button.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.modal-backdrop { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); z-index: 1050; display: flex; justify-content: center; align-items: flex-start; padding-top: 100px; }
.modal-dialog-custom { width: 450px; background: transparent; }
.modal-content-custom { background: #fff; border-radius: 12px; padding: 30px; box-shadow: 0 10px 30px rgba(0,0,0,0.15); text-align: center; }
.modal-header-custom h3 { margin: 0 0 20px 0; color: #1e293b; font-size: 22px; font-weight: 700; }
.modal-body-custom { margin-bottom: 25px; }
.form-control-custom { width: 100%; padding: 12px 15px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 15px; outline: none; transition: 0.2s; }
.form-control-custom:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.modal-footer-custom { display: flex; justify-content: center; gap: 15px; }
.btn-custom { padding: 10px 35px; border-radius: 8px; font-weight: 600; font-size: 15px; cursor: pointer; transition: 0.2s; min-width: 100px; }
.btn-dark-blue { background-color: #1e293b; color: #fff; border: none; }
.btn-white-outline { background-color: #fff; color: #1e293b; border: 1px solid #1e293b; }
.btn-white-outline:hover { background-color: #f8fafc; }
.radio-group { display: flex; justify-content: center; gap: 20px; }
.radio-item { font-size: 14px; cursor: pointer; display: flex; align-items: center; gap: 5px; }
</style>