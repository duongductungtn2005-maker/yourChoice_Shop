package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.CoAo;
import org.example.yourchoiceshop.repository.CoAoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoAoServiceImpl {
    private final CoAoRepository repository;

    // CHUẨN: Hàm này nhận 3 tham số
    public Page<CoAo> getAll(String keyword, Integer status, Pageable pageable) {
        // Gọi repository.search cũng phải truyền đủ 3 cái
        return repository.search(keyword, status, pageable);
    }

    public CoAo getById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Not found ID: " + id));
    }

    public CoAo create(StoreAttributeRequest req) {
        CoAo entity = new CoAo();
        entity.setMaCoAo("CA" + System.currentTimeMillis());
        entity.setTenCoAo(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }

    public CoAo update(Integer id, StoreAttributeRequest req) {
        CoAo entity = getById(id);
        entity.setTenCoAo(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }

    public void delete(Integer id) {
        CoAo entity = getById(id);
        entity.setTrangThai(0);
        repository.save(entity);
    }
    public ByteArrayInputStream exportToExcel() throws IOException {
        List<CoAo> dataList = repository.findAll(); // Lấy tất cả dữ liệu

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Danh sách Cổ Áo");

            // 1. Tạo Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Mã Cổ Áo", "Tên Cổ Áo", "Trạng Thái", "Ngày Tạo"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // 2. Đổ dữ liệu
            int rowIdx = 1;
            for (CoAo item : dataList) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(item.getId());
                row.createCell(1).setCellValue(item.getMaCoAo() != null ? item.getMaCoAo() : "");
                row.createCell(2).setCellValue(item.getTenCoAo());
                row.createCell(3).setCellValue(item.getTrangThai() == 1 ? "Hoạt động" : "Ngừng");
                row.createCell(4).setCellValue(item.getNgayTao() != null ? item.getNgayTao().toString() : "");
            }

            // Auto size cột
            for(int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}