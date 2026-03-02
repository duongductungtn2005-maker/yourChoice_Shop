import axios from 'axios'

const API_URL = 'http://localhost:8080/api/v1/khach-hang'

export const getKhachHang = (params) => {
  return axios.get(API_URL, { params })
}