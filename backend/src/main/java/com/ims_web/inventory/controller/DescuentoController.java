package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Descuento;
import com.ims_web.inventory.service.DescuentoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/descuentos")
public class DescuentoController {

    private final DescuentoService service;

    public DescuentoController(DescuentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Descuento> getAll() {
        return service.getAll();
    }

    @GetMapping("/active")
    public List<Descuento> getActive() {
        return service.getActive();
    }

    @GetMapping("/{id}")
    public Descuento getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Descuento create(@RequestBody Descuento descuento,
                            @RequestHeader("X-User") String currentUser) {
        return service.createDescuento(descuento, currentUser);
    }

    @PutMapping("/{id}")
    public Descuento update(@PathVariable Long id,
                            @RequestBody Descuento descuento,
                            @RequestHeader("X-User") String currentUser) {
        descuento.setDescuentoId(id);
        return service.updateDescuento(descuento, currentUser);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}