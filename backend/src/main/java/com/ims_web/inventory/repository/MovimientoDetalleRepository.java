package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.MovimientoDetalle;
import com.ims_web.inventory.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoDetalleRepository extends JpaRepository<MovimientoDetalle, Long> {
    List<MovimientoDetalle> findByMovimiento(Movimiento movimiento);
}