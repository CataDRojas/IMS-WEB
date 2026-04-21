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
            AuditHelper.setCreationAudit(lugar, currentUser); // static call
        } else {
            AuditHelper.setModificationAudit(lugar, currentUser); // static call
        }
        return repo.save(lugar);
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

        lugar.setMovimientoLugarActivo(false);
        AuditHelper.setModificationAudit(lugar, currentUser); // static call
        return repo.save(lugar);
    }
}