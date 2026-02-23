// File: src/api/ChiTietHoaDon.js

// 1. Sửa lại đường dẫn import cho đúng với file HoaDonApi.js
import request from '@/services/request'; 

// 2. Hàm lấy chi tiết
export function fetchOrderDetail(id) {
  return request({
    url: `/hoa-don/${id}`,
    method: 'get'
  })
}

// 3. Hàm cập nhật trạng thái (Gọi API bạn vừa viết bên Java)
export function updateOrderStatus(maHoaDon, newStatus) {
  return request({
    url: `/hoa-don/${maHoaDon}/status`,
    method: 'put',
    params: { newStatus } // Axios sẽ tự chuyển thành ?newStatus=...
  })
}
export function updateOrderInfo(maHoaDon, infoData) {
  return request({
    url: `/hoa-don/${maHoaDon}/info`,
    method: 'put',
    data: infoData // Gửi cục dữ liệu (Tên, SĐT, Địa chỉ) lên
  })
}