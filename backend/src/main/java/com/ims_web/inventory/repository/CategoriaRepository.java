package com.ims_web.inventory.repository;

import com.ims_web.inventory.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}