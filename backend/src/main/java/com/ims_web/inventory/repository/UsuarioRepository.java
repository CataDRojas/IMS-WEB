package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
}