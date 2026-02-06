<template>
  <div class="attribute-page">
          <h1 class="page-title">Quản lý sản phẩm / Chất liệu</h1>


    <div class="card">
      <div class="card-header">
         <div class="toolbar">
            <div class="search-wrap">
               <span class="search-icon">🔍</span>
               <input 
                 type="text" 
                 v-model="filter.keyword" 
                 placeholder="Tìm kiếm chất liệu..." 
                 @keyup.enter="fetchData"
               >
            </div>
            <div class="action-group">
               <button class="btn btn-outline" @click="exportExcel">
                <font-awesome-icon :icon="['fas', 'file-excel']" /> Xuất Excel
            </button>
               <button class="btn btn-primary" @click="openModal()">+ Tạo mới</button>
            </div>
         </div>
      </div>

      <div class="table-responsive">
        <table>
          <thead>
            <tr>
              <th class="text-center" width="10%">STT</th>
              <th class="text-center" width="40%">Tên Chất Liệu</th>
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
              <td class="text-center font-medium text-primary">{{ item.tenChatLieu }}</td>
              <td class="text-center">
                 <span :class="['badge', item.trangThai === 1 ? 'badge-success' : 'badge-danger']">
                    {{ item.trangThai === 1 ? 'Hoạt động' : 'Ngừng' }}
                 </span>
              </td>
              <td class="text-center col-ngay-tao">{{ formatDate(item.ngayTao) }}</td>
              <td class="text-center">
    <button class="action-btn" @click="openModal(item)" title="Sửa">
    <font-awesome-icon :icon="['far', 'pen-to-square']" />
</button>

    <label class="switch" title="Bật/Tắt trạng thái">
        <input 
            type="checkbox" 
            :checked="item.trangThai === 1" 
            @click="handleToggleStatus(item, $event)"
        >
        <span class="slider round"></span>
    </label>
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
                <h3>{{ isEdit ? 'Cập nhật chất liệu' : 'Thêm mới chất liệu' }}</h3>
             </div>
             <div class="modal-body-custom">
                <form @submit.prevent="saveData">
                   <div class="form-group">
                      <input 
                        type="text" 
                        v-model="form.ten" 
                        class="form-control-custom" 
                        placeholder="Nhập tên chất liệu (VD: Cotton, Lụa...)" 
                        ref="nameInput"
                      >
                   </div>
                   <div class="form-group" v-if="isEdit" style="margin-top: 15px;">
                      <label style="font-size: 13px; font-weight: 500;">Trạng thái:</label>
                      <div class="radio-group">
                         <label class="radio-item"><input type="radio" :value="1" v-model="form.trangThai"> Hoạt động</label>
                         <label class="radio-item"><input type="radio" :value="0" v-model="form.trangThai"> Ngừng</label>
                      </div>
                   </div>
                </form>
             </div>
            <div class="modal-footer-custom">
    <button type="button" class="btn-custom btn-white-outline" @click="showModal = false">Đóng</button>
    
    <button type="button" class="btn-custom btn-dark-blue" @click="saveData">
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

// API URL CHO CHẤT LIỆU
const API_URL = 'http://localhost:8080/api/v1/chat-lieu';

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

// --- CHỨC NĂNG XUẤT EXCEL ---
const exportExcel = async () => {
    const confirmRes = await Swal.fire({
        title: 'Xác nhận',
        text: 'Bạn có muốn tải xuống danh sách chất liệu không?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Có',
        cancelButtonText: 'Hủy'
    });
    if (!confirmRes.isConfirmed) return;

    try {
        const response = await axios.get(`${API_URL}/export`, { responseType: 'blob' });
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        const dateStr = new Date().toISOString().slice(0,10);
        link.setAttribute('download', `DS_ChatLieu_${dateStr}.xlsx`);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

        const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 2000 });
        Toast.fire({ icon: 'success', title: 'Xuất Excel thành công' });

    } catch (e) {
        console.error("Lỗi xuất Excel:", e);
        Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Không thể xuất file Excel.' });
    }
};

const openModal = (item = null) => {
    if (item) {
        isEdit.value = true; currentId.value = item.id;
        form.ten = item.tenChatLieu; // Đổi thành tenChatLieu
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
        Toast.fire({ icon: 'warning', title: 'Vui lòng nhập tên chất liệu!' });
        return;
    }

    try {
        const checkRes = await axios.get(API_URL, {
            params: { keyword: form.ten.trim(), page: 0, size: 100 }
        });
        const listCheck = checkRes.data.content || [];
        
        // Check trùng với tenChatLieu
        const isDuplicate = listCheck.some(item => 
            item.tenChatLieu.toLowerCase() === form.ten.trim().toLowerCase() && 
            item.id !== currentId.value
        );

        if (isDuplicate) {
            Swal.fire({
                icon: 'error', title: 'Tên đã tồn tại!', 
                text: `Chất liệu "${form.ten}" đã có trong hệ thống.`,
                confirmButtonColor: '#ef4444'
            });
            return;
        }
    } catch (e) { console.error(e); }

    const result = await Swal.fire({
        title: `<h3 style="color:#1e293b; margin:0; font-size:20px;">Xác nhận ${isEdit.value ? 'Cập nhật' : 'Thêm mới'}?</h3>`,
        icon: 'warning', 
        iconColor: '#facc15', 
        showCancelButton: true, 
        confirmButtonText: 'Có', 
        cancelButtonText: 'Không',
        // QUAN TRỌNG: confirmButton dùng class 'solid' (đậm), cancelButton dùng 'outline' (trắng)
        customClass: { confirmButton: 'swal-btn-solid', cancelButton: 'swal-btn-outline', popup: 'swal-rounded' }, 
        buttonsStyling: false, 
        reverseButtons: true
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

const handleToggleStatus = async (item, event) => {
    event.preventDefault(); // Chặn đổi trạng thái ngay lập tức

    const currentStatus = item.trangThai;
    const newStatus = currentStatus === 1 ? 0 : 1;
    const actionText = newStatus === 1 ? 'Kích hoạt' : 'Ngừng hoạt động';
    const confirmBtnColor = newStatus === 1 ? '#10b981' : '#ef4444'; 

    const result = await Swal.fire({
        title: `<h3 style="color:#1e293b; font-size:18px;">Xác nhận ${actionText}?</h3>`,
        text: `Bạn có chắc muốn ${actionText.toLowerCase()} tay áo "${item.tenTayAo}"?`, // Dùng tenTayAo
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy',
        confirmButtonColor: confirmBtnColor,
        customClass: { popup: 'swal-rounded' }
    });

    if (result.isConfirmed) {
        try {
            if (newStatus === 0) {
                // Tắt -> Gọi Delete (Xóa mềm)
                await axios.delete(`${API_URL}/${item.id}`);
            } else {
                // Bật -> Gọi Put (Cập nhật trạng thái = 1)
                await axios.put(`${API_URL}/${item.id}`, { 
                    ten: item.tenTayAo, // Gửi đúng tên Tay Áo
                    trangThai: 1 
                });
            }

            // Cập nhật giao diện
            item.trangThai = newStatus;
            
            const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 1500 });
            Toast.fire({ icon: 'success', title: `Đã ${actionText.toLowerCase()} thành công!` });

        } catch (e) {
            console.error(e);
            Swal.fire({
                icon: 'error',
                title: 'Lỗi',
                text: 'Không thể thay đổi trạng thái. Vui lòng thử lại.',
                customClass: { popup: 'swal-rounded' }
            });
        }
    }
};

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
/* --- CSS TOGGLE SWITCH --- */
.switch {
  position: relative;
  display: inline-block;
  width: 36px;
  height: 20px;
  margin-left: 12px; /* TẠO KHOẢNG CÁCH VỚI NÚT SỬA */
}

.switch input { 
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: #cbd5e1;
  transition: .4s;
  border-radius: 34px;
}

.slider:before {
  position: absolute;
  content: "";
  height: 14px;
  width: 14px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
  box-shadow: 0 2px 4px 0 rgba(0,0,0,0.2);
}

input:checked + .slider {
  background-color: #10b981;
}

input:checked + .slider:before {
  transform: translateX(16px);
}
.swal-rounded { border-radius: 12px !important; }
.swal-btn-outline { background-color: #fff !important; color: #1e293b !important; border: 1px solid #1e293b !important; padding: 8px 24px; border-radius: 6px; font-weight: 500; cursor: pointer; margin-left: 10px; }
.swal-btn-outline:hover { background-color: #f1f5f9 !important; }
.swal-btn-solid { background-color: #1e293b !important; color: #fff !important; border: none !important; padding: 8px 24px; border-radius: 6px; font-weight: 500; cursor: pointer; }
.swal-btn-solid:hover { background-color: #334155 !important; }
.swal-btn-danger { background-color: #ef4444 !important; color: #fff !important; padding: 8px 24px; border-radius: 6px; font-weight: 500; cursor: pointer; margin-left: 10px; }
</style>

<style scoped>
.action-btn { 
    background: none; 
    border: none; 
    cursor: pointer; 
    font-size: 18px; 
    color: #475569; 
    transition: 0.2s; 
}

.action-btn:hover { 
    color: #0f172a; 
    transform: scale(1.1); 
}
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }
.attribute-page { font-family: 'Segoe UI', sans-serif; color: #333; background-color: #f8fafc; min-height: 100vh; padding: 20px; }
.header-section { margin-bottom: 20px; }
.breadcrumb { font-size: 14px; color: #64748b; } .breadcrumb .active { font-weight: 500; color: #0f172a; }
.card { background: #fff; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); padding: 28px; border: 1px solid rgba(30,58,138,0.06); transition: box-shadow 0.2s ease; }
.card:hover { box-shadow: 0 10px 28px rgba(0,0,0,0.1); }
.toolbar { display: flex; justify-content: space-between; margin-bottom: 20px; }
.search-wrap { position: relative; width: 300px; }
.search-wrap input { width: 100%; padding: 8px 12px 8px 36px; border: 1px solid #e2e8f0; border-radius: 4px; outline: none; }
.search-wrap .search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: #94a3b8; }
.btn { padding: 8px 16px; border-radius: 4px; font-weight: 500; cursor: pointer; border: 1px solid transparent; }
.btn-primary { background: #0f172a; color: #fff; }
.btn-outline { background: #fff; border-color: #cbd5e1; color: #475569; }
.action-group { display: flex; gap: 10px; }
.table-responsive { overflow-x: auto; border: 1px solid rgba(30,58,138,0.08); border-radius: 10px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
table { width: 100%; border-collapse: collapse; }
th { background: #E9F1FB; padding: 18px 20px; font-weight: 700; color: #1E3A8A; border-bottom: 2px solid #1E3A8A; font-size: 13px; text-transform: uppercase; }
td { padding: 18px 20px; border-bottom: 1px solid #f1f5f9; font-size: 14px; font-weight: 400; vertical-align: middle; transition: background-color 0.2s ease; }
table tbody tr:hover td { background-color: #f8fafc; }
.text-center { text-align: center; } .font-medium { font-weight: 500; } .text-primary { color: #0f172a; } .text-gray { color: #64748b; } .col-ngay-tao { color: #000; }
.badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 500; }
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
.btn-custom { padding: 10px 35px; border-radius: 8px; font-weight: 500; font-size: 15px; cursor: pointer; transition: 0.2s; min-width: 100px; }
.btn-dark-blue { background-color: #1e293b; color: #fff; border: none; }
.btn-white-outline { background-color: #fff; color: #1e293b; border: 1px solid #1e293b; }
.btn-white-outline:hover { background-color: #f8fafc; }
.radio-group { display: flex; justify-content: center; gap: 20px; }
.radio-item { font-size: 14px; cursor: pointer; display: flex; align-items: center; gap: 5px; }
</style>