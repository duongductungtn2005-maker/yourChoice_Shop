<template>
  <div class="register-page">
    <button class="btn-back" @click="$router.push('/')" title="Trở lại">
      <i class="fa-solid fa-arrow-left"></i>
      <span>Trở lại</span>
    </button>

    <div class="register-wrap">
      <div class="register-card">
        <div class="card-left">
          <div class="brand">
            <img v-if="showLogo" class="brand-logo" src="@/img/logo1.png" alt="Logo" @error="showLogo = false" />
            <div class="brand-text">
              <h2>Tạo tài khoản</h2>
              <p>Đăng ký để mua sắm dễ dàng hơn</p>
            </div>
          </div>

          <form class="form" @submit.prevent="handleRegister">
            <div class="form-row">
              <div class="form-group">
                <label>Họ tên <span class="req">*</span></label>
                <div class="input-shell">
                  <input v-model="form.hoTen" type="text" placeholder="Nhập họ tên" maxlength="100" />
                  <i class="fa-regular fa-user icon"></i>
                </div>
                <span class="error" v-if="errors.hoTen">{{ errors.hoTen }}</span>
              </div>
              <div class="form-group">
                <label>Email <span class="req">*</span></label>
                <div class="input-shell">
                  <input v-model="form.email" type="email" placeholder="Nhập email" />
                  <i class="fa-regular fa-envelope icon"></i>
                </div>
                <span class="error" v-if="errors.email">{{ errors.email }}</span>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>Số điện thoại <span class="req">*</span></label>
                <div class="input-shell">
                  <input v-model="form.soDienThoai" type="text" placeholder="Nhập SĐT" maxlength="15" />
                  <i class="fa-solid fa-phone icon"></i>
                </div>
                <span class="error" v-if="errors.soDienThoai">{{ errors.soDienThoai }}</span>
              </div>
              <div class="form-group">
                <label>Ngày sinh</label>
                <div class="input-shell">
                  <input v-model="form.ngaySinh" type="date" />
                  <i class="fa-regular fa-calendar icon"></i>
                </div>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>Tên đăng nhập <span class="req">*</span></label>
                <div class="input-shell">
                  <input v-model="form.taiKhoan" type="text" placeholder="Tên đăng nhập" maxlength="50" />
                  <i class="fa-solid fa-at icon"></i>
                </div>
                <span class="error" v-if="errors.taiKhoan">{{ errors.taiKhoan }}</span>
              </div>
              <div class="form-group">
                <label>Giới tính</label>
                <select v-model="form.gioiTinh">
                  <option :value="null">-- Chọn --</option>
                  <option :value="true">Nam</option>
                  <option :value="false">Nữ</option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>Mật khẩu <span class="req">*</span></label>
                <div class="input-shell">
                  <input v-model="form.matKhau" :type="showPw ? 'text' : 'password'" placeholder="Mật khẩu" />
                  <button type="button" class="icon-btn" @click="showPw = !showPw">
                    <i :class="showPw ? 'fa-regular fa-eye-slash' : 'fa-regular fa-eye'"></i>
                  </button>
                </div>
                <span class="error" v-if="errors.matKhau">{{ errors.matKhau }}</span>
              </div>
              <div class="form-group">
                <label>Xác nhận mật khẩu <span class="req">*</span></label>
                <div class="input-shell">
                  <input v-model="form.xacNhanMK" :type="showPw2 ? 'text' : 'password'" placeholder="Nhập lại mật khẩu" />
                  <button type="button" class="icon-btn" @click="showPw2 = !showPw2">
                    <i :class="showPw2 ? 'fa-regular fa-eye-slash' : 'fa-regular fa-eye'"></i>
                  </button>
                </div>
                <span class="error" v-if="errors.xacNhanMK">{{ errors.xacNhanMK }}</span>
              </div>
            </div>

            <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

            <button type="submit" class="btn-register" :disabled="submitting">
              {{ submitting ? 'Đang xử lý...' : 'Đăng ký' }}
            </button>

            <p class="login-link">
              Đã có tài khoản? <router-link to="/client/login">Đăng nhập</router-link>
            </p>
          </form>
        </div>

        <div class="card-right">
          <img src="https://images.unsplash.com/photo-1483985988355-763728e1935b?w=800&q=80" alt="Fashion" />
          <div class="right-overlay">
            <h3>YourChoice</h3>
            <p>Phong cách thời trang hiện đại</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { registerCustomer } from '@/api/clientApi'
import { toastSuccess } from '@/utils/toast'
import Swal from 'sweetalert2'

const router = useRouter()
const showLogo = ref(true)
const showPw = ref(false)
const showPw2 = ref(false)
const submitting = ref(false)
const errorMessage = ref('')

const form = reactive({
  hoTen: '', email: '', soDienThoai: '', ngaySinh: '',
  taiKhoan: '', matKhau: '', xacNhanMK: '', gioiTinh: null,
})
const errors = reactive({})

const validate = () => {
  Object.keys(errors).forEach(k => delete errors[k])
  let valid = true
  if (!form.hoTen.trim()) { errors.hoTen = 'Vui lòng nhập họ tên'; valid = false }
  if (!form.email.trim()) { errors.email = 'Vui lòng nhập email'; valid = false }
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) { errors.email = 'Email không hợp lệ'; valid = false }
  if (!form.soDienThoai.trim()) { errors.soDienThoai = 'Vui lòng nhập SĐT'; valid = false }
  else if (!/^(0\d{9,10})$/.test(form.soDienThoai)) { errors.soDienThoai = 'SĐT không hợp lệ'; valid = false }
  if (!form.taiKhoan.trim()) { errors.taiKhoan = 'Vui lòng nhập tên đăng nhập'; valid = false }
  else if (form.taiKhoan.trim().length < 4) { errors.taiKhoan = 'Tối thiểu 4 ký tự'; valid = false }
  if (!form.matKhau) { errors.matKhau = 'Vui lòng nhập mật khẩu'; valid = false }
  else if (form.matKhau.length < 6) { errors.matKhau = 'Tối thiểu 6 ký tự'; valid = false }
  if (form.matKhau !== form.xacNhanMK) { errors.xacNhanMK = 'Mật khẩu không khớp'; valid = false }
  return valid
}

const handleRegister = async () => {
  errorMessage.value = ''
  if (!validate()) return

  submitting.value = true
  try {
    const fd = new FormData()
    fd.append('hoTen', form.hoTen.trim())
    fd.append('email', form.email.trim())
    fd.append('soDienThoai', form.soDienThoai.trim())
    fd.append('taiKhoan', form.taiKhoan.trim())
    fd.append('matKhau', form.matKhau)
    if (form.ngaySinh) fd.append('ngaySinh', form.ngaySinh)
    if (form.gioiTinh !== null) fd.append('gioiTinh', form.gioiTinh)

    await registerCustomer(fd)
    toastSuccess('Đăng ký thành công!')
    await Swal.fire({ icon: 'success', title: 'Đăng ký thành công!', text: 'Bạn có thể đăng nhập ngay', confirmButtonColor: '#0f172a' })
    router.push('/client/login')
  } catch (e) {
    errorMessage.value = e.response?.data?.message || 'Đăng ký thất bại. Vui lòng thử lại.'
  } finally { submitting.value = false }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh; display: grid; place-items: center; position: relative; padding: 24px;
  background: radial-gradient(900px 600px at 20% 20%, rgba(0, 255, 255, 0.18), transparent 60%),
              radial-gradient(1000px 700px at 80% 30%, rgba(0, 140, 255, 0.22), transparent 62%),
              linear-gradient(135deg, #001a6b 0%, #0047ff 45%, #00b7ff 100%);
}

.btn-back {
  position: absolute; top: 18px; left: 18px; z-index: 10;
  display: inline-flex; align-items: center; gap: 10px;
  padding: 10px 14px; border-radius: 999px; border: 1px solid rgba(255,255,255,0.25);
  background: rgba(255,255,255,0.16); color: #fff; font-size: 14px; cursor: pointer; backdrop-filter: blur(8px);
}
.btn-back:hover { background: rgba(255,255,255,0.28); }

.register-wrap { position: relative; z-index: 1; width: 100%; max-width: 920px; }

.register-card { display: flex; background: #fff; border-radius: 20px; overflow: hidden; box-shadow: 0 20px 60px rgba(0,0,0,0.15); min-height: 580px; }

.card-left { flex: 1; padding: 40px 35px; }
.card-right { width: 320px; position: relative; overflow: hidden; }
.card-right img { width: 100%; height: 100%; object-fit: cover; }
.right-overlay { position: absolute; bottom: 0; left: 0; right: 0; padding: 30px; background: linear-gradient(transparent, rgba(0,0,0,0.7)); color: #fff; }
.right-overlay h3 { font-size: 24px; font-weight: 800; letter-spacing: 2px; }
.right-overlay p { font-size: 14px; color: #e2e8f0; }

.brand { display: flex; align-items: center; gap: 12px; margin-bottom: 25px; }
.brand-logo { width: 45px; height: 45px; border-radius: 12px; }
.brand-text h2 { font-size: 22px; color: #0f172a; margin: 0; }
.brand-text p { font-size: 13px; color: #64748b; margin: 0; }

.form-row { display: flex; gap: 14px; }
.form-group { flex: 1; margin-bottom: 14px; }
.form-group label { display: block; font-size: 12px; font-weight: 600; color: #334155; margin-bottom: 4px; }
.req { color: #ef4444; }

.input-shell { position: relative; display: flex; align-items: center; }
.input-shell input { width: 100%; padding: 9px 34px 9px 12px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 13px; outline: none; box-sizing: border-box; }
.input-shell input:focus { border-color: #1e3a8a; }
.input-shell .icon { position: absolute; right: 10px; color: #94a3b8; font-size: 14px; pointer-events: none; }
.icon-btn { position: absolute; right: 8px; background: none; border: none; color: #94a3b8; cursor: pointer; font-size: 14px; }

.form-group select { width: 100%; padding: 9px 12px; border: 1px solid #e2e8f0; border-radius: 8px; font-size: 13px; outline: none; box-sizing: border-box; }
.form-group select:focus { border-color: #1e3a8a; }

.error { font-size: 11px; color: #ef4444; display: block; margin-top: 2px; }
.error-message { color: #ef4444; font-size: 13px; text-align: center; margin: 8px 0; }

.btn-register {
  width: 100%; padding: 11px; margin-top: 5px; background: linear-gradient(135deg, #001a6b, #0047ff);
  color: #fff; border: none; border-radius: 10px; font-size: 15px; font-weight: 700; cursor: pointer; transition: 0.3s;
}
.btn-register:hover { opacity: 0.9; transform: translateY(-1px); }
.btn-register:disabled { opacity: 0.6; cursor: not-allowed; transform: none; }

.login-link { text-align: center; font-size: 13px; color: #64748b; margin-top: 15px; }
.login-link a { color: #1e3a8a; font-weight: 600; text-decoration: none; }
.login-link a:hover { text-decoration: underline; }

@media (max-width: 768px) {
  .card-right { display: none; }
  .form-row { flex-direction: column; gap: 0; }
  .card-left { padding: 30px 25px; }
}
</style>
