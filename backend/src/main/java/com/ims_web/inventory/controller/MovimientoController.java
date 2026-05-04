package com.ims_web.inventory.controller;

import com.ims_web.inventory.dto.MovimientoDetalleRequestDTO;
import com.ims_web.inventory.dto.MovimientoResponseDTO;
import com.ims_web.inventory.entity.Configuracion;
import com.ims_web.inventory.entity.Movimiento;
import com.ims_web.inventory.entity.Producto;
import com.ims_web.inventory.service.ConfiguracionService;
import com.ims_web.inventory.service.MovimientoService;
import com.ims_web.inventory.service.ProductoService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;
    private final ConfiguracionService configuracionService;
    private final ProductoService productoService;

    public MovimientoController(
            MovimientoService movimientoService,
            ConfiguracionService configuracionService,
            ProductoService productoService) {
        this.movimientoService = movimientoService;
        this.configuracionService = configuracionService;
        this.productoService = productoService;
    }

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping
    public List<MovimientoResponseDTO> getAll() {
        return movimientoService.getAll();
    }

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/{id}")
    public MovimientoResponseDTO getById(@PathVariable Long id) {
        return movimientoService.getById(id);
    }

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/pendientes")
    public List<MovimientoResponseDTO> getPendientesEntrada() {
        return movimientoService.getPendientesEntrada();
    }

    public static class MovimientoCreateRequest {
        public Movimiento movimiento;
        public List<MovimientoDetalleRequestDTO> detalles;
    }

    @PreAuthorize("hasAnyAuthority('INVENTARIO_MANAGE')")
    @PostMapping("/borrador")
    public MovimientoResponseDTO guardarBorrador(
            @RequestBody MovimientoCreateRequest request) {

        return movimientoService.guardarBorrador(
                request.movimiento,
                request.detalles
        );
    }

    @PreAuthorize("hasAnyAuthority('VENTA_MANAGE', 'INVENTARIO_MANAGE')")
    @PostMapping
    public MovimientoResponseDTO create(
            @RequestBody MovimientoCreateRequest request) {

        return movimientoService.create(
                request.movimiento,
                request.detalles
        );
    }

    @PreAuthorize("hasAnyAuthority('VENTA_MANAGE', 'INVENTARIO_MANAGE')")
    @PutMapping("/{id}")
    public MovimientoResponseDTO update(
            @PathVariable Long id,
            @RequestBody Movimiento movimiento) {

        movimiento.setMovimientoId(id);
        return movimientoService.update(movimiento);
    }

    @PreAuthorize("hasAnyAuthority('VENTA_MANAGE', 'INVENTARIO_MANAGE', 'VENTA_READ', 'INVENTARIO_READ')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        movimientoService.delete(id);
    }

    @PreAuthorize("hasAnyAuthority('VENTA_MANAGE', 'INVENTARIO_MANAGE')")
    @PostMapping("/{id}/confirmar")
    public MovimientoResponseDTO confirmar(@PathVariable Long id) {
        return movimientoService.confirmarMovimiento(id);
    }

    @PreAuthorize("hasAnyAuthority('VENTA_MANAGE', 'INVENTARIO_MANAGE')")
    @PostMapping("/{id}/anular")
    public MovimientoResponseDTO anular(@PathVariable Long id) {
        return movimientoService.anularMovimiento(id);
    }

    @PreAuthorize("hasAnyAuthority('VENTA_MANAGE', 'INVENTARIO_MANAGE')")
    @PostMapping("/{id}/reactivar")
    public MovimientoResponseDTO reactivar(@PathVariable Long id) {
        return movimientoService.reactivarMovimiento(id);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    public Page<MovimientoResponseDTO> search(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return movimientoService.search(tipo, estado, usuario, desde, hasta, page, size);
    }

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/configuracion")
    public Configuracion getConfiguracionForMovimientos() {
        return configuracionService.getConfiguracion();
    }

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/productos")
    public List<Producto> getAllProductos() {
        return productoService.getAllProductos();
    }

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/productos/{id}")
    public Producto getProductoById(@PathVariable Long id) {
        return productoService.getProductoById(id);
    }

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/productos/codigo/{codigo}")
    public Producto getProductoByCodigo(@PathVariable String codigo) {
        return productoService.getProductoByCodigo(codigo);
    }
}