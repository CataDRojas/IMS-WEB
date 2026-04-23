package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
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

}