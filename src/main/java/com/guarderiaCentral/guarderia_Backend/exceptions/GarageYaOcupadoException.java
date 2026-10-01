package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando se intenta asignar un vehículo a un garaje que ya se encuentra ocupado.
 * <p>
 * Extiende de {@link BusinessException} para integrarse con el manejador global
 * {@code @RestControllerAdvice}. Siempre asocia el estado HTTP 409 (CONFLICT).
 * </p>
 *
 * @author Guardería Central
 * @version 2.0
 */
public class GarageYaOcupadoException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto con mensaje predeterminado.
     * Asocia la excepción con un código HTTP 409 CONFLICT.
     */
    public GarageYaOcupadoException() {
        super("El garaje seleccionado ya se encuentra ocupado por otro vehículo.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor que permite definir un mensaje de error personalizado.
     * Siempre asocia la excepción con un código HTTP 409 CONFLICT.
     *
     * @param mensaje Detalle explicativo de la razón de la excepción.
     */
    public GarageYaOcupadoException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }
}