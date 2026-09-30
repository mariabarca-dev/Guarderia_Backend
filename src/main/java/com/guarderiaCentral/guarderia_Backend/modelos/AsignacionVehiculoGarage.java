package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entidad asociativa que representa la relación 1 a 1 entre Vehiculo y Garage,
 * incluyendo la fecha de asignación. La unicidad se garantiza entre asignaciones
 * activas desde la capa de servicio, para no bloquear reasignaciones tras un borrado lógico.
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
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false)
    private Garage garage;

    @Column(name = "fecha_asignacion_garage", nullable = false)
    private LocalDate fechaAsignacionGarage;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}