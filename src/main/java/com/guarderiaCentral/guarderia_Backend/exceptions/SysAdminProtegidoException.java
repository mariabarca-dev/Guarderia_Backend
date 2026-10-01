package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando se intenta realizar una operación prohibida
 * sobre un usuario con rol SYSADMIN (por ejemplo, editarlo o eliminarlo indebidamente).
 * <p>
 * Extiende de {@link BusinessException} asignando siempre el estado HTTP
 * {@link HttpStatus#FORBIDDEN} (403 Forbidden).
 * </p>
 *
 * @author Cátedra Guardería Central
 * @version 2.0
 */
public class SysAdminProtegidoException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto con mensaje predeterminado.
     * Asocia la excepción con un código HTTP 403 FORBIDDEN.
     */
    public SysAdminProtegidoException() {
        super("No se permite realizar esta operación sobre un usuario con rol SYSADMIN.", HttpStatus.FORBIDDEN);
    }

    /**
     * Constructor que permite definir un mensaje de error personalizado.
     * Siempre asocia la excepción con un código HTTP 403 FORBIDDEN.
     *
     * @param mensaje Detalle explicativo de la razón de la excepción.
     */
    public SysAdminProtegidoException(String mensaje) {
        super(mensaje, HttpStatus.FORBIDDEN);
    }
}