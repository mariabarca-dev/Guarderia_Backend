package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada al intentar dar de alta un vehículo con una patente/matrícula que ya existe.
 */
public class MatriculaDuplicadaException extends BusinessException {

    /**
     * Constructor por defecto con código HTTP 409 (CONFLICT).
     */
    public MatriculaDuplicadaException() {
        super("Error: La matrícula ingresada ya se encuentra registrada en el sistema.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor con mensaje personalizado y código HTTP 409 (CONFLICT).
     *
     * @param mensaje Mensaje descriptivo del error.
     */
    public MatriculaDuplicadaException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }
}
