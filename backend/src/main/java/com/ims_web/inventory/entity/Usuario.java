package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @Column(name = "UsuarioEmail", length = 150)
    private String usuarioEmail;

    @Column(name = "UsuarioNombre", nullable = false)
    private String usuarioNombre;

    @Column(name = "UsuarioRun", length = 20)
    private String usuarioRun;

    @Column(name = "UsuarioDV", length = 5)
    private String usuarioDV;

    @Column(name = "UsuarioPassword", nullable = false)
    private String usuarioPassword;

    @Column(name = "UsuarioFechaCreacion", nullable = false)
    private LocalDateTime usuarioFechaCreacion;

    @Column(name = "UsuarioFechaModif")
    private LocalDateTime usuarioFechaModif; // <-- new field

    @ManyToOne
    @JoinColumn(name = "RolId", nullable = false)
    private Rol rol;

    @Column(name = "UsuarioActivo", nullable = false)
    private Boolean usuarioActivo = true;

    // ========================
    // Getters & Setters
    // ========================

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getUsuarioRun() {
        return usuarioRun;
    }

    public void setUsuarioRun(String usuarioRun) {
        this.usuarioRun = usuarioRun;
    }

    public String getUsuarioDV() {
        return usuarioDV;
    }

    public void setUsuarioDV(String usuarioDV) {
        this.usuarioDV = usuarioDV;
    }

    public String getUsuarioPassword() {
        return usuarioPassword;
    }

    public void setUsuarioPassword(String usuarioPassword) {
        this.usuarioPassword = usuarioPassword;
    }

    public LocalDateTime getUsuarioFechaCreacion() {
        return usuarioFechaCreacion;
    }

    public void setUsuarioFechaCreacion(LocalDateTime usuarioFechaCreacion) {
        this.usuarioFechaCreacion = usuarioFechaCreacion;
    }

    public LocalDateTime getUsuarioFechaModif() {
        return usuarioFechaModif;
    }

    public void setUsuarioFechaModif(LocalDateTime usuarioFechaModif) {
        this.usuarioFechaModif = usuarioFechaModif;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Boolean getUsuarioActivo() {
        return usuarioActivo;
    }

    public void setUsuarioActivo(Boolean usuarioActivo) {
        this.usuarioActivo = usuarioActivo;
    }
}