package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada al fallar el proceso de autenticación debido a usuario o clave erróneos.
 */
public class CredencialesInvalidasException extends BusinessException {

    /**
     * Constructor con mensaje personalizado y código HTTP 401 (UNAUTHORIZED).
     *
     * @param mensaje Detalle del fallo de credenciales.
     */
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje, HttpStatus.UNAUTHORIZED);
    }
}
