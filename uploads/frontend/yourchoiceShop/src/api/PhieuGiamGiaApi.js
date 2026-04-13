import axios from 'axios'

const API_URL = 'http://localhost:8080/api/v1/phieu-giam-gia'

export const getPhieuGiamGia = (params) => {
  return axios.get(API_URL, { params })
}