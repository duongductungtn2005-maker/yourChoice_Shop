<template>
  <div class="login-page">
    <button class="btn-back" @click="handleGoBack" title="Trở lại">
      <i class="fa-solid fa-arrow-left"></i> Trở lại
    </button>
    
    <div class="login-card">
      <div class="logo">
        <h2>YourChoice Shop</h2>
        <p>Đăng nhập hệ thống quản trị</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>Tài khoản</label>
          <input 
            type="text" 
            placeholder="Nhập tài khoản" 
            v-model="username" 
            class="form-control"
          />
        </div>

        <div class="form-group">
          <label>Mật khẩu</label>
          <div class="password-wrap">
            <input 
              :type="showPassword ? 'text' : 'password'" 
              placeholder="Nhập mật khẩu" 
              v-model="password"
              class="form-control password-input"
            />
            <button
              type="button"
              class="toggle-password"
              @click="showPassword = !showPassword"
              :title="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
            >
              <i :class="showPassword ? 'fa-regular fa-eye-slash' : 'fa-regular fa-eye'"></i>
            </button>
          </div>
        </div>

        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <button type="submit" class="btn-login">Đăng nhập</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { toastSuccess } from '@/utils/toast';

const router = useRouter();
const username = ref('');
const password = ref('');
const showPassword = ref(false);
const errorMessage = ref('');
const canSubmit = computed(() => username.value.trim() !== '' && password.value.trim() !== '');

const resolveRoleFromUsername = (value) => {
  const account = String(value || '').toLowerCase();
  if (account.includes('admin') || account.includes('quantri') || account.includes('quan-tri')) return 'ADMIN';
  if (account.includes('employee') || account.includes('nhanvien') || account.includes('nhan-vien')) return 'STAFF';
  if (account.includes('customer') || account.includes('khachhang') || account.includes('khach-hang')) return 'CUSTOMER';
  return null;
};

const handleLogin = () => {
  if (!canSubmit.value) {
    errorMessage.value = 'Vui lòng nhập đầy đủ tài khoản và mật khẩu.';
    return;
  }

  if (password.value !== '123456') {
    errorMessage.value = 'Mật khẩu hoặc tài khoản không đúng. Vui lòng thử lại.';
    return;
  }

  errorMessage.value = '';
  // Giả lập đăng nhập thành công
  // Sau này sẽ gọi API Login ở đây
  const role = resolveRoleFromUsername(username.value);
  if (!role) {
    errorMessage.value = 'Mật khẩu hoặc tài khoản không đúng. Vui lòng thử lại.';
    return;
  }

  localStorage.setItem('token', 'demo-token');
  localStorage.setItem('userRole', role);
  toastSuccess('Đăng nhập thành công!');

  if (role === 'CUSTOMER') {
    router.push('/');
    return;
  }

  if (role === 'STAFF') {
    router.push('/staff/pos');
    return;
  }

  router.push('/admin/dashboard');
};

const handleGoBack = () => {
  router.push('/');
};
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f1f5f9;
  position: relative;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.logo {
  text-align: center;
  margin-bottom: 30px;
}
.logo h2 {
  color: #0f172a;
  margin-bottom: 8px;
}
.logo p {
  color: #64748b;
}

.form-group {
  margin-bottom: 20px;
}
.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #334155;
}

.form-control {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  outline: none;
  font-size: 14px;
}
.form-control:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.password-wrap {
  position: relative;
}

.password-input {
  padding-right: 42px;
}

.password-input::-ms-reveal,
.password-input::-ms-clear {
  display: none;
}

.toggle-password {
  position: absolute;
  top: 50%;
  right: 10px;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  color: #64748b;
  cursor: pointer;
  padding: 4px;
  line-height: 1;
}

.toggle-password:hover {
  color: #1e293b;
}

.btn-login {
  width: 100%;
  padding: 12px;
  background-color: #0f172a;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-login:hover {
  background-color: #1e293b;
}
.error-message {
  margin-top: -8px;
  margin-bottom: 14px;
  color: #dc2626;
  font-size: 13px;
}

.btn-back {
  position: absolute;
  top: 20px;
  left: 20px;
  background: white;
  border: 1px solid #cbd5e1;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #0f172a;
  transition: 0.2s;
}

.btn-back:hover {
  background: #f1f5f9;
  border-color: #94a3b8;
  color: #1e293b;
}

.btn-back i {
  font-size: 14px;
}
</style>