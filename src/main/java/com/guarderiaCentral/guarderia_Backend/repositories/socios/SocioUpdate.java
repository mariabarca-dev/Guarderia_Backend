package com.guarderiaCentral.guarderia_Backend.repositories.socios;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Objeto de solicitud para la actualización de los datos de un Socio.
 * Un campo nulo significa "no cambiar". El DNI, si se envía, no puede estar vacío ni en blanco: un socio
 * siempre conserva su DNI. Todos los campos de texto, incluida la clave, solo admiten caracteres válidos,
 * por lo que no aceptan scripts ni caracteres de marcado.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 * @version 1.2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocioUpdate {

    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Pattern(regexp = "^[\\p{L}][\\p{L} .'’-]*$",
            message = "El nombre solo puede contener letras, espacios, punto, apóstrofe y guion")
    private String nombre;

    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    @Pattern(regexp = "^[\\p{L}][\\p{L} .'’-]*$",
            message = "El apellido solo puede contener letras, espacios, punto, apóstrofe y guion")
    private String apellido;

    @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
    @Pattern(regexp = "^[\\p{L}\\p{N} .,#°ºª/()'’-]*$",
            message = "La dirección contiene caracteres no permitidos")
    private String direccion;

    @Size(max = 50, message = "El teléfono no puede superar los 50 caracteres")
    @Pattern(regexp = "^[0-9+() -]*$",
            message = "El teléfono solo puede contener números, espacios, +, paréntesis y guiones")
    private String telefono;

    @Size(max = 50, message = "El nombre de usuario no puede superar los 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$",
            message = "El nombre de usuario solo puede contener letras, números, punto, guion y guion bajo")
    private String nombreUsuario;

    @Size(max = 100, message = "La clave no puede superar los 100 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "La clave no puede contener los caracteres < o >")
    private String clave;

    @Size(max = 20, message = "El DNI no puede superar los 20 caracteres")
    @Pattern(regexp = "^[0-9A-Za-z.-]+$",
            message = "El DNI solo puede contener letras, números, punto y guion")
    private String dni;

    private LocalDate fechaIngreso;
}