package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.MovimientoLugar;
import com.ims_web.inventory.repository.MovimientoLugarRepository;
import com.ims_web.inventory.util.AuditHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovimientoLugarService {

    private final MovimientoLugarRepository repo;

    public MovimientoLugarService(MovimientoLugarRepository repo) {
        this.repo = repo;
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

    @Transactional
    public MovimientoLugar createOrUpdate(MovimientoLugar lugar, String currentUser) {
        if (lugar.getMovimientoLugarId() == null) {
            // NUEVO: Viene sin ID. Le ponemos datos de creación y se va a la BD.
            // Asume que se crea como Activo por defecto en la BD.
            AuditHelper.setCreationAudit(lugar, currentUser);
            return repo.save(lugar);
        } else {
            // EDICIÓN: Trae ID. Buscamos el original primero para rescatar su fecha y
            // estado.
            MovimientoLugar lugarExistente = repo.findById(lugar.getMovimientoLugarId())
                    .orElseThrow(() -> new RuntimeException("MovimientoLugar not found"));

            // Pisamos SOLO el texto que el usuario cambió en el formulario
            lugarExistente.setMovimientoLugarDescripcion(lugar.getMovimientoLugarDescripcion());

            // Le pasamos el objeto mezclado al AuditHelper
            AuditHelper.setModificationAudit(lugarExistente, currentUser);

            // Guardamos el objeto
            return repo.save(lugarExistente);
        }
    }

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

    @Transactional
    public MovimientoLugar softDelete(Long id, String currentUser) {
        MovimientoLugar lugar = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("MovimientoLugar not found"));

        // TOGGLE instead of forcing false
        lugar.setMovimientoLugarActivo(!lugar.getMovimientoLugarActivo());

        AuditHelper.setModificationAudit(lugar, currentUser);
        return repo.save(lugar);
    }
}