package com.ims_web.inventory.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "MovimientoLugarProducto",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"MovimientoLugarId", "ProductoId"}
        )
)
public class MovimientoLugarProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MovimientoLugarProductoId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MovimientoLugarId", nullable = false)
    private MovimientoLugar movimientoLugar;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ProductoId", nullable = false)
    private Producto producto;

    @Column(name = "MovimientoLugarProductoStock", nullable = true)
    private Integer movimientoLugarProductoStock = 0;
}