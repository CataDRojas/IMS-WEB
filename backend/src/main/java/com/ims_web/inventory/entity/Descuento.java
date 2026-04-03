package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Descuento")
public class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DescuentoId")
    private Long descuentoId;

    @Column(name = "DescuentoNombre", nullable = false)
    private String descuentoNombre;

    @Column(name = "DescuentoTipo", nullable = false)
    private String descuentoTipo; // FLAT | PORCENTAJE | MULTIPLICATIVO

    @Column(name = "DescuentoValor", nullable = false, precision = 12, scale = 2)
    private BigDecimal descuentoValor;

    @Column(name = "DescuentoActivo", nullable = false)
    private Boolean descuentoActivo = true;

    // =========================
    // Audit fields
    // =========================
    @Column(name = "DescuentoUsuarioCreacion", nullable = false)
    private String descuentoUsuarioCreacion;

    @Column(name = "DescuentoFechaCreacion", nullable = false)
    private LocalDateTime descuentoFechaCreacion;

    @Column(name = "DescuentoUsuarioModif")
    private String descuentoUsuarioModif;

    @Column(name = "DescuentoFechaModif")
    private LocalDateTime descuentoFechaModif;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getDescuentoId() { return descuentoId; }
    public void setDescuentoId(Long descuentoId) { this.descuentoId = descuentoId; }

    public String getDescuentoNombre() { return descuentoNombre; }
    public void setDescuentoNombre(String descuentoNombre) { this.descuentoNombre = descuentoNombre; }

    public String getDescuentoTipo() { return descuentoTipo; }
    public void setDescuentoTipo(String descuentoTipo) { this.descuentoTipo = descuentoTipo; }

    public BigDecimal getDescuentoValor() { return descuentoValor; }
    public void setDescuentoValor(BigDecimal descuentoValor) { this.descuentoValor = descuentoValor; }

    public Boolean getDescuentoActivo() { return descuentoActivo; }
    public void setDescuentoActivo(Boolean descuentoActivo) { this.descuentoActivo = descuentoActivo; }

    public String getDescuentoUsuarioCreacion() { return descuentoUsuarioCreacion; }
    public void setDescuentoUsuarioCreacion(String descuentoUsuarioCreacion) { this.descuentoUsuarioCreacion = descuentoUsuarioCreacion; }

    public LocalDateTime getDescuentoFechaCreacion() { return descuentoFechaCreacion; }
    public void setDescuentoFechaCreacion(LocalDateTime descuentoFechaCreacion) { this.descuentoFechaCreacion = descuentoFechaCreacion; }

    public String getDescuentoUsuarioModif() { return descuentoUsuarioModif; }
    public void setDescuentoUsuarioModif(String descuentoUsuarioModif) { this.descuentoUsuarioModif = descuentoUsuarioModif; }

    public LocalDateTime getDescuentoFechaModif() { return descuentoFechaModif; }
    public void setDescuentoFechaModif(LocalDateTime descuentoFechaModif) { this.descuentoFechaModif = descuentoFechaModif; }
}