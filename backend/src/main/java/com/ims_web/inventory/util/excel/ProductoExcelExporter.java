package com.ims_web.inventory.util.excel;

import com.ims_web.inventory.dto.ProductoExcelDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductoExcelExporter {

    public Workbook exportProductos(List<ProductoExcelDTO> productos) {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        // Estilo header
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Estilo crítico
        CellStyle criticoStyle = workbook.createCellStyle();
        criticoStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        criticoStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font criticoFont = workbook.createFont();
        criticoFont.setBold(true);
        criticoFont.setColor(IndexedColors.WHITE.getIndex());
        criticoStyle.setFont(criticoFont);

        String[] headers = {
                "Codigo", "Nombre", "Precio", "Stock",
                "Stock Critico Numero", "Cantidad Lote", "Categoria", "Activo"
        };

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIndex = 1;
        for (ProductoExcelDTO dto : productos) {

            Row row = sheet.createRow(rowIndex++);

            boolean critico = dto.getCriticoNumero() != null
                    && dto.getStock() != null
                    && dto.getStock() < dto.getCriticoNumero();

            row.createCell(0).setCellValue(nvl(dto.getCodigo()));
            row.createCell(1).setCellValue(nvl(dto.getNombre()));
            row.createCell(2).setCellValue(
                    dto.getPrecio() != null ? dto.getPrecio().doubleValue() : 0);
            row.createCell(3).setCellValue(
                    dto.getStock() != null ? dto.getStock() : 0);
            row.createCell(4).setCellValue(
                    dto.getCriticoNumero() != null ? dto.getCriticoNumero() : 0);
            row.createCell(5).setCellValue(
                    dto.getCantidadLote() != null ? dto.getCantidadLote() : 1);
            row.createCell(6).setCellValue(nvl(dto.getCategoria()));
            row.createCell(7).setCellValue(
                    dto.getActivo() != null ? dto.getActivo() : true);

            if (critico) {
                row.getCell(3).setCellStyle(criticoStyle); // Stock actual en rojo
            }
        }

        for (int i = 0; i < headers.length; i++)
            sheet.autoSizeColumn(i);

        return workbook;
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }
}