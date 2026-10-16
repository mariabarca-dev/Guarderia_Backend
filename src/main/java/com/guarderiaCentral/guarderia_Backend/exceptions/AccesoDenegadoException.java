package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción personalizada para los casos en que un usuario autenticado intenta acceder
 * a un recurso que no le pertenece (por ejemplo, un socio que consulta el vehículo de otro socio).
 * <p>
 * Extiende de {@link BusinessException} asignando siempre el estado HTTP
 * {@link HttpStatus#FORBIDDEN} (403 Forbidden).
 * </p>
 *
 * @author Cátedra Guardería Central
 * @version 1.0
 */
public class AccesoDenegadoException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto con un mensaje estándar y el estado HTTP 403 (FORBIDDEN).
     */
    public AccesoDenegadoException() {
        super("El recurso solicitado no pertenece al usuario autenticado.", HttpStatus.FORBIDDEN);
    }

    /**
     * Constructor que permite especificar un mensaje descriptivo personalizado
     * manteniendo el estado HTTP 403 (FORBIDDEN).
     *
     * @param message Mensaje descriptivo del error.
     */
    public AccesoDenegadoException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}