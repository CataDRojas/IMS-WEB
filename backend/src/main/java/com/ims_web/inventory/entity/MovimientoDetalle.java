package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    @JoinColumn(name = "MovimientoId")
    private Movimiento movimiento;

    @ManyToOne
    @JoinColumn(name = "ProductoId")
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "MovimientoLugarId")
    private MovimientoLugar movimientoLugar;

    @Column(name = "MovimientoDetalleCantidad")
    private Integer movimientoDetalleCantidad;

    @Column(name = "MovimientoDetalleUnidadesPorPaquete")
    private Integer movimientoDetalleUnidadesPorPaquete;

    @Column(name = "MovimientoDetallePrecioBase")
    private BigDecimal movimientoDetallePrecioBase;

    @Column(name = "MovimientoDetalleDescuentoAplicado")
    private BigDecimal movimientoDetalleDescuentoAplicado;

    @Column(name = "MovimientoDetallePrecioUnitario")
    private BigDecimal movimientoDetallePrecioUnitario;

    @Column(name = "MovimientoDetallePrecioTotal")
    private BigDecimal movimientoDetallePrecioTotal;

    @Column(name = "MovimientoDetalleDescripcion")
    private String movimientoDetalleDescripcion;
}