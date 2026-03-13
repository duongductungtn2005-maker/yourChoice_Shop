<template>
  <div class="schedule-container">
    <div class="page-header">
      <div class="title-section">
        
        <div>
          <h1 class="page-title">Xếp lịch nhân viên</h1>
          <p class="page-subtitle">Quản lý và phân ca làm việc</p>
        </div>
      </div>
      
      <div class="action-section">
        <div class="view-toggle">
          <button 
            :class="['toggle-btn', { active: currentView === 'calendar' }]"
            @click="currentView = 'calendar'"
          >
            <i class="fas fa-th"></i> Lịch biểu
          </button>
          <button 
            :class="['toggle-btn', { active: currentView === 'list' }]"
            @click="currentView = 'list'"
          >
            <i class="fas fa-list"></i> Danh sách
          </button>
        </div>

        <div class="action-buttons-group">
          <button class="btn btn-magic" @click="handleCopyLastWeek">
            <i class="fas fa-copy"></i> Sao chép tuần trước
          </button>

          <div class="dropdown-container">
            <button class="btn btn-excel" @click="showExcelMenu = !showExcelMenu">
              <i class="fas fa-file-excel"></i> Excel <i class="fas fa-chevron-down ml-1"></i>
            </button>
            
            <div class="dropdown-menu" v-if="showExcelMenu">
              <a class="dropdown-item" href="#" @click.prevent="downloadTemplate">
                <i class="fas fa-download"></i> Tải Template
              </a>
              <a class="dropdown-item" href="#" @click.prevent="triggerFileUpload">
                <i class="fas fa-upload"></i> Nhập dữ liệu
              </a>
            </div>
          </div>

          <input 
            type="file" 
            ref="fileInput" 
            accept=".xlsx, .xls" 
            style="display: none" 
            @change="handleFileUpload"
          />

          <button class="btn btn-refresh icon-only" @click="fetchSchedules" title="Làm mới dữ liệu">
            <i class="fas fa-sync-alt"></i>
          </button>
        </div>
      </div>
    </div>
    
    <div class="date-navigation">
      <div class="date-controls">
        <button class="nav-btn" @click="prevWeek"><i class="fas fa-chevron-left"></i></button>
        <div class="date-picker-box">
          {{ formatDateToYYYYMMDD(selectedDate) }} <i class="far fa-calendar"></i>
        </div>
        <button class="nav-btn" @click="nextWeek"><i class="fas fa-chevron-right"></i></button>
        <button class="btn-today" @click="goToToday">Hôm nay</button>
      </div>
      <div class="week-info">
        Tuần từ: <strong>{{ weekRangeText }}</strong>
      </div>
    </div>

    <div v-if="currentView === 'calendar'" class="calendar-view card">
      <table class="calendar-table">
        <thead>
          <tr>
            <th class="shift-col-header">CA / NGÀY</th>
            <th v-for="day in weekDays" :key="day.dateStr" :class="{ 'is-today': day.isToday }">
              <div class="day-name">{{ day.name }}</div>
              <div class="day-date">{{ day.displayDate }}</div>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="shift in shifts" :key="shift.id">
            <td class="shift-info">
              <div class="shift-name">{{ shift.name }}</div>
              <div class="shift-time">{{ shift.time }}</div>
            </td>
            
            <td v-for="day in weekDays" :key="day.dateStr" class="calendar-cell">
              
              <div 
                v-for="assignment in getAssignmentsForCell(shift.id, day.dateStr)" 
                :key="assignment.id" 
                class="assignment-card"
                :title="(assignment.nhanVien.ten || assignment.nhanVien.tenNhanVien) + ' · ' + (assignment.nhanVien.ma || assignment.nhanVien.maNhanVien)"
              >
                <div class="avatar">{{ (assignment.nhanVien.ten || assignment.nhanVien.tenNhanVien || 'N').charAt(0).toUpperCase() }}</div>
                <div class="emp-details">
                  <div class="emp-name">{{ assignment.nhanVien.ten || assignment.nhanVien.tenNhanVien }}</div>
                  <div class="emp-code">{{ assignment.nhanVien.ma || assignment.nhanVien.maNhanVien }}</div>
                </div>
              </div>
              
              <div class="add-assignment-btn" @click="openAssignModal(shift, day.dateStr)">
                <i class="fas fa-plus"></i>
              </div>

            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="currentView === 'list'" class="list-view card">
      
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

      <table class="data-table">
        <thead>
          <tr>
            <th>STT</th>
            <th>Ngày làm việc</th>
            <th>Ca làm việc</th>
            <th>Thời gian</th>
            <th>Nhân viên</th>
            <th class="text-center">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(schedule, index) in filteredSchedules" :key="schedule.id">
            <td>{{ index + 1 }}</td>
            <td><i class="far fa-calendar text-gray"></i> {{ schedule.ngayLamViec }}</td>
            <td><span class="badge-shift">{{ schedule.caLamViec?.tenCa }}</span></td>
            <td>{{ schedule.caLamViec?.thoiGianBatDau }} - {{ schedule.caLamViec?.thoiGianKetThuc }}</td>
            <td>
              <div class="emp-info-inline">
                <div class="avatar-small bg-green">{{ (schedule.nhanVien?.ten || schedule.nhanVien?.tenNhanVien || 'N').charAt(0).toUpperCase() }}</div>
                <div>
                  <div class="font-medium">{{ schedule.nhanVien?.ten || schedule.nhanVien?.tenNhanVien }}</div>
                  <div class="text-xs text-gray">{{ schedule.nhanVien?.ma || schedule.nhanVien?.maNhanVien }}</div>
                </div>
              </div>
            </td>
            <td class="actions text-center">
              <button class="icon-btn edit" title="Sửa lịch" @click="editSchedule(schedule)">
                <i class="fas fa-pen"></i>
              </button>
              <button class="icon-btn delete" title="Xóa lịch" @click="deleteSchedule(schedule)">
                <i class="fas fa-trash"></i>
              </button>
            </td>
          </tr>
          
          <tr v-if="filteredSchedules.length === 0">
            <td colspan="6" class="text-center" style="padding: 30px; color: #6b7280;">
              <span v-if="searchEmployee || searchShift || filterStartDate || filterEndDate">
                Không tìm thấy lịch làm việc phù hợp với bộ lọc!
              </span>
              <span v-else>Tuần này chưa có lịch làm việc nào được phân công.</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>

  <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <div class="modal-header">
        <h3>{{ isEditMode ? 'Cập nhật lịch làm việc' : 'Thêm nhân viên vào ca' }}</h3>
        <button class="btn-close" @click="closeModal"><i class="fas fa-times"></i></button>
      </div>
      <div class="modal-body">
        <div class="info-group">
          <label>Ca làm việc:</label>
          <strong>{{ selectedShiftModal?.name || selectedShiftModal?.tenCa }}</strong>
        </div>
        <div class="info-group">
          <label>Ngày:</label>
          <strong>{{ selectedDateModal }}</strong>
        </div>
        
        <template v-if="isEditMode">
          <div class="form-group mt-3">
            <label>Chọn nhân viên <span class="text-red">*</span></label>
            <select v-model="selectedEmployeeId" class="form-control">
              <option value="" disabled>-- Vui lòng chọn nhân viên --</option>
              <option v-for="emp in employees" :key="emp.id" :value="emp.id">
                {{ emp.maNhanVien || emp.ma }} - {{ emp.tenNhanVien || emp.ten }}
              </option>
            </select>
          </div>
        </template>

        <template v-else>
          <div class="form-group mt-3">
            <label>Chọn nhân viên <span class="text-red">*</span> <span class="text-gray text-xs">(có thể chọn nhiều)</span></label>
            <div class="employee-search-box">
              <i class="fas fa-search search-icon-sm"></i>
              <input type="text" v-model="employeeSearch" class="form-control" placeholder="Tìm tên hoặc mã nhân viên..." />
            </div>
            <div class="employee-checkbox-list">
              <label 
                v-for="emp in availableEmployees" 
                :key="emp.id" 
                class="employee-checkbox-item"
                :class="{ 'is-checked': selectedEmployeeIds.includes(emp.id) }"
              >
                <input type="checkbox" :value="emp.id" v-model="selectedEmployeeIds" />
                <span class="emp-avatar-sm">{{ (emp.tenNhanVien || emp.ten || 'N').charAt(0).toUpperCase() }}</span>
                <span class="emp-label">{{ emp.maNhanVien || emp.ma }} - {{ emp.tenNhanVien || emp.ten }}</span>
              </label>
              <div v-if="availableEmployees.length === 0" class="no-emp-msg">
                <i class="fas fa-info-circle"></i> Không tìm thấy nhân viên phù hợp
              </div>
            </div>
            <div v-if="selectedEmployeeIds.length > 0" class="selected-count-bar">
              Đã chọn: <strong>{{ selectedEmployeeIds.length }}</strong> nhân viên
            </div>
          </div>
        </template>
      </div>
      <div class="modal-footer">
        <button class="btn-cancel" @click="closeModal">Hủy</button>
        <button class="btn-save" @click="saveSchedule">
          <i class="fas fa-save"></i> {{ isEditMode ? 'Cập nhật' : 'Xếp lịch' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import request from '@/services/request'; 
import Swal from 'sweetalert2';           
import axios from 'axios';

const currentView = ref('calendar');
const showExcelMenu = ref(false);

// ==========================================
// 1. LOGIC XỬ LÝ NGÀY THÁNG (TUẦN)
// ==========================================
const selectedDate = ref(new Date()); 

const formatDateToYYYYMMDD = (date) => {
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, '0');
  const dd = String(date.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
};

const weekDays = computed(() => {
  const days = [];
  const curr = new Date(selectedDate.value);
  
  const dayOfWeek = curr.getDay(); 
  const distance = dayOfWeek === 0 ? -6 : 1 - dayOfWeek; 
  curr.setDate(curr.getDate() + distance);

  const dayNames = ['CN', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];

  for (let i = 0; i < 7; i++) {
    const dateObj = new Date(curr);
    dateObj.setDate(dateObj.getDate() + i);
    
    const dateStr = formatDateToYYYYMMDD(dateObj); 
    const displayDate = `${String(dateObj.getDate()).padStart(2, '0')}/${String(dateObj.getMonth() + 1).padStart(2, '0')}`; 
    
    const isToday = dateStr === formatDateToYYYYMMDD(new Date());

    days.push({
      name: dayNames[dateObj.getDay()],
      dateStr: dateStr,
      displayDate: displayDate,
      isToday: isToday
    });
  }
  return days;
});

const weekRangeText = computed(() => {
  if (weekDays.value.length === 0) return '';
  const start = weekDays.value[0].dateStr;
  const end = weekDays.value[6].dateStr;
  return `${start} đến ${end}`;
});

const nextWeek = () => { selectedDate.value = new Date(selectedDate.value.setDate(selectedDate.value.getDate() + 7)); };
const prevWeek = () => { selectedDate.value = new Date(selectedDate.value.setDate(selectedDate.value.getDate() - 7)); };
const goToToday = () => { selectedDate.value = new Date(); };

// ==========================================
// 2. STATE CHO API VÀ MODAL
// ==========================================
const shifts = ref([]); 
const schedules = ref([]); 
const employees = ref([]); 

const showModal = ref(false);
const selectedShiftModal = ref(null);
const selectedDateModal = ref('');
const selectedEmployeeId = ref('');
const selectedEmployeeIds = ref([]);
const employeeSearch = ref('');

// ==========================================
// 3. CÁC HÀM GỌI API
// ==========================================
const fetchShifts = async () => {
  try {
    const res = await request.get('/ca-lam-viec', { params: { status: 1 } });
    const rawData = res.data.content || res.data;
    
    shifts.value = rawData.map(ca => ({
      id: ca.id,
      name: ca.tenCa, 
      time: `${ca.thoiGianBatDau || ''} - ${ca.thoiGianKetThuc || ''}`.trim() 
    }));
  } catch (error) { 
    console.error("Lỗi lấy ca làm việc:", error); 
  }
};

const fetchSchedules = async () => {
  if (weekDays.value.length === 0) return;
  const startDate = weekDays.value[0].dateStr; 
  const endDate = weekDays.value[6].dateStr;

  try {
    const res = await request.get('/lich-lam-viec', {
      params: { startDate, endDate } 
    });
    schedules.value = res.data.content || res.data; 
  } catch (error) { 
    console.error("Lỗi lấy lịch làm việc:", error); 
  }
};

watch(selectedDate, () => {
  fetchSchedules();
});

// ==========================================
// 4. LOGIC HIỂN THỊ VÀ LƯU MODAL
// ==========================================
const getAssignmentsForCell = (shiftId, dateStr) => {
  return schedules.value.filter(s => s.caLamViec?.id === shiftId && s.ngayLamViec === dateStr);
};

const availableEmployees = computed(() => {
  const assigned = getAssignmentsForCell(
    selectedShiftModal.value?.id, 
    selectedDateModal.value
  ).map(a => a.nhanVien?.id);
  
  const keyword = employeeSearch.value.toLowerCase().trim();
  
  return employees.value.filter(emp => {
    if (assigned.includes(emp.id)) return false;
    if (!keyword) return true;
    const name = (emp.tenNhanVien || emp.ten || '').toLowerCase();
    const code = (emp.maNhanVien || emp.ma || '').toLowerCase();
    return name.includes(keyword) || code.includes(keyword);
  });
});

const openAssignModal = async (shift, dateStr) => {
  selectedShiftModal.value = shift;
  selectedDateModal.value = dateStr;
  selectedEmployeeId.value = ''; 
  selectedEmployeeIds.value = [];
  employeeSearch.value = '';
  showModal.value = true;

  if (employees.value.length === 0) {
    try {
      const res = await request.get('/nhan-vien', { params: { size: 100 } }); 
      employees.value = res.data.content || res.data;
    } catch (e) { 
      console.error("Lỗi tải nhân viên:", e); 
    }
  }
};

// ==========================================
// 5. THAO TÁC Ở TAB DANH SÁCH (SỬA / XÓA)
// ==========================================
const isEditMode = ref(false); 
const currentEditId = ref(null);

const deleteSchedule = async (schedule) => {
  const empName = schedule.nhanVien?.ten || schedule.nhanVien?.tenNhanVien;
  
  const result = await Swal.fire({
    title: 'Xác nhận xóa?',
    text: `Bạn có muốn xóa lịch của ${empName} vào ngày ${schedule.ngayLamViec}?`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#ef4444',
    confirmButtonText: 'Đồng ý xóa',
    cancelButtonText: 'Hủy'
  });

  if (result.isConfirmed) {
    try {
      await request.delete(`/lich-lam-viec/${schedule.id}`);
      Swal.fire({ icon: 'success', title: 'Đã xóa', text: 'Xóa lịch thành công!', timer: 1500, showConfirmButton: false });
      fetchSchedules(); 
    } catch (error) {
      Swal.fire('Lỗi', 'Không thể xóa lịch này', 'error');
    }
  }
};

const editSchedule = async (schedule) => {
  isEditMode.value = true;
  currentEditId.value = schedule.id;
  
  selectedShiftModal.value = schedule.caLamViec;
  selectedDateModal.value = schedule.ngayLamViec;
  selectedEmployeeId.value = schedule.nhanVien?.id;
  
  showModal.value = true;

  if (employees.value.length === 0) {
    try {
      const res = await request.get('/nhan-vien', { params: { size: 100 } }); 
      employees.value = res.data.content || res.data;
    } catch (e) { console.error(e); }
  }
};

const saveSchedule = async () => {
  if (isEditMode.value) {
    if (!selectedEmployeeId.value) {
      return Swal.fire('Lỗi', 'Vui lòng chọn nhân viên!', 'warning');
    }
    try {
      const payload = {
        caLamViecId: selectedShiftModal.value.id,
        nhanVienId: selectedEmployeeId.value,
        ngayLamViec: selectedDateModal.value
      };
      await request.put(`/lich-lam-viec/${currentEditId.value}`, payload);
      Swal.fire({ icon: 'success', title: 'Thành công', text: 'Cập nhật lịch thành công!', timer: 1500, showConfirmButton: false });
      closeModal();
      fetchSchedules();
    } catch (error) {
      Swal.fire('Lỗi', error.response?.data?.message || 'Không thể cập nhật lịch', 'error');
    }
  } else {
    if (selectedEmployeeIds.value.length === 0) {
      return Swal.fire('Lỗi', 'Vui lòng chọn ít nhất 1 nhân viên!', 'warning');
    }
    try {
      const promises = selectedEmployeeIds.value.map(empId =>
        request.post('/lich-lam-viec', {
          caLamViecId: selectedShiftModal.value.id,
          nhanVienId: empId,
          ngayLamViec: selectedDateModal.value
        })
      );
      const results = await Promise.allSettled(promises);
      const failed = results.filter(r => r.status === 'rejected');
      
      if (failed.length === 0) {
        Swal.fire({ icon: 'success', title: 'Thành công', text: `Đã xếp lịch cho ${selectedEmployeeIds.value.length} nhân viên!`, timer: 1500, showConfirmButton: false });
      } else if (failed.length < results.length) {
        Swal.fire({ icon: 'warning', title: 'Hoàn tất một phần', text: `Thành công: ${results.length - failed.length}, Thất bại: ${failed.length}` });
      } else {
        Swal.fire('Lỗi', 'Không thể xếp lịch cho nhân viên đã chọn', 'error');
      }
      closeModal();
      fetchSchedules();
    } catch (error) {
      Swal.fire('Lỗi', 'Không thể lưu lịch làm việc', 'error');
    }
  }
};

// ==========================================
// 6. BỘ LỌC TÌM KIẾM (TAB DANH SÁCH)
// ==========================================
const searchEmployee = ref('');
const searchShift = ref('');
const filterStartDate = ref('');
const filterEndDate = ref('');

const clearFilters = () => {
  searchEmployee.value = '';
  searchShift.value = '';
  filterStartDate.value = '';
  filterEndDate.value = '';
};

const filteredSchedules = computed(() => {
  return schedules.value.filter(schedule => {
    const empName = (schedule.nhanVien?.ten || schedule.nhanVien?.tenNhanVien || '').toLowerCase();
    const empCode = (schedule.nhanVien?.ma || schedule.nhanVien?.maNhanVien || '').toLowerCase();
    const empKeyword = searchEmployee.value.toLowerCase().trim();
    const matchEmp = !empKeyword || empName.includes(empKeyword) || empCode.includes(empKeyword);

    const shiftName = (schedule.caLamViec?.tenCa || '').toLowerCase();
    const shiftKeyword = searchShift.value.toLowerCase().trim();
    const matchShift = !shiftKeyword || shiftName.includes(shiftKeyword);

    const scheduleDateStr = schedule.ngayLamViec; 
    let matchDate = true;
    if (filterStartDate.value && filterEndDate.value) {
      matchDate = scheduleDateStr >= filterStartDate.value && scheduleDateStr <= filterEndDate.value;
    } else if (filterStartDate.value) {
      matchDate = scheduleDateStr >= filterStartDate.value;
    } else if (filterEndDate.value) {
      matchDate = scheduleDateStr <= filterEndDate.value;
    }

    return matchEmp && matchShift && matchDate;
  });
});
// Reference tới thẻ input ẩn
const fileInput = ref(null);

// Hàm kích hoạt thẻ input khi bấm nút
const triggerFileUpload = () => {
  if (fileInput.value) {
    fileInput.value.click();
  }
};

// Hàm xử lý file sau khi người dùng chọn
const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append('file', file);

  try {
    const response = await axios.post('http://localhost:8080/api/v1/lich-lam-viec/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    
    alert('Import thành công!');
    // fetchSchedules(); 
  } catch (error) {
    console.error('Lỗi khi import Excel:', error);
    
    // Moi thông báo lỗi chi tiết từ Backend (Spring Boot) gửi về
    const backendMessage = error.response?.data;
    
    if (typeof backendMessage === 'string') {
      // Nếu là lỗi Validate trong file Excel do chúng ta tự viết
      alert("Chi tiết lỗi Import:\n\n" + backendMessage);
    } else {
      // Lỗi hệ thống khác
      alert("Import thất bại: " + error.message);
    }
  } finally {
    event.target.value = null; 
  }
};
const downloadTemplate = async () => {
  try {
    // Gọi API lấy file Excel từ Backend (Nhớ thêm responseType: 'blob' để nhận file)
    const response = await axios.get('http://localhost:8080/api/v1/lich-lam-viec/template', {
      responseType: 'blob' 
    });

    // 1. Lấy ngày đang chọn trên UI và format về chuẩn YYYY-MM-DD
    // (Đảm bảo bạn dùng đúng biến chứa ngày của bạn nhé, ví dụ selectedDate)
    const dateString = formatDateToYYYYMMDD(selectedDate.value); 

    // 2. Tạo tên file động có chứa ngày
    const fileName = `Template_LichLamViec_${dateString}.xlsx`;

    // 3. Xử lý tải file xuống trình duyệt
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    
    // Gán cái tên file vừa tạo vào đây
    link.setAttribute('download', fileName); 
    
    document.body.appendChild(link);
    link.click(); // Giả lập hành động click để tải file

    // Dọn dẹp rác bộ nhớ sau khi tải xong
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

  } catch (error) {
    console.error('Lỗi khi tải template:', error);
    alert('Không thể tải file template!');
  }
};
const handleCopyLastWeek = async () => {
  // 1. Hiển thị Popup xác nhận
  const result = await Swal.fire({
    title: 'Sao chép lịch tuần trước?',
    text: "Hệ thống sẽ lấy lịch của tuần trước và áp dụng cho tuần hiện tại. Những ca đã có sẵn sẽ bị bỏ qua để tránh trùng lặp.",
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#10b981',
    cancelButtonColor: '#d33',
    confirmButtonText: 'Có!',
    cancelButtonText: 'Hủy'
  });

  if (result.isConfirmed) {
    try {
      // Đóng dropdown menu nếu đang mở
      showExcelMenu.value = false;
      
      // Hiện loading để user không bấm lung tung lúc đang chờ API
      Swal.showLoading();

      // 2. Gọi API (Truyền ngày đang chọn trên giao diện: selectedDate)
      // Chú ý: Format ngày sang YYYY-MM-DD trước khi gửi
      const dateString = formatDateToYYYYMMDD(selectedDate.value);
      
      const response = await axios.post(`http://localhost:8080/api/v1/lich-lam-viec/copy-last-week?date=${dateString}`);

      // 3. Tải lại bảng dữ liệu
      await fetchSchedules();

      // 4. Báo thành công
      Swal.fire({
        icon: 'success',
        title: 'Thành công!',
        text: response.data, // Hiển thị câu thông báo từ Backend trả về
        timer: 2000,
        showConfirmButton: false
      });

    } catch (error) {
      console.error('Lỗi khi copy lịch:', error);
      // Báo lỗi (Ví dụ: Tuần trước không có dữ liệu)
      Swal.fire({
        icon: 'error',
        title: 'Không thể sao chép',
        text: error.response?.data || 'Đã có lỗi xảy ra, vui lòng thử lại sau.'
      });
    }
  }
};
const closeModal = () => { 
  showModal.value = false; 
  isEditMode.value = false; 
  currentEditId.value = null;
};

onMounted(() => {
  fetchShifts();
  fetchSchedules();
});
</script>

<style scoped>
/* Reset cơ bản và biến màu sắc dựa trên thiết kế */
.schedule-container {
  
   padding: 20px; font-family: 'Segoe UI', sans-serif; background: #ebecee;; min-height: 100vh; color: #333; font-size: 14px; 
}
.text-gray { color: #888; }
.text-blue { color: #3b82f6; font-weight: 500; }
.bg-green { background-color: #2563eb; color: white; }

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.title-section {
  display: flex;
  align-items: center;
  gap: 15px;
}
.icon-wrapper {
  background: #dbeafe;
  color: #2563eb;
  padding: 12px;
  border-radius: 10px;
  font-size: 24px;
}
.page-title { margin: 0; font-size: 24px; font-weight: bold; color: #1e293b; }
.page-subtitle { margin: 0; color: #6b7280; font-size: 14px; }

/* View Toggle & Buttons */
.action-section { display: flex; gap: 15px; align-items: center; }
.action-buttons-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* Nút Sao chép - Màu Gradient hoặc tím nổi bật */
.btn-magic {
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 6px;
  font-weight: 500;
  transition: opacity 0.3s;
}
.btn-magic:hover {
  opacity: 0.9;
}

/* Nút Dropdown Excel */
.btn-excel {
  background-color: #1b10b9;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 6px;
  font-weight: 500;
}
.dropdown-container {
  position: relative;
}
.dropdown-menu {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 5px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  min-width: 160px;
  z-index: 50;
  overflow: hidden;
}
.dropdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 15px;
  color: #374151;
  text-decoration: none;
  transition: background 0.2s;
}
.dropdown-item:hover {
  background-color: #f3f4f6;
  color: #111827;
}

/* Nút icon only cho nút Refresh */
.icon-only {
  width: 38px;
  height: 38px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
}
.ml-1 {
  margin-left: 4px;
}
.view-toggle {
  display: flex;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}
/* Base button chung cho khu action */
.action-section .btn {
  border: none;
  border-radius: 10px;
  padding: 11px 18px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.22s ease;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 44px;
}

/* Tải template - xanh dương sáng, đồng bộ hơn */
.btn-export {
  background: linear-gradient(135deg, #4f8cff 0%, #2563eb 100%);
  color: #ffffff;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.22);
}

.btn-export:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.3);
  filter: brightness(1.02);
}

/* Nhập excel - xanh lá emerald dịu hơn */
.btn-import {
  background: linear-gradient(135deg, #22c55e 0%, #059669 100%);
  color: #ffffff;
  box-shadow: 0 6px 16px rgba(5, 150, 105, 0.22);
}

.btn-import:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(5, 150, 105, 0.3);
  filter: brightness(1.02);
}

/* Nút làm mới giữ đồng bộ kích thước */
.btn-refresh {
  border: none;
  border-radius: 10px;
  padding: 11px 18px;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  min-height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%);
  color: #fff;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2);
  transition: all 0.22s ease;
}

.btn-refresh:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 15px rgba(15, 23, 42, 0.3);
}
.toggle-btn {
  padding: 8px 16px;
  border: none;
  background: white;
  color: #6b7280;
  cursor: pointer;
  font-weight: 500;
}
.toggle-btn.active {
  background: white;
  color: #2563eb;
  border: 1px solid #2563eb;
  border-radius: 6px;
}
.btn-refresh {
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; 
    box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); 
}


.btn-refresh:hover {
  transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.3); 
}
/* Date Nav */
.date-navigation {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.date-controls { display: flex; gap: 10px; align-items: center; }
.nav-btn, .date-picker-box, .btn-today {
  border: 1px solid #e5e7eb;
  padding: 8px 12px;
  border-radius: 6px;
  background: white;
  cursor: pointer;
}
.btn-today { background: #dbeafe; color: #2563eb; border: none; font-weight: 500; }
.week-info {
  background: #eff6ff;
  color: #2563eb;
  padding: 8px 16px;
  border-radius: 20px;
}

/* Common Card */
.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  padding: 20px;
  overflow-x: auto;
}

/* Calendar Table */
.calendar-table { width: 100%; border-collapse: collapse; text-align: center; }
.calendar-table th, .calendar-table td {
  border: 1px solid #f3f4f6;
  padding: 15px 10px;
}
.shift-col-header { width: 150px; text-align: left; font-weight: bold; color: #4b5563;}
.calendar-table th.is-today { color: #2563eb; background: #f8fafc; border-top: 3px solid #2563eb;}
.day-name { font-weight: bold; font-size: 14px; }
.day-date { font-size: 12px; color: #9ca3af; }

.shift-info { text-align: left; }
.shift-name { font-weight: bold; margin-bottom: 5px; }
.shift-time { font-size: 12px; color: #2563eb; background: #e6fcf5; display: inline-block; padding: 2px 6px; border-radius: 4px; }

/* Grid Cells & Assignment Card */
.calendar-cell {
  min-height: 60px;
  vertical-align: top;
  position: relative;
  padding: 4px 4px 28px 4px;
}
.add-assignment-btn {
  position: absolute;
  bottom: 4px;
  left: 50%;
  transform: translateX(-50%);
  display: flex; justify-content: center; align-items: center;
  height: 22px; width: 22px;
  border-radius: 50%; border: 1px dashed #cbd5e1; color: #2563eb;
  cursor: pointer; opacity: 0; transition: opacity 0.2s;
  background: #f8fafc;
  font-size: 10px;
}
.calendar-cell:hover .add-assignment-btn { opacity: 1; }

.assignment-card {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 5px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-left: 3px solid #2563eb;
  border-radius: 5px;
  padding: 3px 5px;
  margin-bottom: 3px;
  overflow: hidden;
  cursor: default;
}
.avatar {
  width: 22px; height: 22px; flex-shrink: 0;
  border-radius: 50%; background: #1e3a8a; color: white;
  display: flex; align-items: center; justify-content: center;
  font-weight: bold; font-size: 11px;
}
.emp-details { overflow: hidden; min-width: 0; }
.emp-name { font-size: 11px; font-weight: 600; color: #1e293b; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.emp-code { font-size: 10px; color: #2563eb; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* List View Filters & Table */
.filter-row { display: flex; gap: 15px; margin-bottom: 20px; }
.search-box, .date-range-box {
  border: 1px solid #e5e7eb; border-radius: 6px; padding: 8px 12px;
  display: flex; align-items: center; gap: 10px; color: #6b7280; flex: 1;
}
.search-box input { border: none; outline: none; width: 100%; }
.btn-clear-filter { border: none; background: transparent; color: #6b7280; cursor: pointer; }

.data-table { width: 100%; border-collapse: collapse; text-align: left; }
.data-table th { padding: 12px 15px; color: #4b5563; font-weight: bold; border-bottom: 2px solid #f3f4f6; }
.data-table td { padding: 15px; border-bottom: 1px solid #f3f4f6; vertical-align: middle; }
.badge-shift { border: 1px solid #e5e7eb; padding: 4px 10px; border-radius: 15px; font-size: 13px; }
.emp-info-inline { display: flex; align-items: center; gap: 10px; }
.avatar-small { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-weight: bold; }
.icon-btn { border: none; background: #fff1f2; color: #ef4444; width: 30px; height: 30px; border-radius: 50%; cursor: pointer; }
.icon-btn.edit { background: #fffbeb; color: #f59e0b; margin-right: 5px;}
/* --- Modal Styles --- */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex; align-items: center; justify-content: center;
  z-index: 1000;
}
.modal-content {
  background: white; width: 520px; max-height: 85vh; border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1); overflow: hidden;
  display: flex; flex-direction: column;
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 15px 20px; border-bottom: 1px solid #eee; background: #f8fafc;
}
.modal-header h3 { margin: 0; font-size: 18px; color: #1e293b; }
.btn-close { background: none; border: none; font-size: 20px; cursor: pointer; color: #94a3b8; }
.modal-body { padding: 20px; overflow-y: auto; }
.info-group { margin-bottom: 10px; font-size: 15px; color: #475569; }
.info-group label { display: inline-block; width: 100px; }
.form-control {
  width: 100%; padding: 10px; border: 1px solid #cbd5e1;
  border-radius: 6px; margin-top: 5px; outline: none;
}
.form-control:focus { border-color: #2563eb; }
.text-red { color: #ef4444; }
.mt-3 { margin-top: 15px; }
.modal-footer {
  display: flex; justify-content: flex-end; gap: 10px;
  padding: 15px 20px; border-top: 1px solid #eee;
}
.btn-cancel {
  padding: 8px 16px; border: 1px solid #cbd5e1; background: white;
  border-radius: 6px; cursor: pointer; color: #475569; font-weight: 500;
}
.btn-save {
  padding: 8px 16px; border: none; background: #2563eb; color: white;
  border-radius: 6px; cursor: pointer; font-weight: 500;
}

/* Employee multi-select */
.employee-search-box {
  position: relative; margin-bottom: 8px;
}
.employee-search-box .search-icon-sm {
  position: absolute; left: 10px; top: 50%; transform: translateY(-50%); color: #9ca3af; font-size: 13px;
}
.employee-search-box input {
  padding-left: 32px;
}
.employee-checkbox-list {
  max-height: 280px; overflow-y: auto; border: 1px solid #e5e7eb; border-radius: 8px;
  padding: 4px;
}
.employee-checkbox-item {
  display: flex; align-items: center; gap: 10px; padding: 8px 10px; border-radius: 6px;
  cursor: pointer; transition: background 0.15s; margin: 2px 0;
}
.employee-checkbox-item:hover { background: #f1f5f9; }
.employee-checkbox-item.is-checked { background: #eff6ff; }
.employee-checkbox-item input[type="checkbox"] {
  width: 16px; height: 16px; accent-color: #2563eb; cursor: pointer; flex-shrink: 0;
}
.emp-avatar-sm {
  width: 30px; height: 30px; border-radius: 50%; background: #2563eb; color: white;
  display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 600; flex-shrink: 0;
}
.emp-label { font-size: 14px; color: #334155; }
.no-emp-msg { text-align: center; padding: 20px; color: #94a3b8; font-size: 13px; }
.selected-count-bar {
  margin-top: 8px; padding: 6px 12px; background: #eff6ff; border-radius: 6px;
  font-size: 13px; color: #1e40af; text-align: center;
}
.text-xs { font-size: 12px; }
.text-gray { color: #9ca3af; }
/* =======================================
   CSS CHO BỘ LỌC DANH SÁCH (CUSTOM FILTER BAR)
   ======================================= */
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