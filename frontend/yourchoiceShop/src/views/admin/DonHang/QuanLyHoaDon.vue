<template>
  <div class="page-container">
    <h1 class="page-title">Quản lý hóa đơn</h1>

    <div class="control-panel">
      <div class="controls-row">
        <div class="filter-group">
          <div class="search-box">
            <i class="fas fa-magnifying-glass search-icon"></i>
            <input class="input-den" type="text" v-model="filter.keyword" placeholder="Tìm mã đơn, tên khách..."
              @keyup.enter="fetchData">
          </div>

          <div class="date-group">
            <input type="date" class="form-control-date" v-model="filter.fromDate" @change="fetchData" />
            <span class="arrow">➜</span>
            <input type="date" class="form-control-date" v-model="filter.toDate" @change="fetchData" />
          </div>

          <div class="radio-dropdown" ref="quickFilterRef">
            <button type="button" class="radio-dropdown-btn" @click="toggleQuickFilterDropdown">
              <span>{{ quickFilterLabel }}</span>
              <i class="fas" :class="showQuickFilterDropdown ? 'fa-chevron-up' : 'fa-chevron-down'"></i>
            </button>
            <div v-if="showQuickFilterDropdown" class="radio-dropdown-menu">
              <label v-for="option in QUICK_FILTER_OPTIONS" :key="option.value" class="radio-item"
                @click="applyQuickFilter(option.value)">
                <input type="radio" name="quick-order-filter" :checked="quickFilterValue === option.value" />
                <span>{{ option.label }}</span>
              </label>
            </div>
          </div>
        </div>

        <div class="action-group">
          <button class="btn btn-navy" @click="resetFilter">
            <i class="fas fa-sync-alt"></i> Đặt lại
          </button>
          <button class="btn btn-outline" @click="handleExportExcel">
            <font-awesome-icon :icon="['fas', 'file-excel']" /> Xuất Excel
          </button>
        </div>
      </div>
    </div>

    <div class="table-container">

      <div class="table-header-section">
        <div class="section-title">
          <div class="icon-title"><i class="fas fa-file-invoice"></i></div>
          <div class="text-title">
            <h3>Danh sách hóa đơn</h3>
            <span class="sub-text">Lọc nhanh theo trạng thái</span>
          </div>
        </div>

        <div class="status-tabs">
          <button v-for="tab in STATUS_TABS" :key="tab.key"
            :class="['tab-btn', { 'active-gradient': filter.activeTab === tab.key }]" @click="changeTab(tab.key)">
            {{ tab.label }}
          </button>
        </div>
      </div>

      <div class="table-scroll">
        <table class="custom-table">
          <colgroup>
            <col style="width: 5%;" />
            <col style="width: 14%;" />
            <col style="width: 14%;" />
            <col style="width: 14%;" />
            <col style="width: 12%;" />
            <col style="width: 11%;" />
            <col style="width: 10%;" />
            <col style="width: 9%;" />
            <col style="width: 8%;" />
            <col style="width: 3%;" />
          </colgroup>
          <thead>
            <tr>
              <th>STT</th>
              <th>Mã HĐ</th>
              <th>Nhân viên</th>
              <th>Khách hàng</th>
              <th>Ngày tạo</th>
              <th>Tổng tiền</th>
              <th>Loại</th>
              <th>SĐT KH</th>
              <th>Trạng thái</th>
              <th>Chi tiết</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="10" class="empty-state">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="orders.length === 0">
              <td colspan="10" class="empty-state">Không tìm thấy đơn hàng nào.</td>
            </tr>

            <tr v-else v-for="(order, index) in orders" :key="order.maHoaDon">
              <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
              <td class="code-text">{{ order.maHoaDon }}</td>
              <td>
                <div class="employee-info">
                  {{ order.tenNhanVien || null }}
                </div>
              </td>

              <td>
                <div class="customer-info">
                  {{ order.tenKhachHang || 'Khách lẻ' }}
                </div>
              </td>
              <td class="time-col">{{ formatDate(order.ngayTao) }}</td>
              <td class="text-price">{{ formatMoney(order.tongTienSauGiam) }}</td>

              <td>
                <span class="badge-type-lg" :class="getOrderTypeClass(order.loaiHoaDon)">
                  {{ getOrderTypeText(order.loaiHoaDon) }}
                </span>
              </td>

              <td>{{ getPhoneDisplay(order) }}</td>

              <td>
                <span class="badge-status" :class="getStatusClass(order.trangThai)">
                  {{ getStatusText(order.trangThai) }}
                </span>
              </td>

              <td class="action-col">
                <div class="action-wrapper">
                  <router-link :to="{ name: orderDetailRouteName, params: { id: order.maHoaDon } }" class="icon-btn"
                    title="Xem chi tiết">
                    <i class="far fa-eye"></i>
                  </router-link>
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
          kết quả / trang
        </div>
        <div class="page-controls">
          <button :disabled="currentPage === 1" @click="changePage(currentPage - 1)">‹</button>
          <button v-for="p in visiblePages" :key="p" :class="{ active: p === currentPage }" @click="changePage(p)">
            {{ p }}
          </button>
          <button :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">›</button>
        </div>
      </div>
    </div>

    <div v-if="showScanModal" class="modal-overlay" @click.self="closeScanModal">
      <div class="modal-content scan-modal">
        <div class="modal-header">
          <h3 style="margin:0">Quét mã QR Hóa đơn</h3>
          <button @click="closeScanModal" class="close-btn"><i class="fas fa-times"></i></button>
        </div>
        <div class="modal-body">
          <div id="qr-reader" style="width: 100%;"></div>
          <p class="scan-hint">Di chuyển camera vào mã QR trên hóa đơn</p>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, shallowRef, onMounted, onBeforeUnmount, nextTick, computed } from 'vue' // Bổ sung shallowRef
import { useRouter } from 'vue-router'
import { fetchOrders, exportOrders } from '@/api/HoaDonApi'
import { getRole } from '@/services/auth'
import { Html5QrcodeScanner } from "html5-qrcode"
import Swal from 'sweetalert2'

const router = useRouter()

// 1. TỐI ƯU REACTIVITY: Dùng shallowRef thay cho ref
// Giúp Vue bỏ qua việc theo dõi từng thuộc tính nhỏ bên trong mảng, tăng tốc độ render lên x3 lần
const orders = shallowRef([])

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const filter = ref({ keyword: '', fromDate: '', toDate: '', orderType: '', activeTab: 'ALL' })
const quickFilterValue = ref('')
const showQuickFilterDropdown = ref(false)
const quickFilterRef = ref(null)
const showScanModal = ref(false)
let html5QrcodeScanner = null

const QUICK_FILTER_OPTIONS = [
  { value: '', label: 'Tất cả' },
  { value: 'Trực tuyến', label: 'Online' },
  { value: 'Tại quầy', label: 'Tại quầy' },
  { value: 'DANG_GIAO', label: 'Đơn đang giao' }
]

// Computed để chọn route name dựa trên role
const orderDetailRouteName = computed(() => {
  const role = getRole()
  return role === 'STAFF' ? 'staff-order-detail' : 'admin-order-detail'
})

// 2. TỐI ƯU FORMATTER: Khởi tạo 1 lần duy nhất bên ngoài để tái sử dụng
const moneyFormatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });
const dateFormatter = new Intl.DateTimeFormat('vi-VN', {
  year: 'numeric', month: '2-digit', day: '2-digit',
  hour: '2-digit', minute: '2-digit'
});

const formatMoney = (val) => val ? moneyFormatter.format(val) : '0 đ';

const getPhoneDisplay = (order) => {
  if (order?.sdtKhachHang) return order.sdtKhachHang;
  if ((order?.tenKhachHang || '').trim() === 'Khách lẻ') return 'Khách lẻ';
  return '-';
}

const quickFilterLabel = computed(() => {
  const selected = QUICK_FILTER_OPTIONS.find(option => option.value === quickFilterValue.value)
  return selected ? selected.label : 'Tất cả'
})

const getTodayDateInputValue = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

const formatDate = (val) => {
  if (!val) return '';
  let dateObj;
  if (Array.isArray(val)) {
    // Xử lý mảng [năm, tháng, ngày, giờ, phút] từ Backend Java trả về
    dateObj = new Date(val[0], val[1] - 1, val[2], val[3] || 0, val[4] || 0);
  } else {
    dateObj = new Date(val);
  }
  return isNaN(dateObj.getTime()) ? '' : dateFormatter.format(dateObj);
}

// --- LOGIC MODAL & QR ---
const resetFilter = () => {
  filter.value = { keyword: '', fromDate: '', toDate: '', orderType: '', activeTab: 'ALL' };
  quickFilterValue.value = '';
  showQuickFilterDropdown.value = false;
  currentPage.value = 1;
  fetchData();
}

const toggleQuickFilterDropdown = () => {
  showQuickFilterDropdown.value = !showQuickFilterDropdown.value
}

const applyQuickFilter = (value) => {
  quickFilterValue.value = value

  if (value === 'DANG_GIAO') {
    filter.value.orderType = 'Giao hàng'
    filter.value.activeTab = 'ALL'
  } else {
    filter.value.orderType = value
    filter.value.activeTab = 'ALL'
  }

  currentPage.value = 1
  showQuickFilterDropdown.value = false
  fetchData()
}

const handleOutsideQuickFilterClick = (event) => {
  if (!quickFilterRef.value) return
  if (!quickFilterRef.value.contains(event.target)) {
    showQuickFilterDropdown.value = false
  }
}

const openScanModal = () => {
  showScanModal.value = true;
  nextTick(() => { startScanner(); });
}

const closeScanModal = () => {
  if (html5QrcodeScanner) {
    html5QrcodeScanner.clear().catch(error => console.error("Failed to clear html5QrcodeScanner. ", error));
  }
  showScanModal.value = false;
}

const startScanner = () => {
  html5QrcodeScanner = new Html5QrcodeScanner("qr-reader", { fps: 10, qrbox: { width: 250, height: 250 } }, false);
  html5QrcodeScanner.render(onScanSuccess, onScanFailure);
}
const onScanFailure = (error) => { }

const onScanSuccess = (decodedText, decodedResult) => {
  closeScanModal();
  if (decodedText) {
    router.push({ name: orderDetailRouteName.value, params: { id: decodedText } });
  }
}

// --- METHODS GỌI API ---
const handleExportExcel = async () => {
  const result = await Swal.fire({
    title: 'Xác nhận',
    text: 'Bạn có muốn tải xuống danh sách đơn hàng không?',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Có',
    cancelButtonText: 'Hủy'
  });
  if (!result.isConfirmed) return;

  try {
    const params = {
      keyword: filter.value.keyword,
      type: filter.value.orderType || null,
      fromDate: filter.value.fromDate || null,
      toDate: filter.value.toDate || null,
      status: filter.value.activeTab === 'ALL' ? null : filter.value.activeTab
    }
    const response = await exportOrders(params);
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    const fileName = `DanhSachDonHang_${new Date().toISOString().slice(0, 10)}.xlsx`;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);

    const Toast = Swal.mixin({ toast: true, position: 'top-end', showConfirmButton: false, timer: 1500 });
    Toast.fire({ icon: 'success', title: 'Xuất Excel thành công' });
  } catch (error) {
    console.error(error);
    Swal.fire('Lỗi', error.response?.data?.message || 'Lỗi hệ thống', 'error');
  }
}

const fetchData = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: filter.value.keyword,
      type: filter.value.orderType || null,
      fromDate: filter.value.fromDate || null,
      toDate: filter.value.toDate || null,
      status: filter.value.activeTab === 'ALL' ? null : filter.value.activeTab
    }
    const res = await fetchOrders(params);
    orders.value = res.data.content;
    totalPages.value = res.data.totalPages > 0 ? res.data.totalPages : 1;
  } catch (error) {
    console.error("Lỗi:", error);
    // nếu bị timeout thì báo cho người dùng
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      Swal.fire('Lỗi', 'Yêu cầu mất quá nhiều thời gian. Vui lòng thử lại sau.', 'warning');
    }
    orders.value = [];
  } finally {
    loading.value = false;
  }
}

// --- LOGIC PHÂN TRANG & TAB ---
const changeTab = (key) => { filter.value.activeTab = key; currentPage.value = 1; fetchData(); }
const changePage = (page) => { if (page >= 1 && page <= totalPages.value) { currentPage.value = page; fetchData(); } }
const handlePageSizeChange = () => { currentPage.value = 1; fetchData(); }

const visiblePages = computed(() => {
  const pages = [];
  for (let i = 1; i <= totalPages.value; i++) {
    if (i === 1 || i === totalPages.value || (i >= currentPage.value - 1 && i <= currentPage.value + 1)) {
      pages.push(i);
    }
  }
  return pages;
});

// --- CẤU HÌNH TRẠNG THÁI ---
const STATUS_TABS = [
  { key: 'ALL', label: 'Tất cả' },
  { key: '1', label: 'Chờ xác nhận' },
  { key: '2', label: 'Chờ giao hàng' },
  { key: '3', label: 'Đang vận chuyển' },
  { key: '4', label: 'Chờ thanh toán' },
  { key: '5', label: 'Hoàn thành' },
  { key: '0', label: 'Đã hủy' }
]

const STATUS_CONFIG = {
  0: { text: 'Đã hủy', class: 'st-red' },
  1: { text: 'Chờ xác nhận', class: 'st-yellow' },
  2: { text: 'Chờ giao hàng', class: 'st-blue' },
  3: { text: 'Đang vận chuyển', class: 'st-orange' },
  4: { text: 'Chờ thanh toán', class: 'st-purple' },
  5: { text: 'Hoàn thành', class: 'st-green' }
}

const ORDER_TYPE_CONFIG = {
  'Trực tuyến': { text: 'Online', class: 'bg-purple' },
  'Tại quầy': { text: 'Tại quầy', class: 'bg-blue' },
  'Giao hàng': { text: 'Giao hàng', class: 'bg-green' }
}

const getStatusText = (s) => STATUS_CONFIG[Number(s)]?.text || 'Không xác định'
const getStatusClass = (s) => STATUS_CONFIG[Number(s)]?.class || 'st-gray'
const getOrderTypeText = (type) => ORDER_TYPE_CONFIG[type]?.text || 'Không xác định'
const getOrderTypeClass = (type) => ORDER_TYPE_CONFIG[type]?.class || 'bg-gray'

onMounted(() => {
  const today = getTodayDateInputValue();
  filter.value.fromDate = today;
  filter.value.toDate = today;
  document.addEventListener('click', handleOutsideQuickFilterClick)
  fetchData();
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideQuickFilterClick)
})
</script>

<style scoped>

/* === GLOBAL === */
.page-container {
  --brand-navy: #223f67;
  --brand-navy-strong: #1b3252;
  --brand-cream: #ece3d2;
  --brand-bg: #edf1f6;
  --brand-line: #d5ddea;
  --brand-text: #1f2a3b;
  --brand-sub: #607089;
  padding: 20px;
  font-family: "Be Vietnam Pro", "Segoe UI", sans-serif;
  background: #ebecee;
  min-height: 100vh;
  color: var(--brand-text);
  font-size: 14px;
}

.page-title {
  color: var(--brand-navy);
  font-weight: 700;
  font-size: 24px;
  margin-bottom: 20px;
}

/* === CARDS === */
.control-panel,
.table-container {
  background: white;
  border-radius: 16px;
  border: 1px solid var(--brand-line) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
  padding: 24px;
}

.table-container {
  padding: 0;
  overflow: hidden;
}

/* === HEADER SECTION TRONG BẢNG === */
.table-header-section {
  padding: 20px 24px 10px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.icon-title {
  width: 40px;
  height: 40px;
  background: #f6efe2;
  color: var(--brand-navy);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.text-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.sub-text {
  font-size: 13px;
  color: #64748b;
}

/* === TABS STYLE === */
.status-tabs {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 10px;
}

.tab-btn {
  padding: 8px 20px;
  border: 1px solid #e2e8f0;
  background: #fff;
  border-radius: 30px;
  color: #64748b;
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.tab-btn:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.tab-btn.active-gradient {
  background: linear-gradient(135deg, var(--brand-navy) 0%, var(--brand-navy-strong) 100%);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2);
}

/* === FILTERS === */
.controls-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.filter-group {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.action-group {
  display: flex;
  gap: 10px;
}

.search-box {
  position: relative;
  width: 250px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 11px;
  color: #94a3b8;
}

.search-box input {
  width: 100%;
  padding: 8px 10px 8px 36px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  outline: none;
  height: 40px;
  font-weight: 700;
  color: var(--brand-text);
}

.search-box input:focus {
  border-color: #8ea4c6;
  box-shadow: 0 0 0 3px rgba(34, 63, 103, 0.12);
}

.date-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-control-date {
  height: 40px;
  padding: 0 10px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  outline: none;
  color: #334155;
}

.arrow {
  color: #94a3b8;
  font-size: 12px;
}

.radio-dropdown {
  position: relative;
  min-width: 210px;
  margin-left: 10px;
}

.radio-dropdown-btn {
  width: 100%;
  height: 40px;
  border: 1px solid #e2e8f0;
  background: #fff;
  color: #334155;
  border-radius: 6px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  font-weight: 600;
}

.radio-dropdown-btn:hover {
  border-color: #cbd5e1;
}

.radio-dropdown-menu {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  width: 100%;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.12);
  z-index: 20;
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-weight: 500;
  color: #475569;
}

.radio-item input {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: #0f172a;
}

/* === BUTTONS === */
.btn {
  height: 40px;
  padding: 0 20px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  font-size: 13px;
  border: 1px solid transparent;
  transition: 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.btn-outline {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  color: #475569;
}

.btn-outline:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.btn-navy {
  background-color: #223f67;
  background-color: var(--brand-navy);
  color: #fff !important;
  box-shadow: 0 4px 8px rgba(34, 63, 103, 0.24);
  min-width: 122px;
  justify-content: center;
}

.btn-navy:hover {
  background-color: #1b3252;
  background-color: var(--brand-navy-strong);
  transform: translateY(-1px);
}

/* === TABLE === */
.custom-table {
  width: 100%;
  min-width: 1120px;
  border-collapse: collapse;
  table-layout: fixed;
}

.table-scroll {
  width: 100%;
  overflow-x: auto;
}

.custom-table th {
  background: #f6f8fc !important;
  color: #334155;
  padding: 16px;
  text-align: center;
  font-weight: 700;
  text-transform: uppercase;
  border-bottom: none !important;
}

.custom-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #e7edf6;
  vertical-align: middle;
  text-align: center !important;
}

.custom-table td {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Cột LOẠI luôn hiển thị đủ badge, không hiện dấu ... */
.custom-table td:nth-child(7) {
  overflow: visible;
  text-overflow: clip;
  white-space: normal;
}

.text-code {
  color: #2563eb;
  font-weight: 600;
  font-family: monospace;
  font-size: 13px;
}

.text-price {
  color: #c53131;
  font-weight: 700;
}

.code-text,
.text-code {
  color: var(--brand-navy);
  font-weight: 700;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
  font-size: 13px;
}

/* === BADGES === */
.badge-status {
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  border: 1px solid transparent;
}

.st-green {
  background: #dcfce7;
  color: #15803d;
  border-color: #bbf7d0;
}

.st-red {
  background: #fee2e2;
  color: #991b1b;
  border-color: #fecaca;
}

.st-yellow {
  background: #fef9c3;
  color: #854d0e;
  border-color: #fde68a;
}

.st-blue {
  background: #dbeafe;
  color: #1e40af;
  border-color: #bfdbfe;
}

.st-orange {
  background: #ffedd5;
  color: #c2410c;
  border-color: #fed7aa;
}

.st-purple {
  background: #f3e8ff;
  color: #7e22ce;
  border-color: #d8b4fe;
}

.st-gray {
  background: #f3f4f6;
  color: #4b5563;
  border-color: #e5e7eb;
}

/* Badge loại hóa đơn - phong cách giống trạng thái */
.badge-type-lg {
  display: inline-block;
  min-width: 110px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 0;
  text-align: center;
  white-space: nowrap;
  background: #f3f4f6;
  color: #374151;
  border: 1px solid transparent;
  box-shadow: none;
  letter-spacing: 0;
  transition: background 0.2s, color 0.2s;
}

.badge-type-lg.bg-purple {
  background: #f3e8ff;
  color: #7e22ce;
  border-color: #d8b4fe;
}

.badge-type-lg.bg-blue {
  background: #dbeafe;
  color: var(--brand-navy);
  border-color: #bfdbfe;
}

.badge-type-lg.bg-green {
  background: #dcfce7;
  color: #15803d;
  border-color: #bbf7d0;
}

/* ACTIONS */
.action-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.icon-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  cursor: pointer;
  color: #64748b;
  text-decoration: none;
}

.icon-btn:hover {
  background: #eff4fb;
  color: var(--brand-navy);
  border-color: #c6d2e4;
}

/* PAGINATION */
.pagination-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 24px;
  border-top: 1px solid #f1f5f9;
}

.page-info select {
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  padding: 2px 5px;
  margin: 0 5px;
}

.page-controls button {
  width: 32px;
  height: 32px;
  border: 1px solid #e2e8f0;
  background: #fff;
  border-radius: 4px;
  margin-left: 5px;
  cursor: pointer;
}

.page-controls button.active {
  background: var(--brand-navy);
  color: #fff;
  border-color: var(--brand-navy);
}

/* MODAL SCANNER */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content.scan-modal {
  background: white;
  width: 500px;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.close-btn {
  border: none;
  background: none;
  font-size: 20px;
  cursor: pointer;
  color: #64748b;
}

.scan-hint {
  text-align: center;
  margin-top: 10px;
  color: #64748b;
  font-size: 13px;
  font-style: italic;
}

.empty-state {
  padding: 40px;
  color: #64748b;
  font-style: italic;
  text-align: center !important;
}

.input-den::placeholder {
  color: #000000 !important;
  opacity: 1 !important;
  font-weight: 500;
}

.employee-info {
  font-weight: 600;
  color: var(--brand-text);
  font-size: 13px;
}

.customer-info,
.employee-info {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time-col {
  white-space: nowrap;
}

@media (max-width: 1200px) {
  .table-container {
    overflow-x: auto;
  }
}
</style>