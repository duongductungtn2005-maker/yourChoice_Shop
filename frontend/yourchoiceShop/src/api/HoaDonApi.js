import axios from 'axios'

const API_URL = 'http://localhost:8080/api/admin/orders'

// export const fetchOrders = (payload) => {
//   return axios.post(`${API_URL}/search`, payload)
// }
export const fetchOrders = async () => {
  return Promise.resolve({
    data: {
      content: [
        {
          maHoaDon: 'HD001',
          tongSanPham: 3,
          tongTienSauGiam: 450000,
          tenKhachHang: 'Nguyễn Văn A',
          ngayTao: '2024-10-01',
          loaiHoaDon: 'Trực tuyến',
          trangThai: 1
        },
        {
          maHoaDon: 'HD002',
          tongSanPham: 5,
          tongTienSauGiam: 820000,
          tenKhachHang: 'Trần Thị B',
          ngayTao: '2024-10-02',
          loaiHoaDon: 'Tại quầy',
          trangThai: 2
        },
        {
          maHoaDon: 'HD003',
          tongSanPham: 2,
          tongTienSauGiam: 300000,
          tenKhachHang: 'Lê Văn C',
          ngayTao: '2024-10-03',
          loaiHoaDon: 'Trực tuyến',
          trangThai: 3
        },
        {
          maHoaDon: 'HD004',
          tongSanPham: 6,
          tongTienSauGiam: 1250000,
          tenKhachHang: 'Phạm Thị D',
          ngayTao: '2024-10-03',
          loaiHoaDon: 'Tại quầy',
          trangThai: 4
        },
        {
          maHoaDon: 'HD005',
          tongSanPham: 1,
          tongTienSauGiam: 150000,
          tenKhachHang: 'Hoàng Văn E',
          ngayTao: '2024-10-04',
          loaiHoaDon: 'Trực tuyến',
          trangThai: 0
        },
        {
          maHoaDon: 'HD006',
          tongSanPham: 4,
          tongTienSauGiam: 670000,
          tenKhachHang: 'Đặng Thị F',
          ngayTao: '2024-10-05',
          loaiHoaDon: 'Trực tuyến',
          trangThai: 1
        },
        {
          maHoaDon: 'HD007',
          tongSanPham: 7,
          tongTienSauGiam: 1750000,
          tenKhachHang: 'Vũ Văn G',
          ngayTao: '2024-10-06',
          loaiHoaDon: 'Tại quầy',
          trangThai: 2
        },
        {
          maHoaDon: 'HD008',
          tongSanPham: 2,
          tongTienSauGiam: 280000,
          tenKhachHang: 'Bùi Thị H',
          ngayTao: '2024-10-07',
          loaiHoaDon: 'Tại quầy',
          trangThai: 3
        },
        {
          maHoaDon: 'HD009',
          tongSanPham: 9,
          tongTienSauGiam: 2100000,
          tenKhachHang: 'Ngô Văn I',
          ngayTao: '2024-10-07',
          loaiHoaDon: 'Tại quầy',
          trangThai: 4
        },
        {
          maHoaDon: 'HD010',
          tongSanPham: 3,
          tongTienSauGiam: 520000,
          tenKhachHang: 'Phan Thị K',
          ngayTao: '2024-10-08',
          loaiHoaDon: 'Trực tuyến',
          trangThai: 1
        },

        /* ====== THÊM 10 BẢN GHI ====== */

        {
          maHoaDon: 'HD011',
          tongSanPham: 5,
          tongTienSauGiam: 900000,
          tenKhachHang: 'Lý Văn L',
          ngayTao: '2024-10-09',
          loaiHoaDon: 'Tại quầy',
          trangThai: 2
        },
        {
          maHoaDon: 'HD012',
          tongSanPham: 2,
          tongTienSauGiam: 260000,
          tenKhachHang: 'Đỗ Thị M',
          ngayTao: '2024-10-09',
          loaiHoaDon: 'Trực tuyến',
          trangThai: 1
        },
        {
          maHoaDon: 'HD013',
          tongSanPham: 8,
          tongTienSauGiam: 1850000,
          tenKhachHang: 'Nguyễn Văn N',
          ngayTao: '2024-10-10',
          loaiHoaDon: 'Tại quầy',
          trangThai: 3
        },
        {
          maHoaDon: 'HD014',
          tongSanPham: 1,
          tongTienSauGiam: 120000,
          tenKhachHang: 'Trương Thị O',
          ngayTao: '2024-10-10',
          loaiHoaDon: 'Trực tuyến',
          trangThai: 0
        },
        {
          maHoaDon: 'HD015',
          tongSanPham: 6,
          tongTienSauGiam: 1350000,
          tenKhachHang: 'Phạm Văn P',
          ngayTao: '2024-10-11',
          loaiHoaDon: 'Tại quầy',
          trangThai: 4
        },
        {
          maHoaDon: 'HD016',
          tongSanPham: 4,
          tongTienSauGiam: 760000,
          tenKhachHang: 'Võ Thị Q',
          ngayTao: '2024-10-12',
          loaiHoaDon: 'Trực tuyến',
          trangThai: 1
        },
        {
          maHoaDon: 'HD017',
          tongSanPham: 3,
          tongTienSauGiam: 480000,
          tenKhachHang: 'Lê Văn R',
          ngayTao: '2024-10-12',
          loaiHoaDon: 'Tại quầy',
          trangThai: 2
        },
        {
          maHoaDon: 'HD018',
          tongSanPham: 9,
          tongTienSauGiam: 2250000,
          tenKhachHang: 'Nguyễn Thị S',
          ngayTao: '2024-10-13',
          loaiHoaDon: 'Trực tuyến',
          trangThai: 3
        },
        {
          maHoaDon: 'HD019',
          tongSanPham: 2,
          tongTienSauGiam: 310000,
          tenKhachHang: 'Trần Văn T',
          ngayTao: '2024-10-14',
          loaiHoaDon: 'Tại quầy',
          trangThai: 1
        },
        {
          maHoaDon: 'HD020',
          tongSanPham: 7,
          tongTienSauGiam: 1680000,
          tenKhachHang: 'Phan Thị U',
          ngayTao: '2024-10-15',
          loaiHoaDon: 'Trực tuyến',
          trangThai: 4
        }
      ],
      totalElements: 20
    }
  })
}
