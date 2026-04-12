package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.repository.ProductoRepository;
import com.ims_web.inventory.util.AuditHelper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new EntityNotFoundException("Producto not found"));
    }

    public Producto getProductoByCodigo(String codigo) {
        return repo.findByProductoCodigo(codigo)
                .orElseThrow(() -> new EntityNotFoundException("Producto not found"));
    }

    @Transactional
    public Producto createProducto(Producto producto, String currentUser) {
        validateProducto(producto);

        // DB-level uniqueness check
        boolean codigoExists = repo.existsByProductoCodigoIgnoreCase(producto.getProductoCodigo());
        if (codigoExists) {
            throw new IllegalArgumentException("ProductoCodigo must be unique");
        }

        boolean nombreExists = repo.existsByProductoNombreIgnoreCase(producto.getProductoNombre());
        if (nombreExists) {
            throw new IllegalArgumentException("ProductoNombre must be unique");
        }

        AuditHelper.setCreationAudit(producto, currentUser);
        return repo.save(producto);
    }

    @Transactional
    public Producto updateProducto(Producto producto, String currentUser) {
        Producto existing = repo.findById(producto.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

        validateProducto(producto);

        // DB-level uniqueness checks excluding current entity
        boolean codigoExists = repo.existsByProductoCodigoIgnoreCaseAndProductoIdNot(
                producto.getProductoCodigo(), producto.getProductoId());
        if (codigoExists) {
            throw new IllegalArgumentException("ProductoCodigo must be unique");
        }

        boolean nombreExists = repo.existsByProductoNombreIgnoreCaseAndProductoIdNot(
                producto.getProductoNombre(), producto.getProductoId());
        if (nombreExists) {
            throw new IllegalArgumentException("ProductoNombre must be unique");
        }

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

        AuditHelper.setModificationAudit(existing, currentUser);

        return repo.save(existing);
    }

    private void validateProducto(Producto producto) {
        if (producto.getProductoPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Precio cannot be negative");
        }
        if (producto.getProductoStock() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
    }
}