package com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages;

import com.guarderiaCentral.guarderia_Backend.modelos.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Objeto de respuesta para exponer la información de la propiedad de garage,
 * exponiendo IDs limpios de las entidades relacionadas y los datos legibles del socio (DNI, nombre y apellido)
 * y del garage (número). En "mis propiedades" del socio indica además quién ocupa la cochera.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropiedadGarageResponse {

    private Integer id;
    private Integer socioId;
    private String socioDni;
    private String socioNombre;
    private String socioApellido;
    private Integer garageId;
    private Integer garageNumero;
    private LocalDate fechaCompraGarage;
    private Integer mesesContratados;
    private MetodoPago metodoPago;
    /** Valor de la compra y promoción aplicada; null si la cochera ya no está disponible. */
    private PresupuestoCompraResponse presupuesto;
    private Boolean activo;
    /** Matrícula del rodado que ocupa la cochera (solo se completa en "mis propiedades" del socio; null si está libre). */
    private String ocupadaPorMatricula;
    /** true si el rodado que la ocupa es del propio socio, false si es de otro socio; null si está libre. */
    private Boolean ocupadaPorPropio;
}