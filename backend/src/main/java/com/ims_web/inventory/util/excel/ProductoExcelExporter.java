package com.ims_web.inventory.util.excel;

import com.ims_web.inventory.dto.ProductoExcelDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductoExcelExporter {

    private final ProductoImportExcelMapper mapper;

    public ProductoExcelExporter(ProductoImportExcelMapper mapper) {
        this.mapper = mapper;
    }

    public Workbook exportProductos(List<ProductoExcelDTO> productos) {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Productos");

        // =========================
        // HEADER STYLE (consistent + reusable idea)
        // =========================
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        // =========================
        // HEADER ROW (STRICT CONTRACT)
        // =========================
        String[] headers = new String[] {
                "Codigo",
                "Nombre",
                "Precio",
                "Stock",
                "Categoria",
                "Cantidad Lote"
        };

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // =========================
        // DATA ROWS
        // =========================
        mapper.mapProductosToRow(sheet, productos);

        // =========================
        // AUTO SIZE (aligned with contract length)
        // =========================
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }
}