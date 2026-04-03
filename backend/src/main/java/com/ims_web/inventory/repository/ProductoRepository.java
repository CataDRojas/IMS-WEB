package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByProductoCodigo(String codigo);
}