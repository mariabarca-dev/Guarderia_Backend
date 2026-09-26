package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción personalizada para manejar intentos de registro o actualización de empleados
 * con un código de empleado que ya existe en el sistema.
 * <p>
 * Extiende de {@link BusinessException} asignando por defecto el estado HTTP
 * {@link HttpStatus#CONFLICT} (409 Conflict).
 * </p>
 *
 * @author Cátedra Guardería Central
 * @version 2.0
 */
public class CodigoEmpleadoDuplicadoException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto que asigna un mensaje estándar de duplicidad de código de empleado
     * y el estado HTTP 409 (CONFLICT).
     */
    public CodigoEmpleadoDuplicadoException() {
        super("Error: El código de empleado ingresado ya existe en el sistema.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor que permite especificar un mensaje descriptivo personalizado
     * manteniendo el estado HTTP 409 (CONFLICT).
     *
     * @param message Mensaje descriptivo del error de duplicidad.
     */
    public CodigoEmpleadoDuplicadoException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    /**
     * Constructor que permite especificar un mensaje personalizado y la causa raíz del error,
     * manteniendo el estado HTTP 409 (CONFLICT).
     *
     * @param message Mensaje descriptivo del error.
     * @param cause   Causa raíz de la excepción para trazabilidad y depuración.
     */
    public CodigoEmpleadoDuplicadoException(String message, Throwable cause) {
        super(message, HttpStatus.CONFLICT, cause);
    }
}
