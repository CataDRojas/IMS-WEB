package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.MovimientoLugar;
import com.ims_web.inventory.entity.MovimientoLugarProducto;
import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.repository.MovimientoLugarProductoRepository;
import com.ims_web.inventory.repository.MovimientoLugarRepository;
import com.ims_web.inventory.repository.ProductoRepository;
import com.ims_web.inventory.util.AuditHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovimientoLugarService {

    private final MovimientoLugarRepository repo;
    private final ProductoRepository productoRepo;
    private final MovimientoLugarProductoRepository mlpRepo;

    public MovimientoLugarService(
            MovimientoLugarRepository repo,
            ProductoRepository productoRepo,
            MovimientoLugarProductoRepository mlpRepo
    ) {
        this.repo = repo;
        this.productoRepo = productoRepo;
        this.mlpRepo = mlpRepo;
    }

    public List<MovimientoLugar> getAll() {
        return repo.findAll();
    }

    public List<MovimientoLugar> getActive() {
        return repo.findByMovimientoLugarActivoTrue();
    }

    public MovimientoLugar getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("MovimientoLugar not found"));
    }

    // =========================
    // CREATE / UPDATE
    // =========================

    @Transactional
    public MovimientoLugar createOrUpdate(MovimientoLugar lugar, String currentUser) {

        if (lugar.getMovimientoLugarId() == null) {

            // =========================
            // CREATE
            // =========================

            if (lugar.getMovimientoLugarActivo() == null) {
                lugar.setMovimientoLugarActivo(true);
            }

            AuditHelper.setCreationAudit(lugar, currentUser);

            MovimientoLugar saved = repo.save(lugar);

            ensureLugarInAllProductos(saved);

            return saved;
        }

        // =========================
        // UPDATE
        // =========================

        MovimientoLugar lugarExistente = repo.findById(lugar.getMovimientoLugarId())
                .orElseThrow(() -> new RuntimeException("MovimientoLugar not found"));

        lugarExistente.setMovimientoLugarDescripcion(lugar.getMovimientoLugarDescripcion());
        lugarExistente.setMovimientoLugarPrioridad(lugar.getMovimientoLugarPrioridad());

        // 🔥 CRITICAL FIX: allow reactivation / deactivation via update
        if (lugar.getMovimientoLugarActivo() != null) {
            lugarExistente.setMovimientoLugarActivo(lugar.getMovimientoLugarActivo());
        }

        AuditHelper.setModificationAudit(lugarExistente, currentUser);

        return repo.save(lugarExistente);
    }

    // =========================
    // DELETE
    // =========================

    @Transactional
    public void delete(Long id) {

        MovimientoLugar lugar = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("MovimientoLugar not found"));

        boolean inUse = repo.existsInDetalle(id);

        if (inUse) {
            throw new RuntimeException("Cannot delete MovimientoLugar: referenced by MovimientoDetalle");
        }

        repo.delete(lugar);
    }

    // =========================
    // SOFT DELETE (NOW EXPLICIT, NOT TOGGLE)
    // =========================

    @Transactional
    public MovimientoLugar softDelete(Long id, String currentUser) {

        MovimientoLugar lugar = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("MovimientoLugar not found"));

        // 🔥 explicit state change instead of toggle
        lugar.setMovimientoLugarActivo(false);

        AuditHelper.setModificationAudit(lugar, currentUser);

        return repo.save(lugar);
    }

    // =========================
    // CORE GRID LOGIC
    // =========================

    private void ensureLugarInAllProductos(MovimientoLugar lugar) {

        List<Long> existingProductoIds = mlpRepo
                .findByMovimientoLugar_MovimientoLugarId(lugar.getMovimientoLugarId())
                .stream()
                .map(mlp -> mlp.getProducto().getProductoId())
                .toList();

        List<Producto> productos = productoRepo.findAll();

        List<MovimientoLugarProducto> toCreate = productos.stream()
                .filter(p -> !existingProductoIds.contains(p.getProductoId()))
                .map(p -> {
                    MovimientoLugarProducto mlp = new MovimientoLugarProducto();
                    mlp.setMovimientoLugar(lugar);
                    mlp.setProducto(p);
                    mlp.setMovimientoLugarProductoStock(0);
                    return mlp;
                })
                .toList();

        if (!toCreate.isEmpty()) {
            mlpRepo.saveAll(toCreate);
        }
    }
}