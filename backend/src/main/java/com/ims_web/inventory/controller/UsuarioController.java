package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Usuario;
import com.ims_web.inventory.entity.Rol;
import com.ims_web.inventory.service.UsuarioService;
import com.ims_web.inventory.service.RolService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;
    private final RolService rolService;

    public UsuarioController(UsuarioService service, RolService rolService) {
        this.service = service;
        this.rolService = rolService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIOS_MANAGE')")
    public List<Usuario> getAll() {
        return service.getAll();
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('USUARIOS_MANAGE')")
    public List<Usuario> getActive() {
        return service.getActive();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS_MANAGE')")
    public Usuario getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/by-email/{email}")
    @PreAuthorize("hasAuthority('USUARIOS_MANAGE')")
    public Usuario getByEmail(@PathVariable String email) {
        return service.getByEmail(email);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USUARIOS_MANAGE')")
    public Usuario create(@RequestBody Usuario usuario) {
        return service.create(usuario);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS_MANAGE')")
    public Usuario update(@PathVariable Long id, @RequestBody Usuario usuario) {
        return service.update(id, usuario);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS_MANAGE')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // =========================
    // 🔥 ADD THIS ONLY
    // =========================
    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('USUARIOS_MANAGE')")
    public List<Rol> getRolesForUsuarios() {
        return rolService.getAll();
    }
}