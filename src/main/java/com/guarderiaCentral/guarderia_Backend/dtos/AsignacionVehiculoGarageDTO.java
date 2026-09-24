package com.guarderiaCentral.guarderia_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO limpio y simple para la entidad asociativa AsignacionVehiculoGarage,
 * respetando la regla estricta de la cátedra de no incluir entidades completas
 * ni otros DTOs anidados, utilizando únicamente IDs para las relaciones.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionVehiculoGarageDTO {

    private int id;
    private int vehiculoId;
    private int garageId;
    private LocalDate fechaAsignacionGarage;
    private Boolean activo;
}