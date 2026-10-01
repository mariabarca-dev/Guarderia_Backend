package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando se intenta realizar una operación prohibida
 * sobre un usuario con rol SYSADMIN (por ejemplo, editarlo o eliminarlo indebidamente).
 */
public class SysAdminProtegidoException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public SysAdminProtegidoException() {
        super("No se permite realizar esta operación sobre un usuario con rol SYSADMIN.", HttpStatus.FORBIDDEN);
    }

    public SysAdminProtegidoException(String mensaje) {
        super(mensaje, HttpStatus.FORBIDDEN);
    }
}