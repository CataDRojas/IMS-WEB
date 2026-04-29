package com.ims_web.inventory.entity;

import com.ims_web.inventory.util.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "MovimientoLugar")
public class MovimientoLugar implements Auditable {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MovimientoLugarId")
    private Long movimientoLugarId;

    @Setter
    @Column(name = "MovimientoLugarDescripcion", nullable = false)
    private String movimientoLugarDescripcion;

    @Setter
    @Column(name = "MovimientoLugarActivo", nullable = false)
    private Boolean movimientoLugarActivo = true;

    @Setter
    @Column(name = "MovimientoLugarPrioridad", nullable = true)
    private Boolean movimientoLugarPrioridad = false;

    @Column(name = "MovimientoLugarUsuarioCreacion", nullable = false)
    private String movimientoLugarUsuarioCreacion;

    @Column(name = "MovimientoLugarFechaCreacion", nullable = false)
    private LocalDateTime movimientoLugarFechaCreacion;

    @Column(name = "MovimientoLugarUsuarioModif")
    private String movimientoLugarUsuarioModif;

    @Column(name = "MovimientoLugarFechaModif")
    private LocalDateTime movimientoLugarFechaModif;

    @Override
    public void setUsuarioCreacion(String usuarioCreacion) {
        this.movimientoLugarUsuarioCreacion = usuarioCreacion;
    }

    @Override
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.movimientoLugarFechaCreacion = fechaCreacion;
    }

    @Override
    public void setUsuarioModif(String usuarioModif) {
        this.movimientoLugarUsuarioModif = usuarioModif;
    }

    @Override
    public void setFechaModif(LocalDateTime fechaModif) {
        this.movimientoLugarFechaModif = fechaModif;
    }
}