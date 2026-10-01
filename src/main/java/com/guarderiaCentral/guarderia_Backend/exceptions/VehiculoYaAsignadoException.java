package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando se intenta asignar un vehículo que ya posee
 * un garaje activo asignado en el sistema.
 * <p>
 * Extiende de {@link BusinessException} para integrarse con el manejador global
 * {@code @RestControllerAdvice}, retornando por defecto una respuesta HTTP 409 CONFLICT.
 * </p>
 *
 * @author Guardería Central
 * @version 1.0
 */
public class VehiculoYaAsignadoException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto con mensaje predeterminado.
     * Asocia la excepción a un código HTTP 409 CONFLICT.
     */
    public VehiculoYaAsignadoException() {
        super("El vehículo seleccionado ya se encuentra asignado a otro garaje.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor que permite especificar un mensaje de error personalizado.
     *
     * @param mensaje Detalle explicativo de la razón de la excepción.
     */
    public VehiculoYaAsignadoException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }

    /**
     * Constructor avanzado que permite definir un mensaje personalizado y un estado HTTP específico.
     *
     * @param mensaje Detalle explicativo de la razón de la excepción.
     * @param status Estado HTTP a retornar en la respuesta REST de la API.
     */
    public VehiculoYaAsignadoException(String mensaje, HttpStatus status) {
        super(mensaje, status);
    }
}
