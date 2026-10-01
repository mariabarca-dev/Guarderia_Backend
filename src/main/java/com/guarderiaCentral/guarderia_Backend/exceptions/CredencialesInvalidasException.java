package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción personalizada para manejar intentos fallidos de inicio de sesión
 * debido a credenciales inválidas (usuario no encontrado o contraseña incorrecta).
 * <p>
 * Extiende de {@link BusinessException} asignando siempre el estado HTTP
 * {@link HttpStatus#UNAUTHORIZED} (401 Unauthorized).
 * </p>
 *
 * @author Cátedra Guardería Central
 * @version 2.0
 */
public class CredencialesInvalidasException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto que asigna un mensaje estándar de credenciales inválidas
     * y el estado HTTP 401 (UNAUTHORIZED).
     */
    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos.", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Constructor que permite especificar un mensaje descriptivo personalizado
     * manteniendo el estado HTTP 401 (UNAUTHORIZED).
     *
     * @param message Mensaje descriptivo del error de autenticación.
     */
    public CredencialesInvalidasException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Constructor que permite especificar un mensaje personalizado y la causa raíz del error,
     * manteniendo el estado HTTP 401 (UNAUTHORIZED).
     *
     * @param message Mensaje descriptivo del error.
     * @param cause   Causa raíz de la excepción para trazabilidad y depuración.
     */
    public CredencialesInvalidasException(String message, Throwable cause) {
        super(message, HttpStatus.UNAUTHORIZED, cause);
    }
}