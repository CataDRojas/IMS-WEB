package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Permisos;
import com.ims_web.inventory.service.PermisosService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permisos")
public class PermisosController {

    private final PermisosService service;

    public PermisosController(PermisosService service) {
        this.service = service;
    }

    @GetMapping
    public List<Permisos> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Permisos getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Permisos create(@RequestBody Permisos permiso) {
        return service.create(permiso);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}