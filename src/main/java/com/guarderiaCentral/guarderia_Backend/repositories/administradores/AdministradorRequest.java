package com.guarderiaCentral.guarderia_Backend.repositories.administradores;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de solicitud para la creación o registro de un Administrador.
 * Contiene las validaciones de Bean Validation correspondientes.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barcat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdministradorRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    private String apellido;

    @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
    private String direccion;

    @Size(max = 50, message = "El teléfono no puede superar los 50 caracteres")
    private String telefono;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 50, message = "El nombre de usuario no puede superar los 50 caracteres")
    private String nombreUsuario;

    @NotBlank(message = "La clave es obligatoria")
    private String clave;
}