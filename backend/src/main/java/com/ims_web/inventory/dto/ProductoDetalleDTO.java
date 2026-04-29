package com.ims_web.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProductoDetalleDTO {

    private Long productoId;
    private String productoNombre;
    private String productoCodigo;
    private BigDecimal productoPrecio;
    private Integer productoStock;

    private List<ProductoStockLugarDTO> stockPorLugar;
}