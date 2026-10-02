package com.guarderiaCentral.guarderia_Backend.repositories.garages;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de solicitud para la creación o registro de un Garage.
 * Contiene Bean Validation y el ID de la zona asociada.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GarageRequest {

    @NotNull(message = "El número de garage es obligatorio")
    @Min(value = 1, message = "El número de garage debe ser mayor a 0")
    private Integer numeroGarage;

    @NotNull(message = "La lectura de luz es obligatoria")
    private Double lecturaLuz;

    @NotNull(message = "El estado del servicio de mantenimiento es obligatorio")
    private Boolean servicioMantenimiento;

    @NotNull(message = "El ID de la zona es obligatorio")
    private Integer zonaId;
}