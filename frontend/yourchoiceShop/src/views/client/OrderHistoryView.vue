<template>
  <div class="order-history-page">
    <div class="page-header-small">
      <div class="header-overlay"></div>
      <div class="container header-content">
        <h1>ĐƠN HÀNG CỦA TÔI</h1>
        <div class="breadcrumb">
          <span @click="$router.push('/')">Trang chủ</span> / <span>Đơn hàng</span>
        </div>
      </div>
    </div>

    <div class="container">
      <!-- Tabs trạng thái -->
      <div class="status-tabs">
        <button 
          v-for="tab in statusTabs" :key="tab.value"
          :class="{ active: activeStatus === tab.value }"
          @click="changeStatus(tab.value)"
        >
          {{ tab.label }}
          <span class="tab-count" v-if="tab.count > 0">{{ tab.count }}</span>
        </button>
      </div>

      <!-- Tìm kiếm -->
      <div class="search-bar">
        <input type="text" v-model="searchKeyword" placeholder="Tìm theo mã đơn hàng..." @keyup.enter="fetchOrders" />
        <button @click="fetchOrders"><i class="fas fa-search"></i></button>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
      </div>

      <!-- Danh sách đơn hàng -->
      <div v-else-if="orders.length > 0" class="order-list">
        <div class="order-card" v-for="order in orders" :key="order.id">
          <div class="order-header">
            <div class="order-code">
              <span class="label">Mã đơn:</span>
              <strong>{{ order.maHoaDon }}</strong>
            </div>
            <div class="order-status" :class="getStatusClass(order.trangThai)">
              {{ getStatusLabel(order.trangThai) }}
            </div>
          </div>
          
          <div class="order-body" @click="viewDetail(order.maHoaDon)">
            <div class="order-info-row">
              <span><i class="far fa-calendar"></i> {{ order.ngayTao }}</span>
              <span><i class="fas fa-box"></i> {{ order.tongSanPham || 0 }} sản phẩm</span>
              <span class="order-total"><i class="fas fa-tag"></i> {{ formatMoney(order.tongTienSauGiam) }}</span>
            </div>
            <div class="order-customer">
              <span v-if="order.tenKhachHang"><i class="fas fa-user"></i> {{ order.tenKhachHang }}</span>
              <span v-if="order.sdtKhachHang"><i class="fas fa-phone"></i> {{ order.sdtKhachHang }}</span>
            </div>
          </div>

          <div class="order-footer">
            <button class="btn-detail" @click="viewDetail(order.maHoaDon)">
              <i class="fas fa-eye"></i> Xem chi tiết
            </button>
            <button 
              v-if="order.trangThai === 1" 
              class="btn-cancel"
              @click="cancelOrder(order.maHoaDon)"
            >
              <i class="fas fa-times-circle"></i> Hủy đơn
            </button>
          </div>
        </div>
      </div>

      <!-- Empty -->
      <div v-else class="empty-state">
        <i class="fas fa-clipboard-list"></i>
        <h3>Không có đơn hàng</h3>
        <p>Bạn chưa có đơn hàng nào{{ activeStatus !== null ? ' ở trạng thái này' : '' }}.</p>
        <button class="btn-shop" @click="$router.push('/products')">MUA SẮM NGAY</button>
      </div>

      <!-- Pagination -->
      <div class="pagination" v-if="totalPages > 1">
        <button :disabled="page === 0" @click="changePage(page - 1)">‹</button>
        <span>Trang {{ page + 1 }} / {{ totalPages }}</span>
        <button :disabled="page === totalPages - 1" @click="changePage(page + 1)">›</button>
      </div>

      <!-- Order Detail Modal -->
      <div v-if="showDetail" class="modal-overlay" @click.self="showDetail = false">
        <div class="modal-content">
          <div class="modal-header">
            <h3>Chi tiết đơn hàng #{{ orderDetail.maHoaDon }}</h3>
            <button class="btn-close-modal" @click="showDetail = false"><i class="fas fa-times"></i></button>
          </div>
          
          <div class="modal-body" v-if="orderDetail">
            <!-- Trạng thái timeline -->
            <div class="status-timeline">
              <div class="timeline-step" v-for="step in timelineSteps" :key="step.status"
                :class="{ active: orderDetail.trangThai >= step.status, current: orderDetail.trangThai === step.status }">
                <div class="step-icon"><i :class="step.icon"></i></div>
                <span>{{ step.label }}</span>
              </div>
            </div>

            <!-- Thông tin nhận hàng -->
            <div class="detail-section">
              <h4><i class="fas fa-map-marker-alt"></i> Thông tin nhận hàng</h4>
              <p><strong>{{ orderDetail.thongTinNhanHang?.tenNguoiNhan }}</strong></p>
              <p>{{ orderDetail.thongTinNhanHang?.soDienThoai }}</p>
              <p>{{ orderDetail.thongTinNhanHang?.diaChi }}</p>
            </div>

            <!-- Sản phẩm -->
            <div class="detail-section">
              <h4><i class="fas fa-shopping-bag"></i> Sản phẩm</h4>
              <div class="detail-product" v-for="sp in orderDetail.sanPhams" :key="sp.idHoaDonChiTiet">
                <img :src="sp.duongDanAnh || 'https://placehold.co/60x75?text=N/A'" alt="" @error="(e) => e.target.src = 'https://placehold.co/60x75?text=N/A'" />
                <div class="dp-info">
                  <h5>{{ sp.tenSanPham }}</h5>
                  <span>{{ sp.mauSac }} / {{ sp.kichThuoc }} x {{ sp.soLuong }}</span>
                </div>
                <span class="dp-price">{{ formatMoney(sp.thanhTien) }}</span>
              </div>
            </div>

            <!-- Thanh toán -->
            <div class="detail-section payment-summary">
              <div class="ps-row"><span>Tạm tính</span><span>{{ formatMoney(orderDetail.tongTien) }}</span></div>
              <div class="ps-row" v-if="orderDetail.phiVanChuyen"><span>Phí vận chuyển</span><span>{{ formatMoney(orderDetail.phiVanChuyen) }}</span></div>
              <div class="ps-row" v-if="orderDetail.tienGiamGia"><span>Giảm giá</span><span class="discount">-{{ formatMoney(orderDetail.tienGiamGia) }}</span></div>
              <div class="ps-row total"><span>Tổng cộng</span><span>{{ formatMoney(orderDetail.tongTienSauGiam) }}</span></div>
            </div>

            <!-- Lịch sử -->
            <div class="detail-section" v-if="orderDetail.lichSuHoaDon?.length">
              <h4><i class="fas fa-history"></i> Lịch sử đơn hàng</h4>
              <div class="history-list">
                <div class="history-item" v-for="(h, idx) in orderDetail.lichSuHoaDon" :key="idx">
                  <span class="h-time">{{ h.thoiGian }}</span>
                  <span class="h-action">{{ h.hanhDong }}</span>
                  <span class="h-note" v-if="h.ghiChu">{{ h.ghiChu }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getOrders, getOrderDetail, updateOrderStatus } from '@/api/clientApi'
import { getCurrentUserId, isAuthenticated } from '@/services/auth'
import Swal from 'sweetalert2'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const orders = ref([])
const page = ref(0)
const totalPages = ref(1)
const activeStatus = ref(null) // null = tất cả
const searchKeyword = ref('')
const showDetail = ref(false)
const orderDetail = ref(null)

const statusTabs = [
  { label: 'Tất cả', value: null, count: 0 },
  { label: 'Chờ xác nhận', value: 1, count: 0 },
  { label: 'Đã xác nhận', value: 2, count: 0 },
  { label: 'Đang vận chuyển', value: 3, count: 0 },
  { label: 'Hoàn thành', value: 4, count: 0 },
  { label: 'Đã hủy', value: 0, count: 0 },
]

const timelineSteps = [
  { status: 1, label: 'Chờ xác nhận', icon: 'fas fa-clock' },
  { status: 2, label: 'Đã xác nhận', icon: 'fas fa-box' },
  { status: 3, label: 'Đang giao', icon: 'fas fa-truck' },
  { status: 4, label: 'Hoàn thành', icon: 'fas fa-check-circle' },
]

onMounted(() => {
  if (!isAuthenticated()) {
    router.push('/client/login')
    return
  }

  const currentUserId = getCurrentUserId()
  const routeCustomerId = Number(route.params.id)
  if (!currentUserId) {
    router.push('/client/login')
    return
  }
  if (Number.isNaN(routeCustomerId) || routeCustomerId !== currentUserId) {
    router.replace(`/customer/${currentUserId}/orders`)
    return
  }

  fetchOrders()
})

const fetchOrders = async () => {
  loading.value = true
  try {
    const currentUserId = getCurrentUserId()
    const params = {
      page: page.value + 1,
      size: 10,
      keyword: searchKeyword.value || undefined,
      status: activeStatus.value !== null ? activeStatus.value : undefined,
      khachHangId: currentUserId || undefined,
    }
    const res = await getOrders(params)
    orders.value = res.data.content || []
    totalPages.value = res.data.totalPages || 1
  } catch (e) {
    console.error('Lỗi tải đơn hàng:', e)
    orders.value = []
  } finally {
    loading.value = false
  }
}

const viewDetail = async (maHoaDon) => {
  try {
    const res = await getOrderDetail(maHoaDon)
    orderDetail.value = res.data
    showDetail.value = true
  } catch (e) {
    console.error('Lỗi tải chi tiết:', e)
    Swal.fire('Lỗi', 'Không thể tải chi tiết đơn hàng', 'error')
  }
}

const cancelOrder = async (maHoaDon) => {
  const result = await Swal.fire({
    title: 'Hủy đơn hàng?',
    text: 'Bạn có chắc muốn hủy đơn hàng này?',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#ef4444',
    confirmButtonText: 'Hủy đơn',
    cancelButtonText: 'Không',
  })
  if (!result.isConfirmed) return

  try {
    await updateOrderStatus(maHoaDon, 0)
    Swal.fire({ icon: 'success', title: 'Đã hủy đơn hàng', timer: 1500, showConfirmButton: false })
    fetchOrders()
  } catch (e) {
    Swal.fire('Lỗi', e.response?.data?.message || 'Không thể hủy đơn hàng', 'error')
  }
}

const changeStatus = (status) => {
  activeStatus.value = status
  page.value = 0
  fetchOrders()
}

const changePage = (p) => {
  if (p >= 0 && p < totalPages.value) { page.value = p; fetchOrders() }
}

const getStatusLabel = (status) => {
  const map = { 0: 'Đã hủy', 1: 'Chờ xác nhận', 2: 'Đã xác nhận', 3: 'Đang vận chuyển', 4: 'Hoàn thành' }
  return map[status] || 'Không xác định'
}

const getStatusClass = (status) => {
  const map = { 0: 'status-cancelled', 1: 'status-pending', 2: 'status-preparing', 3: 'status-shipping', 4: 'status-completed' }
  return map[status] || ''
}

const formatMoney = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0)
</script>

<style scoped>
.order-history-page { font-family: Arial, sans-serif; color: #333; }
.container { max-width: 1000px; margin: 0 auto; padding: 0 20px; box-sizing: border-box; }

.page-header-small {
  background-image: url('https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=1600&q=80');
  background-size: cover; background-position: center; height: 180px; position: relative;
  display: flex; align-items: center; justify-content: center; margin-bottom: 40px;
}
.header-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.5); }
.header-content { position: relative; z-index: 1; text-align: center; color: #fff; }
.header-content h1 { font-size: 32px; font-weight: 700; margin-bottom: 8px; }
.breadcrumb { font-size: 14px; color: #e2e8f0; }
.breadcrumb span { cursor: pointer; }

/* Status tabs */
.status-tabs { display: flex; gap: 0; border-bottom: 2px solid #f1f5f9; margin-bottom: 25px; overflow-x: auto; }
.status-tabs button { padding: 12px 20px; border: none; background: none; font-size: 14px; font-weight: 600; color: #64748b; cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: 0.2s; white-space: nowrap; position: relative; }
.status-tabs button.active { color: #1e3a8a; border-bottom-color: #1e3a8a; }
.status-tabs button:hover { color: #1e3a8a; }
.tab-count { background: #ef4444; color: #fff; font-size: 11px; padding: 1px 6px; border-radius: 10px; margin-left: 5px; }

/* Search */
.search-bar { display: flex; gap: 10px; margin-bottom: 25px; }
.search-bar input { flex: 1; padding: 11px 16px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; outline: none; }
.search-bar input:focus { border-color: #1e3a8a; }
.search-bar button { padding: 11px 18px; background: #0f172a; color: #fff; border: none; border-radius: 8px; cursor: pointer; }

/* Loading */
.loading-state { text-align: center; padding: 60px; }
.spinner { border: 4px solid #f1f5f9; border-top: 4px solid #1e3a8a; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin: 0 auto; }
@keyframes spin { to { transform: rotate(360deg); } }

/* Order cards */
.order-list { display: flex; flex-direction: column; gap: 15px; margin-bottom: 40px; }
.order-card { border: 1px solid #f1f5f9; border-radius: 12px; overflow: hidden; transition: 0.2s; }
.order-card:hover { box-shadow: 0 4px 15px rgba(0,0,0,0.06); }

.order-header { display: flex; justify-content: space-between; align-items: center; padding: 15px 20px; background: #f8fafc; border-bottom: 1px solid #f1f5f9; }
.order-code .label { font-size: 13px; color: #94a3b8; margin-right: 5px; }
.order-code strong { color: #0f172a; font-size: 15px; }
.order-status { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 700; }
.status-pending { background: #fef3c7; color: #92400e; }
.status-preparing { background: #dbeafe; color: #1e40af; }
.status-shipping { background: #e0e7ff; color: #3730a3; }
.status-completed { background: #d1fae5; color: #065f46; }
.status-cancelled { background: #fee2e2; color: #991b1b; }
.status-payment { background: #fef9c3; color: #854d0e; }

.order-body { padding: 15px 20px; cursor: pointer; }
.order-info-row { display: flex; gap: 25px; font-size: 14px; color: #475569; margin-bottom: 8px; }
.order-info-row i { margin-right: 5px; color: #94a3b8; }
.order-total { font-weight: 700; color: #0f172a; }
.order-customer { font-size: 13px; color: #94a3b8; display: flex; gap: 20px; }
.order-customer i { margin-right: 4px; }

.order-footer { display: flex; gap: 10px; padding: 12px 20px; border-top: 1px solid #f1f5f9; }
.btn-detail { padding: 8px 16px; border: 1px solid #1e3a8a; color: #1e3a8a; background: #fff; font-size: 13px; font-weight: 600; border-radius: 6px; cursor: pointer; transition: 0.2s; }
.btn-detail:hover { background: #eff6ff; }
.btn-cancel { padding: 8px 16px; border: 1px solid #ef4444; color: #ef4444; background: #fff; font-size: 13px; font-weight: 600; border-radius: 6px; cursor: pointer; transition: 0.2s; }
.btn-cancel:hover { background: #fef2f2; }

/* Empty */
.empty-state { text-align: center; padding: 60px 20px; }
.empty-state i { font-size: 50px; color: #cbd5e1; margin-bottom: 15px; }
.empty-state h3 { color: #0f172a; margin-bottom: 8px; }
.empty-state p { color: #64748b; margin-bottom: 25px; }
.btn-shop { padding: 12px 35px; background: #0f172a; color: #fff; border: none; font-weight: 700; border-radius: 6px; cursor: pointer; }

/* Pagination */
.pagination { display: flex; justify-content: center; align-items: center; gap: 15px; margin: 30px 0 60px; }
.pagination button { padding: 8px 16px; border: 1px solid #cbd5e1; background: #fff; border-radius: 6px; cursor: pointer; font-weight: 600; }
.pagination button:hover:not(:disabled) { border-color: #1e3a8a; color: #1e3a8a; }
.pagination button:disabled { opacity: 0.4; cursor: not-allowed; }
.pagination span { font-size: 14px; color: #64748b; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 2000; display: flex; align-items: center; justify-content: center; padding: 20px; }
.modal-content { background: #fff; border-radius: 16px; max-width: 700px; width: 100%; max-height: 90vh; overflow-y: auto; }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 25px; border-bottom: 1px solid #f1f5f9; position: sticky; top: 0; background: #fff; z-index: 1; border-radius: 16px 16px 0 0; }
.modal-header h3 { font-size: 18px; color: #0f172a; margin: 0; }
.btn-close-modal { background: none; border: none; font-size: 20px; color: #94a3b8; cursor: pointer; }
.modal-body { padding: 25px; }

/* Timeline */
.status-timeline { display: flex; justify-content: space-between; margin-bottom: 30px; position: relative; }
.status-timeline::before { content: ''; position: absolute; top: 18px; left: 25px; right: 25px; height: 2px; background: #e2e8f0; z-index: 0; }
.timeline-step { display: flex; flex-direction: column; align-items: center; gap: 8px; z-index: 1; flex: 1; }
.step-icon { width: 36px; height: 36px; border-radius: 50%; display: flex; align-items: center; justify-content: center; background: #e2e8f0; color: #94a3b8; font-size: 14px; transition: 0.3s; }
.timeline-step.active .step-icon { background: #1e3a8a; color: #fff; }
.timeline-step.current .step-icon { box-shadow: 0 0 0 4px rgba(30, 58, 138, 0.2); }
.timeline-step span { font-size: 12px; color: #94a3b8; font-weight: 600; text-align: center; }
.timeline-step.active span { color: #1e3a8a; }

/* Detail sections */
.detail-section { margin-bottom: 25px; }
.detail-section h4 { font-size: 15px; color: #0f172a; margin-bottom: 12px; display: flex; align-items: center; gap: 8px; }
.detail-section h4 i { color: #1e3a8a; }
.detail-section p { margin: 4px 0; font-size: 14px; color: #475569; }

.detail-product { display: flex; gap: 12px; align-items: center; padding: 10px 0; border-bottom: 1px solid #f8fafc; }
.detail-product img { width: 55px; height: 68px; object-fit: cover; border-radius: 6px; border: 1px solid #f1f5f9; }
.dp-info { flex: 1; }
.dp-info h5 { font-size: 14px; margin: 0 0 4px; color: #334155; }
.dp-info span { font-size: 12px; color: #94a3b8; }
.dp-price { font-weight: 700; font-size: 14px; color: #0f172a; }

.payment-summary { background: #f8fafc; border-radius: 8px; padding: 16px; }
.ps-row { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; color: #475569; }
.ps-row.total { font-weight: 700; font-size: 16px; color: #0f172a; border-top: 1px solid #e2e8f0; padding-top: 10px; margin-top: 5px; }
.ps-row .discount { color: #059669; }

.history-list { display: flex; flex-direction: column; gap: 10px; }
.history-item { display: flex; gap: 15px; font-size: 13px; padding: 8px 0; border-bottom: 1px solid #f8fafc; }
.h-time { color: #94a3b8; min-width: 130px; }
.h-action { color: #334155; font-weight: 600; }
.h-note { color: #64748b; font-style: italic; }

@media (max-width: 768px) {
  .status-tabs { gap: 0; }
  .status-tabs button { padding: 10px 14px; font-size: 13px; }
  .order-info-row { flex-wrap: wrap; gap: 10px; }
}
</style>
