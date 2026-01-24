import { fetchOrders } from './HoaDonApi'

export const fetchOrderDetail = async (maHoaDon) => {
  // 1️⃣ Lấy danh sách đơn
  const res = await fetchOrders()
  const order = res.data.content.find(o => o.maHoaDon === maHoaDon)

  if (!order) {
    throw new Error('Không tìm thấy đơn hàng')
  }

  // 2️⃣ Sinh dữ liệu chi tiết từ đơn gốc
  const isOnline = order.loaiHoaDon === 'Trực tuyến'

  return Promise.resolve({
    data: {
      maHoaDon: order.maHoaDon,
      tenKhachHang: order.tenKhachHang,
      loaiHoaDon: order.loaiHoaDon,
      trangThai: order.trangThai,
      ngayTao: order.ngayTao + ' 09:00',

      thongTinNhanHang: isOnline
        ? {
            tenNguoiNhan: order.tenKhachHang,
            sdt: '09xxxxxxxx',
            diaChi: 'Hà Nội'
          }
        : null,

      lichSuDonHang: [
        { hanhDong: 'Tạo đơn hàng', thoiGian: order.ngayTao + ' 09:00' },
        { hanhDong: 'Xử lý đơn', thoiGian: order.ngayTao + ' 09:15' }
      ],

      lichSuThanhToan: [
        {
          soTien: order.tongTienSauGiam,
          ngayThanhToan: order.ngayTao + ' 09:20',
          hinhThucThanhToan: isOnline ? 'Chuyển khoản' : 'Tiền mặt',
          loaiThanhToan: 'Thanh toán',
          trangThai: 'Thành công'
        }
      ],

      sanPhamHoaDon: [
        {
          tenSanPham: 'Áo sơ mi',
          size: 'L',
          mauSac: 'Trắng',
          soLuong: order.tongSanPham,
          donGia: Math.floor(order.tongTienSauGiam / order.tongSanPham),
          thanhTien: order.tongTienSauGiam
        }
      ],

      tongTien: order.tongTienSauGiam,
      giamGia: 0,
      phiVanChuyen: 0,
      tongTienSauGiam: order.tongTienSauGiam
    }
  })
}
