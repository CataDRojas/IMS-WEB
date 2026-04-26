package com.ims_web.inventory.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MovimientoResponseDTO {

    private Long movimientoId;

    private String movimientoDescripcion;
    private String movimientoEstado;
    private String movimientoTipo;
    private String movimientoMetodoPago;

    private LocalDateTime movimientoFechaCreacion;
    private LocalDateTime movimientoFechaModif;

    private String movimientoUsuarioCreacion;
    private String movimientoUsuarioModif;

    // flattened safe structure (NO entity recursion)
    private List<MovimientoDetalleResponseDTO> detalles;
}