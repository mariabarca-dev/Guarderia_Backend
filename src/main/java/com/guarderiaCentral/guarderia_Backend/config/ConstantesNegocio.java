package com.guarderiaCentral.guarderia_Backend.config;

import java.time.LocalDate;

/**
 * Constantes de las reglas de negocio que comparten varios services, para escribirlas en un solo lugar.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
public final class ConstantesNegocio {

    /**
     * Fecha de fundación de la Guardería Central. Ninguna fecha de ingreso de un socio, de asignación de un
     * vehículo a un garage ni de compra de un garage puede ser anterior a ella.
     */
    public static final LocalDate FECHA_FUNDACION = LocalDate.of(1926, 1, 1);

    private ConstantesNegocio() {
        // Clase de constantes: no se instancia.
    }
}