package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad asociativa que representa la relación N a N entre Empleado y Zona,
 * incluyendo la cantidad de vehículos a cargo.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Entity
@Table(name = "asignaciones_empleado_zona")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionEmpleadoZona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @Column(name = "cant_vehiculos_a_cargo", nullable = false)
    private int cantVehiculosACargo;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}