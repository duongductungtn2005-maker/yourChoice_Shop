<template>
  <div class="page-container">
    <h1 class="page-title">Quản lý tài khoản / Quản lý nhân viên</h1>

    <div class="control-panel">
      <div class="controls-row">
        <div class="filter-group">
          <div class="search-box">
            <i class="fas fa-magnifying-glass search-icon"></i>
            <input 
              class="input-den"
              type="text" 
              v-model="searchQuery" 
              placeholder="Tìm tên, SĐT, email..." 
              @keyup.enter="fetchEmployees"
            >
          </div>

          <select v-model="filters.role" @change="handleFilterChange" class="form-select">
            <option :value="null">-- Chức vụ --</option>
            <option :value="'ADMIN'">Admin</option>
            <option :value="'STAFF'">Nhân viên</option>
          </select>

          <select v-model="filters.status" @change="handleFilterChange" class="form-select">
            <option :value="null">-- Trạng thái --</option>
            <option :value="1">Hoạt động</option>
            <option :value="0">Ngừng hoạt động</option>
          </select>
        </div>

        <div class="action-group">
          <button class="btn btn-navy" @click="resetFilter">
            <font-awesome-icon :icon="['fas', 'sync-alt']" /> Đặt lại
          </button>

          <button class="btn btn-outline" @click="exportExcel">
            <font-awesome-icon :icon="['fas', 'file-excel']" /> Xuất Excel
          </button>
          
          <button class="btn btn-gradient" @click="$router.push({ name: 'admin-employee-create' })">
            <font-awesome-icon :icon="['fas', 'plus']" /> Tạo mới
          </button>
        </div>
      </div>
    </div>

    <div class="table-container">
      <table class="custom-table">
        <thead>
          <tr>
            <th class="text-center" width="5%">STT</th>
            <th width="8%" class="text-center">Ảnh</th>
            <th width="10%">Mã NV</th>
            <th width="15%">Họ tên</th>
            <th width="15%">Email</th>
            <th width="10%">SĐT</th>
            <th width="15%">Địa chỉ</th>
            <th class="text-center" width="10%">Chức vụ</th>
            <th class="text-center" width="10%">Trạng thái</th>
            <th class="text-center" width="10%">Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="10" class="text-center empty-state">Đang tải dữ liệu...</td>
          </tr>
          <tr v-else-if="employees.length === 0">
            <td colspan="10" class="text-center empty-state">Không tìm thấy nhân viên nào.</td>
          </tr>

          <tr v-else v-for="(emp, index) in employees" :key="emp.id">
            <td class="text-center">{{ (currentPage * pageSize) + index + 1 }}</td>
            
            <td class="text-center">
              <div class="avatar-wrapper">
                <img 
                  :src="getImageUrl(emp.anhDaiDien)" 
                  @error="handleImageError" 
                  class="avatar-img"
                />
              </div>
            </td>

            <td class="code-text">{{ emp.maNhanVien }}</td>
            <td class="name-text">{{ emp.tenNhanVien }}</td>
            <td class="text-gray">{{ emp.email }}</td>
            <td>{{ emp.soDienThoai }}</td>
            
            <td class="text-address">
              <span class="truncate-text" :title="formatAddress(emp.diaChi)">
                {{ formatAddress(emp.diaChi) }}
              </span>
            </td>

            <td class="text-center">
              <span class="badge" :class="(emp.quyenHan && emp.quyenHan.id === 1) ? 'badge-admin' : 'badge-staff'">
                {{ (emp.quyenHan && emp.quyenHan.id === 1) ? 'Admin' : 'Nhân viên' }}
              </span>
            </td>

            <td class="text-center">
              <span class="badge" :class="emp.trangThai === 1 ? 'badge-active' : 'badge-stopped'">
                {{ emp.trangThai === 1 ? 'Hoạt động' : 'Ngừng' }}
              </span>
            </td>

            <td class="text-center action-col">
              <div class="action-wrapper">
                <button class="icon-btn" @click="editEmployee(emp)" title="Xem chi tiết">
                  <i class="far fa-eye"></i>
                </button>

                <label class="switch" title="Bật/Tắt trạng thái">
                  <input 
                    type="checkbox" 
                    :checked="emp.trangThai === 1" 
                    @click="toggleStatus(emp, $event)"
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
import request from '@/services/request'; 
import Swal from 'sweetalert2';
import { useRouter } from 'vue-router';
import { toastSuccess, toastError } from '@/utils/toast';

const router = useRouter();

// STATE
const employees = ref([]);
const loading = ref(false);
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
  if (!addr || addr === 'null') return "-";
  return addr.replace(/null/gi, "").replace(/(,\s*)+/g, ", ").replace(/^,\s*|,\s*$/g, "") || "-";
};

// ACTIONS
const fetchEmployees = async () => {
  loading.value = true;
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
  } catch (error) { 
      console.error(error); 
  } finally {
      loading.value = false;
  }
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
    const actionText = newStatus === 1 ? 'Kích hoạt' : 'Ngừng hoạt động';
    
    const result = await Swal.fire({
        title: `Xác nhận ${actionText}?`,
        text: `Bạn có muốn ${actionText.toLowerCase()} nhân viên "${emp.tenNhanVien}"?`,
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
/* === CSS CHUẨN ĐỒNG BỘ === */
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background-color: #f8fafc; min-height: 100vh; color: #333; font-size: 14px; }
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
.search-box input { width: 100%; padding: 8px 10px 8px 36px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; height: 40px; }
.search-box input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }

/* SELECT STYLES */
.form-select { height: 40px; padding: 0 10px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; color: #334155; min-width: 150px; cursor: pointer; }
.form-select:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }

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
    background: #eff6ff !important; color: #1e40af; padding: 16px; text-align: left;
     font-weight: 700; text-transform: uppercase; border-bottom: none !important; white-space: nowrap;
}
.custom-table th.text-center { text-align: center; }
.custom-table td { padding: 14px 16px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; font-size: 14px; }

.text-center { text-align: center; }
.code-text { color: #2563eb; font-weight: 600; font-family: monospace; font-size: 13px; }
.name-text { font-weight: 600; color: #1e293b; }
.text-gray { color: #64748b; }
.text-address { max-width: 200px; color: #475569; font-size: 13px; }
.truncate-text { display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; text-overflow: ellipsis; }
.empty-state { padding: 40px; color: #64748b; font-style: italic; }

/* AVATAR */
.avatar-wrapper { display: flex; justify-content: center; }
.avatar-img { width: 36px; height: 36px; border-radius: 50%; object-fit: cover; border: 1px solid #e2e8f0; }

/* BADGES */
.badge { padding: 4px 12px; border-radius: 20px; font-size: 11px; font-weight: 600; white-space: nowrap; border: 1px solid transparent; }
.badge-active { background: #dcfce7; color: #166534; border-color: #bbf7d0; }
.badge-stopped { background: #fee2e2; color: #991b1b; border-color: #fecaca; }

.badge-admin { background: #dbeafe; color: #1e40af; border-color: #bfdbfe; }
.badge-staff { background: #f3f4f6; color: #4b5563; border-color: #e5e7eb; }

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
.btn-navy {
    background-color: #0f172a; /* Xanh than đậm */
    color: #ffffff;
    box-shadow: 0 4px 6px rgba(15, 23, 42, 0.2);
}
.btn-navy:hover {
    background-color: #1e293b;
    transform: translateY(-1px);
}
/* Màu chữ placeholder đen xì, rõ nét */
.input-den::placeholder {
    color: #000000 !important;  /* Màu đen */
    opacity: 1 !important;      /* Chống mờ */
    font-weight: 500;           /* Đậm lên tí cho dễ đọc (tùy chọn) */
}
</style>