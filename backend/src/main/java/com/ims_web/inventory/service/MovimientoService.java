package com.ims_web.inventory.service;

import com.ims_web.inventory.dto.MovimientoDetalleRequestDTO;
import com.ims_web.inventory.dto.MovimientoDetalleResponseDTO;
import com.ims_web.inventory.dto.MovimientoResponseDTO;
import com.ims_web.inventory.entity.Movimiento;
import com.ims_web.inventory.entity.MovimientoDetalle;
import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.repository.MovimientoDetalleRepository;
import com.ims_web.inventory.repository.MovimientoRepository;
import com.ims_web.inventory.repository.ProductoRepository;
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
    private final ProductoRepository productoRepo;
    private final EntityManager entityManager;

    public MovimientoService(MovimientoRepository repo,
            MovimientoDetalleRepository detalleRepo,
            ProductoRepository productoRepo,
            EntityManager entityManager) {
        this.repo = repo;
        this.detalleRepo = detalleRepo;
        this.productoRepo = productoRepo;
        this.entityManager = entityManager;
    }

    // =========================================================
    // READ
    // =========================================================

    public List<MovimientoResponseDTO> getAll() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    public MovimientoResponseDTO getById(Long id) {
        Movimiento movimiento = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento not found"));
        return toDTO(movimiento);
    }

    public List<MovimientoResponseDTO> getPendientesEntrada() {
        return repo.findByMovimientoEstadoAndMovimientoTipo("PENDIENTE", "ENTRADA")
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // =========================================================
    // GUARDAR BORRADOR (cabecera + detalles, estado PENDIENTE)
    // =========================================================

    @Transactional
    public MovimientoResponseDTO guardarBorrador(Movimiento movimiento,
            List<MovimientoDetalleRequestDTO> detalles,
            String currentUser) {

        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalStateException("El inventario debe tener al menos un producto");
        }

        movimiento.setMovimientoEstado("PENDIENTE");

        AuditHelper.setCreationAudit(movimiento, currentUser);
        Movimiento savedMovimiento = repo.save(movimiento);

        for (MovimientoDetalleRequestDTO dto : detalles) {
            Producto producto = productoRepo.findById(dto.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

            MovimientoDetalle detalle = new MovimientoDetalle();
            detalle.setMovimiento(savedMovimiento);
            detalle.setProducto(producto);
            detalle.setMovimientoDetalleCantidad(dto.getMovimientoDetalleCantidad());
            detalle.setMovimientoDetalleUnidadesPorPaquete(
                    dto.getMovimientoDetalleUnidadesPorPaquete() != null
                            ? dto.getMovimientoDetalleUnidadesPorPaquete()
                            : 1);
            detalle.setMovimientoDetalleDescripcion(dto.getMovimientoDetalleDescripcion());
            detalle.setMovimientoDetallePrecioBase(dto.getMovimientoDetallePrecioBase());
            detalle.setMovimientoDetallePrecioUnitario(dto.getMovimientoDetallePrecioUnitario());
            detalle.setMovimientoDetallePrecioTotal(dto.getMovimientoDetallePrecioTotal());
            detalle.setMovimientoDetalleDescuentoAplicado(dto.getMovimientoDetalleDescuentoAplicado());
            detalleRepo.save(detalle);
        }

        entityManager.flush();
        entityManager.createNativeQuery("CALL sp_recalcular_movimiento(:id)")
                .setParameter("id", savedMovimiento.getMovimientoId())
                .executeUpdate();

        return toDTO(savedMovimiento);
    }

    // =========================================================
    // CREATE ATOMIC (para ventas)
    // =========================================================

    @Transactional
    public MovimientoResponseDTO create(Movimiento movimiento,
            List<MovimientoDetalleRequestDTO> detalles,
            String currentUser) {

        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalStateException("Movimiento must contain at least one detalle");
        }

        if (movimiento.getMovimientoEstado() == null || movimiento.getMovimientoEstado().isBlank()) {
            movimiento.setMovimientoEstado("PENDIENTE");
        }

        simulateStockAndRules(movimiento.getMovimientoTipo(), detalles);

        AuditHelper.setCreationAudit(movimiento, currentUser);
        Movimiento savedMovimiento = repo.save(movimiento);

        for (MovimientoDetalleRequestDTO dto : detalles) {
            Producto producto = productoRepo.findById(dto.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

            MovimientoDetalle detalle = new MovimientoDetalle();
            detalle.setMovimiento(savedMovimiento);
            detalle.setProducto(producto);
            detalle.setMovimientoDetalleCantidad(dto.getMovimientoDetalleCantidad());
            detalle.setMovimientoDetalleUnidadesPorPaquete(
                    dto.getMovimientoDetalleUnidadesPorPaquete() != null
                            ? dto.getMovimientoDetalleUnidadesPorPaquete()
                            : 1);
            detalle.setMovimientoDetalleDescripcion(dto.getMovimientoDetalleDescripcion());
            detalle.setMovimientoDetallePrecioBase(dto.getMovimientoDetallePrecioBase());
            detalle.setMovimientoDetallePrecioUnitario(dto.getMovimientoDetallePrecioUnitario());
            detalle.setMovimientoDetallePrecioTotal(dto.getMovimientoDetallePrecioTotal());
            detalle.setMovimientoDetalleDescuentoAplicado(dto.getMovimientoDetalleDescuentoAplicado());
            detalleRepo.save(detalle);
        }

        entityManager.flush();

        entityManager.createNativeQuery("CALL sp_recalcular_movimiento(:id)")
                .setParameter("id", savedMovimiento.getMovimientoId())
                .executeUpdate();

        return toDTO(savedMovimiento);
    }

    // =========================================================
    // SIMULATION CORE
    // =========================================================

    private void simulateStockAndRules(String tipoMovimiento,
            List<MovimientoDetalleRequestDTO> detalles) {

        for (MovimientoDetalleRequestDTO d : detalles) {
            Producto p = productoRepo.findById(d.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

            int cantidadReal = d.getMovimientoDetalleCantidad() *
                    (d.getMovimientoDetalleUnidadesPorPaquete() != null
                            ? d.getMovimientoDetalleUnidadesPorPaquete()
                            : 1);

            if ("SALIDA".equals(tipoMovimiento) || "AJUSTE".equals(tipoMovimiento)) {
                int projectedStock = p.getProductoStock() - cantidadReal;
                if (projectedStock < 0) {
                    throw new IllegalStateException(
                            "ERR_STOCK_NEGATIVE|Product " + p.getProductoId() + " would go below zero");
                }
            }
        }
    }

    // =========================================================
    // UPDATE
    // =========================================================

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

    // =========================================================
    // CONFIRM
    // =========================================================

    @Transactional
    public MovimientoResponseDTO confirmarMovimiento(Long id, String currentUser) {
        Movimiento movimiento = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movimiento not found"));

        if ("CONFIRMADO".equals(movimiento.getMovimientoEstado())) {
            throw new IllegalStateException("Movimiento already confirmed");
        }

        movimiento.setMovimientoEstado("CONFIRMADO");
        AuditHelper.setModificationAudit(movimiento, currentUser);
        return toDTO(repo.save(movimiento));
    }

    // =========================================================
    // DELETE
    // =========================================================

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

    // =========================================================
    // DTO MAPPING
    // =========================================================

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

        // ✅ LUGAR
        if (d.getMovimientoLugar() != null) {
            dto.setMovimientoLugarId(d.getMovimientoLugar().getMovimientoLugarId());
            dto.setMovimientoLugarNombre(d.getMovimientoLugar().getMovimientoLugarDescripcion());
        }

        return dto;
    }
}