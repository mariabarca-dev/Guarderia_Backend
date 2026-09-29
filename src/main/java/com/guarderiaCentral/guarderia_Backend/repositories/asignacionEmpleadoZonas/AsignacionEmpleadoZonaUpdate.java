package com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de solicitud para actualizar la asignación entre un Empleado y una Zona.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionEmpleadoZonaUpdate {

    private Integer empleadoId;

    private Integer zonaId;

    @Min(value = 0, message = "La cantidad de vehículos a cargo no puede ser negativa")
    private Integer cantVehiculosACargo;
}