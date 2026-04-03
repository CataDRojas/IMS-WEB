package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Rol;
import com.ims_web.inventory.repository.RolRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolService {

    private final RolRepository repo;

    public RolService(RolRepository repo) {
        this.repo = repo;
    }

    public List<Rol> getAll() {
        return repo.findAll();
    }

    public Rol getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol not found"));
    }

    public Rol save(Rol rol) {
        return repo.save(rol);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}