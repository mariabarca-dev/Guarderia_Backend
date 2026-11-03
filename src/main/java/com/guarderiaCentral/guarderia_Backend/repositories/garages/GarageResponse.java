package com.guarderiaCentral.guarderia_Backend.repositories.garages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Objeto de respuesta para exponer la información pública de un Garage, con el ID de su zona y la letra de
 * esa zona (dato legible que se muestra en lugar del ID).
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GarageResponse {

    private Integer id;
    private int numeroGarage;
    private double lecturaLuz;
    private boolean servicioMantenimiento;
    private BigDecimal cuotaGarage;
    private Integer zonaId;
    private String zonaLetra;
    private Boolean activo;
}