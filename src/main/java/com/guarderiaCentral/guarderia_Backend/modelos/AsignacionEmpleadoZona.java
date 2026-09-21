package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "asignaciones_empleado_zona",
        uniqueConstraints = @UniqueConstraint(columnNames = {"empleado_id", "zona_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionEmpleadoZona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @Column(name = "cant_vehiculos_a_cargo", nullable = false)
    private int cantVehiculosACargo;
}