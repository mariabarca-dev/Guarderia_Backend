package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "propiedades_garage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropiedadGarage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_id", nullable = false)
    private Socio socio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id", nullable = false, unique = true)
    private Garage garage;

    @Column(name = "fecha_compra_garage", nullable = false)
    private LocalDate fechaCompraGarage;
}