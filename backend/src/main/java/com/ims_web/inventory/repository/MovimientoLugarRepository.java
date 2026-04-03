package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.MovimientoLugar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List; // needed

@Repository
public interface MovimientoLugarRepository extends JpaRepository<MovimientoLugar, Long> {

    List<MovimientoLugar> findByMovimientoLugarActivoTrue();

    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END " +
            "FROM MovimientoDetalle d WHERE d.movimientoLugar.movimientoLugarId = :id")
    boolean existsInDetalle(Long id);
}
