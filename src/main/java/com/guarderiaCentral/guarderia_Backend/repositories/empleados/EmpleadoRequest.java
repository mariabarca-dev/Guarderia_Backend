package com.guarderiaCentral.guarderia_Backend.repositories.empleados;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de solicitud para la creación o registro de un Empleado.
 * Contiene los campos heredados de usuario y los específicos de empleado, junto con Bean Validation.
 * El DNI es opcional. Todos los campos de texto, incluida la clave, solo admiten caracteres válidos,
 * por lo que no aceptan scripts ni caracteres de marcado.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmpleadoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Pattern(regexp = "^[\\p{L}][\\p{L} .'’-]*$",
            message = "El nombre solo puede contener letras, espacios, punto, apóstrofe y guion")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    @Pattern(regexp = "^[\\p{L}][\\p{L} .'’-]*$",
            message = "El apellido solo puede contener letras, espacios, punto, apóstrofe y guion")
    private String apellido;

    @Size(max = 20, message = "El DNI no puede superar los 20 caracteres")
    @Pattern(regexp = "^[0-9A-Za-z.-]+$",
            message = "El DNI solo puede contener letras, números, punto y guion")
    private String dni;

    @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
    @Pattern(regexp = "^[\\p{L}\\p{N} .,#°ºª/()'’-]*$",
            message = "La dirección contiene caracteres no permitidos")
    private String direccion;

    @Size(max = 50, message = "El teléfono no puede superar los 50 caracteres")
    @Pattern(regexp = "^[0-9+() -]*$",
            message = "El teléfono solo puede contener números, espacios, +, paréntesis y guiones")
    private String telefono;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 50, message = "El nombre de usuario no puede superar los 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$",
            message = "El nombre de usuario solo puede contener letras, números, punto, guion y guion bajo")
    private String nombreUsuario;

    @NotBlank(message = "La clave es obligatoria")
    @Size(max = 100, message = "La clave no puede superar los 100 caracteres")
    @Pattern(regexp = "^[^<>]*$", message = "La clave no puede contener los caracteres < o >")
    private String clave;

    @NotBlank(message = "El código de empleado es obligatorio")
    @Size(max = 50, message = "El código no puede superar los 50 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$",
            message = "El código solo puede contener letras, números, punto, guion y guion bajo")
    private String codigo;

    @Size(max = 100, message = "La especialidad no puede superar los 100 caracteres")
    @Pattern(regexp = "^[\\p{L}\\p{N} .,/()'’-]*$",
            message = "La especialidad contiene caracteres no permitidos")
    private String especialidad;
}