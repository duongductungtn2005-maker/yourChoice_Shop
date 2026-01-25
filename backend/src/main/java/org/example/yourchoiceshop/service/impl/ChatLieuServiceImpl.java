package org.example.yourchoiceshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.dto.request.StoreAttributeRequest;
import org.example.yourchoiceshop.entity.ChatLieu;
import org.example.yourchoiceshop.repository.ChatLieuRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
@Service
@RequiredArgsConstructor
public class ChatLieuServiceImpl {
    private final ChatLieuRepository repository;

    public Page<ChatLieu> getAll(String keyword, Integer status, Pageable pageable) {
        return repository.search(keyword, status, pageable);
    }

    public ChatLieu getById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy Chất liệu ID: " + id));
    }

    public ChatLieu create(StoreAttributeRequest req) {
        ChatLieu entity = new ChatLieu();
        entity.setMaChatLieu("CL" + System.currentTimeMillis());
        entity.setTenChatLieu(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }

    public ChatLieu update(Integer id, StoreAttributeRequest req) {
        ChatLieu entity = getById(id);
        entity.setTenChatLieu(req.getTen());
        entity.setTrangThai(req.getTrangThai());
        return repository.save(entity);
    }

    public void delete(Integer id) {
        ChatLieu entity = getById(id);
        entity.setTrangThai(0);
        repository.save(entity);
    }
    public ByteArrayInputStream exportToExcel() throws IOException {
        List<ChatLieu> dataList = repository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Danh sách Chất Liệu");

            // 1. Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Mã Chất Liệu", "Tên Chất Liệu", "Trạng Thái", "Ngày Tạo"};

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
            for (ChatLieu item : dataList) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(item.getId());
                row.createCell(1).setCellValue(item.getMaChatLieu() != null ? item.getMaChatLieu() : "");
                row.createCell(2).setCellValue(item.getTenChatLieu());
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