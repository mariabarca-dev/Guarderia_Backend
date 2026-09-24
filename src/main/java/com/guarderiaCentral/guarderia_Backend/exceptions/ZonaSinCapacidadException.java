package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando una zona ha alcanzado su cupo límite de garages o vehículos asignados.
 */
public class ZonaSinCapacidadException extends BusinessException {

    /**
     * Constructor por defecto con código HTTP 409 (CONFLICT).
     */
    public ZonaSinCapacidadException() {
        super("Error: La zona seleccionada no tiene capacidad disponible para más vehículos.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor con mensaje personalizado y código HTTP 409 (CONFLICT).
     *
     * @param mensaje Mensaje descriptivo del error.
     */
    public ZonaSinCapacidadException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }
}
