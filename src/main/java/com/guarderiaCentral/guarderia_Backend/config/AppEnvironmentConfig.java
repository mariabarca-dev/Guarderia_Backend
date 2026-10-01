package com.guarderiaCentral.guarderia_Backend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//chequeo euge

/**
 * Propiedades de entorno de la aplicación, leídas desde application.properties
 * y desde el archivo externo entorno/config.properties (no versionado).
 * <p>
 * Las propiedades del JWT y de CORS son obligatorias: si faltan, la aplicación no arranca.
 * Las credenciales del SYSADMIN son opcionales: si faltan quedan vacías y el
 * {@code SysAdminInitializer} simplemente no crea el usuario inicial.
 * </p>
 *
 * @author Guardería Central
 * @version 2.1
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

    @Value("${app.sysadmin.username:}")
    private String sysadminUsername;

    @Value("${app.sysadmin.password:}")
    private String sysadminPassword;
}