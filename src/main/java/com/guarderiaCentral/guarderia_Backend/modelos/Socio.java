package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entidad que representa a un Socio del sistema, heredando de Usuario.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Entity
@Table(name = "socios")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Getter
@Setter
@NoArgsConstructor
public class Socio extends Usuario {

    @Column(name = "dni", nullable = false, unique = true, length = 20)
    private String dni;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;
}