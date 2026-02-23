<template>
    <h1 class="page-title">Ca làm việc</h1>

    <div class="card">
      <div class="top-bar">
        <div class="search-box">
          <i class="fas fa-search"></i>
          <input type="text" v-model="keyword" placeholder="Tìm kiếm..." />
        </div>
        <div class="action-group">
        <button class="btn btn-navy" @click="resetFilter">
            <font-awesome-icon :icon="['fas', 'sync-alt']" /> Đặt lại
          </button>
        <button class="btn-add" @click="router.push({ name: 'admin-shift-create' })">
          <i class="fas fa-plus"></i> Thêm ca làm việc
        </button>
        </div>
      </div>

      <div class="filter-row">
        <div class="filter-group">
          <label>Trạng thái</label>
          <select v-model="status">
            <option value="">Tất cả</option>
            <option value="1">Hoạt động</option>
            <option value="0">Ngừng hoạt động</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Thời gian bắt đầu</label>
          <input type="time" v-model="startTime" />
        </div>
        <div class="filter-group">
          <label>Thời gian kết thúc</label>
          <input type="time" v-model="endTime" />
        </div>
      </div>

      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th>STT</th>
              <th>Tên ca</th>
              <th>Giờ bắt đầu</th>
              <th>Giờ kết thúc</th>
              <th>Trạng thái</th>
              <th class="text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(shift, index) in shifts" :key="shift.id">
              <td>{{ index + 1 }}</td>
              <td class="font-medium">{{ shift.tenCa }}</td>
              <td>{{ shift.thoiGianBatDau }}</td>
              <td>{{ shift.thoiGianKetThuc }}</td>
              <td class="text-center">
              <span class="badge" :class="shift.trangThai === 1 ? 'badge-active' : 'badge-stopped'">
                {{ shift.trangThai === 1 ? 'Hoạt động' : 'Ngừng' }}
              </span>
            </td>
              <td class="text-center action-col">
              <div class="action-wrapper">
                <label class="switch" title="Bật/Tắt trạng thái">
                  <input 
                    type="checkbox" 
                    :checked="shift.trangThai === 1" 
                    @click="toggleStatus(shift, $event)"
                  >
                  <span class="slider round"></span>
                </label>
                <button class="icon-btn" @click="editShift(shift)" title="Xem chi tiết">
                  <i class="far fa-eye"></i>
                </button>
              </div>
            </td>
            </tr>
            <tr v-if="shifts.length === 0">
              <td colspan="7" class="text-center py-4">Chưa có dữ liệu ca làm việc</td>
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
          Ca làm việc / trang
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
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'; // Nhớ import watch
import { useRouter } from 'vue-router'; 
import request from '@/services/request'; 
import Swal from 'sweetalert2';

const router = useRouter();

// === State quản lý dữ liệu ===
const shifts = ref([]);
const loading = ref(false);

// === State phân trang ===
const pageSize = ref(5);
const currentPage = ref(0);
const totalPages = ref(0);

// === State bộ lọc ===
const keyword = ref('');
const status = ref(''); // Để chuỗi rỗng mặc định để khớp với value="" trong thẻ select
const startTime = ref(''); 
const endTime = ref('');

// === Hàm gọi API chính lấy danh sách ca làm việc ===
const fetchShifts = async () => {
  loading.value = true;
  try {
    const response = await request.get('/ca-lam-viec', {
      params: {
        keyword: keyword.value || null, 
        status: status.value !== '' ? status.value : null, 
        startTime: startTime.value || null,
        endTime: endTime.value || null,
        page: currentPage.value, // Gửi số trang hiện tại
        size: pageSize.value     // Gửi kích thước trang
      }
    }); 
    
    // Gán dữ liệu trả về từ Spring Boot Page
    shifts.value = response.data.content || []; 
    totalPages.value = response.data.totalPages || 0;
  } catch (error) {
    console.error("Lỗi khi tải danh sách ca làm việc:", error);
  } finally {
    loading.value = false;
  }
};

// === Lắng nghe sự thay đổi của bộ lọc để tự động gọi API ===
watch([keyword, status, startTime, endTime], () => {
  currentPage.value = 0; // Khi đổi điều kiện lọc, luôn quay về trang đầu tiên
  fetchShifts();
});

// === Logic Phân trang ===
const visiblePages = computed(() => {
    const pages = [];
    const current = currentPage.value + 1; // Backend đếm từ 0, UI hiện từ 1
    for (let i = 1; i <= totalPages.value; i++) {
        if (i === 1 || i === totalPages.value || (i >= current - 1 && i <= current + 1)) {
            pages.push(i);
        }
    }
    return [...new Set(pages)].sort((a, b) => a - b); // Loại bỏ trùng lặp và sắp xếp
});

const changePage = (p) => { 
  if (p >= 0 && p < totalPages.value) { 
    currentPage.value = p; 
    fetchShifts(); // Gọi đúng hàm của Ca làm việc
  } 
};

const handlePageSizeChange = () => { 
  currentPage.value = 0; 
  fetchShifts(); // Gọi đúng hàm của Ca làm việc
};

// === Hành động trên từng dòng ===
const toggleStatus = async (shift, event) => {
  event.preventDefault(); 
  const newStatus = shift.trangThai === 1 ? 0 : 1;
  const actionText = newStatus === 1 ? 'Kích hoạt' : 'Ngừng hoạt động';
  
  const result = await Swal.fire({
    title: `Xác nhận ${actionText}?`,
    text: `Bạn có muốn ${actionText.toLowerCase()} ca làm việc "${shift.tenCa}"?`,
    icon: 'question', 
    showCancelButton: true,
    confirmButtonText: 'Đồng ý', 
    cancelButtonText: 'Hủy'
  });

  if (result.isConfirmed) {
    try {
      await request.put(`/ca-lam-viec/${shift.id}/trang-thai`, null, {
        params: { trangThai: newStatus }
      });
      shift.trangThai = newStatus; // Cập nhật UI ngay lập tức
      Swal.fire({
        icon: 'success', 
        title: 'Thành công', 
        text: 'Cập nhật trạng thái thành công!', 
        timer: 1500, 
        showConfirmButton: false
      });
    } catch (error) {
      Swal.fire('Lỗi', 'Không thể cập nhật trạng thái', 'error');
    }
  }
};

const editShift = (shift) => {
    router.push({ 
        name: 'admin-shift-edit', 
        params: { id: shift.id } 
    });
};
const resetFilter = () => {
    keyword.value = '';
    status.value = '';
    startTime.value = '';
    endTime.value = '';
    currentPage.value = 0;
    fetchShifts();
};
// === Chạy khi trang vừa tải lên ===
onMounted(() => {
  fetchShifts();
});
</script>

<style scoped>
/* BADGES */
.badge { padding: 4px 12px; border-radius: 20px; font-size: 11px; font-weight: 600; white-space: nowrap; border: 1px solid transparent; }
.badge-active { background: #dcfce7; color: #166534; border-color: #bbf7d0; }
.badge-stopped { background: #fee2e2; color: #991b1b; border-color: #fecaca; }

.badge-admin { background: #dbeafe; color: #1e40af; border-color: #bfdbfe; }
.badge-staff { background: #f3f4f6; color: #4b5563; border-color: #e5e7eb; }
/* TOGGLE SWITCH */
.switch { position: relative; display: inline-block; width: 36px; height: 20px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #cbd5e1; transition: .4s; border-radius: 34px; }
.slider:before { position: absolute; content: ""; height: 14px; width: 14px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; }
input:checked + .slider { background-color: #10b981; }
input:checked + .slider:before { transform: translateX(16px); }
.action-wrapper { display: flex; align-items: center; justify-content: center; gap: 10px; }
/* Reset cơ bản cho component */
* {
  box-sizing: border-box;
}

.shift-container {
  padding: 24px;
  background-color: #f8f9fa; /* Màu nền xám nhạt */
  min-height: 100vh;
  font-family: Arial, sans-serif;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #1e293b;
  margin-bottom: 24px;
  margin-top: 0;
}

.card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 24px;
  border: 1px solid #f1f5f9;
}

/* Header: Search & Button */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.search-box {
  position: relative;
  width: 350px;
}

.search-box input {
  width: 100%;
  padding: 10px 16px 10px 40px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  outline: none;
  font-size: 14px;
  transition: border-color 0.2s;
}

.search-box input:focus {
  border-color: #3b82f6;
}

.search-box i {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}

.btn-add {
  padding: 10px 20px;
  border: 2px solid #1e293b;
  background: #ffffff;
  color: #1e293b;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  transition: background 0.2s;
}
.action-group { display: flex; gap: 10px; }

.btn-add:hover {
  background: #f8fafc;
}

/* Filters */
.filter-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 24px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group label {
  font-size: 14px;
  font-weight: 500;
  color: #475569;
}

.filter-group input,
.filter-group select {
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  outline: none;
  font-size: 14px;
  background-color: #ffffff;
}

.filter-group input:focus,
.filter-group select:focus {
  border-color: #3b82f6;
}

/* Table */
.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

th, td {
  padding: 16px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
}

th {
  background-color: #f8fafc;
  font-weight: 600;
  color: #475569;
  text-transform: uppercase;
  font-size: 13px;
  border-top: 1px solid #f1f5f9;
}

tr:hover td {
  background-color: #f8fafc;
}

.text-center {
  text-align: center;
}

.font-medium {
  font-weight: 500;
  color: #1e293b;
}

/* Badges */
.badge {
  padding: 4px 12px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}

.badge-success {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
}

/* Icon button in table */
.btn-icon {
  background: none;
  border: none;
  color: #64748b;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  transition: all 0.2s;
}

.btn-icon:hover {
  background-color: #eff6ff;
  color: #3b82f6;
}

/* Pagination */
.pagination-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  color: #475569;
  font-size: 14px;
}

.page-size select {
  padding: 4px 8px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  margin: 0 4px;
  outline: none;
}

.page-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #475569;
}

.page-btn.active {
  background: #eff6ff;
  color: #2563eb;
  border-color: #bfdbfe;
  font-weight: bold;
}

.page-btn:hover:not(.active) {
  background: #f1f5f9;
}

.page-dots {
  padding: 0 8px;
}
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