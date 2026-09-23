package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un Vehículo registrado perteneciente a un Socio
 * y gestionado/registrado por un Empleado responsable.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Entity
@Table(name = "vehiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(name = "nombre", length = 100)
    private String nombre;

    @Column(name = "matricula", nullable = false, unique = true, length = 50)
    private String matricula;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoVehiculo tipo;

    @Column(name = "profundidad", nullable = false)
    private float profundidad;

    @Column(name = "ancho", nullable = false)
    private float ancho;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}