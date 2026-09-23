package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un Garage dentro de una Zona.
 * Nota: La propiedad y fecha de compra con el socio se gestionan a través de PropiedadGarage.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Entity
@Table(name = "garages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Garage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "numero_garage", nullable = false)
    private int numeroGarage;

    @Column(name = "lectura_luz", nullable = false)
    private double lecturaLuz;

    @Column(name = "servicio_mantenimiento", nullable = false)
    private boolean servicioMantenimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}