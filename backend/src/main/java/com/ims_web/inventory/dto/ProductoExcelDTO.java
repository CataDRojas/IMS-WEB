package com.ims_web.inventory.dto;

import java.math.BigDecimal;

public class ProductoExcelDTO {

    private String codigo;
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
    private String categoria;
    private Integer cantidadLote;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getCantidadLote() {
        return cantidadLote;
    }

    public void setCantidadLote(Integer cantidadLote) {
        this.cantidadLote = cantidadLote;
    }
}