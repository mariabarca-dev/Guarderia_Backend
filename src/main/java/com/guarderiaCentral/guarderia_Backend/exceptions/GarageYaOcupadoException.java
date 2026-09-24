package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada al intentar asignar un vehículo a un garage que ya posee una asignación activa.
 */
public class GarageYaOcupadoException extends BusinessException {

    /**
     * Constructor por defecto con código HTTP 409 (CONFLICT).
     */
    public GarageYaOcupadoException() {
        super("Error: El garaje seleccionado ya se encuentra ocupado por otro vehículo.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor con mensaje personalizado y código HTTP 409 (CONFLICT).
     *
     * @param mensaje Mensaje descriptivo del conflicto.
     */
    public GarageYaOcupadoException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }
}
