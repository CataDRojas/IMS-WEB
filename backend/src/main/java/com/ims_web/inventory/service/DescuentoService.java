package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Descuento;
import com.ims_web.inventory.repository.DescuentoRepository;
import com.ims_web.inventory.util.AuditHelper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new EntityNotFoundException("Descuento with ID " + id + " not found"));
    }

    @Transactional
    public Descuento createDescuento(Descuento descuento, String currentUser) {

        validateDescuento(descuento);
        checkUniqueNombre(descuento.getDescuentoNombre(), null);

        AuditHelper.setCreationAudit(descuento, currentUser);

        return repo.save(descuento);
    }

    @Transactional
    public Descuento updateDescuento(Descuento descuento, String currentUser) {

        Descuento existing = repo.findById(descuento.getDescuentoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Descuento with ID " + descuento.getDescuentoId() + " not found"
                ));

        validateDescuento(descuento);
        checkUniqueNombre(descuento.getDescuentoNombre(), descuento.getDescuentoId());

        existing.setDescuentoNombre(descuento.getDescuentoNombre());
        existing.setDescuentoActivo(descuento.getDescuentoActivo());
        existing.setDescuentoTipo(descuento.getDescuentoTipo());

        existing.setDescuentoValor(descuento.getDescuentoValor());

        // NEW FIELD
        existing.setDescuentoValorSecundario(descuento.getDescuentoValorSecundario());

        AuditHelper.setModificationAudit(existing, currentUser);

        return repo.save(existing);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Descuento with ID " + id + " not found");
        }
        repo.deleteById(id);
    }

    private void validateDescuento(Descuento descuento) {

        String tipo = descuento.getDescuentoTipo();

        if (!List.of("FLAT", "PORCENTAJE", "MULTIPLICATIVO").contains(tipo)) {
            throw new IllegalArgumentException("Invalid DescuentoTipo: " + tipo);
        }

        if (descuento.getDescuentoValor() == null) {
            throw new IllegalArgumentException("DescuentoValor cannot be null");
        }

        double valor = descuento.getDescuentoValor().doubleValue();

        switch (tipo) {
            case "MULTIPLICATIVO":
                if (valor <= 0)
                    throw new IllegalArgumentException("MULTIPLICATIVO DescuentoValor must be > 0");

                // NEW RULE (optional but safe)
                if (descuento.getDescuentoValorSecundario() == null ||
                        descuento.getDescuentoValorSecundario().doubleValue() <= 0) {
                    throw new IllegalArgumentException(
                            "MULTIPLICATIVO requires DescuentoValorSecundario (> 0)"
                    );
                }
                break;

            case "FLAT":
                if (valor < 0)
                    throw new IllegalArgumentException("FLAT DescuentoValor must be >= 0");
                break;

            case "PORCENTAJE":
                if (valor < 0 || valor > 100)
                    throw new IllegalArgumentException("PORCENTAJE DescuentoValor must be between 0 and 100");
                break;
        }
    }

    private void checkUniqueNombre(String nombre, Long idToExclude) {
        boolean exists = repo.existsByDescuentoNombreIgnoreCaseAndDescuentoIdNot(
                nombre,
                idToExclude == null ? -1L : idToExclude
        );

        if (exists) {
            throw new IllegalArgumentException("DescuentoNombre must be unique");
        }
    }
}