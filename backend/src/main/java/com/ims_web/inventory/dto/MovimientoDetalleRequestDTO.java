package com.ims_web.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MovimientoDetalleRequestDTO {

    private Long productoId;
    private Long movimientoLugarId;

    private Integer movimientoDetalleCantidad;
    private Integer movimientoDetalleUnidadesPorPaquete;

    private BigDecimal movimientoDetallePrecioBase;
    private BigDecimal movimientoDetallePrecioUnitario;
    private BigDecimal movimientoDetallePrecioTotal;

    private BigDecimal movimientoDetalleDescuentoAplicado;

    private String movimientoDetalleDescripcion;
}