package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.ThuongHieu;
import org.example.yourchoiceshop.repository.ThuongHieuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
// import java.io.ByteArrayInputStream;
// import java.io.ByteArrayOutputStream;
// import java.io.IOException;
// import java.util.List;
@Service
@RequiredArgsConstructor
public class ThuongHieuServiceImpl {
    private final ThuongHieuRepository repository;

    public Page<ThuongHieu> getAll(String keyword, Integer status, Pageable pageable) {
        return repository.search(keyword, status, pageable);
    }

    public ThuongHieu getById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Thương hiệu ID: " + id));
    }

    public ThuongHieu create(StoreAttributeRequest req) {
        ThuongHieu entity = new ThuongHieu();
        entity.setMaThuongHieu("TH" + System.currentTimeMillis());
        entity.setTenThuongHieu(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }

    public ThuongHieu update(Integer id, StoreAttributeRequest req) {
        ThuongHieu entity = getById(id);
        entity.setTenThuongHieu(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }

    public void delete(Integer id) {
        ThuongHieu entity = getById(id);
        entity.setTrangThai(0);
        repository.save(entity);
    }

    public ByteArrayInputStream exportToExcel() throws IOException {
        List<ThuongHieu> dataList = repository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Danh sách Thương Hiệu");

            // 1. Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Mã Thương Hiệu", "Tên Thương Hiệu", "Trạng Thái", "Ngày Tạo"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 2. Data
            int rowIdx = 1;
            for (ThuongHieu item : dataList) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(item.getId());
                row.createCell(1).setCellValue(item.getMaThuongHieu() != null ? item.getMaThuongHieu() : "");
                row.createCell(2).setCellValue(item.getTenThuongHieu());
                row.createCell(3).setCellValue(item.getTrangThai() == 1 ? "Hoạt động" : "Ngừng");
                row.createCell(4).setCellValue(item.getNgayTao() != null ? item.getNgayTao().toString() : "");
            }

            for(int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}