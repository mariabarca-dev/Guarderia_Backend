package com.guarderiaCentral.guarderia_Backend.repositories;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de solicitud para la actualización de los datos de un Garage existente.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GarageUpdate {

    @Min(value = 1, message = "El número de garage debe ser mayor a 0")
    private Integer numeroGarage;

    private Double lecturaLuz;

    private Boolean servicioMantenimiento;

    private Integer zonaId;
}
