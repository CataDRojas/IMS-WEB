package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Categoria;
import com.ims_web.inventory.service.CategoriaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Categoria> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Categoria getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Categoria create(@RequestBody Categoria categoria,
                            @RequestHeader("X-User") String currentUser) {
        return service.createOrUpdate(categoria, currentUser);
    }

    @PutMapping("/{id}")
    public Categoria update(@PathVariable Long id,
                            @RequestBody Categoria categoria,
                            @RequestHeader("X-User") String currentUser) {
        categoria.setCategoriaId(id);
        return service.createOrUpdate(categoria, currentUser);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}