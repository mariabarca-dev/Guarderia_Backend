package com.guarderiaCentral.guarderia_Backend.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando se intenta eliminar o dar de baja un registro
 * que cuenta con dependencias activas asociadas (ej. un Socio con vehículos o una Zona con garajes).
 * <p>
 * Extiende de {@link BusinessException} asignando siempre el estado HTTP
 * {@link HttpStatus#CONFLICT} (409 Conflict).
 * </p>
 *
 * @author Cátedra Guardería Central
 * @version 2.0
 */
public class DependenciasActivasException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor que permite definir un mensaje de error personalizado,
     * por ejemplo indicando qué dependencias impiden la baja.
     * Siempre asocia la excepción con un código HTTP 409 CONFLICT.
     *
     * @param mensaje Detalle explicativo de la razón de la excepción.
     */
    public DependenciasActivasException(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT);
    }

    /**
     * Constructor por defecto con mensaje predeterminado.
     * Asocia la excepción con un código HTTP 409 CONFLICT.
     */
    public DependenciasActivasException() {
        super("No se puede completar la baja debido a la existencia de dependencias activas asociadas.", HttpStatus.CONFLICT);
    }
}