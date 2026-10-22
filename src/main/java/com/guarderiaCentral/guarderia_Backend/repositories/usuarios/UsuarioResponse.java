package com.guarderiaCentral.guarderia_Backend.repositories.usuarios;

import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de respuesta que expone los datos comunes de cualquier cuenta de usuario
 * (socio, empleado, administrador o sysadmin). Nunca incluye la clave.
 * El DNI es nulo en las cuentas que no lo tienen cargado.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {

    private Integer id;
    private String nombre;
    private String apellido;
    private String dni;
    private String nombreUsuario;
    private Rol rol;
    private Boolean activo;
}