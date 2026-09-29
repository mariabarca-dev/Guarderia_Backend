package com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de solicitud para crear una asignación entre un Empleado y una Zona.
 * Contiene IDs simples para las relaciones y validaciones de Bean Validation.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionEmpleadoZonaRequest {

    @NotNull(message = "El ID del empleado es obligatorio")
    private Integer empleadoId;

    @NotNull(message = "El ID de la zona es obligatorio")
    private Integer zonaId;

    @NotNull(message = "La cantidad de vehículos a cargo es obligatoria")
    @Min(value = 0, message = "La cantidad de vehículos a cargo no puede ser negativa")
    private Integer cantVehiculosACargo;
}