import request from '@/services/request'

export const statisticApi = {
  getRevenue(data) {
    // Xóa chữ /api/v1 đi
    return request.post('/statistics/REVENUE', data)
  },
  getProductStats(data) {
    return request.post('/statistics/PRODUCT', data)
  },
  getOrderStatus(data) { return request.post('/statistics/ORDER_STATUS', data) },
  getLowStock() { return request.post('/statistics/LOW_STOCK', {}) },
  exportRevenueExcel(data) {
    return request.post('/statistics/REVENUE/export/excel', data, {
      responseType: 'blob' 
    })
  }
}