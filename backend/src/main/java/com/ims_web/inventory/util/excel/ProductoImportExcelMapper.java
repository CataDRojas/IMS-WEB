package com.ims_web.inventory.util.excel;

import com.ims_web.inventory.dto.ProductoExcelDTO;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductoImportExcelMapper {

    // =========================
    // EXCEL → DTO
    // =========================
    public List<ProductoExcelDTO> mapProductos(Sheet sheet) {
        List<ProductoExcelDTO> productos = new ArrayList<>();

        // start at row 1 (row 0 = headers)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (row == null) continue;

            ProductoExcelDTO dto = new ProductoExcelDTO();

            dto.setCodigo(getString(row.getCell(0)));
            dto.setNombre(getString(row.getCell(1)));
            dto.setPrecio(getBigDecimal(row.getCell(2)));
            dto.setStock(getInteger(row.getCell(3)));
            dto.setCategoria(getString(row.getCell(4)));

            // skip empty rows
            if (dto.getCodigo() == null && dto.getNombre() == null) {
                continue;
            }

            productos.add(dto);
        }

        return productos;
    }

    // =========================
    // DTO → EXCEL
    // =========================
    public void mapProductosToRow(Workbook workbook, Sheet sheet, List<ProductoExcelDTO> data) {

        int rowIndex = 1;

        for (ProductoExcelDTO dto : data) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(nvl(dto.getCodigo()));
            row.createCell(1).setCellValue(nvl(dto.getNombre()));

            if (dto.getPrecio() != null) {
                row.createCell(2).setCellValue(dto.getPrecio().doubleValue());
            }

            if (dto.getStock() != null) {
                row.createCell(3).setCellValue(dto.getStock());
            }

            row.createCell(4).setCellValue(nvl(dto.getCategoria()));
        }
    }

    // =========================
    // HELPERS (dirty Excel survival kit)
    // =========================

    private String getString(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private BigDecimal getBigDecimal(Cell cell) {
        if (cell == null) return null;

        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING -> new BigDecimal(cell.getStringCellValue().trim());
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private Integer getInteger(Cell cell) {
        if (cell == null) return null;

        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (int) cell.getNumericCellValue();
                case STRING -> Integer.parseInt(cell.getStringCellValue().trim());
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}