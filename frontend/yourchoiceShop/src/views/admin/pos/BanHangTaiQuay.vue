<template>
  <div class="pos-page">
    <!-- ===== TOP BAR ===== -->
    <div class="pos-topbar">
      <div class="topbar-left">
        <div class="pos-title">
          <div class="pos-badge">POS</div>
          <div>
            <h2>Bán hàng tại quầy</h2>
            <p class="pos-sub">
              Quản lý đơn nhanh • Tạo tối đa <b>{{ MAX_TABS }}</b> đơn cùng lúc
            </p>
          </div>
        </div>
      </div>

      <div class="topbar-right">
        <div class="tab-counter">
          <span class="dot" :class="{ on: orderTabs.length > 0 }"></span>
          <span>
            Đang mở: <b>{{ orderTabs.length }}</b>/<b>{{ MAX_TABS }}</b>
          </span>
        </div>

        <button class="btn-primary" @click="createNewTab">
          <span class="btn-icon">＋</span>
          <span>Tạo đơn hàng</span>
        </button>
      </div>
    </div>

    <!-- ===== TAB BAR ===== -->
    <div v-if="orderTabs.length" class="order-tabs">
      <div v-for="tab in orderTabs" :key="tab.id" class="order-tab"
        :class="{ active: tab.id === activeTabId, disabled: showModal }" @click="!showModal && (activeTabId = tab.id)">
        <div class="tab-pill">
          <span class="tab-code">{{ tab.maHoaDon || 'Đơn mới' }}</span>
          <span class="tab-meta" v-if="tab.cart?.length">• {{ tab.cart.length }} SP</span>
        </div>

        <button class="close-tab" title="Đóng tab" @click.stop="closeTab(tab.id)">×</button>
      </div>
    </div>

    <!-- ===== EMPTY STATE (KHI CHƯA CÓ ĐƠN) ===== -->
    <div v-if="orderTabs.length === 0" class="empty-shell">
      <div class="empty-card">
        <div class="empty-hero">
          <div class="hero-left">
            <h3>Chưa có đơn hàng nào</h3>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== POS MAIN (CHỈ HIỆN KHI CÓ TAB) ===== -->
    <div v-if="currentOrder" class="pos-main-container">
      <div class="card">
        <div class="card-header">
          <div class="card-title">
            <h3>Sản phẩm trong hóa đơn</h3>
            <span class="chip" v-if="cart.length">{{ cart.length }} sản phẩm</span>
          </div>
          <button class="btn-primary" @click="openProductModal">+ Thêm sản phẩm</button>
        </div>

        <div class="card-body">
          <div v-if="cart.length === 0" class="empty-cart">
            <div class="empty-icon">🛒</div>
            <div class="empty-text">
              <b>Giỏ hàng đang trống</b>
              <div class="muted">Nhấn “Thêm sản phẩm” để bắt đầu</div>
            </div>
          </div>

          <div v-else class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th>Mã</th>
                  <th>Tên</th>
                  <th>Giá</th>
                  <th>SL</th>
                  <th>Thành tiền</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, i) in cart" :key="item.id">
                  <td class="mono">{{ item.code }}</td>
                  <td class="name-cell">
                    <div class="name-main">{{ item.name }}</div>
                    <div class="name-sub muted" v-if="item.brand || item.material">
                      {{ item.brand || '—' }} • {{ item.material || '—' }}
                    </div>
                  </td>
                  <td class="p-price">{{ formatMoney(item.price) }}</td>
                  <td>
                    <div class="item-control">
                      <button @click="item.qty--" :disabled="item.qty === 1" title="Giảm">−</button>
                      <span>{{ item.qty }}</span>
                      <button @click="item.qty++" title="Tăng">＋</button>
                    </div>
                  </td>
                  <td class="p-price">{{ formatMoney(item.price * item.qty) }}</td>
                  <td>
                    <button class="btn-remove" title="Xoá" @click="cart.splice(i, 1)">×</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <!-- ===== LEFT: CART ===== -->
      <div class="pos-cart">

        <!-- ===== CUSTOMER ===== -->
        <div class="card mt">
          <div class="card-header">
            <div class="card-title">
              <h3>Khách hàng</h3>
              <span class="muted">Thông tin người mua</span>
            </div>
            <button class="btn-outline" @click="openCustomerModal">Chọn</button>
          </div>

          <div class="card-body form-grid">
            <input v-model="customer.name" placeholder="Tên khách hàng" />
            <input v-model="customer.phone" placeholder="SĐT" />
            <input v-model="customer.email" placeholder="Email khách hàng" />
          </div>
        </div>

        <div v-if="orderType === 'ONLINE'" class="card mt">
          <div class="card-header">
            <div class="card-title">
              <h3>Thông tin người nhận</h3>
              <span class="muted">Dùng cho đơn online</span>
            </div>
          </div>

          <div class="card-body form-grid">
            <input v-model="customer.name" placeholder="Tên người nhận" />
            <input v-model="customer.phone" placeholder="SĐT người nhận" />
            <input v-model="customer.address" placeholder="Địa chỉ nhận hàng" />
            <textarea v-model="note" placeholder="Ghi chú"></textarea>
          </div>
        </div>
      </div>

      <!-- ===== RIGHT: INFO BAR ===== -->
      <div class="pos-info">
        <!-- DISCOUNT -->
        <div class="card">

          <!-- ORDER TYPE TABS -->
          <div class="order-type-toggle">
            <span :class="{ active: orderType === 'TAI_QUAY' }"></span>

            <label class="switch">
              <input type="checkbox" v-model="isOnline" @change="toggleOrderType" />
              <span class="slider"></span>
            </label>

            <span :class="{ active: orderType === 'ONLINE' }">Online</span>
          </div>
          <div class="card-header">
            <div class="card-title">
              <h3>Giảm giá</h3>
              <span class="muted">Phiếu áp dụng</span>
            </div>
            <button class="btn-primary" @click="openDiscountModal">+ Thêm</button>
          </div>

          <div class="card-body">
            <div v-if="discounts.length === 0" class="empty-small">
              Chưa áp dụng phiếu giảm giá
            </div>

            <div v-else class="table-wrap">
              <table class="table small-table">
                <thead>
                  <tr>
                    <th>Tên phiếu</th>
                    <th>Giảm</th>
                    <th>Thời hạn</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(d, i) in discounts" :key="d.id">
                    <td class="name-cell">
                      <div class="name-main">{{ d.name }}</div>
                      <div class="muted mono" v-if="d.code">{{ d.code }}</div>
                    </td>
                    <td class="p-price">
                      -{{ d.type === 'percent' ? d.value + '%' : formatMoney(d.value) }}
                    </td>
                    <td class="muted">{{ d.startDate }} → {{ d.endDate }}</td>
                    <td>
                      <button class="btn-remove" title="Gỡ" @click="discounts.splice(i, 1)">×</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- STAFF -->
        <div class="card">
          <div class="card-header">
            <div class="card-title">
              <h3>Nhân viên</h3>
              <span class="muted">Người tạo đơn</span>
            </div>
            <button class="btn-outline" @click="openStaffModal">Chọn</button>
          </div>

          <div class="card-body form-grid">
            <input :value="staff.code" placeholder="Mã nhân viên" disabled />
            <input :value="staff.name" placeholder="Tên nhân viên" disabled />
          </div>
        </div>

        <!-- PAYMENT -->
        <div class="card payment-card">
          <div class="card-header">
            <div class="card-title">
              <h3>Thanh toán</h3>
              <span class="muted">Tổng kết đơn hàng</span>
            </div>
          </div>

          <div class="card-body pay-body">
            <div class="row">
              <span>Tổng sản phẩm</span>
              <span class="price-col">{{ formatMoney(totalProductPrice) }}</span>
            </div>

            <div class="row">
              <span>Giảm giá</span>
              <span class="price-col">-{{ formatMoney(totalDiscount) }}</span>
            </div>

            <div class="row" v-if="orderType === 'ONLINE'">
              <span>Phí vận chuyển</span>
              <input type="number" min="0" v-model.number="shippingFee" class="price-col ship-input" placeholder="0" />
            </div>

            <div class="row total-row">
              <span>Khách cần trả</span>
              <span class="price-col total">{{ formatMoney(totalPrice) }}</span>
            </div>

            <button v-if="orderType === 'TAI_QUAY'" class="btn-pay" @click="openPaymentModal">
              THANH TOÁN
            </button>

            <button v-else class="btn-pay" @click="handleCreateOrderOnline">
              TẠO HÓA ĐƠN
            </button>

            <div class="pay-note muted">
              Lưu ý: Vui lòng chọn nhân viên & nhập thông tin khách trước khi thanh toán.
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ===== MODAL SẢN PHẨM ===== -->
    <div v-if="showModal" class="modal-overlay">
      <div class="modal-content large">
        <div class="modal-header-flex">
          <h3>Chọn sản phẩm</h3>
          <button class="close-btn" @click="showModal = false">×</button>
        </div>

        <input v-model="productKeyword" class="search-input" placeholder="Tìm theo tên hoặc mã" />
        <div class="price-range">
          <span>Giá</span>
          <input type="number" v-model.number="priceRange[0]" placeholder="Từ" />
          <span>–</span>
          <input type="number" v-model.number="priceRange[1]" placeholder="Đến" />
        </div>

        <div class="modal-table-wrapper">
          <table class="table modal-table">
            <thead>
              <tr>
                <th>Mã</th>
                <th>Tên</th>
                <th>Giá</th>
                <th>Tồn kho</th>
                <th>SL</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in filteredProducts" :key="p.id">
                <td class="mono">{{ p.code }}</td>
                <td class="name-cell">
                  <div class="name-main">{{ p.name }}</div>
                  <div class="muted">{{ p.brand }} • {{ p.material }}</div>
                </td>
                <td class="p-price">{{ formatMoney(p.price) }}</td>
                <td>{{ p.tonKho }}</td>
                <td>
                  <div class="item-control">
                    <button @click="p.qty--" :disabled="p.qty === 1">−</button>
                    <span>{{ p.qty }}</span>
                    <button @click="p.qty++" :disabled="p.qty >= p.tonKho">＋</button>
                  </div>
                </td>
                <td><input type="checkbox" v-model="p.checked" /></td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination">
          <button @click="productPage--" :disabled="productPage === 0">‹ Trước</button>
          <span>Trang {{ productPage + 1 }} / {{ totalProductPages }}</span>
          <button @click="productPage++" :disabled="productPage + 1 >= totalProductPages">Sau ›</button>
        </div>

        <div class="modal-actions">
          <button class="btn-outline" @click="showModal = false">Hủy</button>
          <button class="btn-primary" @click="confirmAddProduct">Thêm</button>
        </div>
      </div>
    </div>

    <!-- ===== MODAL KHÁCH HÀNG ===== -->
    <div v-if="showCustomerModal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header-flex">
          <h3>Chọn khách hàng</h3>
          <button class="close-btn" @click="showCustomerModal = false">×</button>
        </div>

        <input v-model="customerKeyword" class="search-input" placeholder="Tìm tên / SĐT / email" />

        <div class="modal-table-wrapper">
          <table class="table modal-table">
            <thead>
              <tr>
                <th>Tên KH</th>
                <th>SĐT</th>
                <th>Email</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="c in filteredCustomers" :key="c.id">
                <td class="name-cell">
                  <div class="name-main">{{ c.name }}</div>
                </td>
                <td class="mono">{{ c.phone }}</td>
                <td>{{ c.email }}</td>
                <td>
                  <button class="btn-select" @click="selectCustomer(c)">Chọn</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination">
          <button @click="customerPage--" :disabled="customerPage === 0">‹ Trước</button>
          <span>Trang {{ customerPage + 1 }} / {{ totalCustomerPages }}</span>
          <button @click="customerPage++" :disabled="customerPage + 1 >= totalCustomerPages">Sau ›</button>
        </div>
      </div>
    </div>

    <!-- ===== MODAL GIẢM GIÁ ===== -->
    <div v-if="showDiscountModal" class="modal-overlay">
      <div class="modal-content discount-modal">
        <div class="modal-header-flex">
          <h3>Phiếu giảm giá</h3>
          <button class="close-btn" @click="showDiscountModal = false">×</button>
        </div>

        <input v-model="discountKeyword" class="search-input" placeholder="Tìm mã / tên phiếu" />

        <div class="modal-table-wrapper">
          <table class="table modal-table">
            <thead>
              <tr>
                <th></th>
                <th>Mã</th>
                <th>Tên</th>
                <th>Giá trị</th>
                <th>Thời hạn</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="d in filteredDiscounts" :key="d.id">
                <td><input type="checkbox" v-model="d.checked" /></td>
                <td class="mono">{{ d.code }}</td>
                <td class="name-cell">
                  <div class="name-main">{{ d.name }}</div>
                </td>
                <td class="p-price">
                  {{ d.type === 'percent' ? d.value + '%' : formatMoney(d.value) }}
                </td>
                <td class="muted">{{ d.startDate }} → {{ d.endDate }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination">
          <button @click="discountPage--" :disabled="discountPage === 0">‹ Trước</button>
          <span>Trang {{ discountPage + 1 }} / {{ totalDiscountPages }}</span>
          <button @click="discountPage++" :disabled="discountPage + 1 >= totalDiscountPages">Sau ›</button>
        </div>

        <div class="modal-actions">
          <button class="btn-outline" @click="showDiscountModal = false">Hủy</button>
          <button class="btn-primary" @click="confirmAddDiscount">Áp dụng</button>
        </div>
      </div>
    </div>

    <!-- ===== MODAL THANH TOÁN ===== -->
    <div v-if="showPaymentModal" class="modal-overlay">
      <div class="payment-modal">
        <div class="modal-header-flex">
          <h3>Thanh toán</h3>
          <button class="close-btn" @click="showPaymentModal = false">×</button>
        </div>

        <div class="payment-tabs">
          <div class="tab-item" :class="{ active: paymentMethod === 'TRANSFER' }" @click="paymentMethod = 'TRANSFER'">
            Chuyển khoản
          </div>

          <div class="tab-item" :class="{ active: paymentMethod === 'CASH' }" @click="paymentMethod = 'CASH'">
            Tiền mặt
          </div>
        </div>

        <div v-if="paymentMethod === 'CASH'">
          <input v-model.number="customerCash" type="number" class="search-input" placeholder="Tiền khách đưa" />
          <div class="p-price">Còn lại: {{ formatMoney(calculateRemaining) }}</div>
        </div>

        <div v-if="paymentMethod === 'TRANSFER'" class="qr-section">
          <div class="qr-code">
            <img :src="qrImageUrl" alt="QR chuyển khoản" />
          </div>

          <div class="p-price">Số tiền: {{ formatMoney(totalPrice) }}</div>

          <div style="font-size: 13px; color: #6b7280">
            Nội dung: THANH TOAN HOA DON
          </div>
        </div>

        <div class="payment-footer">
          <button class="btn-pay" @click="confirmCreateOrder">
            {{ orderType === 'TAI_QUAY' ? 'THANH TOÁN' : 'TẠO ĐƠN ONLINE' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ===== MODAL NHÂN VIÊN ===== -->
    <div v-if="showStaffModal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header-flex">
          <h3>Chọn nhân viên</h3>
          <button class="close-btn" @click="showStaffModal = false">×</button>
        </div>

        <input v-model="staffKeyword" class="search-input" placeholder="Tìm mã / tên nhân viên" />

        <div class="modal-table-wrapper">
          <table class="table modal-table">
            <thead>
              <tr>
                <th>Mã NV</th>
                <th>Tên NV</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="s in filteredStaffs" :key="s.id">
                <td class="mono">{{ s.code }}</td>
                <td class="name-cell">
                  <div class="name-main">{{ s.name }}</div>
                </td>
                <td>
                  <button class="btn-select" @click="selectStaff(s)">Chọn</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination">
          <button @click="staffPage--" :disabled="staffPage === 0">‹ Trước</button>
          <span>Trang {{ staffPage + 1 }} / {{ totalStaffPages }}</span>
          <button @click="staffPage++" :disabled="staffPage + 1 >= totalStaffPages">Sau ›</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { createOrder } from '@/api/HoaDonApi'

/* ================= FILTER SẢN PHẨM ================= */
const productKeyword = ref('')
const priceRange = ref([0, 1000000])

/* ================= ROUTER ================= */
const router = useRouter()

/* ================= MODAL KHÁCH HÀNG ================= */
const showCustomerModal = ref(false)
const customerKeyword = ref('')

import { getKhachHang } from '@/api/KhachHangApi'

const customers = ref([])
const filteredCustomers = computed(() =>
  customers.value.filter(c =>
    [c.name, c.phone, c.email]
      .join(' ')
      .toLowerCase()
      .includes(customerKeyword.value.toLowerCase())
  )
)
const loadCustomers = async () => {
  const res = await getKhachHang({
    page: customerPage.value,
    size: customerSize.value,
    trangThai: 1
  })

  customers.value = res.data.content.map(c => ({
    id: c.id,
    code: c.maKhachHang,
    name: c.tenKhachHang,
    phone: c.soDienThoai,
    email: c.email
  }))

  totalCustomerPages.value = res.data.totalPages
}

const customerPage = ref(0)
const customerSize = ref(10)
const totalCustomerPages = ref(0)

const openCustomerModal = async () => {
  await loadCustomers()
  showCustomerModal.value = true
}

const selectCustomer = (c) => {
  customer.value = {
    name: c.name,
    phone: c.phone,
    email: c.email,
    address: ''
  }
  showCustomerModal.value = false
}

/* ================= MODAL SẢN PHẨM ================= */
const showModal = ref(false)

import { getChiTietSanPham } from '@/api/ChiTietSanPhamApi'

const products = ref([])
const productPage = ref(0)
const productSize = ref(10)
const totalProductPages = ref(0)

const loadProducts = async () => {
  const res = await getChiTietSanPham({
    page: productPage.value,
    size: productSize.value,
    trangThai: 1
  })

  products.value = res.data.content.map(p => {
    const inCart = cart.value.find(i => i.id === p.id)

    return {
      id: p.id,
      code: p.maCtsp,
      name: p.sanPham.tenSanPham,
      brand: p.thuongHieu?.tenThuongHieu || '—',
      material: p.chatLieu?.tenChatLieu || '—',
      price: p.giaBan,
      tonKho: p.soLuong,
      qty: 1,
      checked: false
    }
  })

  totalProductPages.value = res.data.totalPages
}
const openProductModal = async () => {
  await loadProducts()
  showModal.value = true
}

const confirmAddProduct = () => {
  products.value.forEach(p => {
    if (!p.checked) return

    const exist = cart.value.find(i => i.id === p.id)

    if (exist) {
      exist.qty = Math.min(exist.qty + p.qty, p.tonKho)
    } else {
      cart.value.push({
        id: p.id,
        code: p.code,
        name: p.name,
        brand: p.brand,
        material: p.material,
        price: p.price,
        qty: p.qty,
        tonKho: p.tonKho
      })
    }

    p.checked = false
    p.qty = 1
  })

  showModal.value = false
}

const filteredProducts = computed(() =>
  products.value.filter(p => {
    const keyword = productKeyword.value.toLowerCase()

    const matchKeyword =
      p.name.toLowerCase().includes(keyword) ||
      (p.code && p.code.toLowerCase().includes(keyword))

    const matchPrice =
      p.price >= priceRange.value[0] &&
      p.price <= priceRange.value[1]

    return matchKeyword && matchPrice
  })
)

/* ================= GIẢM GIÁ ================= */
const showDiscountModal = ref(false)
const discountKeyword = ref('')

import { getPhieuGiamGia } from '@/api/PhieuGiamGiaApi'

const discountList = ref([])

const discountPage = ref(0)
const discountSize = ref(10)
const totalDiscountPages = ref(0)

const loadDiscounts = async () => {
  const res = await getPhieuGiamGia({
    page: discountPage.value,
    size: discountSize.value,
    trangThai: 1
  })

  discountList.value = res.data.content.map(d => ({
    id: d.id,
    code: d.maPhieuGiamGia,
    name: d.tenPhieuGiamGia,
    type: d.loaiPhieu === 'PhanTram' ? 'percent' : 'money',
    value: d.giaTriGiam,
    startDate: d.ngayBatDau,
    endDate: d.ngayKetThuc,
    trangThai: d.trangThai,
    checked: false
  }))

  totalDiscountPages.value = res.data.totalPages
}

const filteredDiscounts = computed(() =>
  discountList.value.filter(d =>
    d.trangThai === 1 &&
    [d.code, d.name]
      .join(' ')
      .toLowerCase()
      .includes(discountKeyword.value.toLowerCase())
  )
)

const openDiscountModal = async () => {
  await loadDiscounts()
  showDiscountModal.value = true
}

const confirmAddDiscount = () => {
  discountList.value.forEach(d => {
    if (d.checked && !discounts.value.find(x => x.id === d.id)) {
      discounts.value.push({
        id: d.id,
        code: d.code,
        name: d.name,
        type: d.type,
        value: d.value,
        startDate: d.startDate,
        endDate: d.endDate
      })
    }
    d.checked = false
  })

  showDiscountModal.value = false
}

/* ================= TÍNH TIỀN ================= */
const totalProductPrice = computed(() =>
  cart.value.reduce((s, i) => s + i.price * i.qty, 0)
)

const totalDiscount = computed(() => {
  let discount = 0

  discounts.value.forEach(d => {
    if (d.type === 'percent') {
      discount += totalProductPrice.value * d.value / 100
    } else {
      discount += d.value
    }
  })

  return Math.min(discount, totalProductPrice.value)
})

const totalPrice = computed(() =>
  Math.max(
    totalProductPrice.value - totalDiscount.value + shippingFee.value,
    0
  )
)

/* ================= THANH TOÁN ================= */
const handleCreateOrder = async () => {
  if (!cart.value.length) {
    alert('Giỏ hàng đang trống!')
    return
  }

  if (!customer.value.name || !customer.value.phone) {
    alert('Vui lòng nhập tên và số điện thoại!')
    return
  }

  if (!confirm('Xác nhận tạo hóa đơn?')) return

  try {
    const payload = {
      tenKhachHang: customer.value.name,
      soDienThoai: customer.value.phone,
      diaChi: customer.value.address,
      email: customer.value.email,
      idNhanVien: staff.value.id,
      tienGiamGia: totalDiscount.value,

      phieuGiamGia: discounts.value.map(d => ({
        loaiPhieu: d.type === 'percent' ? 'PhanTram' : 'TienMat',
        giaTriGiam: d.value,
        trangThai: 1
      })),

      hinhThucThanhToan:
        paymentMethod.value === 'CASH'
          ? 'TIEN_MAT'
          : 'CHUYEN_KHOAN',

      items: cart.value.map(i => ({
        idChiTietSanPham: i.id,
        soLuong: i.qty,
        donGia: i.price
      }))
    }

    await createOrder(payload)

    alert('Tạo hóa đơn thành công!')

    const tabIndex = orderTabs.value.findIndex(t => t.id === activeTabId.value)
    if (tabIndex !== -1) {
      orderTabs.value.splice(tabIndex, 1)
      activeTabId.value = orderTabs.value.length > 0 ? orderTabs.value[0].id : null
    }

    if (orderTabs.value.length === 0) {
      clearOrderTabs()
    }

    router.push({ name: 'admin-order-list' })
  } catch (err) {
    console.error(err)
    alert('Lỗi khi tạo hóa đơn!')
  }
}

/* ================= FORMAT ================= */
const formatMoney = (val) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(val)

/* ================= PAYMENT ================= */
const showPaymentModal = ref(false)
const paymentMethod = ref('TRANSFER') // TRANSFER | CASH
const customerCash = ref(0)

const openPaymentModal = () => {
  if (!cart.value.length) {
    alert('Giỏ hàng đang trống!')
    return
  }
  if (!customer.value.name || !customer.value.phone) {
    alert('Vui lòng nhập tên và số điện thoại!')
    return
  }
  showPaymentModal.value = true
}

const calculateRemaining = computed(() => {
  if (paymentMethod.value === 'CASH') {
    return Math.max(0, totalPrice.value - customerCash.value)
  }
  return 0
})

const confirmCreateOrder = async () => {
  if (paymentMethod.value === 'CASH' && customerCash.value < totalPrice.value) {
    alert('Tiền khách đưa chưa đủ!')
    return
  }

  showPaymentModal.value = false
  await handleCreateOrder()
}
const qrImageUrl = computed(() => {
  const bank = '970422'
  const account = '123456789'
  const name = 'NGUYEN VAN A'
  const amount = totalPrice.value

  return `https://img.vietqr.io/image/${bank}-${account}-compact.png?amount=${amount}&addInfo=THANH TOAN HOA DON&accountName=${encodeURIComponent(name)}`
})

/* ================= STAFF ================= */
const showStaffModal = ref(false)
const staffKeyword = ref('')
import { getNhanVien } from '@/api/NhanVienApi'

const staffs = ref([])
const staffPage = ref(0)
const staffSize = ref(10)
const totalStaffPages = ref(0)

const loadStaffs = async () => {
  const res = await getNhanVien({
    page: staffPage.value,
    size: staffSize.value,
    status: 1
  })

  staffs.value = res.data.content.map(s => ({
    id: s.id,
    code: s.maNhanVien,
    name: s.tenNhanVien
  }))

  totalStaffPages.value = res.data.totalPages
}

const filteredStaffs = computed(() =>
  staffs.value.filter(s =>
    [s.code, s.name]
      .join(' ')
      .toLowerCase()
      .includes(staffKeyword.value.toLowerCase())
  )
)

const openStaffModal = async () => {
  staffPage.value = 0
  await loadStaffs()
  showStaffModal.value = true
}
const selectStaff = (s) => {
  staff.value = {
    id: s.id,
    code: s.code,
    name: s.name
  }
  showStaffModal.value = false
}

import { createOrderOnline } from '@/api/HoaDonApi'

const handleSubmitOrder = async () => {
  if (orderType.value === 'TAI_QUAY') {
    openPaymentModal()
  } else {
    await handleCreateOrderOnline()
  }
}

const handleCreateOrderOnline = async () => {
  if (!customer.value.name || !customer.value.phone || !customer.value.address) {
    alert('Vui lòng nhập đầy đủ thông tin người nhận')
    return
  }

  const payload = {
    tenKhachHang: customer.value.name,
    soDienThoai: customer.value.phone,
    diaChi: customer.value.address,
    email: customer.value.email,
    ghiChu: note.value,
    idNhanVien: staff.value.id,
    tienGiamGia: totalDiscount.value,
    phiVanChuyen: shippingFee.value,

    phieuGiamGia: discounts.value.map(d => ({
      loaiPhieu: d.type === 'percent' ? 'PhanTram' : 'TienMat',
      giaTriGiam: d.value,
      trangThai: 1
    })),

    items: cart.value.map(i => ({
      idChiTietSanPham: i.id,
      soLuong: i.qty,
      donGia: i.price
    }))
  }
  await createOrderOnline(payload)

  alert('Tạo đơn online thành công – chờ xác nhận')

  const tabIndex = orderTabs.value.findIndex(t => t.id === activeTabId.value)
  if (tabIndex !== -1) {
    orderTabs.value.splice(tabIndex, 1)
    activeTabId.value = orderTabs.value.length > 0 ? orderTabs.value[0].id : null
  }

  if (orderTabs.value.length === 0) {
    clearOrderTabs()
  }

  router.push({ name: 'admin-order-list' })
}

const MAX_TABS = 5

const orderTabs = ref([])
const activeTabId = ref(null)

/* ================= LOCALSTORAGE PERSISTENCE ================= */
const STORAGE_KEY = 'pos_order_tabs'

const saveOrderTabs = () => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      orderTabs: orderTabs.value,
      activeTabId: activeTabId.value
    }))
  } catch (err) {
    console.error('Lỗi khi lưu dữ liệu:', err)
  }
}

const loadOrderTabs = () => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY)
    if (stored) {
      const data = JSON.parse(stored)
      orderTabs.value = data.orderTabs || []
      activeTabId.value = data.activeTabId || null
    }
  } catch (err) {
    console.error('Lỗi khi tải dữ liệu:', err)
  }
}

const clearOrderTabs = () => {
  try {
    localStorage.removeItem(STORAGE_KEY)
  } catch (err) {
    console.error('Lỗi khi xóa dữ liệu:', err)
  }
}

onMounted(() => {
  loadOrderTabs()
})

watch(orderTabs, saveOrderTabs, { deep: true })
watch(activeTabId, saveOrderTabs)

watch(staffPage, loadStaffs)
watch(productPage, loadProducts)
watch(customerPage, loadCustomers)
watch(discountPage, loadDiscounts)

const createNewTab = () => {
  if (orderTabs.value.length >= MAX_TABS) {
    alert('Chỉ được tối đa 5 đơn hàng')
    return
  }

  const newTab = {
    id: Date.now(),
    maHoaDon: null,
    orderType: 'TAI_QUAY',
    cart: [],
    customer: { name: '', phone: '', email: '', address: '' },
    staff: { id: null, code: '', name: '' },
    discounts: [],
    shippingFee: 0,
    note: ''
  }

  orderTabs.value.push(newTab)
  activeTabId.value = newTab.id
}

const closeTab = (id) => {
  const index = orderTabs.value.findIndex(t => t.id === id)
  if (index === -1) return

  const tab = orderTabs.value[index]

  if (tab.cart.length > 0) {
    if (!confirm('Đơn hàng này chưa thanh toán, vẫn đóng?')) return
  }

  orderTabs.value.splice(index, 1)

  if (activeTabId.value === id) {
    activeTabId.value = orderTabs.value.length
      ? orderTabs.value[orderTabs.value.length - 1].id
      : null
  }
}

const currentOrder = computed(() =>
  orderTabs.value.find(t => t.id === activeTabId.value)
)

const cart = computed({
  get: () => currentOrder.value?.cart || [],
  set: v => currentOrder.value && (currentOrder.value.cart = v)
})

const customer = computed({
  get: () => currentOrder.value?.customer || {},
  set: v => currentOrder.value && (currentOrder.value.customer = v)
})

const staff = computed({
  get: () => currentOrder.value?.staff || {},
  set: v => currentOrder.value && (currentOrder.value.staff = v)
})

const discounts = computed({
  get: () => currentOrder.value?.discounts || [],
  set: v => currentOrder.value && (currentOrder.value.discounts = v)
})

const shippingFee = computed({
  get: () => currentOrder.value?.shippingFee ?? 0,
  set: v => currentOrder.value && (currentOrder.value.shippingFee = Number(v) || 0)
})

const note = computed({
  get: () => currentOrder.value?.note || '',
  set: v => currentOrder.value && (currentOrder.value.note = v)
})

const orderType = ref('TAI_QUAY')

const isOnline = ref(false)

const toggleOrderType = () => {
  orderType.value = isOnline.value ? 'ONLINE' : 'TAI_QUAY'
}
</script>

<style scoped>
/* ===========================
   ✅ SCROLL FIX TRIỆT ĐỂ
   =========================== */

/* ĐỪNG đặt overflow:auto ở pos-page (nó tạo container scroll riêng + dễ bị cắt trong layout cha) */
.pos-page {
  min-height: 100vh;
}

/* nếu layout cha có overflow hidden, cái này giúp body vẫn scroll */
:global(html),
:global(body) {
  height: 100%;
  overflow: auto;
}

/* ✅ tạo “khung” cho POS: 2 cột cuộn riêng */
.pos-main-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: auto auto;
  gap: 12px;
  margin-top: 12px;

  /* cho phép item co giãn trong grid */
  min-height: 0;

  /* chiều cao làm việc: auto để hiển thị hết nội dung */
  height: auto;
}

/* giữ vị trí */
.pos-main-container>.order-type-tabs {
  grid-column: 1 / -1;
}

/* ✅ Sản phẩm FULL WIDTH hàng 1 */
.pos-main-container > .card:first-child {
  grid-column: 1 / -1;
  grid-row: 1;
  overflow-x: hidden;
}

/* Khách hàng - bên trái hàng 2 */
.pos-main-container>.pos-cart {
  grid-column: 1;
  grid-row: 2;
  overflow-x: hidden;
}

/* Info - bên phải hàng 2 */
.pos-main-container>.pos-info {
  grid-column: 2;
  grid-row: 2;
  overflow-x: hidden;
}

/* Sticky cho tabs loại đơn trong khung cuộn */
.order-type-tabs {
  position: sticky;
  top: 0;
  z-index: 5;
}

/* Mobile: không cuộn riêng, cuộn theo trang */
@media (max-width: 1100px) {
  .pos-main-container {
    grid-template-columns: 1fr;
    grid-template-rows: auto auto auto;
    height: auto;
  }

  /* Sản phẩm full width hàng 1 */
  .pos-main-container > .card:first-child {
    grid-column: 1;
    grid-row: 1;
    overflow: visible;
  }

  /* Khách hàng full width hàng 2 */
  .pos-main-container>.pos-cart {
    grid-column: 1;
    grid-row: 2;
    overflow: visible;
  }

  /* Info full width hàng 3 */
  .pos-main-container>.pos-info {
    grid-column: 1;
    grid-row: 3;
    overflow: visible;
  }
}

/* ===========================
   PHẦN CSS UI (giữ nguyên của bạn)
   =========================== */

.muted {
  color: #64748b;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
}

.mt {
  margin-top: 12px;
}

/* ===== TOP BAR ===== */
.pos-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 14px 14px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 16px;
  box-shadow: 0 10px 24px rgba(2, 6, 23, 0.06);
}

.pos-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pos-badge {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-weight: 800;
  color: #1d4ed8;
  background: linear-gradient(180deg, #eff6ff, #ffffff);
  border: 1px solid #dbeafe;
}

.pos-title h2 {
  margin: 0;
  font-size: 18px;
  line-height: 1.2;
}

.pos-sub {
  margin: 2px 0 0;
  font-size: 13px;
  color: #64748b;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tab-counter {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #fff;
  font-size: 13px;
}

.dot {
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #cbd5e1;
  box-shadow: 0 0 0 4px rgba(203, 213, 225, 0.25);
}

.dot.on {
  background: #22c55e;
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.20);
}

/* ===== BUTTONS ===== */
.btn-primary {
  border: none;
  padding: 10px 14px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(180deg, #2563eb, #1d4ed8);
  box-shadow: 0 10px 18px rgba(37, 99, 235, 0.22);
  transition: transform .12s ease, box-shadow .12s ease, filter .12s ease;
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.btn-primary:hover {
  transform: translateY(-1px);
  filter: brightness(1.02);
}

.btn-primary:active {
  transform: translateY(0px);
  box-shadow: 0 8px 14px rgba(37, 99, 235, 0.18);
}

.btn-primary.big {
  padding: 12px 16px;
  font-size: 14px;
  border-radius: 14px;
}

.btn-icon {
  font-size: 18px;
  line-height: 1;
}

.btn-outline {
  background: #fff;
  border: 1px solid #c7d2fe;
  color: #1d4ed8;
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: background .12s ease, transform .12s ease;
}

.btn-outline:hover {
  background: #eff6ff;
  transform: translateY(-1px);
}

/* ===== ORDER TABS ===== */
.order-tabs {
  display: flex;
  gap: 10px;
  padding: 12px 4px 0;
  flex-wrap: wrap;
  align-items: center;
}

.order-tab {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 999px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  cursor: pointer;
  transition: transform .12s ease, box-shadow .12s ease, border-color .12s ease;
  box-shadow: 0 8px 18px rgba(2, 6, 23, 0.05);
}

.order-tab:hover {
  transform: translateY(-1px);
  border-color: #c7d2fe;
  box-shadow: 0 10px 22px rgba(2, 6, 23, 0.07);
}

.order-tab.active {
  background: linear-gradient(180deg, rgba(37, 99, 235, 0.98), rgba(29, 78, 216, 0.98));
  border-color: rgba(37, 99, 235, 0.55);
  color: #fff;
}

.order-tab.disabled {
  opacity: 0.6;
  pointer-events: none;
}

.tab-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.tab-code {
  font-size: 13px;
  font-weight: 800;
}

.tab-meta {
  font-size: 12px;
  opacity: 0.9;
}

.close-tab {
  width: 26px;
  height: 26px;
  border-radius: 999px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.65);
  cursor: pointer;
  font-size: 16px;
  font-weight: 800;
  line-height: 1;
  display: grid;
  place-items: center;
}

.order-tab.active .close-tab {
  border-color: rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.18);
  color: #fff;
}

/* ===== EMPTY STATE ===== */
.empty-shell {
  padding-top: 14px;
}

.empty-card {
  border-radius: 18px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(10px);
  box-shadow: 0 14px 34px rgba(2, 6, 23, 0.07);
  overflow: hidden;
}

.empty-hero {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 14px;
  padding: 18px;
  align-items: center;
}

.hero-left h3 {
  margin: 10px 0 6px;
  font-size: 20px;
}

.hero-left p {
  margin: 0;
  color: #475569;
  font-size: 14px;
  line-height: 1.6;
  max-width: 560px;
}

.hero-icon {
  width: 60px;
  height: 60px;
  border-radius: 18px;
  display: grid;
  place-items: center;
  color: #1d4ed8;
  background: linear-gradient(180deg, #eff6ff, #ffffff);
  border: 1px solid #dbeafe;
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.12);
}

.empty-actions {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hint {
  font-size: 13px;
  color: #64748b;
}

.hero-right {
  display: flex;
  justify-content: flex-end;
}

.mock {
  width: min(360px, 100%);
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: linear-gradient(180deg, #ffffff, #f8fafc);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.9);
  overflow: hidden;
}

.mock-top {
  display: flex;
  gap: 6px;
  padding: 10px 12px;
  border-bottom: 1px solid #e2e8f0;
}

.mock-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #e2e8f0;
}

.mock-body {
  padding: 12px;
}

.mock-row {
  height: 10px;
  border-radius: 999px;
  background: #e2e8f0;
  margin-bottom: 10px;
}

.w-80 {
  width: 80%;
}

.w-70 {
  width: 70%;
}

.w-60 {
  width: 60%;
}

.w-50 {
  width: 50%;
}

.mock-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 14px 0;
}

.mock-kpi {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 14px;
}

.kpi {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 10px;
  background: #fff;
}

.kpi-label {
  font-size: 12px;
  color: #64748b;
}

.kpi-val {
  font-size: 14px;
  font-weight: 800;
  color: #dc2626;
  margin-top: 2px;
}

.mock-btn {
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(180deg, #2563eb, #1d4ed8);
  opacity: 0.35;
}

.empty-steps {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 14px 18px 18px;
  border-top: 1px solid rgba(226, 232, 240, 0.95);
  background: rgba(248, 250, 252, 0.7);
}

.step {
  display: flex;
  gap: 10px;
  padding: 12px;
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.8);
}

.step-no {
  width: 30px;
  height: 30px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  font-weight: 900;
  color: #1d4ed8;
  background: #eff6ff;
  border: 1px solid #dbeafe;
}

.step-text {
  font-size: 13px;
}

.step-text b {
  display: block;
  margin-bottom: 2px;
}

.step-text .muted {
  font-size: 12px;
}

@media (max-width: 980px) {
  .empty-hero {
    grid-template-columns: 1fr;
  }

  .hero-right {
    justify-content: flex-start;
  }

  .empty-steps {
    grid-template-columns: 1fr;
  }
}

/* ===== ORDER TYPE TABS ===== */
.order-type-tabs {
  display: flex;
  gap: 10px;
  padding: 10px 10px;
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  box-shadow: 0 10px 24px rgba(2, 6, 23, 0.05);
}

.tab-item {
  flex: none;
  padding: 10px 18px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  cursor: pointer;
  font-weight: 800;
  font-size: 13px;
  transition: all .12s ease;
}

.tab-item.active {
  background: linear-gradient(180deg, #2563eb, #1d4ed8);
  border-color: rgba(37, 99, 235, 0.55);
  color: #fff;
}

/* ===== CARDS ===== */
.card {
  border-radius: 16px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(10px);
  box-shadow: 0 12px 26px rgba(2, 6, 23, 0.06);
  overflow: hidden;
}

.card-header {
  padding: 14px 14px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.9);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.card-title h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 900;
}

.card-title .muted {
  display: block;
  margin-top: 2px;
  font-size: 12px;
}

.card-body {
  padding: 14px;
}

/* chips */
.chip {
  margin-left: 10px;
  padding: 6px 10px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  font-size: 12px;
  font-weight: 800;
  color: #334155;
}

/* ===== FORMS ===== */
.form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

input,
textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 11px 12px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: #fff;
  font-size: 13px;
  outline: none;
  transition: border-color .12s ease, box-shadow .12s ease;
}

textarea {
  min-height: 92px;
  resize: vertical;
}

input:focus,
textarea:focus {
  border-color: #93c5fd;
  box-shadow: 0 0 0 4px rgba(147, 197, 253, 0.25);
}

input:disabled {
  background: #f8fafc;
  color: #64748b;
}

/* ===== TABLE ===== */
.table-wrap {
  overflow-x: auto;
}

.table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  background: transparent;
}

.table thead th {
  font-size: 12px;
  text-transform: none;
  letter-spacing: .2px;
  color: #475569;
  background: rgba(248, 250, 252, 0.9);
  border-bottom: 1px solid rgba(226, 232, 240, 0.95);
  padding: 10px 8px;
}

.table td {
  padding: 12px 8px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.85);
  font-size: 13px;
  vertical-align: middle;
}

.table tbody tr:hover td {
  background: rgba(239, 246, 255, 0.35);
}

.name-cell {
  text-align: left;
}

.name-main {
  font-weight: 800;
  color: #0f172a;
}

.name-sub {
  font-size: 12px;
}

.p-price {
  color: #dc2626;
  font-weight: 900;
}

.price-col {
  text-align: right;
  font-weight: 900;
  color: #dc2626;
}

.price-col.total {
  font-size: 18px;
}

/* ===== ITEM CONTROL ===== */
.item-control {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  padding: 6px 8px;
  border-radius: 999px;
  background: rgba(248, 250, 252, 0.9);
  border: 1px solid rgba(226, 232, 240, 0.95);
}

.item-control button {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  border: 1px solid #dbeafe;
  background: #eff6ff;
  cursor: pointer;
  font-weight: 900;
}

.item-control button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.item-control span {
  min-width: 20px;
  text-align: center;
  font-weight: 900;
}

/* ===== EMPTY CART ===== */
.empty-cart {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 44px 12px;
  border-radius: 14px;
  border: 1px dashed rgba(148, 163, 184, 0.55);
  background: rgba(248, 250, 252, 0.65);
}

.empty-icon {
  width: 44px;
  height: 44px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  background: #fff;
  border: 1px solid rgba(226, 232, 240, 0.95);
  font-size: 20px;
}

.empty-text b {
  font-size: 14px;
}

.empty-small {
  font-size: 13px;
  color: #64748b;
  text-align: center;
  padding: 12px 0;
}

/* ===== PAYMENT CARD ===== */
.payment-card {
  margin-top: auto;
}

.pay-body .row {
  display: grid;
  grid-template-columns: 1fr 160px;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(226, 232, 240, 0.75);
  font-size: 13px;
}

.pay-body .row:last-of-type {
  border-bottom: none;
}

.total-row {
  border-bottom: none !important;
  padding-top: 14px !important;
  margin-top: 6px;
  border-top: 2px dashed rgba(226, 232, 240, 0.95);
}

.ship-input {
  width: 100%;
  text-align: right;
  font-weight: 900;
  color: #dc2626;
}

.btn-pay {
  width: 100%;
  padding: 12px 14px;
  margin-top: 12px;
  border: none;
  border-radius: 14px;
  cursor: pointer;
  font-weight: 900;
  font-size: 14px;
  color: #fff;
  background: linear-gradient(180deg, #16a34a, #15803d);
  box-shadow: 0 12px 22px rgba(22, 163, 74, 0.22);
  transition: transform .12s ease, filter .12s ease;
}

.btn-pay:hover {
  transform: translateY(-1px);
  filter: brightness(1.02);
}

.pay-note {
  margin-top: 10px;
  font-size: 12px;
}

/* ===== REMOVE BUTTON ===== */
.btn-remove {
  width: 28px;
  height: 28px;
  border-radius: 999px;
  border: 1px solid rgba(254, 202, 202, 0.95);
  background: rgba(255, 245, 245, 0.95);
  color: #dc2626;
  font-size: 16px;
  font-weight: 900;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: all .12s ease;
}

.btn-remove:hover {
  background: #dc2626;
  color: #fff;
  border-color: #dc2626;
}

/* ===== MODAL ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(2, 6, 23, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-content,
.payment-modal {
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 18px;
  box-shadow: 0 26px 60px rgba(2, 6, 23, 0.25);
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content {
  padding: 16px;
  width: 560px;
}

.modal-content.large {
  width: 900px;
  max-width: 95vw;
}

.modal-content.discount-modal {
  width: 980px;
  max-width: 95vw;
}

.modal-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.modal-header-flex h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 900;
}

.close-btn {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: #fff;
  cursor: pointer;
  font-size: 20px;
  font-weight: 900;
  display: grid;
  place-items: center;
}

.search-input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  margin-bottom: 12px;
  outline: none;
}

.modal-table-wrapper {
  max-height: 55vh;
  overflow-y: auto;
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.9);
  background: rgba(255, 255, 255, 0.7);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  font-size: 13px;
}

.pagination button {
  padding: 8px 12px;
  border-radius: 12px;
  border: 1px solid rgba(226, 232, 240, 0.95);
  background: #fff;
  cursor: pointer;
  font-weight: 800;
}

.pagination button:disabled {
  opacity: .5;
  cursor: not-allowed;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
}

/* ===== PRICE RANGE ===== */
.price-range {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.price-range span {
  font-size: 13px;
  font-weight: 900;
  color: #334155;
}

.price-range input {
  width: 160px;
  padding: 9px 10px;
  border-radius: 14px;
  border: 1px solid rgba(226, 232, 240, 0.95);
}

/* ===== MODAL TABLE ===== */
.modal-table {
  table-layout: fixed;
}

.small-table {
  table-layout: fixed;
  font-size: 12px;
}

.small-table th,
.small-table td {
  padding: 8px 6px;
  font-size: 12px;
}

/* select button */
.btn-select {
  background: linear-gradient(180deg, #2563eb, #1d4ed8);
  color: #fff;
  border: none;
  padding: 8px 10px;
  border-radius: 12px;
  font-weight: 900;
  cursor: pointer;
}

/* ===== PAYMENT MODAL ===== */
.payment-modal {
  width: 440px;
  max-width: 95vw;
  padding: 16px;
  animation: pop .18s ease;
}

@keyframes pop {
  from {
    transform: translateY(6px) scale(.98);
    opacity: 0;
  }

  to {
    transform: translateY(0) scale(1);
    opacity: 1;
  }
}

.payment-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.payment-tabs .tab-item {
  flex: 1;
  text-align: center;
}

.qr-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.qr-code img {
  width: 220px;
  height: 220px;
  object-fit: contain;
}

.payment-footer {
  margin-top: 14px;
}

.order-type-toggle {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  font-weight: 600;
}

.order-type-toggle span {
  color: #9ca3af;
}

.order-type-toggle span.active {
  color: #2563eb;
}

/* SWITCH */
.switch {
  position: relative;
  display: inline-block;
  width: 50px;
  height: 26px;
}

.switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  cursor: pointer;
  inset: 0;
  background: #d1d5db;
  border-radius: 20px;
  transition: 0.3s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 3px;
  bottom: 3px;
  background: white;
  border-radius: 50%;
  transition: 0.3s;
}

.switch input:checked+.slider {
  background: #2563eb;
}

.switch input:checked+.slider:before {
  transform: translateX(24px);
}
</style>