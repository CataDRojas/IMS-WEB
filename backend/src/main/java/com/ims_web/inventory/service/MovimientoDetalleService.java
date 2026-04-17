package com.ims_web.inventory.service;

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

    public List<MovimientoDetalle> getAllDetalles() {
        return detalleRepo.findAll();
    }

    public MovimientoDetalle getDetalleById(Long id) {
        return detalleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MovimientoDetalle not found"));
    }

    @Transactional
    public MovimientoDetalle createDetalle(Long movimientoId, MovimientoDetalle detalle) {

        Movimiento movimiento = movimientoRepo.findById(movimientoId)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento not found"));

        if ("CONFIRMADO".equals(movimiento.getMovimientoEstado())) {
            throw new IllegalStateException("Cannot add detail to a confirmed movimiento");
        }

        Producto producto = productoRepo.findById(detalle.getProducto().getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

        validateDetalle(detalle);

        detalle.setMovimiento(movimiento);
        detalle.setProducto(producto);

        MovimientoDetalle saved = detalleRepo.save(detalle);
        entityManager.flush();
        entityManager.refresh(saved);

        return saved;
    }

    @Transactional
    public MovimientoDetalle updateDetalle(MovimientoDetalle detalle) {

        MovimientoDetalle existing = detalleRepo.findById(detalle.getMovimientoDetalleId())
                .orElseThrow(() -> new EntityNotFoundException("MovimientoDetalle not found"));

        if ("CONFIRMADO".equals(existing.getMovimiento().getMovimientoEstado())) {
            throw new IllegalStateException("Cannot modify detail of a confirmed movimiento");
        }

        validateDetalle(detalle);

        existing.setMovimientoDetalleCantidad(detalle.getMovimientoDetalleCantidad());
        existing.setMovimientoDetalleUnidadesPorPaquete(
                detalle.getMovimientoDetalleUnidadesPorPaquete()
        );
        existing.setMovimientoDetalleDescripcion(detalle.getMovimientoDetalleDescripcion());
        existing.setMovimientoLugar(detalle.getMovimientoLugar());

        MovimientoDetalle updated = detalleRepo.save(existing);
        entityManager.flush();
        entityManager.refresh(updated);

        return updated;
    }

    @Transactional
    public void deleteDetalle(Long id) {

        MovimientoDetalle detalle = detalleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MovimientoDetalle not found"));

        if ("CONFIRMADO".equals(detalle.getMovimiento().getMovimientoEstado())) {
            throw new IllegalStateException("Cannot delete detail of a confirmed movimiento");
        }

        detalleRepo.delete(detalle);
        entityManager.flush();
    }

    // =========================
    // VALIDATION
    // =========================
    private void validateDetalle(MovimientoDetalle detalle) {

        if (detalle.getMovimientoDetalleCantidad() == null ||
                detalle.getMovimientoDetalleCantidad() == 0) {
            throw new IllegalArgumentException("Cantidad cannot be zero");
        }

        if (detalle.getMovimientoDetalleUnidadesPorPaquete() == null ||
                detalle.getMovimientoDetalleUnidadesPorPaquete() < 1) {
            throw new IllegalArgumentException("UnidadesPorPaquete must be >= 1");
        }
    }
}