package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoriaId")
    private Long categoriaId;

    @Column(name = "CategoriaNombre", nullable = false)
    private String categoriaNombre;

    @ManyToOne
    @JoinColumn(name = "DescuentoId")
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

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public Descuento getDescuento() {
        return descuento;
    }

    public void setDescuento(Descuento descuento) {
        this.descuento = descuento;
    }

    public String getCategoriaUsuarioCreacion() {
        return categoriaUsuarioCreacion;
    }

    public void setCategoriaUsuarioCreacion(String categoriaUsuarioCreacion) {
        this.categoriaUsuarioCreacion = categoriaUsuarioCreacion;
    }

    public LocalDateTime getCategoriaFechaCreacion() {
        return categoriaFechaCreacion;
    }

    public void setCategoriaFechaCreacion(LocalDateTime categoriaFechaCreacion) {
        this.categoriaFechaCreacion = categoriaFechaCreacion;
    }

    public String getCategoriaUsuarioModif() {
        return categoriaUsuarioModif;
    }

    public void setCategoriaUsuarioModif(String categoriaUsuarioModif) {
        this.categoriaUsuarioModif = categoriaUsuarioModif;
    }

    public LocalDateTime getCategoriaFechaModif() {
        return categoriaFechaModif;
    }

    public void setCategoriaFechaModif(LocalDateTime categoriaFechaModif) {
        this.categoriaFechaModif = categoriaFechaModif;
    }
}