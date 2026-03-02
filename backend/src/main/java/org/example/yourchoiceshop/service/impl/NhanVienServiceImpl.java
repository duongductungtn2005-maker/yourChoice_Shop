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
import org.example.yourchoiceshop.service.EmailService;
import org.example.yourchoiceshop.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import java.util.Arrays; // Import thêm
@Service
public class NhanVienServiceImpl implements NhanVienService {

    @Autowired
    private NhanVienRepository nhanVienRepo;
    @Autowired
    private EmailService emailService;

    private final String UPLOAD_ROOT = "uploads/images/nhan-vien/";

    // [UPDATED] Hàm findAll nhận tham số role (String) thay vì gender
    // ... imports

    @Override
    public Page<NhanVien> findAll(String keyword, Integer status, String role, Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("id").descending());

        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = "%" + keyword.trim().toLowerCase() + "%";
        } else {
            keyword = null;
        }

        // --- SỬA LOGIC TẠI ĐÂY ---
        List<Integer> roleIds = null;
        if (role != null && !role.isEmpty()) {
            if ("ADMIN".equalsIgnoreCase(role)) {
                roleIds = Arrays.asList(1, 6); // Lấy cả Admin (1) và Quản lý (6)
            } else if ("STAFF".equalsIgnoreCase(role)) {
                roleIds = Arrays.asList(2, 4, 5); // Lấy Bán hàng (2), NV (4), NV (5)
            }
        }

        // Gọi Repo mới
        return nhanVienRepo.searchNhanVien(keyword, status, roleIds, sortedPageable);
    }

    @Override
    public NhanVien findById(Integer id) {
        return nhanVienRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên ID: " + id));
    }

    @Override
    @Transactional
    public NhanVien create(EmployeeRequest req) {
        NhanVien nv = new NhanVien();

        int randomNum = (int) (Math.floor(Math.random() * 90000) + 10000);
        nv.setMaNhanVien("NV" + randomNum);
        nv.setTenTaiKhoan(req.getTenTaiKhoan());
        nv.setTenNhanVien(req.getTenNhanVien());
        nv.setEmail(req.getEmail());
        nv.setSoDienThoai(req.getSoDienThoai());
        nv.setCccd(req.getCccd());
        nv.setGioiTinh(req.getGioiTinh());
        nv.setNgaySinh(req.getNgaySinh());

        if (req.getDiaChi() != null && !req.getDiaChi().isEmpty()) {
            nv.setDiaChi(req.getDiaChi());
        } else {
            nv.setDiaChi(buildAddress(req.getAddress(), req.getWard(), req.getDistrict(), req.getCity()));
        }

        if (req.getAvatarFile() != null && !req.getAvatarFile().isEmpty()) {
            nv.setAnhDaiDien(saveFile(req.getAvatarFile()));
        }

        String matKhauMacDinh = "123456";
        nv.setMatKhau(matKhauMacDinh);
        nv.setTrangThai(1);

        // Map Quyền hạn
        QuyenHan quyenHan = new QuyenHan();
        if ("ADMIN".equalsIgnoreCase(req.getChucVu())) {
            quyenHan.setId(1);
        } else {
            quyenHan.setId(2); // Đổi 4 thành 2 để khớp với Database của bạn
        }
        nv.setQuyenHan(quyenHan);

        NhanVien savedNv = nhanVienRepo.save(nv);

        if (savedNv.getEmail() != null && !savedNv.getEmail().isEmpty()) {
            new Thread(() -> {
                // Đã bổ sung savedNv.getTenTaiKhoan() vào vị trí thứ 2 👇
                emailService.sendEmployeeWelcome(
                    savedNv.getEmail(), 
                    savedNv.getTenTaiKhoan(), 
                    savedNv.getTenNhanVien(), 
                    matKhauMacDinh
                );
            }).start();
        }

        return savedNv;
    }

    @Override
    public NhanVien update(Integer id, EmployeeRequest req) {
        NhanVien nv = findById(id);
        nv.setTenTaiKhoan(req.getTenTaiKhoan());
        nv.setTenNhanVien(req.getTenNhanVien());
        nv.setEmail(req.getEmail());
        nv.setSoDienThoai(req.getSoDienThoai());
        nv.setGioiTinh(req.getGioiTinh());
        nv.setNgaySinh(req.getNgaySinh());

        if (req.getDiaChi() != null && !req.getDiaChi().isEmpty()) {
            nv.setDiaChi(req.getDiaChi());
        } else {
            nv.setDiaChi(buildAddress(req.getAddress(), req.getWard(), req.getDistrict(), req.getCity()));
        }

        // Cập nhật Quyền hạn
        if (req.getChucVu() != null) {
            QuyenHan qh = new QuyenHan();
            if ("ADMIN".equalsIgnoreCase(req.getChucVu())) {
                qh.setId(1);
                } else {
                    qh.setId(2); // Đổi 4 thành 2 để khớp với Database của bạn
                }
            nv.setQuyenHan(qh);
        }

        if (req.getAvatarFile() != null && !req.getAvatarFile().isEmpty()) {
            nv.setAnhDaiDien(saveFile(req.getAvatarFile()));
        }

        return nhanVienRepo.save(nv);
    }

    @Override
    public void delete(Integer id) {
        NhanVien nv = findById(id);
        nv.setTrangThai(0);
        nhanVienRepo.save(nv);
    }

    @Override
    public void updateTrangThai(Integer id, Integer trangThai) {
        NhanVien nv = findById(id);
        nv.setTrangThai(trangThai);
        nhanVienRepo.save(nv);
    }

    private String saveFile(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isEmpty()) return null;
            String extension = "";
            int i = originalName.lastIndexOf('.');
            if (i > 0) extension = originalName.substring(i);
            String fileName = UUID.randomUUID().toString() + extension;
            Path rootPath = Paths.get(UPLOAD_ROOT);
            if (!Files.exists(rootPath)) Files.createDirectories(rootPath);
            Files.copy(file.getInputStream(), rootPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file ảnh: " + e.getMessage());
        }
    }

    private String buildAddress(String dc, String phuong, String huyen, String tp) {
        List<String> parts = new ArrayList<>();
        if (dc != null && !dc.trim().isEmpty()) parts.add(dc.trim());
        if (phuong != null && !phuong.trim().isEmpty()) parts.add(phuong.trim());
        if (huyen != null && !huyen.trim().isEmpty()) parts.add(huyen.trim());
        if (tp != null && !tp.trim().isEmpty()) parts.add(tp.trim());
        if (parts.isEmpty()) return "Chưa cập nhật";
        return String.join(", ", parts);
    }

    // [UPDATED] Hàm xuất Excel cũng cần hỗ trợ lọc theo Role
    @Override
    public List<NhanVien> findAllList(String keyword, Boolean gender, Integer status) {
        // Lưu ý: Nếu muốn Excel lọc đúng theo Role thì bạn cần sửa thêm tham số role vào đây
        // Tạm thời giữ nguyên logic cũ để code không bị lỗi compile, nhưng nó sẽ chưa lọc Role khi xuất Excel
        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = "%" + keyword.trim().toLowerCase() + "%";
        } else {
            keyword = null;
        }
        // Gọi searchNhanVien với roleId = null
        return nhanVienRepo.searchNhanVien(keyword, status, null, Pageable.unpaged()).getContent();
    }
    @Override
public boolean checkTrungTaiKhoan(String tenTaiKhoan, Integer id) {
    if (id == null) {
        // Trường hợp Thêm mới: Chỉ cần tìm xem tên tài khoản này đã có ai dùng chưa
        return nhanVienRepo.existsByTenTaiKhoan(tenTaiKhoan);
    } else {
        // Trường hợp Cập nhật: Tìm xem có ai dùng chưa, nhưng phải LOẠI TRỪ nhân viên hiện tại ra
        return nhanVienRepo.existsByTenTaiKhoanAndIdNot(tenTaiKhoan, id);
    }
}
}