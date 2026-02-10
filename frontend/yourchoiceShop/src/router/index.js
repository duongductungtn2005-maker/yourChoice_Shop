import { createRouter, createWebHistory } from 'vue-router'

// --- IMPORT CÁC MÀN HÌNH (Static Import) ---
// Đảm bảo các file này tồn tại trong folder attribute, nếu chưa có hãy comment lại
import CoAoIndex from '../views/admin/attribute/CoAoIndex.vue'
import TayAoIndex from '../views/admin/attribute/TayAoIndex.vue'
import ChatLieuIndex from '../views/admin/attribute/ChatLieuIndex.vue'
import XuatXuIndex from '../views/admin/attribute/XuatXuIndex.vue'
import ThuongHieuIndex from '../views/admin/attribute/ThuongHieuIndex.vue'
import CustomerCreate from '@/views/admin/customer/CustomerCreate.vue';
import CustomerDetail from '@/views/admin/customer/CustomerDetail.vue';
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ==========================================
    // 1. ROUTE ĐĂNG NHẬP
    // ==========================================
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/',
      component: () => import('../layouts/ClientLayout.vue'),
      children: [
        {
          path: '', // Đường dẫn gốc (Trang chủ)
          name: 'home',
          component: () => import('../views/client/HomeView.vue')
        },
        // Sau này bạn sẽ thêm các trang Shop, Giỏ hàng ở đây
        // { path: 'shop', component: ... },
        // { path: 'cart', component: ... },
      ]
    },

    // ==========================================
    // 2. KHU VỰC ADMIN
    // ==========================================
    {
      path: '/admin',
      component: () => import('../layouts/AdminLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        // --- Dashboard ---
        {
          path: 'dashboard',
          name: 'admin-dashboard',
          // SỬA: Thêm folder /dashboard/ vào đường dẫn cho đúng hình ảnh
          component: () => import('../views/admin/dashboard/Dashboard.vue')
        },
        { 
            path: '', 
            redirect: '/admin/dashboard' 
        },

        // --- QUẢN LÝ KHÁCH HÀNG (Đã sửa tên file) ---
        {
          path: 'customers', // Đường dẫn: /admin/customers
          name: 'admin-customer-list',
          component: () => import('../views/admin/customer/CustomerList.vue')
        },
        {
          path: 'customers/create', // Đường dẫn: /admin/customers/create
          name: 'admin-customer-create',
          component: CustomerCreate,
          meta: { title: 'Thêm khách hàng' }
        },
        {
          path: 'customers/detail/:id', // Đường dẫn nhận ID
          name: 'admin-customer-detail',
          component: CustomerDetail,
          meta: { title: 'Chi tiết khách hàng' }
        },
        // --- QUẢN LÝ NHÂN VIÊN ---
        {
          path: 'employees',
          name: 'admin-employee-list',
          // Kiểm tra đúng tên file: EmployeeList.vue
          component: () => import('../views/admin/employee/EmployeeList.vue')
        },
        {
          path: 'employees/create', 
          name: 'admin-employee-create',
          // Kiểm tra đúng tên file: AddEmployee.vue
          component: () => import('../views/admin/employee/AddEmployee.vue')
        },

        // --- BÁN HÀNG TẠI QUẦY (POS) ---
        {
          path: 'pos',
          name: 'admin-pos',
          component: () => import('../views/admin/pos/CounterSales.vue'),
          meta: { layout: 'full' } 
        },

        // --- QUẢN LÝ SẢN PHẨM ---
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

        // --- QUẢN LÝ THUỘC TÍNH ---
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
          // Lưu ý folder DonHang viết hoa chữ D
          component: () => import('../views/admin/DonHang/QuanLyDonHang.vue')
        },
        {
          path: 'orders/:id',
          name: 'admin-order-detail',
          component: () => import('../views/admin/DonHang/ChiTietDonHang.vue')
        },

        // --- KHUYẾN MÃI (Voucher/Sales) ---
        // (Nếu chưa có file thì tạm thời comment lại để chạy được web)
        {
          path: 'vouchers',
          name: 'admin-voucher-list',
          component: () => import('../views/admin/voucher/VoucherIndex.vue')
        },
        {
          path: 'vouchers/create', // Đường dẫn con: /admin/vouchers/create
          name: 'admin-voucher-create',
          component: () => import('../views/admin/voucher/VoucherCreate.vue')
        },
        {
          path: 'pos',
          name: 'admin-pos',
          // SỬA DÒNG NÀY: Đổi CounterSales.vue thành BanHangTaiQuay.vue
          component: () => import('../views/admin/pos/BanHangTaiQuay.vue'), 
          meta: { layout: 'full' } 
        },
        {
          path: 'sales',
          name: 'admin-sale-list',
          component: () => import('../views/admin/promotion/SaleIndex.vue')
        },
        {
          path: 'sales/create', // Đường dẫn: /admin/sales/create
          name: 'admin-sale-create',
          // Đảm bảo bạn đã tạo file SaleCreate.vue tại đường dẫn này
          component: () => import('../views/admin/promotion/SaleCreate.vue')
        },
      ]
    },

    // ==========================================
    // 3. CATCH ALL
    // ==========================================
    { path: '/', redirect: '/login' },
    { path: '/:pathMatch(.*)*', redirect: '/login' }
  ]
})

export default router