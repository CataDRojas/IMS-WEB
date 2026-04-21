package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "MovimientoDetalle")
public class MovimientoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MovimientoDetalleId")
    private Long movimientoDetalleId;

    @ManyToOne
    @JoinColumn(name = "MovimientoId", nullable = false)
    private Movimiento movimiento;

    @ManyToOne
    @JoinColumn(name = "ProductoId", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "MovimientoLugarId")
    private MovimientoLugar movimientoLugar;

    @Column(name = "MovimientoDetalleCantidad", nullable = false)
    private Integer movimientoDetalleCantidad;

    @Column(name = "MovimientoDetalleUnidadesPorPaquete", nullable = false)
    private Integer movimientoDetalleUnidadesPorPaquete = 1;

    @Setter(AccessLevel.NONE)
    @Column(name = "MovimientoDetallePrecioBase", nullable = false, insertable = false, updatable = false)
    private BigDecimal movimientoDetallePrecioBase;

    @Setter(AccessLevel.NONE)
    @Column(name = "MovimientoDetalleDescuentoAplicado", insertable = false, updatable = false)
    private BigDecimal movimientoDetalleDescuentoAplicado;

    @Setter(AccessLevel.NONE)
    @Column(name = "MovimientoDetallePrecioUnitario", nullable = false, insertable = false, updatable = false)
    private BigDecimal movimientoDetallePrecioUnitario;

    @Setter(AccessLevel.NONE)
    @Column(name = "MovimientoDetallePrecioTotal", nullable = false, insertable = false, updatable = false)
    private BigDecimal movimientoDetallePrecioTotal;

    @Column(name = "MovimientoDetalleDescripcion")
    private String movimientoDetalleDescripcion;
}