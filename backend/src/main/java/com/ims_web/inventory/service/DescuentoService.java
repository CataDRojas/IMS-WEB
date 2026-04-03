package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Descuento;
import com.ims_web.inventory.repository.DescuentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

    @Transactional
    public Descuento createDescuento(Descuento descuento, String currentUser) {
        validateDescuento(descuento);
        checkUniqueNombre(descuento.getDescuentoNombre(), null);

        LocalDateTime now = LocalDateTime.now();
        descuento.setDescuentoUsuarioCreacion(currentUser);
        descuento.setDescuentoFechaCreacion(now);

        return repo.save(descuento);
    }

    @Transactional
    public Descuento updateDescuento(Descuento descuento, String currentUser) {
        Descuento existing = repo.findById(descuento.getDescuentoId())
                .orElseThrow(() -> new RuntimeException("Descuento not found"));

        validateDescuento(descuento);
        checkUniqueNombre(descuento.getDescuentoNombre(), descuento.getDescuentoId());

        existing.setDescuentoNombre(descuento.getDescuentoNombre());
        existing.setDescuentoActivo(descuento.getDescuentoActivo());
        existing.setDescuentoTipo(descuento.getDescuentoTipo());
        existing.setDescuentoValor(descuento.getDescuentoValor());

        // Audit
        LocalDateTime now = LocalDateTime.now();
        existing.setDescuentoUsuarioModif(currentUser);
        existing.setDescuentoFechaModif(now);

        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private void validateDescuento(Descuento descuento) {
        if (!List.of("FLAT", "PORCENTAJE", "MULTIPLICATIVO").contains(descuento.getDescuentoTipo())) {
            throw new RuntimeException("Invalid DescuentoTipo");
        }
        if (descuento.getDescuentoValor() == null || descuento.getDescuentoValor().doubleValue() < 0) {
            throw new RuntimeException("DescuentoValor must be >= 0");
        }
    }

    private void checkUniqueNombre(String nombre, Long idToExclude) {
        repo.findAll().stream()
                .filter(d -> d.getDescuentoNombre().equalsIgnoreCase(nombre))
                .filter(d -> idToExclude == null || !d.getDescuentoId().equals(idToExclude))
                .findAny()
                .ifPresent(d -> { throw new RuntimeException("DescuentoNombre must be unique"); });
    }
}