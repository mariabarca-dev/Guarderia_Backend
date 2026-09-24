package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción base unificada para todos los errores de lógica de negocio del sistema.
 * <p>
 * Hereda de {@link RuntimeException} para permitir rollback automático en transacciones de Spring
 * y almacena el estado HTTP sugerido para la respuesta REST.
 * </p>
 *
 * @author Cátedra Guardería Central
 * @version 1.0
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Construye una excepción de negocio asociando un mensaje y un código HTTP 400 (BAD REQUEST) por defecto.
     *
     * @param message Mensaje descriptivo del error de negocio.
     */
    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    /**
     * Construye una excepción de negocio asociando un mensaje y un código HTTP específico.
     *
     * @param message Mensaje descriptivo del error de negocio.
     * @param status  Estado HTTP correspondiente al error.
     */
    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Construye una excepción de negocio asociando un mensaje, causa raíz y un código HTTP específico.
     *
     * @param message Mensaje descriptivo del error de negocio.
     * @param cause   Causa original de la excepción.
     * @param status  Estado HTTP correspondiente al error.
     */
    public BusinessException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    /**
     * Obtiene el estado HTTP asociado a la excepción.
     *
     * @return {@link HttpStatus} asignado.
     */
    public HttpStatus getStatus() {
        return status;
    }
}

