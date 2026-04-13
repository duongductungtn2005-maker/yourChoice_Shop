/**
 * Client-side API service
 * API calls cho phần bán hàng Online (khách hàng)
 */
import axios from 'axios'
import request from '@/services/request'

const API_URL = 'http://localhost:8080/api/v1'

// ===================== SẢN PHẨM =====================

/** Lấy danh sách sản phẩm (có phân trang, lọc, sắp xếp) */
export function getProducts(params) {
  return axios.get(`${API_URL}/products`, { params })
}

/** Lấy chi tiết sản phẩm */
export function getProductDetail(id) {
  return axios.get(`${API_URL}/products/${id}`)
}

/** Lấy danh sách biến thể của sản phẩm */
export function getProductVariants(id) {
  return axios.get(`${API_URL}/products/${id}/variants`)
}

/** Lấy danh sách chi tiết sản phẩm (có filter) */
export function getChiTietSanPham(params) {
  return axios.get(`${API_URL}/chi-tiet-san-pham`, { params })
}

// ===================== THUỘC TÍNH =====================

export function getThuongHieu(params) {
  return axios.get(`${API_URL}/thuong-hieu`, { params })
}

export function getChatLieu(params) {
  return axios.get(`${API_URL}/chat-lieu`, { params })
}

export function getMauSac(params) {
  return axios.get(`${API_URL}/mau-sac`, { params })
}

export function getKichThuoc(params) {
  return axios.get(`${API_URL}/kich-thuoc`, { params })
}

// ===================== KHÁCH HÀNG (Auth required) =====================

/** Đăng nhập khách hàng */
export function loginCustomer(username, password) {
  return axios.post(`${API_URL}/khach-hang/authenticate`, {
    username, password
  })
}

/** Đăng ký khách hàng mới (client tự đăng ký) */
export function registerCustomer(data) {
  return axios.post(`${API_URL}/khach-hang/register`, data)
}

/** Đăng ký khách hàng mới (admin tạo, multipart) */
export function createCustomer(formData) {
  return axios.post(`${API_URL}/khach-hang`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** Lấy thông tin khách hàng */
export function getCustomerDetail(id) {
  return request.get(`/khach-hang/${id}`)
}

/** Cập nhật thông tin khách hàng */
export function updateCustomer(id, formData) {
  return request.put(`/khach-hang/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** Kiểm tra username tồn tại */
export function checkUsernameExists(username, excludeId) {
  return axios.get(`${API_URL}/khach-hang/exists-username`, {
    params: { username, excludeId }
  })
}

/** Kiểm tra số điện thoại tồn tại */
export function checkPhoneExists(soDienThoai) {
  return axios.get(`${API_URL}/khach-hang/exists-sdt`, {
    params: { soDienThoai }
  })
}

// ===================== ĐỊA CHỈ =====================

export function getAddresses(khachHangId) {
  return request.get('/dia-chi', { params: { khachHangId } })
}

export function createAddress(data) {
  return request.post('/dia-chi', data)
}

export function updateAddress(id, data) {
  return request.put(`/dia-chi/${id}`, data)
}

export function deleteAddress(id) {
  return request.delete(`/dia-chi/${id}`)
}

export function setDefaultAddress(id) {
  return request.put(`/dia-chi/${id}/set-default`)
}

// ===================== ĐƠN HÀNG =====================

/** Tạo đơn hàng online (delivery) */
export function createOrderOnline(data) {
  return request.post('/hoa-don/delivery', data)
}

/** Lấy danh sách đơn hàng */
export function getOrders(params) {
  return request.get('/hoa-don', { params })
}

/** Lấy chi tiết đơn hàng */
export function getOrderDetail(maHoaDon) {
  return request.get(`/hoa-don/${maHoaDon}`)
}

/** Cập nhật trạng thái đơn hàng (hủy đơn) */
export function updateOrderStatus(maHoaDon, newStatus) {
  return request.put(`/hoa-don/${maHoaDon}/status`, null, {
    params: { newStatus }
  })
}

/** Tra cứu đơn hàng theo SĐT (khách vãng lai, không cần đăng nhập) */
export function trackOrderByPhone(params) {
  return axios.get(`${API_URL}/hoa-don/tra-cuu`, { params })
}

// ===================== PHIẾU GIẢM GIÁ =====================

export function getVouchers(params) {
  return axios.get(`${API_URL}/phieu-giam-gia`, { params })
}

// ===================== GHN (Địa chỉ giao hàng) =====================

const ghnRequest = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
})

export function getProvinces() {
  return ghnRequest.get('/api/admin/ghn/tinh-thanh')
}

export function getDistricts(provinceId) {
  return ghnRequest.get(`/api/admin/ghn/quan-huyen/${provinceId}`)
}

export function getWards(districtId) {
  return ghnRequest.get(`/api/admin/ghn/phuong-xa/${districtId}`)
}

export function calculateShippingFee(data) {
  return ghnRequest.post('/api/admin/ghn/tinh-phi-van-chuyen', data)
}
