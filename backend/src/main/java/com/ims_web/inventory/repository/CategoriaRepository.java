package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByCategoriaNombreIgnoreCase(String nombre);
}