<template>
  <div class="product-detail-page">
    <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
    </div>

    <div v-else class="container">
      
      <div class="breadcrumb">
        <span @click="$router.push('/')">Trang chủ</span> / 
        <span @click="$router.push('/products')">Sản phẩm</span> / 
        <span class="current">{{ product.tenSanPham }}</span>
      </div>

      <div class="detail-grid">
        
        <div class="product-gallery">
           <div class="main-image">
              <img :src="activeImage" alt="Main Product" @error="handleImageError">
           </div>
           <div class="thumb-list">
              <div 
                v-for="(img, idx) in currentImages" 
                :key="idx" 
                class="thumb-item"
                :class="{ active: activeImage === getImageUrl(img) }"
                @click="activeImage = getImageUrl(img)"
              >
                 <img :src="getImageUrl(img)" alt="Thumbnail" @error="handleImageError">
              </div>
           </div>
        </div>

        <div class="product-info">
           <h1 class="p-title">{{ product.tenSanPham }}</h1>
           <div class="p-meta">
              <span class="p-brand">Thương hiệu: <strong>{{ product.thuongHieu }}</strong></span>
              <span class="divider">|</span>
              <span class="p-sku">Mã SP: <strong>{{ product.maSanPham }}</strong></span>
           </div>

           <div class="p-price">
              <span class="current-price">{{ formatMoney(displayPrice) }}</span>
              <span v-if="!currentVariant && !originalPrice && product.giaBanMax && product.giaBanMax > product.giaBanMin" class="price-range"> ~ {{ formatMoney(product.giaBanMax) }}</span>
              <span v-if="originalPrice" class="original-price">{{ formatMoney(originalPrice) }}</span>
              <span v-if="discountPercent" class="discount-badge">-{{ discountPercent }}%</span>
           </div>

           <div class="p-desc-short">
              {{ product.moTa }}
           </div>

           <div class="variant-selector">
              <div class="selector-row">
                 <span class="label">Màu sắc:</span>
                 <div class="options">
                    <div 
                       v-for="colorName in uniqueColors" 
                       :key="colorName" 
                       class="color-opt"
                       :class="{ active: selectedColor === colorName }"
                       :style="{ backgroundColor: getColorCode(colorName) }"
                       @click="selectColor(colorName)"
                       :title="colorName"
                    ></div>
                 </div>
                 <span class="selected-val">{{ selectedColor || 'Vui lòng chọn' }}</span>
              </div>

              <div class="selector-row">
                 <span class="label">Kích thước:</span>
                 <div class="options">
                    <div 
                       v-for="sizeName in uniqueSizes" 
                       :key="sizeName" 
                       class="size-opt"
                       :class="{ active: selectedSize === sizeName }"
                       @click="selectSize(sizeName)"
                    >
                       {{ sizeName }}
                    </div>
                 </div>
              </div>

              <div class="selector-row">
                 <span class="label">Số lượng:</span>
                 <div class="qty-control">
                    <button @click="decreaseQty">-</button>
                    <input type="text" v-model="quantity" readonly>
                    <button @click="increaseQty">+</button>
                 </div>
                 <span class="stock-info" :class="{ 'out-of-stock': currentVariant && currentVariant.soLuong <= 0 }">
                    <template v-if="!currentVariant">Vui lòng chọn phân loại</template>
                    <template v-else-if="currentVariant.soLuong <= 0">Đã hết hàng</template>
                    <template v-else>{{ currentVariant.soLuong }} sản phẩm có sẵn</template>
                 </span>
              </div>
           </div>

           <div class="action-buttons">
              <button class="btn-add-cart" @click="addToCart" :disabled="currentVariant && currentVariant.soLuong <= 0">
                 <i class="fas fa-cart-plus"></i> THÊM VÀO GIỎ
              </button>
              <button class="btn-buy-now" @click="buyNow" :disabled="currentVariant && currentVariant.soLuong <= 0">MUA NGAY</button>
           </div>

           <div class="shop-policy">
              <div class="policy-item"><i class="fas fa-truck"></i> Miễn phí vận chuyển đơn từ 500k</div>
              <div class="policy-item"><i class="fas fa-sync-alt"></i> Đổi trả trong 30 ngày</div>
              <div class="policy-item"><i class="fas fa-shield-alt"></i> Bảo hành chính hãng</div>
           </div>
        </div>
      </div>

      <div class="product-tabs">
         <div class="tab-header">
            <button :class="{ active: activeTab === 'desc' }" @click="activeTab = 'desc'">Mô tả chi tiết</button>
            <button :class="{ active: activeTab === 'guide' }" @click="activeTab = 'guide'">Hướng dẫn chọn size</button>
         </div>
         <div class="tab-content">
            <div v-if="activeTab === 'desc'" class="desc-content">
               <p>{{ product.moTa || 'Đang cập nhật nội dung chi tiết...' }}</p>
            </div>
            <div v-if="activeTab === 'guide'" class="guide-content">
               <p>Bảng size tham khảo...</p>
            </div>
         </div>
      </div>

      <div class="related-products">
         <h3>SẢN PHẨM LIÊN QUAN</h3>
         <div class="related-grid">
            <div v-for="i in 4" :key="i" class="rel-item">
               <img :src="getImageUrl('')" alt="">
               <h4>Sơ mi nữ kiểu Hàn Quốc {{ i }}</h4>
               <span class="price">{{ formatMoney(250000) }}</span>
            </div>
         </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import Swal from 'sweetalert2'; // Dùng để thông báo
import { useCartStore } from '@/stores/cart';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const productId = route.params.id;
const API_URL = 'http://localhost:8080/api/v1';
// URL cơ sở để truy vấn ảnh (tên file hoặc URL) từ backend
const IMAGE_BASE_URL = 'http://localhost:8080/images/';

// STATE
const loading = ref(true);
const activeTab = ref('desc');
const quantity = ref(1);
const selectedColor = ref(null);
const selectedSize = ref(null);
const activeImage = ref('');

// helper trả về đường dẫn ảnh hợp lệ
const getImageUrl = (img) => {
    if (!img) return 'https://placehold.co/500x650?text=No+Image';
    if (img.startsWith('http')) return img;
    return `${IMAGE_BASE_URL}${img}`;
};

// Product Data Structure
const product = reactive({
  id: null,
  tenSanPham: '',
  maSanPham: '',
  thuongHieu: '',
  giaBanMin: 0,
  giaBanMax: 0,
  giaSauGiamMin: null,
  phanTramGiamMax: null,
  moTa: '',
  anhChinh: '',
  variants: [] // Chứa danh sách chi tiết (Màu, Size, Số lượng) từ DB
});

// --- FETCH DATA ---
const fetchProductDetail = async () => {
   loading.value = true;
   try {
       // Gọi 2 API song song: thông tin sản phẩm + danh sách biến thể
       const [productRes, variantsRes] = await Promise.all([
           axios.get(`${API_URL}/products/${productId}`),
           axios.get(`${API_URL}/products/${productId}/variants`)
       ]);
       const data = productRes.data;
       const variantsData = variantsRes.data || [];

       // Map dữ liệu từ API vào state
       product.id = data.id;
       product.tenSanPham = data.tenSanPham;
       product.maSanPham = data.maSanPham;
       product.thuongHieu = data.tenThuongHieu;
       product.giaBanMin = data.giaBanMin || 0;
       product.giaBanMax = data.giaBanMax || 0;
       product.giaSauGiamMin = data.giaSauGiamMin || null;
       product.phanTramGiamMax = data.phanTramGiamMax || null;
       product.moTa = data.moTa;
       product.anhChinh = data.anhChinh;
       
       // Xử lý ảnh chính
       activeImage.value = getImageUrl(data.anhChinh);

       // Map variants từ VariantResponse (mauSac.tenMauSac, kichThuoc.tenKichThuoc)
       product.variants = variantsData
           .filter(v => v.trangThai === 1)
           .map(v => ({
               id: v.id,
               maCtsp: v.maCtsp,
               soLuong: v.soLuong,
               giaBan: v.giaBan,
               giaSauGiam: v.giaSauGiam || null,
               phanTramGiam: v.phanTramGiam || null,
               tenDotGiamGia: v.tenDotGiamGia || null,
               tenMauSac: v.mauSac ? v.mauSac.tenMauSac : '',
               tenKichThuoc: v.kichThuoc ? v.kichThuoc.tenKichThuoc : '',
               listAnh: v.listAnh || []
           }));

       // Tự động chọn biến thể đầu tiên nếu có
       if (product.variants.length > 0) {
           selectedColor.value = product.variants[0].tenMauSac;
           selectedSize.value = product.variants[0].tenKichThuoc;
       }

   } catch (e) {
       console.error("Lỗi tải sản phẩm:", e);
   } finally {
       loading.value = false;
   }
};

// --- COMPUTED PROPERTIES (XỬ LÝ BIẾN THỂ) ---

// Giá hiển thị: ưu tiên giá giảm của biến thể đang chọn, fallback giá min
const displayPrice = computed(() => {
    if (currentVariant.value) {
        return currentVariant.value.giaSauGiam || currentVariant.value.giaBan;
    }
    return product.giaSauGiamMin || product.giaBanMin || 0;
});

// Giá gốc (trước giảm) - chỉ hiển thị khi có giảm giá
const originalPrice = computed(() => {
    if (currentVariant.value && currentVariant.value.giaSauGiam) {
        return currentVariant.value.giaBan;
    }
    if (!currentVariant.value && product.giaSauGiamMin) {
        return product.giaBanMin;
    }
    return null;
});

// Phần trăm giảm giá
const discountPercent = computed(() => {
    if (currentVariant.value && currentVariant.value.phanTramGiam) {
        return currentVariant.value.phanTramGiam;
    }
    if (!currentVariant.value && product.phanTramGiamMax) {
        return product.phanTramGiamMax;
    }
    return null;
});

// Lọc ra danh sách màu duy nhất
const uniqueColors = computed(() => {
    if (!product.variants) return [];
    const colors = product.variants.map(v => v.tenMauSac).filter(Boolean);
    return [...new Set(colors)];
});

// Lọc ra danh sách size khả dụng theo màu đã chọn
const uniqueSizes = computed(() => {
    if (!product.variants) return [];
    let filtered = product.variants;
    if (selectedColor.value) {
        filtered = filtered.filter(v => v.tenMauSac === selectedColor.value);
    }
    const sizes = filtered.map(v => v.tenKichThuoc).filter(Boolean);
    return [...new Set(sizes)];
});

// Tìm biến thể hiện tại dựa trên Màu & Size đã chọn
const currentVariant = computed(() => {
    if (!selectedColor.value || !selectedSize.value) return null;
    return product.variants.find(
        v => v.tenMauSac === selectedColor.value && v.tenKichThuoc === selectedSize.value
    );
});

// Danh sách ảnh hiển thị: ưu tiên ảnh biến thể, fallback ảnh chính
const currentImages = computed(() => {
    if (currentVariant.value && currentVariant.value.listAnh && currentVariant.value.listAnh.length > 0) {
        return currentVariant.value.listAnh;
    }
    // Fallback: gom tất cả ảnh từ các biến thể cùng màu
    if (selectedColor.value) {
        const imgs = product.variants
            .filter(v => v.tenMauSac === selectedColor.value)
            .flatMap(v => v.listAnh || []);
        if (imgs.length > 0) return [...new Set(imgs)];
    }
    return product.anhChinh ? [product.anhChinh] : [];
});

// --- METHODS ---

const selectColor = (colorName) => {
    selectedColor.value = colorName;
    // Reset size nếu combination không tồn tại
    const available = product.variants.filter(v => v.tenMauSac === colorName);
    if (selectedSize.value && !available.find(v => v.tenKichThuoc === selectedSize.value)) {
        selectedSize.value = available.length > 0 ? available[0].tenKichThuoc : null;
    }
};

const selectSize = (sizeName) => {
    selectedSize.value = sizeName;
};

const increaseQty = () => {
    if (currentVariant.value && quantity.value < currentVariant.value.soLuong) {
        quantity.value++;
    } else if (!currentVariant.value) {
        Swal.fire('Thông báo', 'Vui lòng chọn màu và kích thước trước', 'info');
    } else {
        Swal.fire('Thông báo', 'Đã đạt số lượng tối đa trong kho', 'warning');
    }
};

const decreaseQty = () => {
    if (quantity.value > 1) quantity.value--;
};

// Hàm làm mới số lượng tồn kho từ API (không reset lựa chọn người dùng)
const refreshVariantStock = async () => {
    try {
        const res = await axios.get(`${API_URL}/products/${productId}/variants`);
        const variantsData = res.data || [];
        variantsData.forEach(v => {
            const existing = product.variants.find(ev => ev.id === v.id);
            if (existing) {
                existing.soLuong = v.soLuong;
                existing.giaSauGiam = v.giaSauGiam || null;
                existing.phanTramGiam = v.phanTramGiam || null;
                existing.tenDotGiamGia = v.tenDotGiamGia || null;
            }
        });
    } catch (e) { /* silent */ }
};

const addToCart = async () => {
    if (!currentVariant.value) {
        Swal.fire('Chưa chọn phân loại', 'Vui lòng chọn Màu sắc và Kích thước', 'warning');
        return;
    }
    await refreshVariantStock();
    if (currentVariant.value.soLuong <= 0) {
        Swal.fire('Hết hàng', 'Sản phẩm này tạm thời hết hàng', 'error');
        return;
    }

    // Add to cart using Pinia store
    const effectivePrice = currentVariant.value.giaSauGiam || currentVariant.value.giaBan;
    const cartItem = {
        variantId: currentVariant.value.id,
        productId: product.id,
        tenSanPham: product.tenSanPham,
        mauSac: selectedColor.value,
        kichThuoc: selectedSize.value,
        soLuong: quantity.value,
        donGia: effectivePrice,
        giaGoc: currentVariant.value.giaSauGiam ? currentVariant.value.giaBan : null,
        phanTramGiam: currentVariant.value.phanTramGiam || null,
        anh: activeImage.value,
        maxStock: currentVariant.value.soLuong
    };

    cartStore.addItem(cartItem);
    
    Swal.fire({
        icon: 'success',
        title: 'Đã thêm vào giỏ',
        showConfirmButton: false,
        timer: 1000
    });
};

const buyNow = async () => {
    if (!currentVariant.value) {
        Swal.fire('Chưa chọn phân loại', 'Vui lòng chọn Màu sắc và Kích thước', 'warning');
        return;
    }
    await refreshVariantStock();
    if (currentVariant.value.soLuong <= 0) {
        Swal.fire('Hết hàng', 'Sản phẩm này tạm thời hết hàng', 'error');
        return;
    }

    Swal.fire({
        title: 'Xác nhận mua ngay',
        html: `<div style="text-align: left; line-height: 1.8;">
            <p><strong>${product.tenSanPham}</strong></p>
            <p>Màu: <strong>${selectedColor.value}</strong></p>
            <p>Kích thước: <strong>${selectedSize.value}</strong></p>
            <p>Số lượng: <strong>${quantity.value}</strong></p>
            <p style="margin-top: 15px; font-size: 18px; color: #d32f2f;">Giá: <strong>${formatMoney((currentVariant.value.giaSauGiam || currentVariant.value.giaBan) * quantity.value)}</strong></p>
            ${currentVariant.value.giaSauGiam ? `<p style="text-decoration: line-through; color: #999;">Giá gốc: ${formatMoney(currentVariant.value.giaBan * quantity.value)}</p>` : ''}
        </div>`,
        icon: 'info',
        showCancelButton: true,
        confirmButtonText: 'Mua ngay',
        cancelButtonText: 'Hủy',
        confirmButtonColor: '#1e3a8a',
        cancelButtonColor: '#999',
        reverseButtons: true
    }).then((result) => {
        if (result.isConfirmed) {
            // Clear cart and add only this item
            cartStore.clearCart();
            
            const buyPrice = currentVariant.value.giaSauGiam || currentVariant.value.giaBan;
            const cartItem = {
                variantId: currentVariant.value.id,
                productId: product.id,
                tenSanPham: product.tenSanPham,
                mauSac: selectedColor.value,
                kichThuoc: selectedSize.value,
                soLuong: quantity.value,
                donGia: buyPrice,
                giaGoc: currentVariant.value.giaSauGiam ? currentVariant.value.giaBan : null,
                phanTramGiam: currentVariant.value.phanTramGiam || null,
                anh: activeImage.value,
                maxStock: currentVariant.value.soLuong
            };
            
            cartStore.addItem(cartItem);
            
            // Navigate to checkout
            router.push('/checkout');
        }
    });
};

const formatMoney = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

const getColorCode = (name) => {
    const map = { 'Đỏ': '#ef4444', 'Xanh': '#3b82f6', 'Vàng': '#eab308', 'Đen': '#000', 'Trắng': '#f3f4f6', 'Tím': '#a855f7', 'Cam': '#f97316', 'Xám': '#6b7280', 'Nâu': '#78350f', 'Hồng': '#ec4899' };
    for(let k in map) if(name && name.includes(k)) return map[k];
    return '#ccc'; // Mặc định xám nhạt
};

const handleImageError = (e) => {
    e.target.src = "https://via.placeholder.com/500x650?text=No+Image";
};

// Reset quantity và cập nhật ảnh khi đổi biến thể
watch(currentVariant, (variant) => {
    quantity.value = 1;
    if (variant && variant.listAnh && variant.listAnh.length > 0) {
        activeImage.value = getImageUrl(variant.listAnh[0]);
    } else if (product.anhChinh) {
        activeImage.value = getImageUrl(product.anhChinh);
    }
});

// Auto-refresh: khi tab được focus lại hoặc mỗi 30 giây
let stockInterval;
const onVisibilityChange = () => {
    if (document.visibilityState === 'visible') refreshVariantStock();
};

onMounted(() => {
   fetchProductDetail();
   document.addEventListener('visibilitychange', onVisibilityChange);
   stockInterval = setInterval(refreshVariantStock, 30000);
});

onUnmounted(() => {
   document.removeEventListener('visibilitychange', onVisibilityChange);
   if (stockInterval) clearInterval(stockInterval);
});
</script>

<style scoped>
/* GENERAL */
.product-detail-page {
  font-family: Arial, sans-serif; color: #333; padding: 30px 0; background: #fff; min-height: 80vh;
}
.container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }

/* LOADING */
.loading-state { display: flex; justify-content: center; align-items: center; height: 400px; }
.spinner { border: 4px solid #f3f3f3; border-top: 4px solid #0f172a; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

/* Breadcrumb */
.breadcrumb { margin-bottom: 20px; font-size: 14px; color: #666; }
.breadcrumb span { cursor: pointer; }
.breadcrumb span:hover { color: #0f172a; text-decoration: underline; }
.breadcrumb .current { color: #0f172a; font-weight: 600; cursor: default; text-decoration: none; }

/* GRID LAYOUT */
.detail-grid { display: grid; grid-template-columns: 50% 50%; gap: 40px; margin-bottom: 50px; }

/* LEFT: GALLERY */
.product-gallery { display: flex; flex-direction: column; gap: 15px; }
.main-image { 
   width: 100%; aspect-ratio: 3/4; overflow: hidden; border-radius: 8px; border: 1px solid #f1f5f9;
}
.main-image img { width: 100%; height: 100%; object-fit: cover; }

.thumb-list { display: flex; gap: 10px; overflow-x: auto; }
.thumb-item { 
   width: 80px; height: 100px; border: 1px solid #eee; border-radius: 4px; cursor: pointer; overflow: hidden; 
   opacity: 0.6; transition: 0.2s; flex-shrink: 0;
}
.thumb-item.active { border-color: #0f172a; opacity: 1; border-width: 2px; }
.thumb-item img { width: 100%; height: 100%; object-fit: cover; }

/* RIGHT: INFO */
.p-title { font-size: 28px; font-weight: 700; margin: 0 0 10px; color: #0f172a; }
.p-meta { font-size: 14px; color: #666; margin-bottom: 20px; }
.divider { margin: 0 10px; color: #ddd; }

.p-price { display: flex; align-items: flex-end; gap: 10px; margin-bottom: 20px; }
.current-price { font-size: 26px; font-weight: 700; color: #d32f2f; }
.price-range { font-size: 18px; color: #64748b; font-weight: 500; }
.old-price { font-size: 16px; text-decoration: line-through; color: #999; margin-bottom: 5px; }
.original-price { font-size: 16px; text-decoration: line-through; color: #999; align-self: center; }
.discount-badge { background: #d32f2f; color: white; padding: 2px 8px; border-radius: 4px; font-size: 13px; font-weight: 600; align-self: center; }
.sale-label { background: #d32f2f; color: white; padding: 2px 6px; border-radius: 4px; font-size: 12px; margin-bottom: 8px; }

.p-desc-short { font-size: 15px; line-height: 1.6; color: #555; margin-bottom: 30px; }

/* SELECTOR */
.selector-row { margin-bottom: 20px; }
.label { font-weight: 700; margin-bottom: 8px; display: block; font-size: 14px; }
.options { display: flex; flex-wrap: wrap; gap: 10px; }

.color-opt { 
   width: 32px; height: 32px; border-radius: 50%; border: 1px solid #ddd; cursor: pointer; position: relative; 
}
.color-opt.active { box-shadow: 0 0 0 2px white, 0 0 0 4px #0f172a; border-color: transparent; }

.size-opt {
   min-width: 40px; height: 35px; border: 1px solid #ddd; display: flex; align-items: center; justify-content: center;
   cursor: pointer; border-radius: 4px; font-weight: 500; font-size: 14px; transition: 0.2s; padding: 0 12px;
}
.size-opt:hover { border-color: #0f172a; }
.size-opt.active { background: #0f172a; color: white; border-color: #0f172a; }

.qty-control { display: flex; align-items: center; border: 1px solid #ddd; width: 120px; height: 36px; border-radius: 4px; }
.qty-control button { width: 36px; height: 100%; border: none; background: #f9f9f9; cursor: pointer; font-size: 16px; }
.qty-control button:hover { background: #e2e8f0; }
.qty-control input { flex: 1; width: 100%; text-align: center; border: none; outline: none; font-weight: 600; }
.stock-info { margin-left: 15px; font-size: 13px; color: #64748b; font-style: italic; }
.stock-info.out-of-stock { color: #dc2626; font-weight: 700; font-style: normal; }
.btn-add-cart:disabled, .btn-buy-now:disabled { opacity: 0.5; cursor: not-allowed; background: #94a3b8 !important; }
.selected-val { margin-left: 10px; font-size: 13px; font-weight: 600; color: #334155; }

/* BUTTONS ACTION */
.action-buttons { display: flex; gap: 15px; margin-top: 30px; margin-bottom: 30px; }
.btn-add-cart {
   flex: 1; height: 48px; background: white; border: 1px solid #0f172a; color: #0f172a; 
   font-weight: 700; cursor: pointer; transition: 0.2s; border-radius: 4px;
}
.btn-add-cart:hover { background: #f1f5f9; }
.btn-buy-now {
   flex: 1; height: 48px; background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
   color: white; border: none; font-weight: 700; cursor: pointer; transition: 0.2s; border-radius: 4px;
}
.btn-buy-now:hover { opacity: 0.9; box-shadow: 0 4px 10px rgba(15, 23, 42, 0.3); }

/* POLICY */
.shop-policy { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; border-top: 1px solid #eee; padding-top: 20px; }
.policy-item { font-size: 13px; color: #555; display: flex; align-items: center; gap: 8px; }
.policy-item i { color: #1e3a8a; }

/* TABS */
.product-tabs { margin-bottom: 50px; }
.tab-header { border-bottom: 1px solid #eee; display: flex; gap: 30px; margin-bottom: 20px; }
.tab-header button { 
   background: none; border: none; padding: 10px 0; font-size: 16px; font-weight: 600; color: #999; 
   cursor: pointer; border-bottom: 2px solid transparent; transition: 0.2s;
}
.tab-header button.active { color: #0f172a; border-bottom-color: #0f172a; }
.tab-content { font-size: 15px; line-height: 1.6; color: #444; }

/* RELATED */
.related-products h3 { text-align: center; font-size: 24px; margin-bottom: 30px; color: #0f172a; }
.related-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; }
.rel-item { cursor: pointer; transition: 0.3s; }
.rel-item:hover { transform: translateY(-5px); }
.rel-item img { width: 100%; aspect-ratio: 3/4; object-fit: cover; border-radius: 6px; margin-bottom: 10px; }
.rel-item h4 { font-size: 15px; margin: 0 0 5px; color: #333; }
.rel-item .price { font-weight: 700; color: #0f172a; }

/* RESPONSIVE */
@media (max-width: 768px) {
   .detail-grid { grid-template-columns: 1fr; }
   .related-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>