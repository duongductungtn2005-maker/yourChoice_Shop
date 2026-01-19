<template>
  <div class="orders-page">
    <!-- Header -->
    <div class="page-header">
      <h2>Quản lý đơn hàng</h2>
      <div class="actions">
        <button class="btn-outline">📷 Quét mã</button>
      </div>
    </div>

    <!-- Filters -->
    <div class="filter-box">
      <input class="input" placeholder="Tìm kiếm hóa đơn" v-model="keyword" />
      <input type="date" class="input" v-model="fromDate" />
      <input type="date" class="input" v-model="toDate" />

      <div class="radio-group">
        <label>
          <input type="radio" value="ALL" v-model="orderType" />
          Tất cả
        </label>

        <label>
          <input type="radio" value="Trực tuyến" v-model="orderType" />
          Trực tuyến
        </label>

        <label>
          <input type="radio" value="Tại quầy" v-model="orderType" />
          Tại quầy
        </label>
      </div>


      <!-- <button class="btn-outline">⬇ Xuất Excel</button> -->
    </div>

    <!-- Tabs -->
    <div class="tabs">
      <span v-for="tab in tabs" :key="tab" :class="['tab', { active: tab === activeTab }]" @click="activeTab = tab">
        {{ tab }}
      </span>
    </div>

    <!-- Table -->
    <div class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>Mã</th>
            <th>Tổng SP</th>
            <th>Tổng số tiền</th>
            <th>Tên khách hàng</th>
            <th>Ngày tạo</th>
            <th>Loại hóa đơn</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(order, index) in paginatedOrders" :key="order.code">
            <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
            <td>{{ order.code }}</td>
            <td>{{ order.totalItems }}</td>
            <td>{{ order.totalPrice }}</td>
            <td>{{ order.customer }}</td>
            <td>{{ order.createdAt }}</td>
            <td>
              <span class="badge" :class="order.type === 'Trực tuyến' ? 'green' : 'blue'">
                {{ order.type }}
              </span>
            </td>
            <td>
              <span v-if="STATUS_INFO[order.status]" class="badge" :class="STATUS_INFO[order.status].class">
                {{ STATUS_INFO[order.status].text }}
              </span>

              <span v-else class="badge gray">
                Không xác định
              </span>
            </td>


            <td>
              <router-link :to="{ name: 'admin-order-detail', params: { id: order.code } }" class="submenu-item">
                ✏️
              </router-link>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pagination">
        <button class="btn-outline" :disabled="currentPage === 1" @click="currentPage--">
          ◀
        </button>

        <span>
          Trang {{ currentPage }} / {{ totalPages }}
        </span>

        <button class="btn-outline" :disabled="currentPage === totalPages" @click="currentPage++">
          ▶
        </button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, computed } from 'vue'
import { fetchOrders } from '@/api/HoaDonApi'

/* ================== DATA ================== */
const allOrders = ref([])
const orders = ref([])

const activeTab = ref('TẤT CẢ')
const keyword = ref('')
const fromDate = ref(null)
const toDate = ref(null)
const orderType = ref('ALL')

/* ================== PAGINATION ================== */
const currentPage = ref(1)
const pageSize = ref(5)

const totalPages = computed(() => {
  const total = Math.ceil(orders.value.length / pageSize.value)
  return total > 0 ? total : 1
})


const paginatedOrders = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return orders.value.slice(start, end)
})

/* ================== CONST ================== */
const tabs = [
  'TẤT CẢ',
  'ĐÃ HỦY',
  'CHỜ XÁC NHẬN',
  'CHỜ GIAO HÀNG',
  'ĐANG VẬN CHUYỂN',
  'HOÀN THÀNH'
]

const STATUS_MAP = {
  'TẤT CẢ': null,
  'ĐÃ HỦY': 0,
  'CHỜ XÁC NHẬN': 1,
  'CHỜ GIAO HÀNG': 2,
  'ĐANG VẬN CHUYỂN': 3,
  'HOÀN THÀNH': 4
}

const STATUS_INFO = {
  0: { text: 'Đã hủy', class: 'red' },
  1: { text: 'Chờ xác nhận', class: 'yellow' },
  2: { text: 'Chờ giao hàng', class: 'blue' },
  3: { text: 'Đang vận chuyển', class: 'purple' },
  4: { text: 'Hoàn thành', class: 'green' }
}

/* ================== METHODS ================== */
const loadOrders = async () => {
  const res = await fetchOrders()
  allOrders.value = res.data.content.map(o => ({
    code: o.maHoaDon,
    totalItems: o.tongSanPham,
    totalPrice: o.tongTienSauGiam,
    customer: o.tenKhachHang,
    createdAt: o.ngayTao,
    type: o.loaiHoaDon,
    status: o.trangThai
  }))
  applyFilter()
}

const applyFilter = () => {
  let result = [...allOrders.value]

  if (keyword.value.trim()) {
    const kw = keyword.value.toLowerCase()
    result = result.filter(o =>
      o.code.toLowerCase().includes(kw) ||
      o.customer.toLowerCase().includes(kw)
    )
  }

  if (fromDate.value) {
    result = result.filter(o => new Date(o.createdAt) >= new Date(fromDate.value))
  }

  if (toDate.value) {
    result = result.filter(o => new Date(o.createdAt) <= new Date(toDate.value))
  }

  const status = STATUS_MAP[activeTab.value]
  if (status !== null) {
    result = result.filter(o => o.status === status)
  }

  if (orderType.value !== 'ALL') {
    result = result.filter(o => o.type === orderType.value)
  }

  orders.value = result
}

/* ================== WATCH ================== */
watch(
  [keyword, fromDate, toDate, orderType, activeTab],
  () => {
    currentPage.value = 1
    applyFilter()
  }
)



/* ================== MOUNT ================== */
onMounted(loadOrders)
</script>



<style scoped>
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding: 16px;
}

.badge.red {
  background: #fee2e2;
  color: #991b1b;
}

.badge.yellow {
  background: #fef9c3;
  color: #854d0e;
}

.badge.blue {
  background: #e0f2fe;
  color: #0369a1;
}

.badge.purple {
  background: #ede9fe;
  color: #5b21b6;
}

.badge.green {
  background: #dcfce7;
  color: #166534;
}


.orders-page {
  background: #f1f5f9;
}

/* Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.actions {
  display: flex;
  gap: 8px;
}

/* Filters */
.filter-box {
  background: #fff;
  padding: 16px;
  border-radius: 12px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.input {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
}

.radio-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* Buttons */
.btn-primary {
  background: #2563eb;
  color: #fff;
  border: none;
  padding: 8px 14px;
  border-radius: 8px;
  cursor: pointer;
}

.btn-outline {
  background: #fff;
  border: 1px solid #e2e8f0;
  padding: 8px 14px;
  border-radius: 8px;
  cursor: pointer;
}

/* Tabs */
.tabs {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  font-size: 14px;
}

.tab {
  cursor: pointer;
  color: #64748b;
  padding-bottom: 6px;
}

.tab.active {
  color: #2563eb;
  border-bottom: 2px solid #2563eb;
}

/* Table */
.table-wrapper {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 12px;
  border-bottom: 1px solid #e2e8f0;
  text-align: left;
  font-size: 14px;
}

th {
  background: #f8fafc;
  color: #475569;
}

/* Badges */
.badge {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
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
</style>