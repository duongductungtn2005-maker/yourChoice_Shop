<template>
  <div class="profile-page">
    <div class="header-section">
      <h1 class="page-title">Chi tiết thông tin</h1>
    </div>

    <div class="card">
      <div class="card-body">
        <form @submit.prevent="handleSubmit">
          <div class="layout-grid">

            <!-- LEFT: Avatar + Name -->
            <div class="left-col">
              <div class="avatar-upload-container">
                <div class="avatar-preview" @click="triggerFileInput">
                  <img v-if="previewImage" :src="previewImage" alt="Avatar" class="avatar-img" />
                  <div v-else class="upload-placeholder">
                    <span class="avatar-letter">{{ avatarLetter }}</span>
                  </div>
                  <div class="camera-badge"><i class="fas fa-camera"></i></div>
                </div>
                <input type="file" ref="fileInput" class="hidden-input" accept="image/*" @change="handleFileChange" />
              </div>
              <h3 class="profile-name">{{ employee.tenNhanVien || 'Chưa cập nhật' }}</h3>
              <span class="role-badge" :class="isCurrentAdmin ? 'admin' : 'staff'">
                {{ isCurrentAdmin ? 'Quản Trị Viên' : 'Nhân Viên' }}
              </span>
              <p class="upload-hint">Nhấn vào biểu tượng máy ảnh để tải ảnh mới lên.</p>
            </div>

            <!-- RIGHT: Info Form -->
            <div class="right-col">

              <div class="form-row">
                <div class="form-group half">
                  <label>Mã nhân viên <span class="required">*</span></label>
                  <input type="text" :value="employee.maNhanVien" class="form-control" readonly />
                </div>
                <div class="form-group half">
                  <label>Tên tài khoản <span class="required">*</span></label>
                  <input type="text" :value="employee.tenTaiKhoan" class="form-control" readonly autocomplete="username" />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group half">
                  <label>Họ và tên <span class="required">*</span></label>
                  <input type="text" v-model="employee.tenNhanVien" class="form-control" placeholder="Nhập họ tên" />
                </div>
                <div class="form-group half">
                  <label>Mật khẩu <span class="required">*</span></label>
                  <div class="password-wrapper">
                    <input :type="showPassword ? 'text' : 'password'" v-model="employee.matKhau" class="form-control" placeholder="Nhập mật khẩu mới (để trống nếu không đổi)" name="new-password" autocomplete="new-password" />
                    <button type="button" class="toggle-pw" @click="showPassword = !showPassword">
                      <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
                    </button>
                  </div>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group half">
                  <label>Email</label>
                  <input type="email" v-model="employee.email" class="form-control" />
                </div>
                <div class="form-group half">
                  <label>Số điện thoại</label>
                  <input type="text" v-model="employee.soDienThoai" class="form-control" />
                </div>
              </div>

              <div class="form-row">
                <div class="form-group half">
                  <label>Ngày sinh</label>
                  <input type="date" v-model="employee.ngaySinh" class="form-control" />
                </div>
                <div class="form-group half">
                  <label>Giới tính</label>
                  <div class="radio-group">
                    <label class="radio-item"><input type="radio" :value="true" v-model="employee.gioiTinh" /> Nam</label>
                    <label class="radio-item"><input type="radio" :value="false" v-model="employee.gioiTinh" /> Nữ</label>
                  </div>
                </div>
              </div>

              <div class="form-row">
                <div class="form-group third">
                  <label>Tỉnh / Thành phố</label>
                  <select v-model="address.provinceId" @change="onProvinceChange" class="form-control">
                    <option value="">-- Tỉnh --</option>
                    <option v-for="p in locationData.provinces" :key="p.code" :value="p.code">{{ p.name }}</option>
                  </select>
                </div>
                <div class="form-group third">
                  <label>Quận / Huyện</label>
                  <select v-model="address.districtId" @change="onDistrictChange" class="form-control" :disabled="!address.provinceId">
                    <option value="">-- Huyện --</option>
                    <option v-for="d in locationData.districts" :key="d.code" :value="d.code">{{ d.name }}</option>
                  </select>
                </div>
                <div class="form-group third">
                  <label>Phường / Xã</label>
                  <select v-model="address.wardCode" class="form-control" :disabled="!address.districtId">
                    <option value="">-- Xã --</option>
                    <option v-for="w in locationData.wards" :key="w.code" :value="w.code">{{ w.name }}</option>
                  </select>
                </div>
              </div>

              <div class="form-group">
                <label>Địa chỉ cụ thể</label>
                <input type="text" v-model="employee.diaChiCuThe" class="form-control" placeholder="Số nhà, đường..." />
              </div>

              <div class="form-group">
                <label>Ghi chú</label>
                <textarea v-model="employee.ghiChu" class="form-control textarea" rows="3" placeholder="Ghi chú..."></textarea>
              </div>

              <div class="form-actions">
                <button type="submit" class="btn btn-gradient" :disabled="saving">
                  <i class="fas fa-save"></i> {{ saving ? 'Đang lưu...' : 'Lưu Thay Đổi' }}
                </button>
              </div>
            </div>

          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/services/request'
import { getCurrentUser, getRole, login as authLogin } from '@/services/auth'
import { toastSuccess, toastError, Toast } from '@/utils/toast'
import { fetchProvinces as apiFetchProvinces, fetchDistricts as apiFetchDistricts, fetchWards as apiFetchWards } from '@/api/locationApi'
import Swal from 'sweetalert2'

const router = useRouter()
const saving = ref(false)
const showPassword = ref(false)

const employee = reactive({
  id: null,
  maNhanVien: '',
  tenNhanVien: '',
  tenTaiKhoan: '',
  matKhau: '',
  email: '',
  soDienThoai: '',
  gioiTinh: true,
  ngaySinh: '',
  diaChiCuThe: '',
  ghiChu: '',
  anhDaiDien: ''
})

const address = reactive({ provinceId: '', districtId: '', wardCode: '' })
const locationData = reactive({ provinces: [], districts: [], wards: [] })
const fileInput = ref(null)
const selectedFile = ref(null)
const previewImage = ref(null)

const isCurrentAdmin = computed(() => {
  const role = String(getRole() || '').toUpperCase()
  return role === 'ADMIN'
})

const avatarLetter = computed(() => {
  const name = employee.tenNhanVien || ''
  return name.charAt(0).toUpperCase() || 'A'
})

// ===== Load profile =====
const loadProfile = async () => {
  try {
    const user = getCurrentUser()
    if (!user?.id) { router.push('/login'); return }

    locationData.provinces = await apiFetchProvinces()

    const res = await request.get(`/nhan-vien/${user.id}`)
    const data = res.data

    employee.id = data.id
    employee.maNhanVien = data.maNhanVien || ''
    employee.tenNhanVien = data.tenNhanVien || ''
    employee.tenTaiKhoan = data.tenTaiKhoan || ''
    employee.email = data.email || ''
    employee.soDienThoai = data.soDienThoai || ''
    employee.gioiTinh = data.gioiTinh ?? true
    employee.ngaySinh = data.ngaySinh || ''
    employee.matKhau = ''

    if (data.anhDaiDien) {
      previewImage.value = `http://localhost:8080/api/v1/nhan-vien/images/${data.anhDaiDien}`
    }

    if (data.diaChi && data.diaChi !== 'Chưa cập nhật') {
      await parseAddressString(data.diaChi)
    } else {
      employee.diaChiCuThe = data.diaChi || ''
    }
  } catch (e) {
    console.error('Lỗi tải thông tin:', e)
  }
}

// ===== Address parsing (reused from EditEmployee) =====
const normalizeName = (str) => {
  if (!str) return ''
  return str.toLowerCase()
    .replace(/(tỉnh|thành phố|tp\.?|quận|huyện|thị xã|tx\.?|xã|phường|thị trấn|tt\.?)\s*/g, '')
    .trim()
}

const findLocationCode = (inputName, listData) => {
  if (!inputName || !listData || listData.length === 0) return ''
  const searchKey = normalizeName(inputName)
  let found = listData.find(item => normalizeName(item.name) === searchKey)
  if (!found) found = listData.find(item => normalizeName(item.name).includes(searchKey))
  return found ? found.code : ''
}

const parseAddressString = async (fullAddr) => {
  if (!fullAddr) return
  const parts = fullAddr.split(',').map(s => s.trim())
  if (parts.length < 3) { employee.diaChiCuThe = fullAddr; return }

  const pName = parts[parts.length - 1]
  const dName = parts[parts.length - 2]
  const wName = parts[parts.length - 3]

  const pCode = findLocationCode(pName, locationData.provinces)
  if (pCode) {
    address.provinceId = pCode
    try {
      locationData.districts = await apiFetchDistricts(pCode)
      const dCode = findLocationCode(dName, locationData.districts)
      if (dCode) {
        address.districtId = dCode
        locationData.wards = await apiFetchWards(dCode)
        const wCode = findLocationCode(wName, locationData.wards)
        if (wCode) address.wardCode = wCode
      }
    } catch (e) { console.error('Lỗi load địa chỉ:', e) }
  }

  if (parts.length > 3) {
    employee.diaChiCuThe = parts.slice(0, parts.length - 3).join(', ')
  } else {
    employee.diaChiCuThe = ''
  }
}

const getNameFromId = (id, list) => { const item = list.find(x => x.code == id); return item ? item.name : '' }
const onProvinceChange = async () => {
  address.districtId = ''; address.wardCode = ''; locationData.districts = []; locationData.wards = []
  if (address.provinceId) { try { locationData.districts = await apiFetchDistricts(address.provinceId) } catch {} }
}
const onDistrictChange = async () => {
  address.wardCode = ''; locationData.wards = []
  if (address.districtId) { try { locationData.wards = await apiFetchWards(address.districtId) } catch {} }
}

// ===== File upload =====
const triggerFileInput = () => fileInput.value.click()
const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (file) { selectedFile.value = file; previewImage.value = URL.createObjectURL(file) }
}

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

// ===== Save =====
const handleSubmit = async () => {
  if (!employee.tenNhanVien.trim()) { Toast.fire({ icon: 'warning', title: 'Vui lòng nhập họ tên' }); return }
  if (employee.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(employee.email)) { Toast.fire({ icon: 'warning', title: 'Email không đúng định dạng' }); return }
  if (employee.soDienThoai && employee.soDienThoai.replace(/[^0-9]/g, '').length !== 10) { Toast.fire({ icon: 'warning', title: 'Số điện thoại phải đúng 10 chữ số' }); return }

  saving.value = true
  try {
    const newPassword = String(employee.matKhau || '').trim()
    const fd = new FormData()
    fd.append('tenNhanVien', employee.tenNhanVien)
    fd.append('email', employee.email)
    fd.append('soDienThoai', employee.soDienThoai)
    fd.append('gioiTinh', employee.gioiTinh)
    if (employee.ngaySinh) fd.append('ngaySinh', employee.ngaySinh)
    if (newPassword) fd.append('matKhau', newPassword)

    const p = getNameFromId(address.provinceId, locationData.provinces)
    const d = getNameFromId(address.districtId, locationData.districts)
    const w = getNameFromId(address.wardCode, locationData.wards)
    const fullAddr = [employee.diaChiCuThe, w, d, p].filter(Boolean).join(', ')
    fd.append('diaChi', fullAddr)

    if (selectedFile.value) fd.append('avatarFile', selectedFile.value)

    await request.put(`/nhan-vien/${employee.id}`, fd)

    // Tải lại dữ liệu mới nhất (bao gồm tên tài khoản tự sinh theo họ tên mới).
    await loadProfile()

    // Chỉ sau khi đã có username mới nhất mới gọi trình quản lý mật khẩu.
    if (newPassword) {
      await syncBrowserPasswordManager(employee.tenTaiKhoan, newPassword, employee.tenNhanVien)
    }

    // Đồng bộ lại session
    try {
      const currentUser = JSON.parse(sessionStorage.getItem('user') || '{}')
      const nextUser = {
        ...currentUser,
        tenTaiKhoan: employee.tenTaiKhoan,
        username: employee.tenTaiKhoan,
        tenNhanVien: employee.tenNhanVien,
        email: employee.email,
        soDienThoai: employee.soDienThoai
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
    const msg = typeof e?.response?.data === 'string' ? e.response.data : (e?.response?.data?.message || 'Không thể cập nhật')
    Swal.fire('Lỗi', msg, 'error')
  } finally { saving.value = false }
}

onMounted(() => loadProfile())
</script>

<style scoped>
.profile-page { font-family: 'Segoe UI', sans-serif; background-color: #ebecee; min-height: 100vh; padding: 20px; }
.header-section { margin-bottom: 20px; }
.page-title { color: #2b4360; font-weight: 700; font-size: 24px; }

.card { background: #fff; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); padding: 30px; border: 1px solid #bfdbfe !important; }

.layout-grid { display: grid; grid-template-columns: 300px 1fr; gap: 40px; }
.left-col { border-right: 1px solid #f1f5f9; padding-right: 30px; display: flex; flex-direction: column; align-items: center; padding-top: 30px; }

/* Avatar */
.avatar-upload-container { display: flex; justify-content: center; margin-bottom: 16px; }
.avatar-preview { width: 180px; height: 180px; border-radius: 50%; overflow: hidden; cursor: pointer; background: #1e293b; display: flex; align-items: center; justify-content: center; position: relative; }
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.upload-placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; }
.avatar-letter { font-size: 72px; font-weight: 700; color: #fff; }
.camera-badge { position: absolute; bottom: 8px; right: 8px; width: 36px; height: 36px; background: #ef4444; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #fff; font-size: 14px; border: 3px solid #fff; }
.hidden-input { display: none; }

.profile-name { font-size: 20px; font-weight: 700; color: #0f172a; margin: 8px 0 8px; text-align: center; }
.role-badge { display: inline-block; padding: 4px 16px; border-radius: 20px; font-size: 12px; font-weight: 700; color: #fff; }
.role-badge.admin { background: #3b82f6; }
.role-badge.staff { background: #3b82f6; }
.upload-hint { font-size: 13px; color: #94a3b8; margin-top: 14px; text-align: center; }

/* Form */
.right-col { padding-top: 10px; }
.form-row { display: flex; gap: 20px; margin-bottom: 0; }
.form-group { margin-bottom: 16px; flex: 1; }
.form-group.half { flex: 1; }
.form-group.third { flex: 1; }
.form-group label { display: block; margin-bottom: 6px; font-weight: 600; font-size: 13px; color: #334155; }
.required { color: #ef4444; }
.form-control { width: 100%; padding: 10px 12px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 14px; outline: none; transition: 0.2s; box-sizing: border-box; }
.form-control:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.1); }
.form-control[readonly] { background: #f8fafc; color: #64748b; }
.textarea { resize: vertical; min-height: 60px; }
.radio-group { display: flex; gap: 20px; align-items: center; height: 42px; }
.radio-item { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 14px; }

.password-wrapper { position: relative; }
.password-wrapper .form-control { padding-right: 40px; }
.toggle-pw { position: absolute; right: 10px; top: 50%; transform: translateY(-50%); background: none; border: none; color: #64748b; cursor: pointer; font-size: 14px; }

.form-actions { display: flex; justify-content: flex-end; margin-top: 20px; border-top: 1px solid #f1f5f9; padding-top: 20px; }
.btn { padding: 12px 28px; border-radius: 8px; font-weight: 600; cursor: pointer; font-size: 14px; border: none; }
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-gradient { background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); color: #fff; box-shadow: 0 4px 10px rgba(15,23,42,0.3); transition: 0.2s; }
.btn-gradient:hover { transform: translateY(-1px); box-shadow: 0 6px 15px rgba(15,23,42,0.4); }

@media (max-width: 768px) {
  .layout-grid { grid-template-columns: 1fr; }
  .left-col { border-right: none; padding-right: 0; border-bottom: 1px solid #f1f5f9; padding-bottom: 20px; }
}
</style>
