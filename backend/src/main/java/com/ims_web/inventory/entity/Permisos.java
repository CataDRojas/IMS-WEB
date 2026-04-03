package com.ims_web.inventory.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "permisos")
public class Permisos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PermisosId")
    private Long permisosId;

    @Column(name = "PermisosNombre", nullable = false, unique = true)
    private String permisosNombre;

    // ================
    // GETTERS & SETTERS
    // ================

    public Long getPermisosId() {
        return permisosId;
    }

    public void setPermisosId(Long permisosId) {
        this.permisosId = permisosId;
    }

    public String getPermisosNombre() {
        return permisosNombre;
    }

    public void setPermisosNombre(String permisosNombre) {
        this.permisosNombre = permisosNombre;
    }
}