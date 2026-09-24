package com.guarderiaCentral.guarderia_Backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO limpio y simple para la entidad Zona.
 * Respeta la estructura estricta de la cátedra (sin JPA, sin lógica de mapeo, utilizando Lombok).
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZonaDTO {

    private int id;
    private String letra;
}