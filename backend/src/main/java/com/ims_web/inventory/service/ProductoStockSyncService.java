package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Movimiento;
import com.ims_web.inventory.entity.MovimientoDetalle;
import com.ims_web.inventory.repository.MovimientoDetalleRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductoStockSyncService {

    private final EntityManager entityManager;
    private final MovimientoDetalleRepository detalleRepo;

    public ProductoStockSyncService(
            EntityManager entityManager,
            MovimientoDetalleRepository detalleRepo
    ) {
        this.entityManager = entityManager;
        this.detalleRepo = detalleRepo;
    }

    // =========================================================
    // SINGLE PRODUCT SYNC
    // =========================================================

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sync(Long productoId) {
        entityManager.createNativeQuery("CALL sp_sync_producto_stock(:id)")
                .setParameter("id", productoId)
                .executeUpdate();
    }

    // =========================================================
    // SYNC BY MOVIMIENTO (FIXED)
    // =========================================================

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncMovimiento(Long movimientoId) {

        // reuse existing repository method (NO new query methods needed)
        Movimiento fake = new Movimiento();
        fake.setMovimientoId(movimientoId);

        List<MovimientoDetalle> detalles = detalleRepo.findByMovimiento(fake);

        Set<Long> productoIds = detalles.stream()
                .map(d -> d.getProducto().getProductoId())
                .collect(Collectors.toSet());

        for (Long productoId : productoIds) {
            entityManager.createNativeQuery("CALL sp_sync_producto_stock(:id)")
                    .setParameter("id", productoId)
                    .executeUpdate();
        }

        // 🔥 NEW: recalculation procedure
        entityManager.createNativeQuery("CALL sp_recalcular_movimiento(:id)")
                .setParameter("id", movimientoId)
                .executeUpdate();
    }

    // =========================================================
    // OPTIONAL BATCH SYNC
    // =========================================================

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncBatch(List<Long> productoIds) {
        productoIds.stream()
                .distinct()
                .forEach(this::sync);
    }

}