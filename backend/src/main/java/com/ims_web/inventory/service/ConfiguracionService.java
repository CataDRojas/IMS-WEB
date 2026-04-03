package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Configuracion;
import com.ims_web.inventory.repository.ConfiguracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConfiguracionService {

    private final ConfiguracionRepository repo;

    public ConfiguracionService(ConfiguracionRepository repo) {
        this.repo = repo;
    }

    public Configuracion getConfiguracion() {
        return repo.findById((byte)1)
                .orElseThrow(() -> new RuntimeException("Configuracion not found"));
    }

    @Transactional
    public Configuracion createOrUpdate(Configuracion config) {
        config.setConfiguracionId((byte)1); // enforce singleton
        return repo.save(config);
    }

    public List<Configuracion> getAll() {
        return repo.findAll();
    }
}