package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.Usuario;
import com.ims_web.inventory.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    List<Usuario> findByUsuarioActivoTrue();

    // ✅ Check if any user is assigned to a specific role
    boolean existsByRol(Rol rol);

    // Lookup by email
    Optional<Usuario> findByUsuarioEmail(String email);

    // Check email uniqueness
    boolean existsByUsuarioEmail(String email);

    Optional<Usuario> findByUsuarioEmailIgnoreCase(String usuarioEmail);
}