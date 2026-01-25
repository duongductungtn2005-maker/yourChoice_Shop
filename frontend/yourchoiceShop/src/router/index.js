import { createRouter, createWebHistory } from 'vue-router'

// --- 1. IMPORT CÁC MÀN HÌNH CỦA TÙNG (THUỘC TÍNH SẢN PHẨM) ---
// (Giữ nguyên import tĩnh để tránh lỗi, hoặc chuyển sang dynamic cũng được)
import CoAoIndex from '../views/admin/attribute/CoAoIndex.vue'
import TayAoIndex from '../views/admin/attribute/TayAoIndex.vue'
import ChatLieuIndex from '../views/admin/attribute/ChatLieuIndex.vue'
import XuatXuIndex from '../views/admin/attribute/XuatXuIndex.vue'
import ThuongHieuIndex from '../views/admin/attribute/ThuongHieuIndex.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ==========================================
    // 1. ROUTE ĐĂNG NHẬP (Public)
    // ==========================================
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },

    // ==========================================
    // 2. KHU VỰC ADMIN (Có Layout, Sidebar)
    // ==========================================
    {
      path: '/admin',
      component: () => import('../layouts/AdminLayout.vue'),
      meta: { requiresAuth: true }, // Check quyền đăng nhập
      children: [
        // --- Dashboard ---
        {
          path: '', 
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('../views/admin/dashboard/Dashboard.vue')
        },

        // --- QUẢN LÝ SẢN PHẨM (Gốc) ---
        {
          path: 'products',
          name: 'admin-product-list',
          component: () => import('../views/admin/product/ProductIndex.vue')
        },
        {
          path: 'products/create',
          name: 'admin-product-create',
          component: () => import('../views/admin/product/ProductCreate.vue')
        },
        {
          path: 'products/:id',
          name: 'admin-product-detail',
          component: () => import('../views/admin/product/ProductDetail.vue')
        },

        // --- QUẢN LÝ THUỘC TÍNH (CODE CỦA TÙNG - MỚI THÊM VÀO) ---
        { 
            path: 'mau-sac', 
            name: 'mau-sac',
            component: () => import('../views/admin/attribute/MauSac.vue') 
        },
        { 
            path: 'kich-thuoc', 
            name: 'kich-thuoc',
            component: () => import('../views/admin/attribute/KichThuoc.vue') 
        },
        { path: 'co-ao', name: 'co-ao', component: CoAoIndex },
        { path: 'tay-ao', name: 'tay-ao', component: TayAoIndex },
        { path: 'chat-lieu', name: 'chat-lieu', component: ChatLieuIndex },
        { path: 'xuat-xu', name: 'xuat-xu', component: XuatXuIndex },
        { path: 'thuong-hieu', name: 'thuong-hieu', component: ThuongHieuIndex },

        // --- QUẢN LÝ HÓA ĐƠN ---
        {
          path: 'orders',
          name: 'admin-order-list',
          component: () => import('../views/admin/DonHang/QuanLyDonHang.vue')
        },
        {
          path: 'orders/:id',
          name: 'admin-order-detail',
          component: () => import('../views/admin/DonHang/ChiTietDonHang.vue')
        },

        // --- BÁN HÀNG TẠI QUẦY (POS) ---
        {
          path: 'pos',
          name: 'admin-pos',
          component: () => import('../views/admin/pos/CounterSales.vue'),
          meta: { layout: 'full' } 
        },

        // --- QUẢN LÝ KHÁCH HÀNG ---
        {
          path: 'customers',
          name: 'admin-customer-list',
          component: () => import('../views/admin/customer/CustomerIndex.vue')
        },

        // --- QUẢN LÝ NHÂN VIÊN ---
        {
          path: 'employees',
          name: 'admin-employee-list',
          component: () => import('../views/admin/employee/EmployeeList.vue')
        },
        {
          path: 'employees/create', 
          name: 'admin-employee-create',
          component: () => import('../views/admin/employee/AddEmployee.vue')
        },

        // --- QUẢN LÝ KHUYẾN MÃI (VOUCHER / SALES) ---
        {
          path: 'vouchers',
          name: 'admin-voucher-list',
          component: () => import('../views/admin/voucher/VoucherIndex.vue')
        },
        {
          path: 'sales',
          name: 'admin-sale-list',
          component: () => import('../views/admin/promotion/SaleIndex.vue')
        }
      ]
    },

    // ==========================================
    // 3. CÁC ROUTE KHÁC
    // ==========================================
    // Redirect trang chủ về login hoặc admin (Tùy bạn chọn)
    { path: '/', redirect: '/login' },

    // Catch all (404) -> Về Login
    {
        path: '/:pathMatch(.*)*',
        redirect: '/login'
    }
  ]
})

export default router