package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Movimiento")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MovimientoId")
    private Long movimientoId;

    @Column(name = "MovimientoDescripcion")
    private String movimientoDescripcion;

    @Column(name = "MovimientoEstado", nullable = false)
    private String movimientoEstado = "PENDIENTE"; // PENDIENTE, CONFIRMADO, ANULADO

    @Column(name = "MovimientoTipo", nullable = false)
    private String movimientoTipo; // ENTRADA, SALIDA, AJUSTE

    @Column(name = "MovimientoMetodoPago")
    private String movimientoMetodoPago;

    // 🔒 CALCULATED BY DB
    @Column(name = "MovimientoStock", nullable = false, insertable = false, updatable = false)
    private Integer movimientoStock;

    // 🔒 CALCULATED BY DB
    @Column(name = "MovimientoPrecioBase", nullable = false, insertable = false, updatable = false)
    private BigDecimal movimientoPrecioBase;

    // 🔒 CALCULATED BY DB (NETO SIN IVA)
    @Column(name = "MovimientoPrecioNeto", nullable = false, insertable = false, updatable = false)
    private BigDecimal movimientoPrecioNeto;

    // 🔒 CALCULATED BY DB (TOTAL CON IVA)
    @Column(name = "MovimientoPrecioTotal", nullable = false, insertable = false, updatable = false)
    private BigDecimal movimientoPrecioTotal;

    @Column(name = "MovimientoDescuento")
    private BigDecimal movimientoDescuento;

    @Column(name = "MovimientoReferenciaExterna", unique = true)
    private String movimientoReferenciaExterna;

    @Column(name = "MovimientoUsuarioCreacion", nullable = false)
    private String movimientoUsuarioCreacion;

    @Column(name = "MovimientoFechaCreacion", nullable = false)
    private LocalDateTime movimientoFechaCreacion;

    @Column(name = "MovimientoUsuarioModif")
    private String movimientoUsuarioModif;

    @Column(name = "MovimientoFechaModif")
    private LocalDateTime movimientoFechaModif;

    @OneToMany(mappedBy = "movimiento", cascade = CascadeType.ALL)
    private List<MovimientoDetalle> detalles;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getMovimientoId() {
        return movimientoId;
    }

    public void setMovimientoId(Long movimientoId) {
        this.movimientoId = movimientoId;
    }

    public String getMovimientoDescripcion() {
        return movimientoDescripcion;
    }

    public void setMovimientoDescripcion(String movimientoDescripcion) {
        this.movimientoDescripcion = movimientoDescripcion;
    }

    public String getMovimientoEstado() {
        return movimientoEstado;
    }

    public void setMovimientoEstado(String movimientoEstado) {
        this.movimientoEstado = movimientoEstado;
    }

    public String getMovimientoTipo() {
        return movimientoTipo;
    }

    public void setMovimientoTipo(String movimientoTipo) {
        this.movimientoTipo = movimientoTipo;
    }

    public String getMovimientoMetodoPago() {
        return movimientoMetodoPago;
    }

    public void setMovimientoMetodoPago(String movimientoMetodoPago) {
        this.movimientoMetodoPago = movimientoMetodoPago;
    }

    public Integer getMovimientoStock() {
        return movimientoStock;
    }

    public void setMovimientoStock(Integer movimientoStock) {
        this.movimientoStock = movimientoStock;
    }

    public BigDecimal getMovimientoPrecioBase() {
        return movimientoPrecioBase;
    }

    public void setMovimientoPrecioBase(BigDecimal movimientoPrecioBase) {
        this.movimientoPrecioBase = movimientoPrecioBase;
    }

    public BigDecimal getMovimientoPrecioNeto() {
        return movimientoPrecioNeto;
    }

    public void setMovimientoPrecioNeto(BigDecimal movimientoPrecioNeto) {
        this.movimientoPrecioNeto = movimientoPrecioNeto;
    }

    public BigDecimal getMovimientoPrecioTotal() {
        return movimientoPrecioTotal;
    }

    public void setMovimientoPrecioTotal(BigDecimal movimientoPrecioTotal) {
        this.movimientoPrecioTotal = movimientoPrecioTotal;
    }

    public BigDecimal getMovimientoDescuento() {
        return movimientoDescuento;
    }

    public void setMovimientoDescuento(BigDecimal movimientoDescuento) {
        this.movimientoDescuento = movimientoDescuento;
    }

    public String getMovimientoReferenciaExterna() {
        return movimientoReferenciaExterna;
    }

    public void setMovimientoReferenciaExterna(String movimientoReferenciaExterna) {
        this.movimientoReferenciaExterna = movimientoReferenciaExterna;
    }

    public String getMovimientoUsuarioCreacion() {
        return movimientoUsuarioCreacion;
    }

    public void setMovimientoUsuarioCreacion(String movimientoUsuarioCreacion) {
        this.movimientoUsuarioCreacion = movimientoUsuarioCreacion;
    }

    public LocalDateTime getMovimientoFechaCreacion() {
        return movimientoFechaCreacion;
    }

    public void setMovimientoFechaCreacion(LocalDateTime movimientoFechaCreacion) {
        this.movimientoFechaCreacion = movimientoFechaCreacion;
    }

    public String getMovimientoUsuarioModif() {
        return movimientoUsuarioModif;
    }

    public void setMovimientoUsuarioModif(String movimientoUsuarioModif) {
        this.movimientoUsuarioModif = movimientoUsuarioModif;
    }

    public LocalDateTime getMovimientoFechaModif() {
        return movimientoFechaModif;
    }

    public void setMovimientoFechaModif(LocalDateTime movimientoFechaModif) {
        this.movimientoFechaModif = movimientoFechaModif;
    }

    public List<MovimientoDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<MovimientoDetalle> detalles) {
        this.detalles = detalles;
    }
}