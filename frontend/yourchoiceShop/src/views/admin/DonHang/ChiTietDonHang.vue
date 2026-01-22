<template>
  <div class="order-detail-page" v-if="order">

    <!-- Breadcrumb -->
    <div class="breadcrumb">
      Quản lý đơn hàng / <strong>{{ order.maHoaDon }}</strong>
    </div>

    <!-- Timeline -->
    <div class="card">
      <h3>Lịch sử đơn hàng</h3>

      <div class="timeline">
        <div v-for="(step, index) in timeline" :key="index" class="timeline-step" :class="{ active: step.active }">
          <div class="circle">{{ index + 1 }}</div>
          <div class="label">{{ step.label }}</div>
          <div class="time">{{ step.time }}</div>
        </div>
      </div>

      <div class="actions">
        <router-link to="/admin/orders" class="submenu-item">
            Quay lại
        </router-link>
        <button class="btn-primary">+ Thêm nhân viên tiếp nhận</button>
      </div>
    </div>

    <!-- Order Info -->
    <div class="card">
      <h3>Thông tin đơn hàng</h3>

      <div class="info-grid">
        <div><strong>Mã:</strong> {{ order.maHoaDon }}</div>
        <div><strong>Tên khách hàng:</strong> {{ order.tenKhachHang }}</div>
        <div><strong>Loại:</strong>
          <span class="badge blue">{{ order.loaiHoaDon }}</span>
        </div>
        <div><strong>Trạng thái:</strong>
          <span class="badge yellow">{{ statusText(order.trangThai) }}</span>
        </div>
      </div>

      <div class="info-actions">
        <button class="btn-outline">Xác nhận giao hàng</button>
        <button class="btn-danger">Hủy đơn</button>
      </div>
    </div>

    <!-- Payment History -->
    <div class="card">
      <h3>Lịch sử thanh toán</h3>

      <table>
        <thead>
          <tr>
            <th>Số tiền</th>
            <th>Thời gian</th>
            <th>Loại giao dịch</th>
            <th>PTTT</th>
            <th>Trạng thái</th>
            <th>Người xác nhận</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(p, i) in payments" :key="i">
            <td>{{ p.amount.toLocaleString() }} đ</td>
            <td>{{ p.time }}</td>
            <td><span class="badge blue">{{ p.type }}</span></td>
            <td><span class="badge yellow">{{ p.method }}</span></td>
            <td><span class="badge green">{{ p.status }}</span></td>
            <td>{{ p.confirmBy }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Product List -->
    <div class="card">
      <h3>Danh sách sản phẩm</h3>

      <div class="product" v-for="(item, i) in products" :key="i">
        <img :src="item.image" />
        <div class="product-info">
          <strong>{{ item.name }}</strong>
          <div>Size: {{ item.size }}</div>
          <div>x{{ item.quantity }}</div>
        </div>
        <div class="price">{{ item.price.toLocaleString() }} đ</div>
      </div>
    </div>

    <!-- Summary -->
    <div class="card summary">
  <div>
    <div>Phiếu giảm giá: <strong>Không có</strong></div>
    <div>Giảm giá: 0%</div>
  </div>

  <div class="total">
    <div>Tổng tiền:</div>
    <strong>{{ order.tongTienSauGiam.toLocaleString() }} đ</strong>
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

/* ====== STATUS ====== */
const STATUS_MAP = {
  0: 'Đã hủy',
  1: 'Chờ xác nhận',
  2: 'Chờ giao hàng',
  3: 'Đang vận chuyển',
  4: 'Hoàn thành'
}

const statusText = (status) => STATUS_MAP[status] || 'Không xác định'

/* ====== TIMELINE ====== */
const timeline = computed(() => {
  if (!order.value) return []

  return [
    { label: 'Tạo đơn', time: order.value.ngayTao, active: order.value.trangThai >= 1 },
    { label: 'Xác nhận', time: '10:30', active: order.value.trangThai >= 2 },
    { label: 'Giao hàng', time: '14:00', active: order.value.trangThai >= 3 },
    { label: 'Hoàn thành', time: '18:00', active: order.value.trangThai >= 4 }
  ]
})

/* ====== PAYMENT ====== */
const payments = computed(() => order.value?.payments || [])

/* ====== PRODUCTS ====== */
const products = computed(() => order.value?.products || [])

const goBack = () => router.push('/admin/orders')
</script>

<style scoped>
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
  gap: 40px;
  margin: 16px 0;
}

.timeline-step {
  text-align: center;
  color: #94a3b8;
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
</style>
