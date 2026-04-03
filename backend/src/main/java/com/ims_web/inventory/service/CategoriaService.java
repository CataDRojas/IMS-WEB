package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Categoria;
import com.ims_web.inventory.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Transactional
    public Categoria createOrUpdate(Categoria categoria, String currentUser) {

        LocalDateTime now = LocalDateTime.now();

        if (categoria.getCategoriaId() == null) {
            // New Categoria → set creation audit
            categoria.setCategoriaUsuarioCreacion(currentUser);
            categoria.setCategoriaFechaCreacion(now);
        } else {
            // Existing Categoria → set modification audit
            categoria.setCategoriaUsuarioModif(currentUser);
            categoria.setCategoriaFechaModif(now);
        }

        return repo.save(categoria);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}