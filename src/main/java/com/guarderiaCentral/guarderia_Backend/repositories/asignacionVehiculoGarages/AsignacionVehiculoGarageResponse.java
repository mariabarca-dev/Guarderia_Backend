package com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Objeto de respuesta para exponer la información de la asignación Vehículo-Garage,
 * exponiendo IDs limpios de las entidades relacionadas y los datos legibles del vehículo (matrícula),
 * del garage (número) y de su zona (letra).
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionVehiculoGarageResponse {

    private Integer id;
    private Integer vehiculoId;
    private String vehiculoMatricula;
    private Integer garageId;
    private Integer garageNumero;
    private String zonaLetra;
    private LocalDate fechaAsignacionGarage;
    private Boolean activo;
}