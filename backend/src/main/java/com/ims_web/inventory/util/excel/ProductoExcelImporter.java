package com.ims_web.inventory.util.excel;

import com.ims_web.inventory.dto.ProductoExcelDTO;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class ProductoExcelImporter {

    private final ProductoImportExcelMapper mapper;

    public ProductoExcelImporter(ProductoImportExcelMapper mapper) {
        this.mapper = mapper;
    }

    public List<ProductoExcelDTO> importProductos(InputStream inputStream) {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            return mapper.mapProductos(sheet);

        } catch (Exception e) {
            throw new RuntimeException("Error reading Excel file", e);
        }
    }
}