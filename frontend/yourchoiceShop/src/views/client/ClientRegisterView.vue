<template>
  <div class="client-register-section">
    <div class="register-container">
      <!-- Logo -->
      <div class="register-logo">
        <img v-if="showLogo" src="@/img/logo1.png" alt="YourChoice Logo" @error="showLogo = false" />
      </div>

      <div class="register-box">
        <h2 class="register-title">Đăng ký tài khoản</h2>
        <p class="register-subtitle">Tạo tài khoản để mua sắm tại YourChoiceShop</p>

        <form @submit.prevent="handleRegister" class="register-form">
          <div class="form-group">
            <label class="form-label">Họ và tên <span class="req">*</span></label>
            <div class="input-wrapper">
              <i class="fa-regular fa-user input-icon"></i>
              <input v-model="form.hoTen" type="text" class="form-input" placeholder="Nhập họ và tên" maxlength="100" />
            </div>
            <span class="field-error" v-if="errors.hoTen">{{ errors.hoTen }}</span>
          </div>

          <div class="form-group">
            <label class="form-label">Email <span class="req">*</span></label>
            <div class="input-wrapper">
              <i class="fa-regular fa-envelope input-icon"></i>
              <input v-model="form.email" type="email" class="form-input" placeholder="Nhập email" />
            </div>
            <span class="field-error" v-if="errors.email">{{ errors.email }}</span>
          </div>

          <div class="form-group">
            <label class="form-label">Số điện thoại</label>
            <div class="input-wrapper">
              <i class="fa-solid fa-phone input-icon"></i>
              <input v-model="form.soDienThoai" type="text" class="form-input" placeholder="Nhập số điện thoại" maxlength="15" />
            </div>
            <span class="field-error" v-if="errors.soDienThoai">{{ errors.soDienThoai }}</span>
          </div>

          <div class="form-group">
            <label class="form-label">Mật khẩu <span class="req">*</span></label>
            <div class="input-wrapper">
              <i class="fa-solid fa-lock input-icon"></i>
              <input
                v-model="form.matKhau"
                :type="showPw ? 'text' : 'password'"
                class="form-input"
                placeholder="Nhập mật khẩu"
              />
              <button type="button" class="toggle-pw" @click="showPw = !showPw">
                <i :class="showPw ? 'fa-regular fa-eye-slash' : 'fa-regular fa-eye'"></i>
              </button>
            </div>
            <span class="field-error" v-if="errors.matKhau">{{ errors.matKhau }}</span>
          </div>

          <div class="form-group">
            <label class="form-label">Xác nhận mật khẩu <span class="req">*</span></label>
            <div class="input-wrapper">
              <i class="fa-solid fa-shield-halved input-icon"></i>
              <input
                v-model="form.xacNhanMK"
                :type="showPw2 ? 'text' : 'password'"
                class="form-input"
                placeholder="Nhập lại mật khẩu"
              />
              <button type="button" class="toggle-pw" @click="showPw2 = !showPw2">
                <i :class="showPw2 ? 'fa-regular fa-eye-slash' : 'fa-regular fa-eye'"></i>
              </button>
            </div>
            <span class="field-error" v-if="errors.xacNhanMK">{{ errors.xacNhanMK }}</span>
          </div>

          <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>

          <button type="submit" class="btn-submit" :disabled="submitting">
            {{ submitting ? 'Đang xử lý...' : 'Đăng ký' }}
          </button>
        </form>

        <p class="login-link">
          Đã có tài khoản?
          <router-link to="/client/login" class="link-highlight">Đăng nhập</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { toastSuccess } from '@/utils/toast'

const router = useRouter()
const showLogo = ref(true)
const showPw = ref(false)
const showPw2 = ref(false)
const submitting = ref(false)
const errorMessage = ref('')

const form = reactive({
  hoTen: '',
  email: '',
  soDienThoai: '',
  matKhau: '',
  xacNhanMK: ''
})

const errors = reactive({
  hoTen: '',
  email: '',
  soDienThoai: '',
  matKhau: '',
  xacNhanMK: ''
})

const validate = () => {
  let valid = true
  Object.keys(errors).forEach(k => errors[k] = '')

  if (!form.hoTen.trim()) { errors.hoTen = 'Vui lòng nhập họ tên'; valid = false }
  if (!form.email.trim()) { errors.email = 'Vui lòng nhập email'; valid = false }
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) { errors.email = 'Email không hợp lệ'; valid = false }
  if (form.soDienThoai.trim() && !/^0\d{9,10}$/.test(form.soDienThoai.trim())) { errors.soDienThoai = 'SĐT không hợp lệ'; valid = false }
  if (!form.matKhau) { errors.matKhau = 'Vui lòng nhập mật khẩu'; valid = false }
  else if (form.matKhau.length < 6) { errors.matKhau = 'Mật khẩu tối thiểu 6 ký tự'; valid = false }
  if (!form.xacNhanMK) { errors.xacNhanMK = 'Vui lòng xác nhận mật khẩu'; valid = false }
  else if (form.xacNhanMK !== form.matKhau) { errors.xacNhanMK = 'Mật khẩu không khớp'; valid = false }

  return valid
}

const handleRegister = async () => {
  errorMessage.value = ''
  if (!validate()) return

  submitting.value = true
  try {
    // TODO: Gọi API đăng ký thật ở đây
    // await request.post('/khach-hang/register', { ... })
    toastSuccess('Đăng ký thành công!')
    router.push('/client/login')
  } catch (e) {
    errorMessage.value = e.response?.data?.message || 'Đăng ký thất bại. Vui lòng thử lại.'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.client-register-section {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
  padding: 60px 20px;
  background: #f8fafc;
}

.register-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 480px;
}

.register-logo {
  margin-bottom: 30px;
}

.register-logo img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 50%;
  background: #fff;
  padding: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.register-box {
  width: 100%;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.register-title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  text-align: center;
}

.register-subtitle {
  margin: 6px 0 28px;
  font-size: 14px;
  color: #64748b;
  text-align: center;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}

.req {
  color: #dc2626;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  color: #94a3b8;
  font-size: 15px;
  pointer-events: none;
}

.form-input {
  width: 100%;
  height: 48px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 0 44px 0 42px;
  font-size: 14px;
  color: #0f172a;
  background: #fff;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-input:focus {
  border-color: #1e3a8a;
  box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.1);
}

.toggle-pw {
  position: absolute;
  right: 8px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: background 0.15s;
}

.toggle-pw:hover {
  background: rgba(0, 0, 0, 0.04);
  color: #0f172a;
}

.field-error {
  color: #dc2626;
  font-size: 12px;
}

.error-msg {
  margin: 0;
  color: #dc2626;
  font-size: 13px;
  font-weight: 600;
}

.btn-submit {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.15s, filter 0.15s, opacity 0.15s;
}

.btn-submit:hover {
  transform: translateY(-1px);
  filter: brightness(1.08);
}

.btn-submit:disabled {
  opacity: 0.55;
  cursor: not-allowed;
  transform: none;
}

.login-link {
  margin: 24px 0 0;
  text-align: center;
  font-size: 14px;
  color: #64748b;
}

.link-highlight {
  color: #1e3a8a;
  font-weight: 700;
  text-decoration: none;
}

.link-highlight:hover {
  text-decoration: underline;
}

@media (max-width: 520px) {
  .register-box {
    padding: 28px 20px;
  }
}
</style>
