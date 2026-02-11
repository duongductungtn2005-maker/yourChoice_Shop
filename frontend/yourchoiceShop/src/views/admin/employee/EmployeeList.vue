<template>
  <div class="employee-page">
    
    <div class="header-section">
      <h1 class="page-title">Quản lý tài khoản / Quản lý nhân viên</h1>
    </div>

    <div class="card filter-card">
      <div class="filter-wrapper">
        <div class="search-area">
           <div class="input-modern">
              <i class="fas fa-search icon"></i>
              <input 
                type="text" 
                v-model="searchQuery" 
                placeholder="Tìm kiếm theo tên, SĐT, email..." 
                @keyup.enter="fetchEmployees"
              >
           </div>
        </div>

        <div class="controls-row">
            <div class="filter-group">
               <div class="select-modern">
                  <span class="label-text">Chức vụ:</span>
                  <select v-model="filters.role" @change="handleFilterChange">
                     <option :value="null">Tất cả</option>
                     <option :value="'ADMIN'">Admin</option>
                     <option :value="'STAFF'">Nhân viên</option>
                  </select>
               </div>
               
               <div class="select-modern">
                  <span class="label-text">Trạng thái:</span>
                  <select v-model="filters.status" @change="handleFilterChange">
                     <option :value="null">Tất cả</option>
                     <option :value="1">Hoạt động</option>
                     <option :value="0">Ngừng hoạt động</option>
                  </select>
               </div>
            </div>

            <div class="action-buttons">
               <button class="btn btn-secondary" @click="resetFilter">
                  <i class="fas fa-sync-alt"></i> Đặt lại
               </button>
               <button class="btn btn-outline" @click="exportExcel">
                  <i class="fas fa-file-excel"></i> Xuất Excel
               </button>
               <button class="btn btn-gradient" @click="$router.push({ name: 'admin-employee-create' })">
                  <i class="fas fa-plus"></i> Tạo nhân viên
               </button>
            </div>
        </div>
      </div>
    </div>

    <div class="card table-card">
      <div class="table-responsive">
        <table>
          <thead>
            <tr>
              <th class="text-center" width="5%">STT</th>
              <th width="80px" class="text-center">Ảnh</th>
              <th width="10%">Mã NV</th>
              <th width="15%">Họ tên</th>
              <th width="15%">Email</th>
              <th width="10%">SĐT</th>
              <th width="20%">Địa chỉ</th>
              <th class="text-center" width="10%">Chức vụ</th>
              <th class="text-center" width="10%">Trạng thái</th>
              <th class="text-center" width="12%">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="employees.length === 0">
               <td colspan="10" class="text-center py-5 empty-text">Không tìm thấy nhân viên nào.</td>
            </tr>

            <tr v-else v-for="(emp, index) in employees" :key="emp.id">
              <td class="text-center">{{ index + 1 + (currentPage * pageSize) }}</td>
              
              <td class="text-center">
                 <div class="avatar-wrapper">
                    <img 
                       :src="getImageUrl(emp.anhDaiDien)" 
                       @error="handleImageError" 
                       class="avatar-img"
                    />
                 </div>
              </td>

              <td class="text-code">{{ emp.maNhanVien }}</td>
              <td class="font-bold text-dark">{{ emp.tenNhanVien }}</td>
              <td class="text-gray">{{ emp.email }}</td>
              <td>{{ emp.soDienThoai }}</td>
              
              <td class="text-address" :title="formatAddress(emp.diaChi)">
                 <span class="truncate-text">{{ formatAddress(emp.diaChi) }}</span>
              </td>

              <td class="text-center">
                 <span :class="['role-badge', (emp.quyenHan && emp.quyenHan.id === 1) ? 'role-admin' : 'role-staff']">
                    {{ (emp.quyenHan && emp.quyenHan.id === 1) ? 'Admin' : 'Nhân viên' }}
                 </span>
              </td>
              
              <td class="text-center">
                 <span :class="['status-badge', emp.trangThai === 1 ? 'active' : 'inactive']">
                    {{ emp.trangThai === 1 ? 'Hoạt động' : 'Ngừng' }}
                 </span>
              </td>

              <td class="text-center">
                 <div class="action-cell">
                    <label class="switch" title="Bật/Tắt">
                        <input type="checkbox" :checked="emp.trangThai === 1" @click="toggleStatus(emp, $event)">
                        <span class="slider round"></span>
                    </label>
                    
                    <button class="btn-icon btn-icon-edit" @click="editEmployee(emp)" title="Sửa thông tin">
                        <i class="far fa-edit"></i>
                    </button>
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
         
         <div class="page-nav">
            <button class="nav-btn" :disabled="currentPage === 0" @click="changePage(currentPage - 1)">
               <i class="fas fa-chevron-left"></i>
            </button>
            <div class="page-numbers">
               <button 
                  v-for="p in visiblePages" 
                  :key="p" 
                  :class="['nav-number', { active: p === currentPage + 1 }]" 
                  @click="changePage(p - 1)"
               >
                  {{ p }}
               </button>
            </div>
            <button class="nav-btn" :disabled="currentPage >= totalPages - 1" @click="changePage(currentPage + 1)">
               <i class="fas fa-chevron-right"></i>
            </button>
         </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue';
import request from '@/services/request'; 
import Swal from 'sweetalert2';
import { useRouter } from 'vue-router';
import { toastSuccess, toastError } from '@/utils/toast';

const router = useRouter();

// STATE
const employees = ref([]);
const searchQuery = ref('');
const pageSize = ref(5);
const currentPage = ref(0);
const totalPages = ref(0);
let timeout = null; 

const filters = reactive({ role: null, status: null });
const API_URL = '/nhan-vien'; 

// HÀM HỖ TRỢ
const getImageUrl = (imageName) => {
    if (!imageName || imageName === 'null') return 'https://via.placeholder.com/150';
    return `http://localhost:8080/api/v1/nhan-vien/images/${imageName}`;
};
const handleImageError = (e) => {
    e.target.src = "https://cdn-icons-png.flaticon.com/512/149/149071.png";
};
const formatAddress = (addr) => {
  if (!addr || addr === 'null') return "Chưa cập nhật";
  return addr.replace(/null/gi, "").replace(/(,\s*)+/g, ", ").replace(/^,\s*|,\s*$/g, "") || "Chưa cập nhật";
};

// ACTIONS
const fetchEmployees = async () => {
  try {
    const res = await request.get(API_URL, {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchQuery.value,
        role: filters.role,
        status: filters.status  
      }
    });
    employees.value = res.data.content || [];
    totalPages.value = res.data.totalPages || 0;
  } catch (error) { console.error(error); }
};

const resetFilter = () => {
    searchQuery.value = '';
    filters.role = null;
    filters.status = null;
    currentPage.value = 0;
    fetchEmployees();
};

const toggleStatus = async (emp, event) => {
    event.preventDefault();
    const newStatus = emp.trangThai === 1 ? 0 : 1;
    const result = await Swal.fire({
        title: 'Xác nhận thay đổi?',
        text: `Đổi trạng thái nhân viên "${emp.tenNhanVien}"?`,
        icon: 'question', showCancelButton: true,
        confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        try {
            await request.put(`${API_URL}/${emp.id}/trang-thai`, null, {
                params: { trangThai: newStatus }
            });
            emp.trangThai = newStatus;
            toastSuccess('Cập nhật trạng thái thành công!');
        } catch (error) {
            toastError('Lỗi cập nhật trạng thái');
        }
    }
};

const editEmployee = (emp) => {
    router.push({ 
        name: 'admin-employee-edit', 
        params: { id: emp.id } 
    });
};

const exportExcel = async () => {
  const result = await Swal.fire({
    title: 'Xác nhận', text: 'Tải xuống danh sách nhân viên?',
    icon: 'question', showCancelButton: true, confirmButtonText: 'Có', cancelButtonText: 'Hủy'
  });
  if (!result.isConfirmed) return;

  try {
    const response = await request.get(`${API_URL}/export-excel`, {
      params: { keyword: searchQuery.value, role: filters.role, trangThai: filters.status },
      responseType: 'blob' 
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `DS_NhanVien_${new Date().toISOString().slice(0,10)}.xlsx`);
    document.body.appendChild(link); link.click(); document.body.removeChild(link);
    toastSuccess('Xuất Excel thành công!');
  } catch (error) { toastError('Lỗi xuất file Excel'); }
};

const changePage = (p) => { if (p >= 0 && p < totalPages.value) { currentPage.value = p; fetchEmployees(); } };
const handlePageSizeChange = () => { currentPage.value = 0; fetchEmployees(); };
const handleFilterChange = () => { currentPage.value = 0; fetchEmployees(); };

const visiblePages = computed(() => {
    const pages = [];
    const current = currentPage.value + 1;
    for (let i = 1; i <= totalPages.value; i++) {
        if (i === 1 || i === totalPages.value || (i >= current - 1 && i <= current + 1)) {
            pages.push(i);
        }
    }
    return pages;
});

watch(searchQuery, () => {
    clearTimeout(timeout);
    timeout = setTimeout(() => { currentPage.value = 0; fetchEmployees(); }, 500);
});

onMounted(() => { fetchEmployees(); });
</script>

<style scoped>
.employee-page { font-family: 'Segoe UI', sans-serif; background-color: #f3f4f6; min-height: 100vh; padding: 20px; }
.header-section { margin-bottom: 20px; }
.page-title { font-size: 22px; font-weight: 700; color: #1e293b; }

/* Cards - Viền màu xanh nhạt */
.card { 
    background: #fff; 
    border-radius: 16px; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    border: 1px solid #bfdbfe; 
    margin-bottom: 24px;
}

.filter-card { padding: 24px; }
.filter-wrapper { display: flex; flex-direction: column; gap: 20px; }

/* Inputs */
.search-area { width: 100%; }
.input-modern, .select-modern { display: flex; align-items: center; border: 1px solid #e2e8f0; border-radius: 6px; padding: 0 12px; background: #fff; transition: 0.2s; height: 40px; }
.input-modern:focus-within, .select-modern:focus-within { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.input-modern input { border: none; outline: none; width: 100%; height: 100%; font-size: 14px; padding-left: 10px; color: #334155; }
.input-modern .icon { color: #94a3b8; font-size: 14px; }

.controls-row { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.filter-group { display: flex; gap: 15px; }
.select-modern { min-width: 200px; background: #f8fafc; }
.select-modern .label-text { font-size: 13px; font-weight: 600; color: #64748b; margin-right: 8px; white-space: nowrap; }
.select-modern select { border: none; outline: none; background: transparent; font-size: 14px; font-weight: 500; color: #334155; cursor: pointer; width: 100%; }

/* Buttons */
.action-buttons { display: flex; gap: 10px; }
.btn { padding: 0 20px; height: 40px; border-radius: 6px; font-weight: 600; font-size: 13px; cursor: pointer; display: inline-flex; align-items: center; gap: 8px; transition: 0.2s; border: none; }
.btn-secondary { background: #334155; color: #fff; } .btn-secondary:hover { background: #1e293b; }
.btn-outline { background: #fff; border: 1px solid #e2e8f0; color: #475569; } .btn-outline:hover { background: #f8fafc; border-color: #cbd5e1; }
.btn-gradient { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); }
.btn-gradient:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.3); }

/* Table */
.table-card { padding: 0; overflow: hidden; }
.table-responsive { width: 100%; overflow-x: auto; }
table { width: 100%; border-collapse: collapse; table-layout: fixed; }

/* === HEADER STYLE (SỬA LẠI) === */
th { 
    background: #eff6ff; 
    padding: 16px; 
    text-align: left; 
    font-size: 12px; 
    font-weight: 700; 
    color: #1e40af; 
    text-transform: uppercase; 
    /* Dùng !important để chắc chắn ghi đè mọi border mặc định */
    border-bottom: 0px solid transparent !important; 
    white-space: nowrap; 
}

td { padding: 14px 16px; border-bottom: 1px solid #f1f5f9; font-size: 14px; color: #334155; vertical-align: middle; }
.text-center { text-align: center; }
.text-code { font-family: monospace; font-weight: 700; color: #3b82f6; }
.font-bold { font-weight: 600; }
.text-gray { color: #64748b; font-size: 13px; }

/* Avatar */
.avatar-wrapper { display: flex; justify-content: center; }
.avatar-img { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 1px solid #e2e8f0; }

/* Text & Badges */
.text-address { max-width: 200px; font-size: 13px; color: #475569; }
.truncate-text { display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; text-overflow: ellipsis; }

.status-badge { padding: 4px 12px; border-radius: 20px; font-size: 11px; font-weight: 600; white-space: nowrap; }
.active { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.inactive { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }

.role-badge { padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; }
.role-admin { background: #dbeafe; color: #1e40af; border: 1px solid #bfdbfe; }
.role-staff { background: #f3f4f6; color: #4b5563; border: 1px solid #e5e7eb; }

/* Actions Column */
.action-cell { display: flex; justify-content: center; align-items: center; gap: 8px; }
.btn-icon { width: 32px; height: 32px; border-radius: 6px; background: #fff; border: 1px solid #e2e8f0; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; color: #64748b; }
.btn-icon:hover { background: #f1f5f9; border-color: #cbd5e1; color: #0f172a; }
.btn-icon-edit:hover { color: #2563eb; border-color: #bfdbfe; background: #eff6ff; }

/* Switch Toggle */
.switch { position: relative; display: inline-block; width: 36px; height: 20px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #cbd5e1; transition: .4s; border-radius: 34px; }
.slider:before { position: absolute; content: ""; height: 14px; width: 14px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; box-shadow: 0 1px 2px rgba(0,0,0,0.2); }
input:checked + .slider { background-color: #10b981; }
input:checked + .slider:before { transform: translateX(16px); }

/* Pagination */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; padding: 15px 24px; border-top: 1px solid #e2e8f0; background: #fff; }
.page-info { font-size: 13px; color: #64748b; font-weight: 500; }
.page-info select { border: 1px solid #e2e8f0; border-radius: 4px; padding: 2px 5px; margin: 0 5px; outline: none; cursor: pointer; }
.page-nav { display: flex; align-items: center; gap: 6px; }
.page-numbers { display: flex; gap: 4px; }
.nav-btn, .nav-number { width: 32px; height: 32px; border: 1px solid #e2e8f0; border-radius: 6px; background: #fff; color: #64748b; font-size: 13px; font-weight: 500; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; }
.nav-btn:hover:not(:disabled), .nav-number:hover { background: #f8fafc; border-color: #cbd5e1; color: #0f172a; }
.nav-number.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.nav-btn:disabled { opacity: 0.5; cursor: not-allowed; background: #f1f5f9; }
</style>