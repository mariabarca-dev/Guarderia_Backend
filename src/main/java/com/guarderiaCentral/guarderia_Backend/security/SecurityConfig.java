package com.guarderiaCentral.guarderia_Backend.security;

import com.guarderiaCentral.guarderia_Backend.config.AppEnvironmentConfig;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

/**
 * Configuración de Spring Security: autenticación por sesión HTTP (cookie), reglas de acceso por rol
 * centralizadas en un único bean, cierre de sesión, seguridad por métodos (@PreAuthorize), CORS y beans de
 * autenticación.
 * <p>
 * El rol del usuario se lee de la base de datos al iniciar sesión y queda guardado en la sesión del servidor.
 * Cada solicitud posterior se identifica por la cookie de sesión.
 * </p>
 *
 * @author Guardería Central
 * @version 3.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String ADMINISTRADOR = "ADMINISTRADOR";
    private static final String SYSADMIN = "SYSADMIN";
    private static final String SOCIO = "SOCIO";
    private static final String EMPLEADO = "EMPLEADO";

    private final AppEnvironmentConfig appEnvironmentConfig;

    /**
     * Crea la configuración de seguridad.
     *
     * @param appEnvironmentConfig configuración externa (origen CORS permitido y nombre de la cookie de sesión)
     */
    public SecurityConfig(AppEnvironmentConfig appEnvironmentConfig) {
        this.appEnvironmentConfig = appEnvironmentConfig;
    }

    /**
     * Define la cadena de filtros: sesión HTTP, login y logout públicos, acceso por rol para cada grupo de
     * rutas y respuestas JSON para los errores de seguridad.
     * <p>
     * Las reglas se evalúan en orden y gana la primera que coincide, por eso las rutas más específicas van
     * antes que las generales. Cuando la solicitud llega sin sesión válida responde 401 (Unauthorized); si el
     * rol no alcanza para el recurso, responde 403 (Forbidden).
     * </p>
     *
     * @param http constructor de la configuración HTTP de seguridad
     * @return la cadena de filtros de seguridad
     * @throws Exception si falla la construcción de la cadena
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .securityContext(context -> context.securityContextRepository(securityContextRepository()))
                .authorizeHttpRequests(auth -> auth
                        // --- Público ---
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()

                        // --- Cuenta propia: cada rol ve solo lo suyo ---
                        .requestMatchers("/api/usuarios/me").authenticated()
                        .requestMatchers("/api/usuarios/me/socio/**").hasRole(SOCIO)
                        .requestMatchers("/api/usuarios/me/empleado/**").hasRole(EMPLEADO)

                        // --- Consultas y asignaciones operativas: solo ADMINISTRADOR ---
                        .requestMatchers(
                                "/api/usuarios/socios/*/vehiculos",
                                "/api/usuarios/socios/*/garages",
                                "/api/usuarios/empleados/*/zonas",
                                "/api/usuarios/empleados/*/vehiculos-a-cargo",
                                "/api/usuarios/empleados/*/vehiculos/*").hasRole(ADMINISTRADOR)

                        // --- Gestión de cuentas: ADMINISTRADOR y SYSADMIN ---
                        .requestMatchers(
                                "/api/usuarios/socios/**",
                                "/api/usuarios/empleados/**",
                                "/api/usuarios/administradores/**").hasAnyRole(ADMINISTRADOR, SYSADMIN)

                        // --- Consultas generales de cuentas: solo SYSADMIN ---
                        .requestMatchers(
                                "/api/usuarios",
                                "/api/usuarios/inactivos",
                                "/api/usuarios/*").hasRole(SYSADMIN)

                        // --- Recursos de negocio: solo ADMINISTRADOR ---
                        .requestMatchers(
                                "/api/zonas/**",
                                "/api/garages/**",
                                "/api/vehiculos/**",
                                "/api/propiedades-garage/**",
                                "/api/asignaciones-vehiculo-garage/**",
                                "/api/asignaciones-empleado-zona/**").hasRole(ADMINISTRADOR)

                        // --- Cualquier otra ruta requiere estar autenticado ---
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies(appEnvironmentConfig.getSessionCookieName())
                        .logoutSuccessHandler((request, response, authentication) ->
                                escribirJson(response, HttpServletResponse.SC_OK, "OK",
                                        "Sesión cerrada correctamente."))
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                escribirJson(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                                        "Sesión ausente o vencida. Iniciá sesión nuevamente."))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                escribirJson(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                                        "No tenés permisos para acceder a este recurso."))
                );

        return http.build();
    }

    /**
     * Repositorio que guarda y recupera el contexto de seguridad en la sesión HTTP.
     * Lo usa la cadena de filtros para leer la sesión en cada solicitud y el {@code AuthController}
     * para guardar al usuario autenticado cuando inicia sesión.
     *
     * @return el repositorio del contexto de seguridad basado en sesión HTTP
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    /**
     * Configura CORS con el origen permitido leído de la configuración externa.
     *
     * @return la fuente de configuración CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(appEnvironmentConfig.getCorsAllowedOrigin()));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Codificador de contraseñas BCrypt usado para guardar y validar las claves.
     *
     * @return el codificador de contraseñas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Expone el AuthenticationManager que utiliza el login.
     *
     * @param config configuración de autenticación de Spring Security
     * @return el AuthenticationManager
     * @throws Exception si no se puede obtener el AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Escribe en la respuesta un cuerpo JSON con el estado, el tipo de error y un mensaje.
     *
     * @param response respuesta HTTP a completar
     * @param status   código de estado HTTP
     * @param error    descripción corta del estado
     * @param mensaje  mensaje para el cliente
     * @throws IOException si falla la escritura de la respuesta
     */
    private void escribirJson(HttpServletResponse response, int status, String error, String mensaje)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + error
                + "\",\"message\":\"" + mensaje + "\"}");
    }
}