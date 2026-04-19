package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "permisos")
public class Permisos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PermisosId")
    private Long permisosId;

    @Column(name = "PermisosNombre", nullable = false, unique = true)
    private String permisosNombre;


}