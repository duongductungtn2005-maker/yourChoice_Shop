import { createRouter, createWebHistory } from 'vue-router'

// Import các màn hình (Đảm bảo đường dẫn đúng với cấu trúc thư mục của bạn)
import AdminLayout from '../layouts/AdminLayout.vue'
import ProductIndex from '../views/admin/product/ProductIndex.vue'
import ProductCreate from '../views/admin/product/ProductCreate.vue'
import ProductDetail from '../views/admin/product/ProductDetail.vue'

// --- CÁC MÀN HÌNH THUỘC TÍNH (Mới thêm) ---
import CoAoIndex from '../views/admin/attribute/CoAoIndex.vue'
import TayAoIndex from '../views/admin/attribute/TayAoIndex.vue'
import ChatLieuIndex from '../views/admin/attribute/ChatLieuIndex.vue'
import XuatXuIndex from '../views/admin/attribute/XuatXuIndex.vue'
import ThuongHieuIndex from '../views/admin/attribute/ThuongHieuIndex.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // Trang Admin (Layout chung)
    {
      path: '/admin',
      component: AdminLayout,
      children: [
        // Quản lý sản phẩm
        { path: 'products', name: 'product-list', component: ProductIndex },
        { path: 'products/create', name: 'product-create', component: ProductCreate },
        { path: 'products/:id', name: 'product-detail', component: ProductDetail },
{
    path: '/admin/mau-sac',
    component: () => import('../views/admin/attribute/MauSac.vue')
},
{
    path: '/admin/kich-thuoc',
    component: () => import('../views/admin/attribute/KichThuoc.vue')
},
        // --- CÁC ROUTE CHO THUỘC TÍNH (Thêm đoạn này vào) ---
        { 
          path: 'co-ao', 
          name: 'co-ao', 
          component: CoAoIndex 
        },
        { 
          path: 'tay-ao', 
          name: 'tay-ao', 
          component: TayAoIndex 
        },
        { 
          path: 'chat-lieu', 
          name: 'chat-lieu', 
          component: ChatLieuIndex 
        },
        { 
          path: 'xuat-xu', 
          name: 'xuat-xu', 
          component: XuatXuIndex 
        },
        { 
          path: 'thuong-hieu', 
          name: 'thuong-hieu', 
          component: ThuongHieuIndex 
        }
      ]
    },
    
    // Redirect trang chủ về admin products (Tùy chọn)
    { path: '/', redirect: '/admin/products' }
  ]
})

export default router