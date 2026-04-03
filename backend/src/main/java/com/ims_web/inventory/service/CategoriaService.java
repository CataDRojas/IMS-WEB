package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Categoria;
import com.ims_web.inventory.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repo;

    public CategoriaService(CategoriaRepository repo) {
        this.repo = repo;
    }

    public List<Categoria> getAll() {
        return repo.findAll();
    }

    public Categoria getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria not found"));
    }

    public Categoria createOrUpdate(Categoria categoria) {
        return repo.save(categoria);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}