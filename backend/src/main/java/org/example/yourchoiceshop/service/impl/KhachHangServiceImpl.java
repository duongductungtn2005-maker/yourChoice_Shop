package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.KhachHangRequest;
import org.example.yourchoiceshop.entity.DiaChiKhachHang;
import org.example.yourchoiceshop.entity.KhachHang;
import org.example.yourchoiceshop.repository.KhachHangRepository;
import org.example.yourchoiceshop.service.EmailService;
import org.example.yourchoiceshop.service.KhachHangService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KhachHangServiceImpl implements KhachHangService {

    private final KhachHangRepository khachHangRepository;
    private final EmailService emailService;
    private static final String UPLOAD_DIR = "uploads/images/khach-hang/";
    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%";
    private static final int PASSWORD_LENGTH = 6;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public Page<KhachHang> findAll(String keyword, Boolean gender, Integer status, Pageable pageable) {
        Specification<KhachHang> spec = createSpecification(keyword, gender, status);
        return khachHangRepository.findAll(spec, pageable);
    }

    @Override
    public KhachHang findById(Integer id) {
        return khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng ID: " + id));
    }

    // --- 3. TẠO MỚI (UPDATE LOGIC) ---
    @Override
    public KhachHang create(KhachHangRequest request) {
        KhachHang kh = new KhachHang();

        // Map thông tin
        if (request.getMaKhachHang() == null || request.getMaKhachHang().trim().isEmpty()) {
            kh.setMaKhachHang("KH" + System.currentTimeMillis());
        } else {
            kh.setMaKhachHang(request.getMaKhachHang());
        }

        kh.setTenKhachHang(request.getTenKhachHang());
        kh.setSoDienThoai(request.getSoDienThoai());
        kh.setEmail(request.getEmail());
        kh.setGioiTinh(request.getGioiTinh());
        kh.setNgaySinh(request.getNgaySinh());
        kh.setTrangThai(1);

        // Xử lý Username & Password
        String finalUsername = request.getUsername() != null ? request.getUsername().trim() : "";
        if (finalUsername.isEmpty()) {
            throw new RuntimeException("Tên tài khoản không được để trống");
        }
        if (khachHangRepository.existsByTenTaiKhoanIgnoreCase(finalUsername)) {
            throw new RuntimeException("Tên tài khoản đã tồn tại");
        }

        String rawPassword = generateRandomPassword();

        kh.setTenTaiKhoan(finalUsername);
        kh.setMatKhau(rawPassword);

        // Xử lý ảnh
        String avatar = saveFile(request.getAvatarFile());
        kh.setAvatar(avatar);

        // Xử lý địa chỉ (QUAN TRỌNG: Gán cha cho con)
        if (request.getListDiaChi() != null && !request.getListDiaChi().isEmpty()) {
            List<DiaChiKhachHang> diaChiList = request.getListDiaChi();
            for (DiaChiKhachHang dc : diaChiList) {
                dc.setKhachHang(kh); // Gán cha
                if(dc.getTrangThai() == null) dc.setTrangThai(1);
            }
            kh.setListDiaChi(diaChiList);
        }

        // Lưu DB
        KhachHang savedKh = khachHangRepository.save(kh);

        // Gửi Mail
        if (savedKh.getEmail() != null && !savedKh.getEmail().isEmpty()) {
            new Thread(() -> {
                try {
                    // Gọi hàm dành riêng cho khách hàng trong EmailService
                    emailService.sendCustomerWelcome(
                            savedKh.getEmail(),
                            savedKh.getTenTaiKhoan(),
                            rawPassword,
                            savedKh.getTenKhachHang()
                    );
                } catch (Exception e) {
                    System.err.println("Lỗi gửi mail: " + e.getMessage());
                }
            }).start();
        }

        return savedKh;
    }

    // --- 4. CẬP NHẬT (UPDATE LOGIC) ---
    @Override
    public KhachHang update(Integer id, KhachHangRequest request) {
        KhachHang kh = findById(id);

        // Update thông tin cơ bản
        kh.setTenKhachHang(request.getTenKhachHang());
        kh.setSoDienThoai(request.getSoDienThoai());
        kh.setEmail(request.getEmail());
        kh.setGioiTinh(request.getGioiTinh());
        kh.setNgaySinh(request.getNgaySinh());
        if (request.getTrangThai() != null) kh.setTrangThai(request.getTrangThai());

        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        if (username.isEmpty()) {
            throw new RuntimeException("Tên tài khoản không được để trống");
        }
        if (khachHangRepository.existsByTenTaiKhoanIgnoreCaseAndIdNot(username, id)) {
            throw new RuntimeException("Tên tài khoản đã tồn tại");
        }
        kh.setTenTaiKhoan(username);

        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            kh.setAvatar(saveFile(request.getAvatarFile()));
        }

        // --- XỬ LÝ ĐỊA CHỈ ---
        if (request.getListDiaChi() != null) {
            List<DiaChiKhachHang> newAddresses = request.getListDiaChi();

            // 1. Chuẩn hóa dữ liệu: Gán cha & Đảm bảo chỉ có 1 cái mặc định
            // (Frontend gửi lên có thể có 1 cái true, nhưng để chắc chắn ta xử lý lại)
            for (DiaChiKhachHang dc : newAddresses) {
                dc.setKhachHang(kh); // Quan trọng: Gán cha
                if(dc.getTrangThai() == null) dc.setTrangThai(1);
            }

            // 2. Cập nhật danh sách (Thay thế hoàn toàn)
            if (kh.getListDiaChi() == null) {
                kh.setListDiaChi(new ArrayList<>());
            }

            // Xóa sạch list cũ và thêm list mới (Hibernate tự lo việc delete/insert nhờ orphanRemoval)
            kh.getListDiaChi().clear();
            kh.getListDiaChi().addAll(newAddresses);
        }

        return khachHangRepository.save(kh);
    }

    @Override
    public void delete(Integer id) {
        KhachHang kh = findById(id);
        kh.setTrangThai(0);
        khachHangRepository.save(kh);
    }

    @Override
    public void updateTrangThai(Integer id, Integer trangThai) {
        KhachHang kh = findById(id);
        kh.setTrangThai(trangThai);
        khachHangRepository.save(kh);
    }

    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return khachHangRepository.existsByTenTaiKhoanIgnoreCase(username.trim());
    }

    @Override
    public boolean authenticateCustomer(String username, String password) {
        String usernameValue = username != null ? username.trim() : "";
        String passwordValue = password != null ? password.trim() : "";
        if (usernameValue.isEmpty() || passwordValue.isEmpty()) {
            return false;
        }
        return khachHangRepository.existsByTenTaiKhoanIgnoreCaseAndMatKhau(usernameValue, passwordValue);
    }

    @Override
    public ByteArrayInputStream exportToExcel(String keyword, Boolean gender, Integer status) throws IOException {
        Specification<KhachHang> spec = createSpecification(keyword, gender, status);
        List<KhachHang> list = khachHangRepository.findAll(spec);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("KhachHang");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Mã KH", "Họ Tên", "Email", "SĐT", "Giới tính", "Ngày sinh", "Trạng thái"};

            // ... (Phần style excel giữ nguyên) ...

            int rowIdx = 1;
            for (KhachHang kh : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(kh.getId());
                row.createCell(1).setCellValue(kh.getMaKhachHang());
                row.createCell(2).setCellValue(kh.getTenKhachHang());
                row.createCell(3).setCellValue(kh.getEmail());
                row.createCell(4).setCellValue(kh.getSoDienThoai());
                row.createCell(5).setCellValue(kh.getGioiTinh() ? "Nam" : "Nữ");
                row.createCell(6).setCellValue(kh.getNgaySinh() != null ? kh.getNgaySinh().toString() : "");
                row.createCell(7).setCellValue(kh.getTrangThai() == 1 ? "Hoạt động" : "Không hoạt động");
                // ...
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // --- Helpers ---
    private Specification<KhachHang> createSpecification(String keyword, Boolean gender, Integer status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.isEmpty()) {
                String likeKey = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("tenKhachHang"), likeKey),
                        cb.like(root.get("soDienThoai"), likeKey),
                        cb.like(root.get("email"), likeKey),
                        cb.like(root.get("maKhachHang"), likeKey)
                ));
            }
            if (gender != null) predicates.add(cb.equal(root.get("gioiTinh"), gender));
            if (status != null) predicates.add(cb.equal(root.get("trangThai"), status));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR);
            if (!Files.exists(path)) Files.createDirectories(path);
            Files.copy(file.getInputStream(), path.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu ảnh: " + e.getMessage());
        }
    }

    private String generateRandomPassword() {
            int idx = SECURE_RANDOM.nextInt(PASSWORD_CHARS.length());
            password.append(PASSWORD_CHARS.charAt(idx));
        }
        return password.toString();
    }

    @Override
    public List<KhachHang> findAllList(String keyword, Boolean gender, Integer status) {
    }
}