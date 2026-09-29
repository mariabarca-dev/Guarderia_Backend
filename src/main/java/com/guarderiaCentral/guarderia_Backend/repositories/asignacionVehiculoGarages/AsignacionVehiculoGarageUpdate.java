package com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Objeto de solicitud para actualizar los datos de la asignación entre un Vehículo y un Garage.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionVehiculoGarageUpdate {

    private Integer vehiculoId;

    private Integer garageId;

    private LocalDate fechaAsignacionGarage;
}
