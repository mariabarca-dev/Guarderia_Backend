package com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Objeto de solicitud para crear una asignación entre un Vehículo y un Garage.
 * Contiene IDs simples para las relaciones y validaciones de Bean Validation.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionVehiculoGarageRequest {

    @NotNull(message = "El ID del vehículo es obligatorio")
    private Integer vehiculoId;

    @NotNull(message = "El ID del garage es obligatorio")
    private Integer garageId;

    @NotNull(message = "La fecha de asignación del garage es obligatoria")
    private LocalDate fechaAsignacionGarage;
}