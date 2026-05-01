package com.ims_web.inventory.service;

import com.ims_web.inventory.dto.*;
import com.ims_web.inventory.entity.*;
import com.ims_web.inventory.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.ims_web.inventory.util.AuditHelper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

@Service
public class MovimientoService {

    private final MovimientoRepository repo;
    private final MovimientoDetalleRepository detalleRepo;
    private final ProductoRepository productoRepo;
    private final MovimientoLugarRepository lugarRepo;
    private final MovimientoLugarProductoRepository mlpRepo;
    private final EntityManager entityManager;
    private final ProductoStockSyncService syncService;

    public MovimientoService(
            MovimientoRepository repo,
            MovimientoDetalleRepository detalleRepo,
            ProductoRepository productoRepo,
            MovimientoLugarRepository lugarRepo,
            MovimientoLugarProductoRepository mlpRepo,
            EntityManager entityManager,
            ProductoStockSyncService syncService) {
        this.repo = repo;
        this.detalleRepo = detalleRepo;
        this.productoRepo = productoRepo;
        this.lugarRepo = lugarRepo;
        this.mlpRepo = mlpRepo;
        this.entityManager = entityManager;
        this.syncService = syncService;
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
                .stream().map(this::toDTO).toList();
    }

    // =========================================================
    // BORRADOR
    // =========================================================

    @Transactional
    public MovimientoResponseDTO guardarBorrador(
            Movimiento movimiento,
            List<MovimientoDetalleRequestDTO> detalles) {

        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalStateException("El inventario debe tener al menos un producto");
        }

        movimiento.setMovimientoEstado("PENDIENTE");
        AuditHelper.setCreationAudit(movimiento, null);
        Movimiento saved = repo.save(movimiento);

        for (MovimientoDetalleRequestDTO dto : detalles) {

            Producto producto = productoRepo.findById(dto.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

            MovimientoDetalle detalle = new MovimientoDetalle();
            detalle.setMovimiento(saved);
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

            if (dto.getMovimientoLugarId() != null) {
                lugarRepo.findById(dto.getMovimientoLugarId())
                        .ifPresent(detalle::setMovimientoLugar);
            }

            detalleRepo.save(detalle);
        }

        entityManager.flush();
        return toDTO(saved);
    }

    // =========================================================
    // CREATE
    // =========================================================

    @Transactional
    public MovimientoResponseDTO create(
            Movimiento movimiento,
            List<MovimientoDetalleRequestDTO> detalles) {

        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalStateException("Movimiento must contain at least one detalle");
        }

        if (movimiento.getMovimientoEstado() == null || movimiento.getMovimientoEstado().isBlank()) {
            movimiento.setMovimientoEstado("PENDIENTE");
        }

        simulateStockAndRules(movimiento.getMovimientoTipo(), detalles);

        AuditHelper.setCreationAudit(movimiento, null);

        Movimiento saved = repo.save(movimiento);

        for (MovimientoDetalleRequestDTO dto : detalles) {

            Producto producto = productoRepo.findById(dto.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

            MovimientoDetalle detalle = new MovimientoDetalle();
            detalle.setMovimiento(saved);
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

            if (dto.getMovimientoLugarId() != null) {
                lugarRepo.findById(dto.getMovimientoLugarId())
                        .ifPresent(detalle::setMovimientoLugar);
            }

            detalleRepo.save(detalle);
        }

        entityManager.flush();
        return toDTO(saved);
    }

    // =========================================================
    // CONFIRMATION
    // =========================================================

    public MovimientoResponseDTO confirmarMovimiento(Long id) {
        Movimiento m = repo.findById(id).orElseThrow();
        m.setMovimientoEstado("CONFIRMADO");

        Movimiento saved = repo.save(m);

        registerAfterCommit(() -> syncService.syncMovimiento(saved.getMovimientoId()));

        return toDTO(saved);
    }

    public MovimientoResponseDTO anularMovimiento(Long id) {
        Movimiento m = repo.findById(id).orElseThrow();

        m.setMovimientoEstado("ANULADO");

        Movimiento saved = repo.save(m);

        registerAfterCommit(() -> syncService.syncMovimiento(saved.getMovimientoId()));

        return toDTO(saved);
    }

    public MovimientoResponseDTO reactivarMovimiento(Long id) {
        Movimiento m = repo.findById(id).orElseThrow();
        m.setMovimientoEstado("CONFIRMADO");

        Movimiento saved = repo.save(m);

        registerAfterCommit(() -> syncService.syncMovimiento(saved.getMovimientoId()));

        return toDTO(saved);
    }

    // =========================================================
    // STOCK SIMULATION
    // =========================================================

    private void simulateStockAndRules(
            String tipoMovimiento,
            List<MovimientoDetalleRequestDTO> detalles) {

        for (MovimientoDetalleRequestDTO d : detalles) {

            Producto p = productoRepo.findById(d.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

            int cantidadReal = d.getMovimientoDetalleCantidad() *
                    (d.getMovimientoDetalleUnidadesPorPaquete() != null
                            ? d.getMovimientoDetalleUnidadesPorPaquete()
                            : 1);

            Integer currentStock = mlpRepo.sumStockByProductoId(p.getProductoId());
            if (currentStock == null)
                currentStock = 0;

            if ("SALIDA".equals(tipoMovimiento)) {

                int projected = currentStock - cantidadReal;

                if (projected < 0) {
                    throw new IllegalStateException(
                            "ERR_STOCK_NEGATIVE|Product " + p.getProductoId());
                }
            }

            if ("AJUSTE".equals(tipoMovimiento)) {

                if (cantidadReal < 0) {
                    throw new IllegalStateException(
                            "ERR_ADJUSTMENT_NEGATIVE|Product " + p.getProductoId());
                }
            }
        }
    }

    // =========================================================
    // SYNC WRAPPER
    // =========================================================

    private void registerAfterCommit(Runnable task) {

        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });

        } else {
            task.run();
        }
    }

    // =========================================================
    // UPDATE / DELETE / SEARCH
    // =========================================================

    public MovimientoResponseDTO update(Movimiento movimiento) {

        Movimiento existing = repo.findById(movimiento.getMovimientoId())
                .orElseThrow(() -> new EntityNotFoundException("Movimiento not found"));

        existing.setMovimientoDescripcion(movimiento.getMovimientoDescripcion());
        existing.setMovimientoEstado(movimiento.getMovimientoEstado());
        existing.setMovimientoTipo(movimiento.getMovimientoTipo());
        existing.setMovimientoMetodoPago(movimiento.getMovimientoMetodoPago());

        AuditHelper.setModificationAudit(existing, null);

        return toDTO(repo.save(existing));
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public Page<MovimientoResponseDTO> search(
            String tipo,
            String estado,
            String usuario,
            String desde,
            String hasta,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        Specification<Movimiento> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (tipo != null)
                p.add(cb.equal(root.get("movimientoTipo"), tipo));
            if (estado != null)
                p.add(cb.equal(root.get("movimientoEstado"), estado));

            return cb.and(p.toArray(new Predicate[0]));
        };

        return repo.findAll(spec, pageable).map(this::toDTO);
    }

    // =========================================================
    // DTO MAPPERS (UNCHANGED)
    // =========================================================

    private MovimientoResponseDTO toDTO(Movimiento m) {
        MovimientoResponseDTO dto = new MovimientoResponseDTO();
        dto.setMovimientoId(m.getMovimientoId());
        dto.setMovimientoDescripcion(m.getMovimientoDescripcion());
        dto.setMovimientoEstado(m.getMovimientoEstado());
        dto.setMovimientoTipo(m.getMovimientoTipo());
        dto.setMovimientoMetodoPago(m.getMovimientoMetodoPago());
        dto.setMovimientoPrecioTotal(m.getMovimientoPrecioTotal());
        dto.setMovimientoPrecioNeto(m.getMovimientoPrecioNeto());
        dto.setMovimientoDescuento(m.getMovimientoDescuento());
        dto.setMovimientoFechaCreacion(m.getMovimientoFechaCreacion());
        dto.setMovimientoFechaModif(m.getMovimientoFechaModif());
        dto.setMovimientoUsuarioCreacion(m.getMovimientoUsuarioCreacion());
        dto.setMovimientoUsuarioModif(m.getMovimientoUsuarioModif());

        dto.setDetalles(
                detalleRepo.findByMovimiento(m)
                        .stream().map(this::mapDetalle).toList());

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
        dto.setMovimientoDetalleDescripcion(d.getMovimientoDetalleDescripcion());

        dto.setMovimientoDetallePrecioBase(d.getMovimientoDetallePrecioBase());
        dto.setMovimientoDetallePrecioUnitario(d.getMovimientoDetallePrecioUnitario());
        dto.setMovimientoDetallePrecioTotal(d.getMovimientoDetallePrecioTotal());
        dto.setMovimientoDetalleDescuentoAplicado(d.getMovimientoDetalleDescuentoAplicado());

        if (d.getMovimientoLugar() != null) {
            dto.setMovimientoLugarId(d.getMovimientoLugar().getMovimientoLugarId());
            dto.setMovimientoLugarNombre(d.getMovimientoLugar().getMovimientoLugarDescripcion());
        }

        return dto;
    }
}