package com.ims_web.inventory.service;

import com.ims_web.inventory.dto.ProductoExcelDTO;
import com.ims_web.inventory.entity.Categoria;
import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.repository.CategoriaRepository;
import com.ims_web.inventory.repository.ProductoRepository;
import com.ims_web.inventory.util.AuditHelper;
import com.ims_web.inventory.util.excel.ProductoExcelExporter;
import com.ims_web.inventory.util.excel.ProductoExcelImporter;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository repo;
    private final ProductoExcelImporter excelImporter;
    private final ProductoExcelExporter excelExporter;
    private final CategoriaRepository categoriaRepo;

    public ProductoService(
            ProductoRepository repo,
            ProductoExcelImporter excelImporter,
            ProductoExcelExporter excelExporter,
            CategoriaRepository categoriaRepo
    ) {
        this.repo = repo;
        this.excelImporter = excelImporter;
        this.excelExporter = excelExporter;
        this.categoriaRepo = categoriaRepo;
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

    // =========================
    // EXISTING METHODS (UNCHANGED)
    // =========================

    @Transactional
    public Producto createProducto(Producto producto, String currentUser) {
        validateProducto(producto);

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

    // =========================
    // EXCEL IMPORT
    // =========================
    @Transactional
    public void importFromExcel(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {

            List<ProductoExcelDTO> productos = excelImporter.importProductos(inputStream);

            for (ProductoExcelDTO dto : productos) {
                Producto producto = new Producto();

                producto.setProductoCodigo(dto.getCodigo());
                producto.setProductoNombre(dto.getNombre());
                producto.setProductoPrecio(dto.getPrecio());
                producto.setProductoStock(dto.getStock());

                // 🔥 EL TORPEDO (Cantidad por Lote)
                // Si viene nulo desde el Excel, por defecto es 1 unidad.
                Integer cantidadLote = dto.getCantidadLote() != null ? dto.getCantidadLote() : 1;
                producto.setProductoCantidadLote(cantidadLote);

                // Lógica de Categorías
                if (dto.getCategoria() != null && !dto.getCategoria().trim().isEmpty()) {
                    String catNombre = dto.getCategoria().trim();
                    Categoria categoria = categoriaRepo.findByCategoriaNombreIgnoreCase(catNombre)
                            .orElseGet(() -> {
                                Categoria nuevaCat = new Categoria();
                                nuevaCat.setCategoriaNombre(catNombre);
                                AuditHelper.setCreationAudit(nuevaCat, "EXCEL_IMPORT");
                                return categoriaRepo.save(nuevaCat);
                            });
                    producto.setCategoria(categoria);
                }

                boolean exists = repo.existsByProductoCodigoIgnoreCase(dto.getCodigo());

                if (exists) {
                    Producto existing = repo.findByProductoCodigo(dto.getCodigo())
                            .orElseThrow();

                    existing.setProductoNombre(dto.getNombre());
                    existing.setProductoPrecio(dto.getPrecio());
                    existing.setProductoStock(dto.getStock());
                    existing.setCategoria(producto.getCategoria());

                    // 🔥 Actualizamos también el torpedo si el producto ya existía
                    existing.setProductoCantidadLote(cantidadLote);

                    repo.save(existing);
                } else {
                    AuditHelper.setCreationAudit(producto, "EXCEL_IMPORT");
                    repo.save(producto);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to import Excel file", e);
        }
    }

    // =========================
    // EXCEL EXPORT
    // =========================
    public Workbook exportToExcel() {

        List<Producto> productos = repo.findAll();

        List<ProductoExcelDTO> dtos = productos.stream().map(p -> {
            ProductoExcelDTO dto = new ProductoExcelDTO();
            dto.setCodigo(p.getProductoCodigo());
            dto.setNombre(p.getProductoNombre());
            dto.setPrecio(p.getProductoPrecio());
            dto.setStock(p.getProductoStock());
            dto.setCategoria(p.getCategoria() != null ? p.getCategoria().getCategoriaNombre() : null);

            // 🔥 Exportamos el torpedo al Excel
            dto.setCantidadLote(p.getProductoCantidadLote());

            return dto;
        }).toList();

        return excelExporter.exportProductos(dtos);
    }
}