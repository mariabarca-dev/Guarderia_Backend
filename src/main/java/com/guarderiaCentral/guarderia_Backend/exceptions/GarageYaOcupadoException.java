package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando se intenta asignar un vehículo a un garaje que ya se encuentra ocupado.
 * <p>
 * Extiende de {@link BusinessException} para integrarse con el manejador global
 * {@code @RestControllerAdvice} y asociar un estado HTTP 400 (BAD REQUEST) o 409 (CONFLICT).
 * </p>
 *
 * @author Guardería Central
 * @version 1.0
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
     *
     * @param mensaje Detalle explicativo de la razón de la excepción.
     */
    public GarageYaOcupadoException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }

    /**
     * Constructor que permite definir un mensaje personalizado y un código de estado HTTP específico.
     *
     * @param mensaje Detalle explicativo de la razón de la excepción.
     * @param status Código HTTP que debe retornar la API en la respuesta REST.
     */
    public GarageYaOcupadoException(String mensaje, HttpStatus status) {
        super(mensaje, status);
    }
}