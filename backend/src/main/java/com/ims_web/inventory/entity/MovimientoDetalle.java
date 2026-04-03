package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

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

    // 🔒 CALCULATED BY DB
    @Column(name = "MovimientoDetallePrecioBase", nullable = false, insertable = false, updatable = false)
    private BigDecimal movimientoDetallePrecioBase;

    // 🔒 CALCULATED BY DB
    @Column(name = "MovimientoDetalleDescuentoAplicado", insertable = false, updatable = false)
    private BigDecimal movimientoDetalleDescuentoAplicado;

    // 🔒 CALCULATED BY DB
    @Column(name = "MovimientoDetallePrecioUnitario", nullable = false, insertable = false, updatable = false)
    private BigDecimal movimientoDetallePrecioUnitario;

    // 🔒 CALCULATED BY DB
    @Column(name = "MovimientoDetallePrecioTotal", nullable = false, insertable = false, updatable = false)
    private BigDecimal movimientoDetallePrecioTotal;

    @Column(name = "MovimientoDetalleDescripcion")
    private String movimientoDetalleDescripcion;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getMovimientoDetalleId() {
        return movimientoDetalleId;
    }

    public void setMovimientoDetalleId(Long movimientoDetalleId) {
        this.movimientoDetalleId = movimientoDetalleId;
    }

    public Movimiento getMovimiento() {
        return movimiento;
    }

    public void setMovimiento(Movimiento movimiento) {
        this.movimiento = movimiento;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public MovimientoLugar getMovimientoLugar() {
        return movimientoLugar;
    }

    public void setMovimientoLugar(MovimientoLugar movimientoLugar) {
        this.movimientoLugar = movimientoLugar;
    }

    public Integer getMovimientoDetalleCantidad() {
        return movimientoDetalleCantidad;
    }

    public void setMovimientoDetalleCantidad(Integer movimientoDetalleCantidad) {
        this.movimientoDetalleCantidad = movimientoDetalleCantidad;
    }

    public BigDecimal getMovimientoDetallePrecioBase() {
        return movimientoDetallePrecioBase;
    }

    public void setMovimientoDetallePrecioBase(BigDecimal movimientoDetallePrecioBase) {
        this.movimientoDetallePrecioBase = movimientoDetallePrecioBase;
    }

    public BigDecimal getMovimientoDetalleDescuentoAplicado() {
        return movimientoDetalleDescuentoAplicado;
    }

    public void setMovimientoDetalleDescuentoAplicado(BigDecimal movimientoDetalleDescuentoAplicado) {
        this.movimientoDetalleDescuentoAplicado = movimientoDetalleDescuentoAplicado;
    }

    public BigDecimal getMovimientoDetallePrecioUnitario() {
        return movimientoDetallePrecioUnitario;
    }

    public void setMovimientoDetallePrecioUnitario(BigDecimal movimientoDetallePrecioUnitario) {
        this.movimientoDetallePrecioUnitario = movimientoDetallePrecioUnitario;
    }

    public BigDecimal getMovimientoDetallePrecioTotal() {
        return movimientoDetallePrecioTotal;
    }

    public void setMovimientoDetallePrecioTotal(BigDecimal movimientoDetallePrecioTotal) {
        this.movimientoDetallePrecioTotal = movimientoDetallePrecioTotal;
    }

    public String getMovimientoDetalleDescripcion() {
        return movimientoDetalleDescripcion;
    }

    public void setMovimientoDetalleDescripcion(String movimientoDetalleDescripcion) {
        this.movimientoDetalleDescripcion = movimientoDetalleDescripcion;
    }
}