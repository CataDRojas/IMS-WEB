package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.MovimientoLugar;
import com.ims_web.inventory.repository.MovimientoLugarRepository;
import org.springframework.stereotype.Service;

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

    public MovimientoLugar createOrUpdate(MovimientoLugar lugar) {
        return repo.save(lugar);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}