package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una Zona dentro de la guardería,
 * la cual agrupa garages con características y tipo de vehículo específicos.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Entity
@Table(name = "zonas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Zona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "letra", nullable = false, unique = true, length = 10)
    private String letra;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vehiculo", nullable = false, length = 30)
    private TipoVehiculo tipoVehiculo;

    @Column(name = "capacidad_vehiculos", nullable = false)
    private int capacidadVehiculos;

    @Column(name = "ancho_garage", nullable = false)
    private float anchoGarage;

    @Column(name = "largo_garage", nullable = false)
    private float largoGarage;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    /**
     * Asegura que la letra se almacene siempre en mayúsculas.
     */
    public void setLetra(String letra) {
        this.letra = (letra != null) ? letra.trim().toUpperCase() : null;
    }
}