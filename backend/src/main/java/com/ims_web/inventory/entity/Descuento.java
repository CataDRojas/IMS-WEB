package com.ims_web.inventory.entity;

import com.ims_web.inventory.util.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "Descuento")
public class Descuento implements Auditable {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DescuentoId")
    private Long descuentoId;

    @Setter
    @Column(name = "DescuentoNombre", nullable = false)
    private String descuentoNombre;

    @Setter
    @Column(name = "DescuentoTipo", nullable = false)
    private String descuentoTipo; // FLAT | PORCENTAJE | MULTIPLICATIVO

    @Setter
    @Column(name = "DescuentoValor", nullable = false, precision = 12, scale = 2)
    private BigDecimal descuentoValor;

    @Setter
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