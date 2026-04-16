<template>

    <div class="top-header">
      <div class="title-group">
        <h1 class="title">Lịch sử hoạt động</h1>
        <p class="subtitle">Theo dõi thời gian ra/vào ca của nhân viên</p>
      </div>
      <button class="btn-refresh" @click="handleRefresh">
        <i class="fas fa-sync-alt"></i> Làm mới
        </button>
    </div>

    <div class="main-content-card">
      
      <div class="custom-filter-bar">
        <div class="filter-input-group">
          <i class="fas fa-search"></i>
          <input type="text" v-model="searchEmployee" placeholder="Tìm tên nhân viên..." />
        </div>

        <div class="filter-input-group">
          <i class="far fa-clock"></i>
          <input type="text" v-model="searchShift" placeholder="Tìm tên ca làm việc..." />
        </div>

        <div class="filter-date-range">
          <input 
            type="text" 
            v-model="filterStartDate" 
            class="date-input" 
            placeholder="Ngày bắt đầu" 
            onfocus="this.type='date'" 
            onblur="if(!this.value) this.type='text'"
          />
          <span class="date-separator"><i class="fas fa-arrow-right"></i></span>
          <input 
            type="text" 
            v-model="filterEndDate" 
            class="date-input" 
            placeholder="Ngày kết thúc" 
            onfocus="this.type='date'" 
            onblur="if(!this.value) this.type='text'"
          />
        </div>

        <button class="btn-clear-filter" @click="clearFilters">
          <i class="fas fa-filter-slash" v-if="searchEmployee || searchShift || filterStartDate || filterEndDate"></i>
          <i class="fas fa-filter" v-else></i>
          Xóa bộ lọc
        </button>
      </div>

      <div class="table-container">
        <table class="standard-table">
          <thead>
            <tr>
              <th width="60">STT</th>
              <th>NHÂN VIÊN / CA LÀM VIỆC</th>
              <th>VÀO CA (THỰC TẾ)</th>
              <th>RA CA (THỰC TẾ)</th>
              <th>TRẠNG THÁI</th>
              <th>GHI CHÚ</th>
            </tr>
          </thead>
          <tbody v-if="!isLoading">
            <tr v-for="(item, index) in activities" :key="item.id">
            <td class="center">{{ (currentPage * pageSize) + index + 1 }}</td>
            <td>
                <div class="user-info">
                <div class="user-vatar">{{ item.tenNhanVien?.charAt(0) }}</div>
                <div class="user-detail">
                    <div class="u-name">{{ item.tenNhanVien }}</div>
                    <div class="u-sub">{{ item.tenCa }} – {{ item.maNhanVien }}</div>
                </div>
                </div>
            </td>
            <td>
                <div class="check-box">
                <i class="fas fa-sign-in-alt in"></i>
                <div class="t-wrap">
                    <span class="t-main">{{ formatTime(item.vaoCaThucTe) }}</span>
                    <span class="t-sub">{{ formatDate(item.vaoCaThucTe) }}</span>
                </div>
                </div>
            </td>
            <td>
                <div class="check-box">
                <i class="fas fa-sign-out-alt out"></i>
                <div class="t-wrap">
                    <span v-if="item.raCaThucTe" class="t-main">{{ formatTime(item.raCaThucTe) }}</span>
                    <span v-if="item.raCaThucTe" class="t-sub">{{ formatDate(item.raCaThucTe) }}</span>
                    <span v-else class="t-none">---</span>
                </div>
                </div>
            </td>
                <td>
                <span :class="['status-pill', getStatusClass(item.trangThai)]">
                  {{
                    item.trangThai === 1
                      ? 'Đang làm việc'
                      : item.trangThai === 2
                      ? 'Đóng ca sớm'
                      : 'Đã đóng ca'
                  }}
                </span>
                </td>
                <td class="note">{{ item.ghiChu || '---' }}</td>
            </tr>
            </tbody>
            <tbody v-else>
            <tr>
                <td colspan="6" class="center">Đang tải dữ liệu...</td></tr>
            </tbody>
        </table>
      </div>

      <div class="pagination-footer">
        <div class="show-limit">
          Hiện 
          <select v-model="pageSize" @change="handlePageSizeChange">
            <option :value="5">5</option>
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select> 
          dòng (Tổng: {{ totalElements }} bản ghi)
        </div>
        
        <div class="page-nav" v-if="totalPages > 0">
          <button class="p-btn" @click="prevPage" :disabled="currentPage === 0">
            <i class="fas fa-chevron-left"></i>
          </button>
          
          <button 
            v-for="page in totalPages" 
            :key="page"
            class="p-btn" 
            :class="{ active: currentPage === page - 1 }"
            @click="goToPage(page - 1)"
          >
            {{ page }}
          </button>
          
          <button class="p-btn" @click="nextPage" :disabled="currentPage >= totalPages - 1">
            <i class="fas fa-chevron-right"></i>
          </button>
        </div>
      </div>
    </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import axios from 'axios';

const pageSize = ref(10);
const currentPage = ref(0);
const totalElements = ref(0);
const totalPages = ref(0); // <== Thêm biến này để lưu tổng số trang

const searchEmployee = ref('');
const searchShift    = ref('');
const filterStartDate = ref('');
const filterEndDate   = ref('');

const activities = ref([]);
const isLoading  = ref(false);

const fetchActivities = async () => {
  isLoading.value = true;
  try {
    const resp = await axios.get('http://localhost:8080/api/v1/lich-su-hoat-dong', {
      params: {
        employee: searchEmployee.value,
        shift:    searchShift.value,
        // Sửa lại ngày tháng: Gửi thẳng chuỗi YYYY-MM-DD cho Spring Boot (LocalDate)
        startDate: filterStartDate.value || null,
        endDate:   filterEndDate.value || null,
        page: currentPage.value,
        size: pageSize.value
      }
    });
    console.log('API response:', resp.data); // Debug
    
    activities.value    = resp.data.content || [];
    totalElements.value = resp.data.totalElements || 0;
    totalPages.value    = resp.data.totalPages || 0; // <== Lấy tổng số trang từ API
  } catch (err) {
    console.error('Lỗi khi lấy dữ liệu:', err);
  } finally {
    isLoading.value = false;
  }
};

// --- CÁC HÀM XỬ LÝ PHÂN TRANG ---
const goToPage = (pageIndex) => {
  if (pageIndex >= 0 && pageIndex < totalPages.value) {
    currentPage.value = pageIndex;
    fetchActivities();
  }
};

const prevPage = () => goToPage(currentPage.value - 1);
const nextPage = () => goToPage(currentPage.value + 1);

const handlePageSizeChange = () => {
  currentPage.value = 0; // Reset về trang đầu khi đổi số lượng dòng
  fetchActivities();
};
// ---------------------------------

const getStatusClass = status => {
  if (status === 1) return 'status-working';
  if (status === 2) return 'status-early';
  return 'status-done';
};
const formatTime = dt => {
  if (!dt) return '---';
  const d = new Date(dt);
  return d.toLocaleTimeString('vi-VN', { hour:'2-digit', minute:'2-digit' });
};
const formatDate = dt => {
  if (!dt) return '';
  const d = new Date(dt);
  return d.toLocaleDateString('vi-VN');
};

const clearFilters = () => {
  searchEmployee.value = '';
  searchShift.value = '';
  filterStartDate.value = '';
  filterEndDate.value   = '';
  // Không cần gọi fetchActivities() ở đây vì watch đã xử lý
};

// Đặt { deep: true } nếu cần, nhưng mảng phụ thuộc này sẽ kích hoạt khi thay đổi
watch([searchEmployee, searchShift, filterStartDate, filterEndDate], () => {
  currentPage.value = 0; // Khi lọc, luôn đưa về trang 0
  fetchActivities();
});

const handleRefresh = () => {
  currentPage.value = 0;
  fetchActivities();
};

onMounted(fetchActivities);
</script>

<style scoped>
/* Nền xám nhạt toàn trang */
.page-wrapper { background: #f4f6f8; padding: 30px; min-height: 100vh; font-family: 'Inter', sans-serif; }

/* Header bên ngoài card */
.top-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
.title { font-size: 24px; font-weight: 700; color: #334155; margin: 0; }
.subtitle { font-size: 13px; color: #94a3b8; margin: 5px 0 0; }
.btn-refresh { background: #fff; border: 1px solid #e2e8f0; padding: 10px 15px; border-radius: 8px; cursor: pointer; font-weight: 600; color: #475569; }

/* MAIN CARD DUY NHẤT */
.main-content-card { background: #fff; border-radius: 12px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); padding: 25px; }

/* Section Lọc */
.filter-row { display: flex; gap: 15px; align-items: flex-end; margin-bottom: 30px; }
.filter-input-group label { display: block; font-size: 11px; font-weight: 700; color: #64748b; margin-bottom: 8px; }
.input-inner { position: relative; width: 220px; }
.input-inner i { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #cbd5e1; }
.input-inner input { width: 100%; padding: 10px 10px 10px 35px; border: 1px solid #e2e8f0; border-radius: 8px; outline: none; font-size: 14px; }

.range-inputs { display: flex; align-items: center; border: 1px solid #e2e8f0; border-radius: 8px; padding: 0 10px; height: 40px; }
.range-inputs input { border: none; outline: none; width: 100px; font-size: 13px; text-align: center; }
.arrow { color: #cbd5e1; padding: 0 10px; }

.btn-clear { height: 40px; padding: 0 20px; border: 1px solid #e2e8f0; background: #fff; border-radius: 8px; color: #64748b; font-weight: 600; cursor: pointer; }

/* BẢNG: Đã thêm Container để tạo khoảng cách với lề Card */
.table-container { border: 1px solid #f1f5f9; border-radius: 10px; overflow: hidden; margin-bottom: 20px; }
.standard-table { width: 100%; border-collapse: collapse; background: #fff; }

/* Header bảng: MÀU TRẮNG XÁM NHẸ, CHỮ ĐEN - GIỐNG ẢNH MẪU */
.standard-table thead th { background: #f8fafc; color: #475569; padding: 15px; text-align: left; font-size: 12px; font-weight: 700; border-bottom: 1px solid #e2e8f0; text-transform: uppercase; }
.standard-table tbody td { padding: 15px; border-bottom: 1px solid #f8fafc; font-size: 14px; color: #1e293b; }

/* Thành phần trong dòng */
.user-info { display: flex; align-items: center; gap: 10px; }
.user-vatar { width: 32px; height: 32px; background: #1e293b; color: #fff; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 12px; }
.u-name { font-weight: 700; }
.u-sub { font-size: 11px; color: #94a3b8; }

.check-box { display: flex; align-items: center; gap: 10px; }
.in { color: #10b981; }
.out { color: #ef4444; }
.t-main { font-weight: 700; display: block; }
.t-sub { font-size: 11px; color: #94a3b8; }
.t-none { color: #cbd5e1; }

.status-pill { background: #eff6ff; color: #3b82f6; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }

/* Phân trang */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; }
.show-limit { font-size: 13px; color: #64748b; }
.page-nav { display: flex; gap: 5px; }
.p-btn { width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 6px; cursor: pointer; color: #64748b; }
.p-btn.active { background: #1e293b; color: #fff; border-color: #1e293b; }

.center { text-align: center; }
.note { color: #94a3b8; }
.custom-filter-bar {
  display: flex;
  gap: 15px;
  padding: 15px 20px;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
}

.filter-input-group {
  position: relative;
  flex: 1.2;
}

.filter-input-group i {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
  font-size: 13px;
}

.filter-input-group input {
  width: 100%;
  padding: 8px 12px 8px 32px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  outline: none;
  font-size: 14px;
  transition: border-color 0.2s;
}

.filter-input-group input:focus {
  border-color: #2563eb;
}

.filter-date-range {
  display: flex;
  align-items: center;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 0 5px;
  flex: 1.5;
  background: #fff;
}

.filter-date-range .date-input {
  border: none;
  padding: 8px;
  outline: none;
  font-size: 13px;
  color: #4b5563;
  width: 100%;
  background: transparent;
}

.date-separator {
  color: #9ca3af;
  font-size: 12px;
  padding: 0 5px;
}

.btn-clear-filter {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  background-color: #fff;
  border: 1px dashed #d1d5db;
  border-radius: 6px;
  color: #6b7280;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-clear-filter:hover {
  border-color: #ef4444;
  color: #ef4444;
  background-color: #fef2f2;
}
</style>