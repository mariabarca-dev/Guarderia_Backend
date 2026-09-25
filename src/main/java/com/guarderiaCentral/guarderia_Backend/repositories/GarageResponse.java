package com.guarderiaCentral.guarderia_Backend.repositories;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de respuesta para exponer la información pública de un Garage,
 * incluyendo el ID de su zona relacionada.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GarageResponse {

    private int id;
    private int numeroGarage;
    private double lecturaLuz;
    private boolean servicioMantenimiento;
    private int zonaId;
    private Boolean activo;
}
