package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "MovimientoLugar")
public class MovimientoLugar {

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

    public String getMovimientoLugarUsuarioCreacion() {
        return movimientoLugarUsuarioCreacion;
    }

    public void setMovimientoLugarUsuarioCreacion(String movimientoLugarUsuarioCreacion) {
        this.movimientoLugarUsuarioCreacion = movimientoLugarUsuarioCreacion;
    }

    public LocalDateTime getMovimientoLugarFechaCreacion() {
        return movimientoLugarFechaCreacion;
    }

    public void setMovimientoLugarFechaCreacion(LocalDateTime movimientoLugarFechaCreacion) {
        this.movimientoLugarFechaCreacion = movimientoLugarFechaCreacion;
    }

    public String getMovimientoLugarUsuarioModif() {
        return movimientoLugarUsuarioModif;
    }

    public void setMovimientoLugarUsuarioModif(String movimientoLugarUsuarioModif) {
        this.movimientoLugarUsuarioModif = movimientoLugarUsuarioModif;
    }

    public LocalDateTime getMovimientoLugarFechaModif() {
        return movimientoLugarFechaModif;
    }

    public void setMovimientoLugarFechaModif(LocalDateTime movimientoLugarFechaModif) {
        this.movimientoLugarFechaModif = movimientoLugarFechaModif;
    }
}