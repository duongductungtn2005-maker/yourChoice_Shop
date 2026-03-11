<template>
  <div class="home-page">
    
    <div class="hero-slider">
      <div 
        v-for="(slide, index) in slides" 
        :key="index"
        class="slide-item"
        :class="{ active: currentSlide === index }"
      >
        <div class="slide-bg" :style="{ backgroundImage: `url(${slide.image})` }"></div>
        <div class="slide-overlay"></div>
      </div>

      <div class="hero-content">
        <span class="sub-title">BỘ SƯU TẬP MỚI 2026</span>
        <h1>{{ slides[currentSlide].title }}</h1>
        <p>{{ slides[currentSlide].desc }}</p>
        <button class="btn-shop-now" @click="$router.push('/products')">MUA NGAY</button>
      </div>

      <div class="slider-dots">
        <span 
          v-for="(slide, index) in slides" 
          :key="'dot-' + index" 
          class="dot" 
          :class="{ active: currentSlide === index }"
          @click="currentSlide = index"
        ></span>
      </div>
    </div>

    <div class="container">
      
      <div class="section-title">
        <h2>SẢN PHẨM MỚI NHẤT</h2>
        <p>Những thiết kế đón đầu xu hướng 2026</p>
      </div>

      <div class="product-grid-home">
         <div 
            v-for="prod in featuredProducts" 
            :key="prod.id" 
            class="product-card" 
            @click="$router.push(`/product/${prod.id}`)"
         >
            <div class="product-image">
               <img :src="getProductImage(prod)" alt="Product Image">
               
               <div class="overlay-actions">
                  <button class="btn-quick-view">Xem chi tiết</button>
               </div>
               <div class="sale-tag" v-if="prod.phanTramGiam">-{{ prod.phanTramGiam }}%</div>
               <div v-if="prod.soLuong <= 0" class="sold-out-overlay">
                  <span class="sold-out-text">Đã hết hàng</span>
               </div>
            </div>
            
            <div class="product-info">
               <div class="brand-name">{{ prod.tenThuongHieu || 'No Brand' }}</div>
               <h3 class="product-name">{{ prod.tenSanPham }}</h3>
               <div class="product-price">
                  <span class="current-price">{{ formatMoney(prod.giaBanMin || 0) }}</span>
                  <span v-if="prod.giaBanMax && prod.giaBanMax > prod.giaBanMin" class="price-range"> ~ {{ formatMoney(prod.giaBanMax) }}</span>
               </div>
            </div>
         </div>
      </div>
      
      <div class="text-center" style="margin-top: 50px;">
         <button class="btn-view-all" @click="$router.push('/products')">XEM TẤT CẢ SẢN PHẨM</button>
      </div>

      <div class="features-row">
         <div class="feature-box">
            <i class="fas fa-shipping-fast"></i>
            <h4>Miễn phí vận chuyển</h4>
            <p>Cho đơn hàng từ 500k</p>
         </div>
         <div class="feature-box">
            <i class="fas fa-undo"></i>
            <h4>Đổi trả dễ dàng</h4>
            <p>Trong vòng 30 ngày</p>
         </div>
         <div class="feature-box">
            <i class="fas fa-headset"></i>
            <h4>Hỗ trợ 24/7</h4>
            <p>Hotline: 0912.345.678</p>
         </div>
         <div class="feature-box">
            <i class="fas fa-shield-alt"></i>
            <h4>Thanh toán an toàn</h4>
            <p>Bảo mật thông tin 100%</p>
         </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import axios from 'axios';

// --- CONFIG ---
const API_URL = 'http://localhost:8080/api/v1';
// backend cung cấp endpoint trả về ảnh sản phẩm (tên file hoặc path)
const IMAGE_BASE_URL = 'http://localhost:8080/images/';
const featuredProducts = ref([]);
const currentSlide = ref(0);
let slideInterval;

// --- DATA: SLIDES ---
const slides = [
  { 
    image: 'https://images.unsplash.com/photo-1483985988355-763728e1935b?q=80&w=2070&auto=format&fit=crop',
    title: 'THANH LỊCH & HIỆN ĐẠI',
    desc: 'Khám phá những mẫu sơ mi thiết kế mới nhất dành riêng cho bạn.'
  },
  { 
    image: 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?q=80&w=2020&auto=format&fit=crop',
    title: 'PHONG CÁCH CÔNG SỞ',
    desc: 'Tự tin tỏa sáng mỗi ngày với bộ sưu tập Thu Đông 2026.'
  },
  { 
    image: 'https://images.unsplash.com/photo-1529139574466-a302c27e3119?q=80&w=2070&auto=format&fit=crop',
    title: 'ƯU ĐÃI ĐẶC BIỆT',
    desc: 'Giảm giá lên đến 50% cho các sản phẩm best-seller.'
  }
];

// --- METHODS ---
const fetchFeatured = async () => {
  try {
    // Lấy 8 sản phẩm mới nhất
    const res = await axios.get(`${API_URL}/products`, {
        params: { page: 0, size: 8, status: 1 }
    });
    featuredProducts.value = res.data.content || [];
  } catch(e) { console.error("Lỗi tải sản phẩm nổi bật:", e); }
}

const startSlideTimer = () => {
  slideInterval = setInterval(() => {
    currentSlide.value = (currentSlide.value + 1) % slides.length;
  }, 5000); 
};

const formatMoney = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

const getProductImage = (prod) => {
    // Dùng ảnh chính từ ProductResponse
    if (prod.anhChinh) {
        if (prod.anhChinh.startsWith('http')) return prod.anhChinh;
        return `${IMAGE_BASE_URL}${prod.anhChinh}`;
    }
    // fallback nếu không có ảnh
    return `https://placehold.co/300x400?text=No+Image`;
};

// --- LIFECYCLE ---
let stockInterval;
const onVisibilityChange = () => {
    if (document.visibilityState === 'visible') fetchFeatured();
};

onMounted(() => { 
    fetchFeatured(); 
    startSlideTimer();
    document.addEventListener('visibilitychange', onVisibilityChange);
    stockInterval = setInterval(fetchFeatured, 30000);
});

onUnmounted(() => {
    if (slideInterval) clearInterval(slideInterval);
    if (stockInterval) clearInterval(stockInterval);
    document.removeEventListener('visibilitychange', onVisibilityChange);
});
</script>

<style scoped>
/* GENERAL FONT */
.home-page { font-family: Arial, sans-serif; font-size: 16px; color: #333; }
.container { max-width: 1280px; margin: 0 auto; padding: 0 20px; box-sizing: border-box; }
.text-center { text-align: center; }

/* === HERO SLIDER === */
.hero-slider {
  position: relative; height: 550px; width: 100%; overflow: hidden;
  display: flex; align-items: center; justify-content: center; text-align: center;
  background: #000;
}
.slide-item {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  opacity: 0; transition: opacity 1s ease-in-out; z-index: 1;
}
.slide-item.active { opacity: 1; }
.slide-bg {
  width: 100%; height: 100%; background-size: cover; background-position: center;
  filter: blur(3px); transform: scale(1.05);
}
.slide-overlay { position: absolute; inset: 0; background: rgba(0, 0, 0, 0.4); }

.hero-content { position: relative; z-index: 10; color: #fff; max-width: 800px; padding: 0 20px; }
.sub-title { font-size: 16px; letter-spacing: 4px; text-transform: uppercase; display: block; margin-bottom: 15px; color: #bfdbfe; font-weight: bold; }
.hero-content h1 { font-size: 52px; font-weight: 700; margin-bottom: 20px; letter-spacing: 2px; text-shadow: 0 2px 10px rgba(0,0,0,0.3); }
.hero-content p { font-size: 19px; margin-bottom: 40px; font-weight: 400; opacity: 0.95; line-height: 1.6; }

.btn-shop-now {
  background: white; color: #0f172a; border: none; padding: 14px 40px;
  font-size: 16px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px;
  cursor: pointer; transition: 0.3s; border-radius: 4px;
}
.btn-shop-now:hover { 
  background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
  color: #fff; box-shadow: 0 5px 20px rgba(15, 23, 42, 0.4);
}

.slider-dots { position: absolute; bottom: 30px; left: 50%; transform: translateX(-50%); z-index: 10; display: flex; gap: 10px; }
.dot { width: 12px; height: 12px; background: rgba(255, 255, 255, 0.5); border-radius: 50%; cursor: pointer; transition: 0.3s; }
.dot.active { background: #fff; transform: scale(1.2); }

/* === SECTION TITLE === */
.section-title { text-align: center; margin: 60px 0 40px; }
.section-title h2 { font-size: 28px; font-weight: 700; color: #0f172a; margin-bottom: 10px; letter-spacing: 1px; }
.section-title p { color: #64748b; font-size: 16px; }

/* === PRODUCT GRID === */
.product-grid-home { 
    display: grid; grid-template-columns: repeat(4, 1fr); gap: 30px; 
}

/* Product Card Style */
.product-card { cursor: pointer; transition: 0.3s; }
.product-card:hover .product-image img { transform: scale(1.05); }
.product-card:hover .btn-quick-view { opacity: 1; transform: translateY(0); }

.product-image { position: relative; overflow: hidden; aspect-ratio: 3/4; background: #f8fafc; margin-bottom: 15px; border-radius: 8px; }
.product-image img { width: 100%; height: 100%; object-fit: cover; transition: 0.5s ease; }

.overlay-actions { position: absolute; bottom: 20px; left: 0; right: 0; display: flex; justify-content: center; }
.btn-quick-view {
  background: white; color: #0f172a; border: none; padding: 10px 20px; border-radius: 4px;
  font-weight: 600; font-size: 14px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); 
  opacity: 0; transform: translateY(20px); transition: 0.3s; cursor: pointer;
}
.btn-quick-view:hover { background: #0f172a; color: white; }

.sale-tag { position: absolute; top: 10px; left: 10px; background: #ef4444; color: white; padding: 4px 8px; font-size: 12px; font-weight: 700; border-radius: 4px; }
.sold-out-overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; z-index: 5; }
.sold-out-text { color: #fff; background: #dc2626; font-size: 14px; font-weight: 700; padding: 6px 18px; border-radius: 4px; letter-spacing: 0.5px; text-transform: uppercase; }

.product-info { text-align: center; }
.brand-name { font-size: 12px; text-transform: uppercase; color: #94a3b8; margin-bottom: 5px; font-weight: 600; }
.product-name { 
  font-size: 16px; font-weight: 600; margin: 0 0 8px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: #334155; 
}
.product-name:hover { color: #1e3a8a; }
.product-price { font-weight: 700; color: #0f172a; font-size: 16px; display: flex; justify-content: center; align-items: baseline; gap: 2px; flex-wrap: wrap; }
.price-range { font-size: 14px; color: #64748b; font-weight: 500; }

/* === BUTTON VIEW ALL === */
.btn-view-all { 
  padding: 14px 50px; background: white; border: 2px solid #0f172a; color: #0f172a; 
  font-weight: 700; font-size: 14px; letter-spacing: 1px; cursor: pointer; transition: 0.3s; 
  border-radius: 4px;
}
.btn-view-all:hover { background: #0f172a; color: white; }

/* === FEATURES === */
.features-row { 
    display: flex; justify-content: space-between; gap: 20px;
    margin: 80px 0; padding: 50px; 
    background: #f8fafc; border-radius: 12px; 
}
.feature-box { text-align: center; flex: 1; }
.feature-box i { font-size: 36px; color: #1e3a8a; margin-bottom: 20px; }
.feature-box h4 { font-weight: 700; font-size: 18px; margin-bottom: 8px; color: #0f172a; }
.feature-box p { font-size: 14px; color: #64748b; }

/* RESPONSIVE */
@media (max-width: 992px) {
  .product-grid-home { grid-template-columns: repeat(3, 1fr); }
  .features-row { flex-wrap: wrap; }
  .feature-box { flex: 1 1 40%; margin-bottom: 20px; }
}
@media (max-width: 768px) {
  .product-grid-home { grid-template-columns: repeat(2, 1fr); gap: 15px; }
  .hero-slider { height: 400px; }
  .hero-content h1 { font-size: 32px; }
  .feature-box { flex: 1 1 100%; }
}
</style>