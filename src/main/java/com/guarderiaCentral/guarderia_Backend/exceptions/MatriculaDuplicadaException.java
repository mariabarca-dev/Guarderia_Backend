package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando se intenta registrar o actualizar un vehículo con una
 * matrícula (patente/dominio) que ya existe registrada y activa en el sistema.
 * <p>
 * Extiende de {@link BusinessException} para integrarse con el esquema global de excepciones
 * de la aplicación, siendo capturada por el {@code GlobalExceptionHandler} para retornar
 * una respuesta HTTP con código 409 CONFLICT por defecto.
 * </p>
 *
 * @author Guardería Central
 * @version 1.0
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
     * Constructor que permite especificar un mensaje de error personalizado.
     *
     * @param mensaje Detalle explicativo del motivo del conflicto de matrícula duplicada.
     */
    public MatriculaDuplicadaException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }

    /**
     * Constructor avanzado que permite establecer un mensaje de error y un estado HTTP específico.
     *
     * @param mensaje Detalle explicativo de la razón de la excepción.
     * @param status Estado HTTP a retornar en la respuesta REST de la API.
     */
    public MatriculaDuplicadaException(String mensaje, HttpStatus status) {
        super(mensaje, status);
    }
}