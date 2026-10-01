package com.guarderiaCentral.guarderia_Backend.security;

import com.guarderiaCentral.guarderia_Backend.exceptions.CredencialesInvalidasException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la autenticación de usuarios en el sistema de la Guardería Central.
 * Expone el endpoint público de inicio de sesión (/api/auth/login).
 *
 * @author Guardería Central
 * @version 2.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    /**
     * Autentica un usuario mediante sus credenciales y emite un token JWT firmado que incluye su rol.
     * Coincide con la regla de seguridad abierta: .requestMatchers("/api/auth/**").permitAll()
     *
     * @param loginRequest objeto JSON que contiene el nombre de usuario y la clave
     * @return una respuesta HTTP con el token JWT de acceso generado
     * @throws CredencialesInvalidasException si el usuario o la clave son incorrectos
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Procesando solicitud de inicio de sesión para el usuario: {}", loginRequest.getUsername());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));
        } catch (BadCredentialsException e) {
            log.warn("Credenciales inválidas para el usuario: {}", loginRequest.getUsername());
            throw new CredencialesInvalidasException();
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String rol = authentication.getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");
        String jwt = jwtProvider.generateToken(authentication.getName(), rol);

        log.info("Autenticación exitosa. Token JWT generado para el usuario: {}", loginRequest.getUsername());

        return ResponseEntity.ok(new JwtResponse(jwt));
    }

    /**
     * DTO interno para recibir la solicitud de inicio de sesión.
     */
    @Data
    public static class LoginRequest {
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
    public static class JwtResponse {
        private final String accessToken;
        private final String tokenType = "Bearer";
    }
}