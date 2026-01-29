import axios from 'axios'

const API_URL = 'http://localhost:8080/admin/orders'

export const fetchOrders = (payload) => {
  return axios.post(API_URL, payload, {
    headers: {
      'Content-Type': 'application/json'
    }
  })
}
