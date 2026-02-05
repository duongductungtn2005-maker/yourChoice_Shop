<template>
  <div class="home-page">
    
    <section class="hero-section">
      <div class="hero-content">
        <span class="sub-title">BỘ SƯU TẬP MỚI 2026</span>
        <h1 class="hero-title">Thanh lịch.<br>Hiện đại.</h1>
        <p class="hero-desc">Khám phá những mẫu áo sơ mi thiết kế tinh tế, chất liệu lụa cao cấp dành riêng cho quý cô công sở.</p>
        <router-link to="/shop" class="btn-cta">Mua ngay <i class="fas fa-arrow-right"></i></router-link>
      </div>
      <div class="hero-image">
        <img src="https://images.unsplash.com/photo-1598554747436-c9293d6a588f?q=80&w=1000&auto=format&fit=crop" alt="Hero Banner">
      </div>
    </section>

    <section class="features-section">
      <div class="container">
        <div class="feature-item">
          <i class="fas fa-shipping-fast"></i>
          <div>
            <h4>Miễn phí vận chuyển</h4>
            <p>Cho đơn hàng trên 500k</p>
          </div>
        </div>
        <div class="feature-item">
          <i class="fas fa-undo"></i>
          <div>
            <h4>Đổi trả trong 30 ngày</h4>
            <p>Nếu có lỗi từ nhà sản xuất</p>
          </div>
        </div>
        <div class="feature-item">
          <i class="fas fa-headset"></i>
          <div>
            <h4>Hỗ trợ 24/7</h4>
            <p>Hotline: 0988.888.888</p>
          </div>
        </div>
      </div>
    </section>

    <section class="product-section">
      <div class="container">
        <div class="section-header">
          <h2>Sản phẩm mới nhất</h2>
          <router-link to="/shop" class="view-all">Xem tất cả</router-link>
        </div>

        <div class="product-grid">
          <div v-for="product in products" :key="product.id" class="product-card">
            <div class="card-image">
              <span v-if="product.sale" class="badge-sale">-{{ product.sale }}%</span>
              <img :src="product.anh || 'https://placehold.co/300x400?text=Ao+So+Mi'" alt="Product">
              
              <div class="card-actions">
                <button title="Thêm vào giỏ" @click="addToCart(product)"><i class="fas fa-cart-plus"></i></button>
                <button title="Xem nhanh"><i class="fas fa-eye"></i></button>
              </div>
            </div>
            
            <div class="card-info">
              <div class="category">Áo Sơ Mi</div>
              <h3 class="product-name">{{ product.tenSanPham }}</h3>
              <div class="price-row">
                <span class="price-new">{{ formatMoney(product.giaBan * (100 - (product.sale || 0)) / 100) }}</span>
                <span v-if="product.sale" class="price-old">{{ formatMoney(product.giaBan) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="newsletter-section">
      <div class="container">
        <div class="newsletter-box">
          <h2>Đăng ký nhận tin</h2>
          <p>Nhận ngay mã giảm giá 10% cho đơn hàng đầu tiên</p>
          <div class="input-group">
            <input type="email" placeholder="Nhập email của bạn...">
            <button class="btn-black">Đăng ký</button>
          </div>
        </div>
      </div>
    </section>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '@/services/request';

const products = ref([]);

// Giả lập dữ liệu (Sau này thay bằng API thật)
onMounted(async () => {
  try {
    // Gọi API thật: const res = await request.get('/san-pham/new-arrival');
    // products.value = res.data;
    
    // Mock data để dựng giao diện trước
    products.value = [
      { id: 1, tenSanPham: 'Áo Sơ Mi Lụa Cổ Nơ', giaBan: 450000, sale: 0, anh: 'https://img.freepik.com/free-photo/portrait-young-woman-white-shirt_1303-18928.jpg' },
      { id: 2, tenSanPham: 'Sơ Mi Voan Tay Bồng', giaBan: 380000, sale: 10, anh: 'https://img.freepik.com/free-photo/elegant-woman-standing-white-shirt_144627-28383.jpg' },
      { id: 3, tenSanPham: 'Áo Kiểu Basic Công Sở', giaBan: 320000, sale: 0, anh: 'https://img.freepik.com/free-photo/fashionable-woman-white-shirt_144627-28384.jpg' },
      { id: 4, tenSanPham: 'Sơ Mi Kẻ Sọc Xanh', giaBan: 420000, sale: 15, anh: 'https://img.freepik.com/free-photo/young-beautiful-woman-casual-outfit_144627-28381.jpg' }
    ];
  } catch (e) {
    console.error(e);
  }
});

const formatMoney = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
const addToCart = (p) => alert(`Đã thêm ${p.tenSanPham} vào giỏ!`);
</script>

<style scoped>
/* --- HERO SECTION --- */
.hero-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 85vh; /* Chiếm 85% màn hình */
  background-color: #f8f9fa; /* Màu nền xám rất nhạt */
  overflow: hidden;
  padding: 0 5%;
}
.hero-content { flex: 1; max-width: 500px; z-index: 2; }
.sub-title { font-size: 14px; letter-spacing: 2px; color: #64748b; font-weight: 700; display: block; margin-bottom: 15px; }
.hero-title { font-size: 60px; line-height: 1.1; font-weight: 800; color: #000; margin-bottom: 20px; }
.hero-desc { font-size: 16px; color: #4b5563; line-height: 1.6; margin-bottom: 35px; }
.hero-image { flex: 1; height: 100%; display: flex; align-items: center; justify-content: flex-end; }
.hero-image img { height: 90%; object-fit: cover; border-radius: 20px; box-shadow: 20px 20px 0px #00000010; }

/* Buttons */
.btn-cta {
  display: inline-flex; align-items: center; gap: 10px;
  background: #000; color: #fff;
  padding: 15px 35px; border-radius: 50px;
  text-decoration: none; font-weight: 600;
  transition: all 0.3s;
}
.btn-cta:hover { background: #333; transform: translateY(-3px); box-shadow: 0 10px 20px rgba(0,0,0,0.2); }

/* --- FEATURES --- */
.features-section { padding: 60px 0; border-bottom: 1px solid #f1f5f9; }
.features-section .container { display: flex; justify-content: space-between; }
.feature-item { display: flex; align-items: center; gap: 15px; }
.feature-item i { font-size: 24px; color: #000; }
.feature-item h4 { font-size: 15px; font-weight: 700; margin-bottom: 4px; }
.feature-item p { font-size: 13px; color: #64748b; margin: 0; }

/* --- PRODUCT SECTION --- */
.product-section { padding: 80px 0; }
.section-header { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 40px; }
.section-header h2 { font-size: 32px; font-weight: 700; color: #000; margin: 0; }
.view-all { color: #000; text-decoration: none; border-bottom: 1px solid #000; padding-bottom: 2px; font-weight: 600; }

/* PRODUCT CARD (Key Focus) */
.product-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 30px; }
.product-card {
  background: #fff;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}
.product-card:hover { transform: translateY(-10px); }

/* Image Area */
.card-image {
  position: relative; overflow: hidden; border-radius: 12px;
  background: #f1f5f9; padding-top: 133%; /* Aspect Ratio 3:4 */
}
.card-image img {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  object-fit: cover; transition: transform 0.5s ease;
}
.product-card:hover .card-image img { transform: scale(1.05); }

/* Badge */
.badge-sale {
  position: absolute; top: 10px; left: 10px;
  background: #000; color: #fff; padding: 4px 8px;
  font-size: 12px; font-weight: 700; border-radius: 4px; z-index: 2;
}

/* Hover Actions */
.card-actions {
  position: absolute; bottom: 15px; left: 50%; transform: translateX(-50%) translateY(20px);
  display: flex; gap: 10px; opacity: 0; transition: all 0.3s ease; z-index: 2;
}
.product-card:hover .card-actions { opacity: 1; transform: translateX(-50%) translateY(0); }
.card-actions button {
  width: 40px; height: 40px; border-radius: 50%; border: none;
  background: #fff; color: #000; font-size: 16px;
  cursor: pointer; box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  transition: 0.2s;
}
.card-actions button:hover { background: #000; color: #fff; }

/* Info Area */
.card-info { padding: 15px 5px; }
.category { font-size: 12px; color: #9ca3af; margin-bottom: 5px; text-transform: uppercase; }
.product-name { font-size: 16px; font-weight: 600; color: #1f2937; margin-bottom: 8px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.price-row { display: flex; align-items: center; gap: 10px; }
.price-new { font-weight: 700; color: #000; font-size: 16px; }
.price-old { text-decoration: line-through; color: #9ca3af; font-size: 14px; }

/* --- NEWSLETTER --- */
.newsletter-section { padding: 80px 0; background: #000; color: #fff; text-align: center; }
.newsletter-box h2 { font-size: 30px; margin-bottom: 10px; }
.newsletter-box p { color: #9ca3af; margin-bottom: 30px; }
.input-group {
  display: inline-flex; background: #fff; padding: 5px; border-radius: 50px;
  width: 500px; max-width: 100%;
}
.input-group input {
  flex: 1; border: none; outline: none; padding: 0 20px; border-radius: 50px;
}
.btn-black {
  background: #000; color: #fff; border: none;
  padding: 12px 30px; border-radius: 50px; font-weight: 600; cursor: pointer;
}

/* RESPONSIVE */
@media (max-width: 768px) {
  .hero-section { flex-direction: column; padding-top: 100px; height: auto; text-align: center; }
  .hero-content { margin-bottom: 40px; }
  .product-grid { grid-template-columns: repeat(2, 1fr); }
  .features-section .container { flex-direction: column; gap: 30px; }
}
</style>