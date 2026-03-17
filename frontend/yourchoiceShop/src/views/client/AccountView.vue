<template>
  <div class="account-page">
    <div class="page-header-small">
      <div class="header-overlay"></div>
      <div class="container header-content">
        <h1>TÀI KHOẢN CỦA TÔI</h1>
        <div class="breadcrumb">
          <span @click="$router.push('/')">Trang chủ</span> / <span>Tài khoản</span>
        </div>
      </div>
    </div>

    <div class="container">
      <div class="account-layout">
        <!-- Sidebar -->
        <aside class="account-sidebar">
          <div class="sidebar-user">
            <div class="avatar-upload-area" @click="triggerAvatarInput">
              <div class="user-avatar">
                <img v-if="previewAvatar" :src="previewAvatar" alt="Avatar" />
                <span v-else class="avatar-letter">{{ avatarLetter }}</span>
              </div>
              <div class="camera-badge"><i class="fas fa-camera"></i></div>
            </div>
            <input type="file" ref="avatarInput" class="hidden-file" accept="image/*" @change="handleAvatarChange" />
            <h4>{{ customer.tenTaiKhoan || customer.username || customer.tenKhachHang || customer.hoTen || 'Khách hàng' }}</h4>
            <p class="text-muted">{{ customer.email }}</p>
          </div>

          <nav class="sidebar-nav">
            <button :class="{ active: activeTab === 'profile' }" @click="activeTab = 'profile'">
              <i class="fas fa-user"></i> Thông tin cá nhân
            </button>
            <button :class="{ active: activeTab === 'addresses' }" @click="activeTab = 'addresses'">
              <i class="fas fa-map-marker-alt"></i> Sổ địa chỉ
            </button>
            <button @click="goToOrders">
              <i class="fas fa-clipboard-list"></i> Đơn hàng
            </button>
          </nav>
        </aside>

        <!-- Main content -->
        <div class="account-main">
          <!-- Profile Tab -->
          <div v-if="activeTab === 'profile'" class="tab-content">
            <h2>Thông tin cá nhân</h2>

            <form class="profile-form" @submit.prevent="saveProfile">
              <div class="form-row">
                <div class="form-group">
                  <label>Tên tài khoản <span class="required">*</span></label>
                  <input v-model="form.username" type="text" maxlength="50" autocomplete="username" />
                  <span class="error" v-if="errors.username">{{ errors.username }}</span>
                </div>
                <div class="form-group">
                  <label>Mật khẩu</label>
                  <div class="password-wrapper">
                    <input :type="showPassword ? 'text' : 'password'" v-model="form.matKhau" placeholder="Nhập mật khẩu mới (để trống nếu không đổi)" autocomplete="new-password" />
                    <button type="button" class="toggle-pw" @click="showPassword = !showPassword">
                      <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                    </button>
                  </div>
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>Họ tên <span class="required">*</span></label>
                  <input v-model="form.hoTen" type="text" maxlength="100" />
                  <span class="error" v-if="errors.hoTen">{{ errors.hoTen }}</span>
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>Email <span class="required">*</span></label>
                  <input v-model="form.email" type="email" />
                  <span class="error" v-if="errors.email">{{ errors.email }}</span>
                </div>
                <div class="form-group">
                  <label>Số điện thoại <span class="required">*</span></label>
                  <input v-model="form.soDienThoai" type="text" maxlength="15" />
                  <span class="error" v-if="errors.soDienThoai">{{ errors.soDienThoai }}</span>
                </div>
                <div class="form-group">
                  <label>Ngày sinh</label>
                  <input v-model="form.ngaySinh" type="date" />
                </div>
              </div>
              <div class="form-row">
                <div class="form-group">
                  <label>Giới tính</label>
                  <select v-model="form.gioiTinh">
                    <option :value="null">-- Chọn --</option>
                    <option :value="true">Nam</option>
                    <option :value="false">Nữ</option>
                  </select>
                </div>
              </div>

              <div class="form-actions">
                <button type="submit" class="btn-save" :disabled="saving">
                  <i class="fas fa-save"></i> {{ saving ? 'Đang lưu...' : 'Lưu thay đổi' }}
                </button>
              </div>
            </form>
          </div>

          <!-- Addresses Tab -->
          <div v-if="activeTab === 'addresses'" class="tab-content">
            <div class="tab-header-row">
              <h2>Sổ địa chỉ</h2>
              <button class="btn-add-addr" @click="openAddressForm(null)">
                <i class="fas fa-plus"></i> Thêm địa chỉ
              </button>
            </div>

            <div v-if="addresses.length === 0" class="empty-addresses">
              <i class="fas fa-map-marker-alt"></i>
              <p>Bạn chưa có địa chỉ nào</p>
            </div>

            <div class="address-list" v-else>
              <div class="address-card" v-for="addr in addresses" :key="addr.id" :class="{ 'is-default': addr.macDinh }">
                <div class="addr-header">
                  <strong>{{ addr.tenNguoiNhan }}</strong>
                  <span class="default-badge" v-if="addr.macDinh">Mặc định</span>
                </div>
                <p>{{ addr.soDienThoai }}</p>
                <p class="addr-detail">{{ addr.diaChiChiTiet }}, {{ addr.phuongXa }}, {{ addr.quanHuyen }}, {{ addr.tinhThanh }}</p>
                <div class="addr-actions">
                  <button @click="openAddressForm(addr)"><i class="fas fa-edit"></i> Sửa</button>
                  <button v-if="!addr.macDinh" @click="setDefault(addr.id)"><i class="fas fa-check"></i> Đặt mặc định</button>
                  <button v-if="!addr.macDinh" class="btn-del" @click="removeAddress(addr.id)"><i class="fas fa-trash"></i> Xóa</button>
                </div>
              </div>
            </div>

            <!-- Address Form Modal -->
            <div v-if="showAddrForm" class="modal-overlay" @click.self="showAddrForm = false">
              <div class="modal-content addr-modal">
                <h3>{{ editAddr ? 'Sửa địa chỉ' : 'Thêm địa chỉ mới' }}</h3>
                <form @submit.prevent="saveAddress">
                  <div class="form-group">
                    <label>Tên người nhận</label>
                    <input v-model="addrForm.tenNguoiNhan" type="text" required />
                  </div>
                  <div class="form-group">
                    <label>Số điện thoại</label>
                    <input v-model="addrForm.soDienThoai" type="text" required />
                  </div>
                  <div class="form-row">
                    <div class="form-group">
                      <label>Tỉnh/Thành</label>
                      <select v-model="addrForm.tinhThanh" @change="onProvinceChange" required>
                        <option value="">-- Chọn --</option>
                        <option v-for="p in provinces" :key="p.ProvinceID" :value="p.ProvinceName">{{ p.ProvinceName }}</option>
                      </select>
                    </div>
                    <div class="form-group">
                      <label>Quận/Huyện</label>
                      <select v-model="addrForm.quanHuyen" @change="onDistrictChange" required>
                        <option value="">-- Chọn --</option>
                        <option v-for="d in districts" :key="d.DistrictID" :value="d.DistrictName">{{ d.DistrictName }}</option>
                      </select>
                    </div>
                  </div>
                  <div class="form-row">
                    <div class="form-group">
                      <label>Phường/Xã</label>
                      <select v-model="addrForm.phuongXa" required>
                        <option value="">-- Chọn --</option>
                        <option v-for="w in wards" :key="w.WardCode" :value="w.WardName">{{ w.WardName }}</option>
                      </select>
                    </div>
                    <div class="form-group">
                      <label>Địa chỉ chi tiết</label>
                      <input v-model="addrForm.diaChiChiTiet" type="text" placeholder="Số nhà, đường..." required />
                    </div>
                  </div>
                  <div class="form-actions">
                    <button type="button" class="btn-cancel" @click="showAddrForm = false">Hủy</button>
                    <button type="submit" class="btn-save">Lưu</button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { isAuthenticated, getCurrentUser, getCurrentUserId, getCustomerOrdersPath, logout } from '@/services/auth'
import { getCustomerDetail, updateCustomer, getAddresses, createAddress, updateAddress, deleteAddress, setDefaultAddress, getProvinces, getDistricts, getWards } from '@/api/clientApi'
import { toastSuccess } from '@/utils/toast'
import Swal from 'sweetalert2'

const router = useRouter()
const route = useRoute()
const activeTab = ref('profile')
const saving = ref(false)
const showPassword = ref(false)

const customer = ref({})
const form = reactive({ username: '', hoTen: '', email: '', soDienThoai: '', ngaySinh: '', gioiTinh: null, matKhau: '' })
const errors = reactive({})

// Avatar
const avatarInput = ref(null)
const selectedAvatarFile = ref(null)
const previewAvatar = ref(null)
const avatarLetter = computed(() => {
  const name = customer.value.tenKhachHang || customer.value.hoTen || ''
  return name.charAt(0).toUpperCase() || 'K'
})
const triggerAvatarInput = () => avatarInput.value.click()
const handleAvatarChange = (e) => {
  const file = e.target.files[0]
  if (file) { selectedAvatarFile.value = file; previewAvatar.value = URL.createObjectURL(file) }
}

// Addresses
const addresses = ref([])
const showAddrForm = ref(false)
const editAddr = ref(null)
const addrForm = reactive({ tenNguoiNhan: '', soDienThoai: '', tinhThanh: '', quanHuyen: '', phuongXa: '', diaChiChiTiet: '' })
const provinces = ref([])
const districts = ref([])
const wards = ref([])

onMounted(async () => {
  if (!isAuthenticated()) { router.push('/client/login'); return }

  const currentUserId = getCurrentUserId()
  const routeCustomerId = Number(route.params.id)
  if (!currentUserId) { router.push('/client/login'); return }
  if (Number.isNaN(routeCustomerId) || routeCustomerId !== currentUserId) {
    router.replace(`/customer/${currentUserId}/account`)
    return
  }

  const user = getCurrentUser()
  if (user?.id) {
    await loadCustomer(user.id)
    await loadAddresses(user.id)
  }
  try { const res = await getProvinces(); provinces.value = res.data?.data || res.data || [] } catch {}
})

const goToOrders = () => {
  router.push(getCustomerOrdersPath())
}

const loadCustomer = async (id) => {
  try {
    const res = await getCustomerDetail(id)
    const c = res.data
    customer.value = c
    form.username = c.tenTaiKhoan || c.username || ''
    form.hoTen = c.tenKhachHang || c.hoTen || ''
    form.email = c.email || ''
    form.soDienThoai = c.soDienThoai || ''
    form.ngaySinh = c.ngaySinh || ''
    form.gioiTinh = c.gioiTinh ?? null
    form.matKhau = ''

    // Avatar
    if (!selectedAvatarFile.value) {
      if (c.avatar) {
        previewAvatar.value = `http://localhost:8080/images/images/khach-hang/${c.avatar}`
      } else {
        previewAvatar.value = null
      }
    }
  } catch (e) { console.error('Lỗi tải thông tin:', e) }
}

const loadAddresses = async (id) => {
  try {
    const res = await getAddresses(id)
    addresses.value = res.data?.content || res.data || []
  } catch { addresses.value = [] }
}

const saveProfile = async () => {
  Object.keys(errors).forEach(k => delete errors[k])
  const normalizedUsername = form.username.trim()
  if (!normalizedUsername) { errors.username = 'Vui lòng nhập tên tài khoản'; return }
  if (!/^[A-Za-z0-9._-]{3,50}$/.test(normalizedUsername)) {
    errors.username = 'Tên tài khoản chỉ gồm chữ, số, dấu chấm, gạch dưới, gạch ngang (3-50 ký tự)'
    return
  }
  if (!form.hoTen.trim()) { errors.hoTen = 'Vui lòng nhập họ tên'; return }
  if (!form.email.trim()) { errors.email = 'Vui lòng nhập email'; return }
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) {
    errors.email = 'Email không hợp lệ'
    return
  }
  if (!form.soDienThoai.trim()) { errors.soDienThoai = 'Vui lòng nhập SĐT'; return }
  const normalizedPhone = form.soDienThoai.replace(/\s/g, '')
  if (!/^0\d{9}$/.test(normalizedPhone)) {
    errors.soDienThoai = 'SĐT không hợp lệ (phải gồm 10 số và bắt đầu bằng 0)'
    return
  }

  saving.value = true
  const newPassword = String(form.matKhau || '').trim()
  try {
    const fd = new FormData()
    fd.append('tenKhachHang', form.hoTen)
    fd.append('email', form.email)
    fd.append('soDienThoai', normalizedPhone)
    fd.append('username', normalizedUsername)
    if (form.ngaySinh) fd.append('ngaySinh', form.ngaySinh)
    if (form.gioiTinh !== null) fd.append('gioiTinh', form.gioiTinh)
    if (newPassword) fd.append('password', newPassword)
    if (selectedAvatarFile.value) fd.append('avatarFile', selectedAvatarFile.value)

    await updateCustomer(customer.value.id, fd)

    // Reload dữ liệu mới nhất
    selectedAvatarFile.value = null
    await loadCustomer(customer.value.id)

    // Gọi trình quản lý mật khẩu trình duyệt (nếu có đổi mật khẩu)
    if (newPassword) {
      await syncBrowserPasswordManager(
        customer.value.tenTaiKhoan || form.username,
        newPassword,
        customer.value.tenKhachHang || form.hoTen
      )
    }

    // Đồng bộ lại user trong session
    try {
      const currentUser = JSON.parse(sessionStorage.getItem('user') || '{}')
      const nextUser = {
        ...currentUser,
        id: customer.value.id,
        tenTaiKhoan: customer.value.tenTaiKhoan || form.username,
        username: customer.value.tenTaiKhoan || form.username,
        tenKhachHang: customer.value.tenKhachHang || form.hoTen,
        email: customer.value.email || form.email,
        soDienThoai: customer.value.soDienThoai || form.soDienThoai,
      }
      sessionStorage.setItem('user', JSON.stringify(nextUser))
      window.dispatchEvent(new Event('auth-user-updated'))
    } catch {}

    await Swal.fire({
      icon: 'success',
      title: 'Lưu thành công',
      text: 'Thông tin cá nhân đã được cập nhật.',
      confirmButtonText: 'OK',
      confirmButtonColor: '#0f172a'
    })
  } catch (e) {
    const backendMessage = typeof e?.response?.data === 'string'
      ? e.response.data
      : (e?.response?.data?.message || 'Không thể cập nhật')
    Swal.fire('Lỗi', backendMessage, 'error')
  } finally { saving.value = false }
}

// Credential Management API — popup lưu mật khẩu trình duyệt
const syncBrowserPasswordManager = async (username, password, fullName) => {
  if (!username || !password) return
  try {
    if (!window.PasswordCredential || !navigator.credentials?.store) return
    const credential = new window.PasswordCredential({
      id: username,
      password,
      name: fullName || username
    })
    await navigator.credentials.store(credential)
  } catch (err) {
    console.warn('Không thể đồng bộ trình quản lý mật khẩu:', err)
  }
}

// Address management
const onProvinceChange = async () => {
  addrForm.quanHuyen = ''; addrForm.phuongXa = ''; districts.value = []; wards.value = []
  const prov = provinces.value.find(p => p.ProvinceName === addrForm.tinhThanh)
  if (prov) {
    try { const res = await getDistricts(prov.ProvinceID); districts.value = res.data?.data || res.data || [] } catch {}
  }
}

const onDistrictChange = async () => {
  addrForm.phuongXa = ''; wards.value = []
  const dist = districts.value.find(d => d.DistrictName === addrForm.quanHuyen)
  if (dist) {
    try { const res = await getWards(dist.DistrictID); wards.value = res.data?.data || res.data || [] } catch {}
  }
}

const openAddressForm = (addr) => {
  editAddr.value = addr
  if (addr) {
    Object.assign(addrForm, { tenNguoiNhan: addr.tenNguoiNhan, soDienThoai: addr.soDienThoai, tinhThanh: addr.tinhThanh, quanHuyen: addr.quanHuyen, phuongXa: addr.phuongXa, diaChiChiTiet: addr.diaChiChiTiet })
  } else {
    Object.assign(addrForm, { tenNguoiNhan: '', soDienThoai: '', tinhThanh: '', quanHuyen: '', phuongXa: '', diaChiChiTiet: '' })
  }
  showAddrForm.value = true
}

const saveAddress = async () => {
  try {
    const data = { ...addrForm, khachHangId: customer.value.id }
    if (editAddr.value) { await updateAddress(editAddr.value.id, data) }
    else { await createAddress(data) }
    toastSuccess(editAddr.value ? 'Đã cập nhật!' : 'Đã thêm địa chỉ!')
    showAddrForm.value = false
    await loadAddresses(customer.value.id)
  } catch (e) { Swal.fire('Lỗi', e.response?.data?.message || 'Không thể lưu', 'error') }
}

const removeAddress = async (id) => {
  const r = await Swal.fire({ title: 'Xóa địa chỉ?', icon: 'warning', showCancelButton: true, confirmButtonColor: '#ef4444', confirmButtonText: 'Xóa', cancelButtonText: 'Không' })
  if (!r.isConfirmed) return
  try { await deleteAddress(id); toastSuccess('Đã xóa!'); await loadAddresses(customer.value.id) } catch {}
}

const setDefault = async (id) => {
  try { await setDefaultAddress(id); toastSuccess('Đã đặt mặc định!'); await loadAddresses(customer.value.id) } catch {}
}

const handleLogout = () => {
  logout()
  toastSuccess('Đăng xuất thành công!')
  router.push('/')
}
</script>

<style scoped>
.account-page { font-family: Arial, sans-serif; color: #333; }
.container { max-width: 1100px; margin: 0 auto; padding: 0 20px; box-sizing: border-box; }

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

/* Layout */
.account-layout { display: flex; gap: 30px; margin-bottom: 60px; }

/* Sidebar */
.account-sidebar { width: 260px; flex-shrink: 0; }
.sidebar-user { text-align: center; padding: 25px 15px; border: 1px solid #f1f5f9; border-radius: 12px; margin-bottom: 15px; }
.avatar-upload-area { position: relative; display: inline-block; cursor: pointer; margin-bottom: 12px; }
.user-avatar { width: 100px; height: 100px; border-radius: 50%; overflow: hidden; background: #1e293b; display: flex; align-items: center; justify-content: center; }
.user-avatar img { width: 100%; height: 100%; object-fit: cover; }
.avatar-letter { font-size: 42px; font-weight: 700; color: #fff; }
.camera-badge { position: absolute; bottom: 2px; right: 2px; width: 30px; height: 30px; background: #ef4444; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 12px; border: 2px solid #fff; }
.hidden-file { display: none; }
.sidebar-user h4 { font-size: 16px; margin: 0 0 4px; color: #0f172a; }
.text-muted { font-size: 13px; color: #94a3b8; margin: 0; }

.sidebar-nav { display: flex; flex-direction: column; gap: 4px; }
.sidebar-nav button { display: flex; align-items: center; gap: 10px; padding: 12px 16px; border: none; background: none; font-size: 14px; color: #334155; cursor: pointer; border-radius: 8px; transition: 0.2s; text-align: left; width: 100%; }
.sidebar-nav button:hover, .sidebar-nav button.active { background: #eff6ff; color: #1e3a8a; font-weight: 600; }
.sidebar-nav button i { width: 18px; text-align: center; }
.btn-logout { color: #ef4444 !important; }
.btn-logout:hover { background: #fef2f2 !important; color: #dc2626 !important; }

/* Main */
.account-main { flex: 1; min-width: 0; }
.tab-content h2 { font-size: 22px; color: #0f172a; margin-bottom: 25px; padding-bottom: 12px; border-bottom: 2px solid #f1f5f9; }
.tab-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 25px; }
.tab-header-row h2 { margin-bottom: 0; border-bottom: none; padding-bottom: 0; }

/* Form */
.profile-form { max-width: 600px; }
.form-row { display: flex; gap: 20px; margin-bottom: 0; }
.form-group { flex: 1; margin-bottom: 18px; }
.form-group label { display: block; font-size: 13px; font-weight: 600; color: #334155; margin-bottom: 6px; }
.required { color: #ef4444; }
.form-group input, .form-group select { width: 100%; padding: 10px 14px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; outline: none; box-sizing: border-box; }
.form-group input:focus, .form-group select:focus { border-color: #1e3a8a; }
.password-wrapper { position: relative; }
.password-wrapper input { width: 100%; padding: 10px 40px 10px 14px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; outline: none; box-sizing: border-box; }
.password-wrapper input:focus { border-color: #1e3a8a; }
.toggle-pw { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); background: none; border: none; color: #64748b; cursor: pointer; font-size: 14px; }
.error { font-size: 12px; color: #ef4444; margin-top: 4px; display: block; }

.form-actions { display: flex; gap: 12px; margin-top: 10px; }
.btn-save { padding: 10px 28px; background: #0f172a; color: #fff; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; font-size: 14px; }
.btn-save:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel { padding: 10px 28px; background: #fff; color: #334155; border: 1px solid #e2e8f0; border-radius: 8px; font-weight: 600; cursor: pointer; font-size: 14px; }

/* Addresses */
.btn-add-addr { padding: 8px 18px; background: #0f172a; color: #fff; border: none; border-radius: 8px; font-size: 13px; font-weight: 600; cursor: pointer; }
.empty-addresses { text-align: center; padding: 50px; color: #94a3b8; }
.empty-addresses i { font-size: 40px; margin-bottom: 10px; }

.address-list { display: flex; flex-direction: column; gap: 12px; }
.address-card { padding: 18px 20px; border: 1px solid #f1f5f9; border-radius: 10px; transition: 0.2s; }
.address-card.is-default { border-color: #1e3a8a; background: #f8faff; }
.addr-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.addr-header strong { font-size: 15px; color: #0f172a; }
.default-badge { font-size: 11px; padding: 3px 10px; background: #1e3a8a; color: #fff; border-radius: 20px; font-weight: 700; }
.address-card p { margin: 3px 0; font-size: 14px; color: #64748b; }
.addr-detail { margin-top: 4px; }
.addr-actions { display: flex; gap: 10px; margin-top: 10px; }
.addr-actions button { padding: 5px 12px; font-size: 12px; border: 1px solid #e2e8f0; background: #fff; border-radius: 5px; cursor: pointer; color: #334155; }
.addr-actions button:hover { border-color: #1e3a8a; color: #1e3a8a; }
.addr-actions .btn-del:hover { border-color: #ef4444; color: #ef4444; }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 2000; display: flex; align-items: center; justify-content: center; padding: 20px; }
.modal-content { background: #fff; border-radius: 16px; max-width: 550px; width: 100%; padding: 30px; max-height: 90vh; overflow-y: auto; }
.modal-content h3 { font-size: 18px; margin-bottom: 20px; color: #0f172a; }

@media (max-width: 768px) {
  .account-layout { flex-direction: column; }
  .account-sidebar { width: 100%; }
  .form-row { flex-direction: column; gap: 0; }
}
</style>
