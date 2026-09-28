package com.guarderiaCentral.guarderia_Backend.repositories;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Objeto de respuesta para exponer la información de la asignación Vehículo-Garage,
 * exponiendo IDs limpios de las entidades relacionadas.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionVehiculoGarageResponse {

    private Integer id;
    private Integer vehiculoId;
    private Integer garageId;
    private LocalDate fechaAsignacionGarage;
    private Boolean activo;
}
