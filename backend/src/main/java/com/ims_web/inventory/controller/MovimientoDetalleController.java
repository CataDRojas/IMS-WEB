package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.MovimientoDetalle;
import com.ims_web.inventory.service.MovimientoDetalleService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimiento-detalles")
public class MovimientoDetalleController {

    private final MovimientoDetalleService service;

    public MovimientoDetalleController(MovimientoDetalleService service) {
        this.service = service;
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