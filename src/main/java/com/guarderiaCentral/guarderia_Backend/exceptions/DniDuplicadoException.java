package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada al intentar registrar un usuario con un DNI ya existente en el sistema.
 */
public class DniDuplicadoException extends BusinessException {

    /**
     * Constructor por defecto con código HTTP 409 (CONFLICT).
     */
    public DniDuplicadoException() {
        super("Error: El DNI ingresado ya se encuentra registrado en el sistema.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor con mensaje personalizado y código HTTP 409 (CONFLICT).
     *
     * @param mensaje Mensaje descriptivo del conflicto.
     */
    public DniDuplicadoException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }
}
