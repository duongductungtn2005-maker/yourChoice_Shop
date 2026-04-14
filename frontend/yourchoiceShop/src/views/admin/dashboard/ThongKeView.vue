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
        
        <div class="flex align-center gap-8 mb-16 mt-1">
          <p class="stat-value text-success m-0">{{ formatCurrency(item.revenue) }}</p>
          <span class="growth-badge" :class="item.growthPercent >= 0 ? 'bg-success-light text-success' : 'bg-danger-light text-danger'">
            {{ item.growthPercent >= 0 ? '↑' : '↓' }} {{ Math.abs(item.growthPercent).toFixed(1) }}%
          </span>
        </div>
        
        <div class="stat-sub-info">
          <span class="text-muted">Sản phẩm đã bán: </span><span class="font-medium">{{ item.products }}</span> <span class="divider">|</span> 
          <span class="text-muted">Đơn hàng: </span><span class="font-medium">{{ item.totalOrders }}</span>
        </div>
        
        <div class="stat-sub-info mt-1">
          <span class="text-muted">Hoàn thành: </span><span class="text-success font-medium">{{ item.successOrders }}</span> <span class="divider">|</span> 
          <span class="text-muted">Hủy: </span><span class="text-danger font-medium">{{ item.cancelOrders || 0 }}</span> <span class="divider">|</span> 
          <span class="text-muted">Xử lý/Trả: </span><span class="text-warning font-medium">{{ (item.processingOrders || 0) + (item.returnOrders || 0) }}</span>
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

          <button @click="showConfirmExportModal = true" class="btn btn-excel">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="9" y1="15" x2="15" y2="15"></line></svg>
            Xuất Excel
          </button>
        </div>
      </div>

      <div class="filter-summary-row" style="grid-template-columns: repeat(8, 1fr);">
        <div class="f-stat-item">
          <span class="f-label">Số đơn hàng</span>
          <span class="f-value text-black">{{ filterSummary.totalOrders }}</span>
        </div>
        <div class="f-stat-item">
          <span class="f-label">Số sản phẩm</span>
          <span class="f-value text-black">{{ filterSummary.totalProducts }}</span>
        </div>
        <div class="f-stat-item">
          <span class="f-label">Hoàn thành</span>
          <span class="f-value text-success">{{ filterSummary.successOrders }}</span>
        </div>
        <div class="f-stat-item">
          <span class="f-label">Đang xử lý</span>
          <span class="f-value text-warning">{{ filterSummary.processingOrders }}</span>
        </div>
        <div class="f-stat-item">
          <span class="f-label">Huỷ</span>
          <span class="f-value text-danger">{{ filterSummary.cancelOrders }}</span>
        </div>
        <div class="f-stat-item text-right">
          <span class="f-label">DT Dự kiến</span>
          <span class="f-value text-muted" style="text-decoration: line-through;">{{ formatCurrency(filterSummary.expectedRevenue) }}</span>
        </div>
        <div class="f-stat-item text-right">
          <span class="f-label">DT Thực tế</span>
          <span class="f-value text-success">{{ formatCurrency(filterSummary.actualRevenue) }}</span>
        </div>
        <div class="f-stat-item text-right" style="background: #eff6ff; padding: 4px 8px; border-radius: 6px;">
          <span class="f-label" style="color: #1e3a8a; font-weight: 600;">Lợi nhuận (Ước tính)</span>
          <span class="f-value" style="color: #1e3a8a;">{{ formatCurrency(filterSummary.profit) }}</span>
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
              <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="mb-8"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
              <p>Chưa có dữ liệu trạng thái biểu đồ</p>
           </div>
        </div>
      </div>
    </div>

    <div class="row-layout mb-24">
      <div class="content-card">
        <div class="card-header border-none flex-between bg-danger-light">
          <span class="font-bold text-danger flex align-center gap-6">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
            Cảnh báo sắp hết hàng
          </span>
          <span class="text-xs badge-danger">Kho <= {{ lowStockThreshold }} SP</span>
        </div>
        <div class="table-responsive" style="max-height: 400px; overflow-y: auto;">
          <table class="modern-table">
            <thead>
              <tr>
                <th class="text-center w-50">STT</th>
                <th class="text-center w-80">Ảnh</th>
                <th class="text-left">Tên Sản Phẩm</th>
                <th class="text-center">Kích cỡ</th>
                <th class="text-center">Tồn kho</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="lowStockProducts.length === 0">
                <td colspan="5" class="empty-state">Kho hàng đang ổn định</td>
              </tr>
              <tr v-for="(sp, i) in lowStockProducts" :key="i">
                <td class="text-center text-muted">{{ i + 1 }}</td>
                <td class="text-center">
                  <img v-if="sp?.anh" :src="sp.anh" class="product-img" />
                  <div v-else class="product-img no-img">No Img</div>
                </td>
                <td class="font-medium text-dark text-left">{{ sp?.tenSanPham || sp?.ten || sp?.tenSP || 'N/A' }}</td>
                <td class="text-center"><span class="badge-blue-light">{{ sp?.kichCo || sp?.tenKichCo || sp?.size || 'N/A' }}</span></td>
                <td class="text-center"><span class="text-danger font-bold">{{ sp?.soLuongBan ?? 0 }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="content-card chart-card">
        <div class="card-header border-none">Tỉ lệ bán theo Kích cỡ (Size)</div>
        <div class="chart-container">
           <div class="chart-wrapper" v-if="hasSizeChartData">
              <Doughnut :data="sizeChartData" :options="chartOptions" :plugins="[outlabelsPlugin]" />
           </div>
           <div v-else class="empty-state">
              <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" class="mb-8"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
              <p>Chưa có dữ liệu bán hàng theo size</p>
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
                <th class="text-center w-120">Giá</th>
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
                <td class="font-medium text-dark text-center">
                  {{ prod?.tenSanPham }} 
                  <span v-if="prod?.kichCo" class="text-xs text-muted block mt-1">Size: {{ prod.kichCo }}</span>
                </td>
                <td class="text-center text-danger font-medium">{{ formatCurrency(prod?.doanhThu) }}</td>
                <td class="text-center">
                  <span class="badge-success-light" >{{ prod?.soLuongBan }}</span>
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

      <div class="content-card chart-card">
        <div class="card-header border-none flex-between">
          <span>Biểu đồ Tốc độ tăng trưởng (%)</span>
        </div>
        <div class="chart-container line-chart-wrapper" style="padding: 20px;">
           <Line :data="growthChartData" :options="growthChartOptions" />
        </div>
      </div>
    </div>

    <div v-if="showEmailModal" class="modal-overlay">
      <div class="modal-content large-modal">
        <div class="modal-header">
          <h3 class="modal-title">Gửi Báo Cáo & Quản Lý Danh Bạ</h3>
          <button class="close-btn" @click="closeEmailModal">✕</button>
        </div>

        <div class="tab-nav">
          <button class="tab-btn" :class="{ active: activeEmailTab === 'SEND' }" @click="activeEmailTab = 'SEND'">📤 Gửi Báo Cáo</button>
          <button class="tab-btn" :class="{ active: activeEmailTab === 'MANAGE' }" @click="activeEmailTab = 'MANAGE'">👥 Quản Lý Danh Bạ</button>
        </div>

        <div class="tab-body" v-if="activeEmailTab === 'SEND'">
          <div class="tab-content-wrapper">
            
            <div class="form-section">
              <label class="form-label font-bold">1. Tìm kiếm / Nhập Email người nhận:</label>
              <div class="search-add-group">
                <input type="email" v-model="searchEmailQuery" class="form-input input-den" placeholder="Nhập email và ấn Thêm..." @keyup.enter="handleSearchAndAddEmail" />
                <button class="btn btn-primary" @click="handleSearchAndAddEmail">Thêm</button>
              </div>
            </div>

            <div class="form-section mt-16">
              <label class="form-label font-bold">Hoặc chọn nhanh từ danh bạ:</label>
              <div class="checkbox-list-container">
                 <div v-if="emailDatabaseList.length === 0" class="text-muted text-sm p-2 text-center">Danh bạ trống. Hãy qua tab Quản lý để thêm!</div>
                 <label v-for="e in paginatedSendEmails" :key="e.id" class="checkbox-item">
                   <input type="checkbox" :value="e.email" v-model="selectedEmailAddresses" />
                   <span class="font-medium text-dark">{{ e.name }}</span> <span class="text-muted">({{ e.email }})</span>
                 </label>
              </div>
              <div class="mini-pagination" v-if="totalSendPages > 1">
                <button :disabled="sendPage === 1" @click="sendPage--">‹</button>
                <span class="page-info-text">{{ sendPage }} / {{ totalSendPages }}</span>
                <button :disabled="sendPage === totalSendPages" @click="sendPage++">›</button>
              </div>
            </div>

            <div class="form-section mt-16 flex-grow-1">
              <label class="form-label font-bold">Danh sách sẽ nhận báo cáo ({{ computedSelectedEmails.length }}):</label>
              <div class="table-container max-h-150">
                <table class="modern-table border-table">
                  <thead>
                    <tr>
                      <th class="text-left">Email</th>
                      <th class="text-left">Tên người nhận</th>
                      <th class="text-center w-50">Xóa</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-if="computedSelectedEmails.length === 0">
                      <td colspan="3" class="text-center text-muted">Chưa chọn người nhận nào</td>
                    </tr>
                    <tr v-for="item in computedSelectedEmails" :key="item.email">
                      <td class="code-text text-left">
                        {{ item.email }}
                        <span v-if="!item.isSaved" class="badge-temp ml-2">Tạm thời</span>
                      </td>
                      <td class="text-left">{{ item.name }}</td>
                      <td class="text-center">
                        <button class="btn-delete" @click="removeSelectedEmail(item.email)" title="Xóa khỏi danh sách gửi">✕</button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div class="form-section mt-16 config-box">
              <label class="form-label font-bold">2. Chọn dữ liệu báo cáo:</label>
              
              <div class="radio-group flex gap-8 mb-8 mt-4">
                <label class="cursor-pointer font-medium text-dark flex align-center gap-2">
                  <input type="radio" value="TODAY" v-model="sendMode" /> Dữ liệu hôm nay (Từ 00:00 đến hiện tại)
                </label>
                <label class="cursor-pointer font-medium text-dark flex align-center gap-2" style="margin-left: 20px;">
                  <input type="radio" value="CUSTOM" v-model="sendMode" /> Tùy chỉnh mốc thời gian
                </label>
              </div>

              <div class="custom-date-box" v-if="sendMode === 'CUSTOM'">
                <div class="date-picker-group">
                  <div class="w-full">
                    <label class="text-xs text-muted block mb-4">Từ ngày giờ:</label>
                    <input type="datetime-local" v-model="reportConfig.fromDate" class="form-input input-den w-full" />
                  </div>
                  <span class="separator mt-16 px-12">-</span>
                  <div class="w-full">
                    <label class="text-xs text-muted block mb-4">Đến ngày giờ:</label>
                    <input type="datetime-local" v-model="reportConfig.toDate" class="form-input input-den w-full" />
                  </div>
                </div>
              </div>
            </div>

          </div>

          <div class="modal-actions border-top pt-16 mt-16">
            <span class="text-xs text-muted flex-grow-1" style="align-self: center;">
              *Lưu ý: Hệ thống vẫn sẽ tự động gửi báo cáo vào 17h hàng ngày cho các email trong danh bạ.
            </span>
            <button class="btn btn-outline" @click="closeEmailModal">Đóng</button>
            <button class="btn btn-gradient" @click="handleSendNow" :disabled="isSendingEmail">
              <i class="fas" :class="isSendingEmail ? 'fa-spinner fa-spin' : 'fa-paper-plane'"></i> 
              {{ isSendingEmail ? 'Đang xử lý...' : 'Gửi Báo Cáo Này Đi' }}
            </button>
          </div>
        </div>

        <div class="tab-body" v-if="activeEmailTab === 'MANAGE'">
          <div class="tab-content-wrapper">
            <div class="add-new-box mb-16">
              <label class="form-label font-bold">Thêm liên hệ mới:</label>
              <div class="flex gap-8">
                <input type="text" v-model="newContact.name" class="form-input input-den flex-1" placeholder="Tên (VD: Giám đốc)" />
                <input type="email" v-model="newContact.email" class="form-input input-den flex-2" placeholder="Địa chỉ Email" />
                <button class="btn btn-primary" @click="addNewContact">
                  <i class="fas fa-plus"></i> Lưu DB
                </button>
              </div>
            </div>

            <label class="form-label font-bold">Danh sách Email sẽ nhận báo cáo tự động ({{ emailDatabaseList.length }}):</label>
            <div class="table-container flex-grow-1 border-table-wrapper">
              <table class="modern-table border-table">
                <thead>
                  <tr>
                    <th class="text-center w-50">STT</th>
                    <th class="text-left">Tên</th>
                    <th class="text-left">Email</th>
                    <th class="text-center w-80">Thao tác</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="emailDatabaseList.length === 0">
                    <td colspan="4" class="text-center empty-state">Không có dữ liệu danh bạ</td>
                  </tr>
                  <tr v-for="(dbItem, index) in paginatedManageEmails" :key="dbItem.id">
                    <td class="text-center text-muted">{{ (managePage - 1) * managePageSize + index + 1 }}</td>
                    <td class="font-medium text-dark text-left">{{ dbItem.name }}</td>
                    <td class="code-text text-left">{{ dbItem.email }}</td>
                    <td class="text-center">
                      <button class="btn-delete px-12" @click="deleteContact(dbItem.id)" title="Xóa khỏi DB">
                        <i class="fas fa-trash-alt"></i> Xóa
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
              
              <div class="pagination-footer bg-white" v-if="totalManagePages > 1">
                <div class="page-info text-muted">
                  Hiển thị trang {{ managePage }} / {{ totalManagePages }}
                </div>
                <div class="page-controls">
                  <button :disabled="managePage === 1" @click="changeManagePage(managePage - 1)">‹</button>
                  <button 
                    v-for="p in visibleManagePages" 
                    :key="p" 
                    :class="{ active: p === managePage }" 
                    @click="changeManagePage(p)"
                  >
                    {{ p }}
                  </button>
                  <button :disabled="managePage === totalManagePages" @click="changeManagePage(managePage + 1)">›</button>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>

    <div v-if="showConfirmExportModal" class="modal-overlay">
      <div class="confirm-modal">
        <div class="confirm-icon">
          <span class="question-mark">?</span>
        </div>
        <h3 class="confirm-title">Xác nhận</h3>
        <p class="confirm-text">Bạn có muốn tải xuống danh sách đơn hàng không?</p>
        <div class="confirm-actions">
          <button class="btn btn-yes" @click="handleExportExcel">Có</button>
          <button class="btn btn-no" @click="showConfirmExportModal = false">Hủy</button>
        </div>
      </div>
    </div>

    <div v-if="showSuccessToast" class="toast-success">
      <div class="toast-icon">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
      </div>
      <span>Xuất Excel thành công</span>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import Swal from 'sweetalert2'
import { statisticApi } from '@/api/statisticApi' 
import { Chart as ChartJS, ArcElement, Tooltip, Legend, CategoryScale, LinearScale, PointElement, LineElement, Title, Filler } from 'chart.js'
import { Doughnut, Line } from 'vue-chartjs'
ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, PointElement, LineElement, Title, Filler)

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0)
const formatNumber = (val) => new Intl.NumberFormat('vi-VN').format(val || 0)

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

          let startAngle = element.startAngle;
          let endAngle = element.endAngle;
          let midAngle = startAngle + (endAngle - startAngle) / 2;

          if (endAngle - startAngle >= Math.PI * 1.99) midAngle = 0; 

          const outerRadius = element.outerRadius;
          const lineStartRadius = outerRadius; 
          const lineEndRadius = outerRadius + 20; 

          const x = element.x;
          const y = element.y;

          const startX = x + Math.cos(midAngle) * lineStartRadius;
          const startY = y + Math.sin(midAngle) * lineStartRadius;
          const edgeX = x + Math.cos(midAngle) * lineEndRadius;
          const edgeY = y + Math.sin(midAngle) * lineEndRadius;

          const isRight = Math.cos(midAngle) >= 0;
          const endX = edgeX + (isRight ? 20 : -20); 
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
          const percent = ((dataVal / total) * 100).toFixed(1) + '%';
          
          ctx.textAlign = isRight ? 'left' : 'right';
          ctx.textBaseline = 'middle';
          
          ctx.fillStyle = '#6b7280';
          ctx.font = '12px sans-serif';
          ctx.fillText(label, endX + (isRight ? 5 : -5), endY - 8);
          
          ctx.fillStyle = '#1f2937';
          ctx.font = 'bold 11px sans-serif';
          ctx.fillText(`${dataVal} (${percent})`, endX + (isRight ? 5 : -5), endY + 8);
        });
      }
    });
  }
};

const activeFilter = ref('DAY') 
const filter = ref({ fromDate: null, toDate: null, status: null, page: 0, size: 5 }) 
const lowStockThreshold = ref(10) 
const lowStockFilter = ref({ page: 0, size: 5 }) 

const topProducts = ref([])         
const lowStockProducts = ref([])    

const filterSummary = ref({
  totalOrders: 0, totalProducts: 0, successOrders: 0, processingOrders: 0, 
  cancelOrders: 0, expectedRevenue: 0, actualRevenue: 0, profit: 0
})

const isLoadingCards = ref(false)   
const isLoadingLowStock = ref(false)
const hasChartData = ref(false)
const hasSizeChartData = ref(false)

const summaryCards = ref([
  { title: 'Hôm nay', revenue: 0, products: 0, totalOrders: 0, successOrders: 0, cancelOrders: 0, processingOrders: 0, returnOrders: 0, growthPercent: 0 },
  { title: 'Tuần này', revenue: 0, products: 0, totalOrders: 0, successOrders: 0, cancelOrders: 0, processingOrders: 0, returnOrders: 0, growthPercent: 0 },
  { title: 'Tháng này', revenue: 0, products: 0, totalOrders: 0, successOrders: 0, cancelOrders: 0, processingOrders: 0, returnOrders: 0, growthPercent: 0 },
  { title: 'Năm nay', revenue: 0, products: 0, totalOrders: 0, successOrders: 0, cancelOrders: 0, processingOrders: 0, returnOrders: 0, growthPercent: 0 }
])

const lineChartData = ref({ labels: [], datasets: [{ label: 'Doanh thu', data: [], borderColor: '#1e3a8a', backgroundColor: 'rgba(30, 58, 138, 0.1)', fill: true, tension: 0.4 }] })
const lineChartOptions = ref({ responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true }, x: { grid: { display: false } } } })

const growthChartData = ref({
  labels: ['Hôm nay', 'Tuần này', 'Tháng này', 'Năm nay'],
  datasets: [
    { label: 'Doanh thu', data: [0, 0, 0, 0], borderColor: '#3b82f6', backgroundColor: '#3b82f6', tension: 0.4 },
    { label: 'Đơn hàng', data: [0, 0, 0, 0], borderColor: '#10b981', backgroundColor: '#10b981', tension: 0.4 },
    { label: 'Sản phẩm', data: [0, 0, 0, 0], borderColor: '#f59e0b', backgroundColor: '#f59e0b', tension: 0.4 }
  ]
})
const growthChartOptions = ref({
  responsive: true, maintainAspectRatio: false,
  plugins: { legend: { position: 'bottom', labels: { usePointStyle: true, boxWidth: 8 } }, tooltip: { callbacks: { label: function(context) { return context.dataset.label + ': ' + context.parsed.y.toFixed(1) + '%'; } } } },
  scales: { y: { beginAtZero: true, ticks: { callback: function(value) { return value + '%' } } }, x: { grid: { display: false } } }
})

const chartData = ref({ labels: [], datasets: [{ backgroundColor: [], data: [], borderWidth: 1, hoverOffset: 4 }] })
const chartOptions = ref({ responsive: true, maintainAspectRatio: false, cutout: '55%', layout: { padding: { top: 60, bottom: 80, left: 80, right: 80 } }, plugins: { legend: { position: 'bottom' } } })

const sizeChartData = ref({ labels: [], datasets: [{ backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6'], data: [], borderWidth: 1 }] })

const showEmailModal = ref(false)
const activeEmailTab = ref('SEND')
const emailDatabaseList = ref([]) 
const newContact = ref({ name: '', email: '' })
const managePage = ref(1); const managePageSize = 5;
const totalManagePages = computed(() => Math.ceil(emailDatabaseList.value.length / managePageSize));
const paginatedManageEmails = computed(() => { const start = (managePage.value - 1) * managePageSize; return emailDatabaseList.value.slice(start, start + managePageSize); });
const changeManagePage = (p) => { if (p >= 1 && p <= totalManagePages.value) managePage.value = p; };
const visibleManagePages = computed(() => { let p = []; for (let i = 1; i <= totalManagePages.value; i++) { if (i===1 || i===totalManagePages.value || (i>=managePage.value-1 && i<=managePage.value+1)) p.push(i); } return [...new Set(p)].sort((a,b)=>a-b); });

const EMAIL_API_URL = 'http://localhost:8080/api/v1/email-recipients';
const isSendingEmail = ref(false); const sendMode = ref('TODAY'); const searchEmailQuery = ref(''); const selectedEmailAddresses = ref([]); const reportConfig = ref({ fromDate: '', toDate: '' });
const sendPage = ref(1); const sendPageSize = 5;
const totalSendPages = computed(() => Math.ceil(emailDatabaseList.value.length / sendPageSize));
const paginatedSendEmails = computed(() => { const start = (sendPage.value - 1) * sendPageSize; return emailDatabaseList.value.slice(start, start + sendPageSize); });
const computedSelectedEmails = computed(() => {
  return selectedEmailAddresses.value.map(emailStr => {
    const foundInDB = emailDatabaseList.value.find(e => e.email === emailStr);
    return foundInDB ? { email: foundInDB.email, name: foundInDB.name, isSaved: true } : { email: emailStr, name: '(Email ngoài)', isSaved: false };
  });
})
const showConfirmExportModal = ref(false)
const showSuccessToast = ref(false)

const formatToLocalDateTime = (date, isEnd = false) => {
  const d = new Date(date); if (isEnd) d.setHours(23, 59, 59, 999); else d.setHours(0, 0, 0, 0);
  const year = d.getFullYear(); const month = String(d.getMonth() + 1).padStart(2, '0'); const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0'); const minutes = String(d.getMinutes()).padStart(2, '0'); const seconds = String(d.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
}
const formatYYYYMMDD = (d) => { const year = d.getFullYear(); const month = String(d.getMonth() + 1).padStart(2, '0'); const day = String(d.getDate()).padStart(2, '0'); return `${year}-${month}-${day}`; }

const getPeriodsData = () => {
  const now = new Date();
  
  const todayStart = formatToLocalDateTime(now); const todayEnd = formatToLocalDateTime(now, true);
  const yesterday = new Date(now); yesterday.setDate(yesterday.getDate() - 1);
  const yestStart = formatToLocalDateTime(yesterday); const yestEnd = formatToLocalDateTime(yesterday, true);
  
  const d = new Date(now);
  const weekStartObj = new Date(d.setDate(d.getDate() - d.getDay() + (d.getDay() === 0 ? -6 : 1)));
  const weekStart = formatToLocalDateTime(weekStartObj);
  const lastWeekStartObj = new Date(weekStartObj); lastWeekStartObj.setDate(lastWeekStartObj.getDate() - 7);
  const lastWeekEndObj = new Date(lastWeekStartObj); lastWeekEndObj.setDate(lastWeekEndObj.getDate() + 6);
  const lastWeekStart = formatToLocalDateTime(lastWeekStartObj); const lastWeekEnd = formatToLocalDateTime(lastWeekEndObj, true);
  
  const monthStartObj = new Date(now.getFullYear(), now.getMonth(), 1); const monthStart = formatToLocalDateTime(monthStartObj);
  const lastMonthStartObj = new Date(now.getFullYear(), now.getMonth() - 1, 1);
  const lastMonthEndObj = new Date(now.getFullYear(), now.getMonth(), 0);
  const lastMonthStart = formatToLocalDateTime(lastMonthStartObj); const lastMonthEnd = formatToLocalDateTime(lastMonthEndObj, true);
  
  const yearStartObj = new Date(now.getFullYear(), 0, 1); const yearStart = formatToLocalDateTime(yearStartObj);
  const lastYearStartObj = new Date(now.getFullYear() - 1, 0, 1);
  const lastYearEndObj = new Date(now.getFullYear() - 1, 11, 31);
  const lastYearStart = formatToLocalDateTime(lastYearStartObj); const lastYearEnd = formatToLocalDateTime(lastYearEndObj, true);
  
  return [
    { current: { fromDate: todayStart, toDate: todayEnd }, prev: { fromDate: yestStart, toDate: yestEnd } },
    { current: { fromDate: weekStart, toDate: todayEnd }, prev: { fromDate: lastWeekStart, toDate: lastWeekEnd } },
    { current: { fromDate: monthStart, toDate: todayEnd }, prev: { fromDate: lastMonthStart, toDate: lastMonthEnd } },
    { current: { fromDate: yearStart, toDate: todayEnd }, prev: { fromDate: lastYearStart, toDate: lastYearEnd } }
  ]
}

const calcGrowthRate = (curr, prev) => {
    if (prev === 0 && curr === 0) return 0;
    if (prev === 0 && curr > 0) return 100;
    return ((curr - prev) / prev) * 100;
};

const fetchSummaryCards = async () => {
  isLoadingCards.value = true;
  try {
    const periodsData = getPeriodsData();
    const currentReqs = periodsData.map(p => statisticApi.getRevenue({ fromDate: p.current.fromDate, toDate: p.current.toDate, status: null }));
    const prevReqs = periodsData.map(p => statisticApi.getRevenue({ fromDate: p.prev.fromDate, toDate: p.prev.toDate, status: null }));
    
    const [currentRes, prevRes] = await Promise.all([ Promise.all(currentReqs), Promise.all(prevReqs) ]);
    
    const revData = []; const ordData = []; const prodData = [];

    currentRes.forEach((res, index) => {
      const currSummary = res.data?.summary || {}; 
      const prevSummary = prevRes[index].data?.summary || {};
      
      const currRev = currSummary.totalRevenue || 0; const prevRev = prevSummary.totalRevenue || 0;
      const revGrowth = calcGrowthRate(currRev, prevRev);

      const currOrders = currSummary.successOrders || 0; const prevOrders = prevSummary.successOrders || 0;
      const ordGrowth = calcGrowthRate(currOrders, prevOrders);

      const currProds = currSummary.totalProducts || 0; const prevProds = prevSummary.totalProducts || 0;
      const prodGrowth = calcGrowthRate(currProds, prevProds);

      summaryCards.value[index].revenue = currRev;
      summaryCards.value[index].totalOrders = currSummary.totalOrders || 0; 
      summaryCards.value[index].successOrders = currOrders;
      summaryCards.value[index].cancelOrders = currSummary.cancelOrders || 0;
      summaryCards.value[index].returnOrders = currSummary.returnOrders || 0;
      summaryCards.value[index].processingOrders = currSummary.processingOrders || 0;
      summaryCards.value[index].products = currProds;
      summaryCards.value[index].growthPercent = revGrowth; 

      revData.push(revGrowth); ordData.push(ordGrowth); prodData.push(prodGrowth);
    });

    growthChartData.value = {
      labels: ['Hôm nay', 'Tuần này', 'Tháng này', 'Năm nay'],
      datasets: [
        { ...growthChartData.value.datasets[0], data: revData },
        { ...growthChartData.value.datasets[1], data: ordData },
        { ...growthChartData.value.datasets[2], data: prodData }
      ]
    };
  } catch (error) { console.error(error) } finally { isLoadingCards.value = false; }
}

const fetchFilterSummary = async (payload) => {
  try {
    const resAll = await statisticApi.getRevenue({ ...payload, status: null });
    const summaryAll = resAll.data?.summary || {};

    filterSummary.value.actualRevenue = summaryAll.totalRevenue || 0; 
    filterSummary.value.expectedRevenue = summaryAll.expectedRevenue || 0; 
    filterSummary.value.profit = (summaryAll.totalRevenue || 0) * 0.35; 
    filterSummary.value.totalOrders = summaryAll.totalOrders || 0; 
    filterSummary.value.totalProducts = summaryAll.totalProducts || 0;
    filterSummary.value.successOrders = summaryAll.successOrders || 0; 
    filterSummary.value.processingOrders = summaryAll.processingOrders || 0; 
    filterSummary.value.cancelOrders = summaryAll.cancelOrders || 0;
  } catch (error) { console.error("Lỗi fetchFilterSummary:", error); }
}

const fetchLineChartData = async (payload) => {
  try {
    const res = await statisticApi.getRevenue(payload); 
    const dataList = res.data?.chartData || res.data?.data || []; 
    if(Array.isArray(dataList) && dataList.length > 0) {
       lineChartData.value = {
         labels: dataList.map(item => item.date || item.ngay || item.label || item.thoiGian || ''),
         datasets: [{ ...lineChartData.value.datasets[0], data: dataList.map(item => item.value || item.doanhThu || item.revenue || item.tongTien || 0) }]
       };
    } else { lineChartData.value = { labels: [], datasets: [{ ...lineChartData.value.datasets[0], data: [] }] }; }
  } catch (error) { console.error("Lỗi fetchLineChartData:", error); }
}

const fetchChartStatus = async (customPayload) => {
  try {
    const payloadForChart = { ...customPayload, status: null };
    const res = await statisticApi.getRevenue(payloadForChart);
    const summary = res.data?.summary || {};

    const success = summary.successOrders || 0; const processing = summary.processingOrders || 0; const cancel = summary.cancelOrders || 0;

    if (success === 0 && processing === 0 && cancel === 0) { hasChartData.value = false; return; }

    hasChartData.value = true;
    const labels = []; const data = []; const bgColors = [];

    if (success > 0) { labels.push('Hoàn thành'); data.push(success); bgColors.push('#10b981'); }
    if (processing > 0) { labels.push('Đang xử lý'); data.push(processing); bgColors.push('#f59e0b'); }
    if (cancel > 0) { labels.push('Đã hủy'); data.push(cancel); bgColors.push('#ef4444'); }

    chartData.value = { labels: labels, datasets: [{ ...chartData.value.datasets[0], data: data, backgroundColor: bgColors }] };
  } catch (error) { console.error("Lỗi fetchChartStatus:", error); hasChartData.value = false; }
}

const fetchSizeChartData = async (payload) => {
  try {
    hasSizeChartData.value = true;
    sizeChartData.value = {
      labels: ['Size S', 'Size M', 'Size L', 'Size XL'],
      datasets: [{
        ...sizeChartData.value.datasets[0],
        data: [120, 310, 240, 85], 
        backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6']
      }]
    };
  } catch (error) { hasSizeChartData.value = false; }
}

const applyQuickFilter = (type) => {
  activeFilter.value = type;
  const now = new Date(); let start = ''; const end = formatYYYYMMDD(now); 
  if (type === 'DAY') start = end;
  else if (type === 'WEEK') { const d = new Date(); start = formatYYYYMMDD(new Date(d.setDate(d.getDate() - d.getDay() + (d.getDay() === 0 ? -6 : 1)))); } 
  else if (type === 'MONTH') start = formatYYYYMMDD(new Date(now.getFullYear(), now.getMonth(), 1));
  else if (type === 'YEAR') start = formatYYYYMMDD(new Date(now.getFullYear(), 0, 1));
  filter.value.fromDate = start; filter.value.toDate = end;
  applyCustomFilter();
}

const applyCustomFilter = async () => {
  const payload = {
    fromDate: filter.value.fromDate ? `${filter.value.fromDate}T00:00:00` : null,
    toDate: filter.value.toDate ? `${filter.value.toDate}T23:59:59` : null,
    status: null, size: filter.value.size, page: filter.value.page
  }
  
  try {
    const resTop = await statisticApi.getProductStats(payload);
    let dataList = resTop.data?.chartData || resTop.data?.data || resTop.data;
    if (dataList && !Array.isArray(dataList) && typeof dataList === 'object') {
       const key = Object.keys(dataList).find(k => Array.isArray(dataList[k])); if (key) dataList = dataList[key];
    }
    if (Array.isArray(dataList)) topProducts.value = dataList.filter(item => item != null);
  } catch (error) { console.error("Lỗi fetch sản phẩm:", error); }

  fetchChartStatus(payload);
  fetchFilterSummary(payload);
  fetchLineChartData(payload);
  fetchSizeChartData(payload); 
}

const changeTopPage = (newPage) => { filter.value.page = newPage; applyCustomFilter(); }

const fetchLowStock = async () => {
    isLoadingLowStock.value = true;
    try {
        const payload = { threshold: 10, size: 5, page: 0 };
        const response = await axios.post('http://localhost:8080/api/v1/statistics/LOW_STOCK', payload); 
        
        let rawData = response.data;
        let dataList = rawData?.content || rawData?.data || rawData?.chartData || rawData;

        if (Array.isArray(dataList)) {
           lowStockProducts.value = dataList.filter(item => item != null);
        } else if (Array.isArray(response)) {
           lowStockProducts.value = response; 
        } else {
           lowStockProducts.value = [];
        }
    } catch (error) { 
        console.error("LỖI GỌI API LOW STOCK:", error); 
    } finally { 
        isLoadingLowStock.value = false; 
    }
}

const fetchEmailDatabase = async () => { try { const res = await axios.get(EMAIL_API_URL); emailDatabaseList.value = res.data; } catch (error) { console.error("Lỗi lấy danh bạ email:", error); } }
const resetEmailModalData = () => { searchEmailQuery.value = ''; selectedEmailAddresses.value = []; reportConfig.value = { fromDate: '', toDate: '' }; sendMode.value = 'TODAY'; managePage.value = 1; sendPage.value = 1; activeEmailTab.value = 'SEND'; }
const closeEmailModal = () => { showEmailModal.value = false; }

const handleSearchAndAddEmail = async () => {
  const emailStr = searchEmailQuery.value.trim(); if (!emailStr) return;
  if (selectedEmailAddresses.value.includes(emailStr)) { searchEmailQuery.value = ''; return; }
  const foundInDB = emailDatabaseList.value.find(e => e.email === emailStr);
  if (foundInDB) { selectedEmailAddresses.value.push(emailStr); } else {
    const result = await Swal.fire({ title: 'Chưa có trong danh bạ!', text: `Email "${emailStr}" là email mới. Bạn có muốn thêm vào danh bạ luôn không?`, icon: 'question', showCancelButton: true, confirmButtonText: 'Có, lưu danh bạ', cancelButtonText: 'Không, chỉ gửi tạm' });
    if (result.isConfirmed) {
      try {
        const tempName = emailStr.split('@')[0]; 
        const res = await axios.post(EMAIL_API_URL, { name: tempName, email: emailStr });
        emailDatabaseList.value.unshift(res.data);
        Swal.fire({ icon: 'success', title: 'Thành công', text: 'Đã lưu vào danh bạ!', showConfirmButton: false, timer: 1500 });
      } catch (error) { Swal.fire('Lỗi', error.response?.data || 'Lỗi khi lưu DB.', 'error'); }
    } 
    selectedEmailAddresses.value.push(emailStr);
  }
  searchEmailQuery.value = '';
}
const removeSelectedEmail = (emailToRemove) => { selectedEmailAddresses.value = selectedEmailAddresses.value.filter(e => e !== emailToRemove); }

const addNewContact = async () => {
  if (!newContact.value.name || !newContact.value.email) { Swal.fire({ icon: 'warning', title: 'Thiếu thông tin', text: 'Vui lòng điền đủ Tên và Email!' }); return; }
  const exist = emailDatabaseList.value.find(e => e.email === newContact.value.email);
  if (exist) { Swal.fire({ icon: 'error', title: 'Trùng lặp', text: 'Email này đã tồn tại trong danh bạ!' }); return; }
  const result = await Swal.fire({ title: 'Xác nhận Thêm mới?', text: `Thêm "${newContact.value.email}" vào danh bạ hệ thống?`, icon: 'question', showCancelButton: true, confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy' });
  if(result.isConfirmed) {
    try {
      const res = await axios.post(EMAIL_API_URL, { name: newContact.value.name, email: newContact.value.email });
      emailDatabaseList.value.unshift(res.data); newContact.value = { name: '', email: '' };
      if (managePage.value > totalManagePages.value) managePage.value = totalManagePages.value;
      Swal.fire({ icon: 'success', title: 'Đã thêm!', showConfirmButton: false, timer: 1500 });
    } catch (error) { Swal.fire('Lỗi', error.response?.data || 'Không thể thêm liên hệ', 'error'); }
  }
}

const deleteContact = async (id) => {
  const emailObj = emailDatabaseList.value.find(e => e.id === id); if(!emailObj) return;
  const result = await Swal.fire({ title: 'Xác nhận Xóa?', text: `Bạn có chắc muốn xóa "${emailObj.email}" khỏi danh bạ?`, icon: 'warning', showCancelButton: true, confirmButtonText: 'Xóa ngay', cancelButtonText: 'Hủy', confirmButtonColor: '#ef4444' });
  if(result.isConfirmed) {
    try {
      await axios.delete(`${EMAIL_API_URL}/${id}`);
      emailDatabaseList.value = emailDatabaseList.value.filter(e => e.id !== id);
      selectedEmailAddresses.value = selectedEmailAddresses.value.filter(e => e !== emailObj.email);
      if (paginatedManageEmails.value.length === 0 && managePage.value > 1) managePage.value--;
      Swal.fire({ icon: 'success', title: 'Đã xóa!', showConfirmButton: false, timer: 1500 });
    } catch (error) { Swal.fire('Lỗi', 'Không thể xóa liên hệ', 'error'); }
  }
}

const handleSendNow = async () => {
  if (selectedEmailAddresses.value.length === 0) { Swal.fire({ icon: 'warning', title: 'Cảnh báo', text: 'Vui lòng chọn ít nhất 1 người nhận!' }); return; }
  const formatExactTime = (d) => { const pad = (n) => String(n).padStart(2, '0'); return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`; }
  let startTimeStr, endTimeStr;
  if (sendMode.value === 'CUSTOM') {
    if (!reportConfig.value.fromDate || !reportConfig.value.toDate) { Swal.fire({ icon: 'warning', title: 'Lỗi', text: 'Vui lòng chọn đủ Từ ngày giờ & Đến ngày giờ!' }); return; }
    startTimeStr = reportConfig.value.fromDate; endTimeStr = reportConfig.value.toDate;
  } else {
    const now = new Date(); const start = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0);
    startTimeStr = formatExactTime(start); endTimeStr = formatExactTime(now); 
  }
  const payload = { emails: selectedEmailAddresses.value, startTime: startTimeStr, endTime: endTimeStr, attachExcel: true };
  isSendingEmail.value = true;
  Swal.fire({ title: 'Đang xử lý...', text: 'Đang tổng hợp dữ liệu và gửi báo cáo, vui lòng đợi!', allowOutsideClick: false, didOpen: () => { Swal.showLoading(); } });
  try {
    await axios.post('http://localhost:8080/api/v1/reports/send-manual', payload);
    Swal.fire({ title: 'Đã gửi thành công!', text: `Hệ thống đã báo cáo tới ${payload.emails.length} liên hệ.`, icon: 'success', showConfirmButton: false, timer: 2000 });
    resetEmailModalData(); showEmailModal.value = false;
  } catch (error) { console.error("Lỗi gửi mail:", error); Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Có lỗi xảy ra phía máy chủ!' }); } finally { isSendingEmail.value = false; }
}

const handleExportExcel = async () => {
  try {
    const payload = { fromDate: filter.value.fromDate ? `${filter.value.fromDate}T00:00:00` : null, toDate: filter.value.toDate ? `${filter.value.toDate}T23:59:59` : null, status: null }
    const res = await statisticApi.exportRevenueExcel(payload);
    const url = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement('a'); link.href = url;
    link.setAttribute('download', `BaoCao_ThongKe_${formatYYYYMMDD(new Date())}.xlsx`);
    document.body.appendChild(link); link.click(); link.remove();
    showConfirmExportModal.value = false; showSuccessToast.value = true;
    setTimeout(() => { showSuccessToast.value = false; }, 3000);
  } catch (error) { console.error("Lỗi xuất Excel:", error); showConfirmExportModal.value = false; }
}

onMounted(() => {
  fetchSummaryCards(); 
  fetchLowStock(); 
  applyQuickFilter('DAY'); 
  fetchEmailDatabase(); 
})
</script>

<style scoped>
/* RESET & BASE */
.dashboard-container { padding: 24px; background-color: #ebecee; min-height: 100vh; color: #374151; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; box-sizing: border-box; position: relative; }
* { box-sizing: border-box; }
.page-header { margin-bottom: 24px; display: flex; align-items: center; }
.title-group { display: flex; flex-direction: column; gap: 4px; }
.page-title { font-size: 24px; font-weight: 700; color: #2b4360; margin: 0; display: flex; align-items: center; gap: 8px; }
.page-subtitle { font-size: 13px; color: #6b7280; margin: 0; }
.mb-24 { margin-bottom: 24px; } .m-0 { margin: 0 !important; } .mt-1 { margin-top: 4px; }
.w-full { width: 100%; } .flex { display: flex; } .gap-8 { gap: 8px; } .gap-2 { gap: 4px; } .flex-1 { flex: 1; } .flex-2 { flex: 2; }
.p-2 { padding: 8px; } .text-center { text-align: center;} .align-center { align-items: center; }

/* GRID LAYOUTS */
.summary-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px; }
.row-layout { display: grid; grid-template-columns: 7fr 5fr; gap: 24px; }

/* CONTENT CARDS */
.content-card { background: #fff; border-radius: 8px; border: 1px solid #f3f4f6; box-shadow: 0 1px 3px rgba(0,0,0,0.02); display: flex; flex-direction: column; overflow: hidden; }
.card-header { font-size: 16px; font-weight: 600; color: #374151; padding: 16px 20px; border-bottom: 1px solid #f3f4f6; }
.border-none { border-bottom: none !important; padding-bottom: 8px; }
.flex-between { display: flex; justify-content: space-between; align-items: center; }
.flex-center { display: flex; align-items: center; justify-content: center; }
.gap-6 { gap: 6px; }

/* SUMMARY CARDS */
.stat-card { background-color: #fff; border-radius: 8px; padding: 20px; display: flex; flex-direction: column; position: relative; border: 1px solid #f3f4f6; box-shadow: 0 1px 3px rgba(0,0,0,0.02); }
.stat-title { font-size: 14px; color: #6b7280; font-weight: 600; margin: 0 0 12px 0; }
.stat-value { font-size: 24px; font-weight: 700; letter-spacing: -0.5px; }
.stat-sub-info { font-size: 12px; color: #6b7280; }
.divider { margin: 0 4px; color: #d1d5db; }

/* BADGES MỚI */
.growth-badge { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 700; display: inline-block; }
.bg-success-light { background: #dcfce7 !important; } .bg-danger-light { background: #fee2e2 !important; }
.badge-success-light { background: #dcfce7; color: #16a34a; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 12px; }
.badge-blue-light { background: #dbeafe; color: #2563eb; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 12px; }
.badge-warning-light { background: #fef3c7; color: #d97706; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 12px; }
.badge-danger { background: #fee2e2; color: #ef4444; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 12px; }
.bg-danger-light { background-color: #fef2f2 !important; border-bottom: 1px solid #fee2e2 !important; }

/* COLOR UTILS */
.text-success { color: #1e3a8a !important; background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%) !important; -webkit-background-clip: text !important; -webkit-text-fill-color: transparent !important; }
.text-danger { color: #ef4444 !important; } .text-warning { color: #f59e0b !important; } .text-primary { color: #3b82f6 !important; } .text-purple { color: #8b5cf6 !important; } .text-muted { color: #6b7280 !important; } .text-dark { color: #1f2937 !important; } .text-black { color: #000 !important; font-weight: 600;} .font-medium { font-weight: 500; } .font-bold { font-weight: 700; } .text-xs { font-size: 10px; } .text-sm { font-size: 13px; } .block { display: block; } .cursor-pointer { cursor: pointer; } .hover-dark:hover { color: #374151 !important; }

/* FILTER SECTION */
.filter-card { background: #fff; border-radius: 8px; margin-bottom: 24px; border: 1px solid #f3f4f6; box-shadow: 0 1px 3px rgba(0,0,0,0.02); }
.filter-header { display: flex; justify-content: space-between; align-items: center; padding: 20px; border-bottom: 1px solid #f3f4f6; }
.filter-title { font-size: 16px; font-weight: 600; color: #374151; margin: 0 0 4px 0; }
.filter-subtitle { font-size: 13px; color: #6b7280; margin: 0; }
.filter-right { display: flex; align-items: center; gap: 16px; }

.button-group { display: flex; gap: 8px; }
.btn { padding: 6px 16px; font-size: 13px; font-weight: 500; cursor: pointer; transition: all 0.2s; background: #fff; border: 1px solid #e5e7eb; color: #6b7280; border-radius: 6px !important; }
.btn:not(.btn-excel):not(.btn-outline):hover { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; border-color: transparent; }
.btn-active { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%) !important; color: #fff !important; border-color: transparent !important; z-index: 2; position: relative; }
.btn-primary { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: white; border-color: transparent;} .btn-primary:hover { box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); transform: translateY(-1px);}
.btn-warning { background: #f59e0b; color: white; border-color: #f59e0b; font-weight: 600;} .btn-warning:hover:not(:disabled) { background: #d97706; }
.btn:disabled { opacity: 0.6; cursor: not-allowed;}
.btn-gradient { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; box-shadow: 0 4px 10px rgba(15, 23, 42, 0.2); border: none; font-weight: 600;} .btn-gradient:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15, 23, 42, 0.3); }

.btn-excel { background: #fff !important; border: 1px solid #e5e7eb !important; color: #334155 !important; border-radius: 6px; font-weight: 600; display: flex; align-items: center; gap: 6px; } .btn-excel:hover { background: #f8fafc !important; color: #0f172a !important; }

.date-picker-group { display: flex; align-items: center; gap: 8px; border: 1px solid #e5e7eb; padding: 4px 8px; border-radius: 6px; }
.date-input { border: none; font-size: 13px; color: #4b5563; outline: none; }

.filter-summary-row { display: grid; grid-template-columns: repeat(7, 1fr); padding: 16px 20px; background: #fafafa; border-radius: 0 0 8px 8px; }
.f-stat-item { display: flex; flex-direction: column; gap: 4px; }
.f-label { font-size: 12px; color: #6b7280; }
.f-value { font-size: 16px; font-weight: 700; }

/* TABLE STYLES */
.table-responsive { overflow-x: auto; flex: 1; padding: 0 20px; }
.modern-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.modern-table thead th { background: #fff; color: #6b7280; font-weight: 500; padding: 12px 8px; border-bottom: 1px solid #e5e7eb; }
.modern-table tbody tr { border-bottom: 1px dashed #f3f4f6; transition: background 0.2s; }
.modern-table tbody tr:hover { background: #f9fafb; }
.modern-table tbody td { padding: 12px 8px; color: #4b5563; vertical-align: middle !important; }

/* PAGINATION */
.pagination { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; background: #fff; font-size: 13px; color: #6b7280; }
.page-controls { display: flex; align-items: center; gap: 4px; }
.page-btn { width: 28px; height: 28px; border: 1px solid transparent; background: #fff; border-radius: 4px; cursor: pointer; display: flex; align-items: center; justify-content: center; color: #4b5563; font-weight: 500; }
.page-btn:hover:not(:disabled) { background: #f3f4f6; } .page-btn:disabled { opacity: 0.3; cursor: not-allowed; }
.page-btn.active { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; border-color: transparent; }

/* CHARTS */
.chart-card { min-height: 300px; }
.chart-container { flex: 1; display: flex; align-items: center; justify-content: center; padding: 20px; }
.chart-wrapper { width: 100%; height: 420px; } 
.line-chart-wrapper { width: 100%; height: 380px; padding: 10px 20px 20px 20px; } 

.empty-state { text-align: center; padding: 40px 20px; color: #6b7280; font-style: italic; font-size: 13px; }

/* MODAL CONFIRM & TOAST EXCEL */
.confirm-modal { background: #fff; padding: 32px 24px; border-radius: 12px; width: 400px; max-width: 90%; text-align: center; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
.confirm-icon { width: 70px; height: 70px; border-radius: 50%; border: 3px solid #6b7280; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; }
.question-mark { font-size: 36px; color: #6b7280; font-weight: 300; }
.confirm-actions { display: flex; justify-content: center; gap: 12px; }
.btn-yes { background-color: #7c3aed !important; color: white !important; padding: 10px 32px !important; border-radius: 6px !important; }
.btn-no { background-color: #6b7280 !important; color: white !important; padding: 10px 32px !important; border-radius: 6px !important; }

.toast-success { position: fixed; top: 24px; right: 24px; background: #fff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); padding: 16px 24px; display: flex; align-items: center; gap: 12px; z-index: 9999; font-weight: 500; color: #4b5563; animation: slideIn 0.3s ease-out; }
.toast-icon { width: 32px; height: 32px; border-radius: 50%; background: #dcfce7; display: flex; align-items: center; justify-content: center; }
@keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }

/* MODAL EMAIL MỚI */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background-color: rgba(0, 0, 0, 0.4); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-content { background: #fff; border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.1); display: flex; flex-direction: column; overflow: hidden; }
.large-modal { width: 850px; max-width: 95%; max-height: 90vh; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 24px; border-bottom: 1px solid #eee; }
.modal-title { font-size: 20px; font-weight: 700; color: #1e293b; margin: 0; }
.close-btn { background: none; border: none; font-size: 20px; color: #64748b; cursor: pointer; } .close-btn:hover { color: #ef4444; }
.tab-nav { display: flex; background: #f8fafc; border-bottom: 1px solid #e2e8f0; }
.tab-btn { flex: 1; padding: 14px 0; font-size: 14px; font-weight: 600; color: #64748b; border: none; background: none; border-bottom: 2px solid transparent; cursor: pointer; transition: 0.2s; }
.tab-btn:hover { color: #1e3a8a; } .tab-btn.active { color: #1e3a8a; border-bottom-color: #1e3a8a; background: #fff; }
.tab-body { height: 650px; padding: 24px; overflow-y: auto; display: flex; flex-direction: column; }
.tab-content-wrapper { display: flex; flex-direction: column; height: 100%; }
.input-den::placeholder { color: #000000 !important; opacity: 1 !important; font-weight: 500; } .input-den { font-weight: 600; color: #1e293b;}
.mt-16 { margin-top: 16px; } .mb-16 { margin-bottom: 16px; } .mb-8 { margin-bottom: 8px; } .mb-4 { margin-bottom: 4px; } .px-12 { padding-left: 12px !important; padding-right: 12px !important; }
.form-section { display: flex; flex-direction: column; gap: 8px; } .form-label { font-size: 14px; color: #374151; }
.form-input { padding: 10px 12px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px; outline: none; } .form-input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.search-add-group { display: flex; gap: 8px; } .search-add-group .form-input { flex: 1; }
.checkbox-list-container { border: 1px solid #e5e7eb; border-radius: 6px 6px 0 0; max-height: 140px; overflow-y: auto; padding: 4px; border-bottom: none;}
.checkbox-item { display: flex; align-items: center; gap: 8px; padding: 8px; cursor: pointer; border-radius: 4px; } .checkbox-item:hover { background: #f3f4f6; } .checkbox-item input { width: 16px; height: 16px; cursor: pointer; }
.mini-pagination { display: flex; align-items: center; justify-content: center; gap: 10px; background: #f8fafc; padding: 6px; border: 1px solid #e5e7eb; border-radius: 0 0 6px 6px;}
.mini-pagination button { border: 1px solid #d1d5db; background: #fff; width: 24px; height: 24px; border-radius: 4px; cursor: pointer; display: flex; align-items: center; justify-content: center; font-weight: bold; color: #475569;}
.mini-pagination button:hover:not(:disabled) { background: #e2e8f0; } .mini-pagination button:disabled { opacity: 0.4; cursor: not-allowed;} .page-info-text { font-size: 12px; font-weight: 600; color: #64748b;}
.flex-grow-1 { flex-grow: 1; }
.table-container { border: 1px solid #e5e7eb; border-radius: 6px; overflow: hidden; display: flex; flex-direction: column;}
.border-table-wrapper { box-shadow: 0 2px 4px rgba(0,0,0,0.02); } .max-h-150 { max-height: 180px; overflow-y: auto; } 
.border-table th { background: #f1f5f9; font-size: 12px; font-weight: 700; text-transform: uppercase; color: #1e293b; border-bottom: 1px solid #e2e8f0; padding: 12px; }
.border-table td { font-size: 13px; padding: 12px; border-bottom: 1px solid #f1f5f9;}
.btn-delete { background: #fee2e2; color: #ef4444; border: none; border-radius: 4px; padding: 6px 10px; cursor: pointer; font-size: 12px; font-weight: 600;} .btn-delete:hover { background: #fca5a5; }
.config-box { background: #ffffff; padding: 16px; border-radius: 8px; border: 1px solid #e2e8f0; } .custom-date-box { padding-top: 12px; border-top: 1px dashed #cbd5e1; }
.modal-actions { display: flex; justify-content: flex-end; gap: 12px; margin-top: auto;} .border-top { border-top: 1px solid #e2e8f0; } .pt-16 { padding-top: 16px; }
.badge-temp { background: #fef3c7; color: #d97706; padding: 2px 6px; border-radius: 4px; font-size: 10px; font-weight: bold; border: 1px solid #fde68a; }
.pagination-footer { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; border-top: 1px solid #e2e8f0; background: #fff;}
.page-info { font-size: 13px; font-weight: 500;}
.page-controls button { width: 30px; height: 30px; border: 1px solid #e2e8f0; background: #fff; border-radius: 4px; margin-left: 5px; cursor: pointer; font-weight: 600; color: #475569;}
.page-controls button:hover:not(:disabled) { background: #f1f5f9; } .page-controls button:disabled { opacity: 0.4; cursor: not-allowed;} .page-controls button.active { background: #0f172a; color: #fff; border-color: #0f172a; }

::-webkit-scrollbar { width: 6px; height: 6px; } ::-webkit-scrollbar-track { background: transparent; } ::-webkit-scrollbar-thumb { background-color: #cbd5e1; border-radius: 10px; } ::-webkit-scrollbar-thumb:hover { background-color: #94a3b8; }
</style>