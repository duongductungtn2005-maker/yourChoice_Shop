// File: src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 1. Route Đăng nhập
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },

    // 2. Route Admin (Sử dụng AdminLayout)
    {
      path: '/admin',
      component: () => import('../layouts/AdminLayout.vue'),
      meta: { requiresAuth: true },
      // LƯU Ý: Tất cả các trang con của Admin phải nằm trong mảng children này
      children: [
        {
          path: '', 
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('../views/admin/dashboard/Dashboard.vue')
        },
        
        // --- Quản lý Sản phẩm ---
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

        // --- Quản lý Hóa đơn ---
        {
          path: 'orders',
          name: 'admin-order-list',
          component: () => import('../views/admin/order/OrderIndex.vue')
        },
        {
          path: 'orders/:id',
          name: 'admin-order-detail',
          component: () => import('../views/admin/order/OrderDetail.vue')
        },

        // --- Bán hàng tại quầy (POS) ---
        {
          path: 'pos',
          name: 'admin-pos',
          component: () => import('../views/admin/pos/CounterSales.vue'),
          meta: { layout: 'full' }
        },

        // --- Quản lý Khách hàng & Nhân viên ---
        {
          path: 'customers',
          name: 'admin-customer-list',
          component: () => import('../views/admin/customer/CustomerIndex.vue')
        },
        {
          path: 'employees',
          name: 'admin-employee-list',
          component: () => import('../views/admin/employee/EmployeeIndex.vue')
        },

        // --- Quản lý Voucher (Phiếu giảm giá) ---
        // SỬA LỖI: Đặt ở đây (trong children) thì không cần dấu "/" ở đầu
        {
          path: 'vouchers',
          name: 'admin-voucher-list',
          component: () => import('../views/admin/voucher/VoucherIndex.vue')
        },
        {
          path: 'vouchers/create',
          name: 'admin-voucher-create',
          component: () => import('../views/admin/voucher/VoucherCreate.vue')
        },

        // --- Quản lý Đợt giảm giá (Promotion) ---
        {
          path: 'sales',
          name: 'admin-sale-list',
          component: () => import('../views/admin/promotion/SaleIndex.vue')
        },
        {
          path: 'sales/create',
          name: 'admin-sale-create',
          component: () => import('../views/admin/promotion/SaleCreate.vue')
        }
      ]
    },

    // 3. Catch all (404)
    {
        path: '/:pathMatch(.*)*',
        redirect: '/login'
    }
  ]
})

export default router