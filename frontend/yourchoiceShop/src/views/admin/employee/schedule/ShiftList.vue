<template>
  <div class="shift-container">
    <h1 class="page-title">Ca làm việc</h1>

    <div class="card">
      <div class="top-bar">
        <div class="search-box">
          <i class="fas fa-search"></i>
          <input type="text" placeholder="Tìm kiếm..." />
        </div>
        
        <button class="btn-add" @click="router.push({ name: 'admin-shift-create' })">
          <i class="fas fa-plus"></i> Thêm ca làm việc
        </button>
      </div>

      <div class="filter-row">
        <div class="filter-group">
          <label>Trạng thái</label>
          <select>
            <option value="">Chọn trạng thái</option>
            <option value="active">Hoạt động</option>
            <option value="inactive">Ngừng hoạt động</option>
          </select>
        </div>
        <div class="filter-group">
          <label>Thời gian bắt đầu</label>
          <input type="time" />
        </div>
        <div class="filter-group">
          <label>Thời gian kết thúc</label>
          <input type="time" />
        </div>
      </div>

      <div class="table-container">
        <table>
          <thead>
            <tr>
              <th class="text-center" style="width: 50px;">
                <input type="checkbox" />
              </th>
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
              <td class="text-center"><input type="checkbox" /></td>
              <td>{{ index + 1 }}</td>
              <td class="font-medium">{{ shift.tenCa }}</td>
              <td>{{ shift.thoiGianBatDau }}</td>
              <td>{{ shift.thoiGianKetThuc }}</td>
              <td>
                <span class="badge badge-success">Hoạt động</span>
              </td>
              <td class="text-center">
                <button class="btn-icon"><i class="fas fa-eye"></i></button>
                <button class="btn-icon text-red-500" @click="handleDelete(shift.id)"><i class="fas fa-trash"></i></button>
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
import { ref, reactive, onMounted, computed } from 'vue'; 
import { useRouter, useRoute } from 'vue-router'; 
import request from '@/services/request'; // Import file cấu hình axios của bạn
import axios from 'axios'; 
import Swal from 'sweetalert2';

const router = useRouter();
const shifts = ref([]);
const pageSize = ref(5);
const currentPage = ref(0);
const totalPages = ref(0);
// Hàm gọi API lấy danh sách ca làm việc
const fetchShifts = async () => {
  try {
    const response = await request.get('/ca-lam-viec'); // Thay bằng URL API của bạn
    shifts.value = response.data; 
  } catch (error) {
    console.error("Lỗi khi tải danh sách ca làm việc:", error);
  }
};

// Hàm xóa (Ví dụ thêm)
const handleDelete = async (id) => {
    const result = await Swal.fire({
        title: 'Xóa ca làm việc?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    });
    if(result.isConfirmed) {
        // await request.delete(`/ca-lam-viec/${id}`);
        // fetchShifts();
        Swal.fire('Đã xóa!', '', 'success');
    }
}
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
const changePage = (p) => { if (p >= 0 && p < totalPages.value) { currentPage.value = p; fetchEmployees(); } };
const handlePageSizeChange = () => { currentPage.value = 0; fetchEmployees(); };
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

// Chạy hàm fetchShifts ngay khi trang được render
onMounted(() => {
  fetchShifts();
});
</script>

<style scoped>
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