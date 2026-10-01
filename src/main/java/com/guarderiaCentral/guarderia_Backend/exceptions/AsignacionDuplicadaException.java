package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción personalizada para manejar intentos de realizar una asignación
 * que ya se encuentra registrada o duplicada en el sistema.
 * <p>
 * Extiende de {@link BusinessException} asignando por defecto el estado HTTP
 * {@link HttpStatus#CONFLICT} (409 Conflict).
 * </p>
 *
 * @author Cátedra Guardería Central
 * @version 2.0
 */
public class AsignacionDuplicadaException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto que asigna un mensaje estándar de asignación duplicada
     * y el estado HTTP 409 (CONFLICT).
     */
    public AsignacionDuplicadaException() {
        super("La asignación ingresada ya existe en el sistema.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor que permite definir un mensaje de error personalizado.
     * Siempre asocia la excepción con un código HTTP 409 CONFLICT.
     *
     * @param message Mensaje descriptivo del error.
     */
    public AsignacionDuplicadaException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    /**
     * Constructor que permite especificar un mensaje personalizado y la causa raíz del error,
     * manteniendo el estado HTTP 409 (CONFLICT).
     *
     * @param message Mensaje descriptivo del error.
     * @param cause   Causa raíz de la excepción para trazabilidad y depuración.
     */
    public AsignacionDuplicadaException(String message, Throwable cause) {
        super(message, HttpStatus.CONFLICT, cause);
    }
}