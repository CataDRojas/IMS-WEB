package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.Usuario;
import com.ims_web.inventory.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    List<Usuario> findByUsuarioActivoTrue();

    // ✅ Check if any user is assigned to a specific role
    boolean existsByRol(Rol rol);
}