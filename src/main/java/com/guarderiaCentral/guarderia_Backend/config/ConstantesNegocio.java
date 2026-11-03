package com.guarderiaCentral.guarderia_Backend.config;

import java.math.BigDecimal;
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
     * Tampoco pueden superar {@link #fechaMaximaPermitida()}.
     */
    public static final LocalDate FECHA_FUNDACION = LocalDate.of(1926, 1, 1);

    /**
     * Cantidad de años hacia adelante, contados desde hoy, que se permite en las fechas de ingreso de un socio,
     * de compra de un garage y de asignación de un vehículo a un garage. Permite registrar con anticipación
     * pero evita errores de carga (por ejemplo, un año mal tipeado).
     */
    public static final int ANIOS_MAXIMOS_HACIA_ADELANTE = 1;

    /** Valor base mensual de una cochera, igual para todas. Valor de ejemplo. */
    public static final BigDecimal VALOR_BASE_GARAGE = new BigDecimal("20000.00");

    /** Precio de cada kWh de la lectura del contador de luz. Valor de ejemplo. */
    public static final BigDecimal PRECIO_KWH = new BigDecimal("150.00");

    /** Importe mensual del servicio de mantenimiento, para las cocheras que lo tienen. Valor de ejemplo. */
    public static final BigDecimal VALOR_MANTENIMIENTO = new BigDecimal("5000.00");

    /** Cantidad máxima de meses que se pueden contratar en una compra. */
    public static final int MESES_MAXIMOS_CONTRATO = 12;

    /** Cantidad de meses a partir de la cual se aplica {@link #DESCUENTO_12_MESES}. */
    public static final int MESES_PROMO_12 = 12;

    /** Porcentaje de descuento por contratar 12 meses. */
    public static final int DESCUENTO_12_MESES = 20;

    /** Cantidad de meses a partir de la cual se aplica {@link #DESCUENTO_6_MESES}. */
    public static final int MESES_PROMO_6 = 6;

    /** Porcentaje de descuento por contratar de 6 a 11 meses. */
    public static final int DESCUENTO_6_MESES = 10;

    /**
     * Calcula la fecha máxima permitida para las fechas de ingreso, compra y asignación.
     *
     * @return La fecha de hoy más {@link #ANIOS_MAXIMOS_HACIA_ADELANTE} año(s).
     */
    public static LocalDate fechaMaximaPermitida() {
        return LocalDate.now().plusYears(ANIOS_MAXIMOS_HACIA_ADELANTE);
    }

    private ConstantesNegocio() {
        // Clase de constantes: no se instancia.
    }
}