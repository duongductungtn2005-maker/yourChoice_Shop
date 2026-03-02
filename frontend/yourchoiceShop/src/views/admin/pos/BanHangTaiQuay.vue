<template>
  <button class="btn-add" @click="createNewTab">
    + Tạo đơn hàng
  </button>

  <!-- TAB BAR -->
  <div v-if="orderTabs.length" class="order-tabs">
    <div v-for="tab in orderTabs" :key="tab.id" class="order-tab"
      :class="{ active: tab.id === activeTabId, disabled: showModal }" @click="!showModal && (activeTabId = tab.id)">
      {{ tab.maHoaDon || 'Đơn mới' }}
      <span class="close-tab" @click.stop="closeTab(tab.id)">×</span>
    </div>
  </div>

  <!-- 🔥 TOÀN BỘ POS CHỈ HIỆN KHI CÓ TAB -->
  <div v-if="currentOrder" class="pos-main-container">
    <!-- ORDER TYPE TABS - HORIZONTAL -->
    <div class="order-type-tabs">
      <div class="tab-item" :class="{ active: orderType === 'TAI_QUAY' }" @click="orderType = 'TAI_QUAY'">
        Tại quầy
      </div>

      <div class="tab-item" :class="{ active: orderType === 'ONLINE' }" @click="orderType = 'ONLINE'">
        Online
      </div>
    </div>

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
    <!-- ===== CUSTOMER ===== -->
    <div class="customer-box" style="margin-top: 16px">
      <div class="box-header">
        <h4>Khách hàng</h4>
        <button class="btn-outline" @click="openCustomerModal">Chọn</button>
      </div>

      <input v-model="customer.name" placeholder="Tên khách hàng" />
      <input v-model="customer.phone" placeholder="SĐT" />
      <input v-model="customer.email" placeholder="Email khách hàng" />
    </div>


    <div v-if="orderType === 'ONLINE'" class="customer-box">
      <h4>Thông tin người nhận</h4>

      <input v-model="customer.name" placeholder="Tên người nhận" />
      <input v-model="customer.phone" placeholder="SĐT người nhận" />
      <input v-model="customer.address" placeholder="Địa chỉ nhận hàng" />
      <textarea v-model="note" placeholder="Ghi chú"></textarea>
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

    <!-- ===== STAFF ===== -->
    <div class="customer-box">
      <div class="box-header">
        <h4>Nhân viên</h4>
        <button class="btn-outline" @click="openStaffModal">Chọn</button>
      </div>

      <input :value="staff.code" placeholder="Mã nhân viên" disabled />
      <input :value="staff.name" placeholder="Tên nhân viên" disabled />
    </div>

    <!-- PAYMENT -->
    <div class="payment-box">
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
        <span class="price-col">{{ formatMoney(totalPrice) }}</span>
      </div>

      <button v-if="orderType === 'TAI_QUAY'" class="btn-pay" @click="openPaymentModal">
        THANH TOÁN
      </button>

      <button v-else class="btn-pay" @click="handleCreateOrderOnline">
        TẠO HÓA ĐƠN
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
              {{ d.type === 'percent'
                ? d.value + '%'
                : formatMoney(d.value)
              }}
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

      <!-- CASH -->
      <div v-if="paymentMethod === 'CASH'">
        <input v-model.number="customerCash" type="number" class="search-input" placeholder="Tiền khách đưa" />
        <div class="p-price">
          Còn lại: {{ formatMoney(calculateRemaining) }}
        </div>
      </div>

      <!-- TRANSFER -->
      <div v-if="paymentMethod === 'TRANSFER'" class="qr-section">
        <div class="qr-code">
          <img :src="qrImageUrl" alt="QR chuyển khoản" />
        </div>

        <div class="p-price">
          Số tiền: {{ formatMoney(totalPrice) }}
        </div>

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
            <td>{{ s.code }}</td>
            <td>{{ s.name }}</td>
            <td>
              <button class="btn-select" @click="selectStaff(s)">
                Chọn
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="pagination">
        <button @click="staffPage--" :disabled="staffPage === 0">
          ‹ Trước
        </button>

        <span>Trang {{ staffPage + 1 }} / {{ totalStaffPages }}</span>

        <button @click="staffPage++" :disabled="staffPage + 1 >= totalStaffPages">
          Sau ›
        </button>
      </div>
    </div>
  </div>

  <!-- GỢI Ý KHI CHƯA CÓ ĐƠN -->
  <div v-if="orderTabs.length === 0" class="empty-pos">
    <p>Chưa có đơn hàng nào</p>
    <p>Nhấn <b>“+ Tạo đơn hàng”</b> để bắt đầu</p>
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

/* ================= CUSTOMER ================= */
// const customer = ref({
//   name: '',
//   phone: '',
//   email: '',
//   address: ''
// })

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
// const cart = ref([])

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
// const discounts = ref([])
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

    // Đóng tab hiện tại và xóa lưu trữ
    const tabIndex = orderTabs.value.findIndex(t => t.id === activeTabId.value)
    if (tabIndex !== -1) {
      orderTabs.value.splice(tabIndex, 1)
      // Update active tab
      activeTabId.value = orderTabs.value.length > 0 ? orderTabs.value[0].id : null
    }

    // Nếu không còn tab nào, xóa localStorage
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
  const bank = '970422' // MB Bank ví dụ
  const account = '123456789'
  const name = 'NGUYEN VAN A'
  const amount = totalPrice.value

  return `https://img.vietqr.io/image/${bank}-${account}-compact.png?amount=${amount}&addInfo=THANH TOAN HOA DON&accountName=${encodeURIComponent(name)}`
})

/* ================= STAFF ================= */
// const staff = ref({
//   id: null,
//   code: '',
//   name: ''
// })
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

  console.log('NHAN VIEN API:', res.data)

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

// const orderType = ref('TAI_QUAY') // TAI_QUAY | ONLINE
// const note = ref('')
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

    // ✅ THÊM DÒNG NÀY
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

  // Đóng tab hiện tại
  const tabIndex = orderTabs.value.findIndex(t => t.id === activeTabId.value)
  if (tabIndex !== -1) {
    orderTabs.value.splice(tabIndex, 1)
    activeTabId.value = orderTabs.value.length > 0 ? orderTabs.value[0].id : null
  }

  // Nếu không còn tab nào, xóa localStorage
  if (orderTabs.value.length === 0) {
    clearOrderTabs()
  }

  router.push({ name: 'admin-order-list' })

}
// const shippingFee = ref(0)

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

// Load tabs khi component mount
onMounted(() => {
  loadOrderTabs()
})

// Save tabs whenever they change
watch(orderTabs, saveOrderTabs, { deep: true })
watch(activeTabId, saveOrderTabs)

// Load data whenever pagination changes
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

const orderType = computed({
  get: () => currentOrder.value?.orderType || 'TAI_QUAY',
  set: v => currentOrder.value && (currentOrder.value.orderType = v)
})
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

.btn-pay {
  width: 100%;
  padding: 12px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  margin-top: 12px;
  transition: background 0.2s ease;
}

.btn-pay:hover {
  background: #1d4ed8;
}

.payment-tabs {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: white;
  border-bottom: 1px solid #e5e7eb;
}

.payment-tabs .tab-item {
  flex: none;
  padding: 8px 20px;
  border-radius: 999px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  font-weight: 600;
  cursor: pointer;
}

.payment-tabs .tab-item.active {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
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
  padding: 0;
  border-top: none;
  position: static;
  box-shadow: none;
  padding: 16px;
}

.discount-box {
  background: white;
  padding: 0;
}

.discount-box h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.customer-box {
  padding: 0;
  border-top: none;
  background: white;
}

.customer-box h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.customer-box input,
.customer-box textarea {
  width: 100%;
  margin-bottom: 8px;
  padding: 8px;
  box-sizing: border-box;
  border: 1px solid #dbeafe;
  border-radius: 6px;
  font-size: 13px;
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
  padding: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  background: #f8fafc;
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
  background: #f8fafc;
  padding: 0;
  border-left: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 0;
  overflow-y: auto;
  height: 100%;
}

.pos-info .discount-box,
.pos-info .customer-box,
.pos-info .payment-box {
  padding: 14px 12px;
  border-bottom: 1px solid #e5e7eb;
  background: white;
  margin-bottom: 1px;
}

.pos-info .payment-box {
  margin-top: auto;
  border-bottom: none;
}

.box-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.box-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.payment-box .row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f3f4f6;
  font-size: 13px;
}

.payment-box .row:last-of-type {
  border-bottom: none;
}

.payment-box .total-row {
  font-size: 15px;
  font-weight: bold;
  padding: 12px 0;
  margin-top: 8px;
  border-top: 2px solid #e5e7eb;
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
.pos-main-container {
  display: grid;
  grid-template-columns: 1fr 420px;
  grid-template-rows: auto 1fr;
  gap: 0;
  height: calc(100vh - 100px);
  background: #ffffff;
}

.pos-main-container > .order-type-tabs {
  grid-column: 1 / -1;
  grid-row: 1;
}

.pos-main-container > .pos-cart {
  grid-column: 1;
  grid-row: 2;
}

.pos-main-container > .pos-info {
  grid-column: 2;
  grid-row: 2;
}

.pos-container {
  display: grid;
  grid-template-columns: 1fr 480px;
  min-height: calc(100vh - 60px);
  /* nếu có header */
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
  flex-shrink: 0;
}

.cart-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
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
  border-radius: 0;
  box-shadow: none;
}

.discount-box h4,
.customer-box h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.box-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.box-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.customer-box input,
.customer-box textarea {
  width: 100%;
  padding: 8px;
  margin-bottom: 6px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  box-sizing: border-box;
}

/* PAYMENT */
.payment-box {
  margin-top: auto;
  padding: 14px;
  border-top: 1px dashed #e5e7eb;
  padding-top: 14px;
  background: white;
}

.payment-box .row {
  display: grid;
  grid-template-columns: 1fr 120px;
  /* 👈 cột tiền cố định */
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.payment-box .total-row {
  font-size: 14px;
  font-weight: 700;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e5e7eb;
}

/* ================= BUTTON ================= */
.btn-add {
  background: #2563eb;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
}

.btn-outline {
  background: white;
  border: 1px solid #2563eb;
  color: #2563eb;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}

.btn-pay {
  width: 100%;
  padding: 12px;
  margin-top: 12px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s ease;
}

.btn-pay:hover {
  background: #1d4ed8;
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
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 1px solid #fecaca;
  background: #fff5f5;
  color: #dc2626;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
  transition: all .2s;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
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
  margin: 12px 0 8px 0;
  font-size: 12px;
}

.small-table th,
.small-table td {
  padding: 6px 4px;
  font-size: 12px;
  vertical-align: top;
  word-break: break-word;
  overflow: hidden;
}

.small-table th:nth-child(1),
.small-table td:nth-child(1) {
  width: 40%;
  max-width: 90px;
}

.small-table th:nth-child(2),
.small-table td:nth-child(2) {
  width: 25%;
  text-align: right;
}

.small-table th:nth-child(3),
.small-table td:nth-child(3) {
  width: 28%;
  font-size: 11px;
  max-width: 85px;
}

.small-table th:nth-child(4),
.small-table td:nth-child(4) {
  width: 7%;
  text-align: center;
  padding: 4px 2px;
}

.small-table thead {
  background: #f3f4f6;
  border-bottom: 1px solid #e5e7eb;
}

.empty-small {
  font-size: 13px;
  color: #9ca3af;
  text-align: center;
  padding: 12px 0;
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
  width: 900px;
  /* 👈 rộng hẳn ra */
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

.ship-input.inline {
  width: 120px;
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  text-align: right;
  font-weight: 600;
  color: #dc2626;
}

.price-col {
  text-align: right;
  font-weight: 600;
  color: #dc2626;
}

.ship-input {
  width: 100%;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  padding: 4px 8px;
  text-align: right;
  font-weight: 600;
  color: #dc2626;
}

/* ===== ORDER TABS ===== */
.order-tabs {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: #f8fafc;
  border-bottom: 1px solid #e5e7eb;
  flex-wrap: wrap;
  align-items: center;
}

.order-tab {
  padding: 8px 14px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.order-tab:hover {
  border-color: #2563eb;
  background: #eff6ff;
}

.order-tab.active {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.close-tab {
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  padding: 0 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.order-tab.disabled {
  opacity: 0.5;
  pointer-events: none;
}

.order-type-tabs {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
  flex-wrap: wrap;
}

.order-type-tabs .tab-item {
  flex: none;
  padding: 10px 24px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.order-type-tabs .tab-item.active {
  background: #2563eb;
  color: white;
  border-color: #2563eb;
}

.payment-modal .tab-item {
  flex: 1;
}
</style>
