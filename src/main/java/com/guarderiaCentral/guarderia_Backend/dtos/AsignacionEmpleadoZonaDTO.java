package com.guarderiaCentral.guarderia_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO limpio y simple para la entidad asociativa AsignacionEmpleadoZona,
 * respetando la regla estricta de la cátedra de no incluir entidades completas
 * ni otros DTOs anidados, utilizando únicamente IDs para las relaciones.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionEmpleadoZonaDTO {

    private int id;
    private int empleadoId;
    private int zonaId;
    private int cantVehiculosACargo;
    private Boolean activo;
}