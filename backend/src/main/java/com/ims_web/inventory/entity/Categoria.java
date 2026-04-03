package com.ims_web.inventory.entity;

import com.ims_web.inventory.util.Auditable;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Categoria")
public class Categoria implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CategoriaId")
    private Long categoriaId;

    @Column(name = "CategoriaNombre", nullable = false)
    private String categoriaNombre;

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

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }

    public Descuento getDescuento() { return descuento; }
    public void setDescuento(Descuento descuento) { this.descuento = descuento; }

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

    public String getCategoriaUsuarioCreacion() { return categoriaUsuarioCreacion; }
    public LocalDateTime getCategoriaFechaCreacion() { return categoriaFechaCreacion; }
    public String getCategoriaUsuarioModif() { return categoriaUsuarioModif; }
    public LocalDateTime getCategoriaFechaModif() { return categoriaFechaModif; }
}