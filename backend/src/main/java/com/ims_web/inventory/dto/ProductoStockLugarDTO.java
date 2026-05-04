package com.ims_web.inventory.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoStockLugarDTO {

    private Long movimientoLugarId;
    private String movimientoLugarDescripcion;
    private Integer stock;
    private Boolean prioridad;
}