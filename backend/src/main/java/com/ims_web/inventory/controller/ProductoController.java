package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.service.ProductoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService service;

    public ProductoController(ProductoService service) {
        this.service = service;
    }

    // -----------------------
    // GET ALL
    // -----------------------
    @GetMapping
    public List<Producto> getAll() {
        return service.getAllProductos();
    }

    // -----------------------
    // GET BY ID
    // -----------------------
    @GetMapping("/{id}")
    public Producto getById(@PathVariable Long id) {
        return service.getProductoById(id);
    }

    // -----------------------
    // GET BY CODIGO
    // -----------------------
    @GetMapping("/codigo/{codigo}")
    public Producto getByCodigo(@PathVariable String codigo) {
        return service.getProductoByCodigo(codigo);
    }

    // -----------------------
    // CREATE
    // -----------------------
    @PostMapping
    public Producto create(@RequestBody Producto producto) {
        // Automatically set creation timestamp if not set
        if (producto.getProductosFechaCreacion() == null) {
            producto.setProductosFechaCreacion(java.time.LocalDateTime.now());
        }
        return service.createProducto(producto);
    }

    // -----------------------
    // UPDATE
    // -----------------------
    @PutMapping("/{id}")
    public Producto update(@PathVariable Long id, @RequestBody Producto producto) {
        producto.setProductoId(id);
        // Automatically set modification timestamp
        producto.setProductosFechaModif(java.time.LocalDateTime.now());
        return service.updateProducto(producto);
    }
}