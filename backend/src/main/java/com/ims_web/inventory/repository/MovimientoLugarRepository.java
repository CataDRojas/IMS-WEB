package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.MovimientoLugar;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoLugarRepository extends JpaRepository<MovimientoLugar, Long> {
    List<MovimientoLugar> findByMovimientoLugarActivoTrue();
}