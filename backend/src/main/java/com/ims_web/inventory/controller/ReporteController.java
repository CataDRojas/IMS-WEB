package com.ims_web.inventory.controller;

import com.ims_web.inventory.service.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PreAuthorize("hasAnyAuthority('VENTA_READ', 'INVENTARIO_READ')")
    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) throws Exception {

        ByteArrayOutputStream out = reporteService.generarExcel(
                tipo, estado, usuario, desde, hasta);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Reporte_IMS.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(out.toByteArray());
    }
}