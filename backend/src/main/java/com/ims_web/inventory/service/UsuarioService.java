package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Usuario;
import com.ims_web.inventory.entity.Rol;
import com.ims_web.inventory.repository.UsuarioRepository;
import com.ims_web.inventory.repository.RolRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final RolRepository rolRepo;

    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repo, RolRepository rolRepo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.rolRepo = rolRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> getAll() {
        return repo.findAll();
    }

    public List<Usuario> getActive() {
        return repo.findByUsuarioActivoTrue();
    }

    public Usuario getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario with ID " + id + " not found"));
    }

    public Usuario getByEmail(String email) {
        String cleanEmail = normalizeEmail(email);
        return repo.findByUsuarioEmail(cleanEmail)
                .orElseThrow(() -> new EntityNotFoundException("Usuario with email " + email + " not found"));
    }

    // =========================
    // NEW: SAFE LOOKUP FOR INITIALIZER
    // =========================
    public Optional<Usuario> findByUsuarioEmailIgnoreCase(String email) {
        return repo.findByUsuarioEmailIgnoreCase(normalizeEmail(email));
    }

    @Transactional
    public Usuario create(Usuario usuario) {
        validateUsuario(usuario, true);

        String cleanEmail = normalizeEmail(usuario.getUsuarioEmail());
        if (repo.existsByUsuarioEmail(cleanEmail)) {
            throw new IllegalArgumentException("Usuario with this email already exists");
        }
        usuario.setUsuarioEmail(cleanEmail);

        Rol rol = rolRepo.findById(usuario.getRol().getRolId())
                .orElseThrow(() -> new EntityNotFoundException("Assigned role does not exist"));
        usuario.setRol(rol);

        usuario.setUsuarioPassword(passwordEncoder.encode(usuario.getUsuarioPassword()));
        usuario.setUsuarioFechaCreacion(LocalDateTime.now());

        return repo.save(usuario);
    }

    @Transactional
    public Usuario update(Long id, Usuario usuario) {
        Usuario existing = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario with ID " + id + " not found"));

        validateUsuario(usuario, false);

        Rol rol = rolRepo.findById(usuario.getRol().getRolId())
                .orElseThrow(() -> new EntityNotFoundException("Assigned role does not exist"));
        existing.setRol(rol);

        existing.setUsuarioNombre(usuario.getUsuarioNombre());
        existing.setUsuarioRun(usuario.getUsuarioRun());
        existing.setUsuarioDV(usuario.getUsuarioDV());
        existing.setUsuarioActivo(usuario.getUsuarioActivo());
        existing.setUsuarioFechaModif(LocalDateTime.now());

        if (usuario.getUsuarioPassword() != null && !usuario.getUsuarioPassword().isBlank()) {
            existing.setUsuarioPassword(passwordEncoder.encode(usuario.getUsuarioPassword()));
        }

        String cleanEmail = normalizeEmail(usuario.getUsuarioEmail());
        if (!cleanEmail.equals(existing.getUsuarioEmail())) {
            if (repo.existsByUsuarioEmail(cleanEmail)) {
                throw new IllegalArgumentException("Usuario with this email already exists");
            }
            existing.setUsuarioEmail(cleanEmail);
        }

        return repo.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Usuario usuario = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario with ID " + id + " not found"));
        repo.delete(usuario);
    }

    // =======================
    // Validation & Helpers
    // =======================
    private void validateUsuario(Usuario usuario, boolean requirePassword) {
        if (usuario.getUsuarioNombre() == null || usuario.getUsuarioNombre().isBlank()) {
            throw new IllegalArgumentException("UsuarioNombre is required");
        }
        if (requirePassword && (usuario.getUsuarioPassword() == null || usuario.getUsuarioPassword().isBlank())) {
            throw new IllegalArgumentException("UsuarioPassword is required");
        }
        if (usuario.getRol() == null || usuario.getRol().getRolId() == null) {
            throw new IllegalArgumentException("Usuario must have a valid role assigned");
        }
        if (usuario.getUsuarioEmail() == null || usuario.getUsuarioEmail().isBlank()) {
            throw new IllegalArgumentException("UsuarioEmail is required");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}