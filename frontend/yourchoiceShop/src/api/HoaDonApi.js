import axios from 'axios'
import request from '@/services/request'; // Hoặc đường dẫn tới file axios instance của bạn
const API_URL = 'http://localhost:8080/api/admin/orders'

// export const fetchOrders = (payload) => {
//   return axios.post(`${API_URL}/search`, payload)
// }
export const fetchOrders = (params) => {
    // Backend Controller: @GetMapping("/api/v1/hoa-don")
    return request.get('/hoa-don', { params });
};
export const createOrder = (data) => {
  return request.post('/hoa-don/create', data);
}
export const exportOrders = (params) => {
  return request.get('/hoa-don/export', {
    params,
    responseType: 'blob' // <--- BẮT BUỘC PHẢI CÓ
  });
}