package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.service.ProductoService;
import com.ims_web.inventory.service.CategoriaService;
import com.ims_web.inventory.service.DescuentoService;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ims_web.inventory.dto.ProductoDetalleDTO;



import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    // 🔥 ADDED SERVICES (needed for aggregation endpoint)
    private final CategoriaService categoriaService;
    private final DescuentoService descuentoService;

    public ProductoController(
            ProductoService service,
            CategoriaService categoriaService,
            DescuentoService descuentoService
    ) {
        this.service = service;
        this.categoriaService = categoriaService;
        this.descuentoService = descuentoService;
    }


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

    // EXCEL IMPORT

    @PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCTO_MANAGE')")
    public ResponseEntity<String> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User") String currentUser
    ) {
        service.importFromExcel(file, currentUser);
        return ResponseEntity.ok("Excel imported successfully");
    }

    // EXCEL EXPORT

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


    @PreAuthorize("hasAuthority('PRODUCTO_READ')")
    @GetMapping("/ui-data")
    public ResponseEntity<Map<String, Object>> getProductoUiData() {

        Map<String, Object> response = new HashMap<>();

        response.put("productos", service.getAllProductos());
        response.put("categorias", categoriaService.getAll());
        response.put("descuentos", descuentoService.getActive());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}/detalle")
    public ProductoDetalleDTO getDetalle(@PathVariable Long id) {
        return service.getProductoDetalle(id);
    }
}