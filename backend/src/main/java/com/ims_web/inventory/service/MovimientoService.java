package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Movimiento;
import com.ims_web.inventory.entity.MovimientoDetalle;
import com.ims_web.inventory.repository.MovimientoDetalleRepository;
import com.ims_web.inventory.repository.MovimientoRepository;
import com.ims_web.inventory.util.AuditHelper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovimientoService {

    private final MovimientoRepository repo;
    private final MovimientoDetalleRepository detalleRepo;

    public MovimientoService(MovimientoRepository repo,
                             MovimientoDetalleRepository detalleRepo) {
        this.repo = repo;
        this.detalleRepo = detalleRepo;
    }

    public List<Movimiento> getAll() {
        return repo.findAll();
    }

    public Movimiento getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento not found"));
    }

    @Transactional
    public Movimiento create(Movimiento movimiento, String currentUser) {
        AuditHelper.setCreationAudit(movimiento, currentUser); // static call
        return repo.save(movimiento);
    }

    @Transactional
    public Movimiento update(Movimiento movimiento, String currentUser) {
        Movimiento existing = repo.findById(movimiento.getMovimientoId())
                .orElseThrow(() -> new EntityNotFoundException("Movimiento not found"));

        if ("CONFIRMADO".equals(existing.getMovimientoEstado())) {
            throw new IllegalStateException("Cannot modify a confirmed movimiento");
        }

        existing.setMovimientoDescripcion(movimiento.getMovimientoDescripcion());
        existing.setMovimientoEstado(movimiento.getMovimientoEstado());
        existing.setMovimientoTipo(movimiento.getMovimientoTipo());
        existing.setMovimientoMetodoPago(movimiento.getMovimientoMetodoPago());

        AuditHelper.setModificationAudit(existing, currentUser); // static call

        return repo.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Movimiento movimiento = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento not found"));

        if ("CONFIRMADO".equals(movimiento.getMovimientoEstado())) {
            throw new IllegalStateException("Cannot delete a confirmed movimiento");
        }

        List<MovimientoDetalle> detalles = detalleRepo.findByMovimiento(movimiento);
        detalleRepo.deleteAll(detalles);
        repo.delete(movimiento);
    }
}