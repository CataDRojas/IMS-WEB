package com.ims_web.inventory.service;

import com.ims_web.inventory.dto.MovimientoDetalleResponseDTO;
import com.ims_web.inventory.dto.MovimientoResponseDTO;
import com.ims_web.inventory.entity.Movimiento;
import com.ims_web.inventory.entity.MovimientoDetalle;
import com.ims_web.inventory.repository.MovimientoDetalleRepository;
import com.ims_web.inventory.repository.MovimientoRepository;
import com.ims_web.inventory.util.AuditHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
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

    // =========================
    // READ ALL
    // =========================
    public List<MovimientoResponseDTO> getAll() {
        return repo.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // =========================
    // READ BY ID
    // =========================
    public MovimientoResponseDTO getById(Long id) {
        Movimiento movimiento = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento not found"));

        return toDTO(movimiento);
    }

    // =========================
    // CREATE
    // =========================
    @Transactional
    public MovimientoResponseDTO create(Movimiento movimiento, String currentUser) {

        if (movimiento.getMovimientoEstado() == null || movimiento.getMovimientoEstado().isBlank()) {
            movimiento.setMovimientoEstado("PENDIENTE");
        }

        AuditHelper.setCreationAudit(movimiento, currentUser);

        Movimiento saved = repo.save(movimiento);

        return toDTO(saved);
    }

    // =========================
    // UPDATE
    // =========================
    @Transactional
    public MovimientoResponseDTO update(Movimiento movimiento, String currentUser) {

        Movimiento existing = repo.findById(movimiento.getMovimientoId())
                .orElseThrow(() -> new EntityNotFoundException("Movimiento not found"));

        if ("CONFIRMADO".equals(existing.getMovimientoEstado())) {
            throw new IllegalStateException("Cannot modify a confirmed movimiento");
        }

        existing.setMovimientoDescripcion(movimiento.getMovimientoDescripcion());
        existing.setMovimientoEstado(movimiento.getMovimientoEstado());
        existing.setMovimientoTipo(movimiento.getMovimientoTipo());
        existing.setMovimientoMetodoPago(movimiento.getMovimientoMetodoPago());

        AuditHelper.setModificationAudit(existing, currentUser);

        return toDTO(repo.save(existing));
    }

    // =========================
    // CONFIRMAR (NO CALCULATION HERE ANYMORE)
    // =========================
    @Transactional
    public MovimientoResponseDTO confirmarMovimiento(Long id, String currentUser) {

        Movimiento movimiento = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento not found"));

        if ("CONFIRMADO".equals(movimiento.getMovimientoEstado())) {
            throw new IllegalStateException("Movimiento already confirmed");
        }

        movimiento.setMovimientoEstado("CONFIRMADO");

        AuditHelper.setModificationAudit(movimiento, currentUser);

        Movimiento saved = repo.save(movimiento);

        return toDTO(saved);
    }

    // =========================
    // DELETE
    // =========================
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

    // =========================
    // MAPPING
    // =========================
    private MovimientoResponseDTO toDTO(Movimiento m) {

        MovimientoResponseDTO dto = new MovimientoResponseDTO();

        dto.setMovimientoId(m.getMovimientoId());
        dto.setMovimientoDescripcion(m.getMovimientoDescripcion());
        dto.setMovimientoEstado(m.getMovimientoEstado());
        dto.setMovimientoTipo(m.getMovimientoTipo());
        dto.setMovimientoMetodoPago(m.getMovimientoMetodoPago());

        dto.setMovimientoFechaCreacion(m.getMovimientoFechaCreacion());
        dto.setMovimientoFechaModif(m.getMovimientoFechaModif());

        dto.setMovimientoUsuarioCreacion(m.getMovimientoUsuarioCreacion());
        dto.setMovimientoUsuarioModif(m.getMovimientoUsuarioModif());

        List<MovimientoDetalleResponseDTO> detalles = detalleRepo.findByMovimiento(m)
                .stream()
                .map(this::mapDetalle)
                .toList();

        dto.setDetalles(detalles);

        return dto;
    }

    private MovimientoDetalleResponseDTO mapDetalle(MovimientoDetalle d) {

        MovimientoDetalleResponseDTO dto = new MovimientoDetalleResponseDTO();

        dto.setMovimientoDetalleId(d.getMovimientoDetalleId());
        dto.setMovimientoId(d.getMovimiento().getMovimientoId());

        dto.setProductoId(d.getProducto().getProductoId());
        dto.setProductoNombre(d.getProducto().getProductoNombre());

        dto.setMovimientoDetalleCantidad(d.getMovimientoDetalleCantidad());
        dto.setMovimientoDetalleUnidadesPorPaquete(d.getMovimientoDetalleUnidadesPorPaquete());

        dto.setMovimientoDetallePrecioBase(d.getMovimientoDetallePrecioBase());
        dto.setMovimientoDetallePrecioUnitario(d.getMovimientoDetallePrecioUnitario());
        dto.setMovimientoDetallePrecioTotal(d.getMovimientoDetallePrecioTotal());

        dto.setMovimientoDetalleDescuentoAplicado(d.getMovimientoDetalleDescuentoAplicado());
        dto.setMovimientoDetalleDescripcion(d.getMovimientoDetalleDescripcion());

        return dto;
    }
}