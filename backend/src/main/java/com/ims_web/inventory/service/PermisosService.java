package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Permisos;
import com.ims_web.inventory.repository.PermisosRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PermisosService {

    private final PermisosRepository repo;

    public PermisosService(PermisosRepository repo) {
        this.repo = repo;
    }

    public List<Permisos> getAll() {
        return repo.findAll();
    }

    public Permisos getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permiso not found"));
    }

    public Optional<Permisos> findByPermisosNombreIgnoreCase(String name) {
        return repo.findByPermisosNombreIgnoreCase(name);
    }

    @Transactional
    public Permisos create(Permisos permiso) {

        if (permiso.getPermisosNombre() == null || permiso.getPermisosNombre().isBlank()) {
            throw new IllegalArgumentException("PermisosNombre is required");
        }

        String cleanName = permiso.getPermisosNombre().trim();
        permiso.setPermisosNombre(cleanName);

        boolean exists = repo.existsByPermisosNombreIgnoreCase(cleanName);
        if (exists) {
            throw new IllegalArgumentException("Permiso already exists");
        }

        return repo.save(permiso);
    }

    @Transactional
    public void delete(Long id) {
        Permisos permiso = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permiso not found"));

        repo.delete(permiso);
    }
}