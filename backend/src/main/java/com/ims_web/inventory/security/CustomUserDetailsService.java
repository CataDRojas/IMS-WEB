package com.ims_web.inventory.security;

import com.ims_web.inventory.entity.Usuario;
import com.ims_web.inventory.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByEmailWithRoleAndPermissions(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 🔥 flatten permissions from ROLE in a defensive way
        var authorities = usuario.getRol()
                .getPermisos()
                .stream()
                .map(p -> new SimpleGrantedAuthority(
                        p.getPermisosNombre().trim()
                ))
                .collect(Collectors.toSet());

        return new User(
                usuario.getUsuarioEmail(),
                usuario.getUsuarioPassword(),
                usuario.getUsuarioActivo(),
                true,
                true,
                true,
                authorities
        );
    }
}