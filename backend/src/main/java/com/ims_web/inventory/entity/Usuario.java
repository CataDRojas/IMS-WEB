package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(columnNames = "UsuarioEmail")
})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UsuarioId")
    private Long usuarioId;

    @Column(name = "UsuarioEmail", length = 150, nullable = false)
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
    private LocalDateTime usuarioFechaModif;

    @ManyToOne
    @JoinColumn(name = "RolId", nullable = false)
    private Rol rol;

    @Column(name = "UsuarioActivo", nullable = false)
    private Boolean usuarioActivo = true;

}