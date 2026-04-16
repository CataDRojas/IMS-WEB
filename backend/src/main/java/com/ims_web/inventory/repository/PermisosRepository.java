package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.Permisos;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PermisosRepository extends JpaRepository<Permisos, Long> {

    boolean existsByPermisosNombreIgnoreCase(String permisosNombre);
    Optional<Permisos> findByPermisosNombreIgnoreCase(String permisosNombre);
}