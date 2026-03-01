import axios from 'axios'
import request from '@/services/request'

export const fetchOrders = (params) => {
  return request.get('/hoa-don', { params })
}

export const createOrder = (data) => {
  // 🏪 TẠI QUẦY
  return request.post('/hoa-don/create', data)
}

export const createOrderOnline = (data) => {
  // 🌐 ONLINE – chỉ tạo đơn
  return request.post('/hoa-don/online', data)
}

export const exportOrders = (params) => {
  return request.get('/hoa-don/export', {
    params,
    responseType: 'blob'
  })
}