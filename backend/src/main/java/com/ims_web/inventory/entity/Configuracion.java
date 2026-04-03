package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Configuracion")
public class Configuracion {

    @Id
    @Column(name = "ConfiguracionId")
    private Byte configuracionId = 1; // always 1

    @Column(name = "EmpresaNombre", nullable = false)
    private String empresaNombre;

    @Column(name = "EmpresaDireccion", nullable = false)
    private String empresaDireccion;

    @Column(name = "EmpresaRun")
    private String empresaRun;

    @Column(name = "EmpresaDV")
    private String empresaDV;

    @Column(name = "IVA", nullable = false)
    private BigDecimal iva;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Byte getConfiguracionId() {
        return configuracionId;
    }

    public void setConfiguracionId(Byte configuracionId) {
        this.configuracionId = configuracionId;
    }

    public String getEmpresaNombre() {
        return empresaNombre;
    }

    public void setEmpresaNombre(String empresaNombre) {
        this.empresaNombre = empresaNombre;
    }

    public String getEmpresaDireccion() {
        return empresaDireccion;
    }

    public void setEmpresaDireccion(String empresaDireccion) {
        this.empresaDireccion = empresaDireccion;
    }

    public String getEmpresaRun() {
        return empresaRun;
    }

    public void setEmpresaRun(String empresaRun) {
        this.empresaRun = empresaRun;
    }

    public String getEmpresaDV() {
        return empresaDV;
    }

    public void setEmpresaDV(String empresaDV) {
        this.empresaDV = empresaDV;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }
}