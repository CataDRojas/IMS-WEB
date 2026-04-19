package com.ims_web.inventory.entity;

import com.ims_web.inventory.util.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "productos")
public class Producto implements Auditable {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductoId")
    private Long productoId;

    @Setter
    @Column(name = "ProductoNombre", nullable = false)
    private String productoNombre;

    @Setter
    @Column(name = "ProductoDesc")
    private String productoDesc;

    @Setter
    @Column(name = "ProductoActivo", nullable = false)
    private Boolean productoActivo = true;

    // ⚠️ Setter restored ONLY for controlled ingestion (e.g. Excel import)
    @Setter
    @Column(name = "ProductoStock", nullable = false)
    private Integer productoStock;

    // 🔒 DB-CONTROLLED (trigger: trg_producto_stockcritico_update)
    @Column(name = "ProductoStockCritico", nullable = false, insertable = false, updatable = false)
    private Boolean productoStockCritico;

    @Setter
    @Column(name = "ProductoCriticoNumero", nullable = false)
    private Integer productoCriticoNumero = 0;

    @Setter
    @Column(name = "ProductoPrecio", nullable = false)
    private BigDecimal productoPrecio;

    @Setter
    @Column(name = "ProductoCantidadLote")
    private Integer productoCantidadLote;

    @Setter
    @Column(name = "ProductoCodigo", nullable = false, unique = true)
    private String productoCodigo;

    @Setter
    @ManyToOne
    @JoinColumn(name = "ProductoCategoria")
    private Categoria categoria;

    @Setter
    @ManyToOne
    @JoinColumn(name = "DescuentoId")
    private Descuento descuento;

    @Column(name = "ProductosUsuarioCreacion", nullable = false)
    private String productosUsuarioCreacion;

    @Column(name = "ProductosFechaCreacion", nullable = false)
    private LocalDateTime productosFechaCreacion;

    @Column(name = "ProductosUsuarioModif")
    private String productosUsuarioModif;

    @Column(name = "ProductosFechaModif")
    private LocalDateTime productosFechaModif;

    // =========================
    // Auditable implementation
    // =========================

    @Override
    public void setUsuarioCreacion(String usuario) {
        this.productosUsuarioCreacion = usuario;
    }

    @Override
    public void setFechaCreacion(LocalDateTime fecha) {
        this.productosFechaCreacion = fecha;
    }

    @Override
    public void setUsuarioModif(String usuario) {
        this.productosUsuarioModif = usuario;
    }

    @Override
    public void setFechaModif(LocalDateTime fecha) {
        this.productosFechaModif = fecha;
    }
}