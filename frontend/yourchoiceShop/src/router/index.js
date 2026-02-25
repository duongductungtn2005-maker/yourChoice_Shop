import { createRouter, createWebHistory } from "vue-router";

// --- IMPORT CÁC MÀN HÌNH (Static Import) ---
import CoAoIndex from "../views/admin/attribute/CoAoIndex.vue";
import TayAoIndex from "../views/admin/attribute/TayAoIndex.vue";
import ChatLieuIndex from "../views/admin/attribute/ChatLieuIndex.vue";
import XuatXuIndex from "../views/admin/attribute/XuatXuIndex.vue";
import ThuongHieuIndex from "../views/admin/attribute/ThuongHieuIndex.vue";
import CustomerCreate from "@/views/admin/customer/CustomerCreate.vue";
import CustomerDetail from "@/views/admin/customer/CustomerDetail.vue";
// Đổi dòng import cũ thành dòng này:
import ThongKeView from '@/views/admin/dashboard/ThongKeView.vue'

// --- ROUTER CONFIGURATION ---
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior(to, from, savedPosition) {
    // Luôn cuộn lên đầu trang khi chuyển route
    return { top: 0 };
  },
  routes: [
    // ==========================================
    // 1. ROUTE ĐĂNG NHẬP
    // ==========================================
    {
      path: "/login",
      name: "login",
      component: () => import("../views/LoginView.vue"),
    },

    // ==========================================
    // 2. KHU VỰC CLIENT (BÁN HÀNG ONLINE)
    // ==========================================
    {
      path: "/",
      component: () => import("../layouts/ClientLayout.vue"),
      children: [
        {
          path: "", // Trang chủ
          name: "home",
          component: () => import("../views/client/HomeView.vue"),
        },
        {
          path: "products", // Danh sách sản phẩm
          name: "products",
          component: () => import("../views/client/ProductView.vue"),
        },
        {
          path: "product/:id", // Chi tiết sản phẩm
          name: "product-detail",
          component: () => import("../views/client/ProductDetailView.vue"),
        },
        {
          path: "coupons", // Kho Voucher
          name: "coupons",
          component: () => import("../views/client/CouponView.vue"),
        },
        {
          path: "news", // Tin tức
          name: "news",
          component: () => import("../views/client/NewsView.vue"),
        },
        {
          path: "contact", // Liên hệ
          name: "contact",
          component: () => import("../views/client/ContactView.vue"),
        },
      ],
    },

    // ==========================================
    // 3. KHU VỰC ADMIN
    // ==========================================
    {
      path: "/admin",
      component: () => import("../layouts/AdminLayout.vue"),
      meta: { requiresAuth: true }, // Cần đăng nhập
      children: [
        // --- Dashboard ---
        {
          path: "dashboard",
          name: "admin-dashboard",
          component: () => import("../views/admin/dashboard/ThongKeView.vue"),
        },
        {
          path: "",
          redirect: "/admin/dashboard",
        },

        // --- QUẢN LÝ KHÁCH HÀNG ---
        {
          path: "customers",
          name: "admin-customer-list",
          component: () => import("../views/admin/customer/CustomerList.vue"),
        },
        {
          path: "customers/create",
          name: "admin-customer-create",
          component: CustomerCreate,
          meta: { title: "Thêm khách hàng" },
        },
        {
          path: "customers/detail/:id",
          name: "admin-customer-detail",
          component: CustomerDetail,
          meta: { title: "Chi tiết khách hàng" },
        },

        // --- QUẢN LÝ NHÂN VIÊN ---
        {
          path: "employees",
          name: "admin-employee-list",
          component: () => import("../views/admin/employee/EmployeeList.vue"),
        },
        {
          path: "employees/create",
          name: "admin-employee-create",
          component: () => import("../views/admin/employee/AddEmployee.vue"),
        },
        {
          path: "employees/edit/:id",
          name: "admin-employee-edit",
          component: () => import("../views/admin/employee/EditEmployee.vue"),
        },

        // --- QUẢN LÝ SẢN PHẨM ---
        {
          path: "products",
          name: "admin-product-list",
          component: () => import("../views/admin/product/ProductIndex.vue"),
        },
        {
          path: "products/create",
          name: "admin-product-create",
          component: () => import("../views/admin/product/ProductCreate.vue"),
        },
        {
          path: "products/:id",
          name: "admin-product-detail",
          component: () => import("../views/admin/product/ProductDetail.vue"),
        },

        // --- QUẢN LÝ THUỘC TÍNH ---
        {
          path: "mau-sac",
          name: "mau-sac",
          component: () => import("../views/admin/attribute/MauSac.vue"),
        },
        {
          path: "kich-thuoc",
          name: "kich-thuoc",
          component: () => import("../views/admin/attribute/KichThuoc.vue"),
        },
        { path: "co-ao", name: "co-ao", component: CoAoIndex },
        { path: "tay-ao", name: "tay-ao", component: TayAoIndex },
        { path: "chat-lieu", name: "chat-lieu", component: ChatLieuIndex },
        { path: "xuat-xu", name: "xuat-xu", component: XuatXuIndex },
        {
          path: "thuong-hieu",
          name: "thuong-hieu",
          component: ThuongHieuIndex,
        },

        // --- QUẢN LÝ HÓA ĐƠN ---
        {
          path: "orders",
          name: "admin-order-list",
          component: () => import("../views/admin/DonHang/QuanLyDonHang.vue"),
        },
        {
          path: "orders/:id",
          name: "admin-order-detail",
          component: () => import("../views/admin/DonHang/ChiTietDonHang.vue"),
        },

        // --- KHUYẾN MÃI (Voucher/Sales) ---
        {
          path: "vouchers",
          name: "admin-voucher-list",
          component: () => import("../views/admin/voucher/VoucherIndex.vue"),
        },
        {
          path: "vouchers/create",
          name: "admin-voucher-create",
          component: () => import("../views/admin/voucher/VoucherCreate.vue"),
        },

        // --- BÁN HÀNG TẠI QUẦY (POS) ---
        {
          path: "pos",
          name: "admin-pos",
          component: () => import("../views/admin/pos/BanHangTaiQuay.vue"),
          meta: { layout: "full" },
        },

        // --- ĐỢT GIẢM GIÁ (SALE) ---
        {
          path: "sales",
          name: "admin-sale-list",
          component: () => import("../views/admin/promotion/SaleIndex.vue"),
        },
        {
          path: "sales/create",
          name: "admin-sale-create",
          component: () => import("../views/admin/promotion/SaleCreate.vue"),
        },
        {
          path: "thong-ke",
          name: "AdminThongKe",
          component: ThongKeView,
        },
        {
  path: '/admin/sale/edit/:id', // Bắt buộc phải có :id ở đây
  name: 'admin-sale-edit',
  component: () => import('@/views/admin/promotion/EditDotGiamGia.vue') // Trỏ đúng đường dẫn file của mày
},
{
    path: '/admin/giam-gia/phieu/sua/:id', // Đường dẫn có chứa tham số động :id
    name: 'admin-voucher-edit',            // Tên route phải khớp y xì đúc với tên trong hàm router.push()
    component: () => import('@/views/admin/voucher/VoucherEdit.vue'), // Import lazy-load (thay đường dẫn trỏ tới file Edit của mày)
    meta: {
      title: 'Chỉnh sửa phiếu giảm giá' // Tùy chọn: dùng để set title cho tab trình duyệt nếu dự án mày có setup
    }
  },
      ],
    },

    // ==========================================
    // 4. CATCH ALL (404)
    // ==========================================
    // { path: '/:pathMatch(.*)*', redirect: '/login' }
    // Tạm thời comment dòng này nếu bạn muốn test trang chủ mà chưa login
  ],
});

export default router;
