package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository repo;

    public ProductoService(ProductoRepository repo) {
        this.repo = repo;
    }

    public List<Producto> getAllProductos() {
        return repo.findAll();
    }

    public Producto getProductoById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto not found"));
    }

    public Producto getProductoByCodigo(String codigo) {
        return repo.findByProductoCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Producto not found"));
    }

    public Producto createProducto(Producto producto) {
        if (producto.getProductoPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Precio cannot be negative");
        }
        if (producto.getProductoStock() < 0) {
            throw new RuntimeException("Stock cannot be negative");
        }
        return repo.save(producto);
    }

    public Producto updateProducto(Producto producto) {
        Producto existing = repo.findById(producto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto not found"));

        existing.setProductoNombre(producto.getProductoNombre());
        existing.setProductoDesc(producto.getProductoDesc());
        existing.setProductoActivo(producto.getProductoActivo());
        existing.setProductoStock(producto.getProductoStock());
        existing.setProductoCriticoNumero(producto.getProductoCriticoNumero());
        existing.setProductoPrecio(producto.getProductoPrecio());
        existing.setProductoCantidadLote(producto.getProductoCantidadLote());
        existing.setProductoCodigo(producto.getProductoCodigo());
        existing.setCategoria(producto.getCategoria());
        existing.setDescuento(producto.getDescuento());
        existing.setProductosUsuarioModif(producto.getProductosUsuarioModif());
        existing.setProductosFechaModif(producto.getProductosFechaModif());

        return repo.save(existing);
    }
}