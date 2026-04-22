package com.ims_web.inventory.service;

import com.ims_web.inventory.entity.Configuracion;
import com.ims_web.inventory.repository.ConfiguracionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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
        config.setConfiguracionId((byte)1);
        if (config.getIva() == null || config.getIva().doubleValue() < 0 || config.getIva().doubleValue() > 100) {
            throw new RuntimeException("IVA must be between 0 and 100");
        }
        return repo.save(config);
    }

    public List<Configuracion> getAll() {
        return Collections.singletonList(getConfiguracion());
    }
}