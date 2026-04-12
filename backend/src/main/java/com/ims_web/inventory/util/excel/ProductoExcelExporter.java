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
        // HEADER ROW
        // =========================
        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("Codigo");
        header.createCell(1).setCellValue("Nombre");
        header.createCell(2).setCellValue("Precio");
        header.createCell(3).setCellValue("Stock");
        header.createCell(4).setCellValue("Categoria");

        // =========================
        // DATA ROWS
        // =========================
        mapper.mapProductosToRow(workbook, sheet, productos);

        // =========================
        // AUTO SIZE (optional but nice)
        // =========================
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }
}