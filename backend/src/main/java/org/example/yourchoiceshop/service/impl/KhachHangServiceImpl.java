package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.KhachHangRequest;
import org.example.yourchoiceshop.entity.KhachHang;
import org.example.yourchoiceshop.repository.KhachHangRepository;
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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KhachHangServiceImpl implements KhachHangService {

    private final KhachHangRepository khachHangRepository;
    private static final String UPLOAD_DIR = "uploads/images/khach-hang/";

    // 1. Tìm kiếm phân trang
    @Override
    public Page<KhachHang> findAll(String keyword, Boolean gender, Integer status, Pageable pageable) {
        Specification<KhachHang> spec = createSpecification(keyword, gender, status);
        return khachHangRepository.findAll(spec, pageable);
    }

    // 2. Tìm theo ID
    @Override
    public KhachHang findById(Integer id) {
        return khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng ID: " + id));
    }

    // 3. Tạo mới
    @Override
    public KhachHang create(KhachHangRequest request) {
        KhachHang kh = new KhachHang();

        // Tạo mã tự động nếu rỗng
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
        kh.setTrangThai(1); // Mặc định hoạt động
        kh.setMatKhau("123456"); // Mặc định

        String avatar = saveFile(request.getAvatarFile());
        kh.setAvatar(avatar);

        return khachHangRepository.save(kh);
    }

    // 4. Cập nhật
    @Override
    public KhachHang update(Integer id, KhachHangRequest request) {
        KhachHang kh = findById(id);

        kh.setTenKhachHang(request.getTenKhachHang());
        kh.setSoDienThoai(request.getSoDienThoai());
        kh.setEmail(request.getEmail());
        kh.setGioiTinh(request.getGioiTinh());
        kh.setNgaySinh(request.getNgaySinh());

        if (request.getTrangThai() != null) {
            kh.setTrangThai(request.getTrangThai());
        }

        if (request.getAvatarFile() != null && !request.getAvatarFile().isEmpty()) {
            String newAvatar = saveFile(request.getAvatarFile());
            kh.setAvatar(newAvatar);
        }

        return khachHangRepository.save(kh);
    }

    // 5. Xóa mềm
    @Override
    public void delete(Integer id) {
        KhachHang kh = findById(id);
        kh.setTrangThai(0);
        khachHangRepository.save(kh);
    }

    // 6. Cập nhật trạng thái nhanh
    @Override
    public void updateTrangThai(Integer id, Integer trangThai) {
        KhachHang kh = findById(id);
        kh.setTrangThai(trangThai);
        khachHangRepository.save(kh);
    }

    // 7. Xuất Excel
    @Override
    public ByteArrayInputStream exportToExcel(String keyword, Boolean gender, Integer status) throws IOException {
        Specification<KhachHang> spec = createSpecification(keyword, gender, status);
        List<KhachHang> list = khachHangRepository.findAll(spec);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("KhachHang");

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Mã KH", "Họ Tên", "Email", "SĐT", "Giới tính", "Ngày sinh", "Trạng thái"};

            // Style Header
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (KhachHang kh : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(kh.getId());
                row.createCell(1).setCellValue(kh.getMaKhachHang());
                row.createCell(2).setCellValue(kh.getTenKhachHang());
                row.createCell(3).setCellValue(kh.getEmail());
                row.createCell(4).setCellValue(kh.getSoDienThoai());
                row.createCell(5).setCellValue(kh.getGioiTinh() != null ? (kh.getGioiTinh() ? "Nam" : "Nữ") : "");
                row.createCell(6).setCellValue(kh.getNgaySinh() != null ? kh.getNgaySinh().toString() : "");
                row.createCell(7).setCellValue(kh.getTrangThai() == 1 ? "Hoạt động" : "Ngừng HĐ");
            }

            for(int i=0; i<columns.length; i++) sheet.autoSizeColumn(i);
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

    // Implement phương thức thừa nếu Interface yêu cầu (findAllList)
    @Override
    public List<KhachHang> findAllList(String keyword, Boolean gender, Integer status) {
        return khachHangRepository.findAll(createSpecification(keyword, gender, status));
    }
}