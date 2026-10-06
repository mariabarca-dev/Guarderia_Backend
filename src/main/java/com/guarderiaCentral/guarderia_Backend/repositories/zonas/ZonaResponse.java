package com.guarderiaCentral.guarderia_Backend.repositories.zonas;

import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de respuesta para exponer la información pública de una Zona,
 * incluyendo su estado actual de disponibilidad.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZonaResponse {

    private Integer id;
    private String letra;
    private TipoVehiculo tipoVehiculo;
    private int capacidadVehiculos;
    private float anchoGarage;
    private float largoGarage;
    private Boolean activo;

    // Campos calculados para reportar disponibilidad a la vista o frontend
    private int espaciosDisponibles;
    private int espaciosOcupados;
}