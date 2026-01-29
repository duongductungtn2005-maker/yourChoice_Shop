package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.MauSac;
import org.example.yourchoiceshop.repository.MauSacRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
@Service
@RequiredArgsConstructor
public class MauSacServiceImpl {
    private final MauSacRepository repository;

    public Page<MauSac> getAll(String keyword, Integer status, Pageable pageable) {
        return repository.search(keyword, status, pageable);
    }

    public MauSac getById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Màu sắc ID: " + id));
    }

    public MauSac create(StoreAttributeRequest req) {
        MauSac entity = new MauSac();
        entity.setMaMauSac("MS" + System.currentTimeMillis());
        entity.setTenMauSac(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }

    public MauSac update(Integer id, StoreAttributeRequest req) {
        MauSac entity = getById(id);
        entity.setTenMauSac(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }

    public void delete(Integer id) {
        MauSac entity = getById(id);
        entity.setTrangThai(0);
        repository.save(entity);
    }
    public ByteArrayInputStream exportToExcel() throws IOException {
        List<MauSac> dataList = repository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Danh sách Màu Sắc");

            // 1. Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Mã Màu", "Tên Màu Sắc", "Trạng Thái", "Ngày Tạo"};

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
            for (MauSac item : dataList) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(item.getId());
                row.createCell(1).setCellValue(item.getMaMauSac() != null ? item.getMaMauSac() : "");
                row.createCell(2).setCellValue(item.getTenMauSac());
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