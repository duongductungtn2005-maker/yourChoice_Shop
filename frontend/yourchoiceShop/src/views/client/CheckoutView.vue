<template>
  <div class="checkout-page">
    <div class="page-header-small">
      <div class="header-overlay"></div>
      <div class="container header-content">
        <h1>THANH TOÁN</h1>
        <div class="breadcrumb">
          <span @click="$router.push('/')">Trang chủ</span> / 
          <span @click="$router.push('/cart')">Giỏ hàng</span> / 
          <span>Thanh toán</span>
        </div>
      </div>
    </div>

    <div class="container checkout-content">
      <div class="checkout-grid">
        <!-- Cột trái: Form thông tin -->
        <div class="checkout-form">
          <!-- Thông tin khách hàng -->
          <div class="form-section">
            <h3><i class="fas fa-user"></i> Thông tin khách hàng</h3>
            <div class="form-row">
              <div class="form-group">
                <label>Họ tên <span class="required">*</span></label>
                <input type="text" v-model="form.tenKhachHang" placeholder="Nhập họ tên" :class="{ error: errors.tenKhachHang }" />
                <span class="error-msg" v-if="errors.tenKhachHang">{{ errors.tenKhachHang }}</span>
              </div>
            </div>
            <div class="form-row two-col">
              <div class="form-group">
                <label>Email <span class="required">*</span></label>
                <input type="email" v-model="form.email" placeholder="email@example.com" :class="{ error: errors.email }" />
                <span class="error-msg" v-if="errors.email">{{ errors.email }}</span>
              </div>
              <div class="form-group">
                <label>Số điện thoại <span class="required">*</span></label>
                <input type="tel" v-model="form.soDienThoai" placeholder="0912 345 678" :class="{ error: errors.soDienThoai }" />
                <span class="error-msg" v-if="errors.soDienThoai">{{ errors.soDienThoai }}</span>
              </div>
            </div>
          </div>

          <!-- Địa chỉ giao hàng -->
          <div class="form-section">
            <h3><i class="fas fa-map-marker-alt"></i> Địa chỉ giao hàng</h3>

            <!-- Chọn địa chỉ có sẵn -->
            <div v-if="savedAddresses.length > 0" class="saved-addresses">
              <label class="address-option" v-for="addr in savedAddresses" :key="addr.id" :class="{ active: selectedAddressId === addr.id }">
                <input type="radio" name="address" :value="addr.id" v-model="selectedAddressId" @change="fillAddress(addr)" />
                <div class="address-detail">
                  <strong>{{ addr.tenNguoiNhan }}</strong> - {{ addr.soDienThoai }}
                  <p>{{ addr.diaChiCuThe }}, {{ addr.phuong }}, {{ addr.quan }}, {{ addr.thanhPho }}</p>
                  <span class="default-badge" v-if="addr.macDinh">Mặc định</span>
                </div>
              </label>
              <label class="address-option" :class="{ active: selectedAddressId === 'new' }">
                <input type="radio" name="address" value="new" v-model="selectedAddressId" @change="clearAddressForm" />
                <div class="address-detail"><strong>+ Nhập địa chỉ mới</strong></div>
              </label>
            </div>

            <div v-if="selectedAddressId === 'new' || savedAddresses.length === 0" class="address-form">
              <div class="form-row three-col">
                <div class="form-group">
                  <label>Tỉnh/Thành phố <span class="required">*</span></label>
                  <select v-model="form.provinceId" @change="onProvinceChange" :class="{ error: errors.province }">
                    <option :value="null">-- Chọn --</option>
                    <option v-for="p in provinces" :key="p.ProvinceID" :value="p.ProvinceID">{{ p.ProvinceName }}</option>
                  </select>
                  <span class="error-msg" v-if="errors.province">{{ errors.province }}</span>
                </div>
                <div class="form-group">
                  <label>Quận/Huyện <span class="required">*</span></label>
                  <select v-model="form.districtId" @change="onDistrictChange" :class="{ error: errors.district }">
                    <option :value="null">-- Chọn --</option>
                    <option v-for="d in districts" :key="d.DistrictID" :value="d.DistrictID">{{ d.DistrictName }}</option>
                  </select>
                  <span class="error-msg" v-if="errors.district">{{ errors.district }}</span>
                </div>
                <div class="form-group">
                  <label>Phường/Xã <span class="required">*</span></label>
                  <select v-model="form.wardCode" :class="{ error: errors.ward }">
                    <option :value="null">-- Chọn --</option>
                    <option v-for="w in wards" :key="w.WardCode" :value="w.WardCode">{{ w.WardName }}</option>
                  </select>
                  <span class="error-msg" v-if="errors.ward">{{ errors.ward }}</span>
                </div>
              </div>
              <div class="form-group">
                <label>Số nhà, đường <span class="required">*</span></label>
                <input type="text" v-model="form.diaChiCuThe" placeholder="VD: 123 Đường Láng" :class="{ error: errors.diaChiCuThe }" />
                <span class="error-msg" v-if="errors.diaChiCuThe">{{ errors.diaChiCuThe }}</span>
              </div>
            </div>
          </div>

          <!-- Phương thức vận chuyển -->
          <div class="form-section">
            <h3><i class="fas fa-truck"></i> Phương thức vận chuyển</h3>
            <div class="shipping-options">
              <label class="shipping-option" :class="{ active: form.shippingMethod === 'standard' }">
                <input type="radio" value="standard" v-model="form.shippingMethod" />
                <div class="option-info">
                  <span class="option-name">Vận chuyển tiêu chuẩn</span>
                  <span class="option-desc">3-5 ngày | {{ cartStore.totalMoney >= 500000 ? 'MIỄN PHÍ' : formatMoney(30000) }}</span>
                </div>
                <span class="option-price">{{ cartStore.totalMoney >= 500000 ? 'Miễn phí' : formatMoney(30000) }}</span>
              </label>
              <label class="shipping-option" :class="{ active: form.shippingMethod === 'express' }">
                <input type="radio" value="express" v-model="form.shippingMethod" />
                <div class="option-info">
                  <span class="option-name">Vận chuyển nhanh</span>
                  <span class="option-desc">1-2 ngày</span>
                </div>
                <span class="option-price">{{ formatMoney(50000) }}</span>
              </label>
            </div>
          </div>

          <!-- Phương thức thanh toán -->
          <div class="form-section">
            <h3><i class="fas fa-credit-card"></i> Phương thức thanh toán</h3>
            <div class="payment-options">
              <label class="payment-option" :class="{ active: form.paymentMethod === 'COD' }">
                <input type="radio" value="COD" v-model="form.paymentMethod" />
                <i class="fas fa-money-bill-wave"></i>
                <span>Thanh toán khi nhận hàng (COD)</span>
              </label>
              <label class="payment-option" :class="{ active: form.paymentMethod === 'BANKING' }">
                <input type="radio" value="BANKING" v-model="form.paymentMethod" />
                <i class="fas fa-university"></i>
                <span>Chuyển khoản ngân hàng</span>
              </label>
              <label class="payment-option" :class="{ active: form.paymentMethod === 'MOMO' }">
                <input type="radio" value="MOMO" v-model="form.paymentMethod" />
                <i class="fas fa-wallet"></i>
                <span>Ví điện tử MoMo</span>
              </label>
            </div>
          </div>

          <!-- Ghi chú -->
          <div class="form-section">
            <h3><i class="fas fa-edit"></i> Ghi chú</h3>
            <textarea v-model="form.ghiChu" placeholder="Ghi chú cho đơn hàng (không bắt buộc)" rows="3"></textarea>
          </div>
        </div>

        <!-- Cột phải: Tóm tắt đơn hàng -->
        <div class="order-summary">
          <h3>TÓM TẮT ĐƠN HÀNG</h3>
          
          <div class="summary-items">
            <div class="summary-item" v-for="item in cartStore.items" :key="item.variantId">
              <img :src="item.anh" alt="" @error="handleImgError" />
              <div class="s-item-info">
                <h4>{{ item.tenSanPham }}</h4>
                <span>{{ item.mauSac }} / {{ item.kichThuoc }} x {{ item.soLuong }}</span>
              </div>
              <span class="s-item-price">{{ formatMoney(item.donGia * item.soLuong) }}</span>
            </div>
          </div>

          <!-- Voucher -->
          <div class="voucher-section">
            <div class="voucher-input">
              <input type="text" v-model="voucherCode" placeholder="Nhập mã giảm giá" />
              <button @click="applyVoucher" :disabled="!voucherCode">Áp dụng</button>
            </div>
            <div v-if="appliedVoucher" class="voucher-applied">
              <span><i class="fas fa-tag"></i> {{ appliedVoucher.maPhieuGiamGia }} - Giảm {{ formatDiscount(appliedVoucher) }}</span>
              <button @click="removeVoucher" class="btn-remove-voucher"><i class="fas fa-times"></i></button>
            </div>
          </div>

          <div class="summary-calculations">
            <div class="calc-row">
              <span>Tạm tính</span>
              <span>{{ formatMoney(cartStore.totalMoney) }}</span>
            </div>
            <div class="calc-row">
              <span>Phí vận chuyển</span>
              <span>{{ formatMoney(shippingFee) }}</span>
            </div>
            <div class="calc-row" v-if="discountAmount > 0">
              <span>Giảm giá</span>
              <span class="discount-val">-{{ formatMoney(discountAmount) }}</span>
            </div>
            <div class="calc-divider"></div>
            <div class="calc-row total">
              <span>TỔNG CỘNG</span>
              <span class="total-amount">{{ formatMoney(totalPayment) }}</span>
            </div>
          </div>

          <button class="btn-place-order" @click="placeOrder" :disabled="isSubmitting">
            <span v-if="isSubmitting"><i class="fas fa-spinner fa-spin"></i> ĐANG XỬ LÝ...</span>
            <span v-else><i class="fas fa-check-circle"></i> ĐẶT HÀNG</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { createOrderOnline, getProvinces, getDistricts, getWards, getVouchers, getAddresses } from '@/api/clientApi'
import { getCurrentUser, isAuthenticated } from '@/services/auth'
import Swal from 'sweetalert2'

const router = useRouter()
const cartStore = useCartStore()
const isSubmitting = ref(false)

// Address data
const provinces = ref([])
const districts = ref([])
const wards = ref([])
const savedAddresses = ref([])
const selectedAddressId = ref('new')

// Voucher
const voucherCode = ref('')
const appliedVoucher = ref(null)

// Form
const form = reactive({
  tenKhachHang: '', email: '', soDienThoai: '',
  provinceId: null, districtId: null, wardCode: null,
  provinceName: '', districtName: '', wardName: '',
  diaChiCuThe: '',
  shippingMethod: 'standard',
  paymentMethod: 'COD',
  ghiChu: '',
})

const errors = reactive({})

// Calculated prices
const shippingFee = computed(() => {
  if (form.shippingMethod === 'express') return 50000
  return cartStore.totalMoney >= 500000 ? 0 : 30000
})

const discountAmount = computed(() => {
  if (!appliedVoucher.value) return 0
  const v = appliedVoucher.value
  if (v.loaiPhieu === 0) { // Phần trăm
    const discount = cartStore.totalMoney * v.giaTriGiam / 100
    return v.giaTriGiamToiDa ? Math.min(discount, v.giaTriGiamToiDa) : discount
  }
  return v.giaTriGiam || 0
})

const totalPayment = computed(() => {
  return Math.max(0, cartStore.totalMoney + shippingFee.value - discountAmount.value)
})

// Load data
onMounted(async () => {
  if (cartStore.items.length === 0) {
    router.push('/cart')
    return
  }

  // Load provinces
  try {
    const res = await getProvinces()
    provinces.value = res.data?.data || res.data || []
  } catch (e) { console.error('Lỗi tải tỉnh/thành:', e) }

  // Pre-fill customer info if logged in
  const user = getCurrentUser()
  if (user) {
    form.tenKhachHang = user.tenKhachHang || ''
    form.email = user.email || ''
    form.soDienThoai = user.soDienThoai || ''

    // Load saved addresses
    try {
      const res = await getAddresses(user.id)
      savedAddresses.value = res.data || []
      const defaultAddr = savedAddresses.value.find(a => a.macDinh)
      if (defaultAddr) {
        selectedAddressId.value = defaultAddr.id
        fillAddress(defaultAddr)
      }
    } catch (e) { console.error('Lỗi tải địa chỉ:', e) }
  }
})

const onProvinceChange = async () => {
  districts.value = []
  wards.value = []
  form.districtId = null
  form.wardCode = null
  const prov = provinces.value.find(p => p.ProvinceID === form.provinceId)
  form.provinceName = prov ? prov.ProvinceName : ''
  if (!form.provinceId) return
  try {
    const res = await getDistricts(form.provinceId)
    districts.value = res.data?.data || res.data || []
  } catch (e) { console.error(e) }
}

const onDistrictChange = async () => {
  wards.value = []
  form.wardCode = null
  const dist = districts.value.find(d => d.DistrictID === form.districtId)
  form.districtName = dist ? dist.DistrictName : ''
  if (!form.districtId) return
  try {
    const res = await getWards(form.districtId)
    wards.value = res.data?.data || res.data || []
  } catch (e) { console.error(e) }
}

watch(() => form.wardCode, (val) => {
  const w = wards.value.find(x => x.WardCode === val)
  form.wardName = w ? w.WardName : ''
})

const fillAddress = (addr) => {
  form.tenKhachHang = form.tenKhachHang || addr.tenNguoiNhan
  form.soDienThoai = form.soDienThoai || addr.soDienThoai
  form.provinceName = addr.thanhPho || ''
  form.districtName = addr.quan || ''
  form.wardName = addr.phuong || ''
  form.diaChiCuThe = addr.diaChiCuThe || ''
}

const clearAddressForm = () => {
  form.provinceId = null; form.districtId = null; form.wardCode = null
  form.provinceName = ''; form.districtName = ''; form.wardName = ''
  form.diaChiCuThe = ''
}

// Voucher
const applyVoucher = async () => {
  if (!voucherCode.value) return
  try {
    const res = await getVouchers({ keyword: voucherCode.value, status: 1, size: 1 })
    const list = res.data.content || []
    const voucher = list.find(v => v.maPhieuGiamGia === voucherCode.value)
    if (!voucher) {
      Swal.fire('Không tìm thấy', 'Mã giảm giá không hợp lệ hoặc đã hết hạn', 'error')
      return
    }
    if (voucher.donHangToiThieu && cartStore.totalMoney < voucher.donHangToiThieu) {
      Swal.fire('Không đủ điều kiện', `Đơn hàng tối thiểu ${formatMoney(voucher.donHangToiThieu)}`, 'warning')
      return
    }
    appliedVoucher.value = voucher
    Swal.fire({ icon: 'success', title: 'Áp dụng thành công!', timer: 1000, showConfirmButton: false })
  } catch (e) {
    Swal.fire('Lỗi', 'Không thể kiểm tra mã giảm giá', 'error')
  }
}

const removeVoucher = () => { appliedVoucher.value = null; voucherCode.value = '' }

const formatDiscount = (v) => {
  if (v.loaiPhieu === 0) return `${v.giaTriGiam}%`
  return formatMoney(v.giaTriGiam)
}

// Validation
const validate = () => {
  const e = {}
  if (!form.tenKhachHang.trim()) e.tenKhachHang = 'Vui lòng nhập họ tên'
  if (!form.email.trim()) e.email = 'Vui lòng nhập email'
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = 'Email không hợp lệ'
  if (!form.soDienThoai.trim()) e.soDienThoai = 'Vui lòng nhập SĐT'
  else if (!/^0\d{9}$/.test(form.soDienThoai.replace(/\s/g, ''))) e.soDienThoai = 'SĐT không hợp lệ'

  if (selectedAddressId.value === 'new' || savedAddresses.value.length === 0) {
    if (!form.provinceName) e.province = 'Chọn tỉnh/thành'
    if (!form.districtName) e.district = 'Chọn quận/huyện'
    if (!form.wardName) e.ward = 'Chọn phường/xã'
    if (!form.diaChiCuThe.trim()) e.diaChiCuThe = 'Nhập địa chỉ cụ thể'
  }

  Object.keys(errors).forEach(k => delete errors[k])
  Object.assign(errors, e)
  return Object.keys(e).length === 0
}

// Place order
const placeOrder = async () => {
  if (!validate()) {
    Swal.fire('Thiếu thông tin', 'Vui lòng điền đầy đủ các trường bắt buộc', 'warning')
    return
  }

  isSubmitting.value = true
  try {
    const diaChiChiTiet = `${form.diaChiCuThe}, ${form.wardName}, ${form.districtName}, ${form.provinceName}`
    const user = getCurrentUser()
    
    const orderData = {
      tenKhachHang: form.tenKhachHang,
      soDienThoai: form.soDienThoai,
      email: form.email,
      diaChiChiTiet,
      wardName: form.wardName,
      districtName: form.districtName,
      provinceName: form.provinceName,
      ghiChu: form.ghiChu,
      idKhachHang: user?.id || null,
      idNhanVien: null,
      tongTien: cartStore.totalMoney,
      tienGiamGia: discountAmount.value,
      phiVanChuyen: shippingFee.value,
      tongTienSauGiam: totalPayment.value,
      hinhThucThanhToan: form.paymentMethod === 'COD' ? 'TIEN_MAT' : 'CHUYEN_KHOAN',
      phieuGiamGia: appliedVoucher.value ? [{ maPhieuGiamGia: appliedVoucher.value.maPhieuGiamGia }] : [],
      items: cartStore.items.map(i => ({
        idChiTietSanPham: i.variantId,
        soLuong: i.soLuong,
        donGia: i.donGia,
      })),
    }

    await createOrderOnline(orderData)
    cartStore.clearCart()

    await Swal.fire({
      icon: 'success',
      title: 'Đặt hàng thành công!',
      text: 'Cảm ơn bạn đã mua hàng tại YourChoice Shop',
      confirmButtonColor: '#1e3a8a',
    })

    router.push(isAuthenticated() ? '/orders' : '/')
  } catch (e) {
    console.error('Lỗi đặt hàng:', e)
    Swal.fire('Lỗi', e.response?.data?.message || 'Không thể tạo đơn hàng. Vui lòng thử lại.', 'error')
  } finally {
    isSubmitting.value = false
  }
}

const formatMoney = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0)
const handleImgError = (e) => { e.target.src = 'https://placehold.co/60x75?text=N/A' }
</script>

<style scoped>
.checkout-page { font-family: Arial, sans-serif; color: #333; }
.container { max-width: 1280px; margin: 0 auto; padding: 0 20px; box-sizing: border-box; }

.page-header-small {
  background-image: url('https://images.unsplash.com/photo-1441984904996-e0b6ba687e04?w=1600&q=80');
  background-size: cover; background-position: center; height: 180px; position: relative;
  display: flex; align-items: center; justify-content: center; margin-bottom: 40px;
}
.header-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.5); }
.header-content { position: relative; z-index: 1; text-align: center; color: #fff; }
.header-content h1 { font-size: 32px; font-weight: 700; margin-bottom: 8px; }
.breadcrumb { font-size: 14px; color: #e2e8f0; }
.breadcrumb span { cursor: pointer; }

.checkout-content { margin-bottom: 60px; }
.checkout-grid { display: grid; grid-template-columns: 1fr 420px; gap: 40px; }

/* Form sections */
.form-section { background: #fff; border: 1px solid #f1f5f9; border-radius: 12px; padding: 25px; margin-bottom: 20px; }
.form-section h3 { font-size: 18px; color: #0f172a; margin-bottom: 20px; font-weight: 700; display: flex; align-items: center; gap: 10px; }
.form-section h3 i { color: #1e3a8a; }
.form-row { margin-bottom: 15px; }
.form-row.two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; }
.form-row.three-col { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 15px; }
.form-group { margin-bottom: 12px; }
.form-group label { display: block; font-size: 14px; font-weight: 600; color: #334155; margin-bottom: 6px; }
.required { color: #ef4444; }
.form-group input, .form-group select { width: 100%; padding: 11px 14px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; outline: none; transition: 0.2s; box-sizing: border-box; }
.form-group input:focus, .form-group select:focus { border-color: #1e3a8a; box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1); }
.form-group input.error, .form-group select.error { border-color: #ef4444; }
.error-msg { color: #ef4444; font-size: 12px; margin-top: 4px; display: block; }
textarea { width: 100%; padding: 12px 14px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; outline: none; resize: vertical; font-family: inherit; box-sizing: border-box; }
textarea:focus { border-color: #1e3a8a; }

/* Saved addresses */
.saved-addresses { display: flex; flex-direction: column; gap: 10px; margin-bottom: 15px; }
.address-option { display: flex; gap: 12px; padding: 14px; border: 1px solid #e2e8f0; border-radius: 8px; cursor: pointer; transition: 0.2s; align-items: flex-start; }
.address-option.active { border-color: #1e3a8a; background: #eff6ff; }
.address-option input { margin-top: 3px; accent-color: #1e3a8a; }
.address-detail { flex: 1; }
.address-detail p { margin: 4px 0 0; font-size: 13px; color: #64748b; }
.default-badge { display: inline-block; background: #1e3a8a; color: #fff; font-size: 11px; padding: 2px 8px; border-radius: 4px; margin-top: 4px; }

/* Shipping */
.shipping-options { display: flex; flex-direction: column; gap: 10px; }
.shipping-option { display: flex; align-items: center; gap: 12px; padding: 14px 16px; border: 1px solid #e2e8f0; border-radius: 8px; cursor: pointer; transition: 0.2s; }
.shipping-option.active { border-color: #1e3a8a; background: #eff6ff; }
.shipping-option input { accent-color: #1e3a8a; }
.option-info { flex: 1; }
.option-name { font-weight: 600; font-size: 14px; display: block; }
.option-desc { font-size: 13px; color: #64748b; }
.option-price { font-weight: 700; color: #0f172a; font-size: 14px; }

/* Payment */
.payment-options { display: flex; flex-direction: column; gap: 10px; }
.payment-option { display: flex; align-items: center; gap: 12px; padding: 14px 16px; border: 1px solid #e2e8f0; border-radius: 8px; cursor: pointer; transition: 0.2s; }
.payment-option.active { border-color: #1e3a8a; background: #eff6ff; }
.payment-option input { accent-color: #1e3a8a; }
.payment-option i { font-size: 20px; color: #1e3a8a; width: 24px; text-align: center; }
.payment-option span { font-weight: 600; font-size: 14px; }

/* Order summary */
.order-summary { background: #f8fafc; border-radius: 12px; padding: 30px; height: fit-content; position: sticky; top: 100px; }
.order-summary h3 { font-size: 18px; color: #0f172a; margin-bottom: 20px; font-weight: 700; }
.summary-items { max-height: 300px; overflow-y: auto; margin-bottom: 20px; }
.summary-item { display: flex; gap: 12px; align-items: center; padding: 10px 0; border-bottom: 1px solid #e2e8f0; }
.summary-item img { width: 55px; height: 68px; object-fit: cover; border-radius: 6px; border: 1px solid #e2e8f0; }
.s-item-info { flex: 1; min-width: 0; }
.s-item-info h4 { font-size: 14px; font-weight: 600; margin: 0 0 4px; color: #334155; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.s-item-info span { font-size: 12px; color: #94a3b8; }
.s-item-price { font-weight: 700; font-size: 14px; color: #0f172a; white-space: nowrap; }

/* Voucher */
.voucher-section { margin-bottom: 20px; }
.voucher-input { display: flex; gap: 8px; }
.voucher-input input { flex: 1; padding: 10px 12px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; outline: none; }
.voucher-input input:focus { border-color: #1e3a8a; }
.voucher-input button { padding: 10px 16px; background: #0f172a; color: #fff; border: none; border-radius: 8px; font-weight: 700; font-size: 13px; cursor: pointer; white-space: nowrap; }
.voucher-input button:disabled { opacity: 0.5; cursor: not-allowed; }
.voucher-input button:hover:not(:disabled) { background: #1e3a8a; }
.voucher-applied { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; padding: 10px 14px; background: #ecfdf5; border-radius: 8px; font-size: 13px; color: #065f46; font-weight: 600; }
.btn-remove-voucher { background: none; border: none; color: #dc2626; cursor: pointer; font-size: 14px; }

/* Calculations */
.summary-calculations { margin-bottom: 20px; }
.calc-row { display: flex; justify-content: space-between; margin-bottom: 12px; font-size: 14px; color: #475569; }
.calc-row.total { font-weight: 700; font-size: 18px; color: #0f172a; }
.total-amount { color: #dc2626; }
.discount-val { color: #059669; }
.calc-divider { border-top: 1px solid #e2e8f0; margin: 15px 0; }

.btn-place-order { width: 100%; padding: 16px; background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; border: none; font-weight: 700; font-size: 16px; border-radius: 8px; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; gap: 8px; }
.btn-place-order:hover:not(:disabled) { opacity: 0.9; box-shadow: 0 4px 15px rgba(15, 23, 42, 0.3); }
.btn-place-order:disabled { opacity: 0.7; cursor: not-allowed; }

@media (max-width: 992px) {
  .checkout-grid { grid-template-columns: 1fr; }
  .order-summary { position: static; }
  .form-row.three-col { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .form-row.two-col { grid-template-columns: 1fr; }
}
</style>
