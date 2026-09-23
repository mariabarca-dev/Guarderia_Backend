package com.guarderiaCentral.guarderia_Backend.dtos;

import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * DTO que representa a un Administrador del sistema.
 * Extiende de {@link UsuarioDTO} y no contiene atributos propios ni lógica JPA.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AdministradorDTO extends UsuarioDTO {

    /**
     * Constructor completo que delega todos los atributos al constructor de la clase padre.
     */
    public AdministradorDTO(int id, String nombre, String apellido, String direccion,
                            String telefono, String nombreUsuario, String clave, Rol rol, Boolean activo) {
        super(id, nombre, apellido, direccion, telefono, nombreUsuario, clave, rol, activo);
    }
}