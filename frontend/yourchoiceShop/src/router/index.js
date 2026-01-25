<<<<<<< HEAD
// File: src/router/index.js
=======
>>>>>>> upstream/main
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
<<<<<<< HEAD
    // 1. Route Đăng nhập
=======
    // 1. Route Đăng nhập (Không có layout admin)
>>>>>>> upstream/main
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },

    // 2. Route Admin (Sử dụng AdminLayout)
    {
      path: '/admin',
      component: () => import('../layouts/AdminLayout.vue'),
<<<<<<< HEAD
      meta: { requiresAuth: true },
      // LƯU Ý: Tất cả các trang con của Admin phải nằm trong mảng children này
      children: [
        {
          path: '', 
=======
      meta: { requiresAuth: true }, // Sau này dùng để check quyền
      children: [
        {
          path: '', // Mặc định vào dashboard
>>>>>>> upstream/main
          redirect: '/admin/dashboard'
        },
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          component: () => import('../views/admin/dashboard/Dashboard.vue')
        },
<<<<<<< HEAD
        
=======
>>>>>>> upstream/main
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
<<<<<<< HEAD

=======
>>>>>>> upstream/main
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
<<<<<<< HEAD

=======
>>>>>>> upstream/main
        // --- Bán hàng tại quầy (POS) ---
        {
          path: 'pos',
          name: 'admin-pos',
          component: () => import('../views/admin/pos/CounterSales.vue'),
<<<<<<< HEAD
          meta: { layout: 'full' }
        },

        // --- Quản lý Khách hàng & Nhân viên ---
=======
          meta: { layout: 'full' } // Có thể ẩn sidebar nếu cần không gian rộng
        },
        // --- Quản lý Khách hàng ---
>>>>>>> upstream/main
        {
          path: 'customers',
          name: 'admin-customer-list',
          component: () => import('../views/admin/customer/CustomerIndex.vue')
        },
<<<<<<< HEAD
=======
        // --- Quản lý Nhân viên ---
>>>>>>> upstream/main
        {
          path: 'employees',
          name: 'admin-employee-list',
          component: () => import('../views/admin/employee/EmployeeIndex.vue')
        },
<<<<<<< HEAD

        // --- Quản lý Voucher (Phiếu giảm giá) ---
        // SỬA LỖI: Đặt ở đây (trong children) thì không cần dấu "/" ở đầu
=======
        // --- Quản lý Voucher (Phiếu giảm giá) ---
>>>>>>> upstream/main
        {
          path: 'vouchers',
          name: 'admin-voucher-list',
          component: () => import('../views/admin/voucher/VoucherIndex.vue')
        },
<<<<<<< HEAD
        {
          path: 'vouchers/create',
          name: 'admin-voucher-create',
          component: () => import('../views/admin/voucher/VoucherCreate.vue')
        },

=======
>>>>>>> upstream/main
        // --- Quản lý Đợt giảm giá (Promotion) ---
        {
          path: 'sales',
          name: 'admin-sale-list',
          component: () => import('../views/admin/promotion/SaleIndex.vue')
<<<<<<< HEAD
        },
        {
          path: 'sales/create',
          name: 'admin-sale-create',
          component: () => import('../views/admin/promotion/SaleCreate.vue')
=======
>>>>>>> upstream/main
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