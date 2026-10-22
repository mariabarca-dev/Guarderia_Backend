package com.guarderiaCentral.guarderia_Backend.repositories.empleados;

import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de respuesta para exponer la información pública de un Empleado.
 * El DNI es nulo si el empleado no lo tiene cargado.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoResponse {

    private Integer id;
    private String nombre;
    private String apellido;
    private String dni;
    private String direccion;
    private String telefono;
    private String nombreUsuario;
    private Rol rol;
    private String codigo;
    private String especialidad;
    private Boolean activo;
}