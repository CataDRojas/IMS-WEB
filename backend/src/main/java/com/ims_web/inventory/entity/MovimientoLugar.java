package com.ims_web.inventory.entity;

import com.ims_web.inventory.util.Auditable;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "MovimientoLugar")
public class MovimientoLugar implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MovimientoLugarId")
    private Long movimientoLugarId;

    @Column(name = "MovimientoLugarDescripcion", nullable = false)
    private String movimientoLugarDescripcion;

    @Column(name = "MovimientoLugarActivo", nullable = false)
    private Boolean movimientoLugarActivo = true;

    @Column(name = "MovimientoLugarUsuarioCreacion", nullable = false)
    private String movimientoLugarUsuarioCreacion;

    @Column(name = "MovimientoLugarFechaCreacion", nullable = false)
    private LocalDateTime movimientoLugarFechaCreacion;

    @Column(name = "MovimientoLugarUsuarioModif")
    private String movimientoLugarUsuarioModif;

    @Column(name = "MovimientoLugarFechaModif")
    private LocalDateTime movimientoLugarFechaModif;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getMovimientoLugarId() {
        return movimientoLugarId;
    }

    public void setMovimientoLugarId(Long movimientoLugarId) {
        this.movimientoLugarId = movimientoLugarId;
    }

    public String getMovimientoLugarDescripcion() {
        return movimientoLugarDescripcion;
    }

    public void setMovimientoLugarDescripcion(String movimientoLugarDescripcion) {
        this.movimientoLugarDescripcion = movimientoLugarDescripcion;
    }

    public Boolean getMovimientoLugarActivo() {
        return movimientoLugarActivo;
    }

    public void setMovimientoLugarActivo(Boolean movimientoLugarActivo) {
        this.movimientoLugarActivo = movimientoLugarActivo;
    }

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

    public String getMovimientoLugarUsuarioCreacion() {
        return movimientoLugarUsuarioCreacion;
    }

    public LocalDateTime getMovimientoLugarFechaCreacion() {
        return movimientoLugarFechaCreacion;
    }

    public String getMovimientoLugarUsuarioModif() {
        return movimientoLugarUsuarioModif;
    }

    public LocalDateTime getMovimientoLugarFechaModif() {
        return movimientoLugarFechaModif;
    }
}