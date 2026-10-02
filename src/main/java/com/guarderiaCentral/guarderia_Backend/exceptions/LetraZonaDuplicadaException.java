package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;
//prueba comit 01/10
/**
 * Excepción personalizada para manejar intentos de registro o actualización de zonas
 * con una letra de zona que ya existe en el sistema.
 * <p>
 * Extiende de {@link BusinessException} asignando por defecto el estado HTTP
 * {@link HttpStatus#CONFLICT} (409 Conflict).
 * </p>
 *
 * @author Cátedra Guardería Central
 * @version 2.0
 */
public class LetraZonaDuplicadaException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto que asigna un mensaje estándar de duplicidad de letra de zona
     * y el estado HTTP 409 (CONFLICT).
     */
    public LetraZonaDuplicadaException() {
        super("La letra de zona ingresada ya existe en el sistema.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor que permite definir un mensaje de error personalizado.
     * Siempre asocia la excepción con un código HTTP 409 CONFLICT.
     *
     * @param message Mensaje descriptivo del error.
     */
    public LetraZonaDuplicadaException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

    /**
     * Constructor que permite especificar un mensaje personalizado y la causa raíz del error,
     * manteniendo el estado HTTP 409 (CONFLICT).
     *
     * @param message Mensaje descriptivo del error.
     * @param cause   Causa raíz de la excepción para trazabilidad y depuración.
     */
    public LetraZonaDuplicadaException(String message, Throwable cause) {
        super(message, HttpStatus.CONFLICT, cause);
    }
}