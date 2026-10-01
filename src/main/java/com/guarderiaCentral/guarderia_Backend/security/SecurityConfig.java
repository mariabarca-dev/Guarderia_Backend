package com.guarderiaCentral.guarderia_Backend.security;

import com.guarderiaCentral.guarderia_Backend.config.AppEnvironmentConfig;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración de Spring Security: autenticación stateless con JWT, reglas de acceso,
 * seguridad por métodos (@PreAuthorize), CORS y beans de autenticación.
 *
 * @author Guardería Central
 * @version 2.1
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final AppEnvironmentConfig appEnvironmentConfig;

    /**
     * Crea la configuración de seguridad.
     *
     * @param jwtProvider proveedor de tokens JWT
     * @param appEnvironmentConfig configuración externa (orígenes CORS permitidos)
     */
    public SecurityConfig(JwtProvider jwtProvider, AppEnvironmentConfig appEnvironmentConfig) {
        this.jwtProvider = jwtProvider;
        this.appEnvironmentConfig = appEnvironmentConfig;
    }

    /**
     * Define la cadena de filtros: sin sesión, login público y el resto de las rutas autenticadas.
     * Cuando una solicitud llega sin token o con un token inválido o vencido, responde 401
     * (Unauthorized) con un cuerpo JSON; si el rol no alcanza para el recurso, responde 403.
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
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                            "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Token ausente, inválido o vencido.\"}");
                }))
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
}