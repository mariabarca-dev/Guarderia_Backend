package com.guarderiaCentral.guarderia_Backend.security;

import com.guarderiaCentral.guarderia_Backend.exceptions.CredencialesInvalidasException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para la autenticación de usuarios en el sistema de la Guardería Central.
 * Expone el endpoint público de inicio de sesión ({@code /api/auth/login}). El cierre de sesión
 * ({@code /api/auth/logout}) lo resuelve la configuración de seguridad.
 * <p>
 * La autenticación usa sesión HTTP: al iniciar sesión el servidor guarda al usuario y su rol en la sesión y
 * devuelve la cookie de sesión en el encabezado {@code Set-Cookie}. El cliente debe reenviar esa cookie en cada
 * solicitud posterior.
 * </p>
 *
 * @author Guardería Central
 * @version 3.0
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    /**
     * Autentica un usuario mediante sus credenciales y abre su sesión HTTP.
     * La respuesta devuelve el nombre de usuario y el rol, para que el cliente sepa a qué vista dirigir al usuario,
     * y la cookie de sesión viaja en el encabezado {@code Set-Cookie}.
     * Coincide con la regla de seguridad abierta: .requestMatchers("/api/auth/**").permitAll()
     *
     * @param loginRequest objeto JSON que contiene el nombre de usuario y la clave
     * @param request      solicitud HTTP, necesaria para crear la sesión
     * @param response     respuesta HTTP, donde se registra la cookie de sesión
     * @return una respuesta HTTP con el nombre de usuario y el rol del usuario autenticado
     * @throws CredencialesInvalidasException si el usuario o la clave son incorrectos
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> iniciarSesion(@Valid @RequestBody LoginRequest loginRequest,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response) {
        log.info("Procesando solicitud de inicio de sesión para el usuario: {}", loginRequest.getUsername());

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));
        } catch (AuthenticationException e) {
            log.warn("Credenciales inválidas para el usuario: {}", loginRequest.getUsername());
            throw new CredencialesInvalidasException();
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // Si ya había una sesión previa, se cambia su identificador para evitar la fijación de sesión.
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        // Guarda el contexto en la sesión HTTP (crea la sesión y la cookie si todavía no existían).
        securityContextRepository.saveContext(context, request, response);

        String rol = authentication.getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");

        log.info("Autenticación exitosa. Sesión iniciada para el usuario: {} con rol {}",
                loginRequest.getUsername(), rol);

        return ResponseEntity.ok(new LoginResponse(authentication.getName(), rol));
    }

    /**
     * DTO interno para recibir la solicitud de inicio de sesión.
     * Los campos rechazan caracteres que permitirían inyectar código (por ejemplo {@code <script>}).
     */
    @Data
    public static class LoginRequest {
        @NotBlank(message = "El nombre de usuario no puede estar vacío")
        @Size(max = 50, message = "El nombre de usuario no puede superar los 50 caracteres")
        @Pattern(regexp = "^[a-zA-Z0-9_.-]*$",
                message = "El nombre de usuario solo puede contener letras, números, punto, guion y guion bajo")
        private String username;

        @NotBlank(message = "La clave no puede estar vacía")
        @Size(max = 100, message = "La clave no puede superar los 100 caracteres")
        @Pattern(regexp = "^[^<>]*$", message = "La clave no puede contener los caracteres < o >")
        private String password;
    }

    /**
     * DTO interno para retornar el resultado del inicio de sesión con el usuario y su rol.
     */
    @Data
    @RequiredArgsConstructor
    public static class LoginResponse {
        private final String nombreUsuario;
        private final String rol;
    }
}