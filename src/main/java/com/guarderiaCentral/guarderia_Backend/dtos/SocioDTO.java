package com.guarderiaCentral.guarderia_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * DTO limpio y simple para la entidad Socio, heredando de UsuarioDTO.
 * Respeta la estructura estricta de la cátedra (sin JPA, sin lógica de mapeo, utilizando Lombok).
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SocioDTO extends UsuarioDTO {

    private String dni;
    private LocalDate fechaIngreso;
}