package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando se intenta realizar una operación de venta o asignación de propiedad
 * sobre un garaje que ya cuenta con un propietario registrado en el sistema.
 * <p>
 * Extiende de {@link BusinessException} para integrarse con la jerarquía global de excepciones
 * de la aplicación y ser capturada automáticamente por el {@code GlobalExceptionHandler},
 * retornando siempre una respuesta HTTP 409 CONFLICT.
 * </p>
 *
 * @author Guardería Central
 * @version 2.0
 */
public class GarageYaVendidoException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto con un mensaje predeterminado.
     * Asocia la excepción al estado HTTP 409 (CONFLICT).
     */
    public GarageYaVendidoException() {
        super("El garaje seleccionado ya ha sido vendido a otro socio.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor que permite definir un mensaje de error personalizado.
     * Siempre asocia la excepción al estado HTTP 409 (CONFLICT).
     *
     * @param mensaje Detalle explicativo de la razón de la excepción.
     */
    public GarageYaVendidoException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }
}