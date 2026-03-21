package org.example.yourchoiceshop.service;

import org.example.yourchoiceshop.dto.request.KhachHangRequest;
import org.example.yourchoiceshop.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public interface KhachHangService {

    Page<KhachHang> findAll(String keyword, Boolean gender, Integer status, Pageable pageable);

    KhachHang findById(Integer id);

    KhachHang create(KhachHangRequest request);

    KhachHang update(Integer id, KhachHangRequest req);

    void delete(Integer id);

    void updateTrangThai(Integer id, Integer trangThai);

    // Check username tồn tại
    boolean existsByUsername(String username);

    // Check username tồn tại, loại trừ 1 id (dùng cho update)
    boolean existsByUsername(String username, Integer excludeId);

    // Check SĐT tồn tại, loại trừ 1 id (dùng cho update)
    boolean existsBySoDienThoai(String soDienThoai, Integer excludeId);

    // Authenticate đăng nhập (true/false)
    boolean authenticateCustomer(String username, String password);

    // Lấy khách hàng theo thông tin đăng nhập
    KhachHang getCustomerByCredentials(String username, String password);

    // Export excel
    ByteArrayInputStream exportToExcel(String keyword, Boolean gender, Integer status) throws IOException;

    // Lấy list không phân trang
    List<KhachHang> findAllList(String keyword, Boolean gender, Integer status);
}