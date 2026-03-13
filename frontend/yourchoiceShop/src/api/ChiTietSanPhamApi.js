import axios from 'axios'

const API_URL = 'http://localhost:8080/api/v1/chi-tiet-san-pham'

export const getChiTietSanPham = (params) => {
  return axios.get(API_URL, { params })
}

export const reserveStock = (id, soLuong) => {
  return axios.post(`${API_URL}/${id}/reserve`, { soLuong })
}

export const releaseStock = (id, soLuong) => {
  return axios.post(`${API_URL}/${id}/release`, { soLuong })
}

export const getByMaCtsp = (maCtsp) => {
  return axios.get(`${API_URL}/by-ma`, { params: { maCtsp } })
}