<template>
  <div class="page-container">
    <h1 class="page-title">Quản lý đơn hàng</h1>

    <div class="control-panel">
      <div class="controls-row">
        <div class="filter-group">
          <div class="search-box">
            <i class="fas fa-magnifying-glass search-icon"></i>
            <input 
              class="input-den"
              type="text" 
              v-model="filter.keyword" 
              placeholder="Tìm mã đơn, tên khách..." 
              @keyup.enter="fetchData"
            >
          </div>

          <div class="date-group">
            <input type="date" class="form-control-date" v-model="filter.fromDate" @change="fetchData" />
            <span class="arrow">➜</span>
            <input type="date" class="form-control-date" v-model="filter.toDate" @change="fetchData" />
          </div>

          <div class="radio-group">
            <label class="radio-item">
              <input type="radio" value="" v-model="filter.orderType" @change="fetchData"> 
              <span>Tất cả</span>
            </label>
            <label class="radio-item">
              <input type="radio" value="Trực tuyến" v-model="filter.orderType" @change="fetchData"> 
              <span>Online</span>
            </label>
            <label class="radio-item">
              <input type="radio" value="Tại quầy" v-model="filter.orderType" @change="fetchData"> 
              <span>Tại quầy</span>
            </label>
          </div>
        </div>

        <div class="action-group">
          <button class="btn btn-navy" @click="resetFilter">
            <i class="fas fa-sync-alt"></i> Đặt lại
          </button>
          <button class="btn btn-outline" @click="handleExportExcel">
            <font-awesome-icon :icon="['fas','file-excel']" /> Xuất Excel
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
            <button 
                v-for="(label, key) in STATUS_TABS" 
                :key="key" 
                :class="['tab-btn', { 'active-gradient': filter.activeTab === key }]" 
                @click="changeTab(key)"
            >
                {{ label }}
            </button>
          </div>
      </div>

      <table class="custom-table">
        <thead>
          <tr>
            <th width="5%">STT</th>
            <th width="10%">Mã HĐ</th>
            <th width="8%">Số SP</th>
            <th width="12%">Tổng tiền</th>
            <th width="15%">Khách hàng</th>
            <th width="12%">Ngày tạo</th>
            <th width="10%">Loại</th>
            <th width="15%">Trạng thái</th>
            <th width="8%">Chi tiết</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="9" class="empty-state">Đang tải dữ liệu...</td>
          </tr>
          <tr v-else-if="orders.length === 0">
            <td colspan="9" class="empty-state">Không tìm thấy đơn hàng nào.</td>
          </tr>

          <tr v-else v-for="(order, index) in orders" :key="order.maHoaDon">
            <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
            <td class="code-text">{{ order.maHoaDon }}</td>
            <td>{{ order.tongSanPham }}</td>
            <td class="text-price">{{ formatMoney(order.tongTienSauGiam) }}</td>
            
            <td>
              <div class="customer-info">
                {{ order.tenKhachHang || 'Khách lẻ' }}
              </div>
            </td>
            
            <td class="time-col">{{ formatDate(order.ngayTao) }}</td>
            
            <td>
              <span class="badge-type-lg" :class="order.loaiHoaDon === 'TRUC_TUYEN' ? 'bg-purple' : 'bg-blue'">
                {{ order.loaiHoaDon === 'TRUC_TUYEN' ? 'Online' : 'Tại quầy' }}
              </span>
            </td>
            
            <td>
              <span class="badge-status" :class="getStatusClass(order.trangThai)">
                {{ getStatusText(order.trangThai) }}
              </span>
            </td>

            <td class="action-col">
              <div class="action-wrapper">
                <router-link 
                  :to="{ name: 'admin-order-detail', params: { id: order.maHoaDon } }" 
                  class="icon-btn" 
                  title="Xem chi tiết"
                >
                  <i class="far fa-eye"></i> 
                </router-link>
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
          kết quả / trang
        </div>
        <div class="page-controls">
          <button :disabled="currentPage === 1" @click="changePage(currentPage - 1)">‹</button>
          <button 
            v-for="p in visiblePages" 
            :key="p" 
            :class="{ active: p === currentPage }" 
            @click="changePage(p)"
          >
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
import { ref, shallowRef, onMounted, nextTick, computed } from 'vue' // Bổ sung shallowRef
import { useRouter } from 'vue-router'
import { fetchOrders, exportOrders } from '@/api/HoaDonApi'
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
const showScanModal = ref(false)
let html5QrcodeScanner = null 

// 2. TỐI ƯU FORMATTER: Khởi tạo 1 lần duy nhất bên ngoài để tái sử dụng
const moneyFormatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });
const dateFormatter = new Intl.DateTimeFormat('vi-VN', { 
    year: 'numeric', month: '2-digit', day: '2-digit', 
    hour: '2-digit', minute: '2-digit' 
});

const formatMoney = (val) => val ? moneyFormatter.format(val) : '0 đ';

const formatDate = (val) => {
  if (!val) return '';
  let dateObj;
  if (Array.isArray(val)) {
      // Xử lý mảng [năm, tháng, ngày, giờ, phút] từ Backend Java trả về
      dateObj = new Date(val[0], val[1] - 1, val[2], val[3]||0, val[4]||0);
  } else {
      dateObj = new Date(val);
  }
  return isNaN(dateObj.getTime()) ? '' : dateFormatter.format(dateObj);
}

// --- LOGIC MODAL & QR ---
const resetFilter = () => {
    filter.value = { keyword: '', fromDate: '', toDate: '', orderType: '', activeTab: 'ALL' };
    currentPage.value = 1;
    fetchData();
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
    html5QrcodeScanner = new Html5QrcodeScanner( "qr-reader", { fps: 10, qrbox: { width: 250, height: 250 } }, false );
    html5QrcodeScanner.render(onScanSuccess, onScanFailure);
}
const onScanFailure = (error) => {}

const onScanSuccess = (decodedText, decodedResult) => {
    closeScanModal();
    if(decodedText) {
        router.push({ name: 'admin-order-detail', params: { id: decodedText } });
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
    const fileName = `DanhSachDonHang_${new Date().toISOString().slice(0,10)}.xlsx`;
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
const STATUS_TABS = { 
  'ALL': 'Tất cả', 
  '1': 'Chờ xác nhận', 
  '2': 'Chờ giao', 
  '3': 'Đang giao', 
  '4': 'Chờ thanh toán',
  '5': 'Hoàn thành',
  '0': 'Đã hủy'
}

const STATUS_CONFIG = { 
    0: {text:'Đã hủy', class:'st-red'}, 
    1: {text:'Chờ xác nhận', class:'st-yellow'}, 
    2: {text:'Chờ giao', class:'st-blue'}, 
    3: {text:'Đang giao', class:'st-orange'}, 
    4: {text:'Chờ thanh toán', class:'st-purple'},
    5: {text:'Hoàn thành', class:'st-green'}
}

const getStatusText = (s) => STATUS_CONFIG[Number(s)]?.text || 'Không xác định'
const getStatusClass = (s) => STATUS_CONFIG[Number(s)]?.class || 'st-gray'

onMounted(() => { fetchData(); })
</script>

<style scoped>
/* === GLOBAL === */
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background: #ebecee; min-height: 100vh; color: #333; font-size: 14px; }
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }

/* === CARDS === */
.control-panel, .table-container { 
    background: white; border-radius: 16px; border: 1px solid #bfdbfe !important; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); margin-bottom: 20px; padding: 24px; 
}
.table-container { padding: 0; overflow: hidden; }

/* === HEADER SECTION TRONG BẢNG === */
.table-header-section { padding: 20px 24px 10px 24px; border-bottom: 1px solid #f1f5f9; }
.section-title { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.icon-title { width: 40px; height: 40px; background: #ffe4e6; color: #e11d48; border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 18px; }
.text-title h3 { margin: 0; font-size: 16px; font-weight: 700; color: #0f172a; }
.sub-text { font-size: 13px; color: #64748b; }

/* === TABS STYLE === */
.status-tabs { display: flex; gap: 10px; overflow-x: auto; padding-bottom: 10px; }
.tab-btn { padding: 8px 20px; border: 1px solid #e2e8f0; background: #fff; border-radius: 30px; color: #64748b; font-weight: 600; font-size: 13px; cursor: pointer; transition: all 0.2s; white-space: nowrap; }
.tab-btn:hover { background: #f1f5f9; color: #0f172a; }
.tab-btn.active-gradient { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; border-color: transparent; box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); }

/* === FILTERS === */
.controls-row { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; }
.filter-group { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.action-group { display: flex; gap: 10px; }

.search-box { position: relative; width: 250px; }
.search-icon { position: absolute; left: 12px; top: 11px; color: #94a3b8; }
.search-box input { width: 100%; padding: 8px 10px 8px 36px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; height: 40px; font-weight: 700; color: #0f172a; }
.search-box input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }

.date-group { display: flex; align-items: center; gap: 8px; }
.form-control-date { height: 40px; padding: 0 10px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; color: #334155; }
.arrow { color: #94a3b8; font-size: 12px; }

.radio-group { display: flex; gap: 15px; margin-left: 10px; }
.radio-item { display: flex; align-items: center; gap: 6px; cursor: pointer; font-weight: 500; color: #475569; }
.radio-item input { width: 16px; height: 16px; cursor: pointer; accent-color: #0f172a; }

/* === BUTTONS === */
.btn { height: 40px; padding: 0 20px; border-radius: 6px; font-weight: 600; cursor: pointer; font-size: 13px; border: 1px solid transparent; transition: 0.2s; display: inline-flex; align-items: center; gap: 8px; text-decoration: none; }
.btn-outline { background: #ffffff; border: 1px solid #e2e8f0; color: #475569; }
.btn-outline:hover { background: #f8fafc; border-color: #cbd5e1; }
.btn-navy { background-color: #0f172a; color: #fff; box-shadow: 0 4px 6px rgba(15, 23, 42, 0.2); }
.btn-navy:hover { background-color: #1e293b; transform: translateY(-1px); }

/* === TABLE === */
.custom-table { width: 100%; border-collapse: collapse; }
.custom-table th { background:#f5f5f5 !important; color: #000000; padding: 16px; text-align: center; font-weight: 700; text-transform: uppercase; border-bottom: none !important; }
.custom-table td { padding: 14px 16px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; text-align: center !important; }

.text-code { color: #2563eb; font-weight: 600; font-family: monospace; font-size: 13px; }
.text-price { color: #ef4444; font-weight: 700; }

/* === BADGES === */
.badge-status { padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; white-space: nowrap; border: 1px solid transparent; }
.st-green { background: #dcfce7; color: #15803d; border-color: #bbf7d0; }
.st-red { background: #fee2e2; color: #991b1b; border-color: #fecaca; }
.st-yellow { background: #fef9c3; color: #854d0e; border-color: #fde68a; }
.st-blue { background: #dbeafe; color: #1e40af; border-color: #bfdbfe; }
.st-orange { background: #ffedd5; color: #c2410c; border-color: #fed7aa; }
.st-purple { background: #f3e8ff; color: #7e22ce; border-color: #d8b4fe; }
.st-gray { background: #f3f4f6; color: #4b5563; border-color: #e5e7eb; }

.badge-type-lg { font-size: 12px; padding: 6px 14px; border-radius: 20px; font-weight: 600; display: inline-block; }
.bg-purple { background: #f3e8ff; color: #7e22ce; border: 1px solid #d8b4fe; }
.bg-blue { background: #e0f2fe; color: #0369a1; border: 1px solid #bae6fd; }

/* ACTIONS */
.action-wrapper { display: flex; align-items: center; justify-content: center; gap: 10px; }
.icon-btn { width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; background: white; border: 1px solid #e2e8f0; border-radius: 6px; cursor: pointer; color: #64748b; text-decoration: none; }
.icon-btn:hover { background: #f1f5f9; color: #0f172a; border-color: #cbd5e1; }

/* PAGINATION */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; padding: 15px 24px; border-top: 1px solid #f1f5f9; }
.page-info select { border: 1px solid #e2e8f0; border-radius: 4px; padding: 2px 5px; margin: 0 5px; }
.page-controls button { width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; margin-left: 5px; cursor: pointer; }
.page-controls button.active { background: #0f172a; color: #fff; border-color: #0f172a; }

/* MODAL SCANNER */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content.scan-modal { background: white; width: 500px; padding: 20px; border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
.modal-header { display: flex; justify-content: space-between; margin-bottom: 15px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
.close-btn { border: none; background: none; font-size: 20px; cursor: pointer; color: #64748b; }
.scan-hint { text-align: center; margin-top: 10px; color: #64748b; font-size: 13px; font-style: italic; }
.empty-state { padding: 40px; color: #64748b; font-style: italic; text-align: center !important; }
.input-den::placeholder { color: #000000 !important; opacity: 1 !important; font-weight: 500; }
</style>