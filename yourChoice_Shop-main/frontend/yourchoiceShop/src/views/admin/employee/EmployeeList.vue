<template>
  <BasePage>
    
    <template #header>
      <h2 class="breadcrumb">Nhân viên <span class="divider">/</span> <span class="current">Danh sách nhân viên</span></h2>
    </template>

    <template #toolbar>
      <div class="search-box">
        <div class="search-icon"><i class="fa-solid fa-magnifying-glass"></i></div>
        
        <input 
            type="text" 
            placeholder="Tìm theo tên, SĐT..." 
            v-model="searchQuery"
            class="form-control search-input"
        />
      </div>
      <div class="action-buttons">
        <button class="btn-outline" @click="exportExcel">Xuất Excel</button>
        <button class="btn-primary" @click="goToCreate">＋ Tạo nhân viên</button>
      </div>
    </template>

    <template #filter>
      <div class="filter-item">
        <label>Giới tính:</label>
        <select v-model="filters.gender" class="form-control filter-select" @change="handleFilterChange">
          <option value="">Tất cả</option>
          <option :value="true">Nam</option>
          <option :value="false">Nữ</option>
        </select>
      </div>
    
    <div class="filter-item">
      <label>Trạng thái:</label>
      <select v-model="filters.status" class="form-control filter-select" @change="handleFilterChange">
         <option value="">Tất cả</option>
         <option value="1">Hoạt động</option>
         <option value="0">Ngừng HĐ</option>
      </select>
    </div>
    </template>

    <template #default>
      <div class="table-responsive">
        <table class="employee-table">
          <thead>
            <tr>
              <th width="50">STT</th>
              <th width="80">Ảnh</th>
              <th>Mã NV</th>
              <th>Họ tên</th>
              <th>Email</th>
              <th>Địa chỉ</th>
              <th>SĐT</th>
              <th>Ngày sinh</th>
              <th>Giới tính</th>
              <th>Chức vụ</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="employees.length === 0">
                <td colspan="12" style="text-align: center; padding: 20px; color: #888;">
                    Không tìm thấy nhân viên nào.
                </td>
            </tr>

            <tr v-for="(emp, index) in employees" :key="emp.id">
              <td>{{ index + 1 + (currentPage * pageSize) }}</td>
              <td>
                <img 
                      class="avatar-image"
                      :src="generateImageUrl(emp.hinhAnh)" 
                      @error="handleImageError"
                      alt="Avatar"
                  />
              </td>
              <td>{{ emp.maNhanVien }}</td>
              <td class="fw-bold">{{ emp.tenNhanVien }}</td>
              <td>{{ emp.email }}</td>
              <td>
                <div class="address-col" :title="formatAddress(emp.diaChi)">
                    {{ formatAddress(emp.diaChi) }}
                </div>
              </td>
              <td>{{ emp.soDienThoai }}</td>
              <td>{{ emp.ngaySinh }}</td>
              <td>{{ emp.gioiTinh ? 'Nam' : 'Nữ' }}</td>
              <td>{{ emp.idChucVu?.tenChucVu || "Nhân viên" }}</td>
              <td>
                <span :class="['status-badge', emp.trangThai === 1 ? 'status-active' : 'status-inactive']">
                  {{ emp.trangThai === 1 ? 'Hoạt động' : 'Ngừng hoạt động' }}
                </span>
              </td>
              <td>
                <div class="status-wrapper" @click="toggleStatus(emp)">
                  <i 
                    class="fa-solid toggle-icon"
                    :class="emp.trangThai === 1 ? 'fa-toggle-on active' : 'fa-toggle-off inactive'"
                  ></i>
                  </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </template>

    <template #footer>
        <div class="page-size">
           Xem 
           <select v-model="pageSize" @change="fetchEmployees" class="form-control size-select">
                <option :value="5">5</option>
                <option :value="10">10</option>
                <option :value="20">20</option>
           </select> 
           nhân viên
        </div>
        <div class="pagination-controls">
           <button class="page-btn" :disabled="currentPage === 0" @click="changePage(currentPage - 1)">‹</button>
           <span class="page-info">Trang {{ currentPage + 1 }} / {{ totalPages }}</span>
           <button class="page-btn" :disabled="currentPage >= totalPages - 1" @click="changePage(currentPage + 1)">›</button>
        </div>
    </template>

  </BasePage>
</template>

<script setup>
import BasePage from '@/views/BasePage.vue';
import { ref, reactive, onMounted, watch } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();

// --- STATE QUẢN LÝ DỮ LIỆU ---
const employees = ref([]);
const searchQuery = ref('');
const pageSize = ref(5);
const currentPage = ref(0);
const totalPages = ref(0);
let timeout = null; 

const filters = reactive({
  gender: '',
  status: ''
});

// 1. Hàm tạo link ảnh (An toàn)
const generateImageUrl = (imageName) => {
    if (!imageName || imageName === 'null' || imageName === '') {
        return 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7'; 
    }
    return `http://localhost:8080/api/v1/nhan-vien/images/${imageName}`;
}

// 2. Hàm xử lý khi ảnh lỗi
const handleImageError = (e) => {
    e.target.onerror = null; 
    e.target.src = "https://cdn-icons-png.flaticon.com/512/1077/1077114.png";
}

// --- API ACTIONS ---

// 1. Lấy danh sách nhân viên
const fetchEmployees = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/v1/nhan-vien', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        keyword: searchQuery.value,
        gender: filters.gender,
        status: filters.status  
      }
    });
    
    employees.value = response.data.content || [];

    if (response.data.page) {
        totalPages.value = response.data.page.totalPages;
    } else {
        totalPages.value = response.data.totalPages || 0;
    }

  } catch (error) {
    console.error("Lỗi API:", error);
  }
};

// 2. Xử lý xóa mềm (Soft Delete)
const softDeleteEmployee = async (emp) => {
    const confirmDelete = confirm(`Bạn có chắc muốn xóa nhân viên "${emp.tenNhanVien}" không?`);
    if (!confirmDelete) return;

    try {
        await axios.delete(`http://localhost:8080/api/v1/nhan-vien/${emp.id}`);
        emp.trangThai = 0; 
        alert("Đã xóa thành công!");
    } catch (error) {
        console.error(error);
        alert("Có lỗi xảy ra khi xóa nhân viên!");
    }
};

// 3. Xử lý bật tắt trạng thái
const toggleStatus = async (emp) => {
    const oldStatus = emp.trangThai;
    const newStatus = oldStatus === 1 ? 0 : 1;

    emp.trangThai = newStatus;

    try {
        await axios.put(`http://localhost:8080/api/v1/nhan-vien/${emp.id}/trang-thai`, null, {
            params: { trangThai: newStatus }
        });
        console.log(`Đã đổi trạng thái nhân viên ${emp.tenNhanVien} thành công.`);
    } catch (error) {
        console.error("Lỗi cập nhật trạng thái:", error);
        emp.trangThai = oldStatus; 
        alert("Lỗi kết nối! Không thể cập nhật trạng thái.");
    }
};

// 4. XUẤT EXCEL (ĐÃ CẬP NHẬT)
const exportExcel = async () => {
    try {
        // Lấy các tham số filter hiện tại để xuất đúng dữ liệu đang xem
        const params = {
            keyword: searchQuery.value,
            gioiTinh: filters.gender, // Lưu ý: Backend thường đặt tên biến là gioitinh
            trangThai: filters.status
        };

        // Gọi API với responseType là 'blob' để nhận file binary
        const response = await axios.get('http://localhost:8080/api/v1/nhan-vien/export-excel', {
            params: params,
            responseType: 'blob' 
        });

        // Tạo URL ảo từ dữ liệu Blob trả về
        const url = window.URL.createObjectURL(new Blob([response.data]));
        
        // Tạo thẻ a ẩn để kích hoạt tải xuống
        const link = document.createElement('a');
        link.href = url;
        const fileName = `Danh_Sach_Nhan_Vien_${new Date().toISOString().slice(0,10)}.xlsx`;
        link.setAttribute('download', fileName);
        
        document.body.appendChild(link);
        link.click();
        
        // Dọn dẹp bộ nhớ
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);

    } catch (error) {
        console.error("Lỗi xuất Excel:", error);
        alert("Lỗi: Không thể tải file Excel. Vui lòng kiểm tra Server!");
    }
};

// --- HELPER FUNCTIONS ---

const formatAddress = (addr) => {
  if (!addr) return "Chưa cập nhật";
  let cleanAddr = addr.replace(/null/gi, ""); 
  cleanAddr = cleanAddr.replace(/(,\s*)+/g, ", ").trim(); 
  cleanAddr = cleanAddr.replace(/^,\s*|,\s*$/g, ""); 
  return cleanAddr || "Chưa cập nhật";
};

// --- NAVIGATION & EVENTS ---

const changePage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page;
    fetchEmployees();
  }
};

const goToCreate = () => {
  router.push({ name: 'admin-employee-create' });
};

const handleFilterChange = () => {
    currentPage.value = 0; 
    fetchEmployees();      
};

// --- WATCHERS & MOUNTED ---

watch(searchQuery, () => {
    clearTimeout(timeout);
    timeout = setTimeout(() => {
        currentPage.value = 0;
        fetchEmployees();
    }, 500);
});

onMounted(() => {
  fetchEmployees();
});
</script>

<style scoped>
/* --- CSS CÁC PHẦN TỬ UI (BUTTON, INPUT, TABLE) --- */

/* Breadcrumb Styling */
.breadcrumb { font-size: 18px; color: #333; font-weight: bold; margin: 0; }
.breadcrumb .divider { color: #999; margin: 0 5px; }
.breadcrumb .current { color: #6c757d; }

/* Search Box */
.search-box { position: relative; width: 350px; }
.search-icon { position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: #999; }
.search-input { padding-left: 30px !important; width: 100%; }

/* Buttons */
.action-buttons { display: flex; gap: 10px; }
.btn-primary { background-color: #2c3e50; color: white; border: none; padding: 8px 16px; border-radius: 4px; font-weight: bold; cursor: pointer; display: flex; align-items: center; gap: 5px; }
.btn-primary:hover { background-color: #1a252f; }
.btn-outline { background-color: white; color: #2c3e50; border: 1px solid #2c3e50; padding: 8px 16px; border-radius: 4px; font-weight: bold; cursor: pointer; }
.btn-outline:hover { background-color: #f0f4f8; }

/* Filters */
.filter-item { display: flex; align-items: center; gap: 10px; font-size: 14px; font-weight: bold; color: #333; }
.filter-select { border: none; cursor: pointer; background: transparent; color: #666; font-weight: normal; }
.filter-select:hover { color: #2c3e50; }

/* Form Control */
.form-control { padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; outline: none; }
.form-control:focus { border-color: #2c3e50; }

/* Table */
.table-responsive { overflow-x: auto; }
.employee-table { width: 100%; border-collapse: collapse; }
.employee-table th { text-align: left; padding: 12px 10px; background-color: #f8f9fa; color: #333; font-weight: bold; border-bottom: 2px solid #eee; font-size: 14px; }
.employee-table td { padding: 12px 10px; border-bottom: 1px solid #eee; color: #555; font-size: 14px; vertical-align: middle; }
.employee-table tr:hover { background-color: #f9f9f9; }
.table-avatar { width: 40px; height: 40px; border-radius: 50%; object-fit: cover; border: 1px solid #ddd; }
.fw-bold { font-weight: 600; color: #333; }

/* Status Badges */
.status-badge { padding: 4px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
.status-active { background-color: #e6fffa; color: #28a745; border: 1px solid #b7eb8f; }
.status-inactive { background-color: #fff1f0; color: #cf1322; border: 1px solid #ffa39e; }

/* Action Icons */
.action-icons { display: flex; gap: 8px; }
.icon-btn { background: none; border: none; cursor: pointer; font-size: 16px; padding: 4px; opacity: 0.7; transition: 0.2s; }
.icon-btn:hover { opacity: 1; background-color: #eee; border-radius: 4px; }
.view { color: #1890ff; }
.edit { color: #faad14; }
.delete { color: #ff4d4f; }

/* Pagination Controls */
.page-size { font-size: 14px; color: #666; display: flex; align-items: center; gap: 5px; }
.size-select { padding: 4px 8px; width: auto; }
.pagination-controls { display: flex; gap: 5px; align-items: center; }
.page-info { font-size: 14px; color: #666; padding: 0 10px; }
.page-btn { background: white; border: 1px solid #ddd; padding: 5px 12px; border-radius: 4px; cursor: pointer; color: #666; }
.page-btn:hover:not(:disabled) { background-color: #f0f0f0; }
.page-btn:disabled { background-color: #f5f5f5; color: #ccc; cursor: not-allowed; }
.btn-icon {
  border: none;
  background: none;
  font-size: 1.2rem;
  cursor: pointer;
  transition: transform 0.2s;
  margin: 0 4px; /* Cách nhau ra một chút */
}

.btn-icon:hover {
  transform: scale(1.2);
}

/* Màu cho nút sửa */
.edit {
    color: #f59e0b; /* Màu cam */
}

/* Màu cho nút xóa */
.delete {
    color: #ef4444; /* Màu đỏ */
}
/* Style cho cái nút trạng thái trên bảng */
.status-badge {
    padding: 5px 10px;
    border-radius: 15px;
    cursor: pointer;
    font-size: 0.9rem;
    display: inline-block;
    transition: all 0.2s;
}
.status-badge:hover {
    opacity: 0.8;
    transform: scale(1.05);
}
.status-badge.active { background-color: #d1fae5; color: #065f46; } /* Xanh lá */
.status-badge.inactive { background-color: #f3f4f6; color: #374151; } /* Xám */

/* --- MODAL CSS --- */

/* Lớp phủ mờ đen toàn màn hình */
.modal-overlay {
    position: fixed;
    top: 0; left: 0; width: 100%; height: 100%;
    background: rgba(0, 0, 0, 0.5); /* Màu đen mờ 50% */
    display: flex;
    justify-content: center; /* Căn giữa ngang */
    align-items: center;     /* Căn giữa dọc */
    z-index: 1000;           /* Nổi lên trên cùng */
}

/* Cái khung trắng (Frame) */
.modal-content {
    background: white;
    padding: 20px;
    border-radius: 8px;
    width: 400px; /* Độ rộng của modal */
    box-shadow: 0 4px 10px rgba(0,0,0,0.2);
    animation: fadeIn 0.3s;
}

/* Header */
.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #eee;
    padding-bottom: 10px;
    margin-bottom: 15px;
}
.modal-header h3 { margin: 0; font-size: 1.2rem; }
.close-btn { cursor: pointer; font-size: 1.5rem; color: #aaa; }
.close-btn:hover { color: black; }

/* Body */
.status-options {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin: 15px 0;
}
.option-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px;
    border: 1px solid #eee;
    border-radius: 5px;
    cursor: pointer;
}
.option-item:hover { background-color: #f9f9f9; }

.text-success { color: #16a34a; font-weight: bold; }
.text-danger { color: #dc2626; font-weight: bold; }

/* Footer (Buttons) */
.modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 20px;
    padding-top: 10px;
    border-top: 1px solid #eee;
}

.btn-primary {
    background-color: #2563eb;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
}
.btn-secondary {
    background-color: white;
    color: #333;
    border: 1px solid #ccc;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
}

/* Hiệu ứng hiện ra nhẹ nhàng */
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(-20px); }
    to { opacity: 1; transform: translateY(0); }
}
/* --- CSS CHO SWITCH TOGGLE --- */
.status-toggle {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Khung ngoài của switch */
.switch {
  position: relative;
  display: inline-block;
  width: 44px;  /* Chiều rộng nút */
  height: 24px; /* Chiều cao nút */
}

/* Ẩn input checkbox mặc định */
.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

/* Thanh trượt (Slider) */
.slider {
  position: absolute;
  cursor: pointer;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: #ccc; /* Màu xám khi tắt */
  transition: .4s;
  border-radius: 34px;
}

/* Cái chấm tròn trắng bên trong */
.slider:before {
  position: absolute;
  content: "";
  height: 18px; /* Chiều cao chấm */
  width: 18px;  /* Chiều rộng chấm */
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: .4s;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

/* Khi Input được Checked (Bật) */
input:checked + .slider {
  background-color: #2c3e50; /* Màu xanh Navy chủ đạo (hoặc dùng #28a745 xanh lá) */
}

/* Di chuyển chấm tròn khi bật */
input:checked + .slider:before {
  transform: translateX(20px);
}

/* Màu chữ trạng thái bên cạnh */
.status-text {
  font-size: 13px;
  font-weight: 600;
}
.status-text.active { color: #28a745; }
.status-text.inactive { color: #999; }
/* Container bao quanh để căn chỉnh */
.status-wrapper {
  display: flex;
  align-items: center;
  gap: 8px; /* Khoảng cách giữa icon và chữ */
  cursor: pointer;
  width: fit-content; /* Để vùng click vừa khít nội dung */
  transition: opacity 0.2s;
}

.status-wrapper:hover {
  opacity: 0.8; /* Hiệu ứng hover nhẹ */
}

/* Style chung cho Icon Toggle */
.toggle-icon {
  font-size: 24px; /* Kích thước icon to rõ */
  transition: color 0.3s ease;
}

/* Trạng thái ON (Hoạt động) - Màu xanh */
.toggle-icon.active {
  color: #2c3e50; /* Hoặc dùng màu xanh lá: #28a745 */
}

/* Trạng thái OFF (Ngừng HĐ) - Màu xám */
.toggle-icon.inactive {
  color: #adb5bd; /* Màu xám nhạt */
}

/* Style cho chữ bên cạnh (nếu dùng) */
.status-text {
  font-size: 14px;
  font-weight: 600;
  user-select: none; /* Không cho bôi đen chữ khi click nhanh */
}

.text-active { color: #2c3e50; }
.text-inactive { color: #999; }
</style>