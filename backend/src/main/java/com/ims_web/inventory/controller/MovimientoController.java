package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Movimiento;
import com.ims_web.inventory.service.MovimientoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService service;

    public MovimientoController(MovimientoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Movimiento> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Movimiento getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Movimiento create(@RequestBody Movimiento movimiento,
                             @RequestHeader("X-User") String currentUser) {
        return service.create(movimiento, currentUser);
    }

    @PutMapping("/{id}")
    public Movimiento update(@PathVariable Long id,
                             @RequestBody Movimiento movimiento,
                             @RequestHeader("X-User") String currentUser) {
        movimiento.setMovimientoId(id);
        return service.update(movimiento, currentUser);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}