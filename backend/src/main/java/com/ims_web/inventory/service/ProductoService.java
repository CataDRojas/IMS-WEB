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
    // CREATE
    // =========================

    @Transactional
    public Producto createProducto(Producto producto, String currentUser) {

        validateProducto(producto);

        if (repo.existsByProductoCodigoIgnoreCase(producto.getProductoCodigo())) {
            throw new IllegalArgumentException("ProductoCodigo must be unique");
        }

        if (repo.existsByProductoNombreIgnoreCase(producto.getProductoNombre())) {
            throw new IllegalArgumentException("ProductoNombre must be unique");
        }

        AuditHelper.setCreationAudit(producto, currentUser);

        return repo.save(producto);
    }

    // =========================
    // UPDATE
    // =========================

    @Transactional
    public Producto updateProducto(Producto producto, String currentUser) {

        Producto existing = repo.findById(producto.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

        validateProducto(producto);

        if (repo.existsByProductoCodigoIgnoreCaseAndProductoIdNot(
                producto.getProductoCodigo(), producto.getProductoId())) {
            throw new IllegalArgumentException("ProductoCodigo must be unique");
        }

        if (repo.existsByProductoNombreIgnoreCaseAndProductoIdNot(
                producto.getProductoNombre(), producto.getProductoId())) {
            throw new IllegalArgumentException("ProductoNombre must be unique");
        }

        existing.setProductoNombre(producto.getProductoNombre());
        existing.setProductoDesc(producto.getProductoDesc());
        existing.setProductoActivo(producto.getProductoActivo());

        existing.setProductoCriticoNumero(producto.getProductoCriticoNumero());
        existing.setProductoPrecio(producto.getProductoPrecio());
        existing.setProductoCantidadLote(producto.getProductoCantidadLote());
        existing.setProductoCodigo(producto.getProductoCodigo());
        existing.setCategoria(producto.getCategoria());
        existing.setDescuento(producto.getDescuento());

        AuditHelper.setModificationAudit(existing, currentUser);

        return repo.save(existing);
    }

    // =========================
    // VALIDATION
    // =========================

    private void validateProducto(Producto producto) {

        if (producto.getProductoPrecio() == null ||
                producto.getProductoPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Precio cannot be negative");
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

                // 🔥 ESCUDOS ANTI-CAÍDAS (Evitan el Error 500 por filas vacías)
                if (dto.getCodigo() == null || dto.getCodigo().trim().isEmpty()) continue;
                if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) continue;
                if (dto.getPrecio() == null) continue;

                Integer cantidadLote = dto.getCantidadLote() != null ? dto.getCantidadLote() : 1;

                Categoria categoria = null;

                if (dto.getCategoria() != null && !dto.getCategoria().trim().isEmpty()) {
                    String catNombre = dto.getCategoria().trim();

                    categoria = categoriaRepo.findByCategoriaNombreIgnoreCase(catNombre)
                            .orElseGet(() -> {
                                Categoria nuevaCat = new Categoria();
                                nuevaCat.setCategoriaNombre(catNombre);
                                AuditHelper.setCreationAudit(nuevaCat, "EXCEL_IMPORT");
                                return categoriaRepo.save(nuevaCat);
                            });
                }

                boolean exists = repo.existsByProductoCodigoIgnoreCase(dto.getCodigo());

                if (exists) {

                    Producto existing = repo.findByProductoCodigo(dto.getCodigo())
                            .orElseThrow();

                    existing.setProductoNombre(dto.getNombre());
                    existing.setProductoPrecio(dto.getPrecio());
                    existing.setProductoCodigo(dto.getCodigo());

                    if (dto.getStock() != null) {
                        existing.setProductoStock(dto.getStock());
                    }

                    existing.setCategoria(categoria);
                    existing.setProductoCantidadLote(cantidadLote);

                    repo.save(existing);

                } else {

                    Producto nuevoProducto = new Producto();

                    nuevoProducto.setProductoCodigo(dto.getCodigo());
                    nuevoProducto.setProductoNombre(dto.getNombre());
                    nuevoProducto.setProductoPrecio(dto.getPrecio());

                    nuevoProducto.setProductoStock(
                            dto.getStock() != null ? dto.getStock() : 0
                    );

                    nuevoProducto.setProductoCantidadLote(cantidadLote);
                    nuevoProducto.setCategoria(categoria);

                    AuditHelper.setCreationAudit(nuevoProducto, "EXCEL_IMPORT");

                    repo.save(nuevoProducto);
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
            dto.setCategoria(
                    p.getCategoria() != null ? p.getCategoria().getCategoriaNombre() : null
            );
            dto.setCantidadLote(p.getProductoCantidadLote());
            return dto;
        }).toList();

        return excelExporter.exportProductos(dtos);
    }
}