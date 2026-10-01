package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entidad que representa un Vehículo registrado perteneciente a un Socio.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Entity
@Table(name = "vehiculos")
@SQLRestriction("activo = true")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

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