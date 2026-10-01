package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando se intenta eliminar o dar de baja un registro
 * que cuenta con dependencias activas asociadas (ej. un Socio con vehículos o una Zona con garajes).
 */
public class DependenciasActivasException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public DependenciasActivasException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }

    public DependenciasActivasException() {
        super("No se puede completar la baja debido a la existencia de dependencias activas asociadas.", HttpStatus.CONFLICT);
    }
}
