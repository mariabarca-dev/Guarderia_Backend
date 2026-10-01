package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando se intenta registrar o actualizar un vehículo con una
 * matrícula (patente/dominio) que ya existe registrada y activa en el sistema.
 * <p>
 * Extiende de {@link BusinessException} para integrarse con el esquema global de excepciones
 * de la aplicación, siendo capturada por el {@code GlobalExceptionHandler} para retornar
 * siempre una respuesta HTTP con código 409 CONFLICT.
 * </p>
 *
 * @author Guardería Central
 * @version 2.0
 */
public class MatriculaDuplicadaException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto con mensaje predeterminado.
     * Asocia la excepción al estado HTTP 409 (CONFLICT).
     */
    public MatriculaDuplicadaException() {
        super("La matrícula ingresada ya se encuentra registrada en el sistema.", HttpStatus.CONFLICT);
    }

    /**
     * Constructor que permite definir un mensaje de error personalizado.
     * Siempre asocia la excepción al estado HTTP 409 (CONFLICT).
     *
     * @param mensaje Detalle explicativo de la razón de la excepción.
     */
    public MatriculaDuplicadaException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }
}