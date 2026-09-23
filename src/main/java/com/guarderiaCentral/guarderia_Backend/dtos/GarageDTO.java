package com.guarderiaCentral.guarderia_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO limpio y simple para la entidad Garage.
 * Excluye estrictamente socioPropietario y fechaCompra por pertenecer exclusivamente
 * a la entidad asociativa PropiedadGarage, utilizando únicamente el ID de la zona relacionada.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GarageDTO {

    private int id;
    private int numeroGarage;
    private float lecturaLuz;
    private boolean servicioMantenimiento;
    private int zonaId;
}