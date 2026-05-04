package com.ims_web.inventory.util;

import com.ims_web.inventory.entity.Permisos;
import com.ims_web.inventory.entity.Rol;
import com.ims_web.inventory.entity.Usuario;
import com.ims_web.inventory.service.PermisosService;
import com.ims_web.inventory.service.RolService;
import com.ims_web.inventory.service.UsuarioService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.*;

@Component
public class DataInitializer {

    private final PermisosService permisosService;
    private final RolService rolService;
    private final UsuarioService usuarioService;

    public DataInitializer(
            PermisosService permisosService,
            RolService rolService,
            UsuarioService usuarioService
    ) {
        this.permisosService = permisosService;
        this.rolService = rolService;
        this.usuarioService = usuarioService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {

        List<String> permisosNombres = List.of(
                "CONFIGURACION_MANAGE",
                "CATEGORIA_READ", "CATEGORIA_MANAGE",
                "DESCUENTO_READ", "DESCUENTO_MANAGE",
                "ROLES_MANAGE",
                "MOVIMIENTO_LUGAR_MANAGE",
                "USUARIOS_MANAGE",
                "PRODUCTO_READ", "PRODUCTO_MANAGE",
                "INVENTARIO_READ" , "INVENTARIO_MANAGE",
                "VENTA_READ" , "VENTA_MANAGE"
        );

        Set<Permisos> allPermisos = new HashSet<>();

        for (String name : permisosNombres) {
            allPermisos.add(getOrCreatePermiso(name));
        }

        Rol admin = getOrCreateRol("ADMIN");
        admin.setPermisos(allPermisos);
        rolService.save(admin);

        Rol vendedor = getOrCreateRol("VENDEDOR");
        vendedor.setPermisos(Set.of(
                getPermiso("VENTA_READ"),
                getPermiso("VENTA_MANAGE"),
                getPermiso("PRODUCTO_READ"),
                getPermiso("CATEGORIA_READ"),
                getPermiso("DESCUENTO_READ")
        ));
        rolService.save(vendedor);

        Rol bodeguero = getOrCreateRol("BODEGUERO");
        bodeguero.setPermisos(Set.of(
                getPermiso("INVENTARIO_READ"),
                getPermiso("INVENTARIO_MANAGE"),
                getPermiso("PRODUCTO_READ"),
                getPermiso("CATEGORIA_READ"),
                getPermiso("DESCUENTO_READ")
        ));
        rolService.save(bodeguero);

        createUsuario("admin@ims.cl", "1234", "Administrador Sistema", admin, null, null);

        createUsuario("carlitos.lechuga@ims.cl", "1234", "Carlitos Lechuga", vendedor, "15482901", "6");

        createUsuario("cosme.fulanito@ims.cl", "1234", "Cosme Fulanito", bodeguero, "18903112", "2");
    }

    private String calculateDV(String rut) {
        int sum = 0;
        int multiplier = 2;

        for (int i = rut.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(rut.charAt(i)) * multiplier;
            multiplier = (multiplier == 7) ? 2 : multiplier + 1;
        }

        int mod = 11 - (sum % 11);

        if (mod == 11) return "0";
        if (mod == 10) return "K";
        return String.valueOf(mod);
    }

    private Permisos getOrCreatePermiso(String name) {
        return permisosService.findByPermisosNombreIgnoreCase(name)
                .orElseGet(() -> {
                    Permisos p = new Permisos();
                    p.setPermisosNombre(name);
                    return permisosService.create(p);
                });
    }

    private Permisos getPermiso(String name) {
        return permisosService.findByPermisosNombreIgnoreCase(name)
                .orElseThrow(() -> new RuntimeException("Missing permiso: " + name));
    }

    private Rol getOrCreateRol(String name) {
        return rolService.findByRolNombreIgnoreCase(name)
                .orElseGet(() -> {
                    Rol r = new Rol();
                    r.setRolNombre(name);
                    r.setPermisos(new HashSet<>());
                    return rolService.save(r);
                });
    }

    private void createUsuario(
            String email,
            String password,
            String nombre,
            Rol rol,
            String run,
            String dv
    ) {
        if (usuarioService.findByUsuarioEmailIgnoreCase(email).isPresent()) {
            return;
        }

        Usuario u = new Usuario();
        u.setUsuarioEmail(email);
        u.setUsuarioPassword(password);
        u.setUsuarioNombre(nombre);
        u.setUsuarioActivo(true);
        u.setRol(rol);
        u.setUsuarioRun(run);
        u.setUsuarioDV(dv);

        usuarioService.create(u);
    }
}