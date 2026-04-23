package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Movimiento;
import com.ims_web.inventory.service.MovimientoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService service;

    public MovimientoController(MovimientoService service) {
        this.service = service;
    }


    @PreAuthorize("hasAuthority('MOVIMIENTO_READ')")
    @GetMapping
    public List<Movimiento> getAll() {
        return service.getAll();
    }

    @PreAuthorize("hasAuthority('MOVIMIENTO_READ')")
    @GetMapping("/{id}")
    public Movimiento getById(@PathVariable Long id) {
        return service.getById(id);
    }


    @PreAuthorize("hasAuthority('MOVIMIENTO_MANAGE')")
    @PostMapping
    public Movimiento create(@RequestBody Movimiento movimiento,
                             @RequestHeader("X-User") String currentUser) {
        return service.create(movimiento, currentUser);
    }

    @PreAuthorize("hasAuthority('MOVIMIENTO_MANAGE')")
    @PutMapping("/{id}")
    public Movimiento update(@PathVariable Long id,
                             @RequestBody Movimiento movimiento,
                             @RequestHeader("X-User") String currentUser) {
        movimiento.setMovimientoId(id);
        return service.update(movimiento, currentUser);
    }

    @PreAuthorize("hasAuthority('MOVIMIENTO_MANAGE')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}