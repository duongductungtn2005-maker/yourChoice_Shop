<template>
  <div class="login-page">
    <button class="btn-back" @click="handleGoBack" title="Trở lại">
      <i class="fa-solid fa-arrow-left"></i>
      <span>Trở lại</span>
    </button>

    <div class="login-wrap">
      <div class="login-card">
        <!-- LEFT -->
        <div class="left">
          <!-- Logo area -->
          <div class="brand">
            <!-- ✅ Đổi đường dẫn logo của bạn ở đây -->
            <img
              v-if="showLogo"
              class="brand-logo"
              src="@/img/logo1.png"
              alt="YourChoice Logo"
              @error="onLogoError"
            />
            <div class="brand-text">
              <h2 class="title">Xin chào quý khách</h2>
              <p class="subtitle">Vui lòng nhập thông tin của bạn</p>
            </div>
          </div>

          <form class="form" @submit.prevent="handleLogin">
            <div class="form-group">
              <label class="label">Tài khoản</label>
              <div class="input-shell">
                <input
                  v-model="username"
                  type="text"
                  class="input"
                  placeholder="Nhập tài khoản"
                  autocomplete="username"
                />
                <i class="fa-regular fa-user icon"></i>
              </div>
            </div>

            <div class="form-group">
              <label class="label">Mật khẩu</label>
              <div class="input-shell">
                <input
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  class="input"
                  placeholder="Nhập mật khẩu"
                  autocomplete="current-password"
                />
                <button
                  type="button"
                  class="icon-btn"
                  @click="showPassword = !showPassword"
                  :title="showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                >
                  <i :class="showPassword ? 'fa-regular fa-eye-slash' : 'fa-regular fa-eye'"></i>
                </button>
              </div>
            </div>

            <div class="row">
              <label class="remember">
                <input type="checkbox" />
                <span>Remember for 30 days</span>
              </label>
              <button type="button" class="link">Quên mật khẩu?</button>
            </div>

            <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

            <button type="submit" class="btn-login" :disabled="!canSubmit">
              Đăng nhập
            </button>

            <div class="divider">
              <span>hoặc</span>
            </div>

            <!-- Social buttons (UI only) -->
            <div class="social">
              <button type="button" class="social-btn" title="Apple">
                <i class="fa-brands fa-apple"></i>
              </button>
              <button type="button" class="social-btn" title="Google">
                <i class="fa-brands fa-google"></i>
              </button>
              <button type="button" class="social-btn" title="Facebook">
                <i class="fa-brands fa-facebook-f"></i>
              </button>
            </div>
          </form>
        </div>

        <!-- RIGHT (SLIDER) -->
        <div class="right">
          <div class="slider" @mouseenter="pauseAuto()" @mouseleave="resumeAuto()">
            <div class="slides" :style="{ transform: `translateX(-${activeIndex * 100}%)` }">
              <div class="slide" v-for="(s, idx) in slides" :key="idx">
                <img class="slide-img" :src="s.img" :alt="s.title" @error="onSlideError(idx)" />
                <div class="slide-overlay">
                  <p class="slide-title">{{ s.title }}</p>
                  <p class="slide-desc">{{ s.desc }}</p>
                </div>
              </div>
            </div>

            <!-- Controls -->
            <button class="nav prev" type="button" @click="prev()" aria-label="Previous slide">
              <i class="fa-solid fa-chevron-left"></i>
            </button>
            <button class="nav next" type="button" @click="next()" aria-label="Next slide">
              <i class="fa-solid fa-chevron-right"></i>
            </button>

            <div class="dots" role="tablist" aria-label="Product slides">
              <button
                v-for="(_, i) in slides"
                :key="i"
                type="button"
                class="dot"
                :class="{ active: i === activeIndex }"
                @click="go(i)"
                :aria-label="`Go to slide ${i + 1}`"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { toastSuccess } from '@/utils/toast';
import request from '@/services/request';
import { login as authLogin } from '@/services/auth';
<<<<<<< Updated upstream
=======
import { useCartStore } from '@/stores/cart';
import { jwtDecode } from 'jwt-decode';
>>>>>>> Stashed changes

const router = useRouter();
const username = ref('');
const password = ref('');
const showPassword = ref(false);
const errorMessage = ref('');
const canSubmit = computed(() => username.value.trim() !== '' && password.value.trim() !== '');

// ===== Logo safe load =====
const showLogo = ref(true);
const onLogoError = () => {
  showLogo.value = false;
};

// ===== Slider (Right) =====
// ✅ Bạn thay các ảnh này thành ảnh sản phẩm của bạn (assets hoặc URL).
// Ví dụ dùng assets: new URL('@/img/product1.jpg', import.meta.url).href
const slides = ref([
  {
    img: 'https://i.pinimg.com/1200x/08/bf/8f/08bf8f4dfe16bec95cc84ca3acd8887a.jpg',
    title: 'Áo sơ mi nữ - Best Seller',
    desc: 'Form chuẩn, dễ phối đồ.'
  },
  {
    img: 'https://i.pinimg.com/736x/31/9d/1d/319d1d917fd49f234e05b2d14538fcac.jpg',
    title: 'New Collection',
    desc: 'Chất vải thoáng mát, mềm.'
  },
  {
    img: 'https://i.pinimg.com/736x/9a/0c/ed/9a0cedacf563bf0db77f93383bf36ff9.jpg',
    title: 'Office Style',
    desc: 'Lịch sự, thanh lịch cho công sở.'
  }
]);

const activeIndex = ref(0);
const intervalMs = 3500;
let timer = null;

const go = (i) => {
  activeIndex.value = i;
};

const next = () => {
  activeIndex.value = (activeIndex.value + 1) % slides.value.length;
};

const prev = () => {
  activeIndex.value = (activeIndex.value - 1 + slides.value.length) % slides.value.length;
};

const startAuto = () => {
  stopAuto();
  timer = setInterval(() => next(), intervalMs);
};

const stopAuto = () => {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
};

const pauseAuto = () => stopAuto();
const resumeAuto = () => startAuto();

const onSlideError = (idx) => {
  // Nếu ảnh lỗi, đổi sang placeholder (tránh crash/slider trắng)
  slides.value[idx].img =
    'https://images.unsplash.com/photo-1520975911159-9c88d1f2e9b2?auto=format&fit=crop&w=1200&q=80';
};

onMounted(() => startAuto());
onBeforeUnmount(() => stopAuto());

// ===== LOGIN LOGIC =====
const authenticateCustomer = async (usernameValue, passwordValue) => {
  const finalUsername = String(usernameValue || '').trim();
  const finalPassword = String(passwordValue || '').trim();
  if (!finalUsername || !finalPassword) return false;

  try {
    const response = await request.get('/khach-hang/authenticate', {
      params: { username: finalUsername, password: finalPassword }
    });
    return response?.data?.authenticated === true;
  } catch {
    return false;
  }
};

const authenticateEmployee = async (usernameValue, passwordValue) => {
  const finalUsername = String(usernameValue || '').trim();
  const finalPassword = String(passwordValue || '').trim();
  if (!finalUsername || !finalPassword) return null;

  try {
    const response = await request.get('/nhan-vien/authenticate', {
      params: { username: finalUsername, password: finalPassword }
    });
    if (response?.data?.authenticated === true) {
      return response?.data?.employee || null;
    }
    return null;
  } catch {
    return null;
  }
};

const determineRole = (employee) => {
  if (!employee || !employee.quyenHan) return 'STAFF';

  const roleId = Number(employee.quyenHan.id);
  if (!Number.isNaN(roleId) && roleId === 1) return 'ADMIN';

  const roleName = String(employee.quyenHan.tenQuyenHan || '').toUpperCase().trim();
  if (roleName.includes('ADMIN') || roleName.includes('QUẢN TRỊ') || roleName.includes('QUANTRI')) {
    return 'ADMIN';
  }
  return 'STAFF';
};

const handleLogin = async () => {
  if (!canSubmit.value) {
    errorMessage.value = 'Vui lòng nhập đầy đủ tài khoản và mật khẩu.';
    return;
  }

  errorMessage.value = '';

  // — Chỉ đăng nhập Nhân viên / Admin —
  const result = await authenticateEmployee(username.value, password.value);
  if (result && result.employee) {
    const employeeData = result.employee;
    const token = result.token;
    
    // ===== CHÍNH LÀ ĐOẠN NÀY: MỔ TOKEN ĐỂ LẤY QUYỀN =====
    let role = 'STAFF'; // Đặt mặc định đề phòng lỗi
    try {
      const decoded = jwtDecode(token);
      if (decoded.role) {
        role = decoded.role; // Lấy chữ 'ADMIN' hoặc 'STAFF' từ bên trong token
      }
      console.log("Quyền lấy từ Token:", role); // Log ra để check cho sướng mắt
    } catch (error) {
      console.error("Không thể giải mã Token:", error);
    }
    // ====================================================

    authLogin({ token, role, user: employeeData });
    toastSuccess(`Đăng nhập thành công! Xin chào ${employeeData.tenNhanVien}`);
    
    // Điều hướng theo quyền mới lấy được
    router.push(role === 'ADMIN' ? '/admin/home' : '/staff/giao-ca'); 
    
  } else {
    errorMessage.value = 'Sai tài khoản hoặc mật khẩu. Vui lòng thử lại.';
  }

  // 2. Thử đăng nhập Khách hàng (từ database)
  const isCustomer = await authenticateCustomer(username.value, password.value);
  if (isCustomer) {
    authLogin({ role: 'CUSTOMER' });
    toastSuccess('Đăng nhập thành công!');
    router.push('/');
    return;
  }

  // Không khớp gì
  errorMessage.value = 'Mật khẩu hoặc tài khoản không đúng. Vui lòng thử lại.';
};
const handleGoBack = () => {
  router.push('/');
};
</script>

<style scoped>
/* ===== Page ===== */
.login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  position: relative;
  padding: 24px;
  overflow: hidden;

  /* ✅ Neon sky-blue (đậm) */
  background: radial-gradient(900px 600px at 20% 20%, rgba(0, 255, 255, 0.18), transparent 60%),
              radial-gradient(1000px 700px at 80% 30%, rgba(0, 140, 255, 0.22), transparent 62%),
              linear-gradient(135deg, #001a6b 0%, #0047ff 45%, #00b7ff 100%);
}

/* Glow mềm cho cảm giác neon */
.login-page::before {
  content: "";
  position: absolute;
  inset: -120px;
  background:
    radial-gradient(500px 300px at 25% 30%, rgba(0, 220, 255, 0.22), transparent 60%),
    radial-gradient(520px 320px at 75% 70%, rgba(0, 120, 255, 0.18), transparent 62%);
  filter: blur(22px);
  pointer-events: none;
}

/* Back button */
.btn-back {
  position: absolute;
  top: 18px;
  left: 18px;
  z-index: 10;

  display: inline-flex;
  align-items: center;
  gap: 10px;

  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.25);
  background: rgba(255, 255, 255, 0.16);
  color: rgba(255, 255, 255, 0.95);
  cursor: pointer;
  transition: transform 160ms ease, background 160ms ease;
  backdrop-filter: blur(10px);
}

.btn-back:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.22);
}

.login-wrap {
  width: min(980px, 96vw);
}

/* ===== Card ===== */
.login-card {
  background: #ffffff;
  border-radius: 26px;
  padding: 18px;
  box-shadow: 0 28px 80px rgba(0, 0, 0, 0.25);
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
  border: 6px solid rgba(255, 255, 255, 0.28); /* subtle inner border feel */
}

/* Left */
.left {
  border-radius: 20px;
  padding: 26px 26px 22px;
  display: flex;
  flex-direction: column;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}

.brand-logo {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  object-fit: cover;
  border: 1px solid rgba(17, 24, 39, 0.12);
  background: #f3f4f6;
}

.title {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
}

.subtitle {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

/* Form */
.form {
  margin-top: 6px;
}

.form-group {
  margin-top: 14px;
}

.label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #334155;
  font-size: 13px;
}

.input-shell {
  position: relative;
}

.input {
  width: 100%;
  height: 44px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  outline: none;
  padding: 0 44px 0 14px;
  font-size: 14px;
  color: #0f172a;
  background: #fff;
  transition: border-color 160ms ease, box-shadow 160ms ease;
}

.input:focus {
  border-color: rgba(29, 59, 211, 0.7);
  box-shadow: 0 0 0 4px rgba(29, 59, 211, 0.12);
}

.icon {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #94a3b8;
}

.icon-btn {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  height: 34px;
  width: 34px;
  border-radius: 999px;
  border: none;
  background: transparent;
  color: #64748b;
  cursor: pointer;
}

.icon-btn:hover {
  background: rgba(2, 6, 23, 0.04);
  color: #0f172a;
}

.row {
  margin-top: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.remember {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: #475569;
  font-size: 12px;
  user-select: none;
}

.remember input {
  width: 14px;
  height: 14px;
  accent-color: #1d3bd3;
}

.link {
  border: none;
  background: transparent;
  color: #64748b;
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}

.link:hover {
  text-decoration: underline;
  color: #334155;
}

.error-message {
  margin-top: 10px;
  color: #dc2626;
  font-size: 13px;
  font-weight: 600;
}

.btn-login {
  margin-top: 14px;
  width: 100%;
  height: 46px;
  border-radius: 999px;
  border: none;
  background: #1d3bd3;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  transition: transform 140ms ease, filter 140ms ease, opacity 140ms ease;
}

.btn-login:hover {
  transform: translateY(-1px);
  filter: brightness(1.03);
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.divider {
  margin: 16px 0 12px;
  display: grid;
  place-items: center;
  position: relative;
}

.divider::before {
  content: "";
  height: 1px;
  width: 100%;
  background: #e2e8f0;
  position: absolute;
  left: 0;
  top: 50%;
}

.divider span {
  position: relative;
  background: #fff;
  padding: 0 10px;
  color: #94a3b8;
  font-size: 12px;
}

.social {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.social-btn {
  width: 40px;
  height: 40px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #fff;
  cursor: pointer;
  color: #0f172a;
  transition: transform 140ms ease, box-shadow 140ms ease;
}

.social-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 18px rgba(2, 6, 23, 0.08);
}

/* Right (Slider) */
.right {
  border-radius: 20px;
  overflow: hidden;
  background: #0b1b5e;
  position: relative;
}

.slider {
  height: 100%;
  min-height: 430px;
  position: relative;
}

.slides {
  height: 100%;
  display: flex;
  transition: transform 420ms ease;
}

.slide {
  min-width: 100%;
  height: 100%;
  position: relative;
}

.slide-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  filter: saturate(1.05);
}

.slide-overlay {
  position: absolute;
  left: 16px;
  right: 16px;
  bottom: 16px;
  padding: 14px 14px;
  border-radius: 14px;
  color: rgba(255, 255, 255, 0.92);
  background: linear-gradient(180deg, rgba(2, 6, 23, 0.0), rgba(2, 6, 23, 0.55));
  backdrop-filter: blur(6px);
}

.slide-title {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
}

.slide-desc {
  margin: 6px 0 0;
  font-size: 12px;
  opacity: 0.9;
}

/* Controls */
.nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  background: rgba(2, 6, 23, 0.35);
  color: #fff;
  cursor: pointer;
  display: grid;
  place-items: center;
  backdrop-filter: blur(8px);
  transition: background 160ms ease, transform 160ms ease;
}

.nav:hover {
  background: rgba(2, 6, 23, 0.48);
}

.nav.prev {
  left: 12px;
}

.nav.next {
  right: 12px;
}

.dots {
  position: absolute;
  bottom: 14px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 999px;
  background: rgba(2, 6, 23, 0.25);
  backdrop-filter: blur(8px);
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  border: none;
  background: rgba(255, 255, 255, 0.45);
  cursor: pointer;
  transition: width 180ms ease, background 180ms ease;
}

.dot.active {
  width: 18px;
  background: rgba(255, 255, 255, 0.9);
}

/* Responsive */
@media (max-width: 920px) {
  .login-card {
    grid-template-columns: 1fr;
  }
  .slider {
    min-height: 300px;
  }
}
</style>