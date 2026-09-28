package com.guarderiaCentral.guarderia_Backend.repositories;

import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de solicitud para la creación o registro de un Vehículo.
 * Contiene Bean Validation y los IDs de las entidades relacionadas (Socio y Empleado).
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoRequest {

    @NotNull(message = "El ID del socio es obligatorio")
    private Integer socioId;

    @NotNull(message = "El ID del empleado es obligatorio")
    private Integer empleadoId;

    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotBlank(message = "La matrícula es obligatoria")
    @Size(max = 50, message = "La matrícula no puede superar los 50 caracteres")
    private String matricula;

    @NotNull(message = "El tipo de vehículo es obligatorio")
    private TipoVehiculo tipo;

    @NotNull(message = "La profundidad es obligatoria")
    @Min(value = 0, message = "La profundidad debe ser mayor o igual a 0")
    private Float profundidad;

    @NotNull(message = "El ancho es obligatorio")
    @Min(value = 0, message = "El ancho debe ser mayor o igual a 0")
    private Float ancho;
}
