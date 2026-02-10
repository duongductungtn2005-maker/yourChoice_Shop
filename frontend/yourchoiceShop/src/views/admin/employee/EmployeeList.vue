<template>
  <div class="employee-page">
    <div class="header-section">
      <h1 class="page-title">Quản lý nhân viên</h1>
    </div>

    <div class="card">
      <div class="card-header">
         <div class="search-wrap">
            <span class="search-icon">🔍</span>
            <input 
              type="text" 
              v-model="searchQuery" 
              placeholder="Tìm theo tên, SĐT, email..." 
              @keyup.enter="fetchEmployees"
            >
         </div>
         <div class="action-group">
             <button class="btn btn-outline" @click="exportExcel">
               <font-awesome-icon :icon="['fas','file-excel']" /> Xuất Excel
             </button>
             <button class="btn btn-primary" @click="$router.push({ name: 'admin-employee-create' })">
                <i class="fas fa-plus"></i> Tạo nhân viên
             </button>
         </div>
      </div>

      <div class="filter-bar">
         <div class="filter-item">
            <label>Giới tính:</label>
            <select v-model="filters.gender" @change="handleFilterChange">
               <option value="">Tất cả</option>
               <option :value="true">Nam</option>
               <option :value="false">Nữ</option>
            </select>
         </div>
         <div class="filter-item">
            <label>Trạng thái:</label>
            <select v-model="filters.status" @change="handleFilterChange">
               <option value="">Tất cả</option>
               <option value="1">Hoạt động</option>
               <option value="0">Ngừng HĐ</option>
            </select>
         </div>
      </div>

      <div class="table-responsive">
        <table>
          <thead>
            <tr>
              <th class="text-center" width="5%">STT</th>
              <th width="80px">Ảnh</th>
              <th>Mã NV</th>
              <th>Họ tên</th>
              <th>Email</th>
              <th>SĐT</th>
              <th>Địa chỉ</th>
              <th class="text-center">Giới tính</th>
              <th class="text-center">Trạng thái</th>
              <th class="text-center">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="employees.length === 0">
                <td colspan="10" class="text-center py-4 text-gray">Không tìm thấy nhân viên nào.</td>
            </tr>

            <tr v-for="(emp, index) in employees" :key="emp.id">
              <td class="text-center">{{ index + 1 + (currentPage * pageSize) }}</td>
              <td>
                <div class="avatar-cell">
                    <img 
                        :src="getImageUrl(emp.anhDaiDien)" 
                        @error="handleImageError" 
                        class="avatar-img"
                    />
                </div>
              </td>
              <td class="text-code">{{ emp.maNhanVien }}</td>
              <td class="font-medium text-primary">{{ emp.tenNhanVien }}</td>
              <td>{{ emp.email }}</td>
              <td>{{ emp.soDienThoai }}</td>
              <td :title="formatAddress(emp.diaChi)" class="text-truncate" style="max-width: 150px;">
                  {{ formatAddress(emp.diaChi) }}
              </td>
              <td class="text-center">
                  {{ emp.gioiTinh === true ? 'Nam' : (emp.gioiTinh === false ? 'Nữ' : '-') }}
              </td>
              
              <td class="text-center">
                 <span :class="['badge', emp.trangThai === 1 ? 'badge-success' : 'badge-secondary']">
                    {{ emp.trangThai === 1 ? 'Hoạt động' : 'Ngừng HĐ' }}
                 </span>
              </td>
              
              <td class="text-center">
                 <div class="action-container">
                    <div class="status-wrapper" @click="toggleStatus(emp)" title="Bật/Tắt trạng thái">
                       <i 
                         class="fas toggle-icon"
                         :class="emp.trangThai === 1 ? 'fa-toggle-on active' : 'fa-toggle-off inactive'"
                       ></i>
                    </div>
                    
                 </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="pagination-footer">
         <div class="page-info">
            Hiển thị 
            <select v-model="pageSize" @change="fetchEmployees">
               <option :value="5">5</option>
               <option :value="10">10</option>
               <option :value="20">20</option>
            </select> 
            nhân viên / trang
         </div>
         <div class="page-controls">
            <button :disabled="currentPage === 0" @click="changePage(currentPage - 1)">‹</button>
            <button 
              v-for="p in visiblePages" 
              :key="p" 
              :class="{ active: p === currentPage + 1 }" 
              @click="changePage(p - 1)"
            >
              {{ p }}
            </button>
            <button :disabled="currentPage >= totalPages - 1" @click="changePage(currentPage + 1)">›</button>
         </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue';
import request from '@/services/request'; // Dùng request chung
import Swal from 'sweetalert2';
import { useRouter } from 'vue-router';

const router = useRouter();

// STATE
const employees = ref([]);
const searchQuery = ref('');
const pageSize = ref(5);
const currentPage = ref(0);
const totalPages = ref(0);
let timeout = null; 

const filters = reactive({ gender: '', status: '' });
const API_URL = '/nhan-vien'; 

// HÀM HỖ TRỢ
const getImageUrl = (imageName) => {
    if (!imageName) return 'https://via.placeholder.com/150';
    return `http://localhost:8080/api/v1/nhan-vien/images/${imageName}`;
};
const handleImageError = (e) => {
    e.target.src = "https://cdn-icons-png.flaticon.com/512/149/149071.png";
};
const formatAddress = (addr) => {
  if (!addr) return "-";
  return addr.replace(/null/gi, "").replace(/(,\s*)+/g, ", ").replace(/^,\s*|,\s*$/g, "") || "-";
};

// ACTIONS
const fetchEmployees = async () => {
  try {
    const res = await request.get(API_URL, {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchQuery.value,
        gender: filters.gender,
        status: filters.status  
      }
    });
    employees.value = res.data.content || [];
    totalPages.value = res.data.totalPages || 0;
  } catch (error) { console.error(error); }
};

const toggleStatus = async (emp) => {
    const oldStatus = emp.trangThai;
    const newStatus = oldStatus === 1 ? 0 : 1;
    emp.trangThai = newStatus; // Optimistic UI

    try {
        await request.put(`${API_URL}/${emp.id}/trang-thai`, null, {
            params: { trangThai: newStatus }
        });
        const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 1500 });
        Toast.fire({ icon: 'success', title: 'Cập nhật trạng thái thành công' });
    } catch (error) {
        emp.trangThai = oldStatus; 
        Swal.fire('Lỗi', 'Không thể cập nhật trạng thái', 'error');
    }
};

const softDeleteEmployee = async (emp) => {
    const result = await Swal.fire({
        title: 'Xác nhận xóa?',
        text: `Bạn có chắc muốn xóa nhân viên ${emp.tenNhanVien}?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Xóa',
        cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        try {
            await request.delete(`${API_URL}/${emp.id}`);
            emp.trangThai = 0; 
            Swal.fire('Thành công', 'Đã xóa nhân viên', 'success');
        } catch (error) { Swal.fire('Lỗi', 'Có lỗi xảy ra', 'error'); }
    }
};

const exportExcel = async () => {
  const result = await Swal.fire({
    title: 'Xác nhận',
    text: 'Bạn có muốn tải xuống danh sách nhân viên không?',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Có',
    cancelButtonText: 'Hủy'
  });
  if (!result.isConfirmed) return;

  try {
    const response = await request.get(`${API_URL}/export-excel`, {
      params: { keyword: searchQuery.value, gioiTinh: filters.gender, trangThai: filters.status },
      responseType: 'blob' 
    });
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `DS_NhanVien_${new Date().toISOString().slice(0,10)}.xlsx`);
    document.body.appendChild(link); link.click(); document.body.removeChild(link); window.URL.revokeObjectURL(url);

    const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 1500 });
    Toast.fire({ icon: 'success', title: 'Xuất Excel thành công' });
  } catch (error) { Swal.fire('Lỗi', 'Không thể xuất file Excel', 'error'); }
};

const changePage = (p) => { if (p >= 0 && p < totalPages.value) { currentPage.value = p; fetchEmployees(); } };
const handleFilterChange = () => { currentPage.value = 0; fetchEmployees(); };

// Trang hiển thị giống màn Sản phẩm
const visiblePages = computed(() => {
    const pages = [];
    const current = currentPage.value + 1; // currentPage là 0-based
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
/* CSS ĐỒNG BỘ VỚI MÀN HÌNH KHÁCH HÀNG */
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }

.employee-page { font-family: 'Segoe UI', sans-serif; background-color: #f8fafc; min-height: 100vh; padding: 20px; }
.header-section { margin-bottom: 20px; }
.breadcrumb { font-size: 14px; color: #64748b; } .breadcrumb .active { font-weight: 500; color: #0f172a; }

.card { background: #fff; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }

.search-wrap { position: relative; width: 350px; }
.search-wrap input { width: 100%; padding: 8px 12px 8px 36px; border: 1px solid #e2e8f0; border-radius: 4px; outline: none; font-size: 14px; }
.search-wrap input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: #94a3b8; }

action-group { display: flex; gap: 10px; }
.btn { 
  height: 38px; /* Chiều cao cố định cho các nút bằng nhau */
  padding: 0 16px; 
  border-radius: 4px; 
  font-weight: 500; 
  cursor: pointer; 
  font-size: 14px; 
  border: 1px solid transparent; 
  transition: 0.2s;
  display: flex; /* Flex để căn giữa icon và chữ */
  align-items: center;
  gap: 8px; /* Khoảng cách giữa icon và chữ */
}
.btn-primary { 
  background: #0f172a; 
  color: #fff; 
}
.btn-primary:hover { 
  background: #1e293b; 
}
.btn-outline { 
  background: #fff; 
  border-color: #cbd5e1; 
  color: #475569; 
}
.btn-outline:hover { 
  background: #f1f5f9; 
  border-color: #94a3b8;
}

.filter-bar { display: flex; gap: 30px; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #f1f5f9; }
.filter-item { display: flex; align-items: center; gap: 10px; font-size: 14px; font-weight: 500; color: #334155; }
.filter-item select { padding: 4px 8px; border: 1px solid #e2e8f0; border-radius: 4px; cursor: pointer; outline: none; color: #475569; }

.table-responsive { overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 4px; }
table { width: 100%; border-collapse: collapse; }
th { background: #E9F1FB; padding: 12px; font-weight: 700; color: #1E3A8A; border-bottom: 1px solid #e2e8f0; font-size: 13px; text-transform: uppercase; text-align: left; }
td { padding: 12px; border-bottom: 1px solid #f1f5f9; font-size: 14px; font-weight: 400; vertical-align: middle; color: #334155; }

.avatar-cell { display: flex; justify-content: center; }
.avatar-img { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 1px solid #e2e8f0; }

.text-center { text-align: center; } .font-bold { font-weight: 400; } .font-medium { font-weight: 500; }
.text-primary { color: #0f172a; } .text-code { color: #64748b; font-family: monospace; }
.text-truncate { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: inline-block; vertical-align: middle; }

.badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.badge-success { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
.badge-secondary { background: #f1f5f9; color: #64748b; border: 1px solid #e2e8f0; }

.action-container { display: flex; align-items: center; justify-content: center; gap: 15px; }
.status-wrapper { cursor: pointer; transition: opacity 0.2s; } .status-wrapper:hover { opacity: 0.8; }
.toggle-icon { font-size: 24px; transition: color 0.3s ease; }
.toggle-icon.active { color: #10b981; } .toggle-icon.inactive { color: #cbd5e1; }
.btn-icon.delete { color: #ef4444; background: none; border: none; font-size: 16px; cursor: pointer; }

.pagination-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; padding-top: 15px; border-top: 1px solid #f1f5f9; }
.page-info { font-size: 14px; color: #64748b; }
.page-info select { border: 1px solid #e2e8f0; border-radius: 4px; padding: 4px 8px; margin: 0 5px; outline: none; }
.page-controls button { width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; margin-left: 5px; cursor: pointer; }
.page-controls button.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.page-controls button:disabled { background: #f8fafc; color: #cbd5e1; cursor: not-allowed; }
</style>