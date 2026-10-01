package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción de negocio lanzada cuando una zona ha alcanzado su capacidad máxima
 * de vehículos y no es posible realizar una nueva asignación o registro en la misma.
 * <p>
 * Extiende de {@link BusinessException} asignando siempre el estado HTTP
 * {@link HttpStatus#CONFLICT} (409 Conflict).
 * </p>
 *
 * @author Cátedra Guardería Central
 * @version 2.0
 */
public class ZonaSinCapacidadException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto que establece un mensaje estándar de capacidad agotada.
     * Siempre asocia la excepción al estado HTTP 409 (CONFLICT).
     */
    public ZonaSinCapacidadException() {
        super("La zona seleccionada ha alcanzado su capacidad máxima y no admite más vehículos.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor que permite especificar un mensaje de error personalizado.
     * Siempre asocia la excepción al estado HTTP 409 (CONFLICT).
     *
     * @param mensaje Detalle del motivo por el cual se lanza la excepción.
     */
    public ZonaSinCapacidadException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }
}