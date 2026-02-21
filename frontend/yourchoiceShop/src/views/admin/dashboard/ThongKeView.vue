<template>
  <div class="dashboard-container">
    <h2 class="page-title">Thống kê</h2>

    <div class="summary-grid">
      <div class="stat-card" v-for="(item, index) in summaryCards" :key="index">
        <h3 class="stat-title">{{ item.title }}</h3>
        <p class="stat-value">{{ formatCurrency(item.revenue) }}</p>
        
        <div class="stat-details">
          <div class="detail-item">
            <span class="label">Sản phẩm</span>
            <span class="value">{{ item.products }}</span>
          </div>
          <div class="detail-item">
            <span class="label">Thành công</span>
            <span class="value text-success">{{ item.successOrders }}</span>
          </div>
          <div class="detail-item">
            <span class="label">Đơn hủy</span>
            <span class="value text-danger">{{ item.cancelOrders || 0 }}</span>
          </div>
          <div class="detail-item">
            <span class="label">Đơn trả</span>
            <span class="value text-warning">{{ item.returnOrders || 0 }}</span>
          </div>
        </div>

        <div v-if="isLoadingCards" class="loading-overlay">
           <div class="spinner"></div>
        </div>
      </div>
    </div>

    <div class="filter-card">
      <div class="filter-header">
        <div class="filter-left">
            <h3 class="filter-title">Bộ lọc</h3>
            <div class="button-group">
              <button class="btn btn-outline" :class="{ 'btn-active': activeFilter === 'DAY' }" @click="applyQuickFilter('DAY')">Ngày</button>
              <button class="btn btn-outline" :class="{ 'btn-active': activeFilter === 'WEEK' }" @click="applyQuickFilter('WEEK')">Tuần</button>
              <button class="btn btn-outline" :class="{ 'btn-active': activeFilter === 'MONTH' }" @click="applyQuickFilter('MONTH')">Tháng</button>
              <button class="btn btn-outline" :class="{ 'btn-active': activeFilter === 'YEAR' }" @click="applyQuickFilter('YEAR')">Năm</button>
              <button class="btn btn-primary" :class="{ 'btn-active-primary': activeFilter === 'CUSTOM' }" @click="applyCustomFilter">Tùy chỉnh</button>
            </div>
            
            <div class="date-picker-group">
              <input type="date" v-model="filter.fromDate" class="date-input" @change="activeFilter = 'CUSTOM'" />
              <span class="separator">-</span>
              <input type="date" v-model="filter.toDate" class="date-input" @change="activeFilter = 'CUSTOM'" />
            </div>
        </div>
        
        <button @click="handleExportExcel" class="btn btn-excel">
          Export to Excel
        </button>
      </div>
    </div>

    <div class="row-layout mb-24">
      
      <div class="table-card table-top-products">
        <div class="card-header">Danh sách sản phẩm bán chạy theo tùy chỉnh</div>
        <div class="table-responsive">
          <table>
            <thead>
              <tr>
                <th class="text-center w-80">Ảnh</th>
                <th>Tên sản phẩm</th>
                <th class="text-center w-100">Số lượng</th>
                <th class="text-right w-120">Giá tiền</th>
                <th class="text-center w-100">Kích cỡ</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="topProducts.length === 0">
                <td colspan="5" class="empty-state">
                  <p>Chưa có dữ liệu sản phẩm bán chạy</p>
                </td>
              </tr>
              <tr v-for="(prod, i) in topProducts" :key="i">
                <td class="text-center">
                  <img v-if="prod?.anh" :src="prod.anh" class="product-img" />
                  <div v-else class="product-img no-img">No Img</div>
                </td>
                <td class="font-medium">{{ prod?.tenSanPham }}</td>
                <td class="text-center font-bold text-primary">{{ prod?.soLuongBan }}</td>
                <td class="text-right font-semibold">{{ formatCurrency(prod?.doanhThu) }}</td>
                <td class="text-center text-muted">{{ prod?.kichCo || '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="pagination">
          <div class="page-size">
            <span>Xem</span>
            <select class="select-box" v-model="filter.size" @change="applyCustomFilter">
               <option :value="5">5</option>
               <option :value="10">10</option>
            </select>
            <span>dòng</span>
          </div>
          <div class="page-controls">
            <button class="page-btn">&lt;</button>
            <button class="page-btn active">1</button>
            <span class="page-dots">...</span>
            <button class="page-btn">10</button>
            <button class="page-btn">&gt;</button>
          </div>
        </div>
      </div>

      <div class="table-card chart-card">
        <div class="card-header">Biểu đồ trạng thái tùy chỉnh</div>
        <div class="chart-container">
           <div class="chart-wrapper" v-if="hasChartData">
              <Pie :data="chartData" :options="chartOptions" />
           </div>
           <div v-else class="empty-state">
              <p>Chưa có dữ liệu thống kê biểu đồ</p>
           </div>
        </div>
      </div>

    </div>

    <div class="row-layout">
      
      <div class="table-card table-low-stock">
        <div class="card-header header-flex">
          <h3 class="title-danger">Danh sách sản phẩm sắp hết hàng</h3>
          
          <div class="threshold-control">
             <span class="threshold-label">Ngưỡng tồn:</span>
             <input type="number" min="0" v-model="lowStockThreshold" @change="fetchLowStock" class="threshold-input" />
             <span class="badge-danger">Số lượng: {{ lowStockProducts.length }}</span>
          </div>
        </div>
        
        <div class="table-responsive max-h-400">
          <table>
            <thead>
              <tr>
                <th class="text-center w-80">Ảnh</th>
                <th>Tên sản phẩm</th>
                <th class="text-center w-100">Tồn kho</th>
                <th class="text-right w-120">Giá bán</th>
                <th class="text-center w-100">Kích cỡ</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="lowStockProducts.length === 0 && !isLoadingLowStock">
                <td colspan="5" class="empty-state skeleton-box">
                    <div class="skeleton-wrapper">
                        <div class="skeleton-item" v-for="n in 3" :key="n">
                            <div class="skeleton-img"></div>
                            <div class="skeleton-lines">
                                <div class="line-1"></div>
                                <div class="line-2"></div>
                            </div>
                        </div>
                    </div>
                    <p class="empty-text">Kho đang đầy đủ</p>
                </td>
              </tr>
              <tr v-for="(prod, i) in lowStockProducts" :key="i">
                <td class="text-center">
                    <img v-if="prod?.anh" :src="prod.anh" class="product-img" />
                    <div v-else class="product-img no-img">No Img</div>
                </td>
                <td class="font-medium">{{ prod?.tenSanPham }}</td>
                <td class="text-center text-danger font-bold">{{ prod?.soLuongBan }}</td>
                <td class="text-right font-semibold">{{ formatCurrency(prod?.doanhThu) }}</td>
                <td class="text-center text-muted">{{ prod?.kichCo || '-' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="dark-card">
        <h3 class="dark-card-title">Tốc độ tăng trưởng</h3>
        
        <div class="growth-list">
          <div class="growth-item" v-for="(growth, i) in growthList" :key="i">
             <div class="growth-label">
                <div class="growth-icon">📈</div>
                {{ growth.label }}
             </div>
             <div class="growth-value-box">
                <span class="growth-number">{{ formatNumber(growth.value) }}</span>
                <span class="growth-percent" :class="parseFloat(growth.percent) >= 0 ? 'bg-success text-success' : 'bg-danger text-danger'">
                   {{ parseFloat(growth.percent) >= 0 ? '↑' : '↓' }} {{ Math.abs(parseFloat(growth.percent)) }}%
                </span>
             </div>
          </div>
          
          <div v-if="growthList.length === 0" class="empty-growth">
             <span class="empty-text">Đang cập nhật...</span>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { statisticApi } from '@/api/statisticApi' // Đảm bảo đường dẫn này đúng với dự án của bạn
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'
import { Pie } from 'vue-chartjs'
ChartJS.register(ArcElement, Tooltip, Legend)

// --- FORMAT UTILS ---
const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0)
const formatNumber = (val) => new Intl.NumberFormat('vi-VN').format(val || 0)

// --- STATES CHÍNH ---
const activeFilter = ref('ALL') // Trạng thái của nút bộ lọc
const filter = ref({ fromDate: null, toDate: null, status: 4, page: 0, size: 5 })
const lowStockThreshold = ref(10) // Ngưỡng tồn kho mặc định

const topProducts = ref([])         
const lowStockProducts = ref([])    
const growthList = ref([])          

const isLoadingCards = ref(false)   
const isLoadingLowStock = ref(false)
const hasChartData = ref(false)

const summaryCards = ref([
  { title: 'Hôm nay', revenue: 0, products: 0, successOrders: 0, cancelOrders: 0, returnOrders: 0 },
  { title: 'Tuần này', revenue: 0, products: 0, successOrders: 0, cancelOrders: 0, returnOrders: 0 },
  { title: 'Tháng này', revenue: 0, products: 0, successOrders: 0, cancelOrders: 0, returnOrders: 0 },
  { title: 'Năm nay', revenue: 0, products: 0, successOrders: 0, cancelOrders: 0, returnOrders: 0 }
])

const chartData = ref({
  labels: [],
  datasets: [{
    backgroundColor: ['#ff4d4f', '#ffa940', '#ffc53d', '#73d13d', '#36cfc9', '#4096ff', '#9254de', '#f759ab'],
    data: [],
    borderWidth: 1,
    hoverOffset: 4
  }]
})

const chartOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'bottom', labels: { boxWidth: 15, padding: 15, font: { size: 12, family: 'sans-serif' }, color: '#4b5563' } }
  }
})

// --- LOGIC TÍNH TOÁN NGÀY THÁNG ---
const getStartOfDay = (date) => new Date(date.setHours(0,0,0,0)).toISOString().slice(0,19);
const getEndOfDay = (date) => new Date(date.setHours(23,59,59,999)).toISOString().slice(0,19);

const getTimeframes = () => {
  const now = new Date();
  const d = new Date();
  
  const todayStart = getStartOfDay(new Date());
  const todayEnd = getEndOfDay(new Date());
  
  const weekStartObj = new Date(d.setDate(d.getDate() - d.getDay() + (d.getDay() === 0 ? -6 : 1)));
  const weekStart = getStartOfDay(weekStartObj);
  
  const monthStart = getStartOfDay(new Date(now.getFullYear(), now.getMonth(), 1));
  const yearStart = getStartOfDay(new Date(now.getFullYear(), 0, 1));
  
  return [
    { fromDate: todayStart, toDate: todayEnd },
    { fromDate: weekStart, toDate: todayEnd },
    { fromDate: monthStart, toDate: todayEnd },
    { fromDate: yearStart, toDate: todayEnd }
  ]
}

// --- LOGIC XỬ LÝ BỘ LỌC BUTTONS ---
const applyQuickFilter = (type) => {
  activeFilter.value = type;
  const now = new Date();
  let start = '';
  const end = now.toISOString().split('T')[0];

  if (type === 'DAY') {
    start = end;
  } else if (type === 'WEEK') {
    const d = new Date();
    const firstDay = new Date(d.setDate(d.getDate() - d.getDay() + (d.getDay() === 0 ? -6 : 1)));
    start = firstDay.toISOString().split('T')[0];
  } else if (type === 'MONTH') {
    start = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0];
  } else if (type === 'YEAR') {
    start = new Date(now.getFullYear(), 0, 1).toISOString().split('T')[0];
  }

  filter.value.fromDate = start;
  filter.value.toDate = end;
  
  applyCustomFilter();
}

// --- LOGIC GỌI API ---
const fetchSummaryCards = async () => {
  isLoadingCards.value = true;
  try {
    const frames = getTimeframes();
    const requests = frames.map(f => statisticApi.getRevenue({ fromDate: f.fromDate, toDate: f.toDate, status: 4 }));
    const responses = await Promise.all(requests);
    responses.forEach((res, index) => {
      if(res.data && res.data.summary) {
        summaryCards.value[index].revenue = res.data.summary.totalRevenue;
        summaryCards.value[index].successOrders = res.data.summary.totalOrders;
      }
    });
  } catch (error) { console.error(error) } finally { isLoadingCards.value = false; }
}

const fetchChartStatus = async (customPayload) => {
  try {
    const res = await statisticApi.getOrderStatus(customPayload);
    if(res.data && res.data.length > 0) {
        hasChartData.value = true;
        const statusMap = { 1: 'Chờ xác nhận', 2: 'Chờ giao hàng', 3: 'Đang giao', 4: 'Hoàn thành', 5: 'Đã hủy', 6: 'Trả hàng' };
        chartData.value.labels = res.data.map(i => statusMap[i.trangThai] || 'Khác');
        chartData.value.datasets[0].data = res.data.map(i => i.soLuong);
    } else { hasChartData.value = false; }
  } catch (error) { console.error(error) }
}

const fetchLowStock = async () => {
    isLoadingLowStock.value = true;
    try {
        // Gửi tham số ngưỡng tồn kho xuống Backend (Cần backend cập nhật DTO nhận tham số này)
        const payload = { threshold: lowStockThreshold.value };
        const resLow = await statisticApi.getLowStock(payload); 
        if(resLow.data) {
           lowStockProducts.value = resLow.data.filter(item => item != null);
        }
    } catch (error) {
        console.error(error);
    } finally {
        isLoadingLowStock.value = false;
    }
}

const applyCustomFilter = async () => {
  if (filter.value.fromDate && filter.value.toDate) {
      activeFilter.value = 'CUSTOM';
  }
  
  const payload = {
    fromDate: filter.value.fromDate ? `${filter.value.fromDate}T00:00:00` : null,
    toDate: filter.value.toDate ? `${filter.value.toDate}T23:59:59` : null,
    status: 4,
    size: filter.value.size
  }
  
  // Tải lại bảng Sản phẩm bán chạy
  try {
    const resTop = await statisticApi.getProductStats(payload);
    if(resTop.data && resTop.data.chartData) {
        topProducts.value = resTop.data.chartData.filter(item => item != null);
    } else if (Array.isArray(resTop.data)) {
        topProducts.value = resTop.data.filter(item => item != null);
    }
  } catch (error) { console.error(error); }

  // Tải lại Biểu đồ
  fetchChartStatus({ fromDate: payload.fromDate, toDate: payload.toDate });
}

// LOGIC XUẤT EXCEL (Tải file Blob)
const handleExportExcel = async () => {
  try {
    const payload = {
      fromDate: filter.value.fromDate ? `${filter.value.fromDate}T00:00:00` : null,
      toDate: filter.value.toDate ? `${filter.value.toDate}T23:59:59` : null,
      status: 4
    }
    const res = await statisticApi.exportRevenueExcel(payload);
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'BaoCaoDoanhThu.xlsx');
    document.body.appendChild(link);
    link.click();
    link.remove();
  } catch (error) { 
    console.error("Lỗi xuất Excel:", error);
  }
}

onMounted(() => {
  fetchSummaryCards();
  fetchLowStock(); // Tải bảng sắp hết hàng lúc đầu
  applyCustomFilter(); // Tải dữ liệu ban đầu cho Bảng Top và Biểu đồ
})
</script>

<style scoped>
/* RESET & BASE */
.dashboard-container {
  padding: 24px;
  background-color: #f4f6f8;
  min-height: 100vh;
  color: #333;
  font-family: Arial, sans-serif;
  box-sizing: border-box;
}
* { box-sizing: border-box; }

.page-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #1f2937;
}

.mb-24 { margin-bottom: 24px; }

/* GRID LAYOUTS */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.row-layout {
  display: grid;
  grid-template-columns: 8fr 4fr;
  gap: 24px;
}

/* CARDS */
.stat-card {
  background-color: #e2e4e6;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  min-height: 180px; 
  border: 1px solid #d1d5db;
}

.stat-title {
  font-size: 12px;
  color: #6b7280;
  text-align: center;
  text-transform: uppercase;
  font-weight: 600;
  margin: 0 0 10px 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 800;
  color: #111827;
  text-align: center;
  margin: 0 0 20px 0;
}

.stat-details {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  text-align: center;
  border-top: 1px solid #ccc;
  padding-top: 12px;
  margin-top: auto;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item .label {
  font-size: 10px;
  color: #6b7280;
  font-weight: 700;
  text-transform: uppercase;
}

.detail-item .value {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
}

.text-success { color: #16a34a !important; }
.text-danger { color: #dc2626 !important; }
.text-warning { color: #ea580c !important; }
.text-primary { color: #2563eb !important; }
.text-muted { color: #6b7280 !important; }

/* LOADING SPINNER */
.loading-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(226, 228, 230, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}
.spinner {
  width: 24px;
  height: 24px;
  border: 4px solid #2b3e50;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin { 100% { transform: rotate(360deg); } }

/* FILTER SECTION */
.filter-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  border: 1px solid #e5e7eb;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.filter-title {
  font-size: 16px;
  font-weight: bold;
  color: #1f2937;
  margin: 0;
  text-transform: uppercase;
}

.button-group {
  display: flex;
  gap: 8px;
}

.btn {
  padding: 6px 16px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 6px;
  cursor: pointer;
  text-transform: uppercase;
  transition: all 0.2s;
}

.btn-outline {
  background: #fff;
  border: 1px solid #d1d5db;
  color: #4b5563;
}
.btn-outline:hover { background: #f9fafb; color: #111827; }
.btn-active {
  background: #e5e7eb;
  color: #111827;
  border-color: #9ca3af;
}

.btn-primary {
  background: #fff;
  border: 1px solid #2b3e50;
  color: #2b3e50;
}
.btn-primary:hover { background: #f8fafc; }
.btn-active-primary {
  background: #2b3e50;
  color: #fff;
}

.btn-excel {
  background: #fff;
  border: 1px solid #16a34a;
  color: #15803d;
}
.btn-excel:hover { background: #f0fdf4; color: #16a34a; }

.date-picker-group {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f9fafb;
  padding: 4px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.date-input {
  border: none;
  background: transparent;
  padding: 4px 8px;
  font-size: 13px;
  color: #4b5563;
  outline: none;
}
.separator { color: #9ca3af; }

/* TABLE CARDS */
.table-card {
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-header {
  font-size: 15px;
  font-weight: bold;
  text-align: center;
  color: #1f2937;
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #f8fafc;
}

.header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-danger { color: #dc2626; margin: 0; font-size: 15px; font-weight: bold;}
.badge-danger {
  background: #fef2f2;
  border: 1px solid #fecaca;
  color: #dc2626;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: bold;
}

.threshold-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.threshold-label {
  font-size: 13px;
  color: #4b5563;
  font-weight: 600;
}

.threshold-input {
  width: 60px;
  padding: 4px 8px;
  border: 1px solid #d1d5db;
  border-radius: 4px;
  font-size: 13px;
  outline: none;
  text-align: center;
}

.table-responsive {
  overflow-x: auto;
  flex: 1;
}
.max-h-400 { max-height: 400px; overflow-y: auto; }

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

thead th {
  background: #2b3e50;
  color: #fff;
  padding: 12px;
  text-align: left;
  font-weight: 600;
  position: sticky;
  top: 0;
  z-index: 5;
}

tbody tr { border-bottom: 1px solid #f3f4f6; transition: background 0.2s; }
tbody tr:hover { background: #f9fafb; }
tbody td { padding: 12px; color: #374151; }

.text-center { text-align: center; }
.text-right { text-align: right; }
.w-80 { width: 80px; }
.w-100 { width: 100px; }
.w-120 { width: 120px; }
.font-medium { font-weight: 500; }
.font-bold { font-weight: bold; }
.font-semibold { font-weight: 600; }

.product-img {
  width: 40px; height: 48px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #e5e7eb;
  margin: 0 auto;
}
.no-img {
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #9ca3af;
}

/* PAGINATION */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-top: 1px solid #e5e7eb;
  background: #f8fafc;
  font-size: 13px;
  color: #4b5563;
}

.page-size { display: flex; align-items: center; gap: 8px; }
.select-box {
  border: 1px solid #d1d5db;
  border-radius: 4px;
  padding: 4px 8px;
  outline: none;
}

.page-controls { display: flex; align-items: center; gap: 6px; }
.page-btn {
  width: 28px; height: 28px;
  border: 1px solid #d1d5db;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
}
.page-btn:hover { background: #f3f4f6; }
.page-btn.active { background: #3b82f6; color: #fff; border-color: #3b82f6; }
.page-dots { color: #9ca3af; }

/* CHARTS */
.chart-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  min-height: 320px;
}
.chart-wrapper { width: 100%; height: 100%; }

/* EMPTY STATES & SKELETONS */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #9ca3af;
}
.empty-text { font-size: 14px; font-weight: 600; text-transform: uppercase; margin-top: 20px;}

.skeleton-box { background: #fff; }
.skeleton-wrapper {
  border: 1.5px dashed #b6bdf8;
  border-radius: 12px;
  padding: 24px;
  width: 70%;
  margin: 0 auto;
  background: #fafafa;
  opacity: 0.5;
}
.skeleton-item {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  margin-bottom: 12px;
}
.skeleton-img { width: 40px; height: 40px; background: #e5e7eb; border-radius: 6px; }
.skeleton-lines { flex: 1; }
.line-1 { height: 8px; width: 40%; background: #d1d5db; border-radius: 4px; margin-bottom: 8px; }
.line-2 { height: 8px; width: 25%; background: #e5e7eb; border-radius: 4px; }

/* DARK CARD (TĂNG TRƯỞNG) */
.dark-card {
  background: #1f1f1f;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  flex-direction: column;
}

.dark-card-title {
  color: #f3f4f6;
  font-size: 15px;
  font-weight: bold;
  border-bottom: 1px solid #374151;
  padding-bottom: 16px;
  margin: 0 0 20px 0;
}

.growth-list { display: flex; flex-direction: column; gap: 16px; flex: 1; }

.growth-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #2a2a2a;
  border: 1px solid #3f3f46;
  padding: 14px;
  border-radius: 8px;
}

.growth-label {
  display: flex; align-items: center; gap: 12px;
  color: #d1d5db; font-size: 13px; font-weight: 500;
}
.growth-icon {
  width: 30px; height: 30px;
  background: #1f1f1f;
  border: 1px solid #4b5563;
  border-radius: 6px;
  display: flex; align-items: center; justify-content: center;
}

.growth-value-box { display: flex; flex-direction: column; align-items: flex-end; gap: 4px; }
.growth-number { font-size: 14px; font-weight: bold; color: #fff; }
.growth-percent {
  font-size: 11px; font-weight: bold;
  padding: 2px 6px; border-radius: 4px;
}
.bg-success { background: rgba(22, 163, 74, 0.1); }
.text-success { color: #16a34a !important; }
.bg-danger { background: rgba(220, 38, 38, 0.1); }
.text-danger { color: #dc2626 !important; }

.empty-growth {
  display: flex; align-items: center; justify-content: center;
  height: 100%; color: #6b7280;
}

/* SCROLLBAR CUSTOMIZATION */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 10px; }
::-webkit-scrollbar-thumb:hover { background-color: #94a3b8; }
</style>