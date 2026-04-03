package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

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
    // GETTERS & SETTERS
    // =========================

    public Long getDescuentoId() {
        return descuentoId;
    }

    public void setDescuentoId(Long descuentoId) {
        this.descuentoId = descuentoId;
    }

    public String getDescuentoNombre() {
        return descuentoNombre;
    }

    public void setDescuentoNombre(String descuentoNombre) {
        this.descuentoNombre = descuentoNombre;
    }

    public String getDescuentoTipo() {
        return descuentoTipo;
    }

    public void setDescuentoTipo(String descuentoTipo) {
        this.descuentoTipo = descuentoTipo;
    }

    public BigDecimal getDescuentoValor() {
        return descuentoValor;
    }

    public void setDescuentoValor(BigDecimal descuentoValor) {
        this.descuentoValor = descuentoValor;
    }

    public Boolean getDescuentoActivo() {
        return descuentoActivo;
    }

    public void setDescuentoActivo(Boolean descuentoActivo) {
        this.descuentoActivo = descuentoActivo;
    }
}