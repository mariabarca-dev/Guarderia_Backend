package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Entidad que representa a un Socio del sistema, heredando de Usuario.
 *
 * @author Franco Buyatti, Daniela Forclaz, Héctor Machaca, María Eugenia Barca
 */
@Entity
@Table(name = "socios")
@PrimaryKeyJoinColumn(name = "usuario_id")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Socio extends Usuario {

    @Column(name = "dni", nullable = false, unique = true, length = 20)
    private String dni;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;
}