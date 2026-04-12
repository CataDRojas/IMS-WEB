package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.MovimientoLugar;
import com.ims_web.inventory.service.MovimientoLugarService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimiento-lugares")
public class MovimientoLugarController {

    private final MovimientoLugarService service;

    public MovimientoLugarController(MovimientoLugarService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MOVIMIENTO_LUGAR_MANAGE')")
    public List<MovimientoLugar> getAll() {
        return service.getAll();
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('MOVIMIENTO_LUGAR_MANAGE')")
    public List<MovimientoLugar> getActive() {
        return service.getActive();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MOVIMIENTO_LUGAR_MANAGE')")
    public MovimientoLugar getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MOVIMIENTO_LUGAR_MANAGE')")
    public MovimientoLugar createOrUpdate(
            @RequestBody MovimientoLugar lugar,
            @RequestHeader("X-User") String currentUser
    ) {
        return service.createOrUpdate(lugar, currentUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MOVIMIENTO_LUGAR_MANAGE')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/soft-delete")
    @PreAuthorize("hasAuthority('MOVIMIENTO_LUGAR_MANAGE')")
    public MovimientoLugar softDelete(
            @PathVariable Long id,
            @RequestHeader("X-User") String currentUser
    ) {
        return service.softDelete(id, currentUser);
    }
}