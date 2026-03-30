<template>
  <div class="client-login-section">
    <div class="login-container">
      <!-- Logo -->
      <div class="login-logo">
        <img v-if="showLogo" src="@/img/logo1.png" alt="YourChoice Logo" @error="showLogo = false" />
      </div>

      <div class="login-box">
        <h2 class="login-title">Đăng nhập</h2>
        <p class="login-subtitle">Chào mừng bạn đến với YourChoiceShop</p>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label class="form-label">Tên tài khoản hoặc Email</label>
            <div class="input-wrapper">
              <i class="fa-regular fa-user input-icon"></i>
              <input
                v-model="username"
                type="text"
                class="form-input"
                placeholder="Nhập tên tài khoản hoặc email"
                autocomplete="username"
              />
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">Mật khẩu</label>
            <div class="input-wrapper">
              <i class="fa-solid fa-lock input-icon"></i>
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                class="form-input"
                placeholder="Nhập mật khẩu"
                autocomplete="current-password"
              />
              <button
                type="button"
                class="toggle-pw"
                @click="showPassword = !showPassword"
                :title="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
              >
                <i :class="showPassword ? 'fa-regular fa-eye-slash' : 'fa-regular fa-eye'"></i>
              </button>
            </div>
          </div>

          <label class="remember-row">
            <input v-model="rememberMe" type="checkbox" />
            <span>Ghi nhớ tôi</span>
          </label>

          <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>

          <button type="submit" class="btn-submit" :disabled="!canSubmit">
            Đăng nhập
          </button>
        </form>

        <p class="register-link">
          Chưa có tài khoản?
          <router-link to="/client/register" class="link-highlight">Đăng ký ngay</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { toastSuccess } from '@/utils/toast'
import request from '@/services/request'
import { login as authLogin } from '@/services/auth'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const username = ref('')
const password = ref('')
const showPassword = ref(false)
const rememberMe = ref(false)
const errorMessage = ref('')
const showLogo = ref(true)
const canSubmit = computed(() => username.value.trim() !== '' && password.value.trim() !== '')

const handleLogin = async () => {
  if (!canSubmit.value) {
    errorMessage.value = 'Vui lòng nhập đầy đủ tài khoản và mật khẩu.'
    return
  }
  errorMessage.value = ''

  try {
    const response = await request.post('/khach-hang/authenticate', {
      username: username.value.trim(),
      password: password.value.trim()
    })
    if (response?.data?.authenticated === true && response?.data?.customer) {
      const customerData = response.data.customer
      const token = response.data.token
      authLogin({ token, role: 'CUSTOMER', user: customerData })
      useCartStore().reloadCart()
      window.dispatchEvent(new Event('auth-user-updated'))
      toastSuccess(`Đăng nhập thành công! Xin chào ${customerData.tenTaiKhoan || customerData.username || customerData.tenKhachHang || 'quý khách'}`)
      router.push('/')
      return
    }
    errorMessage.value = 'Mật khẩu hoặc tài khoản không đúng. Vui lòng thử lại.'
  } catch {
    errorMessage.value = 'Mật khẩu hoặc tài khoản không đúng. Vui lòng thử lại.'
  }
}
</script>

<style scoped>
.client-login-section {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
  padding: 60px 20px;
  background: #f8fafc;
}

.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 480px;
}

.login-logo {
  margin-bottom: 30px;
}

.login-logo img {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 50%;
  background: #fff;
  padding: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.login-box {
  width: 100%;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.login-title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  text-align: center;
}

.login-subtitle {
  margin: 6px 0 28px;
  font-size: 14px;
  color: #64748b;
  text-align: center;
}

.login-form {
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

.remember-row {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: -4px;
  color: #475569;
  font-size: 13px;
  user-select: none;
}

.remember-row input {
  width: 14px;
  height: 14px;
  accent-color: #1e3a8a;
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

.register-link {
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
  .login-box {
    padding: 28px 20px;
  }
}
</style>
