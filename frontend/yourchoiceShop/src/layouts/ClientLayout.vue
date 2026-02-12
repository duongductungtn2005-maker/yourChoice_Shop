<template>
  <div class="client-layout">
    <header class="site-header" :class="{ 'scrolled': isScrolled }">
      <div class="container header-inner">
        
        <div class="brand" @click="$router.push('/')">
            <div class="logo-circle">
                <img src="@/img/logo1.png" alt="Logo" @error="handleImageError" />
            </div>
            <span class="brand-name">YourChoice</span>
        </div>

        <nav class="main-nav">
          <router-link to="/" class="nav-link" active-class="active">Trang chủ</router-link>
          <router-link to="/products" class="nav-link" active-class="active">Sản phẩm</router-link>
          <router-link to="/coupons" class="nav-link" active-class="active">Săn Voucher</router-link>
          <router-link to="/news" class="nav-link" active-class="active">Tin tức</router-link>
          <router-link to="/contact" class="nav-link" active-class="active">Liên hệ</router-link>
        </nav>

        <div class="header-icons">
          <div class="search-wrap">
            <input type="text" placeholder="Tìm kiếm..." class="search-input" />
            <i class="fas fa-search search-icon"></i>
          </div>
          
          <div class="icon-item user-icon">
            <i class="far fa-user"></i>
          </div>
          
          <div class="icon-item cart-icon">
            <i class="fas fa-shopping-bag"></i>
            <span class="cart-badge">0</span>
          </div>
        </div>
      </div>
    </header>

    <main class="site-main">
      <router-view />
    </main>

    <footer class="site-footer">
      <div class="container footer-grid">
        <div class="footer-col">
          <h3>YOURCHOICE</h3>
          <p>Thương hiệu thời trang nữ phong cách, hiện đại và thanh lịch. Chúng tôi mang đến sự tự tin cho phái đẹp.</p>
          <div class="socials">
            <i class="fab fa-facebook-f"></i>
            <i class="fab fa-instagram"></i>
            <i class="fab fa-tiktok"></i>
          </div>
        </div>
        <div class="footer-col">
          <h4>Hỗ trợ khách hàng</h4>
          <ul>
            <li>Hướng dẫn mua hàng</li>
            <li>Chính sách đổi trả</li>
            <li>Bảo mật thông tin</li>
          </ul>
        </div>
        <div class="footer-col">
          <h4>Liên hệ</h4>
          <ul>
            <li><i class="fas fa-map-marker-alt"></i> Nam Từ Liêm, Hà Nội</li>
            <li><i class="fas fa-phone"></i> 0912.345.678</li>
            <li><i class="fas fa-envelope"></i> contact@yourchoice.vn</li>
          </ul>
        </div>
      </div>
      <div class="footer-bottom">
        © 2026 YourChoice Shop. All rights reserved.
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

const isScrolled = ref(false);

const handleScroll = () => {
  isScrolled.value = window.scrollY > 50;
};

// Xử lý ảnh lỗi logo
const handleImageError = (e) => {
    e.target.style.display = 'none'; 
};

onMounted(() => window.addEventListener('scroll', handleScroll));
onUnmounted(() => window.removeEventListener('scroll', handleScroll));
</script>

<style scoped>
/* SỬ DỤNG FONT ARIAL ĐỂ ĐỒNG BỘ */
.client-layout {
  font-family: Arial, sans-serif;
  color: #333;
  background-color: #ffffff;
}

.container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
  box-sizing: border-box;
}

/* HEADER */
.site-header {
  position: fixed; top: 0; left: 0; right: 0;
  height: 80px; background: white; z-index: 1000;
  transition: all 0.3s ease; border-bottom: 1px solid #f1f5f9;
}
.site-header.scrolled { height: 70px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); }

.header-inner { height: 100%; display: flex; align-items: center; justify-content: space-between; }

/* === LOGO STYLE === */
.brand { display: flex; align-items: center; cursor: pointer; }
.logo-circle { width: 45px; height: 45px; border-radius: 12px; overflow: hidden; display: flex; align-items: center; justify-content: center; margin-right: 12px; background: #f8fafc; border: 1px solid #e2e8f0; }
.logo-circle img { width: 100%; height: 100%; object-fit: cover; }
.brand-name { font-weight: 800; font-size: 22px; color: #1e3a8a; letter-spacing: 1px; }

/* NAV LINKS (BLUE HOVER) */
.main-nav { display: flex; gap: 30px; }
.nav-link { 
  text-decoration: none; color: #334155; font-weight: 600; text-transform: uppercase; font-size: 13px; letter-spacing: 1px; transition: 0.2s; position: relative;
}
.nav-link:hover, .nav-link.active { color: #1e3a8a; /* Xanh Navy */ }
.nav-link.active::after {
  content: ''; position: absolute; bottom: -4px; left: 0; width: 100%; height: 2px; 
  background: linear-gradient(90deg, #1e3a8a, #0f172a); 
}
.nav-link::after {
  content: ''; position: absolute; bottom: -4px; left: 0; width: 0; height: 2px; 
  background: linear-gradient(90deg, #1e3a8a, #0f172a);
  transition: 0.3s;
}
.nav-link:hover::after { width: 100%; }

/* ICONS */
.header-icons { display: flex; align-items: center; gap: 20px; }
.search-wrap { position: relative; }
.search-input { 
  border: none; border-bottom: 1px solid #e5e5e5; padding: 5px 25px 5px 0; outline: none; font-family: inherit; width: 150px; transition: 0.3s; 
}
.search-input:focus { border-color: #1e3a8a; width: 200px; }
.search-icon { position: absolute; right: 0; top: 8px; font-size: 14px; color: #64748b; }

.icon-item { font-size: 20px; cursor: pointer; position: relative; transition: 0.2s; color: #334155; }
.icon-item:hover { color: #1e3a8a; }

.cart-badge {
  position: absolute; top: -6px; right: -8px;
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); /* Badge Gradient */
  color: white; font-size: 10px; width: 18px; height: 18px;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 5px rgba(30, 58, 138, 0.3);
}

/* MAIN */
.site-main { padding-top: 80px; min-height: 80vh; }

/* FOOTER */
.site-footer { background: #0f172a; color: #fff; padding: 60px 0 20px; margin-top: 50px; }
.footer-grid { display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 40px; margin-bottom: 40px; }
.footer-col h3 { font-size: 20px; margin-bottom: 15px; letter-spacing: 2px; }
.footer-col h4 { font-size: 16px; margin-bottom: 20px; text-transform: uppercase; color: #bfdbfe; }
.footer-col p { color: #94a3b8; font-size: 14px; line-height: 1.6; max-width: 300px; }
.footer-col ul { list-style: none; padding: 0; }
.footer-col ul li { margin-bottom: 12px; color: #94a3b8; font-size: 14px; cursor: pointer; transition: 0.2s; }
.footer-col ul li:hover { color: #fff; transform: translateX(5px); }
.socials { display: flex; gap: 15px; margin-top: 20px; }
.socials i { width: 35px; height: 35px; background: #1e293b; display: flex; align-items: center; justify-content: center; border-radius: 50%; transition: 0.3s; cursor: pointer; color: #fff; }
.socials i:hover { background: #3b82f6; }
.footer-bottom { text-align: center; border-top: 1px solid #1e293b; padding-top: 20px; color: #64748b; font-size: 13px; }

/* RESPONSIVE */
@media (max-width: 768px) {
  .main-nav { display: none; /* Cần thêm Mobile Menu sau */ }
  .footer-grid { grid-template-columns: 1fr; gap: 30px; text-align: center; }
  .footer-col p { margin: 0 auto; }
  .socials { justify-content: center; }
}
</style>