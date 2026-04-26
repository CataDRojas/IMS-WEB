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

        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        String[] headers = new String[] {
                "Codigo",
                "Nombre",
                "Precio",
                "Stock",
                "Stock Critico Numero",
                "Cantidad Lote",
                "Categoria",
                "Activo"
        };

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        mapper.mapProductosToRow(sheet, productos);

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }
}