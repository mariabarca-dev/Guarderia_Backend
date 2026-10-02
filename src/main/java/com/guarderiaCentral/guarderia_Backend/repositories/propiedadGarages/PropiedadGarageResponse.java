package com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Objeto de respuesta para exponer la información de la propiedad de garage,
 * exponiendo IDs limpios de las entidades relacionadas.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropiedadGarageResponse {

    private Integer id;
    private Integer socioId;
    private Integer garageId;
    private LocalDate fechaCompraGarage;
    private Boolean activo;
}