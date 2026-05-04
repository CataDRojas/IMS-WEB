package com.ims_web.inventory.service;

import com.ims_web.inventory.dto.MovimientoDetalleRequestDTO;
import com.ims_web.inventory.dto.MovimientoDetalleResponseDTO;
import com.ims_web.inventory.entity.Movimiento;
import com.ims_web.inventory.entity.MovimientoDetalle;
import com.ims_web.inventory.entity.MovimientoLugar;
import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MovimientoDetalleService {

    private final MovimientoDetalleRepository detalleRepo;
    private final MovimientoRepository movimientoRepo;
    private final ProductoRepository productoRepo;
    private final MovimientoLugarRepository lugarRepo;
    private final MovimientoLugarProductoRepository mlpRepo;
    private final EntityManager entityManager;

    public MovimientoDetalleService(
            MovimientoDetalleRepository detalleRepo,
            MovimientoRepository movimientoRepo,
            ProductoRepository productoRepo,
            MovimientoLugarRepository lugarRepo,
            MovimientoLugarProductoRepository mlpRepo,
            EntityManager entityManager
    ) {
        this.detalleRepo = detalleRepo;
        this.movimientoRepo = movimientoRepo;
        this.productoRepo = productoRepo;
        this.lugarRepo = lugarRepo;
        this.mlpRepo = mlpRepo;
        this.entityManager = entityManager;
    }

    // =========================================================
    // READ
    // =========================================================

    public List<MovimientoDetalleResponseDTO> getAllDetalles() {
        return detalleRepo.findAll().stream().map(this::toDTO).toList();
    }

    public MovimientoDetalleResponseDTO getDetalleById(Long id) {
        MovimientoDetalle detalle = detalleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MovimientoDetalle not found"));
        return toDTO(detalle);
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Transactional
    public MovimientoDetalleResponseDTO createDetalle(
            Long movimientoId,
            MovimientoDetalleRequestDTO incoming
    ) {

        Movimiento movimiento = getValidatedMovimiento(movimientoId);

        Producto producto = productoRepo.findById(incoming.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

        validate(incoming);

        MovimientoDetalle detalle = buildDetalle(movimiento, producto, incoming);

        validateStockImpact(movimiento, detalle, null);

        MovimientoDetalle saved = detalleRepo.save(detalle);

        entityManager.flush();

        // ❌ REMOVED: sync moved to CONFIRMATION LAYER

        return toDTO(saved);
    }

    // =========================================================
    // BATCH CREATE
    // =========================================================

    @Transactional
    public List<MovimientoDetalleResponseDTO> createDetallesBatch(
            Long movimientoId,
            List<MovimientoDetalleRequestDTO> detalles
    ) {

        Movimiento movimiento = getValidatedMovimiento(movimientoId);
        List<MovimientoDetalle> savedList = new ArrayList<>();

        for (MovimientoDetalleRequestDTO incoming : detalles) {

            Producto producto = productoRepo.findById(incoming.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

            validate(incoming);

            MovimientoDetalle detalle = buildDetalle(movimiento, producto, incoming);

            validateStockImpact(movimiento, detalle, null);

            savedList.add(detalleRepo.save(detalle));
        }

        entityManager.flush();

        // ❌ REMOVED: batch sync moved to CONFIRMATION LAYER

        return savedList.stream().map(this::toDTO).toList();
    }

    // =========================================================
    // UPDATE
    // =========================================================

    @Transactional
    public MovimientoDetalleResponseDTO updateDetalle(
            Long id,
            MovimientoDetalleRequestDTO incoming
    ) {

        MovimientoDetalle existing = detalleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MovimientoDetalle not found"));

        Movimiento movimiento = getValidatedMovimiento(existing.getMovimiento().getMovimientoId());

        validate(incoming);

        Producto producto = existing.getProducto();

        MovimientoDetalle projected = buildDetalle(movimiento, producto, incoming);

        validateStockImpact(movimiento, projected, existing);

        existing.setMovimientoDetalleCantidad(incoming.getMovimientoDetalleCantidad());
        existing.setMovimientoDetalleUnidadesPorPaquete(
                incoming.getMovimientoDetalleUnidadesPorPaquete()
        );
        existing.setMovimientoDetalleDescripcion(incoming.getMovimientoDetalleDescripcion());

        if (incoming.getMovimientoLugarId() != null) {
            lugarRepo.findById(incoming.getMovimientoLugarId())
                    .ifPresent(existing::setMovimientoLugar);
        }

        MovimientoDetalle updated = detalleRepo.save(existing);

        entityManager.flush();

        // ❌ REMOVED: sync moved to CONFIRMATION LAYER

        return toDTO(updated);
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Transactional
    public void deleteDetalle(Long id) {

        MovimientoDetalle detalle = detalleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MovimientoDetalle not found"));

        Movimiento movimiento = getValidatedMovimiento(detalle.getMovimiento().getMovimientoId());

        validateStockImpact(movimiento, null, detalle);

        detalleRepo.delete(detalle);

        entityManager.flush();

        // ❌ REMOVED: sync moved to CONFIRMATION LAYER
    }

    // =========================================================
    // STOCK VALIDATION (UNCHANGED)
    // =========================================================

    private void validateStockImpact(
            Movimiento movimiento,
            MovimientoDetalle newState,
            MovimientoDetalle oldState
    ) {

        List<MovimientoDetalle> all = detalleRepo.findByMovimiento(movimiento);

        if (oldState != null) {
            all.removeIf(d ->
                    d.getMovimientoDetalleId().equals(oldState.getMovimientoDetalleId()));
        }

        if (newState != null) {
            all.add(newState);
        }

        for (MovimientoDetalle d : all) {

            int unidades = d.getMovimientoDetalleCantidad() *
                    (d.getMovimientoDetalleUnidadesPorPaquete() != null
                            ? d.getMovimientoDetalleUnidadesPorPaquete()
                            : 1);

            Integer stock = mlpRepo.sumStockByProductoId(d.getProducto().getProductoId());
            if (stock == null) stock = 0;

            // SALIDA: ensure no negative resulting stock
            if ("SALIDA".equals(movimiento.getMovimientoTipo())) {

                int projected = stock - unidades;

                if (projected < 0) {
                    throw new IllegalStateException(
                            "ERR_STOCK_NEGATIVE|Product " +
                                    d.getProducto().getProductoId() +
                                    " would go below zero"
                    );
                }
            }

            // AJUSTE: only validate adjustment value itself (no stock simulation)
            if ("AJUSTE".equals(movimiento.getMovimientoTipo())) {

                if (unidades < 0) {
                    throw new IllegalStateException(
                            "ERR_ADJUSTMENT_NEGATIVE|Product " +
                                    d.getProducto().getProductoId() +
                                    " adjustment cannot be negative"
                    );
                }
            }
        }
    }

    // =========================================================
    // HELPERS (UNCHANGED)
    // =========================================================

    private MovimientoDetalle buildDetalle(
            Movimiento movimiento,
            Producto producto,
            MovimientoDetalleRequestDTO dto
    ) {

        MovimientoDetalle d = new MovimientoDetalle();
        d.setMovimiento(movimiento);
        d.setProducto(producto);

        d.setMovimientoDetalleCantidad(dto.getMovimientoDetalleCantidad());
        d.setMovimientoDetalleUnidadesPorPaquete(
                dto.getMovimientoDetalleUnidadesPorPaquete() != null
                        ? dto.getMovimientoDetalleUnidadesPorPaquete()
                        : 1
        );
        d.setMovimientoDetalleDescripcion(dto.getMovimientoDetalleDescripcion());

        if (dto.getMovimientoLugarId() != null) {
            lugarRepo.findById(dto.getMovimientoLugarId())
                    .ifPresent(d::setMovimientoLugar);
        }

        return d;
    }

    private void validate(MovimientoDetalleRequestDTO dto) {
        if (dto.getMovimientoDetalleCantidad() == null ||
                dto.getMovimientoDetalleCantidad() <= 0) {
            throw new IllegalArgumentException("Cantidad must be > 0");
        }

        if (dto.getMovimientoDetalleUnidadesPorPaquete() != null &&
                dto.getMovimientoDetalleUnidadesPorPaquete() < 1) {
            throw new IllegalArgumentException("UnidadesPorPaquete must be >= 1");
        }
    }

    private Movimiento getValidatedMovimiento(Long id) {
        Movimiento m = movimientoRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento not found"));

        if ("CONFIRMADO".equals(m.getMovimientoEstado())) {
            throw new IllegalStateException("Cannot modify a confirmed movimiento");
        }

        return m;
    }

    // =========================================================
    // DTO
    // =========================================================

    private MovimientoDetalleResponseDTO toDTO(MovimientoDetalle d) {

        MovimientoDetalleResponseDTO dto = new MovimientoDetalleResponseDTO();

        dto.setMovimientoDetalleId(d.getMovimientoDetalleId());
        dto.setMovimientoId(d.getMovimiento().getMovimientoId());
        dto.setProductoId(d.getProducto().getProductoId());
        dto.setMovimientoDetalleCantidad(d.getMovimientoDetalleCantidad());
        dto.setMovimientoDetalleUnidadesPorPaquete(d.getMovimientoDetalleUnidadesPorPaquete());
        dto.setMovimientoDetalleDescripcion(d.getMovimientoDetalleDescripcion());

        if (d.getMovimientoLugar() != null) {
            dto.setMovimientoLugarId(d.getMovimientoLugar().getMovimientoLugarId());
            dto.setMovimientoLugarNombre(
                    d.getMovimientoLugar().getMovimientoLugarDescripcion()
            );
        }

        return dto;
    }
}