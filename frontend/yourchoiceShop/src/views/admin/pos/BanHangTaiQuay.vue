<template>
  <div class="pos-container">
    <!-- ===== LEFT: CART ===== -->
    <div class="pos-cart">
      <div class="cart-header">
        <h3>Sản phẩm trong hóa đơn</h3>
        <button class="btn-add" @click="openProductModal">+ Thêm sản phẩm</button>
      </div>

      <div class="cart-items">
        <div v-if="cart.length === 0" class="empty-cart">
          Chưa có sản phẩm nào
        </div>

        <table v-else class="table">
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
              <td>{{ item.code }}</td>
              <td>{{ item.name }}</td>
              <td class="p-price">{{ formatMoney(item.price) }}</td>
              <td>
                <div class="item-control">
                  <button @click="item.qty--" :disabled="item.qty === 1">-</button>
                  <span>{{ item.qty }}</span>
                  <button @click="item.qty++">+</button>
                </div>
              </td>
              <td class="p-price">
                {{ formatMoney(item.price * item.qty) }}
              </td>
              <td>
                <button class="btn-remove" @click="cart.splice(i, 1)">×</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- ===== RIGHT: INFO BAR ===== -->
    <div class="pos-info">
      <!-- DISCOUNT -->
      <div class="discount-box">
        <div class="box-header">
          <h4>Giảm giá</h4>
          <button class="btn-add" @click="openDiscountModal">+ Thêm</button>
        </div>

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
              <td>{{ d.name }}</td>
              <td class="p-price">
                -{{ d.type === 'percent' ? d.value + '%' : formatMoney(d.value) }}
              </td>
              <td>{{ d.startDate }} → {{ d.endDate }}</td>
              <td>
                <button class="btn-remove" @click="discounts.splice(i, 1)">×</button>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="discounts.length === 0" class="empty-small">
          Chưa áp dụng
        </div>
      </div>

      <!-- CUSTOMER -->
      <div class="customer-box">
        <div class="box-header">
          <h4>Khách hàng</h4>
          <button class="btn-outline" @click="openCustomerModal">Chọn</button>
        </div>
        <input v-model="customer.name" placeholder="Tên KH" />
        <input v-model="customer.phone" placeholder="SĐT" />
      </div>

      <!-- PAYMENT -->
      <div class="payment-box">
        <div class="row">
          <span>Tổng sản phẩm</span>
          <span>{{ formatMoney(totalProductPrice) }}</span>
        </div>

        <div class="row">
          <span>Giảm giá</span>
          <span class="p-price">-{{ formatMoney(totalDiscount) }}</span>
        </div>

        <div class="row total-row">
          <span>Khách cần trả</span>
          <span class="p-price">{{ formatMoney(totalPrice) }}</span>
        </div>

        <button class="btn-pay" @click="openPaymentModal">
          THANH TOÁN
        </button>
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
            <td>{{ p.code }}</td>
            <td>{{ p.name }}</td>
            <td class="p-price">{{ formatMoney(p.price) }}</td>
            <td>{{ p.tonKho }}</td>
            <td>
              <div class="item-control">
                <button @click="p.qty--" :disabled="p.qty === 1">-</button>
                <span>{{ p.qty }}</span>
                <button @click="p.qty++" :disabled="p.qty >= p.tonKho">+</button>
              </div>
            </td>
            <td><input type="checkbox" v-model="p.checked" /></td>
          </tr>
        </tbody>
      </table>
      <div class="pagination">
        <button @click="productPage--" :disabled="productPage === 0">
          ‹ Trước
        </button>

        <span>Trang {{ productPage + 1 }} / {{ totalProductPages }}</span>

        <button @click="productPage++" :disabled="productPage + 1 >= totalProductPages">
          Sau ›
        </button>
      </div>
      <div class="modal-actions">
        <button class="btn-outline" @click="showModal = false">Hủy</button>
        <button class="btn-add" @click="confirmAddProduct">Thêm</button>
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
            <td>{{ c.name }}</td>
            <td>{{ c.phone }}</td>
            <td>{{ c.email }}</td>
            <td>
              <button class="btn-select" @click="selectCustomer(c)">
                Chọn
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pagination">
        <button @click="customerPage--" :disabled="customerPage === 0">
          ‹ Trước
        </button>

        <span>Trang {{ customerPage + 1 }} / {{ totalCustomerPages }}</span>

        <button @click="customerPage++" :disabled="customerPage + 1 >= totalCustomerPages">
          Sau ›
        </button>
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
            <td>{{ d.code }}</td>
            <td>{{ d.name }}</td>
            <td class="p-price">
              {{ d.type === 'percent' ? d.value + '%' : formatMoney(d.value) }}
            </td>
            <td>
              {{ d.startDate }} → {{ d.endDate }}
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pagination">
        <button @click="discountPage--" :disabled="discountPage === 0">
          ‹ Trước
        </button>

        <span>Trang {{ discountPage + 1 }} / {{ totalDiscountPages }}</span>

        <button @click="discountPage++" :disabled="discountPage + 1 >= totalDiscountPages">
          Sau ›
        </button>
      </div>
      <div class="modal-actions">
        <button class="btn-outline" @click="showDiscountModal = false">Hủy</button>
        <button class="btn-add" @click="confirmAddDiscount">Áp dụng</button>
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
        <div class="p-price">
          Còn lại: {{ formatMoney(calculateRemaining) }}
        </div>
      </div>

      <div class="payment-footer">
        <button class="btn-submit-payment" @click="confirmCreateOrder">
          Xác nhận thanh toán
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { createOrder } from '@/api/HoaDonApi'
/* ================= FILTER SẢN PHẨM ================= */
const productKeyword = ref('')
const priceRange = ref([0, 1000000])


/* ================= ROUTER ================= */
const router = useRouter()

/* ================= CUSTOMER ================= */
const customer = ref({
  name: '',
  phone: '',
  email: '',
  address: ''
})

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

/* ================= CART ================= */
const cart = ref([])

/* ================= MODAL SẢN PHẨM ================= */
const showModal = ref(false)

import { onMounted } from 'vue'
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
      brand: p.thuongHieu?.tenThuongHieu || '—', // 👈 FIX LỖI THƯƠNG HIỆU
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
const discounts = ref([])
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
    type: d.loaiPhieu === 'PERCENT' ? 'percent' : 'money',
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
  Math.max(totalProductPrice.value - totalDiscount.value, 0)
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

      tongTien: totalProductPrice.value,
      tienGiamGia: totalDiscount.value,
      tongTienSauGiam: totalPrice.value,

      phieuGiamGiaIds: discounts.value.map(d => d.id),

      items: cart.value.map(i => ({
        idChiTietSanPham: i.id,
        soLuong: i.qty,
        donGia: i.price
      }))
    }

    await createOrder(payload)

    alert('Tạo hóa đơn thành công!')

    cart.value = []
    discounts.value = []
    customer.value = { name: '', phone: '', email: '', address: '' }

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
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
}

.price-filter {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 300px;
}

.customer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.btn-outline {
  background: white;
  border: 1px solid #2563eb;
  color: #2563eb;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
}

.search-input {
  width: 100%;
  padding: 10px;
  margin: 10px 0;
  border-radius: 8px;
  border: 1px solid #dbeafe;
}

.modal-content.large {
  width: 800px;
}

.btn-select {
  background: #2563eb;
  color: white;
  border: none;
  padding: 6px 10px;
  border-radius: 6px;
}

.p-price {
  color: #dc2626;
  font-weight: 600;
  min-width: 110px;
}



.left-panel {
  width: 400px;
  padding: 20px;
  background: #fff;
  border-right: 1px solid #ddd;
}

.section-title {
  margin-bottom: 10px;
}

.customer-form input,
.customer-form textarea {
  width: 100%;
  padding: 10px;
  margin-bottom: 10px;
  border-radius: 6px;
  border: 1px solid #ddd;
}

.right-panel {
  flex: 1;
  background: white;
  display: flex;
  flex-direction: column;
}

.cart-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  border-bottom: 1px dashed #eee;
  padding-bottom: 10px;
}

.item-name {
  flex: 1;
}

.item-control {
  display: flex;
  gap: 8px;
}

.item-price {
  font-weight: bold;
  margin-left: 15px;
}

.customer-box {
  padding: 15px;
  border-top: 1px solid #eee;
}

.customer-box input,
.customer-box textarea {
  width: 100%;
  margin-bottom: 8px;
  padding: 8px;
}

/* MODAL */
.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-content {
  background: white;
  padding: 20px;
  width: 500px;
  border-radius: 8px;
}

.product-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-cancel {
  background: #e5e7eb;
}

.payment-footer {
  margin-top: 15px;
}

.btn-submit-payment {
  width: 100%;
  padding: 12px;
  background: #16a34a;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: bold;
  cursor: pointer;
}

.payment-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.tab-item {
  flex: 1;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  cursor: pointer;
  font-weight: 600;
}

.tab-item.active {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.modal-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
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

.item-control button {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: 1px solid #dbeafe;
  background: #eff6ff;
  cursor: pointer;
}

.item-control span {
  min-width: 24px;
  text-align: center;
  font-weight: 600;
}

.discount-header,
.customer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.payment-box {
  background: white;
  padding: 20px;
  border-top: 1px solid #e5e7eb;
  position: sticky;
  bottom: 0;
  box-shadow: 0 -6px 20px rgba(0, 0, 0, 0.06);
}

.payment-box .row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.total-row {
  font-size: 18px;
  font-weight: bold;
}

/* ===== MODAL THANH TOÁN ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.payment-modal {
  width: 420px;
  max-width: 95%;
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.25);
  animation: pop 0.25s ease;
}

@keyframes pop {
  from {
    transform: scale(0.95);
    opacity: 0;
  }

  to {
    transform: scale(1);
    opacity: 1;
  }
}

.pagination {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 15px;
}

.pagination button {
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #dbeafe;
  background: white;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ===== CART ===== */
.pos-cart {
  padding: 16px;
  overflow-y: auto;
}

/* EMPTY CART */
.empty-cart {
  text-align: center;
  color: #9ca3af;
  padding: 80px 0;
  font-size: 14px;
}

/* ===== RIGHT INFO ===== */
.pos-info {
  background: #ffffff;
  padding: 16px;
  border-left: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* INFO CARD */
.info-box {
  background: white;
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

/* PAYMENT */
.payment-box {
  margin-top: auto;
  position: sticky;
  bottom: 0;
}

/* ===== FIX MODAL ===== */
.modal,
.modal-overlay {
  position: fixed !important;
  inset: 0 !important;
  z-index: 9999 !important;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal {
  background: rgba(0, 0, 0, 0.45);
}

.modal-content,
.payment-modal {
  background: white;
  border-radius: 16px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content.large {
  width: 800px;
  max-width: 95%;
}

/* ================= ROOT ================= */
.pos-container {
  display: grid;
  grid-template-columns: 1fr 480px;
  height: 100vh;
  background: #f3f4f6;
  overflow: hidden;
}

/* ================= LEFT CART ================= */
.pos-cart {
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}

.cart-header {
  padding: 16px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-items {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
}

/* TABLE */
.table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.table th,
.table td {
  text-align: center;
  vertical-align: middle;
  padding: 12px 8px;
  font-size: 14px;
}

.table th:nth-child(1) {
  width: 140px;
}

/* Mã */
.table th:nth-child(3) {
  width: 120px;
}

/* Giá */
.table th:nth-child(4) {
  width: 120px;
}

/* SL */
.table th:nth-child(6) {
  width: 50px;
}

/* X */

.table td:nth-child(2) {
  text-align: left;
}

/* EMPTY */
.empty-cart {
  text-align: center;
  color: #9ca3af;
  padding: 80px 0;
}

/* ================= RIGHT INFO ================= */
.pos-info {
  background: white;
  border-left: 1px solid #e5e7eb;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.discount-box,
.customer-box {
  background: white;
  padding: 14px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, .05);
}

.box-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.customer-box input {
  width: 100%;
  padding: 8px;
  margin-bottom: 6px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

/* PAYMENT */
.payment-box {
  margin-top: auto;
  padding-top: 14px;
  border-top: 1px dashed #e5e7eb;
}

.payment-box .row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.total-row {
  font-size: 18px;
  font-weight: 700;
}

/* ================= BUTTON ================= */
.btn-add {
  background: #2563eb;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.btn-outline {
  background: white;
  border: 1px solid #2563eb;
  color: #2563eb;
  padding: 6px 10px;
  border-radius: 8px;
}

.btn-pay {
  width: 100%;
  padding: 14px;
  margin-top: 10px;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: white;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  border: none;
}

/* ================= PRICE ================= */
.p-price {
  color: #dc2626;
  font-weight: 600;
}

/* ================= ITEM CONTROL ================= */
.item-control {
  display: flex;
  gap: 6px;
  align-items: center;
}

.item-control button {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: 1px solid #dbeafe;
  background: #eff6ff;
  cursor: pointer;
}

.item-control span {
  min-width: 24px;
  text-align: center;
  font-weight: 600;
}

/* ================= MODAL COMMON ================= */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-content {
  background: white;
  padding: 20px;
  width: 520px;
  border-radius: 16px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content.large {
  width: 800px;
  max-width: 95%;
}

.modal-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
}

.search-input {
  width: 100%;
  padding: 10px;
  border-radius: 10px;
  border: 1px solid #dbeafe;
  margin-bottom: 12px;
}

/* PRODUCT ROW */
.product-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
  border-bottom: 1px dashed #e5e7eb;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 14px;
}

/* ================= PAYMENT MODAL ================= */
.payment-modal {
  width: 420px;
  max-width: 95%;
  background: white;
  border-radius: 16px;
  padding: 20px;
  animation: pop .25s ease;
}

@keyframes pop {
  from {
    transform: scale(.95);
    opacity: 0;
  }

  to {
    transform: scale(1);
    opacity: 1;
  }
}

.payment-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}

.tab-item {
  flex: 1;
  padding: 10px;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  cursor: pointer;
  font-weight: 600;
  text-align: center;
}

.tab-item.active {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.btn-submit-payment {
  width: 100%;
  padding: 14px;
  background: #16a34a;
  color: white;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 700;
  border: none;
}

.btn-remove {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid #fecaca;
  background: #fff5f5;
  color: #dc2626;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all .2s;
}

.btn-remove:hover {
  background: #dc2626;
  color: white;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  font-size: 14px;
}

.pagination button {
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid #dbeafe;
  background: white;
  cursor: pointer;
}

.pagination button:disabled {
  opacity: .5;
  cursor: not-allowed;
}

.modal-table {
  width: 100%;
  border-collapse: collapse;
}

.modal-table td,
.modal-table th {
  word-break: break-word;
  white-space: normal;
  text-align: left;
}

.modal-content.product-modal {
  width: 900px;
  max-width: 95vw;
}

.modal-content.normal-modal {
  width: 600px;
  max-width: 95vw;
}

.modal-table-wrapper {
  max-height: 55vh;
  overflow-y: auto;
}

.modal-table td:nth-child(3) {
  max-width: 220px;
}

.small-table {
  width: 100%;
  table-layout: fixed;
}

.small-table th,
.small-table td {
  padding: 8px;
  font-size: 13px;
  vertical-align: top;
}

.small-table th:nth-child(1) {
  width: 30%;
}

.small-table th:nth-child(2) {
  width: 20%;
}

.small-table th:nth-child(3) {
  width: 40%;
}

.small-table th:nth-child(4) {
  width: 10%;
}

.small-table td {
  word-break: break-word;
  white-space: normal;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.price-range span {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.price-range input {
  width: 140px;
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px solid #dbeafe;
  font-size: 14px;
}

/* ===== DISCOUNT MODAL WIDE ===== */
.modal-content.discount-modal {
  width: 900px;        /* 👈 rộng hẳn ra */
  max-width: 95vw;
}

/* ===== DISCOUNT TABLE ===== */
.discount-modal .modal-table {
  table-layout: fixed;
}

.discount-modal .modal-table th,
.discount-modal .modal-table td {
  word-break: break-word;
  white-space: normal;
  vertical-align: top;
}

/* checkbox */
.discount-modal .modal-table th:nth-child(1),
.discount-modal .modal-table td:nth-child(1) {
  width: 40px;
  text-align: center;
}

/* Mã */
.discount-modal .modal-table th:nth-child(2) {
  width: 140px;
}

/* Tên */
.discount-modal .modal-table th:nth-child(3) {
  width: 260px;
}

/* Giá trị */
.discount-modal .modal-table th:nth-child(4) {
  width: 140px;
}

/* Thời hạn */
.discount-modal .modal-table th:nth-child(5) {
  width: 280px;
}
</style>
