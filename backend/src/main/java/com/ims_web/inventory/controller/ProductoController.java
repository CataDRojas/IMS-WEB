package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.service.ProductoService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    // -----------------------
    // READ ACCESS
    // -----------------------

    @PreAuthorize("hasAuthority('PRODUCTO_READ')")
    @GetMapping
    public List<Producto> getAll() {
        return service.getAllProductos();
    }

    @PreAuthorize("hasAuthority('PRODUCTO_READ')")
    @GetMapping("/{id}")
    public Producto getById(@PathVariable Long id) {
        return service.getProductoById(id);
    }

    @PreAuthorize("hasAuthority('PRODUCTO_READ')")
    @GetMapping("/codigo/{codigo}")
    public Producto getByCodigo(@PathVariable String codigo) {
        return service.getProductoByCodigo(codigo);
    }

    // -----------------------
    // WRITE ACCESS
    // -----------------------

    @PreAuthorize("hasAuthority('PRODUCTO_MANAGE')")
    @PostMapping
    public Producto create(@RequestBody Producto producto,
                           @RequestHeader("X-User") String currentUser) {
        return service.createProducto(producto, currentUser);
    }

    @PreAuthorize("hasAuthority('PRODUCTO_MANAGE')")
    @PutMapping("/{id}")
    public Producto update(@PathVariable Long id,
                           @RequestBody Producto producto,
                           @RequestHeader("X-User") String currentUser) {
        producto.setProductoId(id);
        return service.updateProducto(producto, currentUser);
    }

    // -----------------------
    // EXCEL IMPORT
    // -----------------------

    @PreAuthorize("hasAuthority('PRODUCTO_MANAGE')")
    @PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importExcel(@RequestParam("file") MultipartFile file) {

        service.importFromExcel(file);

        return ResponseEntity.ok("Excel imported successfully");
    }

    // -----------------------
    // EXCEL EXPORT
    // -----------------------

    @PreAuthorize("hasAuthority('PRODUCTO_READ')")
    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel() {

        Workbook workbook = service.exportToExcel();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            workbook.write(out);
            workbook.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=productos.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Error exporting Excel", e);
        }
    }
}