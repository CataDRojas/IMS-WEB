package com.ims_web.inventory.controller;

import com.ims_web.inventory.dto.MovimientoDetalleRequestDTO;
import com.ims_web.inventory.dto.MovimientoDetalleResponseDTO;
import com.ims_web.inventory.entity.Configuracion;
import com.ims_web.inventory.entity.Descuento;
import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.entity.MovimientoLugar;
import com.ims_web.inventory.service.ConfiguracionService;
import com.ims_web.inventory.service.DescuentoService;
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
    private final ConfiguracionService configuracionService;
    private final DescuentoService descuentoService;
    private final ProductoService productoService;
    private final MovimientoLugarService movimientoLugarService;


    public MovimientoDetalleController(
            MovimientoDetalleService service,
            ConfiguracionService configuracionService,
            DescuentoService descuentoService,
            ProductoService productoService,
            MovimientoLugarService movimientoLugarService
    ) {
        this.service = service;
        this.configuracionService = configuracionService;
        this.descuentoService = descuentoService;
        this.productoService = productoService;
        this.movimientoLugarService = movimientoLugarService;
    }

    // =========================
    // CONFIGURACION
    // =========================

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/configuracion")
    public Configuracion getConfiguracion() {
        return configuracionService.getConfiguracion();
    }

    // =========================
    // DESCUENTOS
    // =========================

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/descuentos")
    public List<Descuento> getDescuentosActivos() {
        return descuentoService.getActive();
    }

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/descuentos/{id}")
    public Descuento getDescuentoById(@PathVariable Long id) {
        return descuentoService.getById(id);
    }

    // =========================
    // PRODUCTOS
    // =========================

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/productos")
    public List<Producto> getProductos() {
        return productoService.getAllProductos();
    }

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/productos/{id}")
    public Producto getProductoById(@PathVariable Long id) {
        return productoService.getProductoById(id);
    }

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/productos/codigo/{codigo}")
    public Producto getProductoByCodigo(@PathVariable String codigo) {
        return productoService.getProductoByCodigo(codigo);
    }

    // =========================
    // READ DETALLES
    // =========================

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping
    public List<MovimientoDetalleResponseDTO> getAll() {
        return service.getAllDetalles();
    }

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/{id}")
    public MovimientoDetalleResponseDTO getById(@PathVariable Long id) {
        return service.getDetalleById(id);
    }

    // =========================
    // CREATE DETAIL (STOCK-SAFE)
    // =========================

    @PreAuthorize("hasAnyAuthority('VENTA_MANAGE','INVENTARIO_MANAGE')")
    @PostMapping("/movimiento/{movimientoId}")
    public MovimientoDetalleResponseDTO create(
            @PathVariable Long movimientoId,
            @RequestBody MovimientoDetalleRequestDTO detalle
    ) {
        return service.createDetalle(movimientoId, detalle);
    }

    // =========================
    // UPDATE DETAIL (STOCK VALIDATED)
    // =========================

    @PreAuthorize("hasAnyAuthority('VENTA_MANAGE','INVENTARIO_MANAGE')")
    @PutMapping("/{id}")
    public MovimientoDetalleResponseDTO update(
            @PathVariable Long id,
            @RequestBody MovimientoDetalleRequestDTO detalle
    ) {
        return service.updateDetalle(id, detalle);
    }

    // =========================
    // DELETE DETAIL (STOCK VALIDATED)
    // =========================

    @PreAuthorize("hasAnyAuthority('VENTA_MANAGE','INVENTARIO_MANAGE')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteDetalle(id);
    }
    // =========================
    // MOVIMIENTO LUGAR (READ BRIDGE)
    // =========================

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/movimiento-lugares")
    public List<MovimientoLugar> getMovimientoLugares() {
        return movimientoLugarService.getAll();
    }

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/movimiento-lugares/active")
    public List<MovimientoLugar> getMovimientoLugaresActive() {
        return movimientoLugarService.getActive();
    }

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/movimiento-lugares/{id}")
    public MovimientoLugar getMovimientoLugarById(@PathVariable Long id) {
        return movimientoLugarService.getById(id);
    }
}