package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Usuario;
import com.ims_web.inventory.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;

    public UsuarioService(UsuarioRepository repo) {
        this.repo = repo;
    }

    public List<Usuario> getAll() {
        return repo.findAll();
    }

    public Usuario getByEmail(String email) {
        return repo.findById(email)
                .orElseThrow(() -> new RuntimeException("Usuario not found"));
    }

    public Usuario create(Usuario usuario) {
        return repo.save(usuario);
    }

    public Usuario update(String email, Usuario usuario) {
        Usuario existing = repo.findById(email)
                .orElseThrow(() -> new RuntimeException("Usuario not found"));

        existing.setUsuarioNombre(usuario.getUsuarioNombre());
        existing.setUsuarioRun(usuario.getUsuarioRun());
        existing.setUsuarioDV(usuario.getUsuarioDV());
        existing.setUsuarioPassword(usuario.getUsuarioPassword());
        existing.setRol(usuario.getRol());
        existing.setUsuarioActivo(usuario.getUsuarioActivo());

        return repo.save(existing);
    }

    public void delete(String email) {
        repo.deleteById(email);
    }
}