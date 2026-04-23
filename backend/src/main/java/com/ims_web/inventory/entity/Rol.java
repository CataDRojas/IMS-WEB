package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
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

}