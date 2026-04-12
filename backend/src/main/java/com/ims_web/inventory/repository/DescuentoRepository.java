package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.Descuento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DescuentoRepository extends JpaRepository<Descuento, Long> {

    List<Descuento> findByDescuentoActivoTrue();

    // SQL-level uniqueness check for create/update
    boolean existsByDescuentoNombreIgnoreCaseAndDescuentoIdNot(String nombre, Long idToExclude);
}