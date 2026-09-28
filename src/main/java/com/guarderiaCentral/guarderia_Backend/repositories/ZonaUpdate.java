package com.guarderiaCentral.guarderia_Backend.repositories;

import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de solicitud para la actualización de los datos de una Zona existente.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZonaUpdate {

    @Size(max = 10, message = "La letra no puede superar los 10 caracteres")
    private String letra;

    private TipoVehiculo tipoVehiculo;

    @Min(value = 1, message = "La capacidad de vehículos debe ser al menos 1")
    private Integer capacidadVehiculos;

    @Min(value = 0, message = "El ancho del garage debe ser mayor o igual a 0")
    private Float anchoGarage;

    @Min(value = 0, message = "El largo del garage debe ser mayor o igual a 0")
    private Float largoGarage;
}
