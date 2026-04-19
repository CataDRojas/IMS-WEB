package com.ims_web.inventory.entity;

import com.ims_web.inventory.util.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AccessLevel;

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

    @Column(name = "MovimientoEstado", nullable = false)
    private String movimientoEstado = "PENDIENTE"; // PENDIENTE, CONFIRMADO, ANULADO

    @Column(name = "MovimientoTipo", nullable = false)
    private String movimientoTipo; // ENTRADA, SALIDA, AJUSTE

    @Column(name = "MovimientoMetodoPago")
    private String movimientoMetodoPago;

    // 🔒 CALCULATED BY DB
    @Setter(AccessLevel.NONE)
    @Column(name = "MovimientoStock", nullable = false, insertable = false, updatable = false)
    private Integer movimientoStock;

    @Setter(AccessLevel.NONE)
    @Column(name = "MovimientoPrecioBase", nullable = false, insertable = false, updatable = false)
    private BigDecimal movimientoPrecioBase;

    @Setter(AccessLevel.NONE)
    @Column(name = "MovimientoPrecioNeto", nullable = false, insertable = false, updatable = false)
    private BigDecimal movimientoPrecioNeto;

    @Setter(AccessLevel.NONE)
    @Column(name = "MovimientoPrecioTotal", nullable = false, insertable = false, updatable = false)
    private BigDecimal movimientoPrecioTotal;

    @Column(name = "MovimientoDescuento")
    private BigDecimal movimientoDescuento;

    @Column(name = "MovimientoReferenciaExterna", unique = true)
    private String movimientoReferenciaExterna;

    @Column(name = "MovimientoUsuarioCreacion", nullable = false)
    private String movimientoUsuarioCreacion;

    @Column(name = "MovimientoFechaCreacion", nullable = false)
    private LocalDateTime movimientoFechaCreacion;

    @Column(name = "MovimientoUsuarioModif")
    private String movimientoUsuarioModif;

    @Column(name = "MovimientoFechaModif")
    private LocalDateTime movimientoFechaModif;

    @OneToMany(mappedBy = "movimiento", cascade = CascadeType.ALL)
    private List<MovimientoDetalle> detalles;

    // =========================
    // Auditable implementation
    // =========================

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