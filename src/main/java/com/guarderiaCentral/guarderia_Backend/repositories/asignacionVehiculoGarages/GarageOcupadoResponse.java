package com.guarderiaCentral.guarderia_Backend.repositories.asignacionVehiculoGarages;

import com.guarderiaCentral.guarderia_Backend.repositories.garages.GarageResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Objeto de respuesta que combina un garage ocupado con los datos de la asignación que lo ocupa.
 * Lo usa el socio para consultar dónde están estacionados sus vehículos: el garage
 * (número, lectura de luz, servicio de mantenimiento y zona) y la fecha de asignación.
 * Reutiliza {@link GarageResponse} para no repetir los campos del garage.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GarageOcupadoResponse {

    private Integer asignacionId;
    private Integer vehiculoId;
    private GarageResponse garage;
    private LocalDate fechaAsignacionGarage;
}