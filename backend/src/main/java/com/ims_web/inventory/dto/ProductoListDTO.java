package com.ims_web.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductoListDTO {

    private Long productoId;

    private String productoNombre;
    private String productoCodigo;

    private BigDecimal productoPrecio;
    private Integer productoStock;

    private Boolean productoActivo;

    // for filters + UI mapping
    private Long categoriaId;
    private String categoriaNombre;

    private Long descuentoId;
    private String descuentoNombre;

    // UI-critical flags
    private Boolean productoStockCritico;
    private Integer productoCriticoNumero;
    private Integer productoCantidadLote;
}