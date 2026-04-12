package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Configuracion;
import com.ims_web.inventory.service.ConfiguracionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final ConfiguracionService service;

    public ConfiguracionController(ConfiguracionService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONFIGURACION_MANAGE')")
    public Configuracion getConfiguracion() {
        return service.getConfiguracion();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CONFIGURACION_MANAGE')")
    public Configuracion createOrUpdate(@RequestBody Configuracion config) {
        validateConfig(config);
        return service.createOrUpdate(config);
    }

    private void validateConfig(Configuracion config) {
        if (config.getEmpresaNombre() == null || config.getEmpresaNombre().isBlank()) {
            throw new RuntimeException("EmpresaNombre is required");
        }
        if (config.getEmpresaDireccion() == null || config.getEmpresaDireccion().isBlank()) {
            throw new RuntimeException("EmpresaDireccion is required");
        }
        if (config.getIva() == null ||
                config.getIva().compareTo(BigDecimal.ZERO) < 0 ||
                config.getIva().compareTo(new BigDecimal("100")) > 0) {
            throw new RuntimeException("IVA must be between 0 and 100");
        }
    }
}