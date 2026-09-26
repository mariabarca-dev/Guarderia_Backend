package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción base para todos los errores de lógica de negocio en el sistema de la Guardería Central.
 * <p>
 * Reemplaza la antigua excepción {@code ErrorNegocio} del sistema anterior, adaptándola
 * para extender de {@link RuntimeException} e integrar el código de estado HTTP {@link HttpStatus}
 * correspondiente que será procesado por el {@code GlobalExceptionHandler}.
 * </p>
 *
 * @author Cátedra Guardería Central
 * @version 2.0
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Estado HTTP asociado al error de negocio.
     */
    private final HttpStatus status;

    /**
     * Constructor que recibe el mensaje de error. Por defecto asigna un estado HTTP 400 (BAD_REQUEST).
     *
     * @param message Mensaje descriptivo de la regla de negocio violada.
     */
    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    /**
     * Constructor que recibe el mensaje de error y un estado HTTP personalizado.
     *
     * @param message Mensaje descriptivo de la regla de negocio violada.
     * @param status  Código de estado HTTP a retornar en la respuesta REST (ej. BAD_REQUEST, NOT_FOUND, CONFLICT).
     */
    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Constructor que recibe el mensaje, un estado HTTP personalizado y la causa original del error.
     *
     * @param message Mensaje descriptivo de la regla de negocio violada.
     * @param status  Código de estado HTTP a retornar en la respuesta REST.
     * @param cause   Causa raíz de la excepción (útil para trazabilidad y depuración).
     */
    public BusinessException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    /**
     * Obtiene el estado HTTP asociado a la excepción.
     *
     * @return {@link HttpStatus} asignado a la excepción.
     */
    public HttpStatus getStatus() {
        return status;
    }
}