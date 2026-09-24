package com.guarderiaCentral.guarderia_Backend.dtos;

import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import lombok.*;

/**
 * DTO limpio y simple para la entidad Empleado, heredando de UsuarioDTO.
 * Respeta la estructura estricta de la cátedra (sin JPA, sin lógica de mapeo, utilizando Lombok).
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EmpleadoDTO extends UsuarioDTO {

    private String codigo;
    private String especialidad;


}