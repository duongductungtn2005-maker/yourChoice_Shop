import axios from 'axios'

const API_URL = 'http://localhost:8080/api/v1/nhan-vien'

export const getNhanVien = (params) => {
  return axios.get(API_URL, { params })
}

export const authenticateEmployee = (username, password) => {
  return axios.post(`${API_URL}/authenticate`, {
    username,
    password
  })
}
