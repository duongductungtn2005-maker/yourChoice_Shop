<template>
   <div class="page-container">

      <div class="header-section">
         <div class="header-left">
            <h1 class="page-title">Chi tiết đơn hàng</h1>
            <div class="sub-info" v-if="!loading">
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

      <div v-else class="detail-grid">

         <div class="col-main">

            <div class="card status-card">
               <div class="card-header-icon">
                  <i class="fas fa-truck-fast"></i> <span>Trạng thái đơn hàng</span>
               </div>
               <div class="timeline-wrapper">
                  <div class="steps-container">
  <div
    v-for="(step, index) in visibleSteps"
    :key="index"
    class="step-item active"
  >
    <div class="step-icon">
      <i :class="step.icon"></i>
    </div>
    <div class="step-label">{{ step.label }}</div>
  </div>
</div>
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

            <div class="info-row">
               <div class="card info-card">
                  <div class="card-header-icon">
                     <i class="fas fa-user"></i> <span>Thông tin khách hàng</span>

                  </div>
                  <div class="info-body">
                     <div class="info-line">
                        <span class="label">Tên khách hàng</span>
                        <span class="value">{{ order.tenKhachHang }}</span>
                     </div>
                     <div class="info-line">
                        <span class="label">Số điện thoại</span>
                        <span class="value">{{ order.sdtKhachHang }}</span>
                     </div>
                     <div class="info-line">
                        <span class="label">Email</span>
                        <span class="value">{{ order.emailKhachHang }}</span>
                     </div>
                  </div>
               </div>

               <div class="card info-card">
                  <div class="card-header-icon">
                     <i class="fas fa-map-marker-alt"></i> <span>Thông tin giao hàng</span>
                  </div>
                  <div class="info-body">
                     <div class="info-line">
                        <span class="label">Địa chỉ</span>
                        <span class="value truncate-2">{{ order.diaChiGiaoHang }}</span>
                     </div>
                     <div class="info-line">
                        <span class="label">Loại đơn</span>
                        <span class="value fw-bold text-primary">{{ order.loaiHoaDon }}</span>
                     </div>
                     <div class="info-line">
                        <span class="label">Ghi chú</span>
                        <span class="value text-gray f-italic">{{ order.ghiChu || 'Không có ghi chú' }}</span>
                     </div>
                  </div>
               </div>
            </div>

            <div class="card product-card">
               <div class="card-header-icon">
                  <i class="fas fa-box-open"></i> <span>Danh sách sản phẩm ({{ order.chiTietHoaDonList?.length || 0
                     }})</span>
               </div>
               <div class="table-responsive">
                  <table class="custom-table">
                     <thead>
                        <tr>
                           <th width="5%" class="text-center">STT</th>
                           <th>Tên sản phẩm</th>
                           <th class="text-center">Kích cỡ</th>
                           <th class="text-center">Màu sắc</th>
                           <th width="10%" class="text-center">Số lượng</th>
                           <th width="15%" class="text-right">Đơn giá</th>
                           <th width="15%" class="text-right">Thành tiền</th>
                        </tr>
                     </thead>
                     <tbody>
                        <tr v-for="(item, index) in order.chiTietHoaDonList" :key="index">
                           <td class="text-center">{{ index + 1 }}</td>
                           <td class="fw-bold text-navy">{{ item.tenSanPham }}</td>
                           <td class="text-center"><span class="badge-attr">{{ item.tenKichThuoc }}</span></td>
                           <td class="text-center"><span class="badge-attr">{{ item.tenMauSac }}</span></td>
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
                     <span>TỔNG TIỀN</span>
                     <span class="total-price">{{ formatMoney(order.tongTienSauGiam) }}</span>
                  </div>
               </div>
            </div>

            <div class="card history-card">
               <div class="card-header-icon">
                  <i class="fas fa-history"></i> <span>Lịch sử thanh toán</span>
               </div>

               <div class="history-body">
                  <div v-if="!order.lichSuThanhToanList || order.lichSuThanhToanList.length === 0"
                     class="empty-history">
                     Chưa có lịch sử thanh toán
                  </div>

                  <div v-else class="history-list">
                     <div v-for="(hist, hIdx) in order.lichSuThanhToanList" :key="hIdx" class="history-item">
                        <span class="dot"></span>
                        <div class="h-info">
                           <span class="h-date">{{ formatDate(hist.ngayThanhToan) }}</span>
                           <span class="h-desc">{{ hist.ghiChu }}</span>
                        </div>
                        <span class="h-amount">{{ formatMoney(hist.soTien) }}</span>
                     </div>
                  </div>
               </div>

               <!-- FOOTER THANH TOÁN -->
               <div v-if="order.trangThai === 4" class="history-footer">
                  <button class="btn-pay" @click="payOrder">
                     Xác nhận thanh toán
                  </button>
               </div>
            </div>


            <div class="action-buttons-col">
               <button class="btn btn-blue-block" @click="printOrder">
                  <i class="fas fa-print"></i> In hóa đơn
               </button>
               <button class="btn btn-orange-block" @click="openEditOrder">
                  <i class="fas fa-edit"></i> Chỉnh sửa đơn hàng
               </button>

            </div>

         </div>

      </div>
   </div>

   <div v-if="showEditStatusModal" class="modal-backdrop">
      <div class="modal-container">
         <h3 class="modal-title">Cập nhật trạng thái đơn hàng</h3>

         <p class="modal-current">
            Trạng thái hiện tại:
            <b>{{ statusMap[currentStatusIndex]?.label }}</b>
         </p>

         <div class="modal-actions">
            <button class="btn btn-outline" :disabled="currentStatusIndex === 0" @click="goPrevStatus">
               ⬅ Trạng thái trước
            </button>

            <button class="btn btn-primary" :disabled="currentStatusIndex === statusMap.length - 1"
               @click="goNextStatus">
               Trạng thái tiếp theo ➡
            </button>
         </div>

         <div class="modal-footer modal-footer-between">
  <button class="btn btn-cancel" @click="closeEditStatusModal">
    Đóng
  </button>

  <button
    v-if="[1,2,3,4].includes(order.trangThai)"
    class="btn btn-danger"
    @click="cancelOrder"
  >
    Hủy đơn hàng
  </button>
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
      </div>
   </div>

</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import request from '@/services/request'; // Import request service
import { toastSuccess, toastError } from '@/utils/toast';
import { computed } from 'vue';

const visibleSteps = computed(() => {
  if (!order.value?.trangThai) return [];
  const currentIdx = getCurrentStepIndex(order.value.trangThai);
  if (currentIdx < 0) return [];
  return steps.slice(0, currentIdx + 1);
});


const route = useRoute();
const router = useRouter();
const orderId = route.params.id; // Lấy ID từ URL (VD: /orders/HD0001 -> orderId = HD0001)

const loading = ref(true);
const order = ref({});

// --- API FETCH DATA ---
const fetchOrderDetail = async () => {
   loading.value = true;
   try {
      // Gọi API lấy chi tiết (Thay đổi endpoint nếu BE của bạn khác)
      // Ví dụ: GET /api/v1/hoa-don/detail/HD0001 hoặc /api/v1/hoa-don/HD0001
      // ✅ ĐÚNG
      const res = await request.get(`/hoa-don/${orderId}`);
      order.value = res.data;
   } catch (error) {
      console.error("Lỗi tải đơn hàng:", error);
      toastError("Không tìm thấy thông tin đơn hàng!");
      router.push('/admin/orders'); // Quay về danh sách nếu lỗi
   } finally {
      loading.value = false;
   }
};

// --- TIMELINE SETUP ---
const statusMap = [
   { value: 1, label: 'Chờ xác nhận' },
   { value: 2, label: 'Chờ giao hàng' },
   { value: 3, label: 'Đang vận chuyển' },
   { value: 4, label: 'Chờ thanh toán' },
   { value: 5, label: 'Hoàn thành' }
];

const getStatusIndex = (status) =>
   statusMap.findIndex(s => s.value === status);


const steps = [
  { label: 'Chờ xác nhận', icon: 'fas fa-clipboard-list' }, // 1
  { label: 'Chờ giao hàng', icon: 'fas fa-box' },           // 2
  { label: 'Đang vận chuyển', icon: 'fas fa-shipping-fast' }, // 3
  { label: 'Chờ thanh toán', icon: 'fas fa-credit-card' }, // 4 ✅ MỚI
  { label: 'Hoàn thành', icon: 'fas fa-check-circle' }      // 5
];


const getCurrentStepIndex = (status) => {
  switch (status) {
    case 0: // Đã hủy
      return -1;
    case 1:
      return 0; // Chờ xác nhận
    case 2:
      return 1; // Chờ giao hàng
    case 3:
      return 2; // Đang vận chuyển
    case 4:
      return 3; // Chờ thanh toán ✅
    case 5:
      return 4; // Hoàn thành
    default:
      return -1;

  }
};


const getProgressWidth = (status) => {
  const idx = getCurrentStepIndex(status);
  if (idx <= 0) return '0%';

  return `${(idx / (steps.length - 1)) * 100}%`;
};


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



// --- UTILS ---
const formatMoney = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);
const formatDate = (val) => {
   if (!val) return '';
   return new Date(val).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
};

// --- ACTIONS ---
const printOrder = () => {
   // Gọi API xuất PDF hóa đơn
   window.open(`http://localhost:8080/api/v1/hoa-don/print/${orderId}`, '_blank');
};

const cancelOrder = async () => {
  const res = await Swal.fire({
    title: 'Hủy đơn hàng?',
    text: 'Bạn có chắc chắn muốn hủy đơn hàng này không?',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Đồng ý hủy',
    cancelButtonText: 'Quay lại',
    confirmButtonColor: '#ef4444'
  });

  if (res.isConfirmed) {
    try {
      await request.put(
        `/hoa-don/${orderId}/status`,
        null,
        {
          params: { newStatus: 0 } // 0 = ĐÃ HỦY
        }
      );

      toastSuccess('Đã hủy đơn hàng thành công');
      fetchOrderDetail();
    } catch (e) {
      toastError(e.response?.data || 'Lỗi khi hủy đơn hàng');
    }
  }
};


onMounted(() => {
   if (orderId) {
      fetchOrderDetail();
   } else {
      toastError("Mã đơn hàng không hợp lệ");
      router.push('/admin/orders');
   }
});

const showEditStatusModal = ref(false);
const currentStatusIndex = ref(-1);

const openEditOrder = () => {
   const idx = getStatusIndex(order.value.trangThai);

   if (idx === -1) {
      toastError('Không thể chỉnh sửa trạng thái này');
      return;
   }

   currentStatusIndex.value = idx;
   showEditStatusModal.value = true;
};



const closeEditStatusModal = () => {
   showEditStatusModal.value = false;
};

const goPrevStatus = () => {
   if (currentStatusIndex.value > 0) {
      updateOrderStatus(statusMap[currentStatusIndex.value - 1].value);
      closeEditStatusModal();
   }
};

const goNextStatus = () => {
   if (currentStatusIndex.value < statusMap.length - 1) {
      updateOrderStatus(statusMap[currentStatusIndex.value + 1].value);
      closeEditStatusModal();
   }
};




const updateOrderInfo = async (payload) => {
   try {
      await request.put(`/hoa-don/${orderId}/update-info`, payload);
      toastSuccess('Cập nhật thông tin đơn hàng thành công');
      fetchOrderDetail(); // reload data
   } catch (e) {
      toastError(e.response?.data?.message || 'Cập nhật thất bại');
   }
};

const updateOrderStatus = async (newStatus) => {
   try {
      await request.put(
         `/hoa-don/${orderId}/status`,
         null,
         {
            params: {
               newStatus: newStatus
            }
         }
      );

      toastSuccess('Cập nhật trạng thái đơn hàng thành công');
      fetchOrderDetail();
   } catch (e) {
      toastError(e.response?.data || 'Cập nhật trạng thái thất bại');
   }
};

const payOrder = async () => {
   try {
      // gọi API thanh toán ở đây nếu có

      // sau khi thanh toán thành công
      await updateOrderStatus(5); // 5 = HOÀN THÀNH
   } catch (e) {
      toastError('Thanh toán thất bại');
   }
};



</script>

<style scoped>
.modal-footer-between {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
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



.btn-danger {
  background: #ef4444;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: 0.2s;
}

.btn-danger:hover {
  background: #dc2626;
}


.history-footer {
   padding: 12px 16px;
   border-top: 1px solid #e2e8f0;
   display: flex;
   justify-content: flex-end;
}

.btn-pay {
   padding: 6px 14px;
   background: #10b981;
   color: #fff;
   border: none;
   border-radius: 16px;
   font-size: 13px;
   font-weight: 600;
   cursor: pointer;
   transition: 0.2s;
}

.btn-pay:hover {
   background: #059669;
}


.modal-backdrop {
   position: fixed;
   inset: 0;
   background: rgba(0, 0, 0, 0.45);
   display: flex;
   align-items: center;
   justify-content: center;
   z-index: 999;
}

.modal-container {
   background: #fff;
   padding: 20px;
   border-radius: 8px;
   width: 420px;
   max-width: 90%;
}

.modal-title {
   margin-bottom: 12px;
}

.modal-current {
   margin-bottom: 20px;
   font-size: 14px;
}

.modal-actions {
   display: flex;
   justify-content: space-between;
   gap: 10px;
}

.modal-footer {
   margin-top: 16px;
   text-align: right;
}


/* GENERAL */
.page-container {
   padding: 20px;
   font-family: 'Segoe UI', sans-serif;
   background: #f8fafc;
   min-height: 100vh;
   color: #334155;
}

/* LOADING */
.loading-state {
   display: flex;
   flex-direction: column;
   align-items: center;
   justify-content: center;
   height: 400px;
   color: #64748b;
   gap: 15px;
}

/* HEADER */
.header-section {
   display: flex;
   justify-content: space-between;
   align-items: flex-start;
   margin-bottom: 24px;
}

.page-title {
   margin: 0 0 5px 0;
   font-size: 24px;
   font-weight: 700;
   color: #1e293b;
}

.sub-info {
   font-size: 14px;
   color: #64748b;
   display: flex;
   align-items: center;
   gap: 8px;
}

.divider {
   color: #cbd5e1;
}

.text-primary {
   color: #2563eb;
}

/* BUTTONS HEADER */
.btn {
   height: 38px;
   padding: 0 16px;
   border-radius: 6px;
   font-weight: 600;
   cursor: pointer;
   border: 1px solid transparent;
   display: inline-flex;
   align-items: center;
   gap: 8px;
   font-size: 13px;
   transition: 0.2s;
}

.btn-outline {
   background: #fff;
   border-color: #cbd5e1;
   color: #475569;
}

.btn-outline:hover {
   background: #f1f5f9;
   border-color: #94a3b8;
   color: #0f172a;
}

/* LAYOUT GRID */
.detail-grid {
   display: grid;
   grid-template-columns: 2.5fr 1fr;
   gap: 20px;
}

/* CARD GLOBAL STYLE */
.card {
   background: #fff;
   border: 1px solid #bfdbfe;
   border-radius: 12px;
   box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
   margin-bottom: 20px;
   overflow: hidden;
}

.card-header-icon {
   background: #f8fafc;
   padding: 12px 16px;
   border-bottom: 1px solid #e2e8f0;
   font-weight: 700;
   color: #334155;
   display: flex;
   align-items: center;
   gap: 8px;
}

.card-header-icon i {
   color: #64748b;
}

/* --- CỘT TRÁI --- */

/* 1. TIMELINE */
.status-card {
   padding-bottom: 20px;
}

.timeline-wrapper {
   padding: 30px 40px 10px 40px;
   position: relative;
}

.steps-container {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 0 20px;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}



.step-icon {
   width: 36px;
   height: 36px;
   border-radius: 50%;
   background: #fff;
   border: 2px solid #e2e8f0;
   display: flex;
   align-items: center;
   justify-content: center;
   font-size: 14px;
   transition: 0.3s;
}

.step-label {
   font-size: 12px;
   font-weight: 600;
   text-align: center;
}

/* Active Step */
.step-item.active .step-icon {
   border-color: #10b981;
   background: #10b981;
   color: #fff;
   box-shadow: 0 0 0 4px rgba(16, 185, 129, 0.1);
}

.step-item.active .step-label {
   color: #10b981;
}

/* 2. INFO CARDS (CUSTOMER & SHIPPING) */
.info-row {
   display: grid;
   grid-template-columns: 1fr 1fr;
   gap: 20px;
   margin-bottom: 20px;
}

.info-body {
   padding: 16px;
   display: flex;
   flex-direction: column;
   gap: 12px;
}

.info-line {
   display: flex;
   justify-content: space-between;
   font-size: 13px;
}

.info-line .label {
   color: #64748b;
   width: 100px;
   flex-shrink: 0;
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

.f-italic {
   font-style: italic;
}

/* 3. PRODUCT TABLE */
.table-responsive {
   width: 100%;
   overflow-x: auto;
}

.custom-table {
   width: 100%;
   border-collapse: collapse;
}

.custom-table th {
   background: #eff6ff;
   padding: 12px;
   font-size: 12px;
   color: #1e40af;
   border-bottom: 1px solid #bfdbfe;
   font-weight: 700;
   white-space: nowrap;
}

.custom-table td {
   padding: 12px;
   border-bottom: 1px solid #f1f5f9;
   font-size: 13px;
   color: #334155;
   vertical-align: middle;
}

.text-navy {
   color: #1e293b;
}

.text-price {
   color: #ef4444;
   font-weight: 600;
}

.badge-attr {
   background: #f1f5f9;
   padding: 4px 8px;
   border-radius: 4px;
   font-size: 11px;
   border: 1px solid #e2e8f0;
   color: #475569;
}

/* --- CỘT PHẢI --- */

/* SUMMARY */
.summary-body {
   padding: 16px;
}

.summary-row {
   display: flex;
   justify-content: space-between;
   margin-bottom: 10px;
   font-size: 13px;
   color: #475569;
}

.summary-divider {
   height: 1px;
   background: #e2e8f0;
   margin: 15px 0;
}

.total-row {
   color: #0f172a;
   font-weight: 700;
   font-size: 15px;
   margin-bottom: 0;
   align-items: center;
}

.total-price {
   color: #ef4444;
   font-size: 18px;
}

.text-green {
   color: #10b981;
}

/* HISTORY */
.history-body {
   padding: 16px;
   min-height: 100px;
}

.empty-history {
   color: #94a3b8;
   font-style: italic;
   text-align: center;
   font-size: 13px;
   margin-top: 20px;
}

.history-item {
   display: flex;
   align-items: center;
   gap: 10px;
   margin-bottom: 12px;
   font-size: 13px;
}

.history-item .dot {
   width: 8px;
   height: 8px;
   background: #10b981;
   border-radius: 50%;
}

.h-info {
   flex: 1;
   display: flex;
   flex-direction: column;
}

.h-date {
   color: #64748b;
   font-size: 11px;
}

.h-desc {
   font-weight: 500;
}

.h-amount {
   font-weight: 700;
   color: #0f172a;
}

/* ACTION BUTTONS COLUMN */
.action-buttons-col {
   display: flex;
   flex-direction: column;
   gap: 12px;
}

.btn-blue-block {
   width: 100%;
   padding: 10px;
   background: #3b82f6;
   color: white;
   border: none;
   border-radius: 8px;
   font-weight: 600;
   cursor: pointer;
   transition: 0.2s;
   display: flex;
   align-items: center;
   justify-content: center;
   gap: 8px;
}

.btn-blue-block:hover {
   background: #2563eb;
}

.btn-orange-block {
   width: 100%;
   padding: 10px;
   background: #f97316;
   color: white;
   border: none;
   border-radius: 8px;
   font-weight: 600;
   cursor: pointer;
   transition: 0.2s;
   display: flex;
   align-items: center;
   justify-content: center;
   gap: 8px;
}

.btn-orange-block:hover {
   background: #ea580c;
}

.btn-red-outline-block {
   width: 100%;
   padding: 8px;
   background: #fff;
   color: #ef4444;
   border: 1px solid #ef4444;
   border-radius: 8px;
   font-weight: 600;
   cursor: pointer;
   transition: 0.2s;
}

.btn-red-outline-block:hover {
   background: #fef2f2;
}

/* UTILS */
.text-center {
   text-align: center;
}

.text-right {
   text-align: right;
}

.fw-bold {
   font-weight: 700;
}

</style>