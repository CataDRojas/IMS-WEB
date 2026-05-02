package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long>,
        JpaSpecificationExecutor<Movimiento> {

    Optional<Movimiento> findByMovimientoReferenciaExterna(String referencia);

    List<Movimiento> findByMovimientoEstadoAndMovimientoTipo(String estado, String tipo);
    List<Movimiento> findByMovimientoEstado(String estado);
}