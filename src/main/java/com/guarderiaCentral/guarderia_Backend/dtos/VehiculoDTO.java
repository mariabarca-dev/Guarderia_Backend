package com.guarderiaCentral.guarderia_Backend.dtos;

import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO limpio y simple para la entidad Vehiculo.
 * Respeta la estructura estricta de la cátedra (sin JPA, sin lógica de mapeo, utilizando Lombok)
 * y emplea únicamente los IDs de las entidades relacionadas (socio y empleado).
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoDTO {

    private int id;
    private int socioId;
    private int empleadoId;
    private String nombre;
    private String matricula;
    private TipoVehiculo tipo;
    private float profundidad;
    private float ancho;
}