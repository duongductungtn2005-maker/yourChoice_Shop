<template>
  <div class="order-detail-page" v-if="order">

    <div class="page-header">
      <div class="breadcrumb">
        <router-link to="/admin/orders">Quản lý đơn hàng</router-link> / <span>{{ order.maHoaDon }}</span>
      </div>
      <div class="header-actions">
        <button class="btn-secondary" @click="printOrder">
          <i class="fas fa-print"></i> In hóa đơn
        </button>
        <button class="btn-primary" @click="openEdit">
          <i class="fas fa-edit"></i> Cập nhật đơn
        </button>
      </div>
    </div>

    <div class="section-card timeline-card">
      <div class="timeline-container">
        <div class="timeline-line"></div>
        
        <div class="timeline-progress" :style="{ width: getProgressWidth(order.trangThai) + '%' }"></div>

        <div 
          v-for="(step, index) in timelineSteps" 
          :key="index" 
          class="step-item"
          :class="{ 'active': step.status <= order.trangThai }"
        >
          <div class="step-icon">
            <i :class="step.icon"></i>
          </div>
          
          <div class="step-content">
            <div class="step-label">{{ step.label }}</div>
            <div class="step-time" v-if="step.status <= order.trangThai">
               {{ formatDate(order.ngayTao) }}
            </div>
          </div>
        </div>
      </div>

      <div class="timeline-actions">
        <button v-if="order.trangThai === 1" class="btn-action confirm" @click="updateStatus(2)">Xác nhận đơn hàng</button>
        <button v-if="order.trangThai === 2" class="btn-action ship" @click="updateStatus(3)">Giao hàng</button>
        <button v-if="order.trangThai === 3" class="btn-action complete" @click="updateStatus(4)">Hoàn thành đơn</button>
        <button v-if="order.trangThai < 4 && order.trangThai > 0" class="btn-action cancel" @click="cancelOrder">Hủy đơn hàng</button>
      </div>
    </div>

    <div class="section-card">
      <h3 class="card-title">Thông tin chung</h3>
      <div class="general-info-grid">
        <div class="info-group">
          <span class="label">Mã đơn hàng</span>
          <span class="value link">{{ order.maHoaDon }}</span>
        </div>
        <div class="info-group">
          <span class="label">Ngày tạo</span>
          <span class="value value-ngay-tao">{{ formatDate(order.ngayTao) }}</span>
        </div>
        <div class="info-group">
          <span class="label">Trạng thái</span>
          <span class="badge" :class="statusInfo(order.trangThai).class">
            {{ statusInfo(order.trangThai).text }}
          </span>
        </div>
        <div class="info-group">
          <span class="label">Kênh bán hàng</span>
          <span class="badge" :class="orderTypeInfo(order.loaiHoaDon).class">
            {{ orderTypeInfo(order.loaiHoaDon).text }}
          </span>
        </div>

        <div>
          <strong>Trạng thái:</strong>
          <span class="status-badge" :class="statusInfo(order.trangThai).class">{{
            statusInfo(order.trangThai).text}}</span>
        </div>

        <div v-if="order.thongTinNhanHang">
          <strong>Người nhận:</strong> {{ order.thongTinNhanHang.tenNguoiNhan }}
        </div>

        <div v-if="order.thongTinNhanHang">
          <strong>SĐT:</strong> {{ order.thongTinNhanHang.sdt }}
        </div>

        <div v-if="order.thongTinNhanHang" style="grid-column: span 2">
          <strong>Địa chỉ:</strong> {{ order.thongTinNhanHang.diaChi }}
        </div>
      </div>

    <div class="section-card">
      <h3 class="card-title">Danh sách sản phẩm</h3>
      <div class="table-responsive">
        <table class="product-table">
          <thead>
            <tr>
              <th style="width: 50%">Sản phẩm</th>
              <th class="text-center" style="width: 15%">Số lượng</th>
              <th class="text-end" style="width: 15%">Đơn giá</th>
              <th class="text-end" style="width: 20%">Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(sp, i) in order.sanPhamHoaDon" :key="i">
              <td>
                <div class="product-cell">
                  <img :src="sp.anh || 'https://placehold.co/50x50/png?text=IMG'" alt="sp" class="product-img" />
                  <div class="product-desc">
                    <div class="p-name">{{ sp.tenSanPham }}</div>
                    <div class="p-attr">Màu: {{ sp.mauSac }} | Size: {{ sp.size }}</div>
                  </div>
                </div>
              </td>
              <td class="text-center">{{ sp.soLuong }}</td>
              <td class="text-end">{{ formatMoney(sp.donGia) }}</td>
              <td class="text-end fw-bold">{{ formatMoney(sp.thanhTien) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="order-summary-container">
  <div class="order-summary">
    <div class="summary-row">
      <span class="summary-label">Tổng tiền hàng:</span>
      <span class="summary-value">{{ formatMoney(order.tongTien) }}</span>
    </div>

    <div class="summary-row">
      <span class="summary-label">Phiếu giảm giá:</span>
      <span class="summary-value fw-bold">
        {{ order.maVoucher || '---' }} </span>
    </div>

    <div class="summary-row">
      <span class="summary-label">Giảm giá từ cửa hàng:</span>
      <span class="summary-value">0%</span> </div>

    <div class="summary-row">
      <span class="summary-label">Giảm giá:</span>
      <span class="summary-value discount">
        {{ order.giamGia > 0 ? '-' : '' }} {{ formatMoney(order.giamGia) }}
      </span>
    </div>

    <div class="summary-row">
      <span class="summary-label">Phí vận chuyển:</span>
      <span class="summary-value">{{ formatMoney(order.phiVanChuyen) }}</span>
    </div>
    <div class="summary-note">
      Miễn phí vận chuyển với đơn hàng có tổng tiền trên 1.000.000 VNĐ
    </div>

    <div class="summary-divider"></div>

    <div class="summary-row total-row">
      <span class="summary-label">Tổng tiền:</span>
      <span class="summary-value total-amount">
        {{ formatMoney(order.tongTienSauGiam) }}
      </span>
    </div>
  </div>
</div>
    </div>

    <div class="section-card">
  <h3 class="card-title">Lịch sử thanh toán</h3>
  <table class="payment-table">
  <thead>
    <tr>
      <th style="width: 20%">Thời gian</th>
      <th style="width: 15%; text-align: center;">Hình thức</th>
      <th style="width: 45%; padding-left: 20px;">Ghi chú</th> <th style="width: 20%; text-align: right;">Số tiền</th>
    </tr>
  </thead>
  <tbody>
    <tr v-for="(pay, i) in order.lichSuThanhToan" :key="i">
      <td style="color: #4b5563;">
        {{ formatDate(pay.ngayThanhToan) }}
      </td>
      
      <td style="text-align: center;">
        <span class="payment-badge" 
              :class="pay.hinhThucThanhToan === 'Tiền mặt' ? 'badge-cash' : 'badge-transfer'">
          {{ pay.hinhThucThanhToan }}
        </span>
      </td>

      <td style="color: #6b7280; padding-left: 20px;">
        Thanh toán đơn hàng
      </td>

      <td class="text-end fw-bold" style="color: #dc2626; font-size: 15px;">
        {{ formatMoney(pay.soTien) }}
      </td>
    </tr>

    <tr v-if="!order.lichSuThanhToan || order.lichSuThanhToan.length === 0">
      <td colspan="4" class="text-center empty-text">Chưa có giao dịch nào</td>
    </tr>
  </tbody>
</table>
</div>

    <!-- ===== Danh sách sản phẩm ===== -->
    <div class="card">
      <h3>Danh sách sản phẩm</h3>

      <div class="product" v-for="(sp, i) in order.sanPhamHoaDon" :key="i">
        <!-- <img src="https://via.placeholder.com/80" /> -->
        <div class="product-info">
          <strong>{{ sp.tenSanPham }}</strong>
          <div>Size: {{ sp.size }} | Màu: {{ sp.mauSac }}</div>
          <div>x{{ sp.soLuong }}</div>
        </div>
        <div class="price">{{ formatMoney(sp.thanhTien) }}</div>
      </div>
    </div>

    <!-- ===== Tổng tiền ===== -->
    <div class="card summary">
      <div>
        <div>Giảm giá: <strong>{{ formatMoney(order.giamGia) }}</strong></div>
        <div>Phí vận chuyển: <strong>{{ formatMoney(order.phiVanChuyen) }}</strong></div>
      </div>
      <div class="carrier-logo">
        <img src="https://upload.wikimedia.org/wikipedia/commons/thumb/e/e4/Giao_hang_nhanh_logo.png/800px-Giao_hang_nhanh_logo.png" alt="GHN" />
      </div>
    </div>

  </div>
  <!-- ===== Modal cập nhật ===== -->
<div v-if="showEditModal" class="modal-overlay">
  <div class="modal">
    <h3>Cập nhật thông tin đơn hàng</h3>

    <div class="form-group">
      <label>Khách hàng</label>
      <input v-model="editForm.tenKhachHang" />
    </div>

    <div class="form-group">
      <label>SĐT</label>
      <input v-model="editForm.sdt" />
    </div>

    <div class="form-group">
      <label>Loại hóa đơn</label>
      <select v-model="editForm.loaiHoaDon">
        <option value="Trực tuyến">Trực tuyến</option>
        <option value="Tại quầy">Tại quầy</option>
      </select>
    </div>

    <!-- Chỉ hiện khi Trực tuyến -->
    <template v-if="editForm.loaiHoaDon === 'Trực tuyến'">
      <div class="form-group">
        <label>Người nhận</label>
        <input v-model="editForm.tenNguoiNhan" />
      </div>

      <div class="form-group">
        <label>Địa chỉ</label>
        <input v-model="editForm.diaChi" />
      </div>
    </template>

    <div class="modal-actions">
      <button class="btn-outline" @click="showEditModal = false">
        Hủy
      </button>
      <button class="btn-primary" @click="saveEdit">
        Lưu
      </button>
    </div>
  </div>
</div>

</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchOrderDetail, updateOrderStatus, updateOrderInfo } from '@/api/ChiTietHoaDon'
const route = useRoute()
const router = useRouter()
const order = ref(null)
const statusTimes = ref({})

onMounted(async () => {
  try {
    const id = route.params.id
    const res = await fetchOrderDetail(id)
    order.value = res.data

    // Thời gian tạo đơn = trạng thái 1
    statusTimes.value[1] = order.value.ngayTao
  } catch (e) {
    alert('Không tìm thấy đơn hàng')
    router.push('/admin/orders')
  }
})

// --- HÀM QUAN TRỌNG: Tính độ dài thanh process ---
const getProgressWidth = (status) => {
    if (status <= 0) return 0; // Đã hủy
    if (status >= 4) return 100; // Hoàn thành
    // Chia đều 100% cho 3 khoảng (giữa 4 điểm) -> 33.33% mỗi khoảng
    return (status - 1) * 33.33; 
}

const updateStatus = async (newStatus) => {
    if(!confirm('Xác nhận chuyển trạng thái đơn hàng?')) return;
    
    try {
        // 1. Gọi API lên Server
        await updateOrderStatus(order.value.maHoaDon, newStatus);
        
        // 2. Nếu không lỗi -> Cập nhật giao diện
        order.value.trangThai = newStatus;
        
        // 3. (Tùy chọn) Reload lại dữ liệu để đảm bảo đồng bộ 100%
        // const res = await fetchOrderDetail(order.value.maHoaDon);
        // order.value = res.data;

        alert("Cập nhật trạng thái thành công!");
    } catch (e) {
        console.error(e);
        alert("Lỗi cập nhật: " + (e.response?.data || "Lỗi hệ thống"));
    }
}

const cancelOrder = () => {
  if (confirm('Bạn có chắc chắn muốn hủy đơn hàng này không?')) {
    order.value.trangThai = 0
    statusTimes.value[0] = now()
  }
}


const STATUS_CONFIG = {
  0: { text: 'Đã hủy', class: 'status-cancel' },
  1: { text: 'Chờ xác nhận', class: 'status-pending' },
  2: { text: 'Chờ giao hàng', class: 'status-wait-ship' },
  3: { text: 'Đang vận chuyển', class: 'status-shipping' },
  4: { text: 'Hoàn thành', class: 'status-done' }
}

const statusInfo = (status) => STATUS_CONFIG[status] || {
  text: 'Không xác định',
  class: 'status-unknown'
}

const ORDER_TYPE_CONFIG = {
  'Trực tuyến': {
    text: 'Trực tuyến',
    class: 'type-online'
  },
  'Tại quầy': {
    text: 'Tại quầy',
    class: 'type-offline'
  }
}

const orderTypeInfo = (type) =>
  ORDER_TYPE_CONFIG[type] || {
    text: type || 'Không xác định',
    class: 'type-unknown'
  }

const Loai_Thanh_Toan = {
  'Tiền mặt': {
    text: 'Tiền mặt',
    class: 'type-offline'
  },
  'Chuyển khoản': {
    text: 'Chuyển khoản',
    class: 'type-online'
  }
}

const LoaiThanhToanInfo = (type) =>
  Loai_Thanh_Toan[type] || {
    text: type || 'Không xác định',
    class: 'type-unknown'
  }

onMounted(async () => {
  try {
    const id = route.params.id
    const res = await fetchOrderDetail(id)
    order.value = res.data
  } catch (e) {
    alert('Không tìm thấy đơn hàng')
    router.push('/admin/orders')
  }
})

const timeline = computed(() => {
  if (!order.value) return []

  return [
    {
      label: 'Chờ xác nhận',
      time: statusTimes.value[1] || '',
      active: order.value.trangThai >= 1
    },
    {
      label: 'Chờ giao hàng',
      time: statusTimes.value[2] || '',
      active: order.value.trangThai >= 2
    },
    {
      label: 'Đang vận chuyển',
      time: statusTimes.value[3] || '',
      active: order.value.trangThai >= 3
    },
    {
      label: 'Hoàn thành',
      time: statusTimes.value[4] || '',
      active: order.value.trangThai >= 4
    }
  ]
})

const showEditModal = ref(false)

const editForm = ref({
  tenKhachHang: '',
  sdt: '',
  loaiHoaDon: '',
  tenNguoiNhan: '',
  diaChi: ''
})

const openEdit = () => {
  editForm.value = {
    tenKhachHang: order.value.tenKhachHang,
    sdt: order.value.thongTinNhanHang?.sdt || '',
    loaiHoaDon: order.value.loaiHoaDon,
    tenNguoiNhan: order.value.thongTinNhanHang?.tenNguoiNhan || '',
    diaChi: order.value.thongTinNhanHang?.diaChi || ''
  }
  showEditModal.value = true
}

const saveEdit = () => {
  order.value.tenKhachHang = editForm.value.tenKhachHang
  order.value.loaiHoaDon = editForm.value.loaiHoaDon

  if (editForm.value.loaiHoaDon === 'Trực tuyến') {
    order.value.thongTinNhanHang = {
      tenNguoiNhan: editForm.value.tenNguoiNhan,
      sdt: editForm.value.sdt,
      diaChi: editForm.value.diaChi
    }
  } else {
    order.value.thongTinNhanHang = null
  }

  showEditModal.value = false

  // TODO: gọi API update
  // await updateOrder(order.value.maHoaDon, editForm.value)
}


const formatMoney = (v) =>
  Number(v || 0).toLocaleString('vi-VN') + ' đ'
</script>

<style scoped>
/* --- CSS CHO MÀN HÌNH BÌNH THƯỜNG (Ẩn mẫu in đi) --- */
/* --- CSS MẶC ĐỊNH TRÊN WEB (Ẩn mẫu in đi) --- */
#invoice-print-area {
  display: none;
}

/* --- CSS KHI BẤM CTRL + P (CHẾ ĐỘ IN ẤN) --- */
@media print {
  /* 1. Ẩn toàn bộ trang web (bao gồm menu, nút bấm...) */
  body > * {
    display: none !important;
  }

  /* 2. Chỉ hiện khung hóa đơn và định vị tràn màn hình */
  #invoice-print-area {
    display: block !important;
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: white;
    z-index: 999999; /* Đè lên tất cả */
    padding: 20px;
  }

  /* --- STYLE HÓA ĐƠN --- */
  .invoice-box {
    width: 100%;
    max-width: 700px; /* Khổ giấy A5 đẹp nhất */
    margin: 0 auto;
    border: 2px solid #000;
    font-family: 'Times New Roman', serif;
    color: #000;
  }

  /* Layout Flexbox */
  .header-row, .info-row, .content-row, .footer-row {
    display: flex;
    width: 100%;
  }

  /* Header */
  .header-row {
    justify-content: space-between;
    align-items: center;
    padding: 15px;
  }
  .shop-name { font-size: 24px; font-weight: bold; margin: 0; text-transform: uppercase; }
  .carrier-logo img { height: 40px; }

  /* Các đường kẻ */
  .border-top { border-top: 1px solid #000; }
  .border-right { border-right: 1px solid #000; }
  .padding-10 { padding: 10px; }

  /* Chia cột */
  .col-half { width: 50%; }
  .col-qr { width: 30%; }
  .col-products { width: 70%; }
  .center-flex { display: flex; flex-direction: column; align-items: center; justify-content: center; }

  /* Typography (Phông chữ) */
  .section-title { font-weight: bold; font-size: 14px; text-transform: uppercase; margin-bottom: 5px; }
  .content-text { font-size: 15px; line-height: 1.4; }
  
  /* Mã QR */
  .qr-img { width: 80px; height: 80px; }
  .qr-text { font-size: 12px; font-weight: bold; margin-top: 5px; }

  /* Danh sách hàng */
  .product-list-print { list-style: none; padding: 0; margin: 0; font-size: 14px; }
  .product-list-print li { margin-bottom: 8px; border-bottom: 1px dashed #ccc; padding-bottom: 4px; }
  .product-attr { font-size: 13px; font-style: italic; }

  /* Tiền thu */
  .cod-price { font-size: 26px; font-weight: bold; margin: 10px 0; }
  .note-text { font-size: 12px; font-style: italic; }

  /* Chữ ký */
  .signature-space { height: 100px; }
  .text-center { text-align: center; }
  
  .print-footer { text-align: center; font-size: 11px; padding: 5px; border-top: 1px solid #000; }
}
#print-area {
  display: none;
}

/* --- CSS KHI BẤM CTRL + P (Chế độ in) --- */
@media print {
  /* 1. Ẩn tất cả mọi thứ của trang web */
  body * {
    visibility: hidden;
  }

  /* 2. Chỉ hiện vùng in (Print Area) */
  #print-area, #print-area * {
    visibility: visible;
  }

  /* 3. Căn chỉnh vùng in ra giữa tờ giấy */
  #print-area {
    display: block;
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    padding: 20px;
    background: white;
  }

  /* --- STYLE HÓA ĐƠN (Giống hình mẫu) --- */
  .invoice-container {
    border: 2px solid #000;
    width: 100%;
    max-width: 800px; /* Kích thước A5/A4 */
    margin: 0 auto;
    font-family: 'Times New Roman', Times, serif; /* Font chữ in ấn chuẩn */
    color: #000;
  }

  .invoice-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 20px;
    border-bottom: 2px solid #000;
  }

  .invoice-row {
    display: flex;
    width: 100%;
  }

  .col-half { width: 50%; }
  .col-qr { width: 30%; display: flex; flex-direction: column; align-items: center; justify-content: center; }
  .col-content { width: 70%; }

  .border-bottom { border-bottom: 1px solid #000; }
  .border-right { border-right: 1px solid #000; }
  .padding-10 { padding: 10px; }
  
  .product-list {
    list-style: none;
    padding-left: 5px;
    margin: 5px 0;
    font-size: 14px;
  }

  .cod-amount {
    font-size: 24px;
    font-weight: bold;
    margin-top: 10px;
  }

  .signature-box {
    height: 100px; /* Khoảng trống để ký */
  }
  
  .text-center { text-align: center; }
  
  .invoice-footer {
    text-align: center;
    font-style: italic;
    padding: 5px;
    font-size: 12px;
    border-top: 1px solid #000;
  }
}
/* --- Layout Chính --- */
.order-detail-page {
  padding: 20px;
  background-color: #f3f4f6;
  min-height: 100vh;
  font-family: 'Segoe UI', sans-serif;
  color: #1f2937;
  max-width: 1200px;
  margin: 0 auto;
}

/* Header */
.page-header {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;
}
.breadcrumb { font-size: 14px; color: #6b7280; }
.breadcrumb a { text-decoration: none; color: #6b7280; }
.breadcrumb span { font-weight: bold; color: #111827; }
.header-actions { display: flex; gap: 10px; }

/* --- Section Card --- */
.section-card {
  background: white; border-radius: 8px; padding: 25px; margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05); width: 100%;
}
.card-title {
  font-size: 16px; font-weight: 700; margin-bottom: 20px; color: #111827;
  border-bottom: 1px solid #f3f4f6; padding-bottom: 10px;
}

/* --- TIMELINE CSS (MỚI) --- */
.timeline-card { padding: 40px 20px; overflow: hidden; }
.timeline-container {
  display: flex; justify-content: space-between; position: relative;
  margin-bottom: 30px; padding: 0 20px;
}
/* Kẻ xám nền */
.timeline-line {
  position: absolute; top: 25px; left: 50px; right: 50px; height: 4px;
  background: #e2e8f0; z-index: 0; border-radius: 4px;
}
/* Kẻ xanh (Tiến độ) */
.timeline-progress {
  position: absolute; top: 25px; left: 50px; height: 4px;
  background: #10b981; z-index: 0; transition: width 0.5s ease;
  border-radius: 4px;
}
.step-item {
  position: relative; z-index: 1; text-align: center; width: 120px;
}
/* Icon Tròn */
.step-icon {
  width: 54px; height: 54px; border-radius: 50%; background: white;
  border: 4px solid #e2e8f0; color: #94a3b8;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; margin: 0 auto 10px; transition: all 0.3s ease;
}
/* Trạng thái Active */
.step-item.active .step-icon {
  border-color: #10b981; background: #10b981; color: white;
  box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.2); transform: scale(1.1);
}
.step-label {
  font-size: 14px; font-weight: 700; color: #64748b; margin-bottom: 4px;
}
.step-item.active .step-label { color: #10b981; }
.step-time { font-size: 12px; color: #000; font-weight: 500; }

.timeline-actions { display: flex; justify-content: center; gap: 10px; padding-top: 20px; border-top: 1px solid #f3f4f6; }

/* --- Thông tin chung --- */
.general-info-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.info-group { display: flex; flex-direction: column; gap: 5px; }
.info-group .label { font-size: 13px; color: #6b7280; font-weight: 500; }
.info-group .value { font-size: 15px; color: #111827; font-weight: 400; }
.value-ngay-tao { color: #000; }
.info-group .link { color: #2563eb; cursor: pointer; }

/* --- Khách hàng --- */
.customer-layout { display: flex; gap: 40px; align-items: flex-start; }
.customer-info { display: flex; align-items: center; gap: 15px; flex: 1; }
.avatar-circle { width: 56px; height: 56px; background: #eff6ff; color: #2563eb; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; font-weight: bold; }
.customer-details .name { font-weight: 600; font-size: 16px; margin-bottom: 4px; }
.text-muted { color: #6b7280; font-size: 14px; }
.shipping-info { flex: 2; border-left: 1px solid #e5e7eb; padding-left: 40px; }
.sub-title { font-size: 14px; font-weight: 600; color: #374151; margin-bottom: 10px; text-transform: uppercase; }
.ship-row { font-size: 14px; color: #4b5563; margin-bottom: 6px; display: flex; align-items: center; gap: 8px; }
.ship-row i { color: #9ca3af; width: 16px; }

/* --- Bảng Sản phẩm --- */
.product-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.product-table th { text-align: left; color: #1E3A8A; font-weight: 700; padding: 12px 0; border-bottom: 1px solid #e5e7eb; background: #E9F1FB; }
.product-table td { padding: 15px 0; border-bottom: 1px solid #f3f4f6; vertical-align: middle; }
.product-cell { display: flex; gap: 15px; align-items: center; }
.product-img { width: 56px; height: 56px; object-fit: cover; border-radius: 6px; border: 1px solid #e5e7eb; }
.p-name { font-weight: 600; color: #111827; font-size: 15px; }
.p-attr { font-size: 13px; color: #6b7280; margin-top: 2px; }

.order-summary-container {
  display: flex;
  justify-content: flex-end; /* Đẩy sang phải */
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f3f4f6;
}
.order-summary {
  width: 400px; /* Độ rộng vừa phải giống mẫu */
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #374151;
}
.summary-divider {
  height: 1px;
  background: #e5e7eb; /* Màu xám nhạt */
  margin: 10px 0;
}
.total-row {
  font-size: 16px;
  margin-top: 5px;
}
.total-amount {
  color: #dc2626; /* Màu đỏ đậm */
  font-weight: 800;
  font-size: 18px;
}
.summary-label {
  color: #6b7280;
  font-weight: 500;
}
.summary-note {
  font-size: 12px;
  color: #9ca3af;
  text-align: right;
  font-style: italic;
  margin-top: -5px; /* Kéo gần lên trên */
}
.summary-value {
  font-weight: 600;
  color: #111827;
}
.summary-value.fw-bold { font-weight: 500; }
.summary-value.discount { color: #dc2626; } /* Màu đỏ cho số tiền giảm */
/* --- Thanh toán --- */
.payment-table th { 
  text-align: left; 
  color: #1E3A8A; 
  padding: 12px 0; 
  border-bottom: 1px solid #e5e7eb; 
  font-weight: 700;
  font-size: 14px;
  background: #E9F1FB;
}
.payment-table td { 
  padding: 16px 0; 
  border-bottom: 1px solid #f3f4f6; 
  vertical-align: middle;
  font-size: 14px;
}

.badge.green {
  background: #dcfce7;
  color: #166534;
}

.badge.yellow {
  background: #fef9c3;
  color: #854d0e;
}

.btn-primary {
  background: #2563eb;
  color: #fff;
  border: none;
  padding: 6px 12px;
  border-radius: 8px;
}

.btn-outline {
  border: 1px solid #e2e8f0;
  background: #fff;
  padding: 6px 12px;
  border-radius: 8px;
}

.btn-danger {
  background: #dc2626;
  color: #fff;
  border: none;
  padding: 6px 12px;
  border-radius: 8px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  width: 420px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.form-group input,
.form-group select {
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

</style>