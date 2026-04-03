package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Usuario;
import com.ims_web.inventory.entity.Rol;
import com.ims_web.inventory.repository.UsuarioRepository;
import com.ims_web.inventory.repository.RolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final RolRepository rolRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repo, RolRepository rolRepo) {
        this.repo = repo;
        this.rolRepo = rolRepo;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public List<Usuario> getAll() {
        return repo.findAll();
    }

    public List<Usuario> getActive() {
        return repo.findByUsuarioActivoTrue();
    }

    public Usuario getByEmail(String email) {
        String cleanEmail = normalizeEmail(email);
        return repo.findById(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Usuario not found"));
    }

    @Transactional
    public Usuario create(Usuario usuario) {
        validateUsuario(usuario);

        String cleanEmail = normalizeEmail(usuario.getUsuarioEmail());
        if (repo.existsById(cleanEmail)) {
            throw new RuntimeException("Usuario with this email already exists");
        }
        usuario.setUsuarioEmail(cleanEmail);

        Rol rol = rolRepo.findById(usuario.getRol().getRolId())
                .orElseThrow(() -> new RuntimeException("Assigned role does not exist"));
        usuario.setRol(rol);

        usuario.setUsuarioPassword(passwordEncoder.encode(usuario.getUsuarioPassword()));
        usuario.setUsuarioFechaCreacion(LocalDateTime.now());

        return repo.save(usuario);
    }

    @Transactional
    public Usuario update(String email, Usuario usuario) {
        String cleanEmail = normalizeEmail(email);
        Usuario existing = repo.findById(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Usuario not found"));

        validateUsuario(usuario);

        Rol rol = rolRepo.findById(usuario.getRol().getRolId())
                .orElseThrow(() -> new RuntimeException("Assigned role does not exist"));
        existing.setRol(rol);

        existing.setUsuarioNombre(usuario.getUsuarioNombre());
        existing.setUsuarioRun(usuario.getUsuarioRun());
        existing.setUsuarioDV(usuario.getUsuarioDV());

        // Only update password if provided
        if (usuario.getUsuarioPassword() != null && !usuario.getUsuarioPassword().isBlank()) {
            existing.setUsuarioPassword(passwordEncoder.encode(usuario.getUsuarioPassword()));
        }

        existing.setUsuarioActivo(usuario.getUsuarioActivo());
        existing.setUsuarioFechaModif(LocalDateTime.now());

        return repo.save(existing);
    }

    @Transactional
    public void delete(String email) {
        String cleanEmail = normalizeEmail(email);
        Usuario usuario = repo.findById(cleanEmail)
                .orElseThrow(() -> new RuntimeException("Usuario not found"));

        // Optional: check FK references before deleting

        repo.delete(usuario);
    }

    // =======================
    // Validation & Helpers
    // =======================
    private void validateUsuario(Usuario usuario) {
        if (usuario.getUsuarioNombre() == null || usuario.getUsuarioNombre().isBlank()) {
            throw new RuntimeException("UsuarioNombre is required");
        }
        if (usuario.getUsuarioPassword() == null || usuario.getUsuarioPassword().isBlank()) {
            throw new RuntimeException("UsuarioPassword is required");
        }
        if (usuario.getRol() == null || usuario.getRol().getRolId() == null) {
            throw new RuntimeException("Usuario must have a valid role assigned");
        }
        if (usuario.getUsuarioEmail() == null || usuario.getUsuarioEmail().isBlank()) {
            throw new RuntimeException("UsuarioEmail is required");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}