package com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Objeto de solicitud para actualizar los datos de la propiedad de un garage.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropiedadGarageUpdate {

    private Integer socioId;

    private Integer garageId;

    private LocalDate fechaCompraGarage;
}
