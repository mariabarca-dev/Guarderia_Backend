package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entidad asociativa que representa la propiedad de un Garage por parte de un Socio,
 * incluyendo la fecha de compra.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Entity
@Table(name = "propiedades_garage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropiedadGarage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false, unique = true)
    private Garage garage;

    @Column(name = "fecha_compra_garage", nullable = false)
    private LocalDate fechaCompraGarage;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}