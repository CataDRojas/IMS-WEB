package com.ims_web.inventory.controller;

import com.ims_web.inventory.entity.Configuracion;
import com.ims_web.inventory.service.ConfiguracionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final ConfiguracionService service;

    public ConfiguracionController(ConfiguracionService service) {
        this.service = service;
    }

    @GetMapping
    public Configuracion getConfiguracion() {
        return service.getConfiguracion();
    }

    @PostMapping
    public Configuracion createOrUpdate(@RequestBody Configuracion config) {
        return service.createOrUpdate(config);
    }
}