package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Usuario;
import com.ims_web.inventory.service.UsuarioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<Usuario> getAll() {
        return service.getAll();
    }

    @GetMapping("/active")
    public List<Usuario> getActive() {
        return service.getActive();
    }

    @GetMapping("/{id}")
    public Usuario getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/by-email/{email}")
    public Usuario getByEmail(@PathVariable String email) {
        return service.getByEmail(email);
    }

    @PostMapping
    public Usuario create(@RequestBody Usuario usuario) {
        return service.create(usuario);
    }

    @PutMapping("/{id}")
    public Usuario update(@PathVariable Long id, @RequestBody Usuario usuario) {
        return service.update(id, usuario);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}