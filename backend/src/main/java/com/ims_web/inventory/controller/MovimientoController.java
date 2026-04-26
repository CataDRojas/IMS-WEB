package com.ims_web.inventory.controller;

import com.ims_web.inventory.dto.MovimientoResponseDTO;
import com.ims_web.inventory.entity.Configuracion;
import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.service.ConfiguracionService;
import com.ims_web.inventory.service.MovimientoService;
import com.ims_web.inventory.service.ProductoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final ConfiguracionService configuracionService;
    private final ProductoService productoService;

    public MovimientoController(
            MovimientoService movimientoService,
            ConfiguracionService configuracionService,
            ProductoService productoService
    ) {
        this.movimientoService = movimientoService;
        this.configuracionService = configuracionService;
        this.productoService = productoService;
    }

    // =========================
    // MOVIMIENTOS
    // =========================

    @PreAuthorize("hasAuthority('MOVIMIENTO_READ')")
    @GetMapping
    public List<MovimientoResponseDTO> getAll() {
        return movimientoService.getAll();
    }

    @PreAuthorize("hasAuthority('MOVIMIENTO_READ')")
    @GetMapping("/{id}")
    public MovimientoResponseDTO getById(@PathVariable Long id) {
        return movimientoService.getById(id);
    }

    @PreAuthorize("hasAuthority('MOVIMIENTO_MANAGE')")
    @PostMapping
    public MovimientoResponseDTO create(
            @RequestBody com.ims_web.inventory.entity.Movimiento movimiento,
            @RequestHeader("X-User") String currentUser
    ) {
        return movimientoService.create(movimiento, currentUser);
    }

    @PreAuthorize("hasAuthority('MOVIMIENTO_MANAGE')")
    @PutMapping("/{id}")
    public MovimientoResponseDTO update(
            @PathVariable Long id,
            @RequestBody com.ims_web.inventory.entity.Movimiento movimiento,
            @RequestHeader("X-User") String currentUser
    ) {
        movimiento.setMovimientoId(id);
        return movimientoService.update(movimiento, currentUser);
    }

    @PreAuthorize("hasAuthority('MOVIMIENTO_MANAGE')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        movimientoService.delete(id);
    }

    // =========================
    // CONFIRMAR MOVIMIENTO
    // =========================

    @PreAuthorize("hasAuthority('MOVIMIENTO_MANAGE')")
    @PostMapping("/{id}/confirmar")
    public MovimientoResponseDTO confirmar(
            @PathVariable Long id,
            @RequestHeader("X-User") String currentUser
    ) {
        return movimientoService.confirmarMovimiento(id, currentUser);
    }

    // =========================
    // CONFIGURACION
    // =========================

    @PreAuthorize("hasAuthority('MOVIMIENTO_READ')")
    @GetMapping("/configuracion")
    public Configuracion getConfiguracionForMovimientos() {
        return configuracionService.getConfiguracion();
    }

    // =========================
    // PRODUCTOS
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
}