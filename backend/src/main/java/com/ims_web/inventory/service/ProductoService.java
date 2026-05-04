package com.ims_web.inventory.service;

import com.ims_web.inventory.dto.ProductoDetalleDTO;
import com.ims_web.inventory.dto.ProductoExcelDTO;
import com.ims_web.inventory.dto.ProductoStockLugarDTO;
import com.ims_web.inventory.dto.ProductoListDTO;
import com.ims_web.inventory.entity.*;
import com.ims_web.inventory.repository.*;
import com.ims_web.inventory.util.AuditHelper;
import com.ims_web.inventory.util.excel.ProductoExcelExporter;
import com.ims_web.inventory.util.excel.ProductoExcelImporter;
import jakarta.persistence.EntityNotFoundException;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayOutputStream;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository repo;
    private final ProductoExcelImporter excelImporter;
    private final ProductoExcelExporter excelExporter;
    private final CategoriaRepository categoriaRepo;
    private final MovimientoLugarRepository movimientoLugarRepo;
    private final MovimientoLugarProductoRepository mlpRepo;

    public ProductoService(
            ProductoRepository repo,
            ProductoExcelImporter excelImporter,
            ProductoExcelExporter excelExporter,
            CategoriaRepository categoriaRepo,
            MovimientoLugarRepository movimientoLugarRepo,
            MovimientoLugarProductoRepository mlpRepo) {
        this.repo = repo;
        this.excelImporter = excelImporter;
        this.excelExporter = excelExporter;
        this.categoriaRepo = categoriaRepo;
        this.movimientoLugarRepo = movimientoLugarRepo;
        this.mlpRepo = mlpRepo;
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
    // CREACION
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

        Producto saved = repo.save(producto);

        ensureProductoInAllLugares(saved);

        return saved;
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
    public void importFromExcel(MultipartFile file, String currentUser) {

        try (InputStream inputStream = file.getInputStream()) {

            List<ProductoExcelDTO> productos = excelImporter.importProductos(inputStream);

            for (ProductoExcelDTO dto : productos) {

                if (dto.getCodigo() == null || dto.getCodigo().trim().isEmpty())
                    continue;
                if (dto.getNombre() == null || dto.getNombre().trim().isEmpty())
                    continue;
                if (dto.getPrecio() == null)
                    continue;

                Integer cantidadLote = dto.getCantidadLote() != null ? dto.getCantidadLote() : 1;

                Categoria categoria = null;

                if (dto.getCategoria() != null && !dto.getCategoria().trim().isEmpty()) {
                    String catNombre = dto.getCategoria().trim();

                    categoria = categoriaRepo.findByCategoriaNombreIgnoreCase(catNombre)
                            .orElseGet(() -> {
                                Categoria nuevaCat = new Categoria();
                                nuevaCat.setCategoriaNombre(catNombre);

                                AuditHelper.setCreationAudit(nuevaCat, currentUser);

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
                    existing.setCategoria(categoria);
                    existing.setProductoCantidadLote(cantidadLote);
                    existing.setProductoCriticoNumero(dto.getCriticoNumero() != null ? dto.getCriticoNumero() : 0);

                    repo.save(existing);

                } else {

                    Producto nuevoProducto = new Producto();

                    nuevoProducto.setProductoCodigo(dto.getCodigo());
                    nuevoProducto.setProductoNombre(dto.getNombre());
                    nuevoProducto.setProductoPrecio(dto.getPrecio());
                    nuevoProducto.setProductoCantidadLote(cantidadLote);
                    nuevoProducto.setCategoria(categoria);
                    nuevoProducto.setProductoCriticoNumero(dto.getCriticoNumero() != null ? dto.getCriticoNumero() : 0);

                    AuditHelper.setCreationAudit(nuevoProducto, currentUser);

                    Producto saved = repo.save(nuevoProducto);

                    ensureProductoInAllLugares(saved);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to import Excel file", e);
        }
    }
    // =========================
    // GRID CONSISTENCY CORE
    // =========================

    private void ensureProductoInAllLugares(Producto producto) {

        List<Long> existingLugarIds = mlpRepo
                .findByProducto_ProductoId(producto.getProductoId())
                .stream()
                .map(mlp -> mlp.getMovimientoLugar().getMovimientoLugarId())
                .toList();

        List<MovimientoLugar> lugares = movimientoLugarRepo.findAll();

        List<MovimientoLugarProducto> toCreate = lugares.stream()
                .filter(l -> !existingLugarIds.contains(l.getMovimientoLugarId()))
                .map(lugar -> {
                    MovimientoLugarProducto mlp = new MovimientoLugarProducto();
                    mlp.setMovimientoLugar(lugar);
                    mlp.setProducto(producto);
                    mlp.setMovimientoLugarProductoStock(0);
                    return mlp;
                })
                .toList();

        if (!toCreate.isEmpty()) {
            mlpRepo.saveAll(toCreate);
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
                    p.getCategoria() != null ? p.getCategoria().getCategoriaNombre() : null);
            dto.setCantidadLote(p.getProductoCantidadLote());
            dto.setCriticoNumero(p.getProductoCriticoNumero());
            return dto;
        }).toList();

        return excelExporter.exportProductos(dtos);
    }

    // =========================
    // DETALLE
    // =========================

    @Transactional(readOnly = true)
    public ProductoDetalleDTO getProductoDetalle(Long id) {

        Producto producto = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto not found"));

        List<MovimientoLugarProducto> relaciones = mlpRepo.findByProducto_ProductoId(id);

        List<ProductoStockLugarDTO> stockPorLugar = relaciones.stream().map(mlp -> {
            ProductoStockLugarDTO dto = new ProductoStockLugarDTO();

            dto.setMovimientoLugarId(mlp.getMovimientoLugar().getMovimientoLugarId());
            dto.setMovimientoLugarDescripcion(mlp.getMovimientoLugar().getMovimientoLugarDescripcion());
            dto.setStock(mlp.getMovimientoLugarProductoStock());
            dto.setPrioridad(mlp.getMovimientoLugar().getMovimientoLugarPrioridad());

            return dto;
        }).toList();

        ProductoDetalleDTO dto = new ProductoDetalleDTO();

        dto.setProductoId(producto.getProductoId());
        dto.setProductoNombre(producto.getProductoNombre());
        dto.setProductoCodigo(producto.getProductoCodigo());
        dto.setProductoPrecio(producto.getProductoPrecio());
        dto.setProductoStock(producto.getProductoStock());
        dto.setStockPorLugar(stockPorLugar);

        return dto;
    }

    private String resolveDescuentoNombre(Producto p) {

        if (p.getDescuento() != null && Boolean.TRUE.equals(p.getDescuento().getDescuentoActivo())) {
            return p.getDescuento().getDescuentoNombre();
        }

        if (p.getCategoria() != null
                && p.getCategoria().getDescuento() != null
                && Boolean.TRUE.equals(p.getCategoria().getDescuento().getDescuentoActivo())) {
            return p.getCategoria().getDescuento().getDescuentoNombre();
        }

        return null;
    }

    private ProductoListDTO toListDTO(Producto p) {

        ProductoListDTO dto = new ProductoListDTO();

        dto.setProductoId(p.getProductoId());
        dto.setProductoNombre(p.getProductoNombre());
        dto.setProductoCodigo(p.getProductoCodigo());

        dto.setProductoPrecio(p.getProductoPrecio());
        dto.setProductoStock(p.getProductoStock());
        dto.setProductoActivo(p.getProductoActivo());

        dto.setCategoriaNombre(
                p.getCategoria() != null ? p.getCategoria().getCategoriaNombre() : null);

        dto.setCategoriaId(
                p.getCategoria() != null ? p.getCategoria().getCategoriaId() : null);

        dto.setDescuentoNombre(resolveDescuentoNombre(p));

        dto.setDescuentoId(
                p.getDescuento() != null ? p.getDescuento().getDescuentoId() : null);

        dto.setProductoStockCritico(p.getProductoStockCritico());
        dto.setProductoCriticoNumero(p.getProductoCriticoNumero());
        dto.setProductoCantidadLote(p.getProductoCantidadLote());

        return dto;
    }

    public List<ProductoListDTO> getAllProductosList() {
        return repo.findAll()
                .stream()
                .map(this::toListDTO)
                .toList();
    }

    public byte[] exportToExcelFiltrado(String nombre, Long categoriaId,
            String activo, Boolean critico) throws Exception {
        List<Producto> todos = repo.findAll();

        // Aplicar filtros
        List<Producto> filtrados = todos.stream()
                .filter(p -> nombre == null || nombre.isBlank() ||
                        p.getProductoNombre().toLowerCase().contains(nombre.toLowerCase()))
                .filter(p -> categoriaId == null ||
                        (p.getCategoria() != null &&
                                categoriaId.equals(p.getCategoria().getCategoriaId())))
                .filter(p -> activo == null || activo.isBlank() ||
                        p.getProductoActivo().equals(Boolean.parseBoolean(activo)))
                .filter(p -> critico == null || !critico ||
                        Boolean.TRUE.equals(p.getProductoStockCritico()))
                .toList();
        System.out.println("Filtrados: " + filtrados.size());

        List<ProductoExcelDTO> dtos = filtrados.stream().map(p -> {
            ProductoExcelDTO dto = new ProductoExcelDTO();
            dto.setCodigo(p.getProductoCodigo());
            dto.setNombre(p.getProductoNombre());
            dto.setPrecio(p.getProductoPrecio());
            dto.setStock(p.getProductoStock());
            dto.setCriticoNumero(p.getProductoCriticoNumero());
            dto.setCantidadLote(p.getProductoCantidadLote());
            dto.setCategoria(p.getCategoria() != null
                    ? p.getCategoria().getCategoriaNombre()
                    : null);
            dto.setActivo(p.getProductoActivo());
            return dto;
        }).toList();

        Workbook workbook = excelExporter.exportProductos(dtos);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }
}