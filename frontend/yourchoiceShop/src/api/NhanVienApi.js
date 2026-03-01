import request from '@/services/request'

// Lấy danh sách nhân viên
export const getNhanVien = () => {
  return request({
    url: '/nhan-vien',
    method: 'get',
    params: {
      page: 0,
      size: 1000,
      status: 1   // chỉ lấy nhân viên đang hoạt động
    }
  })
}