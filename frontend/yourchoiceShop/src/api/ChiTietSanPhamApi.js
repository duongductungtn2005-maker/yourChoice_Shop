import axios from 'axios'

const API_URL = 'http://localhost:8080/api/v1/chi-tiet-san-pham'

export const getChiTietSanPham = (params) => {
  return axios.get(API_URL, { params })
}