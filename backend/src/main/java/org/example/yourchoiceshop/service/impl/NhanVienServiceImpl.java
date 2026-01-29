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

@Service
public class NhanVienServiceImpl implements NhanVienService {

    @Autowired
    private NhanVienRepository nhanVienRepo;
    @Autowired
    private EmailService emailService;
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
    @Transactional
    public NhanVien create(EmployeeRequest req) {
        NhanVien nv = new NhanVien();

        // 1. SINH MÃ NHÂN VIÊN (Random 5 số)
        int randomNum = (int) (Math.floor(Math.random() * 90000) + 10000);
        nv.setMaNhanVien("NV" + randomNum);

        // 2. MAP DỮ LIỆU TỪ REQUEST
        nv.setTenNhanVien(req.getTenNhanVien());
        nv.setEmail(req.getEmail());
        nv.setSoDienThoai(req.getSoDienThoai());
        nv.setCccd(req.getCccd());
        nv.setGioiTinh(req.getGioiTinh());
        nv.setNgaySinh(req.getNgaySinh());

        // 3. XỬ LÝ ĐỊA CHỈ & ẢNH
        String cleanAddress = buildAddress(req.getAddress(), req.getWard(), req.getDistrict(), req.getCity());
        nv.setDiaChi(cleanAddress);

        if (req.getAvatarFile() != null && !req.getAvatarFile().isEmpty()) {
            String fileName = saveFile(req.getAvatarFile());
            nv.setAnhDaiDien(fileName);
        }

        // 4. THIẾT LẬP MẶC ĐỊNH (Mật khẩu, Trạng thái, Quyền)
        String matKhauMacDinh = "123456";
        nv.setMatKhau(matKhauMacDinh); // (Nên mã hóa BCrypt ở đây nếu có security)
        nv.setTrangThai(1);

        QuyenHan quyenHan = new QuyenHan();
        quyenHan.setId(1); // ID = 1 là Nhân viên
        nv.setQuyenHan(quyenHan);

        // 5. LƯU VÀO DB (CHỈ LƯU 1 LẦN DUY NHẤT)
        NhanVien savedNv = nhanVienRepo.save(nv);

        // 6. GỬI EMAIL CHÀO MỪNG (CHẠY NGẦM)
        if (savedNv.getEmail() != null && !savedNv.getEmail().isEmpty()) {
            new Thread(() -> {
                emailService.sendWelcomeEmail(savedNv.getEmail(), savedNv.getTenNhanVien(), matKhauMacDinh);
            }).start();
        }

        // Trả về đối tượng đã lưu
        return savedNv;
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
    // ... các hàm override khác ...

    @Override
    public List<NhanVien> findAllList(String keyword, Boolean gender, Integer status) {
        // Xử lý keyword giống hệt hàm findAll phân trang
        if (keyword != null && !keyword.trim().isEmpty()) {
            keyword = "%" + keyword.trim().toLowerCase() + "%";
        } else {
            keyword = null;
        }

        // Gọi Repo lấy list không phân trang (Bạn cần thêm hàm này vào Repo)
        // Hoặc tạm thời dùng cách dưới đây để convert Page sang List nếu lười sửa Repo:
        // (Cách này không tối ưu lắm cho data lớn nhưng nhanh gọn)
        return nhanVienRepo.searchNhanVien(keyword, gender, status, Pageable.unpaged()).getContent();
    }

} // Kết thúc class