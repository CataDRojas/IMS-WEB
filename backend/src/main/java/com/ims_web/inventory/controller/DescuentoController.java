package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Descuento;
import com.ims_web.inventory.service.DescuentoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/descuentos")
public class DescuentoController {

    private final DescuentoService service;

    public DescuentoController(DescuentoService service) {
        this.service = service;
    }

    // =========================
    // READ
    // =========================

    @GetMapping
    @PreAuthorize("hasAuthority('DESCUENTO_READ')")
    public List<Descuento> getAll() {
        return service.getAll();
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('DESCUENTO_READ')")
    public List<Descuento> getActive() {
        return service.getActive();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DESCUENTO_READ')")
    public Descuento getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // =========================
    // CREATE
    // =========================

    @PostMapping
    @PreAuthorize("hasAuthority('DESCUENTO_MANAGE')")
    public Descuento create(@RequestBody Descuento descuento,
                            @RequestHeader("X-User") String currentUser) {

        normalize(descuento);
        return service.createDescuento(descuento, currentUser);
    }

    // =========================
    // UPDATE
    // =========================

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DESCUENTO_MANAGE')")
    public Descuento update(@PathVariable Long id,
                            @RequestBody Descuento descuento,
                            @RequestHeader("X-User") String currentUser) {

        descuento.setDescuentoId(id);
        normalize(descuento);

        return service.updateDescuento(descuento, currentUser);
    }

    // =========================
    // DELETE
    // =========================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DESCUENTO_MANAGE')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // =========================
    // SAFETY LAYER (NEW)
    // =========================

    private void normalize(Descuento d) {

        // ensures new field is never accidentally nullified on partial updates
        if (d.getDescuentoValorSecundario() != null &&
                d.getDescuentoValorSecundario().doubleValue() == 0.0) {
            d.setDescuentoValorSecundario(null);
        }

        // optional: trim name safety
        if (d.getDescuentoNombre() != null) {
            d.setDescuentoNombre(d.getDescuentoNombre().trim());
        }
    }
}