package com.ims_web.inventory.entity;

import com.ims_web.inventory.util.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Movimiento")
public class Movimiento implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MovimientoId")
    private Long movimientoId;

    @Column(name = "MovimientoDescripcion")
    private String movimientoDescripcion;

    @Column(name = "MovimientoEstado")
    private String movimientoEstado; // DB default: PENDIENTE

    @Column(name = "MovimientoTipo")
    private String movimientoTipo;

    @Column(name = "MovimientoMetodoPago")
    private String movimientoMetodoPago;

    @Column(name = "MovimientoStock")
    private Integer movimientoStock;

    @Column(name = "MovimientoPrecioBase")
    private BigDecimal movimientoPrecioBase;

    @Column(name = "MovimientoPrecioNeto")
    private BigDecimal movimientoPrecioNeto;

    @Column(name = "MovimientoPrecioTotal")
    private BigDecimal movimientoPrecioTotal;

    @Column(name = "MovimientoDescuento")
    private BigDecimal movimientoDescuento;

    @Column(name = "MovimientoReferenciaExterna", unique = true)
    private String movimientoReferenciaExterna;

    @Column(name = "MovimientoUsuarioCreacion")
    private String movimientoUsuarioCreacion;

    @Column(name = "MovimientoFechaCreacion")
    private LocalDateTime movimientoFechaCreacion;

    @Column(name = "MovimientoUsuarioModif")
    private String movimientoUsuarioModif;

    @Column(name = "MovimientoFechaModif")
    private LocalDateTime movimientoFechaModif;

    @OneToMany(mappedBy = "movimiento", cascade = CascadeType.ALL)
    private List<MovimientoDetalle> detalles;

    @Override
    public void setUsuarioCreacion(String usuario) {
        this.movimientoUsuarioCreacion = usuario;
    }

    @Override
    public void setFechaCreacion(LocalDateTime fecha) {
        this.movimientoFechaCreacion = fecha;
    }

    @Override
    public void setUsuarioModif(String usuario) {
        this.movimientoUsuarioModif = usuario;
    }

    @Override
    public void setFechaModif(LocalDateTime fecha) {
        this.movimientoFechaModif = fecha;
    }
}