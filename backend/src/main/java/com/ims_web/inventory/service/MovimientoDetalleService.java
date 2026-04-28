package com.ims_web.inventory.service;

import com.ims_web.inventory.dto.MovimientoDetalleRequestDTO;
import com.ims_web.inventory.dto.MovimientoDetalleResponseDTO;
import com.ims_web.inventory.entity.Movimiento;
import com.ims_web.inventory.entity.MovimientoDetalle;
import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.repository.MovimientoDetalleRepository;
import com.ims_web.inventory.repository.MovimientoLugarRepository;
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
    private final MovimientoLugarRepository lugarRepo;
    private final EntityManager entityManager;

    public MovimientoDetalleService(MovimientoDetalleRepository detalleRepo,
            MovimientoRepository movimientoRepo,
            ProductoRepository productoRepo,
            MovimientoLugarRepository lugarRepo,
            EntityManager entityManager) {
        this.detalleRepo = detalleRepo;
        this.movimientoRepo = movimientoRepo;
        this.productoRepo = productoRepo;
        this.lugarRepo = lugarRepo;
        this.entityManager = entityManager;
    }

    public List<MovimientoDetalleResponseDTO> getAllDetalles() {
        return detalleRepo.findAll().stream().map(this::toDTO).toList();
    }

    public MovimientoDetalleResponseDTO getDetalleById(Long id) {
        MovimientoDetalle detalle = detalleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MovimientoDetalle not found"));
        return toDTO(detalle);
    }

    @Transactional
    public MovimientoDetalleResponseDTO createDetalle(Long movimientoId,
            MovimientoDetalleRequestDTO incoming) {

        Movimiento movimiento = getValidatedMovimiento(movimientoId);

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
                        : 1);
        detalle.setMovimientoDetalleDescripcion(incoming.getMovimientoDetalleDescripcion());
        detalle.setMovimientoDetallePrecioBase(incoming.getMovimientoDetallePrecioBase());
        detalle.setMovimientoDetallePrecioUnitario(incoming.getMovimientoDetallePrecioUnitario());
        detalle.setMovimientoDetallePrecioTotal(incoming.getMovimientoDetallePrecioTotal());
        detalle.setMovimientoDetalleDescuentoAplicado(incoming.getMovimientoDetalleDescuentoAplicado());

        // LUGAR
        if (incoming.getMovimientoLugarId() != null) {
            lugarRepo.findById(incoming.getMovimientoLugarId())
                    .ifPresent(detalle::setMovimientoLugar);
        }

        validateStockImpact(movimiento, detalle, null);

        MovimientoDetalle saved = detalleRepo.save(detalle);
        entityManager.flush();
        entityManager.refresh(saved);

        entityManager.createNativeQuery("CALL sp_recalcular_movimiento(:id)")
                .setParameter("id", movimientoId)
                .executeUpdate();

        return toDTO(saved);
    }

    @Transactional
    public List<MovimientoDetalleResponseDTO> createDetallesBatch(
            Long movimientoId,
            List<MovimientoDetalleRequestDTO> detalles) {

        Movimiento movimiento = getValidatedMovimiento(movimientoId);
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
                            : 1);
            detalle.setMovimientoDetalleDescripcion(incoming.getMovimientoDetalleDescripcion());
            detalle.setMovimientoDetallePrecioBase(incoming.getMovimientoDetallePrecioBase());
            detalle.setMovimientoDetallePrecioUnitario(incoming.getMovimientoDetallePrecioUnitario());
            detalle.setMovimientoDetallePrecioTotal(incoming.getMovimientoDetallePrecioTotal());
            detalle.setMovimientoDetalleDescuentoAplicado(incoming.getMovimientoDetalleDescuentoAplicado());

            // LUGAR
            if (incoming.getMovimientoLugarId() != null) {
                lugarRepo.findById(incoming.getMovimientoLugarId())
                        .ifPresent(detalle::setMovimientoLugar);
            }

            validateStockImpact(movimiento, detalle, null);
            savedList.add(detalleRepo.save(detalle));
        }

        entityManager.flush();
        for (MovimientoDetalle d : savedList) {
            entityManager.refresh(d);
        }

        entityManager.createNativeQuery("CALL sp_recalcular_movimiento(:id)")
                .setParameter("id", movimientoId)
                .executeUpdate();

        return savedList.stream().map(this::toDTO).toList();
    }

    @Transactional
    public MovimientoDetalleResponseDTO updateDetalle(Long id,
            MovimientoDetalleRequestDTO incoming) {

        MovimientoDetalle existing = detalleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MovimientoDetalle not found"));

        Movimiento movimiento = getValidatedMovimiento(existing.getMovimiento().getMovimientoId());
        validate(incoming);

        MovimientoDetalle projected = new MovimientoDetalle();
        projected.setMovimiento(existing.getMovimiento());
        projected.setProducto(existing.getProducto());
        projected.setMovimientoDetalleId(existing.getMovimientoDetalleId());
        projected.setMovimientoDetalleCantidad(incoming.getMovimientoDetalleCantidad());
        projected.setMovimientoDetalleUnidadesPorPaquete(incoming.getMovimientoDetalleUnidadesPorPaquete());
        projected.setMovimientoDetalleDescripcion(incoming.getMovimientoDetalleDescripcion());

        validateStockImpact(movimiento, projected, existing);

        existing.setMovimientoDetalleCantidad(incoming.getMovimientoDetalleCantidad());
        existing.setMovimientoDetalleUnidadesPorPaquete(incoming.getMovimientoDetalleUnidadesPorPaquete());
        existing.setMovimientoDetalleDescripcion(incoming.getMovimientoDetalleDescripcion());

        // LUGAR en update también
        if (incoming.getMovimientoLugarId() != null) {
            lugarRepo.findById(incoming.getMovimientoLugarId())
                    .ifPresent(existing::setMovimientoLugar);
        }

        MovimientoDetalle updated = detalleRepo.save(existing);
        entityManager.flush();
        entityManager.refresh(updated);

        entityManager.createNativeQuery("CALL sp_recalcular_movimiento(:id)")
                .setParameter("id", movimiento.getMovimientoId())
                .executeUpdate();

        return toDTO(updated);
    }

    @Transactional
    public void deleteDetalle(Long id) {

        MovimientoDetalle detalle = detalleRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MovimientoDetalle not found"));

        Movimiento movimiento = getValidatedMovimiento(detalle.getMovimiento().getMovimientoId());
        validateStockImpact(movimiento, null, detalle);

        detalleRepo.delete(detalle);
        entityManager.flush();

        entityManager.createNativeQuery("CALL sp_recalcular_movimiento(:id)")
                .setParameter("id", movimiento.getMovimientoId())
                .executeUpdate();
    }

    private void validateStockImpact(Movimiento movimiento,
            MovimientoDetalle newState,
            MovimientoDetalle oldState) {

        List<MovimientoDetalle> all = detalleRepo.findByMovimiento(movimiento);

        if (oldState != null) {
            all.removeIf(d -> d.getMovimientoDetalleId().equals(oldState.getMovimientoDetalleId()));
        }
        if (newState != null) {
            all.add(newState);
        }

        for (MovimientoDetalle d : all) {
            Producto p = productoRepo.findById(d.getProducto().getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

            int unidades = d.getMovimientoDetalleCantidad() *
                    (d.getMovimientoDetalleUnidadesPorPaquete() != null
                            ? d.getMovimientoDetalleUnidadesPorPaquete()
                            : 1);

            if ("SALIDA".equals(movimiento.getMovimientoTipo())) {
                int projectedStock = p.getProductoStock() - unidades;
                if (projectedStock < 0) {
                    throw new IllegalStateException(
                            "ERR_STOCK_NEGATIVE|Product " + p.getProductoId() + " would go below zero");
                }
            }
        }
    }

    private void validate(MovimientoDetalleRequestDTO dto) {
        if (dto.getMovimientoDetalleCantidad() == null || dto.getMovimientoDetalleCantidad() <= 0) {
            throw new IllegalArgumentException("Cantidad must be > 0");
        }
        if (dto.getMovimientoDetalleUnidadesPorPaquete() != null &&
                dto.getMovimientoDetalleUnidadesPorPaquete() < 1) {
            throw new IllegalArgumentException("UnidadesPorPaquete must be >= 1");
        }
    }

    private Movimiento getValidatedMovimiento(Long movimientoId) {
        Movimiento movimiento = movimientoRepo.findById(movimientoId)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento not found"));
        if ("CONFIRMADO".equals(movimiento.getMovimientoEstado())) {
            throw new IllegalStateException("Cannot modify a confirmed movimiento");
        }
        return movimiento;
    }

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

        // LUGAR
        if (d.getMovimientoLugar() != null) {
            dto.setMovimientoLugarId(d.getMovimientoLugar().getMovimientoLugarId());
            dto.setMovimientoLugarNombre(d.getMovimientoLugar().getMovimientoLugarDescripcion());
        }

        return dto;
    }
}