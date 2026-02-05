<template>
  <div class="client-layout">
    <header class="site-header" :class="{ 'scrolled': isScrolled }">
      <div class="container header-inner">
        <div class="logo">
          <router-link to="/">
            <h1>YourChoice.</h1>
          </router-link>
        </div>

        <nav class="main-nav">
          <router-link to="/">Trang chủ</router-link>
          <router-link to="/shop">Sản phẩm</router-link>
          <router-link to="/about">Về chúng tôi</router-link>
          <router-link to="/contact">Liên hệ</router-link>
        </nav>

        <div class="header-actions">
          <button class="icon-btn"><i class="fas fa-search"></i></button>
          <button class="icon-btn cart-btn">
            <i class="fas fa-shopping-bag"></i>
            <span class="badge">0</span>
          </button>
          <button class="icon-btn"><i class="far fa-user"></i></button>
        </div>
      </div>
    </header>

    <main class="main-content">
      <router-view />
    </main>

    <footer class="site-footer">
      <div class="container">
        <div class="footer-grid">
          <div class="footer-col">
            <h3>YourChoice.</h3>
            <p>Thời trang công sở & dạo phố dành cho phái đẹp. Tinh tế trong từng đường kim mũi chỉ.</p>
          </div>
          <div class="footer-col">
            <h4>Liên kết</h4>
            <ul>
              <li><a href="#">Chính sách đổi trả</a></li>
              <li><a href="#">Hướng dẫn chọn size</a></li>
              <li><a href="#">Thanh toán & Vận chuyển</a></li>
            </ul>
          </div>
          <div class="footer-col">
            <h4>Liên hệ</h4>
            <p>Hotline: 0988.888.888</p>
            <p>Email: support@yourchoice.vn</p>
            <p>Store: FPT Polytechnic, Hà Nội</p>
          </div>
        </div>
        <div class="footer-bottom">
          © 2026 YourChoice Shop. All rights reserved.
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

// Hiệu ứng Header khi cuộn
const isScrolled = ref(false);
const handleScroll = () => {
  isScrolled.value = window.scrollY > 50;
};

onMounted(() => window.addEventListener('scroll', handleScroll));
onUnmounted(() => window.removeEventListener('scroll', handleScroll));
</script>

<style scoped>
/* GENERAL */
.client-layout {
  font-family: 'Segoe UI', sans-serif;
  color: #1f2937;
  background-color: #fff;
}
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* HEADER */
.site-header {
  position: fixed;
  top: 0; left: 0; width: 100%;
  background: white;
  z-index: 1000;
  transition: all 0.3s ease;
  border-bottom: 1px solid transparent;
}
.site-header.scrolled {
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  border-bottom: 1px solid #f1f5f9;
}
.header-inner {
  height: 80px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* LOGO */
.logo h1 {
  font-size: 24px;
  font-weight: 800;
  color: #000;
  margin: 0;
  letter-spacing: -0.5px;
}
.logo a { text-decoration: none; color: inherit; }

/* NAV */
.main-nav { display: flex; gap: 40px; }
.main-nav a {
  text-decoration: none;
  color: #4b5563;
  font-weight: 500;
  font-size: 15px;
  transition: color 0.2s;
  position: relative;
}
.main-nav a:hover, .main-nav a.router-link-active {
  color: #000;
}
.main-nav a::after {
  content: ''; position: absolute; bottom: -5px; left: 0; width: 0; height: 2px;
  background: #000; transition: width 0.3s;
}
.main-nav a:hover::after { width: 100%; }

/* ACTIONS */
.header-actions { display: flex; gap: 20px; align-items: center; }
.icon-btn {
  background: none; border: none; font-size: 20px; color: #1f2937;
  cursor: pointer; transition: transform 0.2s; position: relative;
}
.icon-btn:hover { transform: scale(1.1); color: #000; }
.cart-btn .badge {
  position: absolute; top: -5px; right: -8px;
  background: #000; color: white;
  font-size: 10px; width: 18px; height: 18px;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
}

/* MAIN CONTENT */
.main-content { padding-top: 80px; min-height: 80vh; }

/* FOOTER */
.site-footer {
  background: #000;
  color: #fff;
  padding: 60px 0 20px;
  margin-top: 80px;
}
.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 40px;
  margin-bottom: 40px;
}
.footer-col h3 { font-size: 20px; margin-bottom: 20px; font-weight: 700; }
.footer-col h4 { font-size: 16px; margin-bottom: 20px; font-weight: 600; text-transform: uppercase; letter-spacing: 1px; }
.footer-col ul { list-style: none; padding: 0; }
.footer-col ul li { margin-bottom: 10px; }
.footer-col a { color: #9ca3af; text-decoration: none; transition: color 0.2s; }
.footer-col a:hover { color: #fff; }
.footer-col p { color: #9ca3af; line-height: 1.6; margin-bottom: 10px; }
.footer-bottom {
  border-top: 1px solid #333;
  padding-top: 20px;
  text-align: center;
  color: #6b7280;
  font-size: 13px;
}
</style>