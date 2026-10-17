package com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Objeto de solicitud para actualizar los datos de la propiedad de un garage.
 * El socio y el garage son obligatorios; la fecha de compra es opcional y, si no se envía, se conserva la actual.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropiedadGarageUpdate {

    @NotNull(message = "El ID del socio es obligatorio")
    private Integer socioId;

    @NotNull(message = "El ID del garage es obligatorio")
    private Integer garageId;

    private LocalDate fechaCompraGarage;
}