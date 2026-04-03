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

    @GetMapping("/{email}")
    public Usuario getByEmail(@PathVariable String email) {
        return service.getByEmail(email);
    }

    @PostMapping
    public Usuario create(@RequestBody Usuario usuario) {
        return service.create(usuario);
    }

    @PutMapping("/{email}")
    public Usuario update(@PathVariable String email, @RequestBody Usuario usuario) {
        return service.update(email, usuario);
    }

    @DeleteMapping("/{email}")
    public void delete(@PathVariable String email) {
        service.delete(email);
    }
}