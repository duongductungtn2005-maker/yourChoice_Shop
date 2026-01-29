package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.dto.request.DiaChiRequest;
import org.example.yourchoiceshop.entity.DiaChiKhachHang;
import java.util.List;

public interface DiaChiService {
    // Lấy danh sách theo ID Khách hàng
    List<DiaChiKhachHang> getAllByKhachHangId(Integer khachHangId);

    // Thêm mới
    DiaChiKhachHang create(DiaChiRequest request);

    // Cập nhật
    DiaChiKhachHang update(Integer id, DiaChiRequest request);

    // Xóa mềm
    void delete(Integer id);

    // Đặt làm địa chỉ mặc định
    void setDefault(Integer id);
}