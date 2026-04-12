package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {

    boolean existsByRolNombre(String rolNombre);

    Rol findByRolNombre(String rolNombre);

    // SQL-level uniqueness check for create/update
    boolean existsByRolNombreIgnoreCaseAndRolIdNot(String rolNombre, Long idToExclude);
}