import { createRouter, createWebHistory } from "vue-router"
import { isAuthenticated, getRole, getCurrentUserId, getCurrentUser } from "@/services/auth"
import request from "@/services/request"
import { useShiftStore } from '@/stores/shiftStore';
// import { initCrossTabSync } from "@/services/auth" // Đã vô hiệu hóa cross-tab sync

/* ================= STATIC IMPORT ================= */
import CoAoIndex from "../views/admin/attribute/CoAoIndex.vue"
import TayAoIndex from "../views/admin/attribute/TayAoIndex.vue"
import ChatLieuIndex from "../views/admin/attribute/ChatLieuIndex.vue"
import XuatXuIndex from "../views/admin/attribute/XuatXuIndex.vue"
import ThuongHieuIndex from "../views/admin/attribute/ThuongHieuIndex.vue"

import CustomerCreate from "@/views/admin/customer/CustomerCreate.vue"
import CustomerDetail from "@/views/admin/customer/CustomerDetail.vue"

import ThongKeView from "@/views/admin/dashboard/ThongKeView.vue"
import ShiftTracking from "@/views/admin/employee/schedule/ShiftTracking.vue";

import Swal from 'sweetalert2';

/* ================= ROLE HELPER ================= */
const getUserRole = () => {
  const directRole = sessionStorage.getItem("userRole")
  if (directRole) return String(directRole).toUpperCase()

  const rawUser = sessionStorage.getItem("user")
  if (!rawUser) return null

  try {
    const user = JSON.parse(rawUser)
    const role = user?.role || user?.quyenHan?.maQuyen || user?.quyenHan?.tenQuyen
    return role ? String(role).toUpperCase() : null
  } catch {
    return null
  }
}

const normalizeRole = (role) => {
  const value = String(role || "").toUpperCase()
  if (value === "EMPLOYEE" || value === "NHANVIEN" || value === "NHAN_VIEN") return "STAFF"
  return value
}

const getDefaultPathByRole = (role) => {
  const normalized = normalizeRole(role)
  if (normalized === "CUSTOMER") {
    const currentUserId = getCurrentUserId()
    return currentUserId ? `/customer/${currentUserId}/account` : "/"
  }
  if (normalized === "STAFF") return "/staff/pos"
  return "/admin/dashboard"
}

/* ================= ROUTER ================= */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior() {
    return { top: 0 }
  },
  routes: [
    /* ================= LOGIN ================= */
    {
      path: "/login",
      redirect: "/client/login",
    },
    {
      path: "/admin/login",
      name: "admin-login",
      component: () => import("../views/LoginView.vue"),
    },

    /* ================= REGISTER ================= */
    {
      path: "/register",
      name: "register",
      component: () => import("../views/client/RegisterView.vue"),
    },

    /* ================= CLIENT ================= */
    {
      path: "/",
      component: () => import("../layouts/ClientLayout.vue"),
      children: [
        { path: "", name: "home", component: () => import("../views/client/HomeView.vue") },
        { path: "client/login", name: "client-login", component: () => import("../views/client/ClientLoginView.vue") },
        { path: "client/register", name: "client-register", component: () => import("../views/client/ClientRegisterView.vue") },
        { path: "products", name: "products", component: () => import("../views/client/ProductView.vue") },
        { path: "product/:id", name: "product-detail", component: () => import("../views/client/ProductDetailView.vue") },
        { path: "coupons", name: "coupons", component: () => import("../views/client/CouponView.vue") },
        { path: "news", name: "news", component: () => import("../views/client/NewsView.vue") },
        { path: "contact", name: "contact", component: () => import("../views/client/ContactView.vue") },
        { path: "cart", name: "cart", component: () => import("../views/client/CartView.vue") },
        { path: "checkout", name: "checkout", component: () => import("../views/client/CheckoutView.vue") },
        { path: "vnpay-return", name: "vnpay-return", component: () => import("../views/client/VnPayReturnView.vue") },
        { path: "order-tracking", name: "order-tracking", component: () => import("../views/client/OrderTrackingView.vue") },
        {
          path: "customer/:id/orders",
          name: "orders",
          component: () => import("../views/client/OrderHistoryView.vue"),
          meta: { requiresAuth: true, roles: ["CUSTOMER"], customerOwned: true },
        },
        {
          path: "customer/:id/account",
          name: "account",
          component: () => import("../views/client/AccountView.vue"),
          meta: { requiresAuth: true, roles: ["CUSTOMER"], customerOwned: true },
        },
        {
          path: "orders",
          redirect: () => {
            const currentUserId = getCurrentUserId()
            return currentUserId ? `/customer/${currentUserId}/orders` : "/client/login"
          },
        },
        {
          path: "account",
          redirect: () => {
            const currentUserId = getCurrentUserId()
            return currentUserId ? `/customer/${currentUserId}/account` : "/client/login"
          },
        },
      ],
    },

    /* ================= ADMIN ================= */
    {
      path: "/admin",
      component: () => import("../layouts/AdminLayout.vue"),
      meta: { requiresAuth: true, roles: ["ADMIN"] },
      children: [
        { path: "", redirect: "/admin/home" },

        /* Trang chủ Admin */
        { path: "home", name: "admin-home", component: () => import("../views/admin/AdminHome.vue") },

        /* Dashboard */
        { path: "dashboard", name: "admin-dashboard", component: ThongKeView },

        /* Customers */
        { path: "customers", name: "admin-customer-list", component: () => import("../views/admin/customer/CustomerList.vue") },
        { path: "customers/create", name: "admin-customer-create", component: CustomerCreate },
        { path: "customers/detail/:id", name: "admin-customer-detail", component: CustomerDetail },

        /* Employees */
        { path: "employees", name: "admin-employee-list", component: () => import("../views/admin/employee/EmployeeList.vue") },
        { path: "employees/create", name: "admin-employee-create", component: () => import("../views/admin/employee/AddEmployee.vue") },
        { path: "employees/edit/:id", name: "admin-employee-edit", component: () => import("../views/admin/employee/EditEmployee.vue") },

        /* Shifts & Schedule */
        { path: "shifts", name: "admin-shift-list", component: () => import("../views/admin/employee/schedule/ShiftList.vue") },
        { path: "shifts/create", name: "admin-shift-create", component: () => import("../views/admin/employee/schedule/ShiftCreate.vue") },
        { path: "shifts/edit/:id", name: "admin-shift-edit", component: () => import("../views/admin/employee/schedule/ShiftEdit.vue") },
        { path: "schedules", name: "admin-schedule", component: () => import("../views/admin/employee/schedule/ScheduleManager.vue") },
        { path: "history-activity", name: "admin-history-activity", component: () => import("../views/admin/employee/schedule/HistoryActivity.vue") },

        /* Products */
        { path: "products", name: "admin-product-list", component: () => import("../views/admin/product/ProductIndex.vue") },
        { path: "products/variants", name: "admin-all-variants", component: () => import("../views/admin/product/AllVariantsView.vue") },
        { path: "products/create", name: "admin-product-create", component: () => import("../views/admin/product/ProductCreate.vue") },
        { path: "products/:id", name: "admin-product-detail", component: () => import("../views/admin/product/ProductDetail.vue") },

        /* Attributes */
        { path: "mau-sac", name: "mau-sac", component: () => import("../views/admin/attribute/MauSac.vue") },
        { path: "kich-thuoc", name: "kich-thuoc", component: () => import("../views/admin/attribute/KichThuoc.vue") },
        { path: "co-ao", name: "co-ao", component: CoAoIndex },
        { path: "tay-ao", name: "tay-ao", component: TayAoIndex },
        { path: "chat-lieu", name: "chat-lieu", component: ChatLieuIndex },
        { path: "xuat-xu", name: "xuat-xu", component: XuatXuIndex },
        { path: "thuong-hieu", name: "thuong-hieu", component: ThuongHieuIndex },

        /* Orders (theo File 1) */
        { path: "orders", name: "admin-order-list", component: () => import("../views/admin/DonHang/QuanLyHoaDon.vue") },
        { path: "orders/:id", name: "admin-order-detail", component: () => import("../views/admin/DonHang/ChiTietHoaDon.vue") },

        /* Voucher */
        { path: "vouchers", name: "admin-voucher-list", component: () => import("../views/admin/voucher/VoucherIndex.vue") },
        { path: "vouchers/create", name: "admin-voucher-create", component: () => import("../views/admin/voucher/VoucherCreate.vue") },
        { path: "vouchers/edit/:id", name: "admin-voucher-edit", component: () => import("../views/admin/voucher/VoucherEdit.vue") },

        /* POS */
        { path: "pos", name: "admin-pos", component: () => import("../views/admin/pos/BanHangTaiQuay.vue"), meta: { layout: "full" } },

        /* Sales */
        { path: "sales", name: "admin-sale-list", component: () => import("../views/admin/promotion/SaleIndex.vue") },
        { path: "sales/create", name: "admin-sale-create", component: () => import("../views/admin/promotion/SaleCreate.vue") },
        // ĐÂY LÀ PHẦN TAO TÍCH HỢP THÊM VÀO THEO Ý MÀY:
        { path: "sale/edit/:id", name: "admin-sale-edit", component: () => import("@/views/admin/promotion/EditDotGiamGia.vue") },

        /* Thống kê riêng */
        { path: "thong-ke", name: "admin-thong-ke", component: ThongKeView },

        /* Thông tin cá nhân */
        { path: "thong-tin-ca-nhan", name: "admin-profile", component: () => import("../views/admin/employee/ProfileView.vue") },

        /* Chat Management */
        { path: "chat", name: "admin-chat", component: () => import("../views/admin/chat/ChatManagement.vue") },
      ],
    },

    /* ================= STAFF ================= */
    {
      path: "/staff",
      component: () => import("../layouts/AdminLayout.vue"),
      meta: { requiresAuth: true, roles: ["STAFF"] },
      children: [
        { path: "", redirect: "/staff/pos" },

        { 
          path: "pos", 
          name: "staff-pos", 
          component: () => import("../views/admin/pos/PosWrapper.vue"), 
          meta: { requiresShift: true },
          beforeEnter: async (to, from, next) => {
          const user = getCurrentUser();

          if (!user || !user.tenTaiKhoan) {
            next('/login');
            return;
          }

          const username = user.tenTaiKhoan;

          if (!username) {
            console.error("User không hợp lệ:", user);
            next('/login');
            return;
          }

          try {
            const res = await request.get(`/giao-ca/hien-tai?username=${username}`);

            if (res.data && res.data.id) {
              sessionStorage.setItem('hasActiveShift', 'true');
              next();
            } else {
              sessionStorage.setItem('hasActiveShift', 'false');
              next('/staff/giao-ca');
            }
          } catch (error) {
            console.error("Lỗi kiểm tra ca làm việc:", error);
            sessionStorage.setItem('hasActiveShift', 'false');
            next('/staff/giao-ca');
          }
        }
        },

        /* --- THÊM ROUTE GIAO CA VÀO ĐÂY --- */
        { 
          path: "giao-ca", 
          name: "staff-shift-tracking", 
          component: () => import("../views/admin/employee/schedule/ShiftTracking.vue") 
        },

        /* Orders (theo File 1) */
        { 
          path: "orders", 
          name: "staff-order-list", 
          component: () => import("../views/admin/DonHang/QuanLyHoaDon.vue"),
          meta: { requiresShift: true }
        },
        { 
          path: "orders/:id", 
          name: "staff-order-detail", 
          component: () => import("../views/admin/DonHang/ChiTietHoaDon.vue"),
          meta: { requiresShift: true }
        },
        
        /* Customers */
        { path: "customers", name: "staff-customer-list", component: () => import("../views/admin/customer/CustomerList.vue") },
        { path: "customers/create", name: "staff-customer-create", component: CustomerCreate },
        { path: "customers/detail/:id", name: "staff-customer-detail", component: CustomerDetail },

        /* Thông tin cá nhân */
        { path: "thong-tin-ca-nhan", name: "staff-profile", component: () => import("../views/admin/employee/ProfileView.vue") },

        /* Chat Management */
        { path: "chat", name: "staff-chat", component: () => import("../views/admin/chat/ChatManagement.vue") },
      ],
    },
  ],
})

/* ================= NAVIGATION GUARD CẢI TIẾN ================= */
router.beforeEach(async (to, from, next) => {
  const normalizeRole = (value) => {
    const role = String(value || '').toUpperCase();
    if (role === 'EMPLOYEE' || role === 'NHANVIEN' || role === 'NHAN_VIEN') return 'STAFF';
    return role;
  };

  const rawRole = getRole();
  const role = normalizeRole(rawRole);
  const path = to.path;

  // 1. CHƯA ĐĂNG NHẬP: Đá về trang login
  if (!role) {
    // NẾU ĐƯỜNG DẪN ĐÃ LÀ TRANG LOGIN RỒI THÌ CHO QUA LUÔN (Chống lặp vô hạn)
    if (path === '/admin/login') {
      return next(); 
    }
    
    // Nếu chưa đăng nhập mà cố lân la vào các trang nội bộ thì mới đá về login
    if (path.startsWith('/admin') || path.startsWith('/staff')) {
      return next('/admin/login');
    }

    return
  }
  // ================================================================

  // --- LOGIC CA LÀM VIỆC ---
  const requiresShift = to.meta.requiresShift || 
    ['staff-order-list', 'staff-order-detail', 
     'staff-customer-list', 'staff-customer-create', 'staff-customer-detail']
    .includes(to.name);
  
  if (requiresShift && !hasActiveShift && role === 'STAFF') {
    Swal.fire({
      icon: 'warning',
      title: 'Yêu cầu mở ca!',
      text: 'Bạn phải mở ca làm việc mới có thể sử dụng chức năng này.',
      confirmButtonText: 'Đi tới màn trực ca'
    })
    next({ name: 'staff-shift-tracking' })
    return
  }

  // 2. CHẶN VƯỢT QUYỀN CHÉO (Admin <-> Staff)
  if (role === 'STAFF' && path.startsWith('/admin')) {
    return next('/staff/giao-ca'); 
  }
  if (role === 'ADMIN' && path.startsWith('/staff')) {
    return next('/admin/home'); 
  }

  // 3. LUẬT RIÊNG CHO NHÂN VIÊN (STAFF)
  if (role === 'STAFF') {
    // Lưu ý: Phải gọi useShiftStore() ở BÊN TRONG beforeEach để tránh lỗi Pinia chưa khởi tạo
    const shiftStore = useShiftStore(); 
    
    // Nếu ứng dụng vừa F5 (reload), store có thể bị reset, ta cần chắc chắn store đã fetch trạng thái ca
    if (shiftStore.hasActiveShift === null || shiftStore.hasActiveShift === undefined) {
      await shiftStore.fetchShift(); 
    }

    const hasShift = shiftStore.hasActiveShift;

    // Danh sách các trang nhân viên ĐƯỢC PHÉP VÀO khi KHÔNG CÓ CA
    const allowedRoutesWithoutShift = [
      '/staff/giao-ca', 
      '/staff/thong-tin-ca-nhan' // Cho phép xem thông tin cá nhân
    ];

    // Nếu KHÔNG có ca VÀ đang cố vào một trang KHÔNG nằm trong danh sách cho phép
    if (!hasShift && !allowedRoutesWithoutShift.includes(path)) {
      // Đá thẳng về trang giao ca
      return next('/staff/giao-ca');
    }
  }

  // 4. Mọi thứ hợp lệ, cho phép đi tiếp
  next();
  console.log("Role thực tế đang chạy là:", role);
});

/* ================= CROSS-TAB SYNC ================= */
// VÔ HIỆU HÓA đồng bộ tab - mỗi tab hoạt động độc lập
// if (initCrossTabSync) {
//   initCrossTabSync(router)
// }

export default router