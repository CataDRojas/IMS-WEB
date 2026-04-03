package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Rol;
import com.ims_web.inventory.entity.Usuario;
import com.ims_web.inventory.repository.RolRepository;
import com.ims_web.inventory.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .orElseThrow(() -> new RuntimeException("Rol not found"));
    }

    @Transactional
    public Rol save(Rol rol) {
        validateRol(rol);

        // Normalize name
        String cleanName = rol.getRolNombre().trim();
        rol.setRolNombre(cleanName);

        // Check uniqueness for new or updated roles
        boolean nameExists = repo.existsByRolNombre(cleanName)
                && (rol.getRolId() == null || !repo.findByRolNombre(cleanName).getRolId().equals(rol.getRolId()));
        if (nameExists) {
            throw new RuntimeException("RolNombre must be unique");
        }

        return repo.save(rol);
    }

    @Transactional
    public void delete(Long id) {
        Rol rol = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol not found"));

        // prevent deleting a role assigned to users
        boolean inUse = usuarioRepo.existsByRol(rol);
        if (inUse) {
            throw new RuntimeException("Cannot delete Rol: assigned to one or more Usuarios");
        }

        repo.delete(rol);
    }

    private void validateRol(Rol rol) {
        if (rol.getRolNombre() == null || rol.getRolNombre().isBlank()) {
            throw new RuntimeException("RolNombre is required");
        }
    }
}