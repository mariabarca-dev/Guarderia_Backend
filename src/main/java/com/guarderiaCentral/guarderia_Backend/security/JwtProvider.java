package com.guarderiaCentral.guarderia_Backend.security;

import com.guarderiaCentral.guarderia_Backend.config.AppEnvironmentConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Componente encargado de generar y validar los tokens JWT del sistema.
 * El token incluye el rol del usuario como claim para poder reconstruir sus permisos.
 *
 * @author Guardería Central
 * @version 2.0
 */
@Component
public class JwtProvider {

    private static final String CLAIM_ROL = "rol";

    private final AppEnvironmentConfig envConfig;

    /**
     * Crea el proveedor de tokens con la configuración externa de la aplicación.
     *
     * @param envConfig configuración con el secret y la expiración del JWT
     */
    public JwtProvider(AppEnvironmentConfig envConfig) {
        this.envConfig = envConfig;
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(envConfig.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Genera un token JWT firmado con expiración que incluye el rol del usuario como claim.
     *
     * @param username nombre de usuario (subject del token)
     * @param rol nombre del rol del usuario (por ejemplo ADMINISTRADOR)
     * @return el token JWT compacto
     */
    public String generateToken(String username, String rol) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + envConfig.getJwtExpirationMs());

        return Jwts.builder()
                .setSubject(username)
                .claim(CLAIM_ROL, rol)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Verifica que el token esté bien formado, tenga firma válida y no haya expirado.
     *
     * @param token token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Obtiene el nombre de usuario almacenado en el token.
     *
     * @param token token JWT válido
     * @return el nombre de usuario (subject)
     */
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Obtiene el rol almacenado como claim dentro del token.
     *
     * @param token token JWT válido
     * @return el nombre del rol del usuario
     */
    public String getRolFromToken(String token) {
        return getClaims(token).get(CLAIM_ROL, String.class);
    }
}