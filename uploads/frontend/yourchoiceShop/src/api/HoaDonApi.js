import axios from 'axios'
import request from '@/services/request'

export const fetchOrders = (params) => {
  return request.get('/hoa-don', { params })
}

export const createOrder = (data) => {
  // 🏪 TẠI QUẦY
  return request.post('/hoa-don/create', data)
}

export const createPosDraftOrder = (data = {}) => {
  return request.post('/hoa-don/pos/draft', data)
}

export const deletePosDraftOrder = (maHoaDon) => {
  return request.delete(`/hoa-don/pos/draft/${maHoaDon}`)
}

export const createOrderDelivery = (data) => {
  // 🚚 GIAO HÀNG – chỉ tạo đơn
  return request.post('/hoa-don/delivery', data)
}

export const exportOrders = (params) => {
  return request.get('/hoa-don/export', {
    params,
    responseType: 'blob'
  })
}