<template>
  <div class="login-page">
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
            placeholder="admin" 
            v-model="username" 
            class="form-control"
          />
        </div>

        <div class="form-group">
          <label>Mật khẩu</label>
          <input 
            type="password" 
            placeholder="••••••" 
            v-model="password"
            class="form-control"
          />
        </div>

        <button type="submit" class="btn-login">Đăng nhập</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const username = ref('admin');
const password = ref('');

const handleLogin = () => {
  // Giả lập đăng nhập thành công
  // Sau này sẽ gọi API Login ở đây
  router.push('/admin/products');
};
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f1f5f9;
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
</style>