package org.example.yourchoiceshop.controller;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.yourchoiceshop.entity.NhanVien;

public class EmployeeExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<NhanVien> listNhanVien;

    public EmployeeExcelExporter(List<NhanVien> listNhanVien) {
        this.listNhanVien = listNhanVien;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("NhanVien");

        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);

        createCell(row, 0, "ID", style);
        createCell(row, 1, "Mã NV", style);
        createCell(row, 2, "Họ Tên", style);
        createCell(row, 3, "Email", style);
        createCell(row, 4, "SĐT", style);
        createCell(row, 5, "Giới tính", style);
        createCell(row, 6, "Trạng thái", style);
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

    private void writeDataLines() {
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);

        for (NhanVien nv : listNhanVien) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            createCell(row, columnCount++, nv.getId(), style);
            createCell(row, columnCount++, nv.getMaNhanVien(), style);
            createCell(row, columnCount++, nv.getTenNhanVien(), style);
            createCell(row, columnCount++, nv.getEmail(), style);
            createCell(row, columnCount++, nv.getSoDienThoai(), style);
            createCell(row, columnCount++, (nv.getGioiTinh() != null && nv.getGioiTinh()) ? "Nam" : "Nữ", style);
            createCell(row, columnCount++, (nv.getTrangThai() == 1) ? "Hoạt động" : "Ngừng HĐ", style);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}