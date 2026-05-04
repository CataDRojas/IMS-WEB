package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.MovimientoLugarProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MovimientoLugarProductoRepository
        extends JpaRepository<MovimientoLugarProducto, Long> {

    List<MovimientoLugarProducto> findByProducto_ProductoId(Long productoId);

    List<MovimientoLugarProducto> findByMovimientoLugar_MovimientoLugarId(Long movimientoLugarId);

    Optional<MovimientoLugarProducto> findByMovimientoLugar_MovimientoLugarIdAndProducto_ProductoId(
            Long movimientoLugarId,
            Long productoId
    );

    boolean existsByMovimientoLugar_MovimientoLugarIdAndProducto_ProductoId(
            Long movimientoLugarId,
            Long productoId
    );
    @Query("""
    SELECT COALESCE(SUM(m.movimientoLugarProductoStock), 0)
    FROM MovimientoLugarProducto m
    WHERE m.producto.productoId = :productoId
""")
    Integer sumStockByProductoId(@Param("productoId") Long productoId);
}