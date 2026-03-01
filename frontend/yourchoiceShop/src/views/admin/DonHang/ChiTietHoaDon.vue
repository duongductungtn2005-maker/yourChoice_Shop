<template>
  <div class="page-container">
    <div class="header-section">
      <div class="header-left">
        <h1 class="page-title">Chi tiết hóa đơn</h1>
        <div class="sub-info" v-if="!loading && order">
          <span>Mã đơn hàng: <strong class="text-primary">{{ order.maHoaDon }}</strong></span>
          <span class="divider">|</span>
          <span class="text-gray">Ngày tạo: {{ formatDate(order.ngayTao) }}</span>
        </div>
      </div>
      <div class="header-actions">
        <button class="btn btn-outline" @click="$router.push('/admin/orders')">
          <font-awesome-icon :icon="['fas', 'arrow-left']" /> Quay lại danh sách
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      <font-awesome-icon :icon="['fas', 'spinner']" spin size="2x" />
      <p>Đang tải thông tin đơn hàng...</p>
    </div>

    <div v-else-if="order" class="detail-grid">

      <div class="col-main">

        <div class="card status-card">
          <div class="card-header-icon">
            <i class="fas fa-truck-fast"></i> <span>Trạng thái đơn hàng</span>
          </div>
          <div class="timeline-wrapper">
            <div class="steps-container">
              <div v-for="(step, index) in visibleSteps" :key="index" class="step-item active">
                <div class="step-icon">
                  <i :class="step.icon"></i>
                </div>
                <div class="step-label">{{ step.label }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="info-row">
          <div class="card info-card">
            <div class="card-header-icon">
              <i class="fas fa-user"></i> <span>Thông tin khách hàng</span>
            </div>
            <div class="info-body">
              <div class="info-line">
                <span class="label">Tên KH:</span>
                <span class="value">{{ order.thongTinNhanHang?.tenNguoiNhan || order.tenKhachHang }}</span>
              </div>
              <div class="info-line">
                <span class="label">SĐT:</span>
                <span class="value">{{ order.thongTinNhanHang?.sdt || order.sdtKhachHang }}</span>
              </div>
              <div class="info-line">
                <span class="label">Email:</span>
                <span class="value">
  {{ 
    order.emailKhachHang 
    || order.khachHang?.email 
    || 'Không có' 
  }}
</span>
              </div>
            </div>
          </div>

          <div class="card info-card">
            <div class="card-header-icon">
              <i class="fas fa-map-marker-alt"></i> <span>Thông tin giao hàng</span>
            </div>
            <div class="info-body">
              <div class="info-line">
                <span class="label">Người nhận:</span>
                <span class="value">{{ order.thongTinNhanHang?.tenNguoiNhan || order.tenKhachHang }}</span>
              </div>
              <div class="info-line">
                <span class="label">SĐT nhận:</span>
                <span class="value">{{ order.thongTinNhanHang?.sdt || order.sdtKhachHang }}</span>
              </div>
              <div class="info-line">
                <span class="label">Địa chỉ:</span>
                <span class="value truncate-2">{{ order.thongTinNhanHang?.diaChi || 'Tại quầy' }}</span>
              </div>
              <div class="info-line">
                <span class="label">Ghi chú:</span>
                <span class="value text-gray f-italic">{{ order.ghiChu || 'Không có' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="card product-card">
          <div class="card-header-icon">
            <i class="fas fa-box-open"></i>
            <span>Danh sách sản phẩm ({{ order.sanPhamHoaDon?.length || 0 }})</span>
          </div>
          <div class="table-responsive">
            <table class="custom-table">
              <thead>
                <tr>
                  <th width="5%" class="text-center">STT</th>
                  <th>Tên sản phẩm</th>
                  <th class="text-center">Phân loại</th>
                  <th width="10%" class="text-center">Số lượng</th>
                  <th width="15%" class="text-right">Đơn giá</th>
                  <th width="15%" class="text-right">Thành tiền</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in order.sanPhamHoaDon" :key="index">
                  <td class="text-center">{{ index + 1 }}</td>
                  <td>
                    <div class="product-cell-flex">
                      <img :src="item.anh || 'https://placehold.co/40x40'" class="mini-img" />
                      <span class="fw-bold text-navy">{{ item.tenSanPham }}</span>
                    </div>
                  </td>
                  <td class="text-center">
                    <span class="badge-attr">{{ item.tenMauSac }} / {{ item.tenKichThuoc }}</span>
                  </td>
                  <td class="text-center fw-bold">{{ item.soLuong }}</td>
                  <td class="text-right">{{ formatMoney(item.donGia) }}</td>
                  <td class="text-right text-price">{{ formatMoney(item.donGia * item.soLuong) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="col-sidebar">
        <div class="card summary-card">
          <div class="card-header-icon">
            <i class="fas fa-file-invoice-dollar"></i> <span>Tổng kết thanh toán</span>
          </div>
          <div class="summary-body">
            <div class="summary-row">
              <span>Tổng tiền hàng</span>
              <span>{{ formatMoney(order.tongTienHang) }}</span>
            </div>
            <div class="summary-row">
              <span>Giảm giá</span>
              <span class="text-green">- {{ formatMoney(order.tienGiam) }}</span>
            </div>
            <div class="summary-row">
              <span>Phí vận chuyển</span>
              <span>+ {{ formatMoney(order.phiVanChuyen) }}</span>
            </div>
            <div class="summary-divider"></div>
            <div class="summary-row total-row">
              <span>TỔNG THANH TOÁN</span>
              <span class="total-price">{{ formatMoney(order.tongTienSauGiam) }}</span>
            </div>
            <div class="summary-row" style="margin-top: 5px;">
              <span class="badge" :class="orderTypeInfo(order.loaiHoaDon).class">
                {{ orderTypeInfo(order.loaiHoaDon).text }}
              </span>
              <span class="badge" :class="statusInfo(order.trangThai).class">
                {{ statusInfo(order.trangThai).text }}
              </span>
            </div>
          </div>
        </div>

        <div class="card history-card">
          <div class="card-header-icon">
            <i class="fas fa-history"></i> <span>Lịch sử thanh toán</span>
          </div>
          <div class="history-body">
            <div v-if="!order.lichSuThanhToan || order.lichSuThanhToan.length === 0" class="empty-history">
              Chưa có lịch sử thanh toán
            </div>
            <div v-else class="history-list">
              <div v-for="(hist, hIdx) in order.lichSuThanhToan" :key="hIdx" class="history-item">
                <span class="dot"></span>
                <div class="h-info">
                  <span class="h-date">{{ formatDate(hist.ngayThanhToan) }}</span>
                  <span class="h-desc">{{ hist.hinhThucThanhToan }}</span>
                </div>
                <span class="h-amount">{{ formatMoney(hist.soTien) }}</span>
              </div>
            </div>
          </div>
          <div v-if="order.trangThai === 4" class="history-footer">
            <button class="btn-pay" @click="payOrder">Xác nhận thanh toán</button>
          </div>
        </div>

        <div class="action-buttons-col">
          <button class="btn btn-blue-block" @click="printOrder">
            <i class="fas fa-print"></i> In hóa đơn
          </button>
          <button class="btn btn-orange-block" @click="openEditOrder">
            <i class="fas fa-edit"></i> Sửa thông tin
          </button>
        </div>
      </div>
    </div>

    <div v-if="showEditStatusModal" class="modal-backdrop">
      <div class="modal-container">
        <h3 class="modal-title">Cập nhật trạng thái đơn hàng</h3>
        <div class="order-meta">
  <div>
    <span class="label">Mã đơn hàng: </span>
    <br/>
    <span class="value">{{ order.maHoaDon }}</span>
  </div>
  <div>
    <span class="label">Ngày tạo:</span>
    <br/>
    <span class="value">{{ formatDate(order.ngayTao) }}</span>
  </div>
</div>
        <div class="form-group">
          <label>Trạng thái đơn hàng</label>
          <select v-model="selectedStatus" class="select-status">
            <option v-for="st in availableStatuses" :key="st.value" :value="Number(st.value)">
              {{ st.label }}
            </option>
          </select>
        </div>
<!-- ===== THÔNG TIN NHẬN HÀNG ===== -->
<div class="form-group">
  <label>Tên người nhận</label>
  <input v-model="editForm.tenKhachHang" />
</div>

<div class="form-group">
  <label>SĐT người nhận</label>
  <input v-model="editForm.sdt" />
</div>

<div class="form-group">
  <label>Địa chỉ nhận hàng</label>
  <input v-model="editForm.diaChi" />
</div>
<div class="modal-actions">
  <button class="btn btn-outline" @click="closeEditStatusModal">
    Hủy
  </button>

  <button
    v-if="order.trangThai !== 5 && order.trangThai !== 0"
    class="btn btn-danger"
    @click="cancelOrder"
  >
    Hủy đơn hàng
  </button>

  <button class="btn btn-primary" @click="confirmUpdateStatus">
    Cập nhật
  </button>
</div>
      </div>
    </div>

    <div v-if="showEditInfoModal" class="modal-overlay">
      <div class="modal">
        <h3>Cập nhật thông tin</h3>
        <div class="form-group">
          <label>Tên khách hàng</label>
          <input v-model="editForm.tenKhachHang" />
        </div>
        <div class="form-group">
          <label>SĐT</label>
          <input v-model="editForm.sdt" />
        </div>
        <div class="form-group">
          <label>Địa chỉ nhận</label>
          <input v-model="editForm.diaChi" />
        </div>
        <div class="modal-actions">
          <button class="btn-outline" @click="showEditInfoModal = false">Hủy</button>
          <button class="btn-primary" @click="saveEditInfo">Lưu</button>
        </div>
      </div>
    </div>

  </div>
  <div v-if="order && showPaymentModal" class="modal-overlay" @click.self="showPaymentModal = false">
    <div class="modal payment-modal">
      <div class="modal-header-flex">
        <h3>THANH TOÁN</h3>
        <button class="close-btn" @click="showPaymentModal = false">×</button>
      </div>

      <div class="payment-summary">
        <div class="summary-item">
          <span>Tổng tiền hàng</span>
          <span class="text-danger fw-bold">{{ formatMoney(order.tongTienSauGiam) }}</span>
        </div>
      </div>

      <div class="payment-tabs">
        <button :class="['tab-item', { active: paymentMethod === 'TRANSFER' }]" @click="paymentMethod = 'TRANSFER'">
          CHUYỂN KHOẢN
        </button>
        <button :class="['tab-item', { active: paymentMethod === 'CASH' }]" @click="paymentMethod = 'CASH'">
          TIỀN MẶT
        </button>
      </div>

      <div v-if="paymentMethod === 'TRANSFER'" class="payment-content qr-section">
        <div class="bank-info">
          <p>Ngân hàng: <b>MB Bank</b></p>
          <p>Số tài khoản: <b>0876524519</b></p>
        </div>
        <div class="qr-code">
          <img
            :src="`https://img.vietqr.io/image/MB-0876524519-compact2.png?amount=${order.tongTienSauGiam}&addInfo=Thanh toan don hang ${order.maHoaDon}`"
            alt="QR Thanh toán" />
          <p class="qr-hint">Quét mã để thanh toán đúng số tiền</p>
        </div>
      </div>

      <div v-if="paymentMethod === 'CASH'" class="payment-content cash-section">
        <div class="form-group">
          <label>Tiền khách đưa</label>
          <input type="number" v-model="customerCash" placeholder="Nhập số tiền..." class="payment-input" />
        </div>
      </div>

      <div class="payment-table">
        <table class="table-mini">
          <thead>
            <tr>
              <th>STT</th>
              <th>Phương thức</th>
              <th>Số tiền</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(hist, i) in order.lichSuThanhToan" :key="i">
              <td>{{ i + 1 }}</td>
              <td>{{ hist.hinhThucThanhToan }}</td>
              <td>{{ formatMoney(hist.soTien) }}</td>
              <td>✔</td>
            </tr>

            <tr v-if="!order.lichSuThanhToan || order.lichSuThanhToan.length === 0">
              <td colspan="4" class="text-center text-gray">Không có giao dịch</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="payment-footer">
        <div class="remaining-row">
          <span>Tiền thiếu</span>
          <span class="text-danger fw-bold">{{ formatMoney(calculateRemaining) }}</span>
        </div>
        <button class="btn-submit-payment" @click="confirmPayment">Xác nhận thanh toán</button>
      </div>
    </div>
  </div>
  <div v-if="order" id="invoice-print" class="print-only">
    <div class="invoice-header">
      <div class="brand-section">
        <h2 class="brand-name">YOUR CHOICE SHOP</h2>
        <p>Địa chỉ: Số 1 Trịnh Văn Bô, Nam Từ Liêm, Hà Nội</p>
        <p>Hotline: 0988.777.666</p>
      </div>
      <div class="invoice-title">
        <h1>HÓA ĐƠN BÁN HÀNG</h1>
        <p>Mã đơn: <b>{{ order.maHoaDon }}</b></p>
        <p>Ngày tạo: {{ formatDate(order.ngayTao) }}</p>
      </div>
    </div>

    <div class="invoice-info">
      <p><b>Khách hàng:</b> {{ order.tenKhachHang || 'Khách lẻ' }}</p>
      <p><b>SĐT:</b> {{ order.sdtKhachHang || 'N/A' }}</p>
      <p><b>Địa chỉ:</b> {{ order.diaChi || 'Tại quầy' }}</p>
    </div>

    <table class="invoice-table">
      <thead>
        <tr>
          <th width="5%">STT</th>
          <th>Tên sản phẩm</th>
          <th width="15%">Số lượng</th>
          <th width="20%">Đơn giá</th>
          <th width="20%">Thành tiền</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(item, index) in order.sanPhamHoaDon" :key="index">
          <td class="text-center">{{ index + 1 }}</td>
          <td>{{ item.tenSanPham }} ({{ item.tenMauSac }} / {{ item.tenKichThuoc }})</td>
          <td class="text-center">{{ item.soLuong }}</td>
          <td class="text-right">{{ formatMoney(item.donGia) }}</td>
          <td class="text-right">{{ formatMoney(item.donGia * item.soLuong) }}</td>
        </tr>
      </tbody>
    </table>

    <div class="invoice-footer">
      <div class="footer-left">
        <p class="thanks-msg">Cảm ơn quý khách đã mua hàng!</p>
        <p class="note"><i>Lưu ý: Quý khách vui lòng giữ hóa đơn để đổi trả trong vòng 7 ngày.</i></p>
      </div>
      <div class="footer-right">
        <div class="total-line">
          <span>Tổng tiền hàng:</span>
          <span>{{ formatMoney(order.tongTienHang) }}</span>
        </div>
        <div class="total-line">
          <span>Giảm giá:</span>
          <span>- {{ formatMoney(order.tienGiam) }}</span>
        </div>
        <div class="total-line">
          <span>Phí vận chuyển:</span>
          <span>+ {{ formatMoney(order.phiVanChuyen) }}</span>
        </div>
        <div class="total-line grand-total">
          <span>TỔNG THANH TOÁN:</span>
          <span>{{ formatMoney(order.tongTienSauGiam) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>

import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Swal from 'sweetalert2';
// Đảm bảo bạn đã cài axios và setup file request
import request from '@/services/request';
import { toastSuccess, toastError } from '@/utils/toast';

// --- CONFIG ---
const route = useRoute();
const router = useRouter();
const orderId = route.params.id;
const loading = ref(true);
const order = ref(null);
const showPaymentModal = ref(false);
const paymentMethod = ref('TRANSFER'); // 'TRANSFER' hoặc 'CASH'
const customerCash = ref(0);
const confirmPayment = async () => {
  try {
    const paymentData = {
      hinhThucThanhToan: paymentMethod.value === 'CASH'
        ? 'TIEN_MAT'
        : 'CHUYEN_KHOAN',
      soTien: order.value.tongTienSauGiam,
      ghiChu: `Thanh toán đơn hàng ${order.value.maHoaDon}`
    };

    // ✅ URL ĐÚNG
    await request.post(`/hoa-don/${orderId}/payment`, paymentData);

    showPaymentModal.value = false;
    toastSuccess('Thanh toán thành công!');

    // reload lại chi tiết để lấy lịch sử thanh toán mới
    await fetchOrderDetail();

  } catch (e) {
    console.error(e);
    toastError(e.response?.data || 'Lỗi khi xác nhận thanh toán');
  }
};
const showEditStatusModal = ref(false);
const showEditInfoModal = ref(false);
const currentStatusIndex = ref(-1);

const editForm = ref({
  tenKhachHang: '',
  sdt: '',
  diaChi: ''
});

// --- CONSTANTS ---
const steps = [
  { label: 'Chờ xác nhận', icon: 'fas fa-clipboard-list' },   // 1
  { label: 'Chờ giao hàng', icon: 'fas fa-box' },             // 2
  { label: 'Đang vận chuyển', icon: 'fas fa-shipping-fast' }, // 3
  { label: 'Chờ thanh toán', icon: 'fas fa-credit-card' },    // 4
  { label: 'Hoàn thành', icon: 'fas fa-check-circle' }        // 5
];

const statusMap = [
  { value: 1, label: 'Chờ xác nhận' },
  { value: 2, label: 'Chờ giao hàng' },
  { value: 3, label: 'Đang vận chuyển' },
  { value: 4, label: 'Chờ thanh toán' },
  { value: 5, label: 'Hoàn thành' }
];

// --- HELPERS ---
const formatMoney = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);

const formatDate = (val) => {
  if (!val) return '';
  return new Date(val).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
};

const getCurrentStepIndex = (status) => {
  if (status === 0) return -1; // Đã hủy
  // Mapping trạng thái DB (1-5) sang index mảng steps (0-4)
  return status >= 1 && status <= 5 ? status - 1 : -1;
};

const getStatusIndex = (status) => statusMap.findIndex(s => s.value === status);

// Hiển thị Badge trạng thái
const statusInfo = (status) => {
  const map = {
    0: { text: 'Đã hủy', class: 'badge-cancel' },
    1: { text: 'Chờ xác nhận', class: 'badge-pending' },
    2: { text: 'Chờ giao hàng', class: 'badge-wait' },
    3: { text: 'Đang giao', class: 'badge-shipping' },
    4: { text: 'Chờ thanh toán', class: 'badge-payment' },
    5: { text: 'Hoàn thành', class: 'badge-success' }
  };
  return map[status] || { text: 'Không xác định', class: 'badge-unknown' };
};

// --- SỬA HÀM NÀY ---
const orderTypeInfo = (type) => {
  // DB trả về: 'TRUC_TUYEN' hoặc 'TAI_QUAY'
  if (type === 'TRUC_TUYEN') {
    return { text: 'Trực tuyến', class: 'badge-online' };
  }
  // Mặc định hoặc 'TAI_QUAY'
  return { text: 'Tại quầy', class: 'badge-offline' };
};

// --- API ACTIONS ---
const fetchOrderDetail = async () => {
  loading.value = true;
  try {
    const res = await request.get(`/hoa-don/${orderId}`);
    order.value = res.data;

    // Fill data for edit form
    editForm.value = {
      tenKhachHang: order.value.thongTinNhanHang?.tenNguoiNhan || '',
      sdt: order.value.thongTinNhanHang?.sdt || '',
      diaChi: order.value.thongTinNhanHang?.diaChi || ''
    };

  } catch (error) {
    console.error(error);
    toastError("Không tìm thấy thông tin đơn hàng!");
    // router.push('/admin/orders');
  } finally {
    loading.value = false;
  }
};

const updateOrderStatus = async (newStatus) => {
  try {
    await request.put(`/hoa-don/${orderId}/status`, null, { params: { newStatus } });
    toastSuccess('Cập nhật trạng thái thành công');
    await fetchOrderDetail();
    closeEditStatusModal();
  } catch (e) {
    toastError(e.response?.data || 'Cập nhật thất bại');
  }
};


const saveEditInfo = async () => {
  try {
    const payload = {
      tenNguoiNhan: editForm.value.tenKhachHang,
      sdtNguoiNhan: editForm.value.sdt,
      diaChiNguoiNhan: editForm.value.diaChi
    };

    await request.put(`/hoa-don/${orderId}/info`, payload);

    toastSuccess('Cập nhật thông tin thành công');
    showEditInfoModal.value = false;
    await fetchOrderDetail();

  } catch (e) {
    console.error(e);
    toastError('Lỗi cập nhật thông tin');
  }
};

const cancelOrder = async () => {
  const res = await Swal.fire({
    title: 'Hủy đơn hàng?',
    text: 'Bạn có chắc chắn muốn hủy đơn hàng này không?',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Đồng ý hủy',
    confirmButtonColor: '#ef4444'
  });

  if (res.isConfirmed) {
    await updateOrderStatus(0);
  }
};

const payOrder = () => {
  showPaymentModal.value = true;
};
const calculateRemaining = computed(() => {
  if (!order.value) return 0;
  const total = order.value.tongTienSauGiam || 0;
  if (paymentMethod.value === 'CASH') {
    return Math.max(0, total - customerCash.value);
  }
  return 0; // Chuyển khoản mặc định là quét đủ
});

const printOrder = () => {
  // Đợi một chút để dữ liệu kịp render nếu cần
  window.print();
};

// --- MODAL CONTROLLERS ---


const closeEditStatusModal = () => showEditStatusModal.value = false;

const goPrevStatus = () => {
  if (currentStatusIndex.value > 0) {
    updateOrderStatus(statusMap[currentStatusIndex.value - 1].value);
  }
};

const goNextStatus = () => {
  if (currentStatusIndex.value < statusMap.length - 1) {
    updateOrderStatus(statusMap[currentStatusIndex.value + 1].value);
  }
};

const visibleSteps = computed(() => {
  const idx = getCurrentStepIndex(order.value?.trangThai);
  if (idx < 0) return [];
  return steps.slice(0, idx + 1);
});

const availableStatuses = computed(() => {
  if (!order.value) return [];

  const current = order.value.trangThai;

  return statusMap
    .filter(s =>
      s.value === current ||
      s.value === current - 1 ||
      s.value === current + 1
    )
    .sort((a, b) => a.value - b.value);
});
const selectedStatus = ref(null);

const openEditOrder = () => {
  selectedStatus.value = order.value.trangThai;
  showEditStatusModal.value = true;
};

const confirmUpdateStatus = async () => {
  try {
    // 1️⃣ Update thông tin nhận hàng
    const payload = {
      tenNguoiNhan: editForm.value.tenKhachHang,
      sdtNguoiNhan: editForm.value.sdt,
      diaChiNguoiNhan: editForm.value.diaChi
    };

    await request.put(`/hoa-don/${orderId}/info`, payload);

    // 2️⃣ Update trạng thái (nếu có thay đổi)
    const newStatus = Number(selectedStatus.value);
    if (newStatus !== order.value.trangThai) {
      await request.put(`/hoa-don/${orderId}/status`, null, {
        params: { newStatus }
      });
    }

    toastSuccess('Cập nhật thông tin thành công');
    await fetchOrderDetail();
    closeEditStatusModal();

  } catch (e) {
    console.error(e);
    toastError('Cập nhật thất bại');
  }
};
// --- LIFECYCLE ---
onMounted(() => {
  if (orderId) fetchOrderDetail();
  else router.push('/admin/orders');
});
</script>

<style scoped>
/* General */
.page-container {
  padding: 20px;
  background: #ebecee;
  min-height: 100vh;
  font-family: 'Segoe UI', sans-serif;
  color: #334155;
}

.loading-state {
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #64748b;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
}

.sub-info {
  font-size: 14px;
  color: #64748b;
  margin-top: 5px;
}

.divider {
  margin: 0 8px;
  color: #cbd5e1;
}

.text-primary {
  color: #2563eb;
}

/* Grid */
.detail-grid {
  display: grid;
  grid-template-columns: 2.5fr 1fr;
  gap: 20px;
}

.card {
  background: #fff;
  border: 1px solid #bfdbfe;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  margin-bottom: 20px;
  overflow: hidden;
}

.card-header-icon {
  background: #f1f5f9;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
  font-weight: 700;
  color: #334155;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Timeline */
.timeline-wrapper {
  padding: 25px 20px 10px;
}

.steps-container {
  display: flex;
  justify-content: space-between;
  position: relative;
}

.steps-container::before {
  content: '';
  position: absolute;
  top: 18px;
  left: 0;
  right: 0;
  height: 2px;
  background: #e2e8f0;
  z-index: 1;
}

.step-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 2;
  opacity: 0.6;
}

.step-item.active {
  opacity: 1;
}

.step-icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  transition: 0.3s;
  color: #64748b;
}

.step-item.active .step-icon {
  background: #10b981;
  border-color: #10b981;
  color: #fff;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2);
}

.step-label {
  font-size: 11px;
  font-weight: 600;
  text-align: center;
}

/* Info Rows */
.info-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.info-body {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-line {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.info-line .label {
  color: #64748b;
  min-width: 80px;
}

.info-line .value {
  color: #1e293b;
  font-weight: 500;
  text-align: right;
}

.truncate-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Product Table */
.table-responsive {
  width: 100%;
  overflow-x: auto;
}

.custom-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.custom-table th {
  background: #f5f5f5 !important;
  color: #000000;
  padding: 10px;
  text-align: left;
  border-bottom: 1px solid #bfdbfe;
  white-space: nowrap;
}

.custom-table td {
  padding: 10px;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
  color: #334155;
}

.product-cell-flex {
  display: flex;
  align-items: center;
  gap: 8px;
}

.mini-img {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #e2e8f0;
}

.text-right {
  text-align: right;
}

.text-center {
  text-align: center;
}

.badge-attr {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 11px;
  color: #475569;
  border: 1px solid #e2e8f0;
}

/* Sidebar Summary */
.summary-body {
  padding: 16px;
  font-size: 13px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.summary-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 12px 0;
}

.total-row {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  align-items: center;
}

.total-price {
  color: #dc2626;
  font-size: 18px;
}

.text-green {
  color: #16a34a;
}

/* History */
.history-body {
  padding: 16px;
  max-height: 200px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 12px;
  font-size: 12px;
  position: relative;
}

.history-item::before {
  content: '';
  position: absolute;
  left: 3px;
  top: 8px;
  bottom: -12px;
  width: 1px;
  background: #e2e8f0;
}

.history-item:last-child::before {
  display: none;
}

.history-item .dot {
  width: 7px;
  height: 7px;
  background: #3b82f6;
  border-radius: 50%;
  margin-top: 4px;
  z-index: 1;
}

.h-info {
  flex: 1;
}

.h-date {
  color: #64748b;
  font-size: 10px;
  display: block;
  margin-bottom: 2px;
}

.h-desc {
  font-weight: 500;
  color: #334155;
}

.h-amount {
  font-weight: 700;
  color: #0f172a;
}

.history-footer {
  padding: 10px 16px;
  border-top: 1px solid #e2e8f0;
  text-align: right;
}

/* Buttons & Badges */
.btn {
  height: 36px;
  padding: 0 16px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  transition: 0.2s;
}

.btn-outline {
  background: #fff;
  border: 1px solid #cbd5e1;
  color: #475569;
}

.btn-outline:hover {
  background: #f1f5f9;
  color: #0f172a;
}

.btn-primary {
  background: #2563eb;
  color: #fff;
}

.btn-primary:hover {
  background: #1d4ed8;
}

.btn-danger {
  background: #ef4444;
  color: #fff;
}

.btn-blue-block {
  width: 100%;
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%);
  color: #fff;
  ;
  color: white;
  justify-content: center;
}

.btn-orange-block {
  width: 100%;
  background: #f97316;
  color: white;
  justify-content: center;
}

.btn-pay {
  background: #10b981;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
}

.btn-cancel {
  background: transparent;
  border: none;
  color: #64748b;
  cursor: pointer;
}

.badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  margin-right: 5px;
}

.badge-online {
  background: #dbeafe;
  color: #1e40af;
}

.badge-offline {
  background: #f3f4f6;
  color: #374151;
}

.badge-pending {
  background: #fef9c3;
  color: #854d0e;
}

.badge-wait {
  background: #ffedd5;
  color: #9a3412;
}

.badge-shipping {
  background: #e0f2fe;
  color: #075985;
}

.badge-payment {
  background: #fae8ff;
  color: #86198f;
}

.badge-success {
  background: #dcfce7;
  color: #166534;
}

.badge-cancel {
  background: #fee2e2;
  color: #991b1b;
}

/* Modal */
.modal-backdrop,
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-container,
.modal {
  background: #fff;
  padding: 24px;
  border-radius: 12px;
  width: 400px;
  max-width: 95%;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.modal-title {
  margin-top: 0;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.modal-footer-between {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  border-top: 1px solid #f1f5f9;
  padding-top: 15px;
}

.form-group {
  margin-bottom: 12px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.form-group input {
  padding: 8px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
}

/* Payment Modal */
.payment-modal {
  width: 450px !important;
  padding: 20px !important;
}

.modal-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.modal-header-flex h3 {
  margin: 0;
  font-size: 18px;
  color: #334155;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #94a3b8;
}

.payment-summary {
  background: #f8fafc;
  padding: 10px 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  font-size: 15px;
}

.payment-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.tab-item {
  flex: 1;
  padding: 8px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  color: #64748b;
  transition: 0.3s;
}

.tab-item.active {
  background: #10b981;
  color: white;
  border-color: #10b981;
}

.qr-section {
  text-align: center;
}

.bank-info {
  margin-bottom: 10px;
  font-size: 13px;
}

.qr-code img {
  width: 200px;
  height: 200px;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 5px;
}

.qr-hint {
  font-size: 12px;
  color: #64748b;
  margin-top: 5px;
}

.payment-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 16px;
  margin-top: 5px;
}

.table-mini {
  width: 100%;
  border-collapse: collapse;
  margin: 15px 0;
  font-size: 12px;
}

.table-mini th {
  background: #f1f5f9;
  padding: 8px;
  border: 1px solid #e2e8f0;
}

.table-mini td {
  padding: 8px;
  border: 1px solid #e2e8f0;
}

.payment-footer {
  border-top: 1px solid #eee;
  padding-top: 15px;
}

.remaining-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  font-size: 15px;
}

.btn-submit-payment {
  width: 100%;
  padding: 12px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 6px;
  font-weight: 700;
  cursor: pointer;
  transition: 0.3s;
}

.btn-submit-payment:hover {
  background: #1d4ed8;
}

.action-buttons-col {
  display: flex;
  flex-direction: column;
  /* vì button đang xếp dọc */
  gap: 10px;
  /* chỉnh số px theo ý bạn */
}

/* --- CSS CHO GIAO DIỆN IN --- */
@media screen {
  .print-only {
    display: none;
  }

  /* Ẩn template in khi đang xem trên web */
}

@media print {

  /* Ẩn tất cả các thành phần của trang web trừ template in */
  body * {
    visibility: hidden;
  }

  #invoice-print,
  #invoice-print * {
    visibility: visible;
  }

  #invoice-print {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    padding: 20px;
    background: white;
    color: black;
  }

  /* Định dạng trang in A4 */
  @page {
    size: A4;
    margin: 1cm;
  }
}

/* Style chi tiết cho hóa đơn */
#invoice-print {
  font-family: 'Times New Roman', serif;
  line-height: 1.6;
}

.invoice-header {
  display: flex;
  justify-content: space-between;
  border-bottom: 2px solid #333;
  padding-bottom: 10px;
  margin-bottom: 20px;
}

.brand-name {
  color: #d32f2f;
  margin: 0;
}

.invoice-title {
  text-align: right;
}

.invoice-title h1 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.invoice-info {
  margin-bottom: 20px;
}

.invoice-info p {
  margin: 4px 0;
}

.invoice-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.invoice-table th,
.invoice-table td {
  border: 1px solid #ddd;
  padding: 8px;
}

.invoice-table th {
  background: #f2f2f2 !important;
  font-weight: bold;
}

.invoice-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 30px;
}

.footer-right {
  width: 45%;
}

.total-line {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
}

.grand-total {
  font-weight: bold;
  font-size: 18px;
  border-top: 2px solid #333;
  margin-top: 10px;
  padding-top: 10px;
}

.thanks-msg {
  font-weight: bold;
  font-size: 16px;
  margin-top: 20px;
}

.note {
  font-size: 12px;
  margin-top: 5px;
}

.text-right {
  text-align: right;
}

.text-center {
  text-align: center;
}
.order-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 13px;
}

.order-meta .label {
  color: #64748b;
  font-size: 12px;
}

.order-meta .value {
  font-weight: 600;
  color: #1e293b;
}
</style>