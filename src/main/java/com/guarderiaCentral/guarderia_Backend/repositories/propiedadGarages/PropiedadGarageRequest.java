package com.guarderiaCentral.guarderia_Backend.repositories.propiedadGarages;

import com.guarderiaCentral.guarderia_Backend.modelos.MetodoPago;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @NotNull(message = "La cantidad de meses a contratar es obligatoria")
    @Min(value = 1, message = "Se debe contratar al menos 1 mes")
    @Max(value = 12, message = "Se pueden contratar hasta 12 meses")
    private Integer mesesContratados;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;
}