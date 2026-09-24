package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada al intentar vender o asociar la propiedad de un garage que ya tiene dueño.
 */
public class GarageYaVendidoException extends BusinessException {

    /**
     * Constructor por defecto con código HTTP 409 (CONFLICT).
     */
    public GarageYaVendidoException() {
        super("Error: El garaje seleccionado ya ha sido vendido a otro socio.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor con mensaje personalizado y código HTTP 409 (CONFLICT).
     *
     * @param mensaje Mensaje descriptivo del conflicto.
     */
    public GarageYaVendidoException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }
}
