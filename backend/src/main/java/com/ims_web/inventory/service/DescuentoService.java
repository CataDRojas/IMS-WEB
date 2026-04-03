package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Descuento;
import com.ims_web.inventory.repository.DescuentoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DescuentoService {

    private final DescuentoRepository repo;

    public DescuentoService(DescuentoRepository repo) {
        this.repo = repo;
    }

    public List<Descuento> getAll() {
        return repo.findAll();
    }

    public List<Descuento> getActive() {
        return repo.findByDescuentoActivoTrue();
    }

    public Descuento getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Descuento not found"));
    }

    public Descuento createOrUpdate(Descuento descuento) {
        // Optional: validate type & value rules
        if (!List.of("FLAT", "PORCENTAJE", "MULTIPLICATIVO").contains(descuento.getDescuentoTipo())) {
            throw new RuntimeException("Invalid DescuentoTipo");
        }
        return repo.save(descuento);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}