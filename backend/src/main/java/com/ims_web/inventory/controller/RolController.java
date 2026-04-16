package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Rol;
import com.ims_web.inventory.service.RolService;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('ROLES_MANAGE')")
    public List<Rol> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES_MANAGE')")
    public Rol getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLES_MANAGE')")
    public Rol createOrUpdate(@RequestBody Rol rol) {
        return service.save(rol);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLES_MANAGE')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}