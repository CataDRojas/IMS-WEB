package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Transactional
    public Producto createProducto(Producto producto, String currentUser) {
        validateProducto(producto);

        LocalDateTime now = LocalDateTime.now();
        producto.setProductosUsuarioCreacion(currentUser);
        producto.setProductosFechaCreacion(now);

        return repo.save(producto);
    }

    @Transactional
    public Producto updateProducto(Producto producto, String currentUser) {
        Producto existing = repo.findById(producto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto not found"));

        validateProducto(producto);

        // Update fields
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

        // Audit
        LocalDateTime now = LocalDateTime.now();
        existing.setProductosUsuarioModif(currentUser);
        existing.setProductosFechaModif(now);

        return repo.save(existing);
    }

    private void validateProducto(Producto producto) {
        if (producto.getProductoPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Precio cannot be negative");
        }
        if (producto.getProductoStock() < 0) {
            throw new RuntimeException("Stock cannot be negative");
        }
    }
}