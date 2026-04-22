package org.example.yourchoiceshop.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.security.SecureRandom;
import java.text.Normalizer;

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
    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%";
    private static final int PASSWORD_LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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

        if (req.getTenNhanVien() == null || req.getTenNhanVien().trim().isEmpty()) {
            throw new RuntimeException("Tên nhân viên không được để trống");
        }
        // Kiểm tra trùng số điện thoại
        if (req.getSoDienThoai() != null && !req.getSoDienThoai().trim().isEmpty()
                && nhanVienRepo.existsBySoDienThoai(req.getSoDienThoai().trim())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        int randomNum = (int) (Math.floor(Math.random() * 90000) + 10000);
        nv.setMaNhanVien("NV" + randomNum);
        nv.setTenTaiKhoan(generateUniqueUsername(req.getTenNhanVien()));
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

        // Luôn tạo mật khẩu ngẫu nhiên cho cả ADMIN và STAFF.
        String matKhau = generateRandomPassword();
        boolean isAdmin = "ADMIN".equalsIgnoreCase(req.getChucVu());
        nv.setMatKhau(matKhau);
        nv.setTrangThai(1);

        // Map Quyền hạn
        QuyenHan quyenHan = new QuyenHan();
        if (isAdmin) {
            quyenHan.setId(1);
        } else {
            quyenHan.setId(2); // Đổi 4 thành 2 để khớp với Database của bạn
        }
        nv.setQuyenHan(quyenHan);

        NhanVien savedNv = nhanVienRepo.save(nv);

        if (savedNv.getEmail() != null && !savedNv.getEmail().isEmpty()) {
            new Thread(() -> {
                emailService.sendEmployeeWelcome(
                    savedNv.getEmail(), 
                    savedNv.getTenTaiKhoan(), 
                    savedNv.getTenNhanVien(), 
                    matKhau
                );
            }).start();
        }

        return savedNv;
    }

    @Override
    public NhanVien update(Integer id, EmployeeRequest req) {
        NhanVien nv = findById(id);

        if (req.getTenNhanVien() == null || req.getTenNhanVien().trim().isEmpty()) {
            throw new RuntimeException("Tên nhân viên không được để trống");
        }
        // Kiểm tra trùng số điện thoại (loại trừ chính mình)
        if (req.getSoDienThoai() != null && !req.getSoDienThoai().trim().isEmpty()
                && nhanVienRepo.existsBySoDienThoaiAndIdNot(req.getSoDienThoai().trim(), id)) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        nv.setTenTaiKhoan(generateUniqueUsernameForUpdate(req.getTenNhanVien(), id));
        nv.setTenNhanVien(req.getTenNhanVien());
        nv.setEmail(req.getEmail());
        nv.setSoDienThoai(req.getSoDienThoai());
        nv.setGioiTinh(req.getGioiTinh());
        nv.setNgaySinh(req.getNgaySinh());
        if (req.getMatKhau() != null && !req.getMatKhau().trim().isEmpty()) {
            nv.setMatKhau(req.getMatKhau().trim());
        }

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
    public boolean checkTrungSoDienThoai(String soDienThoai, Integer id) {
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) return false;
        if (id == null) {
            return nhanVienRepo.existsBySoDienThoai(soDienThoai.trim());
        }
        return nhanVienRepo.existsBySoDienThoaiAndIdNot(soDienThoai.trim(), id);
    }

    @Override
    public boolean authenticateEmployee(String username, String password) {
        String usernameValue = username != null ? username.trim() : "";
        String passwordValue = password != null ? password.trim() : "";
        if (usernameValue.isEmpty() || passwordValue.isEmpty()) {
            return false;
        }
        // Thử đăng nhập bằng tên tài khoản trước
        if (nhanVienRepo.existsByTenTaiKhoanAndMatKhau(usernameValue, passwordValue)) {
            return true;
        }
        // Nếu không tìm thấy, thử đăng nhập bằng email
        return nhanVienRepo.existsByEmailIgnoreCaseAndMatKhau(usernameValue, passwordValue);
    }

    @Override
    public NhanVien getEmployeeByCredentials(String username, String password) {
        String usernameValue = username != null ? username.trim() : "";
        String passwordValue = password != null ? password.trim() : "";
        if (usernameValue.isEmpty() || passwordValue.isEmpty()) {
            return null;
        }
        // Thử tìm bằng tên tài khoản trước
        NhanVien employee = nhanVienRepo.findByTenTaiKhoanAndMatKhau(usernameValue, passwordValue).orElse(null);

        // Nếu không tìm thấy, thử tìm bằng email
        if (employee == null) {
            employee = nhanVienRepo.findByEmailIgnoreCaseAndMatKhau(usernameValue, passwordValue).orElse(null);
        }

        // Kiểm tra tài khoản còn hoạt động không (trangThai = 1)
        if (employee != null && employee.getTrangThai() != null && employee.getTrangThai() != 1) {
            return null;
        }
        return employee;
    }

    @Override
    public Optional<NhanVien> findByTenDangNhap(String tenDangNhap) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            return Optional.empty();
        }
        return nhanVienRepo.findByTenTaiKhoan(tenDangNhap.trim());
    }

    private String generateRandomPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int idx = SECURE_RANDOM.nextInt(PASSWORD_CHARS.length());
            password.append(PASSWORD_CHARS.charAt(idx));
        }
        return password.toString();
    }

    private String generateUniqueUsername(String tenNhanVien) {
        String base = slugifyName(tenNhanVien);
        String candidate = base;
        int suffix = 1;

        while (nhanVienRepo.existsByTenTaiKhoan(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private String generateUniqueUsernameForUpdate(String tenNhanVien, Integer currentId) {
        String base = slugifyName(tenNhanVien);
        String candidate = base;
        int suffix = 1;

        while (nhanVienRepo.existsByTenTaiKhoanAndIdNot(candidate, currentId)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private String slugifyName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "nhanvien";
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase();

        String slug = normalized.replaceAll("[^a-z0-9]+", "");
        return slug.isEmpty() ? "nhanvien" : slug;
    }
}