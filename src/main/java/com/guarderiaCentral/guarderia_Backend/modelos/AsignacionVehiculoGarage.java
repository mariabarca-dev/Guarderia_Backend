package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entidad asociativa que representa la relación 1 a 1 entre Vehiculo y Garage,
 * incluyendo la fecha de asignación.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Entity
@Table(name = "asignaciones_vehiculo_garage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionVehiculoGarage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id", nullable = false, unique = true)
    private Vehiculo vehiculo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false, unique = true)
    private Garage garage;

    @Column(name = "fecha_asignacion_garage", nullable = false)
    private LocalDate fechaAsignacionGarage;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}