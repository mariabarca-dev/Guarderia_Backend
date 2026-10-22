package com.guarderiaCentral.guarderia_Backend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Propiedades de entorno de la aplicación, leídas desde application.properties
 * y desde el archivo externo entorno/config.properties (no versionado).
 * <p>
 * La propiedad de CORS es obligatoria: si falta, la aplicación no arranca.
 * El nombre de la cookie de sesión es opcional: si falta se usa el valor por defecto de Tomcat.
 * Las credenciales del SYSADMIN son opcionales: si faltan quedan vacías y el
 * {@code SysAdminInitializer} simplemente no crea el usuario inicial.
 * </p>
 *
 * @author Guardería Central
 * @version 3.0
 */
@Component
@Getter
public class AppEnvironmentConfig {

    @Value("${app.cors.allowed-origin}")
    private String corsAllowedOrigin;

    @Value("${server.servlet.session.cookie.name:JSESSIONID}")
    private String sessionCookieName;

    @Value("${app.sysadmin.username:}")
    private String sysadminUsername;

    @Value("${app.sysadmin.password:}")
    private String sysadminPassword;
}