package com.guarderiaCentral.guarderia_Backend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Propiedades de entorno de la aplicación, leídas desde application.properties
 * y desde el archivo externo entorno/config.properties (no versionado).
 *
 * @author Guardería Central
 * @version 2.0
 */
@Component
@Getter
public class AppEnvironmentConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.cors.allowed-origin}")
    private String corsAllowedOrigin;

    @Value("${app.sysadmin.username}")
    private String sysadminUsername;

    @Value("${app.sysadmin.password}")
    private String sysadminPassword;
}