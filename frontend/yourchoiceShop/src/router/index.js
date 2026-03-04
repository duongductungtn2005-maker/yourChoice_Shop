import { createRouter, createWebHistory } from "vue-router"
import { isAuthenticated, getRole, initCrossTabSync } from "@/services/auth"

/* ================= STATIC IMPORT ================= */
import CoAoIndex from "../views/admin/attribute/CoAoIndex.vue"
import TayAoIndex from "../views/admin/attribute/TayAoIndex.vue"
import ChatLieuIndex from "../views/admin/attribute/ChatLieuIndex.vue"
import XuatXuIndex from "../views/admin/attribute/XuatXuIndex.vue"
import ThuongHieuIndex from "../views/admin/attribute/ThuongHieuIndex.vue"

import CustomerCreate from "@/views/admin/customer/CustomerCreate.vue"
import CustomerDetail from "@/views/admin/customer/CustomerDetail.vue"

import ThongKeView from "@/views/admin/dashboard/ThongKeView.vue"

/* ================= ROLE HELPER ================= */
const getUserRole = () => {
  const directRole = localStorage.getItem("userRole")
  if (directRole) return String(directRole).toUpperCase()

  const rawUser = localStorage.getItem("user")
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
  if (normalized === "CUSTOMER") return "/"
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
      name: "login",
      component: () => import("../views/LoginView.vue"),
    },

    /* ================= CLIENT ================= */
    {
      path: "/",
      component: () => import("../layouts/ClientLayout.vue"),
      children: [
        { path: "", name: "home", component: () => import("../views/client/HomeView.vue") },
        { path: "products", name: "products", component: () => import("../views/client/ProductView.vue") },
        { path: "product/:id", name: "product-detail", component: () => import("../views/client/ProductDetailView.vue") },
        { path: "coupons", name: "coupons", component: () => import("../views/client/CouponView.vue") },
        { path: "news", name: "news", component: () => import("../views/client/NewsView.vue") },
        { path: "contact", name: "contact", component: () => import("../views/client/ContactView.vue") },
      ],
    },

    /* ================= ADMIN ================= */
    {
      path: "/admin",
      component: () => import("../layouts/AdminLayout.vue"),
      meta: { requiresAuth: true, roles: ["ADMIN"] },
      children: [
        { path: "", redirect: "/admin/dashboard" },

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
        { path: "shifts", name: "admin-shift", component: () => import("../views/admin/employee/schedule/ShiftList.vue") },
        { path: "shifts/create", name: "admin-shift-create", component: () => import("../views/admin/employee/schedule/ShiftCreate.vue") },
        { path: "shifts/edit/:id", name: "admin-shift-edit", component: () => import("../views/admin/employee/schedule/ShiftEdit.vue") },
        { path: "schedules", name: "admin-schedule", component: () => import("../views/admin/employee/schedule/ScheduleManager.vue") },
        { path: "history-activity", name: "admin-history-activity", component: () => import("../views/admin/employee/schedule/HistoryActivity.vue") },

        /* Products */
        { path: "products", name: "admin-product-list", component: () => import("../views/admin/product/ProductIndex.vue") },
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

        /* POS */
        { path: "pos", name: "admin-pos", component: () => import("../views/admin/pos/BanHangTaiQuay.vue"), meta: { layout: "full" } },

        /* Sales */
        { path: "sales", name: "admin-sale-list", component: () => import("../views/admin/promotion/SaleIndex.vue") },
        { path: "sales/create", name: "admin-sale-create", component: () => import("../views/admin/promotion/SaleCreate.vue") },

        /* Thống kê riêng */
        { path: "thong-ke", name: "admin-thong-ke", component: ThongKeView },
      ],
    },

    /* ================= STAFF ================= */
    {
      path: "/staff",
      component: () => import("../layouts/AdminLayout.vue"),
      meta: { requiresAuth: true, roles: ["STAFF"] },
      children: [
        { path: "", redirect: "/staff/pos" },

        { path: "pos", name: "staff-pos", component: () => import("../views/admin/pos/BanHangTaiQuay.vue"), meta: { layout: "full" } },

        /* Orders (theo File 1) */
        { path: "orders", name: "staff-order-list", component: () => import("../views/admin/DonHang/QuanLyHoaDon.vue") },
        { path: "orders/:id", name: "staff-order-detail", component: () => import("../views/admin/DonHang/ChiTietHoadon.vue") },

        /* Customers */
        { path: "customers", name: "staff-customer-list", component: () => import("../views/admin/customer/CustomerList.vue") },
        { path: "customers/create", name: "staff-customer-create", component: CustomerCreate },
        { path: "customers/detail/:id", name: "staff-customer-detail", component: CustomerDetail },
      ],
    },
  ],
})

/* ================= NAVIGATION GUARD ================= */
router.beforeEach((to, from, next) => {
  // Ưu tiên service auth, fallback qua localStorage
  const authenticated = isAuthenticated ? isAuthenticated() : !!localStorage.getItem("token")
  const roleFromService = authenticated && getRole ? getRole() : null
  const role = normalizeRole(roleFromService || getUserRole())

  const requiresAuth = to.matched.some((r) => r.meta.requiresAuth)

  // Đã đăng nhập mà vào /login → chuyển về trang mặc định theo role
  if (to.path === "/login" && authenticated && role) {
    next(getDefaultPathByRole(role))
    return
  }

  // Route không yêu cầu auth → cho qua
  if (!requiresAuth) {
    next()
    return
  }

  // Chưa đăng nhập hoặc không có role → về login
  if (!authenticated || !role) {
    next("/login")
    return
  }

  // Kiểm tra role
  const allowedRoles = to.matched
    .flatMap((r) => (r.meta?.roles ? r.meta.roles : []))
    .map((r) => normalizeRole(r))

  if (allowedRoles.length && !allowedRoles.includes(role)) {
    next(getDefaultPathByRole(role))
    return
  }

  next()
})

/* ================= CROSS-TAB SYNC ================= */
if (initCrossTabSync) {
  initCrossTabSync(router)
}

export default router