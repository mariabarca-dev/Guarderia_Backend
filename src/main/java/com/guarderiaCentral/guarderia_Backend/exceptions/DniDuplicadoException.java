package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción personalizada para manejar intentos de registro o modificación de socios
 * con un DNI que ya se encuentra registrado en el sistema.
 * <p>
 * Extiende de {@link BusinessException} asignando por defecto el estado HTTP
 * {@link HttpStatus#CONFLICT} (409 Conflict).
 * </p>
 *
 * @author Cátedra Guardería Central
 * @version 2.0
 */
public class DniDuplicadoException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto que asigna un mensaje estándar de duplicidad de DNI de socio
     * y el estado HTTP 409 (CONFLICT).
     */
    public DniDuplicadoException() {
        super("El DNI del socio ingresado ya se encuentra registrado en el sistema.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor que permite especificar un mensaje descriptivo personalizado
     * manteniendo el estado HTTP 409 (CONFLICT).
     *
     * @param message Mensaje descriptivo del error de duplicidad.
     */
    public DniDuplicadoException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    /**
     * Constructor que permite especificar un mensaje personalizado y la causa raíz del error,
     * manteniendo el estado HTTP 409 (CONFLICT).
     *
     * @param message Mensaje descriptivo del error.
     * @param cause   Causa raíz de la excepción para trazabilidad y depuración.
     */
    public DniDuplicadoException(String message, Throwable cause) {
        super(message, HttpStatus.CONFLICT, cause);
    }
}