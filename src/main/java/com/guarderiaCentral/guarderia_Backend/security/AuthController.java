package com.guarderiaCentral.guarderia_Backend.security;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Controlador REST para la autenticación de usuarios en el sistema de la Guardería Central.
 * Expone el endpoint público de inicio de sesión (/api/auth/login).
 *
 * @author Desarrollador Backend
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    /**
     * Endpoint para autenticar un usuario en el sistema mediante sus credenciales.
     * Valida el nombre de usuario y contraseña contra el AuthenticationManager y emite un Token JWT firmado.
     * Coincide con la regla de seguridad abierta: .requestMatchers("/api/auth/**").permitAll()
     *
     * @param loginRequest objeto JSON que contiene el nombre de usuario y la clave
     * @return una respuesta HTTP con el token JWT de acceso generado
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Procesando solicitud de inicio de sesión para el usuario: {}", loginRequest.getUsername());

        // Autentica las credenciales mediante Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // Establece la autenticación en el contexto de seguridad actual
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Genera el token JWT incluyendo el rol extraído de la autenticación
        String jwt = jwtProvider.generateToken(authentication);

        log.info("Autenticación exitosa. Token JWT generado para el usuario: {}", loginRequest.getUsername());

        return ResponseEntity.ok(new JwtResponseDTO(jwt));
    }

    /**
     * DTO interno para recibir la solicitud de inicio de sesión.
     */
    @Data
    public static class LoginRequestDTO {
        @NotBlank(message = "El nombre de usuario no puede estar vacío")
        private String username;

        @NotBlank(message = "La clave no puede estar vacía")
        private String password;
    }

    /**
     * DTO interno para retornar la respuesta estructurada con el token JWT.
     */
    @Data
    @RequiredArgsConstructor
    public static class JwtResponseDTO {
        private final String accessToken;
        private final String tokenType = "Bearer";
    }
}