package com.guarderiaCentral.guarderia_Backend.repositories.asignacionEmpleadoZonas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de respuesta para exponer la información de la asignación Empleado-Zona,
 * exponiendo IDs o referencias limpias de las entidades relacionadas y los datos legibles del empleado
 * (código, nombre y apellido) y de la zona (letra).
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionEmpleadoZonaResponse {

    private Integer id;
    private Integer empleadoId;
    private String empleadoCodigo;
    private String empleadoNombre;
    private String empleadoApellido;
    private Integer zonaId;
    private String zonaLetra;
    private int cantVehiculosACargo;
    private Boolean activo;
}