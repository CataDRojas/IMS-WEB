package com.ims_web.inventory.dto;

import java.math.BigDecimal;

public class MovimientoDetalleDTO {

    private Long movimientoDetalleId;
    private Long productoId;
    private String productoNombre;
    private int cantidad;

    private BigDecimal precioBase;
    private BigDecimal descuentoAplicado;
    private BigDecimal precioUnitario;
    private BigDecimal precioTotal;

    private Long movimientoId;

    // =========================
    // GETTERS AND SETTERS
    // =========================

    public Long getMovimientoDetalleId() {
        return movimientoDetalleId;
    }

    public void setMovimientoDetalleId(Long movimientoDetalleId) {
        this.movimientoDetalleId = movimientoDetalleId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public BigDecimal getDescuentoAplicado() {
        return descuentoAplicado;
    }

    public void setDescuentoAplicado(BigDecimal descuentoAplicado) {
        this.descuentoAplicado = descuentoAplicado;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(BigDecimal precioTotal) {
        this.precioTotal = precioTotal;
    }

    public Long getMovimientoId() {
        return movimientoId;
    }

    public void setMovimientoId(Long movimientoId) {
        this.movimientoId = movimientoId;
    }
}