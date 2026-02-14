<template>
  <!-- PANEL PHẢI -->
  <div class="right-panel">

    <!-- CART -->
    <div class="cart-header">
      <h3>Sản phẩm trong hóa đơn</h3>
      <button class="btn-add" @click="showModal = true">+ Thêm sản phẩm</button>
    </div>

    <div class="cart-items">
  <div v-if="cart.length === 0" class="empty-cart">
    Chưa có sản phẩm nào
  </div>

  <table v-else class="table">
    <thead>
      <tr>
        <th>Mã SP</th>
        <th>Tên SP</th>
        <th>Thương hiệu</th>
        <th>Chất liệu</th>
        <th>Giá bán</th>
        <th>Số lượng</th>
        <th>Thành tiền</th>
        <th></th>
      </tr>
    </thead>

    <tbody>
      <tr v-for="(item, index) in cart" :key="item.id">
        <td>{{ item.code }}</td>
        <td>{{ item.name }}</td>
        <td>{{ item.brand }}</td>
        <td>{{ item.material }}</td>
        <td class="p-price">{{ formatMoney(item.price) }}</td>

        <td>
          <div class="item-control">
            <button @click="item.qty--" :disabled="item.qty === 1">-</button>
            <span>{{ item.qty }}</span>
            <button @click="item.qty++" :disabled="item.qty === item.tonKho">+</button>
          </div>
        </td>

        <td class="p-price">
          {{ formatMoney(item.price * item.qty) }}
        </td>

        <td>
          <button class="btn-remove" @click="cart.splice(index, 1)">×</button>
        </td>
      </tr>
    </tbody>
  </table>
</div>


    <!-- DISCOUNT -->
    <div class="discount-box">
      <div class="discount-header">
        <h4>Giảm giá</h4>
        <button class="btn-add" @click="showDiscountModal = true">
          + Thêm giảm giá
        </button>
      </div>

      <div v-if="discounts.length === 0" class="empty-cart">
        Chưa áp dụng giảm giá
      </div>

      <div v-for="(d, i) in discounts" :key="d.id" class="discount-item">
        <span>{{ d.code }}</span>
        <span class="discount-value">
          -{{ formatMoney(d.type === 'percent'
            ? totalProductPrice * d.value / 100
            : d.value) }}
        </span>
        <button @click="discounts.splice(i, 1)">×</button>
      </div>
    </div>

    <!-- CUSTOMER -->
    <div class="customer-box">
      <div class="customer-header">
        <h4>Thông tin khách hàng</h4>
        <button class="btn-outline" @click="showCustomerModal = true">
          Chọn tài khoản
        </button>
      </div>

      <input v-model="customer.name" placeholder="Tên khách hàng" />
      <input v-model="customer.phone" placeholder="Số điện thoại" />
      <input v-model="customer.email" placeholder="Email" />
      <textarea v-model="customer.address" placeholder="Địa chỉ"></textarea>
    </div>

    <!-- PAYMENT (sticky bottom) -->
    <div class="payment-box">
      <div class="row">
        <span>Tổng sản phẩm:</span>
        <span>{{ formatMoney(totalProductPrice) }}</span>
      </div>

      <div class="row">
        <span>Giảm giá:</span>
        <span class="discount-value">
          -{{ formatMoney(totalDiscount) }}
        </span>
      </div>

      <div class="row total-row">
        <span>Khách cần trả:</span>
        <span class="total-price">
          {{ formatMoney(totalPrice) }}
        </span>
      </div>

      <button class="btn-pay" @click="handlePayment">
        THANH TOÁN ({{ formatMoney(totalPrice) }})
      </button>
    </div>

  </div>
  <!-- MODAL SẢN PHẨM -->
<div v-if="showModal" class="modal">
  <div class="modal-content large">
    <h3>Chọn sản phẩm</h3>

    <!-- FILTER -->
    <div class="filter-bar">
      <input
        v-model="productKeyword"
        class="search-input"
        placeholder="Tìm theo mã / tên sản phẩm"
      />

      <div class="price-filter">
        <span>{{ formatMoney(priceRange[0]) }}</span>
        <input
          type="range"
          min="0"
          max="1000000"
          step="50000"
          v-model.number="priceRange[1]"
        />
        <span>{{ formatMoney(priceRange[1]) }}</span>
      </div>
    </div>

    <!-- TABLE -->
    <table class="table">
      <thead>
        <tr>
          <th></th>
          <th>Mã SP</th>
          <th>Tên SP</th>
          <th>Thương hiệu</th>
          <th>Chất liệu</th>
          <th>Giá bán</th>
          <th>Tồn</th>
          <th>Số lượng</th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="p in filteredProducts" :key="p.id">
          <td>
            <input type="checkbox" v-model="p.checked" />
          </td>
          <td>{{ p.code }}</td>
          <td>{{ p.name }}</td>
          <td>{{ p.brand }}</td>
          <td>{{ p.material }}</td>
          <td class="p-price">{{ formatMoney(p.price) }}</td>
          <td>{{ p.tonKho }}</td>
          <td>
            <input
              type="number"
              v-model.number="p.qty"
              min="1"
              :max="p.tonKho"
              :disabled="!p.checked"
            />
          </td>
        </tr>
      </tbody>
    </table>

    <div class="modal-actions">
      <button class="btn-primary" @click="confirmAddProduct">Thêm</button>
      <button class="btn-cancel" @click="showModal = false">Hủy</button>
    </div>
  </div>
</div>

<!-- MODAL GIẢM GIÁ -->
<div v-if="showDiscountModal" class="modal">
  <div class="modal-content large">
    <h3>Chọn mã giảm giá</h3>

    <!-- SEARCH -->
    <input
      v-model="discountKeyword"
      class="search-input"
      placeholder="Tìm theo mã hoặc tên chương trình"
    />

    <table class="table">
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
          <td>
            <input type="checkbox" v-model="d.checked" />
          </td>
          <td>{{ d.code }}</td>
          <td>{{ d.name }}</td>
          <td class="p-price">
            {{ d.type === 'percent'
              ? `-${d.value}%`
              : formatMoney(d.value) }}
          </td>
          <td>  
            {{ d.startDate }} <br />
            {{ d.endDate }}
          </td>
        </tr>
      </tbody>
    </table>

    <div class="modal-actions">
      <button class="btn-primary" @click="confirmAddDiscount">
        Áp dụng
      </button>
      <button class="btn-cancel" @click="showDiscountModal = false">
        Hủy
      </button>
    </div>
  </div>
</div>

<!-- MODAL KHÁCH HÀNG -->
<div v-if="showCustomerModal" class="modal">
  <div class="modal-content large">
    <h3>Chọn khách hàng</h3>

    <input
      v-model="customerKeyword"
      class="search-input"
      placeholder="Tìm tên, SĐT, email..."
    />

    <table class="table">
      <thead>
        <tr>
          <th>Mã KH</th>
          <th>Họ tên</th>
          <th>SĐT</th>
          <th>Email</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="c in filteredCustomers" :key="c.id">
          <td>{{ c.code }}</td>
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

    <div class="modal-actions">
      <button class="btn-cancel" @click="showCustomerModal = false">
        Đóng
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

const customers = ref([
  {
    id: 1,
    code: 'KH001',
    name: 'Nguyễn Văn A',
    phone: '0912345678',
    email: 'a@gmail.com',
    address: 'Hà Nội'
  },
  {
    id: 2,
    code: 'KH002',
    name: 'Marika',
    phone: '1000000000',
    email: 'marika@gmail.com',
    address: ''
  }
])

const filteredCustomers = computed(() =>
  customers.value.filter(c =>
    [c.name, c.phone, c.email]
      .join(' ')
      .toLowerCase()
      .includes(customerKeyword.value.toLowerCase())
  )
)

const selectCustomer = (c) => {
  customer.value = {
    name: c.name,
    phone: c.phone,
    email: c.email,
    address: c.address
  }
  showCustomerModal.value = false
}

/* ================= CART ================= */
const cart = ref([])

/* ================= MODAL SẢN PHẨM ================= */
const showModal = ref(false)

const products = ref([
  {
    id: 1,
    code: 'SP001',
    name: 'Giày Sneaker A',
    brand: 'Nike',
    material: 'Da',
    price: 250000,
    tonKho: 10,
    checked: false,
    qty: 1
  },
  {
    id: 2,
    code: 'SP002',
    name: 'Giày Sneaker B',
    brand: 'Adidas',
    material: 'Vải',
    price: 300000,
    tonKho: 5,
    checked: false,
    qty: 1
  },
  {
    id: 3,
    code: 'SP003',
    name: 'Giày Sneaker C',
    brand: 'Puma',
    material: 'Canvas',
    price: 400000,
    tonKho: 8,
    checked: false,
    qty: 1
  }
])


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


const filteredDiscounts = computed(() =>
  discountList.value.filter(d =>
    [d.code, d.name]
      .join(' ')
      .toLowerCase()
      .includes(discountKeyword.value.toLowerCase())
  )
)


const discountList = ref([
  {
    id: 1,
    code: 'GG50K',
    name: 'Giảm 50.000 cho đơn hàng',
    type: 'money',
    value: 50000,
    startDate: '2024-01-01',
    endDate: '2026-12-31',
    checked: false
  },
  {
    id: 2,
    code: 'GG10PT',
    name: 'Giảm 10% toàn bộ sản phẩm',
    type: 'percent',
    value: 10,
    startDate: '2024-01-01',
    endDate: '2030-12-31',
    checked: false
  }
])


const confirmAddDiscount = () => {
  discountList.value.forEach(d => {
    if (d.checked && !discounts.value.find(x => x.id === d.id)) {
      discounts.value.push({
        id: d.id,
        code: d.code,
        type: d.type,
        value: d.value
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

const totalDiscount = computed(() =>
  discounts.value.reduce((sum, d) => {
    if (d.type === 'percent') {
      return sum + totalProductPrice.value * d.value / 100
    }
    return sum + d.value
  }, 0)
)

const totalPrice = computed(() =>
  Math.max(totalProductPrice.value - totalDiscount.value, 0)
)

/* ================= THANH TOÁN ================= */
const handlePayment = async () => {
  if (!cart.value.length) {
    alert('Giỏ hàng đang trống!')
    return
  }

  if (!customer.value.name || !customer.value.phone) {
    alert('Vui lòng nhập tên và số điện thoại!')
    return
  }

  if (!confirm(`Xác nhận thanh toán ${formatMoney(totalPrice.value)}?`)) return

  try {
    const payload = {
      tenKhachHang: customer.value.name,
      soDienThoai: customer.value.phone,
      diaChi: customer.value.address,
      items: cart.value.map(i => ({
        idSanPham: i.id,
        soLuong: i.qty,
        donGia: i.price
      }))
    }

    await createOrder(payload)

    alert('Thanh toán thành công!')
    cart.value = []
    customer.value = { name: '', phone: '', email: '', address: '' }

    router.push({ name: 'admin-order-list' })
  } catch (err) {
    console.error(err)
    alert('Lỗi thanh toán!')
  }
}

/* ================= FORMAT ================= */
const formatMoney = (val) =>
  new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(val)
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

.table {
  width: 100%;
  border-collapse: collapse;
}

.table th {
  background: #eff6ff;
  color: #1d4ed8;
  padding: 10px;
  text-align: left;
}

.table td {
  padding: 10px;
  border-bottom: 1px solid #e5e7eb;
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

.pos-container {
  display: flex;
  height: 100vh;
  background: #f3f4f6;
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

.cart-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
}

.cart-items {
  padding: 20px;
  overflow-y: auto;
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

.btn-pay {
  width: 100%;
  padding: 15px;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
}

.btn-add {
  background: #16a34a;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
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
</style>
