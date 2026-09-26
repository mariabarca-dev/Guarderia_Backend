package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción de negocio lanzada cuando una zona ha alcanzado su capacidad máxima
 * de vehículos y no es posible realizar una nueva asignación o registro en la misma.
 * <p>
 * Extiende de {@link BusinessException} con un estado HTTP 400 (BAD_REQUEST).
 * </p>
 *
 * @author GuarderiaCentral
 */
public class ZonaSinCapacidadException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto que establece un mensaje estándar de capacidad agotada.
     */
    public ZonaSinCapacidadException() {
        super("La zona seleccionada ha alcanzado su capacidad máxima y no admite más vehículos.", HttpStatus.BAD_REQUEST);
    }

    /**
     * Constructor que permite especificar un mensaje de error personalizado.
     *
     * @param mensaje Detalle del motivo por el cual se lanza la excepción.
     */
    public ZonaSinCapacidadException(String mensaje) {
        super(mensaje, HttpStatus.BAD_REQUEST);
    }
}