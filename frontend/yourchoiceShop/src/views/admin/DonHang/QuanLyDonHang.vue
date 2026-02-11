<template>
  <div class="page-container">
    <h1 class="page-title">Quản lý đơn hàng</h1>

    <div class="card">
      <div class="control-panel">
        
        <div class="controls-row">
          <div class="filter-group">
             <div class="search-wrap">
                <i class="fas fa-search search-icon"></i>
                <input 
                  class="search-input" 
                  placeholder="Tìm mã đơn, tên khách..." 
                  v-model="filter.keyword" 
                  @keyup.enter="fetchData"
                />
             </div>

             <div class="date-group">
                <div class="date-input-wrapper">
                   <input type="date" class="date-input" v-model="filter.fromDate" @change="fetchData" />
                </div>
                <span class="arrow">➜</span>
                <div class="date-input-wrapper">
                   <input type="date" class="date-input" v-model="filter.toDate" @change="fetchData" />
                </div>
             </div>

             <div class="radio-group">
                <label class="radio-item">
                   <input type="radio" value="" v-model="filter.orderType" @change="fetchData" /> 
                   <span>Tất cả</span>
                </label>
                <label class="radio-item">
                   <input type="radio" value="Trực tuyến" v-model="filter.orderType" @change="fetchData" /> 
                   <span>Online</span>
                </label>
                <label class="radio-item">
                   <input type="radio" value="Tại quầy" v-model="filter.orderType" @change="fetchData" /> 
                   <span>Tại quầy</span>
                </label>
             </div>
          </div>

          <div class="action-group">
             <button class="btn btn-secondary" @click="resetFilter">
                <i class="fas fa-sync-alt"></i> Đặt lại
             </button>

             <button class="btn btn-outline" @click="openScanModal">
                <i class="fas fa-qrcode"></i> Quét mã
             </button>

             <button class="btn btn-outline" @click="handleExportExcel">
                <font-awesome-icon :icon="['fas','file-excel']" /> Xuất Excel
             </button>
          </div>
        </div>

        <div class="status-tabs-wrapper">
           <div class="status-tabs">
              <button 
                v-for="(label, key) in STATUS_TABS" 
                :key="key" 
                :class="['tab-btn', { 'active': filter.activeTab === key }]" 
                @click="changeTab(key)"
              >
                {{ label }}
              </button>
           </div>
        </div>
      </div>

      <div class="table-container">
        <div class="table-responsive">
          <table>
            <thead>
              <tr>
                <th width="50" class="text-center">STT</th>
                <th>Mã HĐ</th>
                <th class="text-center">Số SP</th>
                <th class="text-right">Tổng tiền</th>
                <th>Khách hàng</th>
                <th class="text-center">Ngày tạo</th>
                <th class="text-center">Loại</th>
                <th class="text-center">Trạng thái</th>
                <th class="text-center">HĐ</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                 <td colspan="9" class="text-center py-5">Đang tải dữ liệu...</td>
              </tr>
              <tr v-else-if="orders.length === 0">
                 <td colspan="9" class="text-center py-5 empty-state">Không tìm thấy đơn hàng nào.</td>
              </tr>

              <tr v-else v-for="(order, index) in orders" :key="order.maHoaDon">
                <td class="text-center">{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                <td class="text-code">{{ order.maHoaDon }}</td>
                <td class="text-center">{{ order.tongSanPham }}</td>
                <td class="text-right text-price">{{ formatMoney(order.tongTienSauGiam) }}</td>
                
                <td>
                    <div class="customer-info">
                        {{ order.tenKhachHang || 'Khách lẻ' }}
                    </div>
                </td>
                
                <td class="text-center col-ngay-tao">{{ formatDate(order.ngayTao) }}</td>
                
                <td class="text-center">
                  <span class="badge-type" :class="order.loaiHoaDon === 'Trực tuyến' ? 'bg-purple' : 'bg-blue'">
                    {{ order.loaiHoaDon === 'Trực tuyến' ? 'Online' : 'Tại quầy' }}
                  </span>
                </td>
                
                <td class="text-center">
                  <span class="badge-status" :class="getStatusClass(order.trangThai)">
                    {{ getStatusText(order.trangThai) }}
                  </span>
                </td>
                
                <td class="text-center">
                  <router-link 
                    :to="{ name: 'admin-order-detail', params: { id: order.maHoaDon } }" 
                    class="action-btn" 
                    title="Xem chi tiết"
                  >
                    <i class="far fa-eye"></i> 
                  </router-link>
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
              đơn hàng / trang
          </div>
          <div class="page-controls">
              <button class="page-btn" :disabled="currentPage === 1" @click="changePage(currentPage - 1)">‹</button>
              <button
                v-for="p in visiblePages"
                :key="p"
                :class="['page-btn', { active: p === currentPage }]"
                @click="changePage(p)"
              >
                {{ p }}
              </button>
              <button class="page-btn" :disabled="currentPage >= totalPages" @click="changePage(currentPage + 1)">›</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showScanModal" class="modal-overlay" @click.self="closeScanModal">
      <div class="modal-content scan-modal">
        <div class="modal-header">
          <h3>Quét mã QR Hóa đơn</h3>
          <button @click="closeScanModal" class="close-btn">&times;</button>
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
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { fetchOrders, exportOrders } from '@/api/HoaDonApi'
import { Html5QrcodeScanner } from "html5-qrcode"
import Swal from 'sweetalert2'

const router = useRouter()
const orders = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = ref(1)
const filter = ref({ keyword: '', fromDate: '', toDate: '', orderType: '', activeTab: 'ALL' })
const showScanModal = ref(false)
let html5QrcodeScanner = null 

// --- RESET FILTER ---
const resetFilter = () => {
    filter.value = { keyword: '', fromDate: '', toDate: '', orderType: '', activeTab: 'ALL' };
    currentPage.value = 1;
    fetchData();
}

// ... (Các hàm QR Code giữ nguyên) ...
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

const onScanSuccess = (decodedText, decodedResult) => {
    closeScanModal();
    if(decodedText) {
        router.push({ name: 'admin-order-detail', params: { id: decodedText } });
    }
}

const onScanFailure = (error) => {}

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
const formatMoney = (val) => val ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val) : '0 đ';
const formatDate = (val) => {
  if (!val) return '';
  let date = Array.isArray(val) ? new Date(val[0], val[1] - 1, val[2], val[3]||0, val[4]||0) : new Date(val);
  return isNaN(date.getTime()) ? '' : date.toLocaleString('vi-VN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}

// Cấu hình Tabs & Trạng thái
const STATUS_TABS = { 
    'ALL': 'Tất cả', 
    '1': 'Chờ xác nhận', 
    '2': 'Chờ giao hàng', 
    '3': 'Đang vận chuyển', 
    '4': 'Hoàn thành', 
    '0': 'Đã hủy'
}

const STATUS_CONFIG = { 
    0: {text:'Đã hủy', class:'st-red'}, 
    1: {text:'Chờ xác nhận', class:'st-yellow'}, 
    2: {text:'Chờ giao', class:'st-blue'}, 
    3: {text:'Đang giao', class:'st-orange'}, 
    4: {text:'Hoàn thành', class:'st-green'},
    5: {text:'Đã thanh toán', class:'st-purple'},
    6: {text:'Hoàn trả', class:'st-gray'}
}
const getStatusText = (s) => STATUS_CONFIG[s]?.text || 'Khác';
const getStatusClass = (s) => STATUS_CONFIG[s]?.class || 'st-gray';

onMounted(() => { fetchData(); })
</script>

<style scoped>
.page-container { padding: 20px; font-family: 'Segoe UI', sans-serif; background-color: #f8fafc; min-height: 100vh; }
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; margin-bottom: 20px; }

/* === CARD STYLE (Viền xanh + Bo góc) === */
.card { 
    background: #fff; 
    border-radius: 16px; 
    border: 1px solid #bfdbfe !important; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.05); 
    padding: 24px; 
    margin-bottom: 20px;
}

/* === CONTROL PANEL === */
.control-panel { margin-bottom: 10px; }
.controls-row { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 15px; margin-bottom: 20px; }
.filter-group { display: flex; gap: 15px; align-items: center; flex-wrap: wrap; }

.search-wrap { position: relative; width: 280px; }
.search-input { width: 100%; padding: 10px 12px 10px 36px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; font-size: 14px; }
.search-input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; }

.date-group { display: flex; align-items: center; gap: 8px; }
.date-input { padding: 9px; border: 1px solid #e2e8f0; border-radius: 6px; color: #475569; outline: none; font-size: 13px; }
.date-input:focus { border-color: #3b82f6; }
.arrow { color: #cbd5e1; font-size: 12px; }

.radio-group { display: flex; align-items: center; gap: 15px; font-size: 14px; margin-left: 10px; }
.radio-item { cursor: pointer; display: flex; align-items: center; gap: 6px; color: #475569; font-weight: 500; }
.radio-item input { accent-color: #0f172a; width: 16px; height: 16px; cursor: pointer; }

/* === ACTION BUTTONS === */
.action-group { display: flex; gap: 10px; }
.btn { height: 38px; padding: 0 16px; border-radius: 6px; font-weight: 600; cursor: pointer; font-size: 13px; border: 1px solid transparent; transition: 0.2s; display: flex; align-items: center; gap: 8px; }
.btn-secondary { background: #334155; color: #fff; } .btn-secondary:hover { background: #1e293b; }
.btn-outline { background: #fff; border-color: #cbd5e1; color: #475569; } .btn-outline:hover { background: #f8fafc; border-color: #94a3b8; color: #0f172a; }

/* === STATUS TABS (Kiểu PILLS XANH DƯƠNG) === */
.status-tabs-wrapper { margin-bottom: 10px; overflow-x: auto; padding-bottom: 5px; }
.status-tabs { display: flex; gap: 8px; }
.tab-btn { 
    padding: 8px 16px; 
    border: 1px solid #e2e8f0; 
    background: #fff; 
    border-radius: 20px; /* Bo tròn kiểu pill */
    color: #64748b; 
    font-weight: 600; 
    font-size: 13px; 
    cursor: pointer; 
    transition: all 0.2s; 
    white-space: nowrap;
}
.tab-btn:hover { background: #eef2ff; color: #3b82f6; border-color: #bfdbfe; }

/* Active State: Xanh Dương (Blue) */
.tab-btn.active { 
    background-color: #3b82f6; /* Màu nền xanh */
    color: #fff; /* Chữ trắng */
    border-color: #3b82f6; 
    box-shadow: 0 2px 6px rgba(59, 130, 246, 0.3);
}

/* === TABLE STYLES === */
.table-container { border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden; }
.table-responsive { width: 100%; overflow-x: auto; }
table { width: 100%; border-collapse: collapse; }
th { background: #eff6ff; padding: 14px; font-weight: 700; color: #1e40af; font-size: 13px; text-transform: uppercase; text-align: left; border-bottom: none; }
td { padding: 12px 14px; border-bottom: 1px solid #f1f5f9; font-size: 14px; vertical-align: middle; color: #334155; }
.text-center { text-align: center; } .text-right { text-align: right; }
.text-code { color: #2563eb; font-family: monospace; font-weight: 600; }
.text-price { color: #ef4444; font-weight: 600; }
.empty-state { font-style: italic; color: #94a3b8; }

/* BADGES TRẠNG THÁI */
.badge-status { padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 600; border: 1px solid transparent; }
.st-green { background: #dcfce7; color: #15803d; border-color: #bbf7d0; }
.st-red { background: #fee2e2; color: #991b1b; border-color: #fecaca; }
.st-yellow { background: #fef9c3; color: #854d0e; border-color: #fde68a; }
.st-blue { background: #dbeafe; color: #1e40af; border-color: #bfdbfe; }
.st-orange { background: #ffedd5; color: #c2410c; border-color: #fed7aa; }
.st-purple { background: #f3e8ff; color: #7e22ce; border-color: #d8b4fe; }
.st-gray { background: #f3f4f6; color: #4b5563; border-color: #e5e7eb; }

/* BADGE TYPE */
.badge-type { font-size: 11px; padding: 2px 8px; border-radius: 12px; font-weight: 500; }
.bg-purple { background: #f3e8ff; color: #7e22ce; }
.bg-blue { background: #e0f2fe; color: #0369a1; }

.action-btn { width: 32px; height: 32px; border-radius: 4px; border: 1px solid #e2e8f0; background: #fff; cursor: pointer; color: #475569; transition: 0.2s; display: inline-flex; align-items: center; justify-content: center; }
.action-btn:hover { background: #f1f5f9; color: #0f172a; border-color: #cbd5e1; }

/* PAGINATION */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; padding: 15px 20px; border-top: 1px solid #f1f5f9; }
.page-info { font-size: 13px; color: #64748b; font-weight: 500; }
.page-info select { padding: 4px 8px; border: 1px solid #cbd5e1; border-radius: 4px; margin: 0 5px; outline: none; cursor: pointer; }
.page-controls { display: flex; gap: 5px; }
.page-btn { min-width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; cursor: pointer; color: #64748b; font-weight: 500; display: flex; align-items: center; justify-content: center; }
.page-btn:hover:not(:disabled) { border-color: #0f172a; color: #0f172a; }
.page-btn.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; background: #f8fafc; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content.scan-modal { background: white; width: 500px; padding: 20px; border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
.modal-header { display: flex; justify-content: space-between; margin-bottom: 15px; }
.modal-header h3 { margin: 0; font-size: 18px; color: #0f172a; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #94a3b8; }
.close-btn:hover { color: #ef4444; }
.scan-hint { text-align: center; margin-top: 10px; color: #64748b; font-size: 14px; }
</style>