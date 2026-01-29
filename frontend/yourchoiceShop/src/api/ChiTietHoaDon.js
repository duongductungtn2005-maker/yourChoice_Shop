import axios from 'axios'

export function fetchOrderDetail(id) {
  return axios.get(`http://localhost:8080/admin/orders/${id}`)
}
