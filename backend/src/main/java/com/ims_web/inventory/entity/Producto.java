package com.ims_web.inventory.entity;

import com.ims_web.inventory.util.Auditable;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
public class Producto implements Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductoId")
    private Long productoId;

    @Column(name = "ProductoNombre", nullable = false)
    private String productoNombre;

    @Column(name = "ProductoDesc")
    private String productoDesc;

    @Column(name = "ProductoActivo", nullable = false)
    private Boolean productoActivo = true;

    @Column(name = "ProductoStock", nullable = false)
    private Integer productoStock = 0;

    @Column(name = "ProductoStockCritico", nullable = false)
    private Boolean productoStockCritico = false;

    @Column(name = "ProductoCriticoNumero", nullable = false)
    private Integer productoCriticoNumero = 0;

    @Column(name = "ProductoPrecio", nullable = false)
    private BigDecimal productoPrecio;

    @Column(name = "ProductoCantidadLote")
    private Integer productoCantidadLote;

    @Column(name = "ProductoCodigo", nullable = false, unique = true)
    private String productoCodigo;

    @ManyToOne
    @JoinColumn(name = "ProductoCategoria")
    private Categoria categoria;

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
    // GETTERS & SETTERS
    // =========================
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getProductoDesc() { return productoDesc; }
    public void setProductoDesc(String productoDesc) { this.productoDesc = productoDesc; }
    public Boolean getProductoActivo() { return productoActivo; }
    public void setProductoActivo(Boolean productoActivo) { this.productoActivo = productoActivo; }
    public Integer getProductoStock() { return productoStock; }
    public void setProductoStock(Integer productoStock) { this.productoStock = productoStock; }
    public Boolean getProductoStockCritico() { return productoStockCritico; }
    public void setProductoStockCritico(Boolean productoStockCritico) { this.productoStockCritico = productoStockCritico; }
    public Integer getProductoCriticoNumero() { return productoCriticoNumero; }
    public void setProductoCriticoNumero(Integer productoCriticoNumero) { this.productoCriticoNumero = productoCriticoNumero; }
    public BigDecimal getProductoPrecio() { return productoPrecio; }
    public void setProductoPrecio(BigDecimal productoPrecio) { this.productoPrecio = productoPrecio; }
    public Integer getProductoCantidadLote() { return productoCantidadLote; }
    public void setProductoCantidadLote(Integer productoCantidadLote) { this.productoCantidadLote = productoCantidadLote; }
    public String getProductoCodigo() { return productoCodigo; }
    public void setProductoCodigo(String productoCodigo) { this.productoCodigo = productoCodigo; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public Descuento getDescuento() { return descuento; }
    public void setDescuento(Descuento descuento) { this.descuento = descuento; }

    // =========================
    // Auditable interface
    // =========================
    @Override
    public void setUsuarioCreacion(String usuario) { this.productosUsuarioCreacion = usuario; }
    @Override
    public void setFechaCreacion(LocalDateTime fecha) { this.productosFechaCreacion = fecha; }
    @Override
    public void setUsuarioModif(String usuario) { this.productosUsuarioModif = usuario; }
    @Override
    public void setFechaModif(LocalDateTime fecha) { this.productosFechaModif = fecha; }

    public String getProductosUsuarioCreacion() { return productosUsuarioCreacion; }
    public LocalDateTime getProductosFechaCreacion() { return productosFechaCreacion; }
    public String getProductosUsuarioModif() { return productosUsuarioModif; }
    public LocalDateTime getProductosFechaModif() { return productosFechaModif; }
}