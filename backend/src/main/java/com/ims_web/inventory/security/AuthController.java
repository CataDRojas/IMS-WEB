package com.ims_web.inventory.security;

import com.ims_web.inventory.entity.Usuario;
import com.ims_web.inventory.repository.UsuarioRepository;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    public AuthController(
            AuthenticationManager authManager,
            JwtUtil jwtUtil,
            UsuarioRepository usuarioRepository
    ) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String password = body.get("password");

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        String token = jwtUtil.generateToken(email);

        Usuario usuario = usuarioRepository.findByUsuarioEmail(email)
                .orElseThrow();

        List<String> permisos = usuario.getRol()
                .getPermisos()
                .stream()
                .map(p -> p.getPermisosNombre())
                .collect(Collectors.toList());

        return Map.of(
                "token", token,
                "rol", usuario.getRol().getRolNombre(),
                "nombre", usuario.getUsuarioNombre(),
                "permisos", permisos
        );
    }
}