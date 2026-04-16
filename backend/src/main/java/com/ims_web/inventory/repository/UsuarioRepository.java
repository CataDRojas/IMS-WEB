package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.Usuario;
import com.ims_web.inventory.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findByUsuarioActivoTrue();

    boolean existsByRol(Rol rol);

    Optional<Usuario> findByUsuarioEmail(String email);

    boolean existsByUsuarioEmail(String email);

    Optional<Usuario> findByUsuarioEmailIgnoreCase(String usuarioEmail);

    // =========================
    // SECURITY SAFE FETCH (NO LAZY ISSUES)
    // =========================
    @Query("""
        SELECT u FROM Usuario u
        JOIN FETCH u.rol r
        JOIN FETCH r.permisos
        WHERE LOWER(u.usuarioEmail) = LOWER(:email)
    """)
    Optional<Usuario> findByEmailWithRoleAndPermissions(String email);
}