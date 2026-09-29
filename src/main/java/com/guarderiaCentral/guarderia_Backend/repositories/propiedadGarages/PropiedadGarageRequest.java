package com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Objeto de solicitud para registrar la propiedad de un garage por parte de un socio.
 * Contiene IDs simples para las relaciones y validaciones de Bean Validation.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropiedadGarageRequest {

    @NotNull(message = "El ID del socio es obligatorio")
    private Integer socioId;

    @NotNull(message = "El ID del garage es obligatorio")
    private Integer garageId;

    @NotNull(message = "La fecha de compra del garage es obligatoria")
    private LocalDate fechaCompraGarage;
}
