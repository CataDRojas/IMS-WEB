package com.ims_web.inventory.entity;

import com.ims_web.inventory.util.Auditable;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Descuento")
public class Descuento implements Auditable {

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
    // Getters & Setters
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
    public LocalDateTime getDescuentoFechaCreacion() { return descuentoFechaCreacion; }

    public String getDescuentoUsuarioModif() { return descuentoUsuarioModif; }
    public LocalDateTime getDescuentoFechaModif() { return descuentoFechaModif; }

    // =========================
    // Auditable implementation
    // =========================
    @Override
    public void setUsuarioCreacion(String usuario) { this.descuentoUsuarioCreacion = usuario; }
    @Override
    public void setFechaCreacion(LocalDateTime fecha) { this.descuentoFechaCreacion = fecha; }
    @Override
    public void setUsuarioModif(String usuario) { this.descuentoUsuarioModif = usuario; }
    @Override
    public void setFechaModif(LocalDateTime fecha) { this.descuentoFechaModif = fecha; }
}