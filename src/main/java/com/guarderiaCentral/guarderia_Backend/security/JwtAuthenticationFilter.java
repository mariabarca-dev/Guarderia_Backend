package com.guarderiaCentral.guarderia_Backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que valida el token JWT de cada solicitud y reconstruye la autenticación
 * del usuario, incluyendo su rol como GrantedAuthority para que funcione @PreAuthorize.
 * El token se parsea una sola vez por solicitud.
 *
 * @author Guardería Central
 * @version 2.1
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    /**
     * Crea el filtro con el proveedor de tokens JWT.
     *
     * @param jwtProvider proveedor utilizado para validar y leer el token
     */
    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    /**
     * Lee el encabezado Authorization, valida el token y, si es válido, registra en el
     * contexto de seguridad al usuario con la autoridad ROLE_ correspondiente a su rol.
     * Si el token es inválido o está vencido, la solicitud continúa sin autenticar y
     * la configuración de seguridad responde 401.
     *
     * @param request solicitud HTTP entrante
     * @param response respuesta HTTP
     * @param filterChain cadena de filtros a continuar
     * @throws ServletException si ocurre un error del servlet
     * @throws IOException si ocurre un error de entrada/salida
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtProvider.parseToken(token);
                String rol = claims.get(JwtProvider.CLAIM_ROL, String.class);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        claims.getSubject(), null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException e) {
                log.debug("Token rechazado: {} - Path: {}", e.getMessage(), request.getRequestURI());
            }
        }
        filterChain.doFilter(request, response);
    }
}