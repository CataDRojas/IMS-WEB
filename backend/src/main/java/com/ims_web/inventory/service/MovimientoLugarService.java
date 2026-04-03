package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.MovimientoLugar;
import com.ims_web.inventory.repository.MovimientoLugarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List; // ✅ needed for List

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

    public MovimientoLugar createOrUpdate(MovimientoLugar lugar) {
        return repo.save(lugar);
    }

    /** HARD DELETE – unsafe if referenced */
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

    /** SOFT DELETE – just mark as inactive */
    @Transactional
    public MovimientoLugar softDelete(Long id) {
        MovimientoLugar lugar = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("MovimientoLugar not found"));

        lugar.setMovimientoLugarActivo(false);
        return repo.save(lugar);
    }
}
