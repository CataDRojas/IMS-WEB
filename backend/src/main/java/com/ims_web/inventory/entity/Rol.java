package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "Rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RolId")
    private Long rolId;

    @Column(name = "RolNombre", nullable = false, unique = true)
    private String rolNombre;

    @ManyToMany
    @JoinTable(
            name = "RolPermisos",
            joinColumns = @JoinColumn(name = "RolId"),
            inverseJoinColumns = @JoinColumn(name = "PermisosId")
    )
    private Set<Permisos> permisos;

    // ================
    // GETTERS & SETTERS
    // ================

    public Long getRolId() {
        return rolId;
    }

    public void setRolId(Long rolId) {
        this.rolId = rolId;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
    }

    public Set<Permisos> getPermisos() {
        return permisos;
    }

    public void setPermisos(Set<Permisos> permisos) {
        this.permisos = permisos;
    }
}