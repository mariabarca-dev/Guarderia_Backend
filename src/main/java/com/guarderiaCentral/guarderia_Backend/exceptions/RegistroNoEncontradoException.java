package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando no se encuentra una entidad o recurso específico en la base de datos
 * (ej. socio, empleado, vehículo, garage, etc.) al intentar consultar, actualizar o eliminar por su ID.
 * <p>
 * Extiende de {@link BusinessException} para integrarse con la jerarquía global de excepciones
 * de la aplicación y ser capturada automáticamente por el {@code GlobalExceptionHandler},
 * retornando una respuesta HTTP 404 NOT FOUND por defecto.
 * </p>
 *
 * @author Guardería Central
 * @version 1.0
 */
public class RegistroNoEncontradoException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto con mensaje predeterminado.
     * Asocia la excepción al estado HTTP 404 (NOT_FOUND).
     */
    public RegistroNoEncontradoException() {
        super("No se encontró el registro solicitado en el sistema.", HttpStatus.NOT_FOUND);
    }

    /**
     * Constructor que permite especificar un mensaje de error personalizado.
     *
     * @param mensaje Detalle explicativo de la entidad o registro que no se encontró.
     */
    public RegistroNoEncontradoException(String mensaje) {
        super(mensaje, HttpStatus.NOT_FOUND);
    }

    /**
     * Constructor avanzado que permite definir un mensaje de error y un estado HTTP específico.
     *
     * @param mensaje Detalle explicativo de la razón de la excepción.
     * @param status Estado HTTP a retornar en la respuesta REST de la API.
     */
    public RegistroNoEncontradoException(String mensaje, HttpStatus status) {
        super(mensaje, status);
    }
}