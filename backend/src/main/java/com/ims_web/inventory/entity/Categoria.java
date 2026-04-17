package com.ims_web.inventory.entity;

import com.ims_web.inventory.util.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "Categoria")
public class Categoria implements Auditable {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoriaId")
    private Long categoriaId;

    @Setter
    @Column(name = "CategoriaNombre", nullable = false)
    private String categoriaNombre;

    @Setter
    @ManyToOne
    @JoinColumn(name = "DescuentoId", referencedColumnName = "DescuentoId")
    private Descuento descuento;

    @Column(name = "CategoriaUsuarioCreacion", nullable = false)
    private String categoriaUsuarioCreacion;

    @Column(name = "CategoriaFechaCreacion", nullable = false)
    private LocalDateTime categoriaFechaCreacion;

    @Column(name = "CategoriaUsuarioModif")
    private String categoriaUsuarioModif;

    @Column(name = "CategoriaFechaModif")
    private LocalDateTime categoriaFechaModif;

    // =========================
    // GETTERS & SETTERS
    // =========================

    // =========================
    // Auditable interface
    // =========================
    @Override
    public void setUsuarioCreacion(String usuario) { this.categoriaUsuarioCreacion = usuario; }
    @Override
    public void setFechaCreacion(LocalDateTime fecha) { this.categoriaFechaCreacion = fecha; }
    @Override
    public void setUsuarioModif(String usuario) { this.categoriaUsuarioModif = usuario; }
    @Override
    public void setFechaModif(LocalDateTime fecha) { this.categoriaFechaModif = fecha; }

}