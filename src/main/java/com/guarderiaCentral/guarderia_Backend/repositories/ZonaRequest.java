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
 * Objeto de solicitud para la creación o registro de una Zona.
 * Contiene Bean Validation para asegurar la integridad de los datos de entrada.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZonaRequest {

    @NotBlank(message = "La letra de la zona es obligatoria")
    @Size(max = 10, message = "La letra no puede superar los 10 caracteres")
    private String letra;

    @NotNull(message = "El tipo de vehículo de la zona es obligatorio")
    private TipoVehiculo tipoVehiculo;

    @NotNull(message = "La capacidad de vehículos es obligatoria")
    @Min(value = 1, message = "La capacidad de vehículos debe ser al menos 1")
    private Integer capacidadVehiculos;

    @NotNull(message = "El ancho del garage es obligatorio")
    @Min(value = 0, message = "El ancho del garage debe ser mayor o igual a 0")
    private Float anchoGarage;

    @NotNull(message = "El largo del garage es obligatorio")
    @Min(value = 0, message = "El largo del garage debe ser mayor o igual a 0")
    private Float largoGarage;
}
