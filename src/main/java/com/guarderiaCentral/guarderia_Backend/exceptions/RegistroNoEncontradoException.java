package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando una entidad solicitada por ID u otro identificador único no existe o está inactiva.
 */
public class RegistroNoEncontradoException extends BusinessException {

    /**
     * Constructor por defecto con código HTTP 404 (NOT FOUND).
     */
    public RegistroNoEncontradoException() {
        super("Error: No se encontró el registro solicitado en el sistema.", HttpStatus.NOT_FOUND);
    }

    /**
     * Constructor con mensaje personalizado y código HTTP 404 (NOT FOUND).
     *
     * @param mensaje Mensaje descriptivo del error.
     */
    public RegistroNoEncontradoException(String mensaje) {
        super(mensaje, HttpStatus.NOT_FOUND);
    }
}
