package com.guarderiaCentral.guarderia_Backend.repositories.vehiculos;

import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de solicitud para la actualización de los datos de un Vehículo existente.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoUpdate {

    private Integer socioId;

    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @Size(max = 50, message = "La matrícula no puede superar los 50 caracteres")
    private String matricula;

    private TipoVehiculo tipo;

    @Min(value = 0, message = "La profundidad debe ser mayor o igual a 0")
    private Float profundidad;

    @Min(value = 0, message = "El ancho debe ser mayor o igual a 0")
    private Float ancho;
}