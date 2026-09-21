package com.guarderiaCentral.guarderia_Backend.modelos;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "socios")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Socio extends Usuario {

    @Column(nullable = false, unique = true, length = 15)
    private String dni;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;
}