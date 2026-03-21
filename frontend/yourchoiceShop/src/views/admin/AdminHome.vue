<template>
  <div class="admin-home">
    <!-- HERO SECTION -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">
            Chào mừng đến trang quản trị
            <span class="brand-highlight">YourChoice</span>
          </h1>
          <p class="hero-desc">
            Quản lý cửa hàng thời trang của bạn một cách hiệu quả. Theo dõi doanh thu,
            xử lý đơn hàng, quản lý sản phẩm và nhân sự — tất cả tại một nơi.
          </p>
          <div class="hero-stats">
            <div class="stat-chip">
              <i class="fa-solid fa-bolt"></i> Nhanh chóng
            </div>
            <div class="stat-chip">
              <i class="fa-solid fa-shield-halved"></i> An toàn
            </div>
            <div class="stat-chip">
              <i class="fa-solid fa-chart-line"></i> Hiệu quả
            </div>
          </div>
        </div>
        <div class="hero-illustration">
          <div class="illustration-circle">
            <i class="fa-solid fa-store"></i>
          </div>
        </div>
      </div>
    </section>

    <!-- SLIDER SECTION -->
    <section class="slider-section">
      <div class="slider-grid">
        <div class="slider-wrapper">
          <div class="slider-track" :style="{ transform: `translateX(-${currentSlideLeft * 100}%)` }">
            <div class="slide" v-for="(slide, idx) in slidesLeft" :key="`left-${idx}`">
              <img :src="slide.img" :alt="slide.title" @error="handleSlideError($event)" />
              <div class="slide-overlay">
                <h3>{{ slide.title }}</h3>
                <p>{{ slide.desc }}</p>
              </div>
            </div>
          </div>

          <button class="slider-btn prev" @click="prevSlideLeft" aria-label="Trước khu trái">
            <i class="fa-solid fa-chevron-left"></i>
          </button>
          <button class="slider-btn next" @click="nextSlideLeft" aria-label="Sau khu trái">
            <i class="fa-solid fa-chevron-right"></i>
          </button>

          <div class="slider-dots">
            <span
              v-for="(_, idx) in slidesLeft"
              :key="`left-dot-${idx}`"
              :class="['dot', { active: idx === currentSlideLeft }]"
              @click="goToSlideLeft(idx)"
            ></span>
          </div>
        </div>

        <div class="slider-wrapper">
          <div class="slider-track" :style="{ transform: `translateX(-${currentSlideRight * 100}%)` }">
            <div class="slide" v-for="(slide, idx) in slidesRight" :key="`right-${idx}`">
              <img :src="slide.img" :alt="slide.title" @error="handleSlideError($event)" />
              <div class="slide-overlay">
                <h3>{{ slide.title }}</h3>
                <p>{{ slide.desc }}</p>
              </div>
            </div>
          </div>

          <button class="slider-btn prev" @click="prevSlideRight" aria-label="Trước khu phải">
            <i class="fa-solid fa-chevron-left"></i>
          </button>
          <button class="slider-btn next" @click="nextSlideRight" aria-label="Sau khu phải">
            <i class="fa-solid fa-chevron-right"></i>
          </button>

          <div class="slider-dots">
            <span
              v-for="(_, idx) in slidesRight"
              :key="`right-dot-${idx}`"
              :class="['dot', { active: idx === currentSlideRight }]"
              @click="goToSlideRight(idx)"
            ></span>
          </div>
        </div>
      </div>
    </section>

    <!-- CARD NAVIGATION -->
    <section class="cards-section">
      <h2 class="section-title">
        <i class="fa-solid fa-grip"></i> Truy cập nhanh
      </h2>
      <div class="cards-grid">
        <div
          class="nav-card"
          v-for="(card, idx) in navCards"
          :key="idx"
          @click="$router.push(card.route)"
          :style="{ '--accent': card.color }"
        >
          <div class="card-icon-wrap" :style="{ background: card.color + '15' }">
            <i :class="card.icon" :style="{ color: card.color }"></i>
          </div>
          <div class="card-body">
            <h3 class="card-title">{{ card.title }}</h3>
            <p class="card-desc">{{ card.desc }}</p>
          </div>
          <div class="card-arrow">
            <i class="fa-solid fa-arrow-right"></i>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

import somi1 from '@/img/somi1.jpg'
import somi3 from '@/img/somi3.jpg'
import somi5 from '@/img/somi5.jpg'
import somi7 from '@/img/somi7.jpg'
import somi10 from '@/img/somi10.jpg'

const slidesLeft = ref([
  { img: somi1, title: 'Bộ sưu tập mới nhất', desc: 'Cập nhật xu hướng thời trang 2026' },
  { img: somi5, title: 'Phong cách hiện đại', desc: 'Đa dạng mẫu mã, phù hợp mọi phong cách' },
  { img: somi10, title: 'Ưu đãi hấp dẫn', desc: 'Khuyến mãi liên tục cho khách hàng thân thiết' },
])

const slidesRight = ref([
  { img: somi3, title: 'Chất lượng vượt trội', desc: 'Sản phẩm được tuyển chọn kỹ lưỡng' },
  { img: somi7, title: 'Dịch vụ chuyên nghiệp', desc: 'Giao hàng nhanh, đổi trả dễ dàng' },
  { img: somi1, title: 'Thiết kế tinh tế', desc: 'Cân bằng giữa thời trang và tính ứng dụng' },
])

const currentSlideLeft = ref(0)
const currentSlideRight = ref(0)
let sliderIntervalLeft = null
let sliderIntervalRight = null

const nextSlideLeft = () => {
  currentSlideLeft.value = (currentSlideLeft.value + 1) % slidesLeft.value.length
}
const prevSlideLeft = () => {
  currentSlideLeft.value = (currentSlideLeft.value - 1 + slidesLeft.value.length) % slidesLeft.value.length
}
const goToSlideLeft = (idx) => {
  currentSlideLeft.value = idx
  resetIntervalLeft()
}

const nextSlideRight = () => {
  currentSlideRight.value = (currentSlideRight.value + 1) % slidesRight.value.length
}
const prevSlideRight = () => {
  currentSlideRight.value = (currentSlideRight.value - 1 + slidesRight.value.length) % slidesRight.value.length
}
const goToSlideRight = (idx) => {
  currentSlideRight.value = idx
  resetIntervalRight()
}

const resetIntervalLeft = () => {
  clearInterval(sliderIntervalLeft)
  sliderIntervalLeft = setInterval(nextSlideLeft, 3800)
}
const resetIntervalRight = () => {
  clearInterval(sliderIntervalRight)
  sliderIntervalRight = setInterval(nextSlideRight, 4300)
}
const handleSlideError = (e) => {
  e.target.src = 'https://placehold.co/1200x400/1e293b/white?text=YourChoice+Fashion'
}

onMounted(() => {
  resetIntervalLeft()
  resetIntervalRight()
})
onUnmounted(() => {
  clearInterval(sliderIntervalLeft)
  clearInterval(sliderIntervalRight)
})

const navCards = [
  {
    title: 'Thống kê',
    desc: 'Xem báo cáo doanh thu, đơn hàng và hiệu suất kinh doanh',
    icon: 'fa-solid fa-chart-pie',
    route: '/admin/dashboard',
    color: '#0ea5e9',
  },
  {
    title: 'Bán hàng tại quầy',
    desc: 'Tạo đơn hàng nhanh tại cửa hàng, quét QR sản phẩm',
    icon: 'fa-solid fa-cash-register',
    route: '/admin/pos',
    color: '#8b5cf6',
  },
  {
    title: 'Quản lý hóa đơn',
    desc: 'Theo dõi, xác nhận và xử lý đơn hàng trực tuyến',
    icon: 'fa-solid fa-file-invoice',
    route: '/admin/orders',
    color: '#f59e0b',
  },
  {
    title: 'Quản lý sản phẩm',
    desc: 'Thêm, sửa, xóa sản phẩm và quản lý biến thể, thuộc tính',
    icon: 'fa-solid fa-shirt',
    route: '/admin/products',
    color: '#10b981',
  },
  {
    title: 'Giảm giá',
    desc: 'Tạo và quản lý phiếu giảm giá, đợt khuyến mãi',
    icon: 'fa-solid fa-tags',
    route: '/admin/vouchers',
    color: '#ef4444',
  },
  {
    title: 'Quản lý lịch làm việc',
    desc: 'Xếp ca, theo dõi lịch làm việc và hoạt động nhân viên',
    icon: 'fa-solid fa-calendar-check',
    route: '/admin/shifts',
    color: '#06b6d4',
  },
  {
    title: 'Quản lý tài khoản',
    desc: 'Quản lý thông tin khách hàng và nhân viên',
    icon: 'fa-solid fa-users-gear',
    route: '/admin/customers',
    color: '#6366f1',
  },
  {
    title: 'Quản lý Chat',
    desc: 'Hỗ trợ khách hàng trực tuyến qua hệ thống chat',
    icon: 'fa-solid fa-comments',
    route: '/admin/chat',
    color: '#ec4899',
  },
]
</script>

<style scoped>
.admin-home {
  font-family: 'Segoe UI', sans-serif;
  color: #1e293b;
}

/* ============ HERO ============ */
.hero-section {
  background: linear-gradient(135deg, #0f172a 0%, #1e3a5f 50%, #0c4a6e 100%);
  border-radius: 16px;
  padding: 48px 44px;
  margin-bottom: 28px;
  position: relative;
  overflow: hidden;
}
.hero-section::before {
  content: '';
  position: absolute;
  top: -60px;
  right: -60px;
  width: 260px;
  height: 260px;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.15) 0%, transparent 70%);
  border-radius: 50%;
}
.hero-section::after {
  content: '';
  position: absolute;
  bottom: -40px;
  left: 30%;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.1) 0%, transparent 70%);
  border-radius: 50%;
}
.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}
.hero-text { max-width: 600px; }
.hero-title {
  font-size: 32px;
  font-weight: 800;
  color: #fff;
  line-height: 1.3;
  margin: 0 0 14px;
}
.brand-highlight {
  background: linear-gradient(135deg, #38bdf8, #818cf8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.hero-desc {
  font-size: 15px;
  color: #94a3b8;
  line-height: 1.7;
  margin: 0 0 22px;
}
.hero-stats { display: flex; gap: 10px; }
.stat-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  color: #e2e8f0;
  padding: 8px 16px;
  border-radius: 24px;
  font-size: 13px;
  font-weight: 500;
  backdrop-filter: blur(4px);
}
.stat-chip i { font-size: 12px; color: #38bdf8; }

.hero-illustration {
  flex-shrink: 0;
}
.illustration-circle {
  width: 140px;
  height: 140px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.15), rgba(129, 140, 248, 0.15));
  border: 2px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 56px;
  color: rgba(255, 255, 255, 0.6);
}

/* ============ SLIDER ============ */
.slider-section { margin-bottom: 32px; }
.slider-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.slider-wrapper {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  background: #0f172a;
}
.slider-track {
  display: flex;
  transition: transform 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}
.slide {
  min-width: 100%;
  position: relative;
}
.slide img {
  width: 100%;
  height: 540px;
  object-fit: contain;
  display: block;
  background: #0f172a;
}
.slide-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 32px 36px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
}
.slide-overlay h3 {
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 6px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}
.slide-overlay p {
  color: #e2e8f0;
  font-size: 14px;
  margin: 0;
}

.slider-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #1e293b;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: all 0.2s;
  z-index: 2;
}
.slider-btn:hover {
  background: #fff;
  transform: translateY(-50%) scale(1.08);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}
.slider-btn.prev { left: 16px; }
.slider-btn.next { right: 16px; }

.slider-dots {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 2;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  transition: all 0.3s;
}
.dot.active {
  background: #fff;
  width: 28px;
  border-radius: 5px;
  box-shadow: 0 2px 6px rgba(255, 255, 255, 0.4);
}

/* ============ CARDS ============ */
.cards-section { margin-bottom: 20px; }
.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.section-title i {
  color: #0ea5e9;
  font-size: 18px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}

.nav-card {
  background: #fff;
  border-radius: 14px;
  padding: 24px 20px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  border: 1px solid #e8ecf1;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.nav-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--accent);
  opacity: 0;
  transition: opacity 0.3s;
}
.nav-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  border-color: transparent;
}
.nav-card:hover::before { opacity: 1; }

.card-icon-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.card-body { flex: 1; }
.card-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 6px;
}
.card-desc {
  font-size: 13px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

.card-arrow {
  align-self: flex-end;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #94a3b8;
  transition: all 0.3s;
}
.nav-card:hover .card-arrow {
  background: var(--accent);
  color: #fff;
  transform: translateX(3px);
}

/* ============ RESPONSIVE ============ */
@media (max-width: 1200px) {
  .cards-grid { grid-template-columns: repeat(3, 1fr); }
  .slider-grid { grid-template-columns: 1fr; }
}
@media (max-width: 900px) {
  .cards-grid { grid-template-columns: repeat(2, 1fr); }
  .hero-content { flex-direction: column; text-align: center; }
  .hero-illustration { margin-top: 24px; }
  .hero-stats { justify-content: center; }
  .hero-title { font-size: 26px; }
  .slide img { height: 440px; }
}
@media (max-width: 600px) {
  .cards-grid { grid-template-columns: 1fr; }
  .hero-section { padding: 32px 24px; }
  .hero-title { font-size: 22px; }
  .slide img { height: 320px; }
  .slider-btn { width: 36px; height: 36px; font-size: 14px; }
}
</style>
