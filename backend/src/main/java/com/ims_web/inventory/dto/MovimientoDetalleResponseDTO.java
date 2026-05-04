package com.ims_web.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MovimientoDetalleResponseDTO {

    private Long movimientoDetalleId;

    private Long movimientoId;

    private Long productoId;
    private String productoNombre;

    private Integer movimientoDetalleCantidad;
    private Integer movimientoDetalleUnidadesPorPaquete;

    private BigDecimal movimientoDetallePrecioBase;
    private BigDecimal movimientoDetallePrecioUnitario;
    private BigDecimal movimientoDetallePrecioTotal;

    private BigDecimal movimientoDetalleDescuentoAplicado;

    private String movimientoDetalleDescripcion;

    private Long movimientoLugarId;
    private String movimientoLugarNombre;
}