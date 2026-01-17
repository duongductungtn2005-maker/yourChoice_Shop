// src/api/ChiTietHoaDon.js
import { fetchOrders } from './HoaDonApi'

// Tạo danh sách sản phẩm giả
const PRODUCT_POOL = [
  {
    name: 'Sơ mi Gà trống',
    size: 36,
    image: 'https://via.placeholder.com/80'
  },
  {
    name: 'Áo thun Basic',
    size: 38,
    image: 'https://via.placeholder.com/80'
  },
  {
    name: 'Quần jean xanh',
    size: 30,
    image: 'https://via.placeholder.com/80'
  },
  {
    name: 'Áo khoác bomber',
    size: 40,
    image: 'https://via.placeholder.com/80'
  }
]

// Sinh sản phẩm theo số lượng
const generateProducts = (order) => {
  const products = []
  let remain = order.tongSanPham
  let totalMoney = order.tongTienSauGiam

  let index = 0
  while (remain > 0) {
    const quantity = Math.min(1 + (remain % 2), remain)
    const price = Math.floor(totalMoney / order.tongSanPham)

    products.push({
      ...PRODUCT_POOL[index % PRODUCT_POOL.length],
      quantity,
      price
    })

    remain -= quantity
    index++
  }

  return products
}

// Sinh lịch sử thanh toán
const generatePayments = (order) => {
  return [
    {
      amount: order.tongTienSauGiam,
      time: `${order.ngayTao} 10:30`,
      type: 'Thanh toán',
      method: order.loaiHoaDon === 'ONLINE' ? 'Chuyển khoản' : 'Tiền mặt',
      status: 'Thành công',
      confirmBy: 'Admin'
    }
  ]
}

// API mock chi tiết hóa đơn
export const fetchOrderDetail = async (maHoaDon) => {
  const res = await fetchOrders()

  const order = res.data.content.find(
    (o) => o.maHoaDon === maHoaDon
  )

  if (!order) {
    throw new Error('Không tìm thấy đơn hàng')
  }

  return Promise.resolve({
    data: {
      ...order,
      payments: generatePayments(order),
      products: generateProducts(order)
    }
  })
}
