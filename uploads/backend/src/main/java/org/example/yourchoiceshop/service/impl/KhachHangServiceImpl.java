package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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

    // =========================
    // CREATE
    // =========================
    @Override
    public KhachHang create(KhachHangRequest request) {
        if (request == null) {
            throw new RuntimeException("Dữ liệu khách hàng không hợp lệ");
        }

        KhachHang kh = new KhachHang();

        String maKh = request.getMaKhachHang() != null ? request.getMaKhachHang().trim() : "";
        String tenKh = request.getTenKhachHang() != null ? request.getTenKhachHang().trim() : "";
        String sdt = request.getSoDienThoai() != null ? request.getSoDienThoai().trim() : "";
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        String finalUsername = request.getUsername() != null ? request.getUsername().trim() : "";

        if (tenKh.isEmpty()) throw new RuntimeException("Họ và tên không được để trống");
        if (finalUsername.isEmpty()) throw new RuntimeException("Tên tài khoản không được để trống");

        // Mã KH
        kh.setMaKhachHang(maKh.isEmpty() ? ("KH" + System.currentTimeMillis()) : maKh);

        // ✅ CHỈ CHẶN TRÙNG USERNAME
        if (khachHangRepository.existsByTenTaiKhoanIgnoreCase(finalUsername)) {
            throw new RuntimeException("Tên tài khoản đã tồn tại");
        }

        // ✅ (GIỮ) CHẶN TRÙNG SĐT - nếu muốn cho trùng SĐT thì xoá block này
        if (!sdt.isEmpty() && khachHangRepository.existsBySoDienThoai(sdt)) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        // ✅ EMAIL ĐƯỢC TRÙNG -> KHÔNG CHECK existsByEmail
        kh.setTenKhachHang(tenKh);
        kh.setSoDienThoai(sdt.isEmpty() ? null : sdt);
        kh.setEmail(email.isEmpty() ? null : email);
        kh.setGioiTinh(request.getGioiTinh());
        kh.setNgaySinh(request.getNgaySinh());
        kh.setTrangThai(1);

        // Username & Password
        String rawPassword = generateRandomPassword();
        kh.setTenTaiKhoan(finalUsername);
        kh.setMatKhau(rawPassword);

        // Avatar
        kh.setAvatar(saveFile(request.getAvatarFile()));

        // Địa chỉ (gán cha cho con)
        if (request.getListDiaChi() != null && !request.getListDiaChi().isEmpty()) {
            List<DiaChiKhachHang> diaChiList = request.getListDiaChi();
            for (DiaChiKhachHang dc : diaChiList) {
                dc.setKhachHang(kh);
                if (dc.getTrangThai() == null) dc.setTrangThai(1);
            }
            kh.setListDiaChi(diaChiList);
        }

        KhachHang savedKh = khachHangRepository.save(kh);

        // Gửi mail (nếu có email)
        if (savedKh.getEmail() != null && !savedKh.getEmail().trim().isEmpty()) {
            new Thread(() -> {
                try {
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

    // =========================
    // UPDATE
    // =========================
    @Override
    public KhachHang update(Integer id, KhachHangRequest request) {
        if (request == null) {
            throw new RuntimeException("Dữ liệu khách hàng không hợp lệ");
        }

        KhachHang kh = findById(id);

        String tenKh = request.getTenKhachHang() != null ? request.getTenKhachHang().trim() : "";
        String sdt = request.getSoDienThoai() != null ? request.getSoDienThoai().trim() : "";
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        String username = request.getUsername() != null ? request.getUsername().trim() : "";

        if (tenKh.isEmpty()) throw new RuntimeException("Họ và tên không được để trống");
        if (username.isEmpty()) throw new RuntimeException("Tên tài khoản không được để trống");
        if (username.length() < 3 || username.length() > 50) {
            throw new RuntimeException("Tên tài khoản phải từ 3 đến 50 ký tự");
        }
        if (!email.isEmpty() && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new RuntimeException("Email không hợp lệ");
        }
        if (!sdt.isEmpty() && !sdt.matches("^0\\d{9}$")) {
            throw new RuntimeException("Số điện thoại không hợp lệ (phải gồm 10 số, bắt đầu bằng 0)");
        }

        // ✅ CHỈ CHẶN TRÙNG USERNAME (ngoại trừ chính nó)
        if (khachHangRepository.existsByTenTaiKhoanIgnoreCaseAndIdNot(username, id)) {
            throw new RuntimeException("Tên tài khoản đã tồn tại");
        }

        // ✅ (GIỮ) CHẶN TRÙNG SĐT (ngoại trừ chính nó)
        if (!sdt.isEmpty() && khachHangRepository.existsBySoDienThoaiAndIdNot(sdt, id)) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        kh.setTenKhachHang(tenKh);
        kh.setSoDienThoai(sdt.isEmpty() ? null : sdt);
        kh.setEmail(email.isEmpty() ? null : email);
        kh.setGioiTinh(request.getGioiTinh());
        kh.setNgaySinh(request.getNgaySinh());
        if (request.getTrangThai() != null) kh.setTrangThai(request.getTrangThai());
        kh.setTenTaiKhoan(username);

        // Password update (chỉ khi có giá trị mới)
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            if (request.getPassword().trim().length() < 6) {
                throw new RuntimeException("Mật khẩu tối thiểu 6 ký tự");
            }
            kh.setMatKhau(request.getPassword().trim());
        }

        // Avatar update
        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            kh.setAvatar(saveFile(request.getAvatarFile()));
        }

        // Địa chỉ: thay thế hoàn toàn list
        if (request.getListDiaChi() != null) {
            List<DiaChiKhachHang> newAddresses = request.getListDiaChi();

            for (DiaChiKhachHang dc : newAddresses) {
                dc.setKhachHang(kh);
                if (dc.getTrangThai() == null) dc.setTrangThai(1);
            }

            if (kh.getListDiaChi() == null) {
                kh.setListDiaChi(new ArrayList<>());
            }
            kh.getListDiaChi().clear();
            kh.getListDiaChi().addAll(newAddresses);
        }

        return khachHangRepository.save(kh);
    }

    // =========================
    // DELETE / STATUS
    // =========================
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

    // =========================
    // USERNAME / PHONE CHECK
    // =========================
    @Override
    public boolean existsByUsername(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        return khachHangRepository.existsByTenTaiKhoanIgnoreCase(username.trim());
    }

    @Override
    public boolean existsByUsername(String username, Integer excludeId) {
        if (username == null || username.trim().isEmpty()) return false;
        if (excludeId == null) return existsByUsername(username);
        return khachHangRepository.existsByTenTaiKhoanIgnoreCaseAndIdNot(username.trim(), excludeId);
    }

    @Override
    public boolean existsBySoDienThoai(String soDienThoai, Integer excludeId) {
        if (soDienThoai == null || soDienThoai.trim().isEmpty()) return false;
        if (excludeId == null) return khachHangRepository.existsBySoDienThoai(soDienThoai.trim());
        return khachHangRepository.existsBySoDienThoaiAndIdNot(soDienThoai.trim(), excludeId);
    }

    // =========================
    // AUTH
    // =========================
    @Override
    public boolean authenticateCustomer(String username, String password) {
        String usernameValue = username != null ? username.trim() : "";
        String passwordValue = password != null ? password.trim() : "";
        if (usernameValue.isEmpty() || passwordValue.isEmpty()) return false;

        // Thử đăng nhập bằng tên tài khoản trước
        if (khachHangRepository.existsByTenTaiKhoanIgnoreCaseAndMatKhau(usernameValue, passwordValue)) {
            return true;
        }
        // Nếu không tìm thấy, thử đăng nhập bằng email
        return khachHangRepository.existsByEmailIgnoreCaseAndMatKhau(usernameValue, passwordValue);
    }

    @Override
    public KhachHang getCustomerByCredentials(String username, String password) {
        String usernameValue = username != null ? username.trim() : "";
        String passwordValue = password != null ? password.trim() : "";
        if (usernameValue.isEmpty() || passwordValue.isEmpty()) return null;

        // Thử tìm bằng tên tài khoản trước
        KhachHang customer = khachHangRepository
                .findByTenTaiKhoanIgnoreCaseAndMatKhau(usernameValue, passwordValue)
                .orElse(null);

        // Nếu không tìm thấy, thử tìm bằng email
        if (customer == null) {
            customer = khachHangRepository
                    .findByEmailIgnoreCaseAndMatKhau(usernameValue, passwordValue)
                    .orElse(null);
        }

        // Kiểm tra tài khoản còn hoạt động không (trangThai = 1)
        if (customer != null && customer.getTrangThai() != null && customer.getTrangThai() != 1) {
            return null;
        }
        return customer;
    }

    // =========================
    // REGISTER (Client tự đăng ký)
    // =========================
    @Override
    public KhachHang registerCustomer(org.example.yourchoiceshop.dto.request.RegisterRequest request) {
        if (request == null) {
            throw new RuntimeException("Dữ liệu đăng ký không hợp lệ");
        }

        String hoTen = request.getHoTen() != null ? request.getHoTen().trim() : "";
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        String sdt = request.getSoDienThoai() != null ? request.getSoDienThoai().trim() : "";
        String tenTaiKhoan = request.getTenTaiKhoan() != null ? request.getTenTaiKhoan().trim() : "";
        String matKhau = request.getMatKhau() != null ? request.getMatKhau().trim() : "";

        if (hoTen.isEmpty()) throw new RuntimeException("Họ và tên không được để trống");
        if (email.isEmpty()) throw new RuntimeException("Email không được để trống");
        if (tenTaiKhoan.isEmpty()) throw new RuntimeException("Tên tài khoản không được để trống");
        if (matKhau.isEmpty()) throw new RuntimeException("Mật khẩu không được để trống");
        if (matKhau.length() < 6) throw new RuntimeException("Mật khẩu tối thiểu 6 ký tự");

        // Kiểm tra trùng username
        if (khachHangRepository.existsByTenTaiKhoanIgnoreCase(tenTaiKhoan)) {
            throw new RuntimeException("Tên tài khoản đã tồn tại");
        }

        // Kiểm tra trùng SĐT
        if (!sdt.isEmpty() && khachHangRepository.existsBySoDienThoai(sdt)) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        KhachHang kh = new KhachHang();
        kh.setMaKhachHang("KH" + System.currentTimeMillis());
        kh.setTenKhachHang(hoTen);
        kh.setEmail(email);
        kh.setSoDienThoai(sdt.isEmpty() ? null : sdt);
        kh.setTenTaiKhoan(tenTaiKhoan);
        kh.setMatKhau(matKhau);
        kh.setTrangThai(1);

        return khachHangRepository.save(kh);
    }

    // =========================
    // EXPORT EXCEL
    // =========================
    @Override
    public ByteArrayInputStream exportToExcel(String keyword, Boolean gender, Integer status) throws IOException {
        Specification<KhachHang> spec = createSpecification(keyword, gender, status);
        List<KhachHang> list = khachHangRepository.findAll(spec);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("KhachHang");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Mã KH", "Họ Tên", "Email", "SĐT", "Giới tính", "Ngày sinh", "Trạng thái"};

            for (int i = 0; i < columns.length; i++) {
                headerRow.createCell(i).setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (KhachHang kh : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(kh.getId() != null ? kh.getId() : 0);
                row.createCell(1).setCellValue(kh.getMaKhachHang() != null ? kh.getMaKhachHang() : "");
                row.createCell(2).setCellValue(kh.getTenKhachHang() != null ? kh.getTenKhachHang() : "");
                row.createCell(3).setCellValue(kh.getEmail() != null ? kh.getEmail() : "");
                row.createCell(4).setCellValue(kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "");
                row.createCell(5).setCellValue(kh.getGioiTinh() != null && kh.getGioiTinh() ? "Nam" : "Nữ");
                row.createCell(6).setCellValue(kh.getNgaySinh() != null ? kh.getNgaySinh().toString() : "");
                row.createCell(7).setCellValue(kh.getTrangThai() != null && kh.getTrangThai() == 1 ? "Hoạt động" : "Không hoạt động");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // =========================
    // FIND LIST
    // =========================
    @Override
    public List<KhachHang> findAllList(String keyword, Boolean gender, Integer status) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = "%" + keyword.trim().toLowerCase() + "%";
        } else {
            keyword = null;
        }
        return khachHangRepository.searchKhachHang(keyword, gender, status, Pageable.unpaged()).getContent();
    }

    // =========================
    // HELPERS
    // =========================
    private Specification<KhachHang> createSpecification(String keyword, Boolean gender, Integer status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
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
            String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
            String fileName = System.currentTimeMillis() + "_" + original;

            Path path = Paths.get(UPLOAD_DIR);
            if (!Files.exists(path)) Files.createDirectories(path);

            Files.copy(file.getInputStream(), path.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi lưu ảnh: " + e.getMessage());
        }
    }

    private String generateRandomPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int idx = SECURE_RANDOM.nextInt(PASSWORD_CHARS.length());
            password.append(PASSWORD_CHARS.charAt(idx));
        }
        return password.toString();
    }
}