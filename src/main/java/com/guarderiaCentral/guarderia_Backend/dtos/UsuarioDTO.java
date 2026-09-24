package com.guarderiaCentral.guarderia_Backend.dtos;

import com.guarderiaCentral.guarderia_Backend.modelos.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO limpio y simple para la entidad base Usuario, respetando la estructura
 * estricta de la cátedra (sin JPA, sin lógica de mapeo, utilizando Lombok).
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private int id;
    private String nombre;
    private String apellido;
    private String direccion;
    private String telefono;
    private String nombreUsuario;
    private String clave;
    private Rol rol;
    private Boolean activo;
}

