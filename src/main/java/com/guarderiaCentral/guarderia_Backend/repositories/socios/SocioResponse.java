package com.guarderiaCentral.guarderia_Backend.repositories.socios;

import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Objeto de respuesta para exponer la información pública de un Socio.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocioResponse {

    private Integer id;
    private String nombre;
    private String apellido;
    private String direccion;
    private String telefono;
    private String nombreUsuario;
    private Rol rol;
    private String dni;
    private LocalDate fechaIngreso;
    private Boolean activo;
}
