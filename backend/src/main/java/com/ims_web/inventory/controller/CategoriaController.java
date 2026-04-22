package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Categoria;
import com.ims_web.inventory.entity.Descuento;
import com.ims_web.inventory.service.CategoriaService;
import com.ims_web.inventory.service.DescuentoService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService service;
    private final DescuentoService descuentoService;

    public CategoriaController(CategoriaService service,
                               DescuentoService descuentoService) {
        this.service = service;
        this.descuentoService = descuentoService;
    }

    // CATEGORIA

    @GetMapping
    @PreAuthorize("hasAuthority('CATEGORIA_READ')")
    public List<Categoria> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORIA_READ')")
    public Categoria getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORIA_MANAGE')")
    public Categoria create(@RequestBody Categoria categoria,
                            @RequestHeader("X-User") String currentUser) {
        return service.createOrUpdate(categoria, currentUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORIA_MANAGE')")
    public Categoria update(@PathVariable Long id,
                            @RequestBody Categoria categoria,
                            @RequestHeader("X-User") String currentUser) {
        categoria.setCategoriaId(id);
        return service.createOrUpdate(categoria, currentUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORIA_MANAGE')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // DESCUENTO

    @GetMapping("/descuentos")
    @PreAuthorize("hasAuthority('CATEGORIA_READ')")
    public List<Descuento> getAllDescuentos() {
        return descuentoService.getAll();
    }

    @GetMapping("/descuentos/active")
    @PreAuthorize("hasAuthority('CATEGORIA_READ')")
    public List<Descuento> getActiveDescuentos() {
        return descuentoService.getActive();
    }

    @GetMapping("/descuentos/{id}")
    @PreAuthorize("hasAuthority('CATEGORIA_READ')")
    public Descuento getDescuentoById(@PathVariable Long id) {
        return descuentoService.getById(id);
    }
}