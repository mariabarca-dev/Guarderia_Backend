package com.guarderiaCentral.guarderia_Backend.repositories.administradores;

import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de respuesta para exponer la información pública de un Administrador.
 * El DNI es nulo si el administrador no lo tiene cargado.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barcat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdministradorResponse {

    private Integer id;
    private String nombre;
    private String apellido;
    private String dni;
    private String direccion;
    private String telefono;
    private String nombreUsuario;
    private Rol rol;
    private Boolean activo;
}