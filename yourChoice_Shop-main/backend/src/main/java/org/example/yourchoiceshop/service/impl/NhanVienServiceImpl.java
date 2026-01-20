package org.example.yourchoiceshop.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.example.yourchoiceshop.dto.request.EmployeeRequest;
import org.example.yourchoiceshop.entity.NhanVien;
import org.example.yourchoiceshop.entity.QuyenHan;
import org.example.yourchoiceshop.repository.NhanVienRepository;
import org.example.yourchoiceshop.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class NhanVienServiceImpl implements NhanVienService {

    @Autowired
    private NhanVienRepository nhanVienRepo;

    // Đường dẫn thư mục lưu ảnh
    private final String UPLOAD_ROOT = "uploads/images/nhan-vien/";

    @Override
    public Page<NhanVien> findAll(String keyword, Boolean gender, Integer status, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").descending());
        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = "%" + keyword.trim().toLowerCase() + "%";
        } else {
            keyword = null;
        }
        return nhanVienRepo.searchNhanVien(keyword, gender, status, sortedPageable);
    }

    @Override
    public NhanVien findById(Integer id) {
        return nhanVienRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên ID: " + id));
    }

    @Override
    public NhanVien create(EmployeeRequest req) {
        NhanVien nv = new NhanVien();

        // 1. Map thông tin cơ bản
        nv.setTenNhanVien(req.getTenNhanVien());
        nv.setEmail(req.getEmail());
        nv.setSoDienThoai(req.getSoDienThoai());
        nv.setCccd(req.getCccd());
        nv.setGioiTinh(req.getGioiTinh());
        nv.setNgaySinh(req.getNgaySinh());

        // 2. Xử lý địa chỉ thông minh (Tránh null)
        String cleanAddress = buildAddress(req.getAddress(), req.getWard(), req.getDistrict(), req.getCity());
        nv.setDiaChi(cleanAddress);

        // 3. Set thông tin hệ thống
        nv.setMatKhau("123456");
        nv.setTrangThai(1);
        nv.setMaNhanVien("NV" + System.currentTimeMillis());

        // 4. --- QUYỀN HẠN (Quan Trọng) ---
        QuyenHan quyenHan = new QuyenHan();
        // LƯU Ý: Đảm bảo ID này tồn tại trong DB của bác (Check lại DB xem là 1 hay số khác)
        quyenHan.setId(1); 
        nv.setQuyenHan(quyenHan);

        // 5. Xử lý ảnh (Dùng hàm helper bên dưới)
        if (req.getAvatarFile() != null && !req.getAvatarFile().isEmpty()) {
            String fileName = saveFile(req.getAvatarFile());
            nv.setAnhDaiDien(fileName);
        }

        return nhanVienRepo.save(nv);
    }

    @Override
    public NhanVien update(Integer id, EmployeeRequest req) {
        NhanVien nv = findById(id);

        // Map thông tin update
        nv.setTenNhanVien(req.getTenNhanVien());
        nv.setEmail(req.getEmail());
        nv.setSoDienThoai(req.getSoDienThoai());
        nv.setGioiTinh(req.getGioiTinh());
        nv.setNgaySinh(req.getNgaySinh());
        
        // Cập nhật địa chỉ
        String cleanAddress = buildAddress(req.getAddress(), req.getWard(), req.getDistrict(), req.getCity());
        nv.setDiaChi(cleanAddress);

        // Cập nhật ảnh mới (nếu có)
        if (req.getAvatarFile() != null && !req.getAvatarFile().isEmpty()) {
            String fileName = saveFile(req.getAvatarFile());
            nv.setAnhDaiDien(fileName);
        }

        return nhanVienRepo.save(nv);
    }

    @Override
    public void delete(Integer id) {
        NhanVien nv = findById(id);
        nv.setTrangThai(0); // Xóa mềm
        nhanVienRepo.save(nv);
    }

    @Override
    public void updateTrangThai(Integer id, Integer trangThai) {
        NhanVien nv = findById(id);
        nv.setTrangThai(trangThai);
        nhanVienRepo.save(nv);
    }

    // ==========================================================
    // CÁC HÀM PHỤ TRỢ (HELPER METHODS) - GIỮ CODE GỌN GÀNG
    // ==========================================================

    /**
     * Hàm lưu file vật lý vào ổ cứng
     * Sử dụng UUID để đảm bảo tên file không bao giờ trùng
     */
    private String saveFile(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isEmpty()) return null;

            // Lấy đuôi file (ví dụ .jpg, .png)
            String extension = "";
            int i = originalName.lastIndexOf('.');
            if (i > 0) {
                extension = originalName.substring(i);
            }

            // Tạo tên file ngẫu nhiên: "uuid-code.jpg"
            String fileName = UUID.randomUUID().toString() + extension;

            // Tạo thư mục nếu chưa có
            Path rootPath = Paths.get(UPLOAD_ROOT);
            if (!Files.exists(rootPath)) {
                Files.createDirectories(rootPath);
            }

            // Lưu file
            Files.copy(file.getInputStream(), rootPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file ảnh: " + e.getMessage());
        }
    }

    /**
     * Hàm nối chuỗi địa chỉ, bỏ qua các thành phần bị null
     */
    private String buildAddress(String dc, String phuong, String huyen, String tp) {
        List<String> parts = new ArrayList<>();
        
        if (dc != null && !dc.trim().isEmpty()) parts.add(dc.trim());
        if (phuong != null && !phuong.trim().isEmpty()) parts.add(phuong.trim());
        if (huyen != null && !huyen.trim().isEmpty()) parts.add(huyen.trim());
        if (tp != null && !tp.trim().isEmpty()) parts.add(tp.trim());

        if (parts.isEmpty()) return "Chưa cập nhật";
        
        return String.join(", ", parts);
    }
}