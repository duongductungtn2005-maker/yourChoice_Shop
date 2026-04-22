package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.KichThuoc;
import org.example.yourchoiceshop.repository.KichThuocRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
@Service
@RequiredArgsConstructor
public class KichThuocServiceImpl {
    private final KichThuocRepository repository;

    public Page<KichThuoc> getAll(String keyword, Integer status, Pageable pageable) {
        return repository.search(keyword, status, pageable);
    }

    public KichThuoc getById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Kích thước ID: " + id));
    }

    public KichThuoc create(StoreAttributeRequest req) {
        KichThuoc entity = new KichThuoc();
        entity.setMaKichThuoc("KT" + System.currentTimeMillis());
        entity.setTenKichThuoc(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }

    public KichThuoc update(Integer id, StoreAttributeRequest req) {
        KichThuoc entity = getById(id);
        entity.setTenKichThuoc(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }

    public void delete(Integer id) {
        KichThuoc entity = getById(id);
        entity.setTrangThai(0);
        repository.save(entity);
    }
    public ByteArrayInputStream exportToExcel() throws IOException {
        List<KichThuoc> dataList = repository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Danh sách Kích Thước");

            // 1. Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Mã Kích Thước", "Tên Kích Thước", "Trạng Thái", "Ngày Tạo"};

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
            for (KichThuoc item : dataList) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(item.getId());
                row.createCell(1).setCellValue(item.getMaKichThuoc() != null ? item.getMaKichThuoc() : "");
                row.createCell(2).setCellValue(item.getTenKichThuoc());
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