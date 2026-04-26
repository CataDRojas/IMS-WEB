package com.ims_web.inventory.service;

import com.ims_web.inventory.dto.MovimientoDetalleRequestDTO;
import com.ims_web.inventory.dto.MovimientoDetalleResponseDTO;
import com.ims_web.inventory.entity.Movimiento;
import com.ims_web.inventory.entity.MovimientoDetalle;
import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.repository.MovimientoDetalleRepository;
import com.ims_web.inventory.repository.MovimientoRepository;
import com.ims_web.inventory.repository.ProductoRepository;
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
    private final EntityManager entityManager;

    public MovimientoDetalleService(MovimientoDetalleRepository detalleRepo,
                                    MovimientoRepository movimientoRepo,
                                    ProductoRepository productoRepo,
                                    EntityManager entityManager) {
        this.detalleRepo = detalleRepo;
        this.movimientoRepo = movimientoRepo;
        this.productoRepo = productoRepo;
        this.entityManager = entityManager;
    }

    // =========================
    // READ
    // =========================

    public List<MovimientoDetalleResponseDTO> getAllDetalles() {
        return detalleRepo.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public MovimientoDetalleResponseDTO getDetalleById(Long id) {
        MovimientoDetalle detalle = detalleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MovimientoDetalle not found"));

        return toDTO(detalle);
    }

    // =========================
    // CREATE (SINGLE)
    // =========================

    @Transactional
    public MovimientoDetalleResponseDTO createDetalle(Long movimientoId, MovimientoDetalleRequestDTO incoming) {

        Movimiento movimiento = movimientoRepo.findById(movimientoId)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento not found"));

        if ("CONFIRMADO".equals(movimiento.getMovimientoEstado())) {
            throw new IllegalStateException("Cannot add detail to a confirmed movimiento");
        }

        Producto producto = productoRepo.findById(incoming.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

        validate(incoming);

        MovimientoDetalle detalle = new MovimientoDetalle();
        detalle.setMovimiento(movimiento);
        detalle.setProducto(producto);

        detalle.setMovimientoDetalleCantidad(incoming.getMovimientoDetalleCantidad());
        detalle.setMovimientoDetalleUnidadesPorPaquete(
                incoming.getMovimientoDetalleUnidadesPorPaquete() != null
                        ? incoming.getMovimientoDetalleUnidadesPorPaquete()
                        : 1
        );
        detalle.setMovimientoDetalleDescripcion(incoming.getMovimientoDetalleDescripcion());

        detalle.setMovimientoDetallePrecioBase(incoming.getMovimientoDetallePrecioBase());
        detalle.setMovimientoDetallePrecioUnitario(incoming.getMovimientoDetallePrecioUnitario());
        detalle.setMovimientoDetallePrecioTotal(incoming.getMovimientoDetallePrecioTotal());
        detalle.setMovimientoDetalleDescuentoAplicado(incoming.getMovimientoDetalleDescuentoAplicado());

        MovimientoDetalle saved = detalleRepo.save(detalle);

        entityManager.flush();
        entityManager.refresh(saved);

        // 🔥 ALWAYS RECALCULATE AFTER SINGLE INSERT
        entityManager.createNativeQuery("""
            CALL sp_recalcular_movimiento(:id)
        """)
                .setParameter("id", movimientoId)
                .executeUpdate();

        return toDTO(saved);
    }

    // =========================
    // CREATE (BATCH)
    // =========================

    @Transactional
    public List<MovimientoDetalleResponseDTO> createDetallesBatch(
            Long movimientoId,
            List<MovimientoDetalleRequestDTO> detalles
    ) {

        Movimiento movimiento = movimientoRepo.findById(movimientoId)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento not found"));

        if ("CONFIRMADO".equals(movimiento.getMovimientoEstado())) {
            throw new IllegalStateException("Cannot add details to a confirmed movimiento");
        }

        List<MovimientoDetalle> savedList = new ArrayList<>();

        for (MovimientoDetalleRequestDTO incoming : detalles) {

            Producto producto = productoRepo.findById(incoming.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

            validate(incoming);

            MovimientoDetalle detalle = new MovimientoDetalle();
            detalle.setMovimiento(movimiento);
            detalle.setProducto(producto);

            detalle.setMovimientoDetalleCantidad(incoming.getMovimientoDetalleCantidad());
            detalle.setMovimientoDetalleUnidadesPorPaquete(
                    incoming.getMovimientoDetalleUnidadesPorPaquete() != null
                            ? incoming.getMovimientoDetalleUnidadesPorPaquete()
                            : 1
            );
            detalle.setMovimientoDetalleDescripcion(incoming.getMovimientoDetalleDescripcion());

            detalle.setMovimientoDetallePrecioBase(incoming.getMovimientoDetallePrecioBase());
            detalle.setMovimientoDetallePrecioUnitario(incoming.getMovimientoDetallePrecioUnitario());
            detalle.setMovimientoDetallePrecioTotal(incoming.getMovimientoDetallePrecioTotal());
            detalle.setMovimientoDetalleDescuentoAplicado(incoming.getMovimientoDetalleDescuentoAplicado());

            savedList.add(detalleRepo.save(detalle));
        }

        entityManager.flush();

        for (MovimientoDetalle d : savedList) {
            entityManager.refresh(d);
        }

        // 🔥 ALWAYS RECALCULATE AFTER BATCH INSERT
        entityManager.createNativeQuery("""
            CALL sp_recalcular_movimiento(:id)
        """)
                .setParameter("id", movimientoId)
                .executeUpdate();

        return savedList.stream().map(this::toDTO).toList();
    }

    // =========================
    // UPDATE
    // =========================

    @Transactional
    public MovimientoDetalleResponseDTO updateDetalle(Long id, MovimientoDetalleRequestDTO incoming) {

        MovimientoDetalle existing = detalleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MovimientoDetalle not found"));

        if ("CONFIRMADO".equals(existing.getMovimiento().getMovimientoEstado())) {
            throw new IllegalStateException("Cannot modify detail of a confirmed movimiento");
        }

        validate(incoming);

        existing.setMovimientoDetalleCantidad(incoming.getMovimientoDetalleCantidad());
        existing.setMovimientoDetalleUnidadesPorPaquete(incoming.getMovimientoDetalleUnidadesPorPaquete());
        existing.setMovimientoDetalleDescripcion(incoming.getMovimientoDetalleDescripcion());

        MovimientoDetalle updated = detalleRepo.save(existing);

        entityManager.flush();
        entityManager.refresh(updated);

        // 🔥 RECALCULATE AFTER UPDATE TOO
        Long movimientoId = existing.getMovimiento().getMovimientoId();

        entityManager.createNativeQuery("""
            CALL sp_recalcular_movimiento(:id)
        """)
                .setParameter("id", movimientoId)
                .executeUpdate();

        return toDTO(updated);
    }

    // =========================
    // DELETE
    // =========================

    @Transactional
    public void deleteDetalle(Long id) {

        MovimientoDetalle detalle = detalleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MovimientoDetalle not found"));

        if ("CONFIRMADO".equals(detalle.getMovimiento().getMovimientoEstado())) {
            throw new IllegalStateException("Cannot delete detail of a confirmed movimiento");
        }

        Long movimientoId = detalle.getMovimiento().getMovimientoId();

        detalleRepo.delete(detalle);

        entityManager.flush();

        // 🔥 RECALCULATE AFTER DELETE TOO
        entityManager.createNativeQuery("""
            CALL sp_recalcular_movimiento(:id)
        """)
                .setParameter("id", movimientoId)
                .executeUpdate();
    }

    // =========================
    // VALIDATION
    // =========================

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

    // =========================
    // MAPPING
    // =========================

    private MovimientoDetalleResponseDTO toDTO(MovimientoDetalle d) {

        MovimientoDetalleResponseDTO dto = new MovimientoDetalleResponseDTO();

        dto.setMovimientoDetalleId(d.getMovimientoDetalleId());
        dto.setMovimientoId(d.getMovimiento().getMovimientoId());
        dto.setProductoId(d.getProducto().getProductoId());

        dto.setMovimientoDetalleCantidad(d.getMovimientoDetalleCantidad());
        dto.setMovimientoDetalleUnidadesPorPaquete(d.getMovimientoDetalleUnidadesPorPaquete());

        dto.setMovimientoDetalleDescripcion(d.getMovimientoDetalleDescripcion());

        dto.setMovimientoDetallePrecioBase(d.getMovimientoDetallePrecioBase());
        dto.setMovimientoDetallePrecioUnitario(d.getMovimientoDetallePrecioUnitario());
        dto.setMovimientoDetallePrecioTotal(d.getMovimientoDetallePrecioTotal());
        dto.setMovimientoDetalleDescuentoAplicado(d.getMovimientoDetalleDescuentoAplicado());

        return dto;
    }
}