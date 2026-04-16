package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Rol;
import com.ims_web.inventory.repository.RolRepository;
import com.ims_web.inventory.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RolService {

    private final RolRepository repo;
    private final UsuarioRepository usuarioRepo;

    public RolService(RolRepository repo, UsuarioRepository usuarioRepo) {
        this.repo = repo;
        this.usuarioRepo = usuarioRepo;
    }

    public List<Rol> getAll() {
        return repo.findAll();
    }

    public Rol getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol with ID " + id + " not found"));
    }

    // =========================
    // NEW: SAFE LOOKUP FOR DATA INITIALIZER
    // =========================
    public Optional<Rol> findByRolNombreIgnoreCase(String name) {
        return repo.findByRolNombreIgnoreCase(name);
    }

    @Transactional
    public Rol save(Rol rol) {
        validateRol(rol);

        String cleanName = rol.getRolNombre().trim();
        rol.setRolNombre(cleanName);

        Long idToExclude = rol.getRolId() == null ? -1L : rol.getRolId();
        boolean nameExists = repo.existsByRolNombreIgnoreCaseAndRolIdNot(cleanName, idToExclude);
        if (nameExists) {
            throw new IllegalArgumentException("RolNombre must be unique");
        }

        return repo.save(rol);
    }

    @Transactional
    public void delete(Long id) {
        Rol rol = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol with ID " + id + " not found"));

        boolean inUse = usuarioRepo.existsByRol(rol);
        if (inUse) {
            throw new IllegalArgumentException("Cannot delete Rol: assigned to one or more Usuarios");
        }

        repo.delete(rol);
    }

    private void validateRol(Rol rol) {
        if (rol.getRolNombre() == null || rol.getRolNombre().isBlank()) {
            throw new IllegalArgumentException("RolNombre is required");
        }
    }
}