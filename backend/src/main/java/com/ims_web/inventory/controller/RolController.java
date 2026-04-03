package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Rol;
import com.ims_web.inventory.service.RolService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    private final RolService service;

    public RolController(RolService service) {
        this.service = service;
    }

    @GetMapping
    public List<Rol> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Rol getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Rol createOrUpdate(@RequestBody Rol rol) {
        return service.save(rol);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}