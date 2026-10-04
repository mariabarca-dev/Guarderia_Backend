package com.guarderiaCentral.guarderia_Backend.repositories.vehiculos;

import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de respuesta para exponer la información pública de un Vehículo,
 * incluyendo el ID de su socio relacionado.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoResponse {

    private Integer id;
    private Integer socioId;
    private String nombre;
    private String matricula;
    private TipoVehiculo tipo;
    private float profundidad;
    private float ancho;
    private Boolean activo;
}