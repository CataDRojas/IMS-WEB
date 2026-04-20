package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.MovimientoDetalle;
import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.entity.MovimientoLugar;
import com.ims_web.inventory.service.MovimientoDetalleService;
import com.ims_web.inventory.service.ProductoService;
import com.ims_web.inventory.service.MovimientoLugarService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimiento-detalles")
public class MovimientoDetalleController {

    private final MovimientoDetalleService service;

    // 🔥 ADDED SERVICES
    private final ProductoService productoService;
    private final MovimientoLugarService movimientoLugarService;

    public MovimientoDetalleController(
            MovimientoDetalleService service,
            ProductoService productoService,
            MovimientoLugarService movimientoLugarService
    ) {
        this.service = service;
        this.productoService = productoService;
        this.movimientoLugarService = movimientoLugarService;
    }

    // -----------------------
    // READ ACCESS
    // -----------------------

    @PreAuthorize("hasAuthority('MOVIMIENTO_READ')")
    @GetMapping
    public List<MovimientoDetalle> getAll() {
        return service.getAllDetalles();
    }

    @PreAuthorize("hasAuthority('MOVIMIENTO_READ')")
    @GetMapping("/{id}")
    public MovimientoDetalle getById(@PathVariable Long id) {
        return service.getDetalleById(id);
    }

    // =========================
    // 🔥 PRODUCT LOOKUPS
    // =========================

    @PreAuthorize("hasAuthority('MOVIMIENTO_READ')")
    @GetMapping("/productos")
    public List<Producto> getAllProductos() {
        return productoService.getAllProductos();
    }

    @PreAuthorize("hasAuthority('MOVIMIENTO_READ')")
    @GetMapping("/productos/{id}")
    public Producto getProductoById(@PathVariable Long id) {
        return productoService.getProductoById(id);
    }

    @PreAuthorize("hasAuthority('MOVIMIENTO_READ')")
    @GetMapping("/productos/codigo/{codigo}")
    public Producto getProductoByCodigo(@PathVariable String codigo) {
        return productoService.getProductoByCodigo(codigo);
    }

    // =========================
    // 🔥 MOVIMIENTO LUGAR LOOKUPS
    // =========================

    @PreAuthorize("hasAuthority('MOVIMIENTO_READ')")
    @GetMapping("/lugares")
    public List<MovimientoLugar> getAllLugares() {
        return movimientoLugarService.getAll();
    }

    @PreAuthorize("hasAuthority('MOVIMIENTO_READ')")
    @GetMapping("/lugares/active")
    public List<MovimientoLugar> getActiveLugares() {
        return movimientoLugarService.getActive();
    }

    @PreAuthorize("hasAuthority('MOVIMIENTO_READ')")
    @GetMapping("/lugares/{id}")
    public MovimientoLugar getLugarById(@PathVariable Long id) {
        return movimientoLugarService.getById(id);
    }

    // -----------------------
    // WRITE ACCESS
    // -----------------------

    @PreAuthorize("hasAuthority('MOVIMIENTO_MANAGE')")
    @PostMapping("/movimiento/{movimientoId}")
    public MovimientoDetalle create(@PathVariable Long movimientoId,
                                    @RequestBody MovimientoDetalle detalle) {
        return service.createDetalle(movimientoId, detalle);
    }

    @PreAuthorize("hasAuthority('MOVIMIENTO_MANAGE')")
    @PutMapping("/{id}")
    public MovimientoDetalle update(@PathVariable Long id,
                                    @RequestBody MovimientoDetalle detalle) {
        detalle.setMovimientoDetalleId(id);
        return service.updateDetalle(detalle);
    }

    @PreAuthorize("hasAuthority('MOVIMIENTO_MANAGE')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteDetalle(id);
    }
}