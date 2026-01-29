<template>
  <div class="order-detail-page" v-if="order">

    <!-- Breadcrumb -->
    <div class="breadcrumb">
      Quản lý đơn hàng / <strong>{{ order.maHoaDon }}</strong>
    </div>

    <!-- ===== Timeline ===== -->
    <div class="card">
      <h3>Lịch sử đơn hàng</h3>

      <div class="timeline">
        <div v-for="(step, index) in timeline" :key="index" class="timeline-step" :class="{ active: step.active }">
          <div class="circle">✔</div>
          <div class="label">{{ step.label }}</div>
          <div class="time">{{ step.time }}</div>
        </div>
      </div>

      <div class="actions">
        <router-link to="/admin/orders" class="btn-outline">
          Quay lại
        </router-link>
        <button class="btn-primary">+ Thêm nhân viên tiếp nhận</button>
      </div>
    </div>

    <!-- ===== Thông tin đơn hàng ===== -->
    <div class="card">
      <h3>Thông tin đơn hàng</h3>

      <div class="info-grid">
        <div><strong>Mã:</strong> {{ order.maHoaDon }}</div>
        <div><strong>Khách hàng:</strong> {{ order.tenKhachHang }}</div>

        <div>
          <strong>Loại:</strong>
          <span class="type-badge" :class="orderTypeInfo(order.loaiHoaDon).class">
            {{ orderTypeInfo(order.loaiHoaDon).text }}
          </span>

        </div>

        <div>
          <strong>Trạng thái:</strong>
          <span class="status-badge" :class="statusInfo(order.trangThai).class">{{
            statusInfo(order.trangThai).text }}</span>
        </div>

        <div v-if="order.loaiHoaDon === 'TRUC_TUYEN'">
  <strong>Người nhận:</strong> {{ order.thongTinNhanHang?.tenNguoiNhan }}
</div>

<div v-if="order.loaiHoaDon === 'TRUC_TUYEN'">
  <strong>SĐT:</strong> {{ order.thongTinNhanHang?.sdt }}
</div>

<div v-if="order.loaiHoaDon === 'TRUC_TUYEN'" style="grid-column: span 2">
  <strong>Địa chỉ:</strong> {{ order.thongTinNhanHang?.diaChi }}
</div>


      </div>

      <div class="info-actions">
        <button class="btn-outline" @click="openEdit">
          Cập nhật
        </button>

        <!-- Quay lại trạng thái trước -->
        <button v-if="order.trangThai > 1 && order.trangThai < 4" class="btn-outline" @click="prevStatus">
          Quay lại trạng thái trước
        </button>

        <!-- Xác nhận → sang trạng thái tiếp -->
        <button v-if="order.trangThai >= 1 && order.trangThai < 4" class="btn-primary" @click="nextStatus">
          Xác nhận
        </button>

        <!-- Đã hoàn thành -->
        <button v-if="order.trangThai === 4" class="btn-outline" disabled>
          Đơn đã hoàn thành
        </button>

        <!-- Hủy đơn -->
        <button v-if="order.trangThai !== 0 && order.trangThai !== 4" class="btn-danger" @click="cancelOrder">
          Hủy đơn
        </button>

      </div>


    </div>

    <!-- ===== Lịch sử thanh toán ===== -->
    <div class="card">
      <h3>Lịch sử thanh toán</h3>

      <table style="width: 100%; border-collapse: collapse;">
        <thead>
          <tr style="text-align: center;">
            <th>Số tiền</th>
            <th>Thời gian</th>
            <th>Hình thức</th>
            <th>Loại</th>
            <th>Trạng thái</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(p, i) in order.lichSuThanhToan" :key="i" style="text-align: center;">
            <td>{{ formatMoney(p.soTien) }}</td>
            <td>{{ p.ngayThanhToan }}</td>
            <td><span class="type-badge" :class="LoaiThanhToanInfo(p.hinhThucThanhToan).class">
                {{ LoaiThanhToanInfo(p.hinhThucThanhToan).text }}
              </span></td>
            <td><span class="type-badge" :class="orderTypeInfo(order.loaiHoaDon).class">
                {{ orderTypeInfo(order.loaiHoaDon).text }}
              </span>
            </td>
            <td><span class="status-badge" :class="statusInfo(order.trangThai).class">
                {{ statusInfo(order.trangThai).text }}
              </span>

            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ===== Danh sách sản phẩm ===== -->
    <div class="card">
      <h3>Danh sách sản phẩm</h3>

      <div class="product" v-for="(sp, i) in order.sanPhamList" :key="i">
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
        <div>Giảm giá: <strong>{{ formatMoney(order.tienGiamGia) }}</strong></div>
        <div>Phí vận chuyển: <strong>{{ formatMoney(order.phiVanChuyen) }}</strong></div>
      </div>

      <div class="total">
        <div>Tổng thanh toán:</div>
        <strong>{{ formatMoney(order.tongTienSauGiam) }}</strong>
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

import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchOrderDetail } from '@/api/ChiTietHoaDon'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const statusTimes = ref({})

const now = () =>
  new Date().toLocaleString('vi-VN')

const nextStatus = () => {
  if (order.value.trangThai < 4) {
    order.value.trangThai++
    statusTimes.value[order.value.trangThai] = now()
  }
}

const prevStatus = () => {
  if (order.value.trangThai > 1) {
    delete statusTimes.value[order.value.trangThai]
    order.value.trangThai--
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
  TRUC_TUYEN: {
    text: 'Trực tuyến',
    class: 'type-online'
  },
  TAI_QUAY: {
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
    statusTimes.value[1] = order.value.ngayTao
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
.status-badge {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  display: inline-block;
}

/* Chờ xác nhận */
.status-pending {
  background: #fef3c7;
  color: #92400e;
}

/* Chờ giao hàng */
.status-wait-ship {
  background: #e0f2fe;
  color: #0369a1;
}

/* Đang vận chuyển */
.status-shipping {
  background: #ede9fe;
  color: #6d28d9;
}

/* Hoàn thành */
.status-done {
  background: #dcfce7;
  color: #166534;
}

/* Đã hủy */
.status-cancel {
  background: #fee2e2;
  color: #991b1b;
}

.status-unknown {
  background: #e5e7eb;
  color: #374151;
}


.type-badge {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 500;
  display: inline-block;
}

/* Trực tuyến – xanh lá */
.type-online {
  background: #dcfce7;
  color: #166534;
}

/* Tại quầy – xanh dương */
.type-offline {
  background: #e0f2fe;
  color: #0369a1;
}

.type-unknown {
  background: #e5e7eb;
  color: #374151;
}


.order-detail-page {
  background: #f1f5f9;
  padding: 20px;
}

.breadcrumb {
  margin-bottom: 16px;
  color: #64748b;
}

.card {
  background: #fff;
  padding: 16px;
  border-radius: 12px;
  margin-bottom: 16px;
}

.timeline {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin: 16px 0;
}

.timeline-step {
  flex: 1;
  text-align: center;
  color: #94a3b8;
  position: relative;
}


.timeline-step.active {
  color: #16a34a;
}

.circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: auto;
}

.timeline-step.active .circle {
  background: #22c55e;
  color: #fff;
}

.actions {
  display: flex;
  gap: 8px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.product {
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px solid #e2e8f0;
  padding: 12px 0;
}

.product img {
  width: 80px;
  border-radius: 8px;
}

.price {
  margin-left: auto;
  color: #dc2626;
}

.summary {
  display: flex;
  justify-content: space-between;
}

.badge {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
}

.badge.blue {
  background: #e0f2fe;
  color: #0369a1;
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
