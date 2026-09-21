package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "garages")
@Data
@NoArgsConstructor
public class Garage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_garage", nullable = false, unique = true)
    private int numeroGarage;

    @Column(name = "lectura_luz", nullable = false)
    private double lecturaLuz;

    @Column(name = "servicio_mantenimiento", nullable = false)
    private boolean servicioMantenimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "socio_propietario_id")
    private Socio socioPropietario; // null = garage libre

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra; // null = todavia no vendido

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id", nullable = false)
    private Zona zona;

    @Transient
    public boolean isVendido() {
        return socioPropietario != null;
    }
}