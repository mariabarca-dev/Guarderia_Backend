package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada al intentar registrar un empleado con un código de legado/legajo duplicado.
 */
public class CodigoEmpleadoDuplicadoException extends BusinessException {

    /**
     * Constructor por defecto con código HTTP 409 (CONFLICT).
     */
    public CodigoEmpleadoDuplicadoException() {
        super("Error: El código de empleado ingresado ya existe en el sistema.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor con mensaje personalizado y código HTTP 409 (CONFLICT).
     *
     * @param mensaje Mensaje descriptivo del error.
     */
    public CodigoEmpleadoDuplicadoException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }
}
