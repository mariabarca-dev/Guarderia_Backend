package com.guarderiaCentral.guarderia_Backend.repositories.socios;

import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Objeto de solicitud para la actualización de los datos de un Socio.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocioUpdate {

    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    private String apellido;

    @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
    private String direccion;

    @Size(max = 50, message = "El teléfono no puede superar los 50 caracteres")
    private String telefono;

    @Size(max = 50, message = "El nombre de usuario no puede superar los 50 caracteres")
    private String nombreUsuario;

    private String clave;

    private Rol rol;

    @Size(max = 20, message = "El DNI no puede superar los 20 caracteres")
    private String dni;

    private LocalDate fechaIngreso;
}