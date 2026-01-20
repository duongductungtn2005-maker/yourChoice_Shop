import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 1. Route Đăng nhập (Không có layout admin)
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },

    // 2. Route Admin (Sử dụng AdminLayout)
    {
      path: '/admin',
      component: () => import('../layouts/AdminLayout.vue'),
      meta: { requiresAuth: true }, // Sau này dùng để check quyền
      children: [
        {
          path: '', // Mặc định vào dashboard
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
          meta: { layout: 'full' } // Có thể ẩn sidebar nếu cần không gian rộng
        },
        // --- Quản lý Khách hàng ---
        {
          path: 'customers',
          name: 'admin-customer-list',
          component: () => import('../views/admin/customer/CustomerIndex.vue')
        },
        // --- Quản lý Nhân viên ---
        {
          path: 'employees',
          name: 'admin-employee-list',
          // Sửa đường dẫn trỏ về file EmployeeList.vue
          component: () => import('../views/admin/employee/EmployeeList.vue')
        },
        {
          // Thêm route cho trang Thêm nhân viên
          path: 'employees/create', 
          name: 'admin-employee-create',
          // Trỏ về file AddEmployee.vue
          component: () => import('../views/admin/employee/AddEmployee.vue')
        },
        // --- Quản lý Voucher (Phiếu giảm giá) ---
        {
          path: 'vouchers',
          name: 'admin-voucher-list',
          component: () => import('../views/admin/voucher/VoucherIndex.vue')
        },
        // --- Quản lý Đợt giảm giá (Promotion) ---
        {
          path: 'sales',
          name: 'admin-sale-list',
          component: () => import('../views/admin/promotion/SaleIndex.vue')
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