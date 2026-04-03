package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Movimiento;
import com.ims_web.inventory.entity.MovimientoDetalle;
import com.ims_web.inventory.repository.MovimientoDetalleRepository;
import com.ims_web.inventory.repository.MovimientoRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovimientoService {

    private final MovimientoRepository repo;
    private final MovimientoDetalleRepository detalleRepo;
    private final EntityManager entityManager;

    public MovimientoService(MovimientoRepository repo,
                             MovimientoDetalleRepository detalleRepo,
                             EntityManager entityManager) {
        this.repo = repo;
        this.detalleRepo = detalleRepo;
        this.entityManager = entityManager;
    }

    public List<Movimiento> getAll() {
        return repo.findAll();
    }

    public Movimiento getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento not found"));
    }

    @Transactional
    public Movimiento create(Movimiento movimiento) {
        // DB handles defaults, totals, etc.
        Movimiento saved = repo.save(movimiento);

        entityManager.flush();
        entityManager.refresh(saved);

        return saved;
    }

    @Transactional
    public Movimiento update(Movimiento movimiento) {

        Movimiento existing = repo.findById(movimiento.getMovimientoId())
                .orElseThrow(() -> new RuntimeException("Movimiento not found"));

        if ("CONFIRMADO".equals(existing.getMovimientoEstado())) {
            throw new RuntimeException("Cannot modify a confirmed movimiento");
        }

        boolean isConfirming =
                !"CONFIRMADO".equals(existing.getMovimientoEstado()) &&
                        "CONFIRMADO".equals(movimiento.getMovimientoEstado());

        existing.setMovimientoDescripcion(movimiento.getMovimientoDescripcion());
        existing.setMovimientoEstado(movimiento.getMovimientoEstado());
        existing.setMovimientoTipo(movimiento.getMovimientoTipo());
        existing.setMovimientoMetodoPago(movimiento.getMovimientoMetodoPago());
        existing.setMovimientoUsuarioModif(movimiento.getMovimientoUsuarioModif());
        existing.setMovimientoFechaModif(movimiento.getMovimientoFechaModif());

        Movimiento updated = repo.save(existing);

        // 🔥 Force DB execution (triggers, stock updates, totals)
        entityManager.flush();

        // 🔄 Reload DB state (important after CONFIRMADO)
        entityManager.refresh(updated);

        return updated;
    }

    @Transactional
    public void delete(Long id) {

        Movimiento movimiento = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Movimiento not found"));

        if ("CONFIRMADO".equals(movimiento.getMovimientoEstado())) {
            throw new RuntimeException("Cannot delete a confirmed movimiento");
        }

        List<MovimientoDetalle> detalles = detalleRepo.findByMovimiento(movimiento);

        detalleRepo.deleteAll(detalles);
        repo.delete(movimiento);

        entityManager.flush(); // ensure DB consistency
    }
}