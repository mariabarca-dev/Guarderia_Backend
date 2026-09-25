package com.guarderiaCentral.guarderia_Backend.repositories;

import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de respuesta para exponer la información pública de un Empleado.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoResponse {

    private int id;
    private String nombre;
    private String apellido;
    private String direccion;
    private String telefono;
    private String nombreUsuario;
    private Rol rol;
    private String codigo;
    private String especialidad;
    private Boolean activo;
}
