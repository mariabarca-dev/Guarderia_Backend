package com.guarderiaCentral.guarderia_Backend.repositories.vehiculos;

import com.guarderiaCentral.guarderia_Backend.modelos.TipoVehiculo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Objeto de respuesta para exponer la información pública de un Vehículo,
 * incluyendo el ID de su socio relacionado y los datos legibles del socio (DNI, nombre y apellido).
 * Para los vehículos a cargo de un empleado agrega además la cochera y la zona que ocupan.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehiculoResponse {

    private Integer id;
    private Integer socioId;
    private String socioDni;
    private String socioNombre;
    private String socioApellido;
    private String nombre;
    private String matricula;
    private TipoVehiculo tipo;
    private float profundidad;
    private float ancho;
    private Boolean activo;
    /** Número de la cochera que ocupa (solo se completa en "vehículos a cargo de un empleado"; null en el resto). */
    private Integer garageNumero;
    /** Letra de la zona de esa cochera (solo se completa en "vehículos a cargo de un empleado"; null en el resto). */
    private String zonaLetra;
}