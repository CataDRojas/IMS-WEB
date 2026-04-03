package com.ims_web.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MovimientoDTO {

    private Long movimientoId;
    private String movimientoDescripcion;
    private String movimientoEstado;
    private String movimientoTipo;
    private String movimientoMetodoPago;

    private int movimientoStock;
    private BigDecimal movimientoPrecioBase;
    private BigDecimal movimientoPrecioNeto;
    private BigDecimal movimientoPrecioTotal;
    private BigDecimal movimientoDescuento;

    private String movimientoUsuarioCreacion;
    private LocalDateTime movimientoFechaCreacion;
    private String movimientoUsuarioModif;
    private LocalDateTime movimientoFechaModif;

    private List<MovimientoDetalleDTO> detalles;

    // =========================
    // GETTERS AND SETTERS
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

    public int getMovimientoStock() {
        return movimientoStock;
    }

    public void setMovimientoStock(int movimientoStock) {
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

    public List<MovimientoDetalleDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<MovimientoDetalleDTO> detalles) {
        this.detalles = detalles;
    }
}