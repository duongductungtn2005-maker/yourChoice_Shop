<template>
  <div class="page-container">
    
    <div class="page-header">
       <span class="sub-title">BLOG & SỰ KIỆN</span>
       <h1>TIN TỨC MỚI NHẤT</h1>
       <div class="divider"></div>
    </div>

    <div class="blog-grid">
       <div v-for="(item, index) in newsList" :key="index" class="blog-card">
          <div class="blog-img">
             <img :src="item.image" :alt="item.title">
             <div class="overlay"></div>
          </div>
          <div class="blog-content">
             <div class="b-meta">
                <span class="b-date"><i class="far fa-calendar-alt"></i> {{ item.date }}</span>
                <span class="b-cat">{{ item.category }}</span>
             </div>
             <h3 class="b-title">{{ item.title }}</h3>
             <p class="b-desc">{{ item.desc }}</p>
             <a href="#" class="read-more">Xem chi tiết <i class="fas fa-arrow-right"></i></a>
          </div>
       </div>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue';

// Dữ liệu giả lập (Sau này bạn có thể gọi API để thay thế)
const newsList = ref([
    {
        title: "Xu hướng thời trang công sở Thu Đông 2026",
        date: "12/02/2026",
        category: "Xu hướng",
        desc: "Khám phá những mẫu sơ mi, vest và chân váy đang làm mưa làm gió tại các sàn diễn thời trang quốc tế...",
        image: "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=600&q=80"
    },
    {
        title: "Bí quyết phối đồ thanh lịch cho nàng công sở",
        date: "10/02/2026",
        category: "Tips phối đồ",
        desc: "Làm thế nào để vừa chuyên nghiệp vừa sành điệu? Hãy cùng YourChoice tìm hiểu 5 quy tắc vàng...",
        image: "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&q=80"
    },
    {
        title: "Sale Sập Sàn - Giảm giá 50% toàn bộ cửa hàng",
        date: "05/02/2026",
        category: "Khuyến mãi",
        desc: "Cơ hội săn hàng hiệu giá hời chưa từng có. Chương trình áp dụng từ ngày 15/02 đến 20/02...",
        image: "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=600&q=80"
    },
    {
        title: "Ra mắt bộ sưu tập 'Lady in Blue'",
        date: "01/02/2026",
        category: "Sự kiện",
        desc: "Sự kiện ra mắt bộ sưu tập mới lấy cảm hứng từ vẻ đẹp của đại dương và bầu trời...",
        image: "https://images.unsplash.com/photo-1529139574466-a302c27e3119?w=600&q=80"
    }
]);
</script>

<style scoped>
/* GENERAL */
.page-container { 
    max-width: 1280px; margin: 0 auto; padding: 60px 20px; 
    font-family: Arial, sans-serif; color: #333;
}

/* HEADER */
.page-header { text-align: center; margin-bottom: 50px; }
.sub-title { font-size: 14px; letter-spacing: 2px; color: #64748b; text-transform: uppercase; display: block; margin-bottom: 10px; font-weight: 600; }
.page-header h1 { font-size: 32px; color: #0f172a; margin: 0 0 15px 0; font-weight: 800; }
.divider { width: 60px; height: 4px; background: #1e3a8a; margin: 0 auto; border-radius: 2px; }

/* GRID */
.blog-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 30px; }

/* CARD */
.blog-card { 
    background: #fff; border: 1px solid #f1f5f9; border-radius: 12px; 
    overflow: hidden; transition: all 0.3s ease; display: flex; flex-direction: column;
}
.blog-card:hover { 
    transform: translateY(-5px); 
    box-shadow: 0 15px 30px rgba(0,0,0,0.08); 
    border-color: #e2e8f0;
}

/* IMAGE */
.blog-img { position: relative; overflow: hidden; height: 200px; }
.blog-img img { width: 100%; height: 100%; object-fit: cover; transition: 0.5s ease; }
.blog-card:hover .blog-img img { transform: scale(1.1); }

/* CONTENT */
.blog-content { padding: 24px; flex: 1; display: flex; flex-direction: column; }

.b-meta { display: flex; justify-content: space-between; font-size: 12px; color: #94a3b8; margin-bottom: 12px; }
.b-cat { color: #1e3a8a; font-weight: 600; background: #eff6ff; padding: 2px 8px; border-radius: 4px; }

.b-title { 
    font-size: 18px; margin: 0 0 12px; color: #0f172a; line-height: 1.4; font-weight: 700;
    display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.b-title:hover { color: #1e3a8a; cursor: pointer; }

.b-desc { 
    font-size: 14px; color: #64748b; line-height: 1.6; margin-bottom: 20px; flex: 1;
    display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;
}

.read-more { 
    display: inline-flex; align-items: center; gap: 6px; 
    color: #1e3a8a; font-weight: 700; text-decoration: none; font-size: 14px; transition: 0.2s;
}
.read-more:hover { gap: 10px; color: #0f172a; }

/* RESPONSIVE */
@media (max-width: 992px) {
    .blog-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 600px) {
    .blog-grid { grid-template-columns: 1fr; }
}
</style>