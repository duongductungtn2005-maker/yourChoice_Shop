<template>
  <div class="orders-page">
    <h2 class="page-title">Quản lý đơn hàng</h2>

    <div class="control-panel">
      <div class="search-group">
        <i class="fas fa-search search-icon"></i>
        <input 
          class="search-input" 
          placeholder="Tìm kiếm hoá đơn..." 
          v-model="filter.keyword" 
          @keyup.enter="fetchData"
        />
      </div>

      <div class="action-group">
        <button class="btn btn-outline" @click="openScanModal">
          <i class="fas fa-qrcode"></i> Quét mã
        </button>

        <button class="btn btn-primary" @click="handleCreateOrder">
          <i class="fas fa-plus"></i> Tạo hoá đơn
        </button>
      </div>
    </div>

    <div class="filter-panel">
      <div class="filter-left">
        <div class="date-group">
          <input type="date" class="date-input" v-model="filter.fromDate" @change="fetchData" />
          <span class="arrow">➜</span>
          <input type="date" class="date-input" v-model="filter.toDate" @change="fetchData" />
        </div>

        <div class="radio-group">
          <span class="radio-label">Loại:</span>
          <label><input type="radio" value="" v-model="filter.orderType" @change="fetchData" /> Tất cả</label>
          <label><input type="radio" value="Trực tuyến" v-model="filter.orderType" @change="fetchData" /> Trực tuyến</label>
          <label><input type="radio" value="Tại quầy" v-model="filter.orderType" @change="fetchData" /> Tại quầy</label>
        </div>
      </div>

      <div class="filter-right">
        <button class="btn btn-outline" @click="handleExportExcel">
          <font-awesome-icon :icon="['fas','file-excel']" /> Xuất Excel
        </button>
      </div>
    </div>

    <div class="tabs">
      <span 
        v-for="(label, key) in STATUS_TABS" 
        :key="key" 
        :class="['tab', { active: filter.activeTab === key }]" 
        @click="changeTab(key)"
      >
        {{ label }}
      </span>
    </div>

    <div class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>Mã HĐ</th>
            <th>Tổng SP</th>
            <th>Tổng tiền</th>
            <th>Tên khách hàng</th>
            <th>Ngày tạo</th>
            <th>Loại hoá đơn</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
             <td colspan="9" style="text-align: center; padding: 20px;">Đang tải dữ liệu...</td>
          </tr>

          <tr v-else v-for="(order, index) in orders" :key="order.maHoaDon">
            <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
            <td style="color: #334155;">{{ order.maHoaDon }}</td>
            <td>{{ order.tongSanPham }}</td>
            <td style="color: #dc2626;">{{ formatMoney(order.tongTienSauGiam) }}</td>
            <td><span class="badge-customer">{{ order.tenKhachHang || 'Khách lẻ' }}</span></td>
            <td class="col-ngay-tao">{{ formatDate(order.ngayTao) }}</td>
            <td>
              <span class="badge" :class="order.loaiHoaDon === 'Trực tuyến' ? 'purple' : 'green'">
                {{ order.loaiHoaDon }}
              </span>
            </td>
            <td>
              <span class="badge" :class="getStatusClass(order.trangThai)">
                {{ getStatusText(order.trangThai) }}
              </span>
            </td>
           <td>
  <router-link 
    :to="{ name: 'admin-order-detail', params: { id: order.maHoaDon } }" 
    class="action-btn" 
    title="Xem chi tiết"
  >
    <i class="far fa-pen-to-square"></i> 
  </router-link>
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
            đơn hàng / trang
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
// --- PHẦN SCRIPT GIỮ NGUYÊN KHÔNG THAY ĐỔI ---
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRouter } from 'vue-router'
import { fetchOrders, exportOrders } from '@/api/HoaDonApi'
import { Html5QrcodeScanner } from "html5-qrcode"
import Swal from 'sweetalert2'

const router = useRouter()
const orders = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(5)
const totalPages = ref(1)
const filter = ref({ keyword: '', fromDate: '', toDate: '', orderType: '', activeTab: 'ALL' })
const showScanModal = ref(false)
let html5QrcodeScanner = null 

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
        alert("Đã tìm thấy đơn hàng: " + decodedText);
        router.push({ name: 'admin-order-detail', params: { id: decodedText } });
    }
}

const onScanFailure = (error) => {}

const handleCreateOrder = () => { router.push({ name: 'admin-pos' }); }
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

// Trang hiển thị giống màn Sản phẩm
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
const STATUS_TABS = { 'ALL': 'TẤT CẢ', '0': 'ĐÃ HỦY', '1': 'CHỜ XÁC NHẬN', '2': 'CHỜ GIAO HÀNG', '3': 'ĐANG VẬN CHUYỂN', '4': 'ĐÃ GIAO HÀNG', '5': 'ĐÃ THANH TOÁN', '6': 'HOÀN THÀNH' }
const STATUS_CONFIG = { 0: {text:'Đã hủy',class:'red'}, 1: {text:'Chờ xác nhận',class:'yellow'}, 2: {text:'Chờ giao hàng',class:'blue'}, 3: {text:'Đang vận chuyển',class:'orange'}, 4: {text:'Hoàn thành',class:'pink'} }
const getStatusText = (s) => STATUS_CONFIG[s]?.text || 'Unknown';
const getStatusClass = (s) => STATUS_CONFIG[s]?.class || 'gray';
onMounted(() => { fetchData(); })
</script>

<style scoped>
/* --- STYLES CŨ (LAYOUT) GIỮ NGUYÊN --- */
.orders-page { background: #f8fafc; padding: 20px; min-height: 100vh; font-family: 'Segoe UI', sans-serif; }
.page-title { font-size: 24px; font-weight: 700; color: #1e293b; margin-bottom: 20px; }
.control-panel { background: white; padding: 15px 20px; border-radius: 8px 8px 0 0; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #f1f5f9; }
.search-group { position: relative; width: 40%; }
.search-input { width: 100%; padding: 10px 10px 10px 35px; border: 1px solid #e2e8f0; border-radius: 6px; font-size: 14px; outline: none; transition: 0.3s; }
.search-input:focus { border-color: #2563eb; } /* Đổi màu focus sang xanh cho đồng bộ */
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; }
.action-group { display: flex; gap: 10px; }
.filter-panel { background: white; padding: 15px 20px; border-radius: 0 0 8px 8px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.filter-left { display: flex; gap: 20px; align-items: center; }
.date-group { display: flex; align-items: center; gap: 10px; }
.date-input { padding: 8px; border: 1px solid #e2e8f0; border-radius: 6px; color: #475569; }
.col-ngay-tao { color: #000; }
.arrow { color: #cbd5e1; font-size: 12px; }
.radio-group { display: flex; align-items: center; gap: 15px; font-size: 14px; color: #475569; }
.radio-group label { display: flex; align-items: center; gap: 5px; cursor: pointer; }
.radio-label { font-weight: 500; color: #334155; }

/* --- BUTTON STYLES MỚI (GIỐNG MÀN CỔ ÁO) --- */
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

/* Nút chính (Tạo mới) - Màu xanh đậm */
.btn-primary {
  background-color: #0f172a; 
  color: #ffffff;
  border-color: #0f172a;
}
.btn-primary:hover {
  background-color: #1e293b;
  border-color: #1e293b;
}
/* Nút phụ (Quét mã, Xuất Excel) - Nền trắng, viền xám */
.btn-outline { 
    background: #fff; 
    border-color: #cbd5e1; 
    color: #475569; 
}
.btn-outline:hover { 
    background: #f1f5f9; 
    border-color: #94a3b8;
}
/* --- CÁC STYLE KHÁC GIỮ NGUYÊN --- */
.tabs { background: white; padding: 0 20px; border-radius: 8px; display: flex; gap: 25px; margin-bottom: 2px; border-bottom: 1px solid #f1f5f9; }
.tab { padding: 15px 0; color: #64748b; font-weight: 500; font-size: 13px; cursor: pointer; border-bottom: 2px solid transparent; text-transform: uppercase; transition: 0.3s; }
.tab:hover { color: #3b82f6; }
.tab.active { color: #3b82f6; border-bottom-color: #3b82f6; }
.table-wrapper { background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
table { width: 100%; border-collapse: collapse; }
th { background: #E9F1FB; color: #1E3A8A; font-weight: 700; font-size: 13px; padding: 15px; text-align: center; border-bottom: 1px solid #e2e8f0; }
td { padding: 15px; border-bottom: 1px solid #f1f5f9; color: #334155; font-size: 14px; font-weight: 400; text-align: center; vertical-align: middle; }
.badge { padding: 5px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; display: inline-block; }
.badge.red { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
.badge.yellow { background: #fef9c3; color: #854d0e; border: 1px solid #fde68a; }
.badge.blue { background: #e0f2fe; color: #0369a1; border: 1px solid #bae6fd; }
.badge.orange { background: #ffedd5; color: #c2410c; border: 1px solid #fed7aa; }
.badge.pink { background: #fce7f3; color: #be185d; border: 1px solid #fbcfe8; }
.badge.green { background: #dcfce7; color: #15803d; border: 1px solid #bbf7d0; }
.badge.purple { background: #f3e8ff; color: #7e22ce; border: 1px solid #d8b4fe; }
.badge-customer { background: #f1f5f9; color: #475569; padding: 4px 10px; border-radius: 12px; font-size: 13px; border: 1px solid #e2e8f0; }
/* --- ACTION BUTTON STYLE (Giống màn Cổ áo) --- */
.action-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 18px;
  color: #475569; /* Màu xám xanh */
  transition: all 0.2s ease;
  text-decoration: none; /* Quan trọng: Bỏ gạch chân vì là thẻ a/router-link */
  display: inline-flex; /* Để căn chỉnh icon tốt hơn */
  align-items: center;
  justify-content: center;
}

.action-btn:hover {
  color: #0f172a; /* Màu đậm hơn khi hover */
  transform: scale(1.1); /* Phóng to nhẹ */
}

/* Pagination (đồng bộ màn Sản phẩm) */
.pagination-footer { display: flex; justify-content: space-between; align-items: center; padding: 15px 20px; border-top: 1px solid #f1f5f9; }
.page-info { font-size: 14px; color: #64748b; }
.page-info select { padding: 4px 8px; border: 1px solid #cbd5e1; border-radius: 4px; margin: 0 5px; outline: none; }
.page-controls button { width: 32px; height: 32px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; margin-left: 5px; cursor: pointer; color: #64748b; }
.page-controls button.active { background: #0f172a; color: #fff; border-color: #0f172a; }
.page-controls button:disabled { opacity: 0.5; cursor: not-allowed; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content.scan-modal { background: white; width: 500px; padding: 20px; border-radius: 12px; }
.modal-header { display: flex; justify-content: space-between; margin-bottom: 15px; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; }
.scan-hint { text-align: center; margin-top: 10px; color: #64748b; font-size: 14px; }
</style>