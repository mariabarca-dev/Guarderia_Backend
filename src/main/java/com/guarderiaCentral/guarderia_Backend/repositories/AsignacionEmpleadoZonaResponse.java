package com.guarderiaCentral.guarderia_Backend.repositories;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de respuesta para exponer la información de la asignación Empleado-Zona,
 * exponiendo IDs o referencias limpias de las entidades relacionadas.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionEmpleadoZonaResponse {

    private Integer id;
    private int empleadoId;
    private Integer zonaId;
    private int cantVehiculosACargo;
    private Boolean activo;
}