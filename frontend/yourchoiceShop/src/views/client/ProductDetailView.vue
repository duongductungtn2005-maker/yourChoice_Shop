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
                v-for="(img, idx) in allImages" 
                :key="idx" 
                class="thumb-item"
                :class="{ active: activeImage === img }"
                @click="activeImage = img"
              >
                 <img :src="img" alt="Thumbnail" @error="handleImageError">
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
              <span v-if="originalPrice > displayPrice" class="old-price">{{ formatMoney(originalPrice) }}</span>
           </div>

           <div class="p-desc-short">
              {{ product.moTa || 'Sản phẩm thời trang cao cấp từ YourChoice Shop.' }}
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
                       v-for="sizeName in availableSizes" 
                       :key="sizeName" 
                       class="size-opt"
                       :class="{ active: selectedSize === sizeName, disabled: !isSizeAvailable(sizeName) }"
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
                    <input type="text" :value="quantity" readonly>
                    <button @click="increaseQty">+</button>
                 </div>
                 <span class="stock-info" :class="{ 'out-of-stock': currentVariant && currentVariant.soLuong <= 0 }">
                    <template v-if="currentVariant">
                      <template v-if="currentVariant.soLuong > 0">{{ currentVariant.soLuong }} sản phẩm có sẵn</template>
                      <template v-else>Hết hàng</template>
                    </template>
                    <template v-else>Vui lòng chọn phân loại</template>
                 </span>
              </div>
           </div>

           <div class="action-buttons">
              <button class="btn-add-cart" @click="addToCart" :disabled="!currentVariant || currentVariant.soLuong <= 0">
                 <i class="fas fa-cart-plus"></i> THÊM VÀO GIỎ
              </button>
              <button class="btn-buy-now" @click="buyNow" :disabled="!currentVariant || currentVariant.soLuong <= 0">MUA NGAY</button>
           </div>

           <div class="shop-policy">
              <div class="policy-item"><i class="fas fa-truck"></i> Miễn phí vận chuyển đơn từ 500k</div>
              <div class="policy-item"><i class="fas fa-sync-alt"></i> Đổi trả trong 30 ngày</div>
              <div class="policy-item"><i class="fas fa-shield-alt"></i> Bảo hành chính hãng</div>
           </div>
        </div>
      </div>

      <!-- Tabs mô tả -->
      <div class="product-tabs">
         <div class="tab-header">
            <button :class="{ active: activeTab === 'desc' }" @click="activeTab = 'desc'">Mô tả chi tiết</button>
            <button :class="{ active: activeTab === 'spec' }" @click="activeTab = 'spec'">Thông số kỹ thuật</button>
            <button :class="{ active: activeTab === 'guide' }" @click="activeTab = 'guide'">Hướng dẫn chọn size</button>
         </div>
         <div class="tab-content">
            <div v-if="activeTab === 'desc'" class="desc-content">
               <p>{{ product.moTa || 'Đang cập nhật nội dung chi tiết...' }}</p>
            </div>
            <div v-if="activeTab === 'spec'" class="spec-content">
               <table class="spec-table">
                  <tr><td>Thương hiệu</td><td>{{ product.thuongHieu }}</td></tr>
                  <tr><td>Chất liệu</td><td>{{ product.chatLieu || 'N/A' }}</td></tr>
                  <tr><td>Xuất xứ</td><td>{{ product.xuatXu || 'N/A' }}</td></tr>
                  <tr><td>Cổ áo</td><td>{{ product.coAo || 'N/A' }}</td></tr>
                  <tr><td>Tay áo</td><td>{{ product.tayAo || 'N/A' }}</td></tr>
                  <tr><td>Màu sắc có sẵn</td><td>{{ uniqueColors.join(', ') || 'N/A' }}</td></tr>
                  <tr><td>Kích thước có sẵn</td><td>{{ uniqueSizes.join(', ') || 'N/A' }}</td></tr>
               </table>
            </div>
            <div v-if="activeTab === 'guide'" class="guide-content">
               <table class="size-guide-table">
                  <thead><tr><th>Size</th><th>Cân nặng (kg)</th><th>Chiều cao (cm)</th></tr></thead>
                  <tbody>
                     <tr><td>S</td><td>40-50</td><td>150-160</td></tr>
                     <tr><td>M</td><td>50-58</td><td>158-165</td></tr>
                     <tr><td>L</td><td>55-65</td><td>163-170</td></tr>
                     <tr><td>XL</td><td>63-73</td><td>168-175</td></tr>
                     <tr><td>XXL</td><td>70-80</td><td>173-180</td></tr>
                  </tbody>
               </table>
            </div>
         </div>
      </div>

      <!-- Sản phẩm liên quan -->
      <div class="related-products" v-if="relatedProducts.length > 0">
         <h3>SẢN PHẨM LIÊN QUAN</h3>
         <div class="related-grid">
            <div 
              v-for="rp in relatedProducts" 
              :key="rp.id" 
              class="rel-item"
              @click="goToProduct(rp.id)"
            >
               <img :src="getRelatedImage(rp)" :alt="rp.tenSanPham" @error="handleImageError">
               <h4>{{ rp.tenSanPham }}</h4>
               <span class="price">{{ formatMoney(rp.giaBan || rp.giaGoc || 0) }}</span>
            </div>
         </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail, getProductVariants, getProducts } from '@/api/clientApi'
import { useCartStore } from '@/stores/cart'
import Swal from 'sweetalert2'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const loading = ref(true)
const activeTab = ref('desc')
const quantity = ref(1)
const selectedColor = ref(null)
const selectedSize = ref(null)
const activeImage = ref('')
const relatedProducts = ref([])

const product = reactive({
  id: null, tenSanPham: '', maSanPham: '', thuongHieu: '',
  chatLieu: '', xuatXu: '', coAo: '', tayAo: '',
  giaBan: 0, moTa: '', listAnh: [], variants: []
})

const allImages = computed(() => {
  // Kết hợp ảnh từ tất cả variants
  const imgs = new Set()
  if (product.listAnh) product.listAnh.forEach(img => imgs.add(img))
  product.variants.forEach(v => {
    if (v.listAnh) v.listAnh.forEach(img => {
      const url = img.startsWith('http') ? img : `http://localhost:8080/images/${img}`
      imgs.add(url)
    })
  })
  return imgs.size > 0 ? [...imgs] : ['https://placehold.co/500x650?text=No+Image']
})

const fetchProductDetail = async () => {
   loading.value = true
   try {
       const [prodRes, varRes] = await Promise.all([
         getProductDetail(route.params.id),
         getProductVariants(route.params.id),
       ])
       const data = prodRes.data

       product.id = data.id
       product.tenSanPham = data.tenSanPham
       product.maSanPham = data.maSanPham
       product.thuongHieu = data.tenThuongHieu || ''
       product.chatLieu = data.tenChatLieu || ''
       product.xuatXu = data.tenXuatXu || ''
       product.coAo = data.tenCoAo || ''
       product.tayAo = data.tenTayAo || ''
       product.moTa = data.moTa || data.moTaChiTiet || ''

       // Variants
       const variants = varRes.data || []
       product.variants = variants.map(v => ({
         id: v.id,
         tenMauSac: v.mauSac?.ten || v.mauSac?.tenMauSac || '',
         tenKichThuoc: v.kichThuoc?.ten || v.kichThuoc?.tenKichThuoc || '',
         soLuong: v.soLuong || 0,
         giaBan: v.giaBan || 0,
         giaNhap: v.giaNhap || 0,
         listAnh: v.listAnh || [],
       }))

       // Images
       if (variants.length > 0) {
         const firstImg = variants[0].listAnh?.[0]
         if (firstImg) {
           activeImage.value = firstImg.startsWith('http') ? firstImg : `http://localhost:8080/images/${firstImg}`
         } else {
           activeImage.value = 'https://placehold.co/500x650?text=No+Image'
         }
       }

       // Giá mặc định
       if (product.variants.length > 0) {
         const priced = product.variants.filter(v => v.giaBan > 0)
         product.giaBan = priced.length > 0 ? Math.min(...priced.map(v => v.giaBan)) : 0
       }

       // Auto-select first variant
       if (product.variants.length > 0) {
           selectedColor.value = product.variants[0].tenMauSac
           selectedSize.value = product.variants[0].tenKichThuoc
       }

       // Fetch related products
       fetchRelated(data.idThuongHieu)
   } catch (e) {
       console.error('Lỗi tải sản phẩm:', e)
   } finally {
       loading.value = false
   }
}

const fetchRelated = async (brandId) => {
  try {
    const res = await getProducts({ page: 0, size: 4, status: 1, idThuongHieu: brandId || undefined })
    relatedProducts.value = (res.data.content || []).filter(p => p.id !== product.id).slice(0, 4)
  } catch (e) { console.error('Lỗi tải SP liên quan:', e) }
}

// Computed
const uniqueColors = computed(() => [...new Set(product.variants.map(v => v.tenMauSac).filter(Boolean))])
const uniqueSizes = computed(() => [...new Set(product.variants.map(v => v.tenKichThuoc).filter(Boolean))])

const availableSizes = computed(() => {
  if (!selectedColor.value) return uniqueSizes.value
  return [...new Set(product.variants.filter(v => v.tenMauSac === selectedColor.value).map(v => v.tenKichThuoc).filter(Boolean))]
})

const isSizeAvailable = (sizeName) => {
  if (!selectedColor.value) return true
  const v = product.variants.find(x => x.tenMauSac === selectedColor.value && x.tenKichThuoc === sizeName)
  return v && v.soLuong > 0
}

const currentVariant = computed(() => {
    if (!selectedColor.value || !selectedSize.value) return null
    return product.variants.find(
        v => v.tenMauSac === selectedColor.value && v.tenKichThuoc === selectedSize.value
    )
})

const displayPrice = computed(() => {
  if (currentVariant.value && currentVariant.value.giaBan > 0) return currentVariant.value.giaBan
  return product.giaBan || 0
})

const originalPrice = computed(() => {
  return displayPrice.value > 0 ? Math.round(displayPrice.value * 1.1) : 0
})

// Methods
const selectColor = (colorName) => {
    selectedColor.value = colorName
    // Update images based on color
    const colorVariants = product.variants.filter(v => v.tenMauSac === colorName)
    if (colorVariants.length > 0 && colorVariants[0].listAnh?.length > 0) {
      const img = colorVariants[0].listAnh[0]
      activeImage.value = img.startsWith('http') ? img : `http://localhost:8080/images/${img}`
    }
    // Reset size if not available
    if (selectedSize.value && !availableSizes.value.includes(selectedSize.value)) {
      selectedSize.value = availableSizes.value[0] || null
    }
}

const selectSize = (sizeName) => { selectedSize.value = sizeName }

const increaseQty = () => {
    if (!currentVariant.value) {
        Swal.fire('Thông báo', 'Vui lòng chọn màu và kích thước trước', 'info')
        return
    }
    if (quantity.value < currentVariant.value.soLuong) quantity.value++
    else Swal.fire('Thông báo', 'Đã đạt số lượng tối đa trong kho', 'warning')
}

const decreaseQty = () => { if (quantity.value > 1) quantity.value-- }

const addToCart = () => {
    if (!currentVariant.value) {
        Swal.fire('Chưa chọn phân loại', 'Vui lòng chọn Màu sắc và Kích thước', 'warning')
        return
    }
    if (currentVariant.value.soLuong <= 0) {
        Swal.fire('Hết hàng', 'Sản phẩm này tạm thời hết hàng', 'error')
        return
    }

    cartStore.addItem({
        variantId: currentVariant.value.id,
        productId: product.id,
        tenSanPham: product.tenSanPham,
        mauSac: selectedColor.value,
        kichThuoc: selectedSize.value,
        soLuong: quantity.value,
        donGia: displayPrice.value,
        anh: activeImage.value,
        maxStock: currentVariant.value.soLuong,
    })

    Swal.fire({ icon: 'success', title: 'Đã thêm vào giỏ', showConfirmButton: false, timer: 1000 })
}

const buyNow = () => {
    addToCart()
    setTimeout(() => router.push('/cart'), 500)
}

const goToProduct = (id) => {
  router.push(`/product/${id}`)
  // Reload data khi navigate cùng route
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const formatMoney = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0)

const getColorCode = (name) => {
    const map = { 'Đỏ': '#ef4444', 'Xanh': '#3b82f6', 'Vàng': '#eab308', 'Đen': '#000', 'Trắng': '#f3f4f6', 'Tím': '#a855f7', 'Cam': '#f97316', 'Xám': '#6b7280', 'Nâu': '#78350f', 'Hồng': '#ec4899', 'Be': '#d4a574' }
    for (let k in map) if (name && name.includes(k)) return map[k]
    return '#ccc'
}

const handleImageError = (e) => { e.target.src = 'https://placehold.co/500x650?text=No+Image' }

const getRelatedImage = (prod) => {
  if (prod.listAnh && prod.listAnh.length > 0) {
    const img = prod.listAnh[0]
    return img.startsWith('http') ? img : `http://localhost:8080/images/${img}`
  }
  return 'https://placehold.co/300x400?text=No+Image'
}

watch(currentVariant, () => { quantity.value = 1 })

// Watch route changes for same component navigation
watch(() => route.params.id, (newId) => {
  if (newId) fetchProductDetail()
})

onMounted(() => { fetchProductDetail() })
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
.old-price { font-size: 16px; text-decoration: line-through; color: #999; margin-bottom: 5px; }
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
.size-opt.disabled { opacity: 0.4; cursor: not-allowed; text-decoration: line-through; }

.qty-control { display: flex; align-items: center; border: 1px solid #ddd; width: 120px; height: 36px; border-radius: 4px; }
.qty-control button { width: 36px; height: 100%; border: none; background: #f9f9f9; cursor: pointer; font-size: 16px; }
.qty-control button:hover { background: #e2e8f0; }
.qty-control input { flex: 1; width: 100%; text-align: center; border: none; outline: none; font-weight: 600; }
.stock-info { margin-left: 15px; font-size: 13px; color: #64748b; font-style: italic; }
.selected-val { margin-left: 10px; font-size: 13px; font-weight: 600; color: #334155; }

/* BUTTONS ACTION */
.action-buttons { display: flex; gap: 15px; margin-top: 30px; margin-bottom: 30px; }
.btn-add-cart {
   flex: 1; height: 48px; background: white; border: 1px solid #0f172a; color: #0f172a; 
   font-weight: 700; cursor: pointer; transition: 0.2s; border-radius: 4px;
}
.btn-add-cart:hover { background: #f1f5f9; }
.btn-add-cart:disabled, .btn-buy-now:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-buy-now {
   flex: 1; height: 48px; background: linear-gradient(135deg, #1e3a8a 0%, #0f172a 100%); 
   color: white; border: none; font-weight: 700; cursor: pointer; transition: 0.2s; border-radius: 4px;
}
.btn-buy-now:hover { opacity: 0.9; box-shadow: 0 4px 10px rgba(15, 23, 42, 0.3); }

/* POLICY */
.shop-policy { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; border-top: 1px solid #eee; padding-top: 20px; }
.policy-item { font-size: 13px; color: #555; display: flex; align-items: center; gap: 8px; }

/* TABS */
.product-tabs { margin-bottom: 50px; }
.tab-header { border-bottom: 1px solid #eee; display: flex; gap: 30px; margin-bottom: 20px; }
.tab-header button { 
   background: none; border: none; padding: 10px 0; font-size: 16px; font-weight: 600; color: #999; 
   cursor: pointer; border-bottom: 2px solid transparent; transition: 0.2s;
}
.tab-header button.active { color: #0f172a; border-bottom-color: #0f172a; }
.tab-content { font-size: 15px; line-height: 1.6; color: #444; }

.spec-table { width: 100%; border-collapse: collapse; }
.spec-table tr:nth-child(even) { background: #f8fafc; }
.spec-table td { padding: 12px 16px; border-bottom: 1px solid #f1f5f9; font-size: 14px; }
.spec-table td:first-child { font-weight: 600; color: #334155; width: 200px; }

.size-guide-table { width: 100%; border-collapse: collapse; text-align: center; }
.size-guide-table th { background: #0f172a; color: #fff; padding: 12px; font-size: 14px; }
.size-guide-table td { padding: 10px; border-bottom: 1px solid #f1f5f9; font-size: 14px; }
.size-guide-table tr:hover { background: #f8fafc; }

.out-of-stock { color: #ef4444 !important; font-weight: 600; }

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