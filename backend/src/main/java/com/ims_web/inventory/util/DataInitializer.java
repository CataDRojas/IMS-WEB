package com.ims_web.inventory.util;

import com.ims_web.inventory.entity.Permisos;
import com.ims_web.inventory.entity.Rol;
import com.ims_web.inventory.entity.Usuario;
import com.ims_web.inventory.service.PermisosService;
import com.ims_web.inventory.service.RolService;
import com.ims_web.inventory.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            PermisosService permisosService,
            RolService rolService,
            UsuarioService usuarioService
    ) {
        return args -> {

            // ========================
            // PERMISOS (FULL SET)
            // ========================

            List<String> permisosNombres = List.of(
                    "CONFIGURACION_MANAGE",
                    "CATEGORIA_READ", "CATEGORIA_MANAGE",
                    "DESCUENTO_READ", "DESCUENTO_MANAGE",
                    "PERMISOS_MANAGE",
                    "ROLES_MANAGE",
                    "MOVIMIENTO_LUGAR_MANAGE",
                    "USUARIOS_MANAGE",
                    "PRODUCTO_READ", "PRODUCTO_MANAGE",
                    "MOVIMIENTO_READ", "MOVIMIENTO_MANAGE"
            );

            Set<Permisos> allPermisos = new HashSet<>();
            for (String name : permisosNombres) {
                allPermisos.add(getOrCreatePermiso(permisosService, name));
            }

            // ========================
            // ROLES
            // ========================

            Rol admin = getOrCreateRol(rolService, "ADMIN");
            admin.setPermisos(allPermisos);
            admin = rolService.save(admin);

            Rol vendedor = getOrCreateRol(rolService, "VENDEDOR");
            vendedor.setPermisos(Set.of(
                    getPermiso(permisosService, "MOVIMIENTO_READ"),
                    getPermiso(permisosService, "MOVIMIENTO_MANAGE"),
                    getPermiso(permisosService, "PRODUCTO_READ"),
                    getPermiso(permisosService, "CATEGORIA_READ"),
                    getPermiso(permisosService, "DESCUENTO_READ")
            ));
            vendedor = rolService.save(vendedor);

            Rol bodeguero = getOrCreateRol(rolService, "BODEGUERO");
            bodeguero.setPermisos(Set.of(
                    getPermiso(permisosService, "MOVIMIENTO_READ"),
                    getPermiso(permisosService, "MOVIMIENTO_MANAGE"),
                    getPermiso(permisosService, "PRODUCTO_READ"),
                    getPermiso(permisosService, "CATEGORIA_READ"),
                    getPermiso(permisosService, "DESCUENTO_READ")
            ));
            bodeguero = rolService.save(bodeguero);

            // ========================
            // USUARIOS
            // ========================

            createUsuario(usuarioService, "vendedor@test.com", "1234", "Vendedor", vendedor);
            createUsuario(usuarioService, "bodeguero@test.com", "1234", "Bodeguero", bodeguero);
            createUsuario(usuarioService, "admin@test.com", "1234", "Admin", admin);
        };
    }

    // ========================
    // HELPERS
    // ========================

    private Permisos getOrCreatePermiso(PermisosService service, String name) {
        return service.getAll().stream()
                .filter(p -> p.getPermisosNombre().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Permisos p = new Permisos();
                    p.setPermisosNombre(name);
                    return service.create(p);
                });
    }

    private Permisos getPermiso(PermisosService service, String name) {
        return service.getAll().stream()
                .filter(p -> p.getPermisosNombre().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Missing permiso: " + name));
    }

    private Rol getOrCreateRol(RolService service, String name) {
        return service.getAll().stream()
                .filter(r -> r.getRolNombre().equalsIgnoreCase(name))
                .findFirst()
                .orElseGet(() -> {
                    Rol r = new Rol();
                    r.setRolNombre(name);
                    r.setPermisos(new HashSet<>());
                    return service.save(r);
                });
    }

    private void createUsuario(
            UsuarioService service,
            String email,
            String password,
            String nombre,
            Rol rol
    ) {
        boolean exists = service.getAll().stream()
                .anyMatch(u -> u.getUsuarioEmail().equalsIgnoreCase(email));

        if (!exists) {
            Usuario u = new Usuario();

            u.setUsuarioEmail(email);
            u.setUsuarioPassword(password);
            u.setUsuarioNombre(nombre);
            u.setUsuarioActivo(true);
            u.setRol(rol);

            service.create(u);
        }
    }
}