<template>
  <div class="dashboard-container">
    <div class="page-header">
      <div class="title-group">
        <h2 class="page-title">
          Thống Kê
        </h2> 
      </div>
    </div>

    <div class="summary-grid">
      <div class="stat-card" v-for="(item, index) in summaryCards" :key="index">
        <h3 class="stat-title">{{ item.title }}</h3>
        <p class="stat-value text-success">{{ formatCurrency(item.revenue) }}</p>
        
        <div class="stat-sub-info">
          <span class="text-muted">Sản phẩm đã bán: </span><span class="font-medium">{{ item.products }}</span> <span class="divider">|</span> 
          <span class="text-muted">Đơn hàng: </span><span class="font-medium">{{ item.successOrders + item.cancelOrders + item.returnOrders }}</span>
        </div>
        
        <div class="stat-sub-info mt-1">
          <span class="text-muted">Hoàn thành: </span><span class="text-success font-medium">{{ item.successOrders }}</span> <span class="divider">|</span> 
          <span class="text-muted">Hủy: </span><span class="text-danger font-medium">{{ item.cancelOrders || 0 }}</span> <span class="divider">|</span> 
          <span class="text-muted">Xử lý/Trả: </span><span class="text-warning font-medium">{{ item.returnOrders || 0 }}</span>
        </div>

        <div v-if="isLoadingCards" class="loading-overlay">
           <div class="spinner"></div>
        </div>
      </div>
    </div>

    <div class="filter-card">
      <div class="filter-header">
        <div class="filter-left">
            <h3 class="filter-title">Bộ Lọc Tìm Kiếm</h3>
            <p class="filter-subtitle">Chọn khoảng thời gian để xem số liệu</p>
        </div>
        
        <div class="filter-right">
          <div class="button-group">
            <button class="btn btn-outline" :class="{ 'btn-active': activeFilter === 'DAY' }" @click="applyQuickFilter('DAY')">Hôm nay</button>
            <button class="btn btn-outline" :class="{ 'btn-active': activeFilter === 'WEEK' }" @click="applyQuickFilter('WEEK')">Tuần này</button>
            <button class="btn btn-outline" :class="{ 'btn-active': activeFilter === 'MONTH' }" @click="applyQuickFilter('MONTH')">Tháng này</button>
            <button class="btn btn-outline" :class="{ 'btn-active': activeFilter === 'YEAR' }" @click="applyQuickFilter('YEAR')">Năm nay</button>
            <button class="btn btn-outline" :class="{ 'btn-active': activeFilter === 'CUSTOM' }" @click="activeFilter = 'CUSTOM'; applyCustomFilter()">Tùy chỉnh</button>
          </div>
          
          <div class="date-picker-group" v-if="activeFilter === 'CUSTOM'">
            <input type="date" v-model="filter.fromDate" class="date-input" @change="applyCustomFilter" />
            <span class="separator">-</span>
            <input type="date" v-model="filter.toDate" class="date-input" @change="applyCustomFilter" />
          </div>

          <button @click="showEmailModal = true" class="btn btn-outline flex-center gap-6">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><polyline points="22,6 12,13 2,6"></polyline></svg>
            Gửi Báo Cáo
          </button>

          <button @click="handleExportExcel" class="btn btn-excel">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="9" y1="15" x2="15" y2="15"></line></svg>
            Xuất Excel
          </button>
        </div>
      </div>

      <div class="filter-summary-row">
        <div class="f-stat-item">
          <span class="f-label">Tổng số đơn hàng (Tùy chỉnh)</span>
          <span class="f-value text-black">{{ filterSummary.totalOrders }}</span>
        </div>
        <div class="f-stat-item">
          <span class="f-label">Số lượng sản phẩm(Tùy chỉnh)</span>
          <span class="f-value text-black">{{ filterSummary.totalProducts }}</span>
        </div>
        <div class="f-stat-item">
          <span class="f-label">Hoàn thành(Tùy chỉnh)</span>
          <span class="f-value text-primary">{{ filterSummary.successOrders }}</span>
        </div>
        <div class="f-stat-item">
          <span class="f-label">Đang xử lý(Tùy chỉnh)</span>
          <span class="f-value text-warning">{{ filterSummary.processingOrders }}</span>
        </div>
        <div class="f-stat-item">
          <span class="f-label">Huỷ(Tùy chỉnh)</span>
          <span class="f-value text-danger">{{ filterSummary.cancelOrders }}</span>
        </div>
        <div class="f-stat-item text-right">
          <span class="f-label">DT Dự kiến (Tạm tính)</span>
          <span class="f-value text-purple">{{ formatCurrency(filterSummary.expectedRevenue) }}</span>
        </div>
      </div>
    </div>

    <div class="row-layout mb-24">
      <div class="content-card chart-card">
        <div class="card-header border-none">Biểu Đồ Doanh Thu</div>
        <div class="chart-container line-chart-wrapper">
           <Line :data="lineChartData" :options="lineChartOptions" />
        </div>
      </div>

      <div class="content-card chart-card">
        <div class="card-header border-none">Phân bổ trạng thái đơn hàng</div>
        <div class="chart-container">
           <div class="chart-wrapper" v-if="hasChartData">
              <Doughnut :data="chartData" :options="chartOptions" :plugins="[outlabelsPlugin]" />
           </div>
           <div v-else class="empty-state">
              <p>Chưa có dữ liệu thống kê biểu đồ</p>
           </div>
        </div>
      </div>
    </div>

    <div class="row-layout mb-24">
      <div class="content-card">
        <div class="card-header border-none">Top sản phẩm bán chạy</div>
        <div class="table-responsive">
          <table class="modern-table">
            <thead>
              <tr>
                <th class="text-center w-50">#</th>
                <th class="text-center w-80">Ảnh</th>
                <th class="text-left">Tên Sản Phẩm</th>
                <th class="text-right w-120">Giá</th>
                <th class="text-center w-80">Bán</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="topProducts.length === 0">
                <td colspan="5" class="empty-state">
                  <p>Chưa có dữ liệu sản phẩm bán chạy</p>
                </td>
              </tr>
              <tr v-for="(prod, i) in topProducts" :key="i">
                <td class="text-center text-muted">{{ i + 1 + (filter.page * filter.size) }}</td>
                <td class="text-center">
                  <img v-if="prod?.anh" :src="prod.anh" class="product-img" />
                  <div v-else class="product-img no-img">No Img</div>
                </td>
                <td class="font-medium text-dark text-left">
                  {{ prod?.tenSanPham }} 
                  <span v-if="prod?.kichCo" class="text-xs text-muted block mt-1">Size: {{ prod.kichCo }}</span>
                </td>
                <td class="text-right text-danger font-medium">{{ formatCurrency(prod?.doanhThu) }}</td>
                <td class="text-center">
                  <span class="badge-success-light">{{ prod?.soLuongBan }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="pagination border-top">
          <div class="page-size"></div>
          <div class="page-controls">
            <button class="page-btn" :disabled="filter.page === 0" @click="changeTopPage(filter.page - 1)">&lt;</button>
            <button class="page-btn active">{{ filter.page + 1 }}</button>
            <span class="page-dots">...</span>
            <button class="page-btn" @click="changeTopPage(filter.page + 1)">&gt;</button>
          </div>
        </div>
      </div>

      <div class="content-card">
        <div class="card-header border-none flex-between">
          <span>Tốc độ tăng trưởng</span>
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="text-muted cursor-pointer hover-dark"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/></svg>
        </div>
        
        <div class="growth-list">
          <div class="growth-item" v-for="(growth, i) in growthList" :key="i">
             <div class="growth-label">
                <span class="growth-icon" :class="growth.colorClass">
                   <svg v-if="growth.icon === 'revenue'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
                   <svg v-if="growth.icon === 'order'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="21" r="1"></circle><circle cx="20" cy="21" r="1"></circle><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path></svg>
                   <svg v-if="growth.icon === 'product'" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path><polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline><line x1="12" y1="22.08" x2="12" y2="12"></line></svg>
                </span>
                {{ growth.label }}
             </div>
             <div class="growth-value-box">
                <span class="growth-number">
                   {{ growth.type === 'currency' ? formatCurrency(growth.value) : formatNumber(growth.value) }}
                </span>
                <span class="growth-percent" :class="parseFloat(growth.percent) >= 0 ? 'bg-success-light text-success' : 'bg-danger-light text-danger'">
                   {{ parseFloat(growth.percent) >= 0 ? '↑' : '↓' }} {{ Math.abs(parseFloat(growth.percent)) }}%
                </span>
             </div>
          </div>
          
          <div v-if="growthList.length === 0" class="empty-growth">
             <span class="empty-text text-muted">Đang cập nhật dữ liệu tăng trưởng...</span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showEmailModal" class="modal-overlay">
      <div class="modal-content">
        <h3 class="modal-title">Cài đặt gửi báo cáo qua Email</h3>
        <p class="modal-subtitle">Hệ thống sẽ tự động tổng hợp số liệu và gửi về email đã đăng ký.</p>
        
        <div class="form-group">
          <label class="form-label">Email nhận báo cáo:</label>
          <input type="email" v-model="emailConfig.email" class="form-input" placeholder="ví dụ: admin@gmail.com" />
        </div>
        
        <div class="form-group">
          <label class="form-label">Chu kỳ gửi báo cáo:</label>
          <select v-model="emailConfig.frequency" class="form-input">
            <option value="DAILY_17H">Hàng ngày (gửi lúc 17:00)</option>
            <option value="MONTHLY">Hàng tháng (gửi ngày cuối tháng)</option>
            <option value="QUARTERLY">Hàng quý (3 tháng/lần)</option>
            <option value="YEARLY">Hàng năm (tổng kết năm)</option>
          </select>
        </div>

        <div class="modal-actions">
          <button class="btn btn-outline" @click="showEmailModal = false">Hủy</button>
          <button class="btn btn-active" @click="handleSaveEmailConfig">Lưu thiết lập</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { statisticApi } from '@/api/statisticApi' 
import { Chart as ChartJS, ArcElement, Tooltip, Legend, CategoryScale, LinearScale, PointElement, LineElement, Title, Filler } from 'chart.js'
import { Doughnut, Line } from 'vue-chartjs'
ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, PointElement, LineElement, Title, Filler)

// --- FORMAT UTILS ---
const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0)
const formatNumber = (val) => new Intl.NumberFormat('vi-VN').format(val || 0)

// --- PLUGIN VẼ DÂY VÀ CHỮ NGOÀI BIỂU ĐỒ TRẠNG THÁI ---
const outlabelsPlugin = {
  id: 'outlabels',
  afterDraw(chart) {
    const ctx = chart.ctx;
    chart.data.datasets.forEach((dataset, i) => {
      const meta = chart.getDatasetMeta(i);
      if (!meta.hidden) {
        meta.data.forEach((element, index) => {
          const dataVal = dataset.data[index];
          if (dataVal <= 0) return; 

          const startAngle = element.startAngle;
          const endAngle = element.endAngle;
          const midAngle = startAngle + (endAngle - startAngle) / 2;

          const outerRadius = element.outerRadius;
          const lineStartRadius = outerRadius; 
          const lineEndRadius = outerRadius + 15; 

          const x = element.x;
          const y = element.y;

          const startX = x + Math.cos(midAngle) * lineStartRadius;
          const startY = y + Math.sin(midAngle) * lineStartRadius;
          const edgeX = x + Math.cos(midAngle) * lineEndRadius;
          const edgeY = y + Math.sin(midAngle) * lineEndRadius;

          const isRight = Math.cos(midAngle) > 0;
          const endX = edgeX + (isRight ? 15 : -15); 
          const endY = edgeY;

          ctx.beginPath();
          ctx.moveTo(startX, startY);
          ctx.lineTo(edgeX, edgeY);
          ctx.lineTo(endX, endY);
          ctx.strokeStyle = dataset.backgroundColor[index];
          ctx.lineWidth = 1.5;
          ctx.stroke();

          const label = chart.data.labels[index];
          const total = dataset.data.reduce((a, b) => a + b, 0);
          const percent = ((dataVal / total) * 100).toFixed(2) + '%';
          
          ctx.textAlign = isRight ? 'left' : 'right';
          ctx.textBaseline = 'middle';
          
          ctx.fillStyle = '#6b7280';
          ctx.font = '12px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
          ctx.fillText(label, endX + (isRight ? 5 : -5), endY - 8);
          
          ctx.font = 'bold 11px -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif';
          ctx.fillText(`${dataVal} đơn (${percent})`, endX + (isRight ? 5 : -5), endY + 8);
        });
      }
    });
  }
};

// --- STATES CHÍNH ---
const activeFilter = ref('ALL') 
const filter = ref({ fromDate: null, toDate: null, status: 5, page: 0, size: 5 }) 
const lowStockThreshold = ref(10) 
const lowStockFilter = ref({ page: 0, size: 5 }) 

const topProducts = ref([])         
const lowStockProducts = ref([])    
const growthList = ref([])          

const filterSummary = ref({
  totalOrders: 0,
  totalProducts: 0,
  successOrders: 0,
  processingOrders: 0,
  cancelOrders: 0,
  expectedRevenue: 0
})

const isLoadingCards = ref(false)   
const isLoadingLowStock = ref(false)
const hasChartData = ref(false)

const summaryCards = ref([
  { title: 'Hôm nay', revenue: 0, products: 0, successOrders: 0, cancelOrders: 0, returnOrders: 0 },
  { title: 'Tuần này', revenue: 0, products: 0, successOrders: 0, cancelOrders: 0, returnOrders: 0 },
  { title: 'Tháng này', revenue: 0, products: 0, successOrders: 0, cancelOrders: 0, returnOrders: 0 },
  { title: 'Năm nay', revenue: 0, products: 0, successOrders: 0, cancelOrders: 0, returnOrders: 0 }
])

// --- STATES EMAIL MODAL ---
const showEmailModal = ref(false)
const emailConfig = ref({
  email: '',
  frequency: 'DAILY_17H' // Default gửi lúc 17h
})

// --- LINE CHART DATA ---
const lineChartData = ref({
  labels: [], 
  datasets: [{
    label: 'Doanh thu',
    data: [], 
    borderColor: '#1e3a8a',
    backgroundColor: 'rgba(30, 58, 138, 0.1)', 
    fill: true,
    tension: 0.4,
    pointBackgroundColor: '#fff',
    pointBorderColor: '#1e3a8a',
    pointBorderWidth: 2,
    pointRadius: 3,
    pointHoverRadius: 5
  }]
})

const lineChartOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { display: false } }, 
  scales: {
    y: { beginAtZero: true },
    x: { grid: { display: false } }
  }
})

// --- DOUGHNUT CHART DATA ---
const chartData = ref({
  labels: [],
  datasets: [{
    backgroundColor: [],
    data: [],
    borderWidth: 1,
    hoverOffset: 4
  }]
})

const chartOptions = ref({
  responsive: true,
  maintainAspectRatio: false,
  cutout: '55%', 
  layout: {
    padding: { top: 30, bottom: 30, left: 70, right: 70 } 
  },
  plugins: {
    legend: { 
      position: 'bottom', 
      labels: { boxWidth: 12, padding: 15, font: { size: 12, family: 'sans-serif' }, color: '#4b5563', usePointStyle: true } 
    },
    tooltip: { enabled: false } 
  }
})

// --- TÍNH TOÁN THỜI GIAN ---
const formatToLocalDateTime = (date, isEnd = false) => {
  const d = new Date(date);
  if (isEnd) d.setHours(23, 59, 59, 999);
  else d.setHours(0, 0, 0, 0);
  
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
}

const formatYYYYMMDD = (d) => {
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

const getTimeframes = () => {
  const now = new Date();
  const todayStart = formatToLocalDateTime(now);
  const todayEnd = formatToLocalDateTime(now, true);
  
  const d = new Date();
  const weekStartObj = new Date(d.setDate(d.getDate() - d.getDay() + (d.getDay() === 0 ? -6 : 1)));
  const weekStart = formatToLocalDateTime(weekStartObj);
  
  const monthStartObj = new Date(now.getFullYear(), now.getMonth(), 1);
  const monthStart = formatToLocalDateTime(monthStartObj);
  
  const yearStartObj = new Date(now.getFullYear(), 0, 1);
  const yearStart = formatToLocalDateTime(yearStartObj);
  
  return [
    { fromDate: todayStart, toDate: todayEnd },
    { fromDate: weekStart, toDate: todayEnd },
    { fromDate: monthStart, toDate: todayEnd },
    { fromDate: yearStart, toDate: todayEnd }
  ]
}

// --- LOGIC API ---

const fetchSummaryCards = async () => {
  isLoadingCards.value = true;
  try {
    const frames = getTimeframes();
    const requests = frames.map(f => statisticApi.getRevenue({ fromDate: f.fromDate, toDate: f.toDate, status: 5 }));
    const responses = await Promise.all(requests);
    
    growthList.value = [];
    const periods = ['ngày', 'tuần', 'tháng', 'năm'];
    const revenues = [];
    const orders = [];
    const products = [];

    responses.forEach((res, index) => {
      const summary = res.data?.summary || {}; 
      
      summaryCards.value[index].revenue = summary.totalRevenue || 0;
      summaryCards.value[index].successOrders = summary.totalOrders || 0; 
      summaryCards.value[index].products = summary.totalProducts || 0;
      summaryCards.value[index].cancelOrders = summary.cancelOrders || 0;
      summaryCards.value[index].returnOrders = summary.returnOrders || 0;

      revenues.push({
          label: `Doanh thu ${periods[index]}`,
          value: summary.totalRevenue || 0,
          percent: summary.growthPercent || summary.revenueGrowth || 0,
          type: 'currency',
          icon: 'revenue',
          colorClass: 'text-blue'
      });

      orders.push({
          label: `Đơn hàng ${periods[index]}`,
          value: summary.totalOrders || 0,
          percent: summary.orderGrowth || summary.growthPercent || 0,
          type: 'number',
          icon: 'order',
          colorClass: 'text-success-icon' 
      });

      products.push({
          label: `Sản phẩm ${periods[index]}`,
          value: summary.totalProducts || 0,
          percent: summary.productGrowth || summary.growthPercent || 0,
          type: 'number',
          icon: 'product',
          colorClass: 'text-warning-icon' 
      });
    });

    growthList.value = [...revenues, ...orders, ...products];

  } catch (error) { console.error(error) } finally { isLoadingCards.value = false; }
}

const fetchFilterSummary = async (payload) => {
  try {
    const payloadAll = { ...payload, status: null }; 
    const res = await statisticApi.getRevenue(payloadAll);
    const summary = res.data?.summary || {};
    
    filterSummary.value.expectedRevenue = summary.totalRevenue || 0;
    filterSummary.value.totalOrders = summary.totalOrders || 0;
    filterSummary.value.totalProducts = summary.totalProducts || 0;
    filterSummary.value.successOrders = summary.successOrders || 0;
    filterSummary.value.processingOrders = summary.processingOrders || 0; 
    filterSummary.value.cancelOrders = summary.cancelOrders || 0;
    
  } catch (error) { console.error("Lỗi fetchFilterSummary:", error); }
}

const fetchLineChartData = async (payload) => {
  try {
    const res = await statisticApi.getRevenue(payload); 
    const dataList = res.data?.chartData || res.data?.data || []; 
    
    if(Array.isArray(dataList) && dataList.length > 0) {
       lineChartData.value = {
         labels: dataList.map(item => item.date || item.ngay || item.label || item.thoiGian || ''),
         datasets: [{
           ...lineChartData.value.datasets[0],
           data: dataList.map(item => item.value || item.doanhThu || item.revenue || item.tongTien || 0)
         }]
       };
    } else {
       lineChartData.value = { labels: [], datasets: [{ ...lineChartData.value.datasets[0], data: [] }] };
    }
  } catch (error) { console.error("Lỗi fetchLineChartData:", error); }
}

const fetchChartStatus = async (customPayload) => {
  try {
    const payloadForChart = { ...customPayload, status: null };
    const res = await statisticApi.getOrderStatus(payloadForChart);
    const dataList = res.data?.data || res.data;

    if(dataList && dataList.length > 0) {
        hasChartData.value = true;
        
        const statusMap = { 
            0: 'Đã hủy', 1: 'Chờ xác nhận', 2: 'Chờ giao', 
            3: 'Đang giao', 4: 'Chờ thanh toán', 5: 'Hoàn thành' 
        };
        const colorMap = {
            0: '#ef4444', 1: '#3b82f6', 2: '#eab308', 
            3: '#f97316', 4: '#a855f7', 
            5: '#10b981' 
        };

        chartData.value.labels = dataList.map(i => statusMap[i.trangThai] || 'Khác');
        chartData.value.datasets[0].data = dataList.map(i => i.soLuong);
        chartData.value.datasets[0].backgroundColor = dataList.map(i => colorMap[i.trangThai] || '#cbd5e1');
    } else { hasChartData.value = false; }
  } catch (error) { console.error(error) }
}

const applyQuickFilter = (type) => {
  activeFilter.value = type;
  const now = new Date();
  let start = '';
  const end = formatYYYYMMDD(now); 

  if (type === 'DAY') {
    start = end;
  } else if (type === 'WEEK') {
    const d = new Date();
    const firstDay = new Date(d.setDate(d.getDate() - d.getDay() + (d.getDay() === 0 ? -6 : 1)));
    start = formatYYYYMMDD(firstDay);
  } else if (type === 'MONTH') {
    start = formatYYYYMMDD(new Date(now.getFullYear(), now.getMonth(), 1));
  } else if (type === 'YEAR') {
    start = formatYYYYMMDD(new Date(now.getFullYear(), 0, 1));
  }

  filter.value.fromDate = start;
  filter.value.toDate = end;
  
  applyCustomFilter();
}

const applyCustomFilter = async () => {
  const payload = {
    fromDate: filter.value.fromDate ? `${filter.value.fromDate}T00:00:00` : null,
    toDate: filter.value.toDate ? `${filter.value.toDate}T23:59:59` : null,
    status: 5, 
    size: filter.value.size,
    page: filter.value.page
  }
  
  try {
    const resTop = await statisticApi.getProductStats(payload);
    let dataList = resTop.data?.chartData || resTop.data?.data || resTop.data;

    if (dataList && !Array.isArray(dataList) && typeof dataList === 'object') {
       const key = Object.keys(dataList).find(k => Array.isArray(dataList[k]));
       if (key) dataList = dataList[key];
    }

    if (Array.isArray(dataList)) {
        topProducts.value = dataList.filter(item => item != null);
    } else {
        topProducts.value = [];
    }
  } catch (error) { console.error("Lỗi applyCustomFilter:", error); }

  fetchChartStatus({ fromDate: payload.fromDate, toDate: payload.toDate });
  fetchFilterSummary({ fromDate: payload.fromDate, toDate: payload.toDate });
  fetchLineChartData({ fromDate: payload.fromDate, toDate: payload.toDate });
}

const validateAndFetchLowStock = () => {
    if (lowStockThreshold.value < 1) lowStockThreshold.value = 1;
    if (lowStockThreshold.value > 100) lowStockThreshold.value = 100;
    fetchLowStock();
}

const fetchLowStock = async () => {
    isLoadingLowStock.value = true;
    try {
        const payload = { 
            threshold: lowStockThreshold.value,
            size: lowStockFilter.value.size,
            page: lowStockFilter.value.page 
        };
        const resLow = await statisticApi.getLowStock(payload); 
        const dataList = resLow.data?.data || resLow.data;

        if(dataList && Array.isArray(dataList)) {
           lowStockProducts.value = dataList.filter(item => item != null);
        } else {
           lowStockProducts.value = [];
        }
    } catch (error) { console.error(error); } 
    finally { isLoadingLowStock.value = false; }
}

const changeTopPage = (newPage) => {
    filter.value.page = newPage;
    applyCustomFilter();
}
const changeLowStockPage = (newPage) => {
    lowStockFilter.value.page = newPage;
    fetchLowStock();
}

const handleExportExcel = async () => {
  try {
    const payload = {
      fromDate: filter.value.fromDate ? `${filter.value.fromDate}T00:00:00` : null,
      toDate: filter.value.toDate ? `${filter.value.toDate}T23:59:59` : null,
      status: 5
    }
    const res = await statisticApi.exportRevenueExcel(payload);
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `BaoCao_ThongKe_${formatYYYYMMDD(new Date())}.xlsx`);
    document.body.appendChild(link);
    link.click();
    link.remove();
  } catch (error) { 
    console.error("Lỗi xuất Excel:", error);
  }
}

// Xử lý lưu cấu hình Email
const handleSaveEmailConfig = async () => {
  if (!emailConfig.value.email) {
    alert("Vui lòng nhập địa chỉ email nhận báo cáo!");
    return;
  }
  
  try {
    // Đoạn này mày cần mở cmt và gọi API thực tế nếu đã viết API ở backend
    // await statisticApi.saveEmailReportConfig(emailConfig.value);
    
    alert(`Đã lưu cấu hình! Hệ thống sẽ gửi báo cáo về [${emailConfig.value.email}] với chu kỳ: ${emailConfig.value.frequency}.\n\n(Lưu ý: Mày cần setup hàm @Scheduled ở phía Backend để việc gửi mail này thực sự chạy ngầm nhé!)`);
    showEmailModal.value = false;
  } catch (error) {
    console.error("Lỗi khi lưu cấu hình mail:", error);
  }
}

onMounted(() => {
  fetchSummaryCards();
  fetchLowStock(); 
  applyQuickFilter('MONTH'); 
})
</script>

<style scoped>
/* RESET & BASE */
.dashboard-container {
  padding: 24px;
  background-color: #ebecee;
  min-height: 100vh;
  color: #374151;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  box-sizing: border-box;
  position: relative;
}
* { box-sizing: border-box; }

.page-header { margin-bottom: 24px; display: flex; align-items: center; }
.title-group { display: flex; flex-direction: column; gap: 4px; }
.page-title {
  font-size: 24px; font-weight: 700; color: #2b4360; margin: 0;
  display: flex; align-items: center; gap: 8px;
}
.page-subtitle { font-size: 13px; color: #6b7280; margin: 0; }

.mb-24 { margin-bottom: 24px; }
.mt-1 { margin-top: 4px; }
.w-full { width: 100%; }

/* GRID LAYOUTS */
.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.row-layout {
  display: grid;
  grid-template-columns: 7fr 5fr;
  gap: 24px;
}

/* CONTENT CARDS */
.content-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #f3f4f6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-header {
  font-size: 16px; font-weight: 600; color: #374151;
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
}
.border-none { border-bottom: none !important; padding-bottom: 8px; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.flex-center { display: flex; align-items: center; justify-content: center; }
.gap-6 { gap: 6px; }

/* SUMMARY CARDS */
.stat-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  position: relative;
  border: 1px solid #f3f4f6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
}

.stat-title {
  font-size: 14px; color: #6b7280; font-weight: 600; margin: 0 0 12px 0;
}

.stat-value {
  font-size: 24px; font-weight: 700; margin: 0 0 16px 0; letter-spacing: -0.5px;
}

.stat-sub-info { font-size: 12px; color: #6b7280; }
.divider { margin: 0 4px; color: #d1d5db; }

/* COLOR UTILS */
.text-success { 
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%) !important;
  -webkit-background-clip: text !important;
  -webkit-text-fill-color: transparent !important;
  color: #1e3a8a !important; 
}
.text-danger { color: #ef4444 !important; }
.text-warning { color: #f59e0b !important; }
.text-primary { color: #3b82f6 !important; }
.text-purple { color: #8b5cf6 !important; }
.text-muted { color: #6b7280 !important; } 
.text-dark { color: #1f2937 !important; }
.text-black { color: #000 !important; font-weight: 600;}
.font-medium { font-weight: 500; }
.font-bold { font-weight: 700; }
.text-xs { font-size: 11px; }
.text-sm { font-size: 13px; }
.block { display: block; }
.cursor-pointer { cursor: pointer; }
.hover-dark:hover { color: #374151 !important; }
.text-blue { color: #1e3a8a; }
.text-success-icon { color: #10b981; }
.text-warning-icon { color: #f59e0b; }

/* SPINNER */
.loading-overlay {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(255,255,255, 0.8);
  display: flex; align-items: center; justify-content: center; z-index: 10;
}
.spinner {
  width: 24px; height: 24px; border: 3px solid #e5e7eb;
  border-top-color: #1e3a8a; border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin { 100% { transform: rotate(360deg); } }

/* FILTER SECTION */
.filter-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 24px;
  border: 1px solid #f3f4f6;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
}

.filter-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 20px; border-bottom: 1px solid #f3f4f6;
}

.filter-title { font-size: 16px; font-weight: 600; color: #374151; margin: 0 0 4px 0; }
.filter-subtitle { font-size: 13px; color: #6b7280; margin: 0; }

.filter-right { display: flex; align-items: center; gap: 16px; }

.button-group { display: flex; gap: 8px; }
.btn {
  padding: 6px 16px; font-size: 13px; font-weight: 500;
  cursor: pointer; transition: all 0.2s;
  background: #fff; border: 1px solid #e5e7eb; color: #6b7280;
  border-radius: 6px !important;
}

.btn:not(.btn-excel):not(.btn-outline):hover { 
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
  color: #fff; 
  border-color: transparent; 
}

.btn-active { 
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%) !important; 
  color: #fff !important; 
  border-color: transparent !important;
  z-index: 2; 
  position: relative;
}

.btn-excel {
  background: #fff !important; 
  border: 1px solid #e5e7eb !important; 
  color: #334155 !important; 
  border-radius: 6px; 
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px; 
}
.btn-excel:hover { background: #f8fafc !important; color: #0f172a !important; }

.date-picker-group { display: flex; align-items: center; gap: 8px; border: 1px solid #e5e7eb; padding: 4px 8px; border-radius: 6px; }
.date-input { border: none; font-size: 13px; color: #4b5563; outline: none; }
.separator { color: #6b7280; }

.filter-summary-row {
  display: grid; grid-template-columns: repeat(6, 1fr);
  padding: 16px 20px; background: #fafafa; border-radius: 0 0 8px 8px;
}
.f-stat-item { display: flex; flex-direction: column; gap: 4px; }
.f-label { font-size: 12px; color: #6b7280; }
.f-value { font-size: 16px; font-weight: 700; }

/* TABLE STYLES */
.table-responsive { overflow-x: auto; flex: 1; padding: 0 20px; }
.max-h-400 { max-height: 400px; overflow-y: auto; }

.modern-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.modern-table thead th {
  background: #fff; color: #6b7280; font-weight: 500;
  padding: 12px 8px; text-align: left;
  border-bottom: 1px solid #e5e7eb;
}
.modern-table tbody tr { border-bottom: 1px dashed #f3f4f6; transition: background 0.2s; }
.modern-table tbody tr:hover { background: #f9fafb; }
.modern-table tbody td { 
  padding: 12px 8px; 
  color: #4b5563; 
  vertical-align: middle !important; 
}

.text-left { text-align: left; }
.text-center { text-align: center; }
.text-right { text-align: right; }
.w-50 { width: 50px; }
.w-80 { width: 80px; }
.w-100 { width: 100px; }
.w-120 { width: 120px; }

.product-img { width: 40px; height: 40px; object-fit: cover; border-radius: 6px; border: 1px solid #f3f4f6; margin: 0 auto; }
.no-img { background: #f3f4f6; display: flex; align-items: center; justify-content: center; font-size: 10px; color: #6b7280; }

.badge-success-light { background: #e0e7ff; color: #1e3a8a; padding: 4px 12px; border-radius: 20px; font-weight: 600; font-size: 12px; }

/* PAGINATION */
.pagination {
  display: flex; justify-content: space-between; align-items: center;
  padding: 16px 20px; background: #fff; font-size: 13px; color: #6b7280;
}
.border-top { border-top: 1px solid #f3f4f6; }

.page-size { display: flex; align-items: center; gap: 8px; }
.page-controls { display: flex; align-items: center; gap: 4px; }
.page-btn {
  width: 28px; height: 28px; border: 1px solid transparent; background: #fff;
  border-radius: 4px; cursor: pointer; display: flex; align-items: center; justify-content: center; color: #4b5563; font-weight: 500;
}
.page-btn:hover:not(:disabled) { background: #f3f4f6; }
.page-btn:disabled { opacity: 0.3; cursor: not-allowed; }

.page-btn.active { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; border-color: transparent; }
.page-dots { color: #6b7280; padding: 0 4px; }

/* CHARTS */
.chart-card { min-height: 300px; }
.chart-container { flex: 1; display: flex; align-items: center; justify-content: center; padding: 20px; }
.chart-wrapper { width: 100%; height: 250px; }
.line-chart-wrapper { width: 100%; height: 320px; padding: 10px 20px 20px 20px; } 

/* GROWTH LIST */
.growth-list { padding: 0 20px 20px 20px; display: flex; flex-direction: column; gap: 12px; }
.growth-item { display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px dashed #f3f4f6; }
.growth-item:last-child { border-bottom: none; }
.growth-label { display: flex; align-items: center; gap: 12px; color: #4b5563; font-size: 13px; font-weight: 500; }

.growth-value-box { display: flex; align-items: center; gap: 16px; }
.growth-number { font-size: 14px; font-weight: 600; color: #1f2937; }
.growth-percent { font-size: 12px; font-weight: 600; padding: 4px 8px; border-radius: 4px; }
.bg-success-light { background: #e0e7ff; }
.bg-danger-light { background: #fef2f2; }
.empty-growth { display: flex; align-items: center; justify-content: center; height: 100px; color: #6b7280; font-size: 13px; }
.empty-state { text-align: center; padding: 40px 20px; color: #6b7280; font-size: 13px; }

/* --- EMAIL MODAL CSS --- */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  width: 450px;
  max-width: 90%;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.modal-title { font-size: 18px; font-weight: 600; color: #1f2937; margin: 0 0 8px 0; }
.modal-subtitle { font-size: 13px; color: #6b7280; margin: 0 0 20px 0; }

.form-group { margin-bottom: 16px; }
.form-label { display: block; font-size: 13px; font-weight: 500; color: #374151; margin-bottom: 6px; }
.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  color: #1f2937;
  outline: none;
}
.form-input:focus { border-color: #1e3a8a; box-shadow: 0 0 0 2px rgba(30,58,138,0.1); }

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

/* SCROLLBAR CUSTOMIZATION */
::-webkit-scrollbar { width: 6px; height: 6px; }
::-webkit-scrollbar-track { background: transparent; }
::-webkit-scrollbar-thumb { background-color: #d1d5db; border-radius: 10px; }
::-webkit-scrollbar-thumb:hover { background-color: #6b7280; }
</style>